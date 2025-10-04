package com.wind.nacos;

import com.alibaba.cloud.nacos.NacosConfigProperties;
import com.alibaba.cloud.nacos.NacosPropertySourceRepository;
import com.alibaba.cloud.nacos.client.NacosPropertySource;
import com.alibaba.cloud.nacos.parser.NacosDataParserHandler;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.AbstractListener;
import com.alibaba.nacos.api.exception.NacosException;
import com.wind.common.exception.BaseException;
import com.wind.common.exception.DefaultExceptionCode;
import com.wind.common.jul.WindJulLogFactory;
import com.wind.configcenter.core.ConfigFunctionEvaluator;
import com.wind.configcenter.core.ConfigRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.PropertySource;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

/**
 * 从 nacos 中加载配置
 *
 * @author wuxp
 * @date 2023-10-15 14:59
 **/
@Slf4j
public record NacosConfigRepository(ConfigService configService, NacosConfigProperties properties) implements ConfigRepository {

    private static final Logger LOGGER = WindJulLogFactory.getLogger(NacosConfigRepository.class);

    @Override
    public void saveTextConfig(ConfigDescriptor descriptor, String content) {
        try {
            configService.publishConfig(descriptor.getConfigId(), descriptor.getGroup(), content, descriptor.getFileType().getFileExtension());
        } catch (NacosException exception) {
            throw new BaseException(DefaultExceptionCode.COMMON_ERROR, String.format("save config：%s error", descriptor.getConfigId()), exception);
        }
    }

    @Override
    public String getTextConfig(ConfigDescriptor descriptor) {
        LOGGER.info("get config content dataId = " + descriptor.getConfigId());
        try {
            String result = configService.getConfig(descriptor.getConfigId(), descriptor.getGroup(), properties.getTimeout());
            // 尝试执行配置的函数
            return ConfigFunctionEvaluator.getInstance().eval(descriptor.getConfigId(), result);
        } catch (NacosException exception) {
            throw new BaseException(DefaultExceptionCode.COMMON_ERROR, String.format("load config：%s error", descriptor.getConfigId()), exception);
        }
    }

    @Override
    public List<PropertySource<?>> getConfigs(ConfigDescriptor descriptor) {
        String config = getTextConfig(descriptor);
        List<PropertySource<?>> result = getPropertySources(descriptor, config);
        collectNacosPropertySource(result, descriptor);
        return result;
    }

    @Override
    public ConfigSubscription onChange(ConfigDescriptor descriptor, TextConfigListener listener) {
        final AbstractListener wrapperListener = new AbstractListener() {
            @Override
            public void receiveConfigInfo(String content) {
                listener.change(content);
            }
        };
        return addListener(descriptor, wrapperListener);
    }

    @Override
    public ConfigSubscription onChange(ConfigDescriptor descriptor, PropertyConfigListener listener) {
        final AbstractListener wrapperListener = new AbstractListener() {
            @Override
            public void receiveConfigInfo(String content) {
                listener.change(getPropertySources(descriptor, content));
            }
        };
        return addListener(descriptor, wrapperListener);
    }

    private ConfigSubscription addListener(ConfigDescriptor descriptor, AbstractListener wrapperListener) {
        if (!descriptor.isRefreshable()) {
            log.warn("config unsupported refresh, dataId = {}，group = {}", descriptor.getConfigId(), descriptor.getGroup());
            return ConfigSubscription.empty(descriptor);

        }
        try {
            configService.addListener(descriptor.getConfigId(), descriptor.getGroup(), wrapperListener);
        } catch (NacosException exception) {
            throw new BaseException(DefaultExceptionCode.COMMON_ERROR, exception.getMessage(), exception);
        }
        return new ConfigSubscription() {
            @Override
            public ConfigDescriptor getConfigDescriptor() {
                return descriptor;
            }

            @Override
            public void unsubscribe() {
                // 移除订阅
                configService.removeListener(descriptor.getConfigId(), descriptor.getGroup(), wrapperListener);
            }
        };
    }

    @Override
    public String getConfigSourceName() {
        return "Nacos-Config";
    }

    private List<PropertySource<?>> getPropertySources(ConfigDescriptor descriptor, String content) {
        try {
            return NacosDataParserHandler.getInstance().parseNacosData(descriptor.getConfigId(), content,
                    descriptor.getFileType().getFileExtension());
        } catch (IOException exception) {
            throw new BaseException(DefaultExceptionCode.COMMON_ERROR, String.format("parse config：%s error", descriptor.getConfigId()), exception);
        }
    }

    private void collectNacosPropertySource(List<PropertySource<?>> propertySources, ConfigDescriptor descriptor) {
        NacosPropertySource nacosPropertySource = new NacosPropertySource(propertySources,
                descriptor.getGroup(), descriptor.getConfigId(), new Date(), descriptor.isRefreshable());
        NacosPropertySourceRepository.collectNacosPropertySource(nacosPropertySource);
    }
}
