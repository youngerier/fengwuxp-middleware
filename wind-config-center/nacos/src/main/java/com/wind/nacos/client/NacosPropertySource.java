/*
 * Copyright 2013-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wind.nacos.client;

import com.wind.nacos.NacosConfigProperties;
import lombok.Getter;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author xiaojing
 * @author pbting
 */
@Getter
public class NacosPropertySource extends MapPropertySource {

    /**
     * Nacos Group.
     */
    private final String group;

    /**
     * Nacos dataID.
     */
    private final String dataId;

    /**
     * timestamp the property get.
     */
    private final Date timestamp;

    /**
     * Whether to support dynamic refresh for this Property Source.
     */
    private final boolean refreshable;

    NacosPropertySource(String group, String dataId, Map<String, Object> source, Date timestamp, boolean refreshable) {
        super(String.join(NacosConfigProperties.COMMAS, dataId, group), source);
        this.group = group;
        this.dataId = dataId;
        this.timestamp = timestamp;
        this.refreshable = refreshable;
    }

    public NacosPropertySource(List<PropertySource<?>> propertySources, String group, String dataId, Date timestamp, boolean refreshable) {
        this(group, dataId, getSourceMap(group, dataId, propertySources), timestamp,
                refreshable);
    }

    private static Map<String, Object> getSourceMap(String group, String dataId, List<PropertySource<?>> propertySources) {
        if (CollectionUtils.isEmpty(propertySources)) {
            return Collections.emptyMap();
        }
        // If only one, return the internal element, otherwise wrap it.
        if (propertySources.size() == 1) {
            PropertySource<?> propertySource = propertySources.get(0);
            if (propertySource != null && propertySource.getSource() instanceof Map) {
                return (Map<String, Object>) propertySource.getSource();
            }
        }

        Map<String, Object> sourceMap = new LinkedHashMap<>();
        List<PropertySource<?>> otherTypePropertySources = new ArrayList<>();
        for (PropertySource<?> propertySource : propertySources) {
            if (propertySource == null) {
                continue;
            }
            if (propertySource instanceof MapPropertySource) {
                // If the Nacos configuration file uses "---" to separate property name,
                // propertySources will be multiple documents, and every document is a
                // map.
                // see org.springframework.boot.env.YamlPropertySourceLoader#load
                Map<String, Object> source = ((MapPropertySource) propertySource).getSource();
                sourceMap.putAll(source);
            } else {
                otherTypePropertySources.add(propertySource);
            }
        }

        // Other property sources which is not instanceof MapPropertySource will be put as
        // it is,
        // and the internal elements cannot be directly retrieved,
        // so the user needs to implement the retrieval logic by himself
        if (!otherTypePropertySources.isEmpty()) {
            sourceMap.put(String.join(NacosConfigProperties.COMMAS, dataId, group), otherTypePropertySources);
        }
        return sourceMap;
    }

}
