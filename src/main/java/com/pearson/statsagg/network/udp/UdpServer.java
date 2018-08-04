package com.pearson.statsagg.network.udp;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import com.pearson.statsagg.network.NettyServer;
import com.pearson.statsagg.utilities.core_utils.StackTrace;
import com.pearson.statsagg.utilities.core_utils.Threads;
import io.netty.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class UdpServer implements Runnable, NettyServer {

    private static final Logger logger = LoggerFactory.getLogger(UdpServer.class.getName());
    
    public static final String SERVER_TYPE_STATSD = "STATSD";
    public static final String SERVER_TYPE_GRAPHITE_AGGREGATOR = "GRAPHITE_AGGREGATOR";
    public static final String SERVER_TYPE_GRAPHITE_PASSTHROUGH = "GRAPHITE_PASSTHROUGH";
    
    private final int port_;
    private final String serverType_;
    private EventLoopGroup group_ = null;
    private boolean initializeSuccess = true;

    public UdpServer(int port, String serverType) {
        this.port_ = port;
        this.serverType_ = serverType;
    }

    @Override
    public void run() {

        if ((port_ < 0) || (port_ > 65535)) {
            logger.error("Error running " + serverType_.toLowerCase() + " UDP server. Bad input arguments.");
            initializeSuccess = false;
            return;
        }

        try {
            group_ = new NioEventLoopGroup();

            Bootstrap b = new Bootstrap();

            if (serverType_.equals(SERVER_TYPE_STATSD)) {
                b.group(group_).channel(NioDatagramChannel.class).option(ChannelOption.SO_BROADCAST, true).handler(new UdpServerHandler_Statsd());
            }
            else if (serverType_.equals(SERVER_TYPE_GRAPHITE_AGGREGATOR)) {
                b.group(group_).channel(NioDatagramChannel.class).option(ChannelOption.SO_BROADCAST, true).handler(new UdpServerHandler_GraphiteAggregator());
            }
            else if (serverType_.equals(SERVER_TYPE_GRAPHITE_PASSTHROUGH)) {
                b.group(group_).channel(NioDatagramChannel.class).option(ChannelOption.SO_BROADCAST, true).handler(new UdpServerHandler_GraphitePassthrough());
            }
            
            b.bind(port_).sync().channel().closeFuture().await();
        }
        catch (Exception e) {
            initializeSuccess = false;
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        finally {
            shutdownServer();
        }
    }

    @Override
    public void shutdownServer() {

        try {
            if (group_ != null) {
                Future future = group_.shutdownGracefully();
                
                try {
                    future.await();
                }
                catch (Exception e2) {
                    logger.error(e2.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e2));
                }

                while ((group_ != null) && !group_.isTerminated()) {
                    Threads.sleepSeconds(1);

                    boolean isGroupTerminated = false;
                    try {
                        isGroupTerminated = group_.isTerminated();
                    }
                    catch (Exception e) {
                        isGroupTerminated = true;
                    }

                    logger.info("Is " + serverType_.toLowerCase() + " UDP Server Terminated? " + isGroupTerminated);
                }
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        finally {
            group_ = null;
        }
    }
    
    public boolean isInitializeSuccess() {
        return initializeSuccess;
    }
    
}