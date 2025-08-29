package com.wind.nacos;


import com.alibaba.nacos.api.config.ConfigService;
import com.wind.configcenter.core.ConfigRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration(proxyBeanMethods = false)
public class WindNacosConfigAutoConfiguration {

    @Bean
    public ConfigService nacosConfigService() {
        return WindNacosBootstrapRegistryInitializer.CONFIG_SERVICE.get();
    }

    @Bean
    public ConfigRepository nacosConfigRepository() {
        return WindNacosBootstrapRegistryInitializer.CONFIG_REPOSITORY.get();
    }

}
