## sentinel集成

maven依赖(自己搭建的maven私服,依赖仅供参考)
```xml
<dependency>
    <groupId>com.littlehow</groupId>
    <artifactId>littlehow-sentinel</artifactId>
    <version>1.0.0</version>
</dependency>
```

### sentinel的feign集成
- 源地址：https://github.com/alibaba/spring-cloud-alibaba/tree/master/alibaba-sentinel-spring-cloud/src/main/java/com/alibaba/cloud/sentinel/feign

### 项目如需要支持feign-sentinel，需要配置以下内容
```properties
#是否启用feign-sentinel
feign.sentinel.enabled=true

#该项不配置的情况下默认为(1,10,0.4#2,10,20)
#配置格式如：grade,timeWindow,count#grade,timeWindow,count
#同一个规则属性用,隔开,多个规则用#隔开
#grade参考com.alibaba.csp.sentinel.slots.block.RuleConstant下
#  DEGRADE_GRADE_RT:平均响应时长(ms)
#  DEGRADE_GRADE_EXCEPTION_RATIO:异常率, 例:0.4表示异常数/通过数>0.4将降级;
#  DEGRADE_GRADE_EXCEPTION_COUNT:异常数
#timeWindow:单位s，10表示10秒的时间窗口内，上面的grade统计值都在10秒窗口内计算;
#count:为double值,根据grade配置适合的数字即可
#feign.sentinel.degrade = 1,10,0.4#2,10,20

#默认为false，表示是否除了业务异常，其余所有异常都进行fallback处理
#false情况下，降级只有DegradeException，限流只有FlowException会fallback
#true情况下，只有BizException不会fallback，其余异常将全部fallback
#feign.sentinel.fallback.all = false
```
  
### 项目中feign没有写fallback的情况下可以配置默认全局fallback返回
#### 系统默认返回fallback，
> 限流和熔断都将抛出SentinelException异常<br>
> SentinelException拥有getCode和getMessage方法，用于获取异常码和异常信息<br>
> 所以需要在全局异常处理器中处理该异常

- 熔断降级：异常码和异常信息为("degrade exception"和"666001")

- 限流:异常码和异常信息为("too many requests"和"666002")


#### 支持外部定义fallback
**接口实现**
- 熔断降级需要实现:GlobalDegradeFallback接口
- 限流需要实现:GlobalFlowFallback接口

**生效**

接受spring容器管理即可(也就是可以基于@Component一族或者@Bean注解即可)


### 注解使用

> 所有基于注解的限流、熔断、降级都在调用端生效；

#### Flow注解

- grade为限流方式:FLOW_GRADE_QPS=1表示基于QPS限流,FLOW_GRADE_THREAD=0表示基于线程数限流
- count为限流阈值:表示QPS或者线程数达到该值则进行限流

#### Degrade注解
- grade为限流方式:上面properties配置项有说明
- count为限流阈值:不同grade分别表示响应时长毫秒数、异常率、异常数
- timeWindow为时间窗口:表示在多长的时间内(秒为单位)达到上述配置阈值则进行降级

### Rule注解

可以同时配置多个限流和降级

