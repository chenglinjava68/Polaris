package com.polaris.core.config.properties;

import java.lang.annotation.Annotation;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.annotation.AnnotatedElementUtils;

import com.polaris.core.config.Config;
import com.polaris.core.config.ConfigFactory;
import com.polaris.core.config.provider.ConfHandlerProviderFactory;
import com.polaris.core.util.StringUtil;

public class ConfigurationGlobal implements BeanPostProcessor, PriorityOrdered, ApplicationContextAware, InitializingBean{
	public static final String BEAN_NAME = ConfigurationGlobal.class.getName();
	
	@Override
	public int getOrder() {
		return Ordered.HIGHEST_PRECEDENCE + 2;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
	}
	
	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		PolarisConfigurationGlobal annotation = getAnnotation(bean, beanName, PolarisConfigurationGlobal.class);
		if (annotation != null) {
			init(bean,annotation);
		}
		return bean;
	}
	
	
	private <A extends Annotation> A getAnnotation(Object bean, String beanName, Class<A> type) {
		return AnnotatedElementUtils.findMergedAnnotation(bean.getClass(), type);
	}
	
	private void init(Object bean, PolarisConfigurationGlobal annotation) {
		String[] files = annotation.value();
		for (String file : files) {
			if (StringUtil.isNotEmpty(file)) {
				if (ConfigFactory.get(Config.GLOBAL).getProperties(file) == null) {
					if (!ConfHandlerProviderFactory.get(Config.GLOBAL).init(file)) {
						throw new RuntimeException("type:global file:" + file + " is not exsit");
					}
				} 
			} 
		}
	}

}