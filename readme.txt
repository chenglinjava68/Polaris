1:��install Polaris_parent

2:��install ȫ��

3:�ṩ�������͵� demo������dubboӦ�� �� ��dubboӦ�ã�
  ��Ҫ�������Բο�Polaris_demo_web_nodubbo��application.properties

4:��Ⱥ��������ʱ����Ҫע�����ģ�֧��nacos����Ҫ��������nacos��server��,
  ֧��eureka(����robbin���ؾ���)�����ؾ������ ���ݲ���robbin.loadbalancer=com.netflix.loadbalancer.AvailabilityFilteringRule(����ѡ��������rule)
  ÿһ������ ��Ҫ���Լ��������ļ�������ע������
  #name.registry.address=127.0.0.1:8848
  ��Ҫ���Լ��ķ���pom.xml������ Polaris_naming_nacos
  

5:��������֧������ģʽ��zookeeper,nacos,apollo��ConfigFile������Ҫ���Լ��������ļ���������������
  #config.registry.address=127.0.0.1:8848
  
  ��Ҫ���Լ��ķ���pom.xml������ Polaris_conf_nacos
  �����������ĺ����е�properties�ļ������Է���nacos������application.properties �� log4j.properties��
  ������application.properties��������Ҫ�����������ĵ������ļ�
  #extension files
  #project.extension.properties=main.properties,redis.properties
  ��������ȫ������,�������redis��Ⱥ���ã����ݿ���������õȵ�
  #global files
  #project.global.group=global
  #project.global.properties=redis.properties,database.properties

6:Polaris_workflow�����еķ��񣨹�����activity�ںˣ�
  �ṩdubbo�ӿں�http�ӿ����ַ�ʽ��û�л��棬������ο�ģ�������

7:Polaris_gateway�����е�api���ܣ��ṩapi��ͳһ��ڷ���(����netty httpʵ��)
  �����api������ο�config\upstream.txt,����static:��ͷ�Ĵ���Ĵ澲̬�ļ����������е�filter
  ����֧�־�̬�ļ����ã�������config\static.txt�����Ӿ�̬�ļ�·��

8:֧��Sentinel����������ࣩ����Ҫ���Լ��������ļ�����������
  #sentinel
  #csp.sentinel.dashboard.server=127.0.0.1:8858
  #csp.sentinel.heartbeat.interval.ms=5000
  #csp.sentinel.api.port=9008
  ��Ҫ���Լ��ķ���pom.xml������ Polaris_sentinel
  �ýӿ���Ҫ�����ṩapi��servlet

9,�����������eclipse������Application.java�ļ��������½�web���̶���Ҫcopy���ļ�����Ŀ¼��������

  9.1 pom.xml���ṩ tomcat��jetty��������ģʽ,�����ṩresteasy��springmvc��ϵķ�ʽ
      ����ο�pom.xml
	  <dependency>
            <groupId>com.polaris</groupId>
            <artifactId>Polaris_container_jetty</artifactId>  ->���޸ĳ�Polaris_container_tomcat
        </dependency>
        <dependency>
            <groupId>com.polaris</groupId>
            <artifactId>Polaris_container_springmvc</artifactId> ->���޸ĳ�Polaris_container_resteasy
        </dependency>
  assemblyĿ¼�µ������ļ������޸ģ�
  applicationContext.xml�ļ���Ҫ���޸�,����ɨ��·�����ã����ݿ����ã�swagger���õȵȣ������dubbo������Ҫ����dubbo��Ϣ
  ֧��applicationContext-xxx.xml��չ
  ���Polaris_container_springmvc����Ҫ����spring-context-mvc.xml�����ļ�
  application.properties ��������project.name,context,port,�Ƿ���websocket(Ŀǰֻ֧��Polaris_container_tomcat)���Ƿ���servlet�������ȵ�

10,�����˻���netty�ľ�̬�ļ�����������֧��jsp��servlet��
   ����context���ò���config\static.txt
   
11��֧�����������·�ĸ��٣�����traceId, moduleId, parentId, 
    ��־����slf4j�� Logger xLogger = LoggerFactory.getLogger(xxx.class);ֻ������Polaris_core��
	������������̳߳صķ�ʽ����ҪInheritableThreadLocalExecutor��InheritablePolarisThreadLocal���䷽ʽʹ�ã��̳߳��е�traceId��ϢҲ����д���
	����dubbo��ʽ ��Ҫӳ��polaris_dubboģ�飬��������traceID�Ĵ��ݣ�
	http��ʽ ����HttpClientUtil��ʽ����������traceID�Ĵ���
	
12,����ģ��Polaris_cache,
   CacheFactory.getCache(cachename);��ȡ���棬Ĭ�ϲ���EHCache, 
   ���ݻ������ò������Զ�̬�л� RedisSingle��RedisCluster
   ������������
   cache.xxx.type=ehcache
   cache.xxx.type=redis
   cache.xxx.type=rediscluster
   ��������
   ���Բ���ע��com.polaris.cache.Cacheable
   Ŀǰ֧�ֵķ����ο�com.polaris.cache.Cache�ӿ�
   ���� ���л������Լ����ã�Ĭ��KryoSerializer
   
13����Springboot���ں�
    ����ӳ��ע������
	<dependency>
            <groupId>com.polaris</groupId>
            <artifactId>Polaris_naming_nacos</artifactId>
            <version>1.0.0-SNAPSHOT</version>
        </dependency>
		
		����ӳ����������
        <dependency>
            <groupId>com.polaris</groupId>
            <artifactId>Polaris_conf_nacos</artifactId>
            <version>1.0.0-SNAPSHOT</version>
         </dependency>
		
		�ڴ�������ǰ��������
		//����
    	//����
    	ConfClient.init();
    	
		//��������
        SpringApplication springApplication = new SpringApplication(Application.class);
        springApplication.addListeners(new ApplicationListener<ContextRefreshedEvent>() {

			@Override
			public void onApplicationEvent(ContextRefreshedEvent event) {
				
				//ע������
		    	NameingClient.register();
			}
        	
        });
        springApplication.run(args);
