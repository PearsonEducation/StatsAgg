package com.pearson.statsagg.utilities;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class ProcessUtils {
    
    private static final Logger logger = LoggerFactory.getLogger(ProcessUtils.class.getName());

    public static String runProcessAndGetProcessOutput(String commands) {
        
        String output = null;
                
        try {
            String[] commandAndArgs = commands.split(" ");
            ProcessBuilder processBuilder  = new ProcessBuilder().command(commandAndArgs);
            Process process = processBuilder.start();
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder processOutput = new StringBuilder();
            String line;
            
            while ((line = reader.readLine()) != null) {
               processOutput.append(line);
               processOutput.append(System.getProperty("line.separator"));
            }
            
            output = processOutput.toString();
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
        return output;
    }
    
}
