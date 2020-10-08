package com.pearson.statsagg.network.http;

import com.pearson.statsagg.network.JettyServer;
import com.pearson.statsagg.utilities.core_utils.StackTrace;
import com.pearson.statsagg.web_api.InfluxdbV1_Write;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class JettyInfluxdb implements JettyServer {

    private static final Logger logger = LoggerFactory.getLogger(JettyInfluxdb.class.getName());

    private final int port_;
    private final int stopServerTimeout_;
    private Server jettyServer_ = null;
    
    public JettyInfluxdb(int port, int stopServerTimeout) {
        port_ = port;
        stopServerTimeout_ = stopServerTimeout;
    }
    
    public void startServer() {
        jettyServer_ = new Server(port_);
        jettyServer_.setStopAtShutdown(true);
        jettyServer_.setStopTimeout(stopServerTimeout_);
        ServletHandler handler = new ServletHandler();
        jettyServer_.setHandler(handler);
        handler.addServletWithMapping(InfluxdbV1_Write.class, "/db/*");
        
        try {
            jettyServer_.start();
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
    }
    
    @Override
    public boolean isRunning() {
        if (jettyServer_ == null) return false;
        
        try {
            return jettyServer_.isRunning();
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return false;
        }
    }
    
    @Override
    public void stopServer() {
        try {
            jettyServer_.stop();
            jettyServer_ = null;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
    }
    
}
