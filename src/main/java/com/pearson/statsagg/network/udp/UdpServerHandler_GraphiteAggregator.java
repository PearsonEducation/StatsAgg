package com.pearson.statsagg.network.udp;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;
import java.util.List;
import com.pearson.statsagg.globals.ApplicationConfiguration;
import com.pearson.statsagg.globals.GlobalVariables;
import com.pearson.statsagg.metric_aggregation.graphite.GraphiteMetricRaw;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class UdpServerHandler_GraphiteAggregator extends SimpleChannelInboundHandler<DatagramPacket> {
    
    private static final Logger logger = LoggerFactory.getLogger(UdpServerHandler_GraphiteAggregator.class.getName());
    
    @Override
    public void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) throws Exception {
        String udpContentString = packet.content().toString(CharsetUtil.UTF_8);
        
        long currentTimestampInMilliseconds = System.currentTimeMillis();

        List<GraphiteMetricRaw> graphiteMetricsRaw = GraphiteMetricRaw.parseGraphiteMetricsRaw(udpContentString, currentTimestampInMilliseconds);

        for (GraphiteMetricRaw graphiteMetricRaw : graphiteMetricsRaw) {
            Long hashKey = GlobalVariables.rawMetricHashKeyGenerator.incrementAndGet();
            graphiteMetricRaw.setHashKey(hashKey);
            GlobalVariables.graphiteAggregatorMetricsRaw.put(graphiteMetricRaw.getHashKey(), graphiteMetricRaw);
            GlobalVariables.incomingMetricsCount.incrementAndGet();
        }
         
        if (ApplicationConfiguration.isDebugModeEnabled()) {
            logger.info("UDP_Graphite_Aggregator_Received_Metrics=" + graphiteMetricsRaw.size());
            logger.info("UDP_Graphite_Aggregator_String=\"" + udpContentString + "\"");
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