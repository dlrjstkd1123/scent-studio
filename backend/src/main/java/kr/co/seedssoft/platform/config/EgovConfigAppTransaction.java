package kr.co.seedssoft.platform.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.aop.Advisor;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.interceptor.NameMatchTransactionAttributeSource;
import org.springframework.transaction.interceptor.RollbackRuleAttribute;
import org.springframework.transaction.interceptor.RuleBasedTransactionAttribute;
import org.springframework.transaction.interceptor.TransactionAttribute;
import org.springframework.transaction.interceptor.TransactionInterceptor;

/**
 * @ClassName : EgovConfigAppTransaction.java
 * @Description : 트랜잭션 (기존 context-transaction.xml 역할).
 *                service.impl 패키지의 *Impl 클래스 메서드 전체에 트랜잭션을 건다.
 *                메서드에서 Exception 이 나면 롤백한다. (@Transactional 을 따로 붙이지 않아도 됨)
 *                단, 외부 API 만 호출하는 Fragella*Impl · Gemini*Impl 은 제외한다 (응답을 기다리는 동안 DB 연결을 붙잡지 않도록).
 * @author : 이건상
 * @since : 2026. 09. 27.
 */
@Configuration
public class EgovConfigAppTransaction {

    @Bean
    public DataSourceTransactionManager txManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean
    public TransactionInterceptor txAdvice(DataSourceTransactionManager txManager) {
        RuleBasedTransactionAttribute txAttribute = new RuleBasedTransactionAttribute();
        txAttribute.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        txAttribute.setRollbackRules(Collections.singletonList(new RollbackRuleAttribute(Exception.class)));

        Map<String, TransactionAttribute> txMethods = new HashMap<>();
        txMethods.put("*", txAttribute);

        NameMatchTransactionAttributeSource txAttributeSource = new NameMatchTransactionAttributeSource();
        txAttributeSource.setNameMap(txMethods);

        TransactionInterceptor txAdvice = new TransactionInterceptor();
        txAdvice.setTransactionAttributeSource(txAttributeSource);
        txAdvice.setTransactionManager(txManager);
        return txAdvice;
    }

    @Bean
    public Advisor txAdvisor(TransactionInterceptor txAdvice) {
        AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
        pointcut.setExpression(
            "execution(* kr.co.seedssoft.platform..impl.*Impl.*(..))"
                + " && !within(kr.co.seedssoft.platform..impl.Fragella*Impl)"
                + " && !within(kr.co.seedssoft.platform..impl.Gemini*Impl)"
        );
        return new DefaultPointcutAdvisor(pointcut, txAdvice);
    }
}
