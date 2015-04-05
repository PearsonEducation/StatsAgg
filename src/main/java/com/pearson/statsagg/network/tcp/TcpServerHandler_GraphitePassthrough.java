package com.pearson.statsagg.network.tcp;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.ReferenceCountUtil;
import java.util.List;
import com.pearson.statsagg.globals.ApplicationConfiguration;
import com.pearson.statsagg.globals.GlobalVariables;
import com.pearson.statsagg.metric_aggregation.graphite.GraphiteMetricRaw;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class TcpServerHandler_GraphitePassthrough extends SimpleChannelInboundHandler<String> {

    private static final Logger logger = LoggerFactory.getLogger(TcpServerHandler_GraphitePassthrough.class.getName());

    @Override
    public void channelRead0(ChannelHandlerContext ctx, String message) throws Exception {
        try {
            long currentTimestampInMilliseconds = System.currentTimeMillis();
            
            List<GraphiteMetricRaw> graphiteMetricsRaw = GraphiteMetricRaw.parseGraphiteMetricsRaw(message, 
                    GlobalVariables.graphitePassthroughPrefix, currentTimestampInMilliseconds);
            
            for (GraphiteMetricRaw graphiteMetricRaw : graphiteMetricsRaw) {
                Long hashKey = GlobalVariables.metricHashKeyGenerator.incrementAndGet();
                graphiteMetricRaw.setHashKey(hashKey);
                GlobalVariables.graphitePassthroughMetricsRaw.put(graphiteMetricRaw.getHashKey(), graphiteMetricRaw);
                GlobalVariables.incomingMetricsCount.incrementAndGet();
            }
            
            if (ApplicationConfiguration.isDebugModeEnabled()) {
                logger.info("TCP_Graphite_Passthrough_Received_Metrics=" + graphiteMetricsRaw.size());
                logger.info("TCP_Graphite_Passthrough_String=\"" + message + "\"");
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