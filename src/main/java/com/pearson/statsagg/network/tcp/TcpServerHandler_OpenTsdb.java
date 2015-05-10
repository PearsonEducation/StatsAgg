package com.pearson.statsagg.network.tcp;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.ReferenceCountUtil;
import java.util.List;
import com.pearson.statsagg.globals.ApplicationConfiguration;
import com.pearson.statsagg.globals.GlobalVariables;
import com.pearson.statsagg.metric_aggregation.opentsdb.OpenTsdbMetric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class TcpServerHandler_OpenTsdb extends SimpleChannelInboundHandler<String> {

    private static final Logger logger = LoggerFactory.getLogger(TcpServerHandler_OpenTsdb.class.getName());

    @Override
    public void channelRead0(ChannelHandlerContext ctx, String message) throws Exception {

        try {
            if ((message != null) && message.trim().equals("version")) {
                ctx.write(com.pearson.statsagg.controller.Version.getProjectVersion() + "-" + com.pearson.statsagg.controller.Version.getBuildTimestamp() + "\n");
            }
            else if ((message != null) && (message.length() > 4) && message.startsWith("put ")){
                long currentTimestampInMilliseconds = System.currentTimeMillis();

                List<OpenTsdbMetric> openTsdbMetrics = OpenTsdbMetric.parseOpenTsdbMetrics(message.substring(4), 
                        GlobalVariables.openTsdbPrefix, currentTimestampInMilliseconds);

                for (OpenTsdbMetric openTsdbMetric : openTsdbMetrics) {
                    long hashKey = GlobalVariables.metricHashKeyGenerator.incrementAndGet();
                    openTsdbMetric.setHashKey(hashKey);
                    GlobalVariables.openTsdbMetrics.put(openTsdbMetric.getHashKey(), openTsdbMetric);
                    GlobalVariables.incomingMetricsCount.incrementAndGet();
                }

                if (ApplicationConfiguration.isDebugModeEnabled()) {
                    logger.info("TCP_OpenTsdb_Received_Metrics=" + openTsdbMetrics.size());
                    logger.info("TCP_OpenTsdb_String=\"" + message + "\"");
                }
            }
        }
        finally {
            ReferenceCountUtil.safeRelease(message);
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