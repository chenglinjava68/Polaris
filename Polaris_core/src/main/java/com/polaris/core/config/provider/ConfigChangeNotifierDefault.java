package com.polaris.core.config.provider;

import java.util.Map;
import java.util.Objects;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.polaris.core.config.Config;
import com.polaris.core.config.ConfigChangeListener;
import com.polaris.core.config.ConfigChangeNotifier;
import com.polaris.core.config.Config.Opt;
import com.polaris.core.config.Config.Type;
import com.polaris.core.config.reader.ConfReaderFactory;
import com.polaris.core.util.UuidUtil;

@SuppressWarnings("rawtypes")
public class ConfigChangeNotifierDefault implements ConfigChangeNotifier {
	private static final Logger logger = LoggerFactory.getLogger(ConfigChangeNotifierDefault.class);
	public static final ConfigChangeNotifier INSTANCE = new ConfigChangeNotifierDefault();
	private ConfigChangeNotifierDefault() {}
	
	@Override
	public void notify(Config config, String file, String contents, ConfigChangeListener... configListeners) {
		Properties oldProperties = config.getProperties(file);
		Properties newProperties = ConfReaderFactory.get(file).getProperties(contents);
		
		//generate id for one notify
		String sequence = UuidUtil.generateUuid();
		
		//start
		if (configListeners != null) {
            for (ConfigChangeListener configListener : configListeners) {
                configListener.onStart(sequence);
            }
        }
		
		//add or update or delete
		for (Map.Entry entry : newProperties.entrySet()) {
			if (oldProperties == null || !oldProperties.containsKey(entry.getKey())) {
				if (canUpdate(sequence, config, file, entry.getKey(), entry.getValue(), Opt.ADD)) {
                    if (configListeners != null) {
                        for (ConfigChangeListener configListener : configListeners) {
                            configListener.onChange(sequence, entry.getKey(), entry.getValue(), Opt.ADD);
                        }
                    }
					if (oldProperties != null) {
						logger.info("type:{} file:{} key:{} newValue:{} opt:{}", config.getType(),file,entry.getKey(),entry.getValue(),Opt.ADD.name());
					}
				}
			} else if (!Objects.equals(oldProperties.get(entry.getKey()), newProperties.get(entry.getKey()))) {
				if (canUpdate(sequence, config, file, entry.getKey(), entry.getValue(), Opt.UPD)) {
                    if (configListeners != null) {
                        for (ConfigChangeListener configListener : configListeners) {
                            configListener.onChange(sequence, entry.getKey(), entry.getValue(), Opt.UPD);
                        }
                    }
					logger.info("type:{} file:{} key:{} oldValue:{} newvalue:{} opt:{}", config.getType(),file,entry.getKey(),oldProperties.get(entry.getKey()), entry.getValue(),Opt.UPD.name());
				}
			}
			if (oldProperties != null) {
				oldProperties.remove(entry.getKey());
			}
		}
		if (oldProperties != null) {
			for (Map.Entry entry : oldProperties.entrySet()) {
				if (canUpdate(sequence, config, file, entry.getKey(), entry.getValue(), Opt.DEL)) {
		            if (configListeners != null) {
		                for (ConfigChangeListener configListener : configListeners) {
		                    configListener.onChange(sequence, entry.getKey(), entry.getValue(), Opt.DEL);
		                }
		            }
					logger.info("type:{} file:{}, key:{} value:{} opt:{}", config.getType(),file,entry.getKey(),entry.getValue(),Opt.DEL.name());
				}
			}
		}
		config.put(file, newProperties);
		
		//complete
		if (configListeners != null) {
            for (ConfigChangeListener configListener : configListeners) {
                configListener.onComplete(sequence);
            }
        }
	}
	
	
	//update strategy
	public boolean canUpdate(String sequence, Config config,String file, Object key, Object value,Opt opt) {
		//优先级-ext
		if (config == ConfigFactory.get(Type.EXT)) {
			if (ConfigFactory.get(Type.SYS).contain(key)) {
				logger.warn("type:{} file:{}, key:{} value:{} opt:{} failed ,"
						+ "caused by conflicted with system properties ", config.getType(),file,key,value,opt.name());
				return false;
			}
		}
		
		//优先级-global
		if (config == ConfigFactory.get(Type.GBL)) {
			if (ConfigFactory.get(Type.SYS).contain(key)) {
				logger.warn("type:{} file:{}, key:{} value:{} opt:{} failed ,"
						+ "caused by conflicted with system properties", config.getType(),file,key,value,opt.name());
				return false;
			}
			if (ConfigFactory.get(Type.EXT).contain(key)) {
				logger.warn("type:{} file:{}, key:{} value:{} opt:{} failed ,"
						+ "caused by conflicted with ext properties", config.getType(),file,key,value,opt.name());
				return false;
			}
		}
		return true;
	}
	

}
