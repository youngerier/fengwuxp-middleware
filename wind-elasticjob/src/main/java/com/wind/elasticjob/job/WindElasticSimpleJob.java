package com.wind.elasticjob.job;

import org.apache.shardingsphere.elasticjob.api.ShardingContext;
import org.apache.shardingsphere.elasticjob.simple.job.SimpleJob;

/**
 * @author wuxp
 * @date 2024-12-15 18:33
 **/
public interface WindElasticSimpleJob extends WindElasticJob, SimpleJob {

    @Override
    default void execute(ShardingContext shardingContext) {
        execute(new WindElasticShardingContext(shardingContext));
    }

    default void execute(WindElasticShardingContext context) {
        throw new UnsupportedOperationException("please use execute(ShardingContext shardingContext)");
    }
}
