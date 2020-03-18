package com.polaris.extension.sentinel;

import java.util.HashSet;
import java.util.Set;

import com.alibaba.csp.sentinel.adapter.servlet.callback.UrlCleaner;
import com.alibaba.csp.sentinel.adapter.servlet.callback.WebCallbackManager;
import com.polaris.core.Constant;
import com.polaris.core.config.ConfHandlerListener;
import com.polaris.core.config.Config.Type;
import com.polaris.core.config.provider.ConfHandlerProviderFactory;
import com.polaris.core.util.PropertyUtil;
import com.polaris.core.util.StringUtil;

public class WebFilterInit {
	private final static String FILE_NAME = "sentinel.txt";
    private volatile static Set<String> restUriSet = new HashSet<>();

	private static void loadFile(String content) {
		if (StringUtil.isEmpty(content)) {
			restUriSet = new HashSet<>();
			return;
		}
    	String[] contents = content.split(Constant.LINE_SEP);
    	Set<String> TEMP_REST_URI = new HashSet<>();
 
    	for (String conf : contents) {
    		if (StringUtil.isNotEmpty(conf) && !conf.startsWith("#")) {
    			conf = conf.replace("\n", "");
    			conf = conf.replace("\r", "");
				String[] kv = PropertyUtil.getKeyValue(conf);
    			if (kv[0].equals("csp.sentinel.filter.restUri")) {
    				TEMP_REST_URI.add(kv[1]);
    			}
    		}
    	}
    	
    	restUriSet = TEMP_REST_URI;
    }
	
	public void init() throws Exception {
		
		//先获取
		try {
			loadFile(ConfHandlerProviderFactory.get(Type.EXT).get(FILE_NAME));
			
			//后监听
			ConfHandlerProviderFactory.get(Type.EXT).listen(FILE_NAME, new ConfHandlerListener() {
				@Override
				public void receive(String content) {
					loadFile(content);
				}
	    	});
		} catch (Exception ex) {
			//File nothing
		}
   	
		//url过滤
		WebCallbackManager.setUrlCleaner(new UrlCleaner() {
			@Override
			public String clean(String originUrl) {
				if (originUrl != null) {
					
					//rest风格的URI需要统合，比如/user/1,user/2,user/3,etc统合成/user/*
					if (restUriSet.size() > 0) {
						for (String prefix : restUriSet) {
							if (originUrl.startsWith(prefix)) {
								return prefix + "/*";
							}
						}
						
					}
				}
				
				return originUrl;
			}
		});
	}
}
