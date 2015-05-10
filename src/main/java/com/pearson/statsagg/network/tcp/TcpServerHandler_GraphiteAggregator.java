package com.pearson.statsagg.network.tcp;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.ReferenceCountUtil;
import java.util.List;
import com.pearson.statsagg.globals.ApplicationConfiguration;
import com.pearson.statsagg.globals.GlobalVariables;
import com.pearson.statsagg.metric_aggregation.graphite.GraphiteMetric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class TcpServerHandler_GraphiteAggregator extends SimpleChannelInboundHandler<String> {

    private static final Logger logger = LoggerFactory.getLogger(TcpServerHandler_GraphiteAggregator.class.getName());

    @Override
    public void channelRead0(ChannelHandlerContext ctx, String message) throws Exception {
        try {
            long currentTimestampInMilliseconds = System.currentTimeMillis();
            
            List<GraphiteMetric> graphiteMetrics = GraphiteMetric.parseGraphiteMetrics(message, 
                    GlobalVariables.graphiteAggregatedPrefix, currentTimestampInMilliseconds);
            
            for (GraphiteMetric graphiteMetric : graphiteMetrics) {
                long hashKey = GlobalVariables.metricHashKeyGenerator.incrementAndGet();
                graphiteMetric.setHashKey(hashKey);
                GlobalVariables.graphiteAggregatorMetrics.put(graphiteMetric.getHashKey(), graphiteMetric);
                GlobalVariables.incomingMetricsCount.incrementAndGet();
            }
            
            if (ApplicationConfiguration.isDebugModeEnabled()) {
                logger.info("TCP_Graphite_Aggregator_Received_Metrics=" + graphiteMetrics.size());
                logger.info("TCP_Graphite_Aggregator_String=\"" + message + "\"");
            }
        }
        finally {
            ReferenceCountUtil.release(message);
        }
    }
    
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error(cause.getMessage());
    }
    
}