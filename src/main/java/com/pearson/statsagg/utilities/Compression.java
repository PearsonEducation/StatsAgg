package com.pearson.statsagg.utilities;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import org.apache.commons.compress.compressors.deflate.DeflateCompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class Compression {
    
    private static final Logger logger = LoggerFactory.getLogger(Compression.class.getName());
    
    public static String decompressDeflateToString(InputStream compressedData, String charset) {
        
        if (compressedData == null) {
            return null;
        } 
        
        Charset charsetToUse;
        
        try {
            charsetToUse = Charset.availableCharsets().get(charset);
            if (charsetToUse == null) charsetToUse = Charset.defaultCharset();
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            charsetToUse = Charset.defaultCharset();
        }
        
        byte[] bytes = decompressDeflateToByteArray(compressedData);
        
        if (bytes != null) {
            try {
                return new String(bytes, charsetToUse);
            }
            catch (Exception e) {
                logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
                return null;
            }
        }
        else {
            return null;
        }
    }
    
    public static byte[] decompressDeflateToByteArray(InputStream compressedData) {
        
        if (compressedData == null) {
            return null;
        } 
        
        BufferedInputStream bufferedInputStream = null;
        DeflateCompressorInputStream deflateCompressorInputStream = null;
        ByteArrayOutputStream byteArrayOutputStream = null;
        
        try {
            bufferedInputStream = new BufferedInputStream(compressedData);
            deflateCompressorInputStream = new DeflateCompressorInputStream(bufferedInputStream);
            byteArrayOutputStream = new ByteArrayOutputStream();
            final byte[] buffer = new byte[65536];
            int n = 0;
            while (-1 != (n = deflateCompressorInputStream.read(buffer))) byteArrayOutputStream.write(buffer, 0, n);

            return byteArrayOutputStream.toByteArray();
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        finally {
            try {
                if (byteArrayOutputStream != null) {
                    byteArrayOutputStream.close();
                    byteArrayOutputStream = null;
                }
            }
            catch (Exception e){}
            
            try {
                if (deflateCompressorInputStream != null) {
                    deflateCompressorInputStream.close();
                    deflateCompressorInputStream = null;
                }
            }
            catch (Exception e){}
            
            try {
                if (bufferedInputStream != null) {
                    bufferedInputStream.close();
                    bufferedInputStream = null;
                }
            }
            catch (Exception e){}
        }
    }
    
    public static String decompressGzipToString(InputStream compressedData, String charset) {
        
        if (compressedData == null) {
            return null;
        } 
        
        Charset charsetToUse;
        
        try {
            charsetToUse = Charset.availableCharsets().get(charset);
            if (charsetToUse == null) charsetToUse = Charset.defaultCharset();
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            charsetToUse = Charset.defaultCharset();
        }
        
        byte[] bytes = decompressGzipToByteArray(compressedData);
        
        if (bytes != null) {
            try {
                return new String(bytes, charsetToUse);
            }
            catch (Exception e) {
                logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
                return null;
            }
        }
        else {
            return null;
        }
    }
    
    public static byte[] decompressGzipToByteArray(InputStream compressedData) {
        
        if (compressedData == null) {
            return null;
        } 
        
        BufferedInputStream bufferedInputStream = null;
        GzipCompressorInputStream gzipCompressorInputStream = null;
        ByteArrayOutputStream byteArrayOutputStream = null;
        
        try {
            bufferedInputStream = new BufferedInputStream(compressedData);
            gzipCompressorInputStream = new GzipCompressorInputStream(bufferedInputStream);
            byteArrayOutputStream = new ByteArrayOutputStream();
            final byte[] buffer = new byte[65536];
            int n = 0;
            while (-1 != (n = gzipCompressorInputStream.read(buffer))) byteArrayOutputStream.write(buffer, 0, n);

            return byteArrayOutputStream.toByteArray();
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        finally {
            try {
                if (byteArrayOutputStream != null) {
                    byteArrayOutputStream.close();
                    byteArrayOutputStream = null;
                }
            }
            catch (Exception e){}
            
            try {
                if (gzipCompressorInputStream != null) {
                    gzipCompressorInputStream.close();
                    gzipCompressorInputStream = null;
                }
            }
            catch (Exception e){}
            
            try {
                if (bufferedInputStream != null) {
                    bufferedInputStream.close();
                    bufferedInputStream = null;
                }
            }
            catch (Exception e){}
        }
    }
    
}
