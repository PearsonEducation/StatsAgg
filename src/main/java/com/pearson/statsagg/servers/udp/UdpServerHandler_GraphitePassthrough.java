package com.pearson.statsagg.servers.udp;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;
import java.util.List;
import com.pearson.statsagg.configuration.ApplicationConfiguration;
import com.pearson.statsagg.globals.GlobalVariables;
import com.pearson.statsagg.metric_formats.graphite.GraphiteMetric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class UdpServerHandler_GraphitePassthrough extends SimpleChannelInboundHandler<DatagramPacket> {
    
    private static final Logger logger = LoggerFactory.getLogger(UdpServerHandler_GraphitePassthrough.class.getName());
    
    @Override
    public void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) throws Exception {
        String udpContentString = packet.content().toString(CharsetUtil.UTF_8);
        
        long currentTimestampInMilliseconds = System.currentTimeMillis();

        List<GraphiteMetric> graphiteMetrics = GraphiteMetric.parseGraphiteMetrics(udpContentString, 
                GlobalVariables.graphitePassthroughPrefix, currentTimestampInMilliseconds);
            
        for (GraphiteMetric graphiteMetric : graphiteMetrics) {
            long hashKey = GlobalVariables.metricHashKeyGenerator.incrementAndGet();
            graphiteMetric.setHashKey(hashKey);
            GlobalVariables.graphitePassthroughMetrics.put(graphiteMetric.getHashKey(), graphiteMetric);
            GlobalVariables.incomingMetricsCount.incrementAndGet();
        }
         
        if (ApplicationConfiguration.isDebugModeEnabled()) {
            logger.info("UDP_Graphite_Passthrough_Received_Metrics=" + graphiteMetrics.size());
            logger.info("UDP_Graphite_Passthrough_String=\"" + udpContentString + "\"");
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