package com.pearson.statsagg.utilities;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.apache.commons.compress.compressors.deflate.DeflateCompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class Compression {
    
    private static final Logger logger = LoggerFactory.getLogger(Compression.class.getName());
        
    public static String decompressDeflateToString(InputStream compressedData, String charsetString) {
        
        if ((compressedData == null) || (charsetString == null)) {
            return null;
        } 
        
        Charset charsetToUse = StringUtilities.getCharsetFromString(charsetString);
        
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
    
    public static byte[] decompressDeflateToByteArray(byte[] compressedData) {
        
        if (compressedData == null) {
            return null;
        } 
        
        ByteArrayInputStream byteArrayInputStream = null;
        BufferedInputStream bufferedInputStream = null;
        DeflateCompressorInputStream deflateCompressorInputStream = null;
        ByteArrayOutputStream byteArrayOutputStream = null;
        
        try {
            byteArrayInputStream = new ByteArrayInputStream(compressedData);
            bufferedInputStream = new BufferedInputStream(byteArrayInputStream);
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
            
            try {
                if (byteArrayInputStream != null) {
                    byteArrayInputStream.close();
                    byteArrayInputStream = null;
                }
            }
            catch (Exception e){}
        }
    }
    
    public static byte[] compressStringToGzip(String inputString, GzipParameters gzipParameters) {
        
        if ((inputString == null) || inputString.isEmpty() || (gzipParameters == null)) {
            return null;
        }
        
        InputStream inputStream = null;
        BufferedInputStream bufferedInputStream = null;
        ByteArrayOutputStream byteArrayOutputStream = null;
        GzipCompressorOutputStream gzipCompressorOutputStream = null;
            
        try {
            inputStream = new ByteArrayInputStream(inputString.getBytes(StandardCharsets.UTF_8));
            bufferedInputStream = new BufferedInputStream(inputStream);
            byteArrayOutputStream = new ByteArrayOutputStream();
            gzipCompressorOutputStream = new GzipCompressorOutputStream(byteArrayOutputStream, gzipParameters);

            byte[] buffer = new byte[65536];
            
            int n = 0;
            while (-1 != (n = bufferedInputStream.read(buffer))) gzipCompressorOutputStream.write(buffer, 0, n);
            
            gzipCompressorOutputStream.finish();
            return byteArrayOutputStream.toByteArray();
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        finally {
            try {
                if (gzipCompressorOutputStream != null) {
                    gzipCompressorOutputStream.close();
                    gzipCompressorOutputStream = null;
                }
            }
            catch (Exception e){}
            
            try {
                if (byteArrayOutputStream != null) {
                    byteArrayOutputStream.close();
                    byteArrayOutputStream = null;
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
            
            try {
                if (inputStream != null) {
                    inputStream.close();
                    inputStream = null;
                }
            }
            catch (Exception e){}
        }
    }
    
    public static String decompressGzipToString(InputStream compressedData, String charsetString) {
        
        if ((compressedData == null) || (charsetString == null)) {
            return null;
        } 
        
        Charset charsetToUse = StringUtilities.getCharsetFromString(charsetString);

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
