package com.polaris.gateway;

import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.util.List;

import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.littleshoot.proxy.HttpFiltersAdapter;
import org.littleshoot.proxy.impl.ClientToProxyConnection;
import org.littleshoot.proxy.impl.ProxyToServerConnection;

import com.polaris.comm.Constant;
import com.polaris.comm.dto.ResultDto;
import com.polaris.comm.util.LogUtil;
import com.polaris.comm.util.UuidUtil;
import com.polaris.comm.util.WeightedRoundRobinScheduling;
import com.polaris.gateway.request.HttpRequestFilter;
import com.polaris.gateway.request.HttpRequestFilterChain;
import com.polaris.gateway.response.HttpResponseFilterChain;
import com.polaris.gateway.support.HttpRequestFilterSupport;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpContent;
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
	private static LogUtil logger = LogUtil.getInstance(HttpFilterAdapterImpl.class);

    //构造过滤器适配器
    public HttpFilterAdapterImpl(HttpRequest originalRequest, ChannelHandlerContext ctx) {
        super(originalRequest, ctx);
    }

    //处理所有的request请求
    @Override
    public HttpResponse clientToProxyRequest(HttpObject httpObject) {
    	
        HttpResponse httpResponse = null;
        try {
        	
        	//Trace
        	if (httpObject instanceof HttpRequest) {
        		Constant.removeContext();
            	Constant.setContext(LogUtil.TRACE_ID, UuidUtil.generateUuid());
        		HttpRequest httpRequest = (HttpRequest) httpObject;
        		replaceHost(httpRequest);
        		httpRequest.headers().set(LogUtil.TRACE_ID, Constant.getContext(LogUtil.TRACE_ID));
        	}
        	
        	//进入request过滤器
            ImmutablePair<Boolean, HttpRequestFilter> immutablePair = HttpRequestFilterChain.doFilter(originalRequest, httpObject, ctx);
            
            //过滤不通过的直接进入response过滤器
            if (immutablePair.left) {
                httpResponse = createResponse(HttpResponseStatus.FORBIDDEN, originalRequest, immutablePair.right.getResultDto());
            }
        } catch (Exception e) {
        	
        	//存在异常的直接进入response过滤器
            httpResponse = createResponse(HttpResponseStatus.BAD_GATEWAY, originalRequest, HttpRequestFilterSupport.createResultDto(e));
            logger.error("client's request failed", e.getCause());
            
        } finally {
        	
        	//请求出错需要清空上下文
        	if (httpResponse != null) {
        		Constant.removeContext();//先清空后载入
        	} else {
        		
        		//请求结束的时候也要清空上下文
            	if (httpObject instanceof HttpContent) {
            		Constant.removeContext();//先清空后载入
            	}
        	}
        }
        
        //返回
        return httpResponse;
    }
    
    @Override
    public void proxyToServerResolutionSucceeded(String serverHostAndPort,
                                                 InetSocketAddress resolvedRemoteAddress) {
        if (resolvedRemoteAddress == null) {
        	try {
                ctx.writeAndFlush(createResponse(HttpResponseStatus.BAD_GATEWAY, originalRequest, HttpRequestFilterSupport.createResultDto(Constant.MESSAGE_GLOBAL_ERROR)));
        	} finally {
        		Constant.removeContext();
        	}
        } 
    }

    //进入resoponse过滤器
    @Override
    public HttpObject proxyToClientResponse(HttpObject httpObject) {
    	try {
            if (httpObject instanceof HttpResponse) {
            	HttpResponseFilterChain.doFilter(originalRequest, (HttpResponse) httpObject);
            	if (((HttpResponse) httpObject).status().code() == HttpResponseStatus.BAD_GATEWAY.code()) {
                    ctx.writeAndFlush(createResponse(HttpResponseStatus.BAD_GATEWAY, originalRequest, HttpRequestFilterSupport.createResultDto(Constant.MESSAGE_GLOBAL_ERROR)));
            	}
            }
            return httpObject;
    	} finally {
    		
    		//返回成功
    		if (!(httpObject instanceof HttpResponse)) {
    			Constant.removeContext();
    		}
    	}
    }

    //下游的服务器连接成功
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
    
    //下游服务器连接失败
    @Override
    public void proxyToServerConnectionFailed() {
        try {
   		 	ClientToProxyConnection clientToProxyConnection = (ClientToProxyConnection) ctx.handler();
            Field field = ClientToProxyConnection.class.getDeclaredField("currentServerConnection");
            field.setAccessible(true);
            ProxyToServerConnection proxyToServerConnection = (ProxyToServerConnection) field.get(clientToProxyConnection);
   		 	String remoteHostName = proxyToServerConnection.getRemoteAddress().getAddress().getHostAddress();
            int remoteHostPort = proxyToServerConnection.getRemoteAddress().getPort();
            
            String serverHostAndPort = proxyToServerConnection.getServerHostAndPort();
            String port = serverHostAndPort.substring(serverHostAndPort.indexOf(":")+1);
            WeightedRoundRobinScheduling weightedRoundRobinScheduling = HostResolverImpl.getSingleton().getServers(port);
            weightedRoundRobinScheduling.unhealthilyServers.add(weightedRoundRobinScheduling.getServer(remoteHostName, remoteHostPort));
            weightedRoundRobinScheduling.healthilyServers.remove(weightedRoundRobinScheduling.getServer(remoteHostName, remoteHostPort));
        } catch (Exception e) {
            logger.error("connection of proxy->server is failed", e);
        } finally {
        	Constant.removeContext();
        }
    }

    //创建resoponse(中途退出错误的场合)
    private HttpResponse createResponse(HttpResponseStatus httpResponseStatus, HttpRequest originalRequest, ResultDto responseDto) {
        HttpHeaders httpHeaders=new DefaultHttpHeaders();
        httpHeaders.add("Transfer-Encoding","chunked");
        HttpResponse httpResponse;
        if (responseDto != null) {
        	ByteBuf buf = io.netty.buffer.Unpooled.copiedBuffer(responseDto.toJSON().toJSONString(), CharsetUtil.UTF_8); 
        	httpResponse  = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, httpResponseStatus, buf);
        	httpHeaders.set("Content-Type", MediaType.APPLICATION_JSON);
        } else {
            httpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, httpResponseStatus);
        }

        //support CORS（服务器跨域请求）
        List<String> originHeader = GatewayConstant.getHeaderValues(originalRequest, "Origin");
        if (originHeader.size() > 0) {
            httpHeaders.set("Access-Control-Allow-Credentials", "true");
            httpHeaders.set("Access-Control-Allow-Origin", originHeader.get(0));
        }
        httpResponse.headers().add(httpHeaders);
        return httpResponse;
    }
    
    //替换host
    private void replaceHost(HttpRequest httpRequest) {
    	String host = httpRequest.headers().get(GatewayConstant.HOST);
		httpRequest.headers().remove(GatewayConstant.HOST);
		String uri = httpRequest.uri();
		String port = HostResolverImpl.getSingleton().getPort(uri);
		httpRequest.headers().add(GatewayConstant.HOST, 
				host.replace(GatewayConstant.SERVER_PORT, port));
    }
}
