1:install ȫ��

2:�ṩ�������͵� demo������dubboӦ�� �� ��dubboӦ�ã�
  ��Ҫ�������Բο�Polaris_demo_web_nodubbo��application.properties

3:��Ⱥ��������ʱ����Ҫע�����ģ�
  ֧��nacos����Ҫ��������nacos��server��,
  ֧��eureka(����robbin���ؾ���)�����ؾ������ ���ݲ���robbin.loadbalancer=com.netflix.loadbalancer.AvailabilityFilteringRule(����ѡ��������rule)
  �����汾֧��zookeeperע������(web��һ��)
  ÿһ������ ��Ҫ���Լ��������ļ�������ע������
  #name.registry.address=127.0.0.1:8848
  ��Ҫ���Լ��ķ���pom.xml������ Polaris_naming_nacos
  

4:��������֧�ֶ���ģʽ��zookeeper,nacos,apollo��ConfigFile�Լ������ļ�file������Ҫ���Լ��������ļ���������������
  ���е��������࣬���Լ��������������nacos,apollo,zookeeper�Ľ���ʹ��ZooViewer��https://github.com/HelloKittyNII/ZooViewer��
  #zookeeper��Ҫ����config.zk.root.pathĬ��ֵ��/polaris_conf��
  #config.registry.address=127.0.0.1:8848
  
  ��Ҫ���Լ��ķ���pom.xml������ Polaris_conf_nacos
  �����������ĺ����е�properties�ļ������Է���nacos������application.properties �� log4j.properties��
  ������application.properties��������Ҫ�����������ĵ������ļ�
  #extension files
  #project.extension.properties=main.properties,redis.properties
  ��������ȫ������,�������redis��Ⱥ���ã����ݿ���������õȵ�
  #global files
  #project.global.config.name=global
  #project.global.properties=redis.properties,database.properties
  
  Ĭ��֧��Springע��@Value���Զ�����
  ������value.auto.update=false���رգ�Ƶ���������û�Ӱ�����ܣ����ø��²����ļ���ʽ��

5:Polaris_workflow�����еķ��񣨹�����activity�ںˣ�
  �ṩdubbo�ӿں�http�ӿ����ַ�ʽ��û�л��棬������ο�ģ�������

6:Polaris_gateway�����е�api���ܣ��ṩapi��ͳһ��ڷ���(����netty httpʵ��)
  �����api������ο�config\upstream.txt,����static:��ͷ�Ĵ���Ĵ澲̬�ļ����������е�filter
  ����֧�־�̬�ļ����ã�������config\static.txt�����Ӿ�̬�ļ�·��

7:֧��Sentinel����������ࣩ����Ҫ���Լ��������ļ�����������
  #sentinel
  #csp.sentinel.dashboard.server=127.0.0.1:8858
  #csp.sentinel.heartbeat.interval.ms=5000
  #csp.sentinel.api.port=9008
  ��Ҫ���Լ��ķ���pom.xml������ Polaris_sentinel
  �ýӿ���Ҫ�����ṩapi��servlet

8,�����������eclipse������xxxApplication.java�ļ���������Ҫʵ��Launcher�ӿڣ�����resources\META-INF\services\com.polaris.core.Launcher�ļ��м�¼���ࣩ

  8.1 pom.xml���ṩ tomcat��jetty��������ģʽ,�����ṩresteasy��springmvc��ϵķ�ʽ
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
  ����ע�����ã���ϸ�ο�Polaris_demo_web_nodubbo����ο�ע���Ĵ���
  application.properties ��������project.name,context,port,���Ƿ���servlet�������ȵ�

9,�����˻���netty�ľ�̬�ļ�����������֧��jsp��servlet��
   ����context���ò���config\static.txt
   
10��֧�����������·�ĸ��٣�����traceId, moduleId, parentId, 
    ��־����slf4j�� Logger xLogger = LoggerFactory.getLogger(xxx.class);ֻ������Polaris_core��
	������������̳߳صķ�ʽ����ҪInheritableThreadLocalExecutor��InheritablePolarisThreadLocal���䷽ʽʹ�ã��̳߳��е�traceId��ϢҲ����д���
	����dubbo��ʽ ��Ҫӳ��polaris_dubboģ�飬��������traceID�Ĵ��ݣ�
	http��ʽ ����HttpClientUtil��ʽ����������traceID�Ĵ���
	
11,����ģ��Polaris_cache,
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
   
12����Springboot���ںϣ���������ע������
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
	@SpringBootApplication
	@ComponentScan(basePackages = {"com.polaris","�Լ���package",})
    	//����
    	ConfClient.init();
    	
	//��������
        SpringApplication springApplication = new SpringApplication(Application.class);
        
        //ע�����
        springApplication.addListeners(new ApplicationListener<ContextStartedEvent>() {

			@Override
			public void onApplicationEvent(ContextStartedEvent event) {
				
				//ע������
		    	NameingClient.register();
			}
        	
        });
        
        //ע������
        springApplication.addListeners(new ApplicationListener<ContextStoppedEvent>() {

			@Override
			public void onApplicationEvent(ContextStoppedEvent event) {
				
				//ע������
		    	NameingClient.unRegister();
			}
        	
        });
        springApplication.run(args);
        
        �����Ҫ������־�����������������ã�������Ҫ�ų�Ĭ�ϵ�logback
        <dependency>
            <groupId>com.polaris</groupId>
            <artifactId>Polaris_logger</artifactId>
            <version>1.0.0-SNAPSHOT</version>
        </dependency> 
   �������Ľ��� ��resourceĿ¼�½���config�ļ���