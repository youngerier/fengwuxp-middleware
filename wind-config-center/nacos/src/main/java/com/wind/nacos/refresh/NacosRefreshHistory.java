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

package com.wind.nacos.refresh;

import com.alibaba.cloud.commons.lang.StringUtils;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.DigestUtils;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;


/**
 * 配置刷新记录
 * @author wuxp
 */
@Slf4j
public class NacosRefreshHistory {

    private static final ThreadLocal<DateFormat> DATE_FORMAT = ThreadLocal
            .withInitial(() -> new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

    private static final int MAX_SIZE = 20;

    @Getter
    private final LinkedList<Record> records = new LinkedList<>();

    public void addRefreshRecord(String dataId, String group, String data) {
        records.addFirst(new Record(DATE_FORMAT.get().format(new Date()), dataId, group, md5(data)));
        if (records.size() > MAX_SIZE) {
            records.removeLast();
        }
    }

    private String md5(String data) {
        if (StringUtils.isEmpty(data)) {
            return null;
        }

        return new BigInteger(1, DigestUtils.md5Digest(data.getBytes(StandardCharsets.UTF_8))).toString(16);
    }

    @Data
    static class Record {

        private final String timestamp;

        private final String dataId;

        private final String group;

        private final String md5;

        Record(String timestamp, String dataId, String group, String md5) {
            this.timestamp = timestamp;
            this.dataId = dataId;
            this.group = group;
            this.md5 = md5;
        }

    }

}
