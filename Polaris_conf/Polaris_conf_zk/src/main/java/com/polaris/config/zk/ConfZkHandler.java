package com.polaris.config.zk;

import org.springframework.core.annotation.Order;

import com.polaris.core.config.ConfHandlerListener;
import com.polaris.core.config.ConfHandlerOrder;
import com.polaris.core.config.ConfHandler;

@Order(ConfHandlerOrder.ZK)
public class ConfZkHandler implements ConfHandler {

	@Override
	public String get(String fileName, String group) {
		return ConfZkClient.getConfig(fileName,group);
	}

	@Override
	public void listen(String fileName, String group, ConfHandlerListener listener) {
		ConfZkClient.addListener(fileName, group, listener);
	}
}
