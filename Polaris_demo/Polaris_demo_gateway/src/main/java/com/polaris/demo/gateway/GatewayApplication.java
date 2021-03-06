package com.polaris.demo.gateway;


import com.polaris.container.ServerRunner;
import com.polaris.container.annotation.PolarisApplication;
import com.polaris.container.gateway.HttpFilterHelper;
import com.polaris.container.gateway.pojo.HttpFilterEntity;
import com.polaris.container.gateway.pojo.HttpFilterEntityEnum;
import com.polaris.container.listener.ServerListener;
import com.polaris.core.component.LifeCycle;
import com.polaris.demo.gateway.request.TokenExtendHttpRequestFilter;
import com.polaris.demo.gateway.response.HttpTokenResponseFilter;

@PolarisApplication
public class GatewayApplication {
	
    public static void main(String[] args) throws Exception {
    	
    	//启动网关应用
    	ServerRunner.run(args,GatewayApplication.class, new ServerListener() {
    		@Override
    		public void started(LifeCycle event) {
                //HttpFilterHelper.INSTANCE.removeFilter(HttpFilterEntityEnum.CC.getFilterEntity());
    			HttpFilterHelper.INSTANCE.replaceFilter(HttpFilterEntityEnum.Token.getFilterEntity(), new TokenExtendHttpRequestFilter());
                HttpFilterHelper.INSTANCE.addFilter(new HttpFilterEntity(new HttpTokenResponseFilter(), "gateway.token", 2));
    		}
    	});
    }
}
