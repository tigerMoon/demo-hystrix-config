package org.tiger.demohystrixconfig;


import com.netflix.config.ConfigurationManager;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicWatchedConfiguration;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;


@RestController
public class HelloController {


    private  final Logger logger = LoggerFactory.getLogger(HelloController.class);

    @RequestMapping(value = "/cache",method = RequestMethod.GET,produces = "application/json")
    public ResponseEntity<String> getPathCacheData() throws ExecutionException, InterruptedException {

        //节点信息
        String nodeProperty = DynamicPropertyFactory.getInstance()
                .getStringProperty("config", "<none>")
                .get();

        // before this set hystrix.command.HystrixCommandKey.execution.isolation.thread.timeoutInMilliseconds filed
        // ExampleKey is HystrixCommandKey singleton
        String dynamicProperty = DynamicPropertyFactory.getInstance()
                .getStringProperty("hystrix.command.ExampleKey.execution.isolation.thread.timeoutInMilliseconds",
                        "<none>")
                .get();


        logger.info(" config node property:{},dynamicProperty:{}" ,nodeProperty,dynamicProperty);


        Future<String> future = new HelloCommand(dynamicProperty).queue();
        future.get();
        return new ResponseEntity<>("Hello " , HttpStatus.OK);

    }


}
