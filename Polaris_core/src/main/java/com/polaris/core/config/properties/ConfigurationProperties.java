package com.polaris.core.config.properties;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.MethodCallback;

import com.polaris.core.config.ConfClient;
import com.polaris.core.config.ConfEndPoint;
import com.polaris.core.config.ConfigFactory;
import com.polaris.core.config.provider.ConfCompositeProvider;
import com.polaris.core.util.ReflectionUtil;
import com.polaris.core.util.StringUtil;

public class ConfigurationProperties implements BeanPostProcessor, PriorityOrdered, ApplicationContextAware, InitializingBean,ConfEndPoint{
	
	private Map<Object, PolarisConfigurationProperties> annotationMap = new ConcurrentHashMap<>();
	public static final String BEAN_NAME = ConfigurationProperties.class.getName();
	
	@Override
	public int getOrder() {
		return Ordered.HIGHEST_PRECEDENCE + 1;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
	}
	
	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		PolarisConfigurationProperties annotation = getAnnotation(bean, beanName, PolarisConfigurationProperties.class);
		if (annotation != null) {
			annotationMap.put(bean,annotation);
			bind(bean,annotation);
		}
		return bean;
	}
	
	
	private <A extends Annotation> A getAnnotation(Object bean, String beanName, Class<A> type) {
		return AnnotationUtils.findAnnotation(bean.getClass(), type);
	}
	
	private void bind(Object bean, PolarisConfigurationProperties annotation) {
		String type = annotation.type();
		String file = annotation.file();
		if (StringUtil.isNotEmpty(file)) {
			if (null == ConfigFactory.get(type).getProperties(file)) {
				ConfCompositeProvider.INSTANCE.init(type, file);
			}
		} 
		fieldSet(bean, annotation);
	}
	protected void fieldSet(Object bean, PolarisConfigurationProperties annotation) {
		ReflectionUtils.doWithMethods(bean.getClass(), new MethodCallback() {
			@Override
			public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
				String fileName = ReflectionUtil.getFieldNameForSet(method);
				if (StringUtil.isEmpty(fileName)) {
					return;
				}
				if (StringUtil.isNotEmpty(annotation.value())) {
					fileName = annotation.value()+ "." + fileName;
				}
				String value = ConfClient.get(fileName);
				if (value == null) {
					return;
				}
				try {
					ReflectionUtil.setMethodValue(method, bean, value);
				} catch (RuntimeException ex) {
					if (!annotation.ignoreInvalidFields()) {
						throw ex;
					}
				}
				
			}
		});
	}
	protected void fieldSet(Object bean, PolarisConfigurationProperties annotation, String key, String value) {
		ReflectionUtils.doWithMethods(bean.getClass(), new MethodCallback() {
			@Override
			public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
				String fileName = ReflectionUtil.getFieldNameForSet(method);
				if (StringUtil.isEmpty(fileName)) {
					return;
				}
				if (StringUtil.isNotEmpty(annotation.value())) {
					fileName = annotation.value()+ "." + fileName;
				}
				if (fileName.equals(key)) {
					try {
						ReflectionUtil.setMethodValue(method, bean, value);
					} catch (RuntimeException ex) {
						if (!annotation.ignoreInvalidFields()) {
							throw ex;
						}
					}
				}
			}
		});
	}

	protected Map<Object,PolarisConfigurationProperties> getAnnotationMap() {
		return annotationMap;
	}
}
