1:��install Polaris_parent

2:��install ȫ��

3:�ṩ�������͵� demo������dubboӦ�� �� ��dubboӦ�ã�

4:��Ⱥ��������ʱ����Ҫע�����ģ�Ŀǰ��֧��nacos����Ҫ��������nacos��server��
  ÿһ������ ��Ҫ���Լ��������ļ�������ע������
  #name.registry.address=127.0.0.1:8848
  ��Ҫ���Լ��ķ���pom.xml������ Polaris_naming
  

5:��������֧������ģʽ��zookeeper��nacos������Ҫ���Լ��������ļ���������������
  #config.registry.address=127.0.0.1:8848
  ��Ҫ���Լ��ķ���pom.xml������ Polaris_conf_nacos
  �����������ĺ����е�properties�ļ������Է���nacos������application.properties �� log4j.properties��
  ������application.properties��������Ҫ�����������ĵ������ļ�
  #extension files
  #project.extension.properties=main.properties,redis.properties

6:Polaris_timer��Polaris_workflow�����е��������񣨶�ʱ�� �� ������activity�ںˣ�

7:Polaris_log�����е���־�ɼ�������Ҫ��������mongodb�Լ����п�������

8:Polaris_gateway�����е�api���ܣ��ṩapi��ͳһ��ڷ���(����netty httpʵ��)

9:֧��Sentinel����������ࣩ����Ҫ���Լ��������ļ�����������
  #sentinel
  #csp.sentinel.dashboard.server=127.0.0.1:8858
  #csp.sentinel.heartbeat.interval.ms=5000
  #csp.sentinel.api.port=9008
  ��Ҫ���Լ��ķ���pom.xml������ Polaris_sentinel

...