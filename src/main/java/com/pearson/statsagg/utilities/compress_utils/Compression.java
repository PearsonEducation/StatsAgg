package com.pearson.statsagg.utilities.compress_utils;

import com.pearson.statsagg.utilities.string_utils.StringUtilities;
import com.pearson.statsagg.utilities.core_utils.StackTrace;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.deflate.DeflateCompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipParameters;
import org.apache.commons.compress.compressors.xz.XZCompressorInputStream;
import org.apache.commons.compress.compressors.xz.XZCompressorOutputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class Compression {
    
    private static final Logger logger = LoggerFactory.getLogger(Compression.class.getName());
    public static boolean createTar(String tarFilePathAndName, String parentPath, Set<String> tarEntryFilePathsAndNames) {
        
        if ((tarFilePathAndName == null) || (parentPath == null) || (tarEntryFilePathsAndNames == null)) {
            return false;
        }
         
        boolean tarSuccess = true;
        
        OutputStream outputStream = null;
        TarArchiveOutputStream tarArchiveOutputStream = null;
        
        try {
            String sanitizedParentPath = new File(parentPath).getAbsolutePath() + File.separator;
            
            File tarFile = new File(tarFilePathAndName);
            outputStream = new FileOutputStream(tarFile);
            tarArchiveOutputStream = (TarArchiveOutputStream) new ArchiveStreamFactory().createArchiveOutputStream("tar", outputStream);

            for (String tarEntryFilePathAndName : tarEntryFilePathsAndNames){
                File file = new File(tarEntryFilePathAndName);

                String tarEntryRelativePathFilePathAndName = file.getAbsolutePath().replace(sanitizedParentPath, "");
                TarArchiveEntry entry = new TarArchiveEntry(file, tarEntryRelativePathFilePathAndName);
                tarArchiveOutputStream.putArchiveEntry(entry);
                
                if (!file.isDirectory()) {
                    FileInputStream fileInputStream = new FileInputStream(file);
                    IOUtils.copy(fileInputStream, tarArchiveOutputStream);
                    fileInputStream.close();
                }
               
                tarArchiveOutputStream.closeArchiveEntry();
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            tarSuccess = false;
        }
        finally {
            try {
                if (tarArchiveOutputStream != null) tarArchiveOutputStream.finish();
            }
            catch (Exception e) {
                logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
                tarSuccess = false;
            }
            
            try {
                if (tarArchiveOutputStream != null) tarArchiveOutputStream.close();
            }
            catch (Exception e) {
                logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
                tarSuccess = false;
            }
            
            try {
                if (outputStream != null) outputStream.close();
            }
            catch (Exception e) {
                logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
                tarSuccess = false;
            }
            
            return tarSuccess;
        }
    }
  
    /* modified from code on this page: http://stackoverflow.com/questions/315618/how-do-i-extract-a-tar-file-in-java */
    public static boolean unTar(String outputDirectory, String inputTarFile) {
        
        if ((outputDirectory == null) || (inputTarFile == null)) {
            return false;
        }
        
        boolean unTarDirectoriesSuccess = unTar_Directories(outputDirectory, inputTarFile);
        
        boolean unTarFilesSuccess = false;
        if (unTarDirectoriesSuccess) {
            unTarFilesSuccess = unTar_Files(outputDirectory, inputTarFile);
        }
        
        return unTarDirectoriesSuccess && unTarFilesSuccess;
    }

    private static boolean unTar_Directories(String outputDirectory, String inputTarFile) {
        
        if ((outputDirectory == null) || (inputTarFile == null)) {
            return false;
        }
        
        boolean untarSuccess = true;
        
        InputStream inputStream = null;
        TarArchiveInputStream tarArchiveInputStream = null;
    
        try {
            inputStream = new FileInputStream(inputTarFile);
            tarArchiveInputStream = (TarArchiveInputStream) new ArchiveStreamFactory().createArchiveInputStream("tar", inputStream);
            TarArchiveEntry entry = tarArchiveInputStream.getNextTarEntry();
            
            while (entry != null) {
                File outputFile = new File(outputDirectory, entry.getName());
                
                if (entry.isDirectory()) {
                    if (!outputFile.exists()) {
                        outputFile.mkdirs();
                    }
                }

                entry = tarArchiveInputStream.getNextTarEntry();
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            untarSuccess = false;
        }
        finally {            
            try {
                if (tarArchiveInputStream != null) tarArchiveInputStream.close();
            }
            catch (Exception e) {
                logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
                untarSuccess = false;
            }
            
            try {
                if (inputStream != null) inputStream.close();
            }
            catch (Exception e) {
                logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
                untarSuccess = false;
            }
            
            return untarSuccess;
        }
    }
    
    private static boolean unTar_Files(String outputDirectory, String inputTarFile) {
        
        if ((outputDirectory == null) || (inputTarFile == null)) {
            return false;
        }
        
        boolean untarSuccess = true;
        
        InputStream inputStream = null;
        TarArchiveInputStream tarArchiveInputStream = null;
    
        try {
            inputStream = new FileInputStream(inputTarFile);
            tarArchiveInputStream = (TarArchiveInputStream) new ArchiveStreamFactory().createArchiveInputStream("tar", inputStream);
            TarArchiveEntry entry = tarArchiveInputStream.getNextTarEntry();
            
            while (entry != null) {
                File outputFile = new File(outputDirectory, entry.getName());
                
                if (!entry.isDirectory()) {
                    OutputStream outputStream = new FileOutputStream(outputFile);
                    IOUtils.copy(tarArchiveInputStream, outputStream);
                    outputStream.close();
                }
                
                outputFile.setLastModified(entry.getModTime().getTime());
                
                entry = tarArchiveInputStream.getNextTarEntry();
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            untarSuccess = false;
        }
        finally {            
            try {
                if (tarArchiveInputStream != null) tarArchiveInputStream.close();
            }
            catch (Exception e) {
                logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
                untarSuccess = false;
            }
            
            try {
                if (inputStream != null) inputStream.close();
            }
            catch (Exception e) {
                logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
                untarSuccess = false;
            }
            
            return untarSuccess;
        }
    }
    
    public static boolean createXZ(String fileToCompressPathAndName, String xzFilePathAndName) {
        return createXZ(fileToCompressPathAndName, xzFilePathAndName, 9);
    }
    
    public static boolean createXZ(String fileToCompressPathAndName, String xzFilePathAndName, int compressionPreset) {
        
        if ((fileToCompressPathAndName == null) || (xzFilePathAndName == null) || (compressionPreset < 0)) {
            return false;
        }
                
        try {     
            File file = new File(fileToCompressPathAndName);
            if (file.isDirectory()) {
                return false;
            }
        }
        catch (Exception e) {
            logger.warn(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return false;
        }
        
        boolean compressSuccess = true;
        
        FileInputStream fileInputStream = null;
        BufferedInputStream bufferedInputStream = null;
        FileOutputStream fileOutputStream = null;
        XZCompressorOutputStream xzCompressorOutputStream = null;
            
        try {
            fileInputStream = new FileInputStream(fileToCompressPathAndName);
            bufferedInputStream = new BufferedInputStream(fileInputStream);
            fileOutputStream = new FileOutputStream(xzFilePathAndName);
            xzCompressorOutputStream = new XZCompressorOutputStream(fileOutputStream, compressionPreset);

            byte[] buffer = new byte[1024 * 8];
            
            int n = 0;
            while (-1 != (n = bufferedInputStream.read(buffer))) {
                xzCompressorOutputStream.write(buffer, 0, n);
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            compressSuccess = false;
        }
        finally {
            try {
                if (xzCompressorOutputStream  != null) xzCompressorOutputStream.close();
            }
            catch (Exception e) {
                logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
                compressSuccess = false;
            }
            
            try {
                if (fileOutputStream  != null) fileOutputStream.close();
            }
            catch (Exception e) {
                logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
                compressSuccess = false;
            }
            
            try {
                if (bufferedInputStream  != null) bufferedInputStream.close();
            }
            catch (Exception e) {
                logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
                compressSuccess = false;
            }
            
            try {
                if (fileInputStream  != null) fileInputStream.close();
            }
            catch (Exception e) {
                logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
                compressSuccess = false;
            }
            
            return compressSuccess;
        }
    }
   
    public static boolean decompressXZ(String decompressFilename, String xzFilePathAndName) {
        
        if ((decompressFilename == null) || (xzFilePathAndName == null)) {
            return false;
        }
        
        boolean decompressSuccess = true;
        
        FileInputStream fileInputStream = null;
        BufferedInputStream bufferedInputStream = null;
        XZCompressorInputStream xzCompressorInputStream = null;
        FileOutputStream fileOutputStream = null;
        
        try {
            fileInputStream = new FileInputStream(xzFilePathAndName);
            bufferedInputStream = new BufferedInputStream(fileInputStream);
            xzCompressorInputStream = new XZCompressorInputStream(bufferedInputStream);
            fileOutputStream = new FileOutputStream(decompressFilename);

            byte[] buffer = new byte[1024 * 8];
            
            int n = 0;
            while (-1 != (n = xzCompressorInputStream.read(buffer))) {
                fileOutputStream.write(buffer, 0, n);
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            decompressSuccess = false;
        }
        finally {
            try {
                if (fileOutputStream != null) fileOutputStream.close();
            }
            catch (Exception e) {
                logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
                decompressSuccess = false;
            }
            
            try {
                if (xzCompressorInputStream != null) xzCompressorInputStream.close();
            }
            catch (Exception e) {
                logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
                decompressSuccess = false;
            }
            
            try {
                if (bufferedInputStream != null) bufferedInputStream.close();
            }
            catch (Exception e) {
                logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
                decompressSuccess = false;
            }
            
            try {
                if (fileInputStream != null) fileInputStream.close();
            }
            catch (Exception e) {
                logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
                decompressSuccess = false;
            }
            
            return decompressSuccess;
        }
    }

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
