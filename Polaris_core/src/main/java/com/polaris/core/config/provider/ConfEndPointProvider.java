package com.polaris.core.config.provider;

import java.util.ServiceLoader;

import com.polaris.core.config.ConfEndPoint;

public class ConfEndPointProvider {
    protected final ServiceLoader<ConfEndPoint> endPointLoader = ServiceLoader.load(ConfEndPoint.class);
    private ConfEndPointProvider() {}
    public static ConfEndPointProvider INSTANCE = new ConfEndPointProvider();
    public void init(ConfCompositeProvider composite) {
    	for (ConfEndPoint confEndPoint : endPointLoader) {
	    	confEndPoint.init();
        }
    }
    public void put (String type, String file, String key, String value) {
    	for (ConfEndPoint confEndPoint : endPointLoader) {
	    	confEndPoint.put(type, file, key, value);
        }
    }
}
