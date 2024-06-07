package com.pearson.statsagg.servers.tcp;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.ReferenceCountUtil;
import java.util.List;
import com.pearson.statsagg.configuration.ApplicationConfiguration;
import com.pearson.statsagg.globals.GlobalVariables;
import com.pearson.statsagg.metric_formats.statsd.StatsdMetric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class TcpServerHandler_Statsd extends SimpleChannelInboundHandler<String> {

    private static final Logger logger = LoggerFactory.getLogger(TcpServerHandler_Statsd.class.getName());
 
    @Override
    public void channelRead0(ChannelHandlerContext ctx, String message) throws Exception {
        try {
            long currentTimestampInMilliseconds = System.currentTimeMillis();
            
            List<StatsdMetric> statsdMetrics = StatsdMetric.parseStatsdMetrics(message, currentTimestampInMilliseconds);
            
            for (StatsdMetric statsdMetric : statsdMetrics) {
                long hashKey = GlobalVariables.metricHashKeyGenerator.incrementAndGet();
                statsdMetric.setHashKey(hashKey);
                
                if (statsdMetric.getMetricTypeCode() == StatsdMetric.GAUGE_TYPE) GlobalVariables.statsdGaugeMetrics.put(statsdMetric.getHashKey(), statsdMetric);
                else GlobalVariables.statsdNotGaugeMetrics.put(statsdMetric.getHashKey(), statsdMetric);
                
                if (statsdMetric.getBucket() != null) statsdMetric.getBucket().hashCode();
                
                GlobalVariables.incomingMetricsCount.incrementAndGet();
            }
            
            if (ApplicationConfiguration.isDebugModeEnabled()) {
                logger.info("TCP_Statsd_Received_Metrics=" + statsdMetrics.size());
                logger.info("TCP_Statsd_String=\"" + message + "\"");
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