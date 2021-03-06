package com.polaris.container.gateway.response;

import java.util.Map;

import com.polaris.container.gateway.pojo.HttpFilterMessage;
import com.polaris.container.gateway.request.HttpCorsRequestFilter;

import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;

/**
 * @author:Tom.Yu
 *
 * Description:
 *
 */
public class HttpCorsResponseFilter extends HttpResponseFilter {
	
    @Override
    public boolean doFilter(HttpRequest originalRequest, HttpObject httpObject, HttpFilterMessage httpMessage) {
    	HttpResponse httpResponse = (HttpResponse)httpObject;
    	Map<String, String> corsMap = HttpCorsRequestFilter.getCorsMap();
    	for (Map.Entry<String, String> entry : corsMap.entrySet()) {
    		httpResponse.headers().add(entry.getKey(), entry.getValue());
    	}
        return false;
    }
}
