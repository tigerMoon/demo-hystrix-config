package org.tiger;

import com.netflix.config.ConfigurationManager;
import com.netflix.config.DynamicWatchedConfiguration;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.tiger.demohystrixconfig.ZooKeeperPathCacheConfigurationSource;
import org.tiger.demohystrixconfig.ZookeeperNodeCacheConfigurationSource;

import static org.tiger.demohystrixconfig.ZookeeperConfig.zkConfigRootPath;
import static org.tiger.demohystrixconfig.ZookeeperConfig.zkConnectionString;

@SpringBootApplication
public class Application {


	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
		startZookeeper();
	}

	private static void startZookeeper() {
		CuratorFramework client = CuratorFrameworkFactory.newClient(zkConnectionString,
				new ExponentialBackoffRetry(1000, 3));
		if(!client.getState().equals(CuratorFrameworkState.STARTED)){
			client.start();
		}

		ZookeeperNodeCacheConfigurationSource zkConfigSource = new ZookeeperNodeCacheConfigurationSource(client, zkConfigRootPath);
		//change cache here
//		ZooKeeperPathCacheConfigurationSource zkConfigSource = new ZooKeeperPathCacheConfigurationSource(client, zkConfigRootPath);
		try {
			zkConfigSource.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
		DynamicWatchedConfiguration zkDynamicConfig = new DynamicWatchedConfiguration(zkConfigSource);
		ConfigurationManager.install(zkDynamicConfig);
	}

}
