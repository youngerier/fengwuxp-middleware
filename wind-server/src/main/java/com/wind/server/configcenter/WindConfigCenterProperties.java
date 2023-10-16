package com.wind.server.configcenter;

import com.wind.common.enums.ConfigFileType;
import lombok.Data;

import java.util.Collections;
import java.util.List;

/**
 * 配置中心相关配置
 *
 * @author wuxp
 * @date 2023-10-16 08:57
 **/
@Data
public class WindConfigCenterProperties {


    /**
     * 额外加载的扩展配置
     */
    private List<SimpleConfigDescriptor> extensionConfigs = Collections.emptyList();

    /**
     * 配置文件类型
     */
    private ConfigFileType configFileType = ConfigFileType.PROPERTIES;
}
