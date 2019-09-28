package com.polaris.http.initializer;

import java.util.EnumSet;

import javax.servlet.DispatcherType;
import javax.servlet.ServletContext;

import com.polaris.core.config.ConfClient;
import com.polaris.core.util.StringUtil;
import com.polaris.http.filter.FlowControlFilter;
import com.polaris.http.filter.RequestFirstFilter;

public abstract class AbsHttpInitializer implements HttpInitializer {
	protected ServletContext servletContext = null;
	public void onStartup(ServletContext servletContext) {
		this.servletContext = servletContext;
		loadContext();
		addInitParameter();
		addListener();
		addFilter();
		addServlet();
	}
	public abstract void loadContext();
	public void addInitParameter() {
		String names = ConfClient.get("servlet.init.parameter.names");
		String values = ConfClient.get("servlet.init.parameter.values");
		if (StringUtil.isNotEmpty(names) && StringUtil.isNotEmpty(values)) {
			String[] nameArray = names.split(",");
			String[] valueArray = values.split(",");
			for (int i0 = 0; i0 < nameArray.length; i0++) {
				servletContext.setInitParameter(nameArray[i0], valueArray[i0]);
			}
		}
	}
	public void addListener() {
		String listeners = ConfClient.get("servlet.listeners");
		if (StringUtil.isNotEmpty(listeners)) {
			String[] listenerArray = listeners.split(",");
			for (String listener : listenerArray) {
				servletContext.addListener(listener);
			}
		}
	}
	public void addFilter() {
		String POLARIS_REQUEST_FIRST_FILTER = "PolarisRequestFirstFilter";
		String POLARIS_FLOW_CONTROL_FILTER = "PolarisFlowControlFilter";
		
		// filter
		servletContext.addFilter(POLARIS_REQUEST_FIRST_FILTER, new RequestFirstFilter())
					  .addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), true, "/*");
		
		// 流控
		if ("true".equals(ConfClient.get("server.flowcontrol.enabled", "false"))) {
			servletContext.addFilter(POLARIS_FLOW_CONTROL_FILTER, new FlowControlFilter())
			  .addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), true, "/*");
		}
		
		//其他filter
		String names = ConfClient.get("servlet.filter.names");
		String values = ConfClient.get("servlet.filter.values");
		String urls = ConfClient.get("servlet.filter.urls");
		if (StringUtil.isNotEmpty(names) && StringUtil.isNotEmpty(values) && StringUtil.isNotEmpty(urls)) {
			String[] nameArray = names.split(",");
			String[] valueArray = values.split(",");
			String[] urlArray = urls.split(",");
			for (int i0 = 0; i0 < nameArray.length; i0++) {
				servletContext.addFilter(nameArray[i0], valueArray[i0])
				  .addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), true, urlArray[i0]);
			}
		}
		
	}
	public void addServlet(){
	}
}
