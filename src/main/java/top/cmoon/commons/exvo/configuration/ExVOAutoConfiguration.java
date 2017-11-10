package top.cmoon.commons.exvo.configuration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass(VOAspect.class)
public class ExVOAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(VOAspect.class)
    public VOAspect voAspectBean() {
        return new VOAspect();
    }

}
