package com.pearson.statsagg.servers;

/**
 * @author Jeffrey Schmidt
 */
public interface JettyServer {
    
    public boolean isRunning();
    
    public void stopServer();
     
}
