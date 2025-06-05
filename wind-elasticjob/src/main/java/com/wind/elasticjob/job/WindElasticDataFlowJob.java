package com.wind.elasticjob.job;

import org.apache.shardingsphere.elasticjob.api.ShardingContext;
import org.apache.shardingsphere.elasticjob.dataflow.job.DataflowJob;

import java.util.List;

/**
 * @author wuxp
 * @date 2024-12-15 18:33
 **/
public interface WindElasticDataFlowJob<T> extends WindElasticJob, DataflowJob<T> {

    @Override
    default void processData(ShardingContext shardingContext, List<T> data) {
        processData(new WindElasticShardingContext(shardingContext), data);
    }

    default void processData(WindElasticShardingContext context, List<T> data) {
        throw new UnsupportedOperationException("please use execute(ShardingContext shardingContext, List<T> data)");
    }
}
