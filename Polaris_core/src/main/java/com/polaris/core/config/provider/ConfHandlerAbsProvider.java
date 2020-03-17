package com.polaris.core.config.provider;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.polaris.core.OrderWrapper;
import com.polaris.core.config.ConfHandler;
import com.polaris.core.config.ConfHandlerListener;
import com.polaris.core.config.Config;
import com.polaris.core.config.Config.Opt;
import com.polaris.core.config.ConfigListener;
import com.polaris.core.config.ConfigStrategy;
import com.polaris.core.config.ConfigStrategyFactory;
import com.polaris.core.config.reader.ConfReaderFactory;
import com.polaris.core.util.StringUtil;
import com.polaris.core.util.UuidUtil;

public abstract class ConfHandlerAbsProvider implements ConfHandlerProvider{
	private static final Logger logger = LoggerFactory.getLogger(ConfHandlerAbsProvider.class);
    private static final ServiceLoader<ConfHandler> handlerLoader = ServiceLoader.load(ConfHandler.class);
	private static volatile AtomicBoolean initialized = new AtomicBoolean(false);
	protected static ConfHandler handler;
	protected ConfigStrategy strategy = ConfigStrategyFactory.get();
	
    @SuppressWarnings("rawtypes")
	protected static ConfHandler initHandler() {
		if (!initialized.compareAndSet(false, true)) {
            return handler;
        }
		List<OrderWrapper> configHandlerList = new ArrayList<OrderWrapper>();
    	for (ConfHandler configHandler : handlerLoader) {
    		OrderWrapper.insertSorted(configHandlerList, configHandler);
        }
    	if (configHandlerList.size() > 0) {
        	handler = (ConfHandler)configHandlerList.get(0).getHandler();
    	}
    	return handler;
    }
    
    @Override
    public void init(ConfigListener configListener) {
    	strategy.init(configListener);
    	initHandler();
    }
    
	@SuppressWarnings("rawtypes")
	public boolean init(String file, String group, Config config) {
    	
		//get
		String contents = get(file,group);
		if (StringUtil.isEmpty(contents)) {
			return false;
		}
		
		//config -set
		Properties properties = ConfReaderFactory.get(file).getProperties(contents);
		String sequence = UuidUtil.generateUuid();
		for (Map.Entry entry : properties.entrySet()) {
			strategy.onChange(sequence, config, file, entry.getKey(), entry.getValue(), Opt.ADD);
		}
		config.put(file, properties);
		strategy.onComplete(sequence);
		
		//listen
    	listen(file, group, new ConfHandlerListener() {
			@Override
			public void receive(String content) {
				Properties oldProperties = config.getProperties(file);
				Properties newProperties = ConfReaderFactory.get(file).getProperties(get(file,group));
				String sequence = UuidUtil.generateUuid();
				for (Map.Entry entry : newProperties.entrySet()) {
					if (!oldProperties.containsKey(entry.getKey())) {
						if (strategy.onChange(sequence, config, file, entry.getKey(), entry.getValue(), Opt.ADD)) {
							logger.info("type:{} file:{} key:{} newValue:{} opt:{}", config.getType(),file,entry.getKey(),entry.getValue(),Opt.ADD.name());
						}


					} else if (!Objects.equals(oldProperties.get(entry.getKey()), newProperties.get(entry.getKey()))) {
						if (strategy.onChange(sequence, config, file, entry.getKey(), entry.getValue(), Opt.UPDATE)) {
							logger.info("type:{} file:{} key:{} oldValue:{} newvalue:{} opt:{}", config.getType(),file,entry.getKey(),oldProperties.get(entry.getKey()), entry.getValue(),Opt.UPDATE.name());
						}
					}
					oldProperties.remove(entry.getKey());
				}
				for (Map.Entry entry : oldProperties.entrySet()) {
					if (strategy.onChange(sequence, config, file, entry.getKey(), entry.getValue(), Opt.DELETE)) {
						logger.info("type:{} file:{}, key:{} value:{} opt:{}", config.getType(),file,entry.getKey(),entry.getValue(),Opt.DELETE.name());
					}
				}
				config.put(file, newProperties);
				strategy.onComplete(sequence);
			}
		});
    	return true;
    }

    public String get(String fileName, String group) {
		if (handler != null) {
			return handler.get(fileName, group);
		}
    	return null;
	}
    public void listen(String fileName,String group, ConfHandlerListener listener) {
		if (handler != null) {
			handler.listen(fileName, group, listener);
		}
	}
}
