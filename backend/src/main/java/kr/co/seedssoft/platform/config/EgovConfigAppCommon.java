package kr.co.seedssoft.platform.config;

import org.egovframe.rte.fdl.cmmn.trace.LeaveaTrace;
import org.egovframe.rte.fdl.cmmn.trace.handler.DefaultTraceHandler;
import org.egovframe.rte.fdl.cmmn.trace.handler.TraceHandler;
import org.egovframe.rte.fdl.cmmn.trace.manager.DefaultTraceHandleManager;
import org.egovframe.rte.fdl.cmmn.trace.manager.TraceHandlerService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.util.AntPathMatcher;

/**
 * @ClassName : EgovConfigAppCommon.java
 * @Description : 공통 빈 (기존 context-common.xml 역할).
 *                EgovAbstractServiceImpl 이 이름으로 주입받는 messageSource, leaveaTrace 빈을 등록한다.
 *                이 빈이 없으면 ServiceImpl 을 만들 때 서버가 뜨지 않는다.
 * @author : 이건상
 * @since : 2026. 09. 27.
 */
@Configuration
public class EgovConfigAppCommon {

    @Bean
    public AntPathMatcher antPathMatcher() {
        return new AntPathMatcher();
    }

    @Bean
    public ReloadableResourceBundleMessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasenames(
                "classpath:/egovframework/message/message-common",
                "classpath:/org/egovframe/rte/fdl/property/messages/properties");
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setCacheSeconds(60);
        return messageSource;
    }

    @Bean
    public DefaultTraceHandler defaultTraceHandler() {
        return new DefaultTraceHandler();
    }

    @Bean
    public DefaultTraceHandleManager traceHandlerService() {
        DefaultTraceHandleManager traceHandlerService = new DefaultTraceHandleManager();
        traceHandlerService.setReqExpMatcher(antPathMatcher());
        traceHandlerService.setPatterns(new String[] {"*"});
        traceHandlerService.setHandlers(new TraceHandler[] {defaultTraceHandler()});
        return traceHandlerService;
    }

    @Bean
    public LeaveaTrace leaveaTrace() {
        LeaveaTrace leaveaTrace = new LeaveaTrace();
        leaveaTrace.setTraceHandlerServices(new TraceHandlerService[] {traceHandlerService()});
        return leaveaTrace;
    }
}
