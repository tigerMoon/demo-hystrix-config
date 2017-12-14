package org.tiger.demohystrixconfig;


import com.netflix.hystrix.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class HelloCommand extends HystrixCommand<String> {
    private  final Logger logger = LoggerFactory.getLogger(HelloCommand.class);


    private final String name;


    public HelloCommand(String name ) {
        super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("ExampleGroup"))
                .andCommandKey(HystrixCommandKey.Factory.asKey("ExampleKey"))
                .andThreadPoolKey(HystrixThreadPoolKey.Factory.asKey("HelloWorldPool"))
                .andCommandPropertiesDefaults(HystrixCommandProperties.Setter()
                        .withExecutionIsolationStrategy(HystrixCommandProperties.ExecutionIsolationStrategy.THREAD)
                //超时
                .withExecutionTimeoutInMilliseconds(1000)
                // fallback 调用限制
                .withFallbackIsolationSemaphoreMaxConcurrentRequests(200)
                // 熔断配置
                .withCircuitBreakerEnabled(true)
                .withCircuitBreakerRequestVolumeThreshold(10)
                .withCircuitBreakerSleepWindowInMilliseconds(6000)
                .withCircuitBreakerErrorThresholdPercentage(60) )
        );
        this.name = name;
    }

    @Override
    protected String run() {
        // a real example would do work like a network call here
        try {
            logger.info("sleep 2000 millseconds");
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "Hello " + name + "!";
    }

    @Override
    protected String getFallback() {
        logger.info("get fall back , default millseconds is  1000, thread sleep 2000, dynamic config is 4000 ");
        return "Hello Failure " + name + "!";
    }



}
