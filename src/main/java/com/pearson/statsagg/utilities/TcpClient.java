package com.pearson.statsagg.utilities;

import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public final class TcpClient {

    private static final Logger logger = LoggerFactory.getLogger(TcpClient.class.getName());
    
    private final String host_;
    private final int port_;
    
    private Socket socket_ = null;
    private BufferedWriter bufferedWriter_ = null;
    private DataOutputStream dataOutputStream_ = null;
            
    public TcpClient(String host, int port, boolean connectImmediately) {
        this.host_ = host;
        this.port_ = port;
        
        if (connectImmediately) {
            connect();
        }
    }

    public boolean reset() {
        close();
        return connect();
    }
    
    public boolean connect() {   
        
        boolean isConnectSuccess;
        
        try {
            socket_ = new Socket(host_, port_);
            dataOutputStream_ = new DataOutputStream(socket_.getOutputStream());
            bufferedWriter_ = new BufferedWriter(new OutputStreamWriter(dataOutputStream_));
            
            isConnectSuccess = isConnected();
        } 
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            isConnectSuccess = false;
        }
        
        return isConnectSuccess;
    }
    
    public void close() {
            
        if (bufferedWriter_ != null) {
            try {
                bufferedWriter_.close();
                bufferedWriter_ = null;
            } 
            catch (Exception e) {
                logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            }
        }

        if (dataOutputStream_ != null) {
            try {
                dataOutputStream_.close();
                dataOutputStream_= null;
            } 
            catch (Exception e) {
                logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            }
        }
        
        if (socket_ != null) {
            try {
                socket_.close();
                socket_ = null;
            } 
            catch (Exception e) {
                logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            }
        }
        
    }
    
    public boolean send(String dataString, int numRetries, boolean closeClientOnExit, boolean flushImmediately) {
        
        boolean isSendSucess = false;
        
        for (int i = 0; (i <= numRetries) && !isSendSucess; i++) {
            if (!isSendSucess && !isConnected()) {
                reset();
            }
            
            if (isConnected()) {
                isSendSucess = send(dataString, closeClientOnExit, flushImmediately);
            }
        }
        
        return isSendSucess;
    }
    
    public boolean send(String dataString, boolean closeClientOnExit, boolean flushImmediately) {
        
        if (!isConnected()) {
            return false;
        }
        
        boolean isSendSuccess = false;
        
        try {
            if ((socket_ != null) && (dataOutputStream_ != null) && (bufferedWriter_ != null)) {
                bufferedWriter_.write(dataString);
                
                if (flushImmediately) {
                    bufferedWriter_.flush();
                }
                
                isSendSuccess = true;
            }
            else {
                isSendSuccess = false;
            }
        } 
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            isSendSuccess = false;
        } 
        finally {
            if (closeClientOnExit) {
                close();
            }
        }
        
        return isSendSuccess;
    }
    
    public boolean isConnected() {
        
        if (socket_ == null) {
            return false;
        }
        
        try {
            return (socket_.isConnected() && !socket_.isClosed());
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return false;
        }
        
    }
    
    public boolean isClosed() {
        
        if (socket_ == null) {
            return true;
        }
        
        try {
            return (socket_.isClosed());
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return false;
        }
        
    }
    
}
