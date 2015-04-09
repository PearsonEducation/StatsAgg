package com.pearson.statsagg.network.tcp;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;
import com.pearson.statsagg.network.NettyServer;
import com.pearson.statsagg.utilities.StackTrace;
import com.pearson.statsagg.utilities.Threads;
import io.netty.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class TcpServer implements Runnable, NettyServer {

    private static final Logger logger = LoggerFactory.getLogger(TcpServer.class.getName());
    
    public static final String SERVER_TYPE_STATSD = "STATSD";
    public static final String SERVER_TYPE_GRAPHITE_AGGREGATOR = "GRAPHITE_AGGREGATOR";
    public static final String SERVER_TYPE_GRAPHITE_PASSTHROUGH = "GRAPHITE_PASSTHROUGH";
    public static final String SERVER_TYPE_OPENTSDB_TELNET = "OPENTSDB";
    
    private final int port_;
    private final String serverType_;
    private EventLoopGroup bossGroup_ = null;
    private EventLoopGroup workerGroup_ = null;
    private boolean initializeSuccess = true;
    
    public TcpServer(int port, String serverType) {
        this.port_ = port;
        this.serverType_ = serverType;
    }

    @Override
    public void run() {

        if ((port_ < 0) || (port_ > 65535)) {
            logger.error("Error running " + serverType_.toLowerCase() + " TCP server. Bad input arguments.");
            initializeSuccess = false;
            return;
        }

        try {
            bossGroup_ = new NioEventLoopGroup();
            workerGroup_ = new NioEventLoopGroup();

            ServerBootstrap b = new ServerBootstrap();

            if (serverType_.equals(SERVER_TYPE_STATSD)) {
                b.group(bossGroup_, workerGroup_).channel(NioServerSocketChannel.class).childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline().addLast(
                                new StringEncoder(CharsetUtil.UTF_8),
                                new LineBasedFrameDecoder(32767),
                                new StringDecoder(CharsetUtil.UTF_8), 
                                new TcpServerHandler_Statsd());
                    }
                });
            }
            else if (serverType_.equals(SERVER_TYPE_GRAPHITE_AGGREGATOR)) {
                b.group(bossGroup_, workerGroup_).channel(NioServerSocketChannel.class).childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline().addLast(
                                new StringEncoder(CharsetUtil.UTF_8),
                                new LineBasedFrameDecoder(32767),
                                new StringDecoder(CharsetUtil.UTF_8), 
                                new TcpServerHandler_GraphiteAggregator());
                    }
                });
            }
            else if (serverType_.equals(SERVER_TYPE_GRAPHITE_PASSTHROUGH)) {
                b.group(bossGroup_, workerGroup_).channel(NioServerSocketChannel.class).childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline().addLast(
                                new StringEncoder(CharsetUtil.UTF_8),
                                new LineBasedFrameDecoder(32767),
                                new StringDecoder(CharsetUtil.UTF_8), 
                                new TcpServerHandler_GraphitePassthrough());
                    }
                });
            }
            else if (serverType_.equals(SERVER_TYPE_OPENTSDB_TELNET)) {
                b.group(bossGroup_, workerGroup_).channel(NioServerSocketChannel.class).childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline().addLast(
                                new StringEncoder(CharsetUtil.UTF_8),
                                new LineBasedFrameDecoder(32767),
                                new StringDecoder(CharsetUtil.UTF_8), 
                                new TcpServerHandler_OpenTsdb());
                    }
                });
            }
            
            b.bind(port_).sync().channel().closeFuture().sync();
        }
        catch (Exception e) {
            initializeSuccess = false;
            logger.error(e.toString() + " - Port=" + port_ + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        finally {
            shutdownServer();
        }
    }

    @Override
    public void shutdownServer() {

        try {
            if (workerGroup_ != null) {
                Future futureWorkerGroup = workerGroup_.shutdownGracefully();
                futureWorkerGroup.await();
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
        try {
            if (bossGroup_ != null) {
                Future futureBossGroup = bossGroup_.shutdownGracefully();
                futureBossGroup.await();
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }

        try {
            if (bossGroup_ != null) {
                while ((bossGroup_ != null) && !bossGroup_.isTerminated()) {
                    Threads.sleepSeconds(1);

                    boolean isGroupTerminated = false;
                    try {
                        isGroupTerminated = bossGroup_.isTerminated();
                    }
                    catch (Exception e) {
                        isGroupTerminated = true;
                    }

                    logger.info("Is " + serverType_.toLowerCase() + " TCP Server BossGroup Terminated? " + isGroupTerminated);
                }

                bossGroup_ = null;
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        finally {
            bossGroup_ = null;
        }
        
        try {
            if (workerGroup_ != null) {
                while ((workerGroup_ != null) && !workerGroup_.isTerminated()) {
                    Threads.sleepSeconds(1);

                    boolean isGroupTerminated = false;
                    try {
                        isGroupTerminated = workerGroup_.isTerminated();
                    }
                    catch (Exception e) {
                        isGroupTerminated = true;
                    }

                    logger.info("Is " + serverType_.toLowerCase() + " TCP Server WorkerGroup Terminated? " + isGroupTerminated);
                }

                workerGroup_ = null;
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        finally {
            workerGroup_ = null;
        }
    }

    public boolean isInitializeSuccess() {
        return initializeSuccess;
    }
    
}