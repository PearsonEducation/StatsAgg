package com.pearson.statsagg.network.udp;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;
import java.util.List;
import com.pearson.statsagg.globals.ApplicationConfiguration;
import com.pearson.statsagg.globals.GlobalVariables;
import com.pearson.statsagg.metric_formats.statsd.StatsdMetric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class UdpServerHandler_Statsd extends SimpleChannelInboundHandler<DatagramPacket> {

    private static final Logger logger = LoggerFactory.getLogger(UdpServerHandler_Statsd.class.getName());
    
    @Override
    public void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) throws Exception {
        String udpContentString = packet.content().toString(CharsetUtil.UTF_8);
        
        long currentTimestampInMilliseconds = System.currentTimeMillis();
        
        List<StatsdMetric> statsdMetrics = StatsdMetric.parseStatsdMetrics(udpContentString, currentTimestampInMilliseconds); 

        for (StatsdMetric statsdMetric : statsdMetrics) {
            long hashKey = GlobalVariables.metricHashKeyGenerator.incrementAndGet();
            statsdMetric.setHashKey(hashKey);
            
            if (statsdMetric.getMetricTypeCode() == StatsdMetric.GAUGE_TYPE) GlobalVariables.statsdGaugeMetrics.put(statsdMetric.getHashKey(), statsdMetric);
            else GlobalVariables.statsdNotGaugeMetrics.put(statsdMetric.getHashKey(), statsdMetric);
            
            if (statsdMetric.getBucket() != null) statsdMetric.getBucket().hashCode();

            GlobalVariables.incomingMetricsCount.incrementAndGet();
        }
        
        if (ApplicationConfiguration.isDebugModeEnabled()) {
            logger.info("UDP_Statsd_Received_Metrics=" + statsdMetrics.size());
            logger.info("UDP_Statsd_String=\"" + udpContentString + "\"");
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        //if (cause.getMessage())
        logger.error(cause.getMessage());
    }
    
}