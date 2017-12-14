package org.tiger.demohystrixconfig;

import com.google.common.io.Closeables;
import com.netflix.config.WatchedConfigurationSource;
import com.netflix.config.WatchedUpdateListener;
import com.netflix.config.WatchedUpdateResult;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class ZookeeperNodeCacheConfigurationSource implements WatchedConfigurationSource, Closeable {
    private static final Logger logger = LoggerFactory.getLogger(ZookeeperNodeCacheConfigurationSource.class);

    private List<WatchedUpdateListener> listeners = new CopyOnWriteArrayList<>();

    private final CuratorFramework client;
    private final String configRootPath;
    private final NodeCache nodeCache;

    public ZookeeperNodeCacheConfigurationSource(CuratorFramework client, String configRootPath) {
        this.client = client;
        this.configRootPath = configRootPath;
        this.nodeCache = new NodeCache(client, configRootPath, false);
    }

    public void start() throws Exception {
        nodeCache.getListenable().addListener(() -> {
            ChildData currentData = nodeCache.getCurrentData();
            Stat stat = currentData.getStat();
            byte[] data = currentData.getData();
            String configData = new String(data, Charset.forName("UTF-8"));
            logger.info("initial node cache data:{} version:{}",configData,stat.getAversion());
            Map<String, Object> map = new HashMap<>();

            if (configData.contains("=")) {  //todo change by your own logic
                String[] split = configData.split("=");
                map.put(split[0], split[1]);
            }

            WatchedUpdateResult result = WatchedUpdateResult.createFull(map);

            fireEvent(result);
        });

        nodeCache.start();

    }

    protected void fireEvent(WatchedUpdateResult result) {
        for (WatchedUpdateListener l : listeners) {
            try {
                l.updateConfiguration(result);
            } catch (Throwable ex) {
                logger.error("Error in invoking WatchedUpdateListener", ex);
            }
        }
    }


    @Override
    public void addUpdateListener(WatchedUpdateListener l) {
        if (l != null) {
            listeners.add(l);
        }

    }

    @Override
    public void removeUpdateListener(WatchedUpdateListener l) {
        listeners.remove(l);

    }

    @Override
    public Map<String, Object> getCurrentData() throws Exception {
        ChildData currentData = nodeCache.getCurrentData();
        if(currentData != null){
            Map<String, Object> map = new HashMap<>();
            map.put("currentData", new String(currentData.getData()));
            logger.info("get current data :{}  version:{}", new String(currentData.getData()), currentData.getStat().getAversion());
            return map;
        }
        return null;
    }

    @Override
    public void close() throws IOException {
        try {
            Closeables.close(nodeCache, true);
        } catch (IOException exc) {
            logger.error("IOException should not have been thrown.", exc);
        }
    }
}
