package com.polaris.gateway;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.littleshoot.proxy.HostResolver;

import com.github.pagehelper.util.StringUtil;
import com.polaris.comm.Constant;
import com.polaris.comm.config.ConfClient;
import com.polaris.comm.config.ConfListener;
import com.polaris.comm.config.ConfigHandlerProvider;
import com.polaris.core.connect.ServerDiscoveryHandlerProvider;

/**
 * @author:Tom.Yu Description:
 */
public class HostResolverImpl implements HostResolver {

    private volatile static HostResolverImpl singleton;
    private volatile Map<String, String> serverMap = new ConcurrentHashMap<>();
    private volatile Map<String, String> uriMap = new ConcurrentHashMap<>();
    public static final String UPSTREAM = "upstream.txt";

    //载入需要代理的IP(需要动态代理)
    private void loadUpstream(String content) {
        if (StringUtil.isEmpty(content)) {
            return;
        }

        Map<String, String> tempServerMap = new ConcurrentHashMap<>();
        Map<String, String> tempUriMap = new ConcurrentHashMap<>();
        String[] contents = content.split(Constant.LINE_SEP);
        int port = 7000;
        for (String detail : contents) {
            String[] keyvalue = ConfigHandlerProvider.getKeyValue(detail);
            if (keyvalue != null) {
                tempServerMap.put(String.valueOf(port), keyvalue[1]);
                tempUriMap.put(keyvalue[0], String.valueOf(port));
                port++;
            }
        }
        serverMap = tempServerMap;
        uriMap = tempUriMap;
    }

    //构造函数（单例）
    private HostResolverImpl() {
        
        ConfClient.addListener(UPSTREAM, new ConfListener() {
            @Override
            public void receive(String content) {
                loadUpstream(content);
            }
        });
        try {
			Thread.sleep(100);
			if (serverMap.size() == 0) {
		        loadUpstream(ConfigHandlerProvider.getLocalFileContent(UPSTREAM));//载入配置文件
			}
		} catch (InterruptedException e) {
			//nothing
		}
    }

    public static HostResolverImpl getSingleton() {
        if (singleton == null) {
            synchronized (HostResolverImpl.class) {
                if (singleton == null) {
                    singleton = new HostResolverImpl();
                }
            }
        }
        return singleton;
    }

    //获取服务
    String getServers(String key) {
        return serverMap.get(key);
    }

    String getPort(String uri) {
        if (uri != null) {
            for (Entry<String, String> entry : uriMap.entrySet()) {
                if (uri.startsWith(entry.getKey())) {
                    return entry.getValue();
                }

            }
        }

        // default
        if (uriMap.containsKey(GatewayConstant.DEFAULT)) {
            return uriMap.get(GatewayConstant.DEFAULT);
        }

        //异常
        throw new NullPointerException("url is null");
    }

    @Override
    public InetSocketAddress resolve(String host, int port)
            throws UnknownHostException {
        String defaultUri = ServerDiscoveryHandlerProvider.getInstance().getUrl(serverMap.get(uriMap.get(GatewayConstant.DEFAULT)));
        String key = String.valueOf(port);
        if (serverMap.containsKey(key)) {
            String uri = ServerDiscoveryHandlerProvider.getInstance().getUrl(serverMap.get(key));
            if (StringUtil.isNotEmpty(uri)) {

                String[] si = uri.split(":");
                return new InetSocketAddress(si[0], Integer.parseInt(si[1]));
            } else {
                String[] si = defaultUri.split(":");
                return new InetSocketAddress(si[0], Integer.parseInt(si[1]));
            }
        } else {
            String[] si = defaultUri.split(":");
            return new InetSocketAddress(si[0], Integer.parseInt(si[1]));
        }
    }
}
