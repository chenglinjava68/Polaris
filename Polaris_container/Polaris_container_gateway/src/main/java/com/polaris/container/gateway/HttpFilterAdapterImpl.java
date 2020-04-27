package com.polaris.container.gateway;

import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.util.Map;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.polaris.container.gateway.pojo.HostUpstream;
import com.polaris.container.gateway.proxy.HttpFiltersAdapter;
import com.polaris.container.gateway.proxy.impl.ClientToProxyConnection;
import com.polaris.container.gateway.proxy.impl.ProxyToServerConnection;
import com.polaris.container.gateway.request.HttpRequestFilter;
import com.polaris.container.gateway.request.HttpRequestFilterChain;
import com.polaris.container.gateway.response.HttpResponseFilter;
import com.polaris.container.gateway.response.HttpResponseFilterChain;
import com.polaris.container.gateway.util.RequestUtil;
import com.polaris.core.Constant;
import com.polaris.core.naming.provider.ServerStrategyProviderFactory;
import com.polaris.core.util.ResultUtil;
import com.polaris.core.util.StringUtil;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.CharsetUtil;

/**
 * @author:Tom.Yu
 *
 * Description:
 *
 */
public class HttpFilterAdapterImpl extends HttpFiltersAdapter {
	private static Logger logger = LoggerFactory.getLogger(HttpFilterAdapterImpl.class);

    public HttpFilterAdapterImpl(HttpRequest originalRequest, ChannelHandlerContext ctx) {
        super(originalRequest, ctx);
    }

    @Override
    public HttpResponse clientToProxyRequest(HttpObject httpObject) {
    	
        HttpResponse httpResponse = null;
        try {
        	        	
        	//request init
        	if (httpObject instanceof HttpRequest) {
        		RequestUtil.remove();
        	}
        	
        	//进入request过滤器
            ImmutablePair<Boolean, HttpRequestFilter> immutablePair = HttpRequestFilterChain.INSTANCE.doFilter(originalRequest, httpObject, ctx);
            
            //过滤不通过的直接进入response过滤器
            if (immutablePair.left) {
                httpResponse = createResponse(immutablePair.right.getStatus(), originalRequest, immutablePair.right.getResult(),immutablePair.right.getHeaderMap());
            }
        } catch (Exception e) {
            httpResponse = createResponse(HttpResponseStatus.BAD_GATEWAY, originalRequest, ResultUtil.create(Constant.RESULT_FAIL,e.toString()).toJSONString(), null);
            logger.error("client's request failed", e);
        } 
        
        //返回
        return httpResponse;
    }
    
    @Override
    public void proxyToServerResolutionSucceeded(String serverHostAndPort,
                                                 InetSocketAddress resolvedRemoteAddress) {
        if (resolvedRemoteAddress == null) {
            ctx.writeAndFlush(createResponse(HttpResponseStatus.BAD_GATEWAY, originalRequest, ResultUtil.create(Constant.RESULT_FAIL,Constant.MESSAGE_GLOBAL_ERROR).toJSONString(), null));
        } 
    }

    @Override
    public HttpObject proxyToClientResponse(HttpObject httpObject) {
        if (httpObject instanceof HttpResponse) {
        	
        	//系统异常
        	if (((HttpResponse) httpObject).status().code() == HttpResponseStatus.BAD_GATEWAY.code()) {
                ctx.writeAndFlush(createResponse(HttpResponseStatus.BAD_GATEWAY, 
                		originalRequest, ResultUtil.create(Constant.RESULT_FAIL,Constant.MESSAGE_GLOBAL_ERROR).toJSONString(), null));
                return httpObject;
        	}

        	//response调用链
        	ImmutablePair<Boolean, HttpResponseFilter> immutablePair = HttpResponseFilterChain.INSTANCE.doFilter(originalRequest, (HttpResponse) httpObject);
        	
        	//业务异常
        	if (immutablePair.left) {
        		ctx.writeAndFlush(createResponse(immutablePair.right.getStatus(), originalRequest, immutablePair.right.getResult(),immutablePair.right.getHeaderMap()));
                return httpObject;
        	}
        }
        return httpObject;
    }

    @Override
    public void proxyToServerConnectionSucceeded(final ChannelHandlerContext serverCtx) {
        ChannelPipeline pipeline = serverCtx.pipeline();
        //当没有修改getMaximumResponseBufferSizeInBytes中buffer默认的大小时,下面两个handler是不存在的
        if (pipeline.get("inflater") != null) {
            pipeline.remove("inflater");
        }
        if (pipeline.get("aggregator") != null) {
            pipeline.remove("aggregator");
        }
        super.proxyToServerConnectionSucceeded(serverCtx);    
    }
    
    @Override
    public void proxyToServerConnectionFailed() {
        try {
   		 	ClientToProxyConnection clientToProxyConnection = (ClientToProxyConnection) ctx.handler();
            Field field = ClientToProxyConnection.class.getDeclaredField("currentServerConnection");
            field.setAccessible(true);
            ProxyToServerConnection proxyToServerConnection = (ProxyToServerConnection) field.get(clientToProxyConnection);
            String remoteIp = proxyToServerConnection.getRemoteAddress().getAddress().getHostAddress();
            int remotePort = proxyToServerConnection.getRemoteAddress().getPort();
            String remoteUrl = remoteIp + ":" + remotePort;
            String uri = proxyToServerConnection.getInitialRequest().uri();
            ServerStrategyProviderFactory.get().connectionFail(HostUpstream.getFromUri(uri).getHost(), remoteUrl);
        } catch (Exception e) {
            logger.error("connection of proxy->server is failed", e);
        } 
    }

	private HttpResponse createResponse(HttpResponseStatus httpResponseStatus, HttpRequest originalRequest, String result, Map<String, Object> headerMap) {
        HttpResponse httpResponse;
        if (StringUtil.isNotEmpty(result)) {
        	ByteBuf buf = io.netty.buffer.Unpooled.copiedBuffer(result, CharsetUtil.UTF_8); 
        	httpResponse  = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, httpResponseStatus, buf);
        } else {
            httpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, httpResponseStatus);
        }
        HttpHeaders httpHeaders=new DefaultHttpHeaders();
        httpHeaders.add("Transfer-Encoding","chunked");
    	httpHeaders.set("Content-Type", "application/json");
    	if (headerMap != null) {
    		for (Map.Entry<String, Object> entry : headerMap.entrySet()) {
    			httpHeaders.set(entry.getKey(), entry.getValue());
    		}
    	}
        httpResponse.headers().add(httpHeaders);
        return httpResponse;
    }
}
