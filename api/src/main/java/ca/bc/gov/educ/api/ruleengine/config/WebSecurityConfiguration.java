package ca.bc.gov.educ.api.ruleengine.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Override
    public void configure(WebSecurity web) {
        web.ignoring().antMatchers("/api/v1/api-docs-ui.html",
                "/api/v1/swagger-ui/**", "/api/v1/api-docs/**",
                "/actuator/health","/actuator/prometheus", "/health");
    }
}