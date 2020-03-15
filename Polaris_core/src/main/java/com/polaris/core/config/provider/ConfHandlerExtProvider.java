package com.polaris.core.config.provider;

import com.polaris.core.Constant;
import com.polaris.core.config.ConfClient;
import com.polaris.core.config.ConfHandlerListener;
import com.polaris.core.config.Config;
import com.polaris.core.config.ConfigFactory;
import com.polaris.core.config.ConfigListener;
import com.polaris.core.util.StringUtil;

public class ConfHandlerExtProvider extends ConfHandlerAbsProvider {

	public static ConfHandlerExtProvider INSTANCE = new ConfHandlerExtProvider();
	
	private ConfHandlerExtProvider() {
	}
	
	@Override
	public void init(ConfigListener configListener) {
		
		super.init(configListener);
		
		//get target files
		String files = ConfigFactory.get(Config.SYSTEM).getProperties(Config.SYSTEM).getProperty(Constant.PROJECT_EXTENSION_PROPERTIES);
		if (StringUtil.isEmpty(files)) {
			return;
		}
		String[] fileArray = files.split(",");
		
		//target files loop
		for (String file : fileArray) {
			if (!init(file)) {
				throw new RuntimeException("type:ext file:"+file+" is not exsit");
			}
		}
	}

	//@Override
    public boolean init(String file) {
    	
		//get config
		Config config = ConfigFactory.get(Config.EXT);
		
		//get config-center-group
		String group = ConfClient.getAppName();
		
		//result
		return init(file, group, config);
    }
    
	@Override
	public String get(String file) {
		return get(file,ConfClient.getAppName());
	}
	
	@Override
	public void listen(String file, ConfHandlerListener listener) {
		listen(file,ConfClient.getAppName(),listener);
	}

}