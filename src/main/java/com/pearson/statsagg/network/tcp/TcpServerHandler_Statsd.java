package com.pearson.statsagg.network.tcp;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.ReferenceCountUtil;
import java.util.List;
import com.pearson.statsagg.globals.ApplicationConfiguration;
import com.pearson.statsagg.globals.GlobalVariables;
import com.pearson.statsagg.metric_aggregation.statsd.StatsdMetricRaw;
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
            
            List<StatsdMetricRaw> statsdMetricsRaw = StatsdMetricRaw.parseStatsdMetricsRaw(message, currentTimestampInMilliseconds);
            
            for (StatsdMetricRaw statsdMetricRaw : statsdMetricsRaw) {
                Long hashKey = GlobalVariables.rawMetricHashKeyGenerator.incrementAndGet();
                statsdMetricRaw.setHashKey(hashKey);
                GlobalVariables.statsdMetricsRaw.put(statsdMetricRaw.getHashKey(), statsdMetricRaw);
                GlobalVariables.incomingMetricsCount.incrementAndGet();
            }
            
            if (ApplicationConfiguration.isDebugModeEnabled()) {
                logger.info("TCP_Statsd_Received_Metrics=" + statsdMetricsRaw.size());
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