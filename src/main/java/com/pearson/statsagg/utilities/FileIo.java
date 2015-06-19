package com.pearson.statsagg.utilities;

import java.io.*;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class FileIo {

    private static final Logger logger = LoggerFactory.getLogger(FileIo.class.getName());
    
    /**
     * Creates a directory. 
     * @return Returns false if unsuccessful or if an exception was encountered. Returns true if the directory was successfully created.
     */
    public static boolean createDirectory(String parentPath, String directoryName) {
        
        if ((parentPath == null) || parentPath.isEmpty() || (directoryName == null) || directoryName.isEmpty()) {
            return false;
        }

        try {
            File directory = new File(parentPath + File.separator + directoryName);
            boolean doesDirectoryExist = directory.exists();

            boolean createDirectorySuccess = true;
            if (!doesDirectoryExist) {
                createDirectorySuccess = directory.mkdir();
            }

            return createDirectorySuccess;
        }
        catch (Exception e) {
            logger.debug(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return false;
        }
    }

    /**
     * This is a quiet method.
     */    
    public static boolean deleteFile(String filePath, String filename) {
        
        if ((filePath == null) || filePath.isEmpty() || (filename == null) || filename.isEmpty()) {
            return false;
        }
        
        return deleteFile(filePath + File.separator + filename);
    }
    
    /**
     * This is a quiet method.
     */
    public static boolean deleteFile(String filePathAndName) {
        
        if ((filePathAndName == null) || filePathAndName.isEmpty()) {
            return false;
        }
        
        boolean isSuccessfulDelete = false;

        try {
            File fileToDelete = new File(filePathAndName);
            isSuccessfulDelete = fileToDelete.delete();
        }
        catch (Exception e) {
            logger.debug(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            isSuccessfulDelete = false;
        }
        
        return isSuccessfulDelete;
    }
    
    /**
     * This is a quiet method.
     */
    public static boolean deleteFilesInADirectory(String directoryPath) {
        
        if ((directoryPath == null) || directoryPath.isEmpty()) {
            return false;
        }
        
        boolean isSuccessfulDelete = true;

        List<File> files = getListOfFilesInADirectory(directoryPath);
        
        if (files == null) {
            return false;
        }
        
        try {
            for (File file : files) {
                boolean fileDeleteSuccess = deleteFile(directoryPath, file.getName());

                if (!fileDeleteSuccess) {
                    isSuccessfulDelete = false;
                }
            }
        }
        catch (Exception e) {
            logger.debug(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            isSuccessfulDelete = false;
        }
        
        return isSuccessfulDelete;
    }
    
    /**
     * This is a quiet method.
     * Only deletes files. Doesn't delete directories.
     */
    public static boolean deleteDirectoryFiles(Set<String> inputFilePathsAndNames) {
        
        if ((inputFilePathsAndNames == null)) {
            return false;
        }

        boolean didSuccessfullyDeleteAllFiles = true; 
        
        try {
            for (String filePathAndName : inputFilePathsAndNames) {
                File file = new File(filePathAndName);

                if (!file.isDirectory()) {
                    boolean deleteSuccess = deleteFile(filePathAndName);

                    if (!deleteSuccess) {
                        logger.debug("Warning - " + filePathAndName + " failed to delete");
                        didSuccessfullyDeleteAllFiles = false;
                    }
                }
            }
        }
        catch (Exception e) {
            logger.debug(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return false;
        }
        
        return didSuccessfullyDeleteAllFiles;
    }
    
    /**
     * This is a quiet method.
     * Only deletes directories. Will fail if the directories have files in them.
     */
    public static boolean deleteDirectorySubdirectories(String rootDirectory) {
        
        if ((rootDirectory == null)) {
            return false;
        }

        boolean didSuccessfullyDeleteAllDirectories = true; 
        
        try {
            List<File> files = getListOfFilesInADirectory(rootDirectory);

            for (File file : files) {
                if (file.isDirectory()) {
                    boolean deleteSuccess = deleteDirectoryAndContents(file);

                    if (!deleteSuccess) {
                        logger.debug("Warning - failed to delete " + file.getAbsolutePath());
                        didSuccessfullyDeleteAllDirectories = false;
                    }
                }
            }
        }
        catch (Exception e) {
            logger.debug(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return false;
        }
        
        return didSuccessfullyDeleteAllDirectories;
    }
    
    /**
     * This is a quiet method.
     * Method template code from http://www.exampledepot.com/egs/java.io/DeleteDir.html
     */
    public static boolean deleteDirectoryAndContents(File rootDirectory) {
        
        if ((rootDirectory == null) || !rootDirectory.isDirectory()) {
            return false;
        } 
        
        try {
            String[] directoryContents = rootDirectory.list();
            for (int i = 0; i < directoryContents.length; i++) {
                boolean success = deleteDirectoryAndContents(new File(rootDirectory, directoryContents[i]));

                if (!success) {
                    return false;
                }
            }
                
            return rootDirectory.delete();
        }
        catch (Exception e) {
            logger.debug(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return false;
        }
    }
    
    /**
     * This is a quiet method.
     */
    public static boolean doesFileExist(String filePathAndName) {
        
        if ((filePathAndName == null) || filePathAndName.isEmpty()) {
            return false;
        }

        File file = new File(filePathAndName);
        
        boolean doesFileExist;
        
        try {
            doesFileExist = file.exists();
        }
        catch (Exception e) {
            logger.debug(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            doesFileExist = false;
        }

        return doesFileExist;
    }
    
    /**
     * This is a quiet method.
     */
    public static boolean doesFileExist(File file) {
        
        if (file == null) {
            return false;
        }
        
        boolean doesFileExist;
        
        try {
            doesFileExist = file.exists();
        }
        catch (Exception e) {
            logger.debug(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            doesFileExist = false;
        }

        return doesFileExist;
    }
    
    /**
     * This is a quiet method.
     */
    public static boolean doesFileExist(String filePath, String filename) {
        if ((filePath == null) || filePath.isEmpty() || (filename == null) || filename.isEmpty()) {
            return false;
        }
        
        return doesFileExist(filePath + File.separator + filename);
    }
    
    public static long getFileLastModified(String filePathAndName) {
        File file = new File(filePathAndName);
        long lastModified = file.lastModified();

        return lastModified;
    }
        
    /**
     * This is a quiet method.
     */
    public static boolean renameFile(String filePath, String oldFilename, String newFilename) {
        
        if ((filePath == null) || filePath.isEmpty() ||
                (oldFilename == null) || oldFilename.isEmpty() || 
                (newFilename == null) || newFilename.isEmpty()) {
            return false;
        }
        
        File oldFile = new File(filePath + File.separator + oldFilename);
        File newFile = new File(filePath + File.separator + newFilename);

        boolean isSuccessfulRename;
        
        try {
            isSuccessfulRename = oldFile.renameTo(newFile);
        }
        catch (Exception e) {
            logger.debug(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            isSuccessfulRename = false;
        }
        
        return isSuccessfulRename;
    }

    public static boolean copyFile(String sourceFilePath, String sourceFilename, String destinationFilePath, String destinationFilename) {
        
        if ((sourceFilePath == null) || sourceFilePath.isEmpty() ||
                (sourceFilename == null) || sourceFilename.isEmpty() || 
                (destinationFilePath == null) || destinationFilePath.isEmpty() ||
                (destinationFilename == null) || destinationFilename.isEmpty()) {
            return false;
        }

        boolean isCopySuccess = false;
        
        FileChannel sourceFileChannel = null;
        FileChannel destinationFileChannel = null;

        try {
            File sourceFile = new File(sourceFilePath + File.separator + sourceFilename);
            File destinationFile = new File(destinationFilePath + File.separator + destinationFilename);

            if (sourceFile.exists()) {

                if (!destinationFile.exists()) {
                    destinationFile.createNewFile();
                }

                sourceFileChannel = new FileInputStream(sourceFile).getChannel();
                destinationFileChannel = new FileOutputStream(destinationFile).getChannel();

                destinationFileChannel.transferFrom(sourceFileChannel, 0, sourceFileChannel.size());
                
                isCopySuccess = true;
            }
        }
        catch (Exception e) {
            logger.debug(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        finally {
            if (sourceFileChannel != null) {
                try {
                    sourceFileChannel.close();
                }
                catch (Exception e) {
                    logger.debug(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
                }
            }
            
            if (destinationFileChannel != null) {
                try {
                    destinationFileChannel.close();
                }
                catch (Exception e) {
                    logger.debug(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
                }
            }
            
            return isCopySuccess;
        }
    }
    
    /**
     * This is a quiet method.
     */
    public static List<String> getListOfDirectoryNamesInADirectory(String directoryPath) {
        
        if ((directoryPath == null) || directoryPath.isEmpty()) {
            return new ArrayList<>();
        }

        File directory = new File(directoryPath);
        String[] filenames;
        
        try {
            filenames = directory.list();
        }
        catch (Exception e) {
           logger.debug(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
           return new ArrayList<>();
        }
        
        List<String> directoryNamesList = new ArrayList<>();
        
        if (filenames != null) {
            for (int i = 0; i < filenames.length; i++) {
                try {
                    File file = new File(directoryPath + File.separator + filenames[i]);

                    if (file.isDirectory()) {
                        directoryNamesList.add(file.getName());
                    }
                }
                catch (Exception e) {
                    logger.debug(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
                }
            }
        }
        
        return directoryNamesList;
    }
    
    /**
     * This is a quiet method.
     */
    public static List<File> getListOfDirectoryFilesInADirectory(String directoryPath) {
        
        if ((directoryPath == null) || directoryPath.isEmpty()) {
            return new ArrayList<>();
        }

        File directory = new File(directoryPath);
        String[] filenames;
        
        try {
            filenames = directory.list();
        }
        catch (Exception e) {
           logger.debug(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
           return new ArrayList<>();
        }
        
        List<File> directoryFilesList = new ArrayList<>();
        
        if (filenames != null) {
            for (int i = 0; i < filenames.length; i++) {
                try {
                    File file = new File(directoryPath + File.separator + filenames[i]);
                    
                    if (file.isDirectory()) {
                        directoryFilesList.add(file);
                    }
                }
                catch (Exception e) {
                    logger.debug(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
                }
            }
        }
        
        return directoryFilesList;
    }
    
    /**
     * This is a quiet method.
     */
    public static List<String> getListOfFilenamesInADirectory(String directoryPath) {
        
        if ((directoryPath == null) || directoryPath.isEmpty()) {
            return new ArrayList<>();
        }

        File directory = new File(directoryPath);
        String[] filenames;
        
        try {
            filenames = directory.list();
        }
        catch (Exception e) {
            logger.debug(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return new ArrayList<>();
        }
        
        List<String> filenamesList = new ArrayList<>();
        
        for (int i = 0; i < filenames.length; i++) {
            filenamesList.add(filenames[i]);
        }
        
        return filenamesList;
    }
    
    /**
     * This is a quiet method.
     */
    public static List<File> getListOfFilesInADirectory(String directoryPath) {
        
        if ((directoryPath == null) || directoryPath.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<File> files = new ArrayList<>();
        
        File directory = new File(directoryPath);
        String[] filenames;
        
        try {
            filenames = directory.list();
        }
        catch (Exception e) {
           logger.debug(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
           return new ArrayList<>();
        }
        
        if (filenames != null) {
            for (int i = 0; i < filenames.length; i++) {
                try {
                    File file = new File(directoryPath + File.separator + filenames[i]);

                    if (file.exists()) {
                        files.add(file);
                    }
                }
                catch (Exception e) {
                    logger.debug(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
                }
            }
        }
        
        return files;
    }
    
    /**
     * This is a quiet method.
     */
    public static List<File> getListOfFilesInADirectory(String directoryPath, List<String> filenames) {
        
        if ((directoryPath == null) || directoryPath.isEmpty() || (filenames == null) || filenames.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<File> files = new ArrayList<>();
 
        for (int i = 0; i < filenames.size(); i++) {
            try {
                File file = new File(directoryPath + File.separator + filenames.get(i));

                if (file.exists()) {
                    files.add(file);
                }
            }
            catch (Exception e) {
                logger.debug(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            }
        }
        
        return files;
    }
    
    public static String getFileExtensionFromFilename(String filename) {
        
        if ((filename == null) || filename.isEmpty()) {
            return null;
        }
        
        int indexOfBeginExtension = filename.lastIndexOf('.');

        if ((indexOfBeginExtension != -1) && ((indexOfBeginExtension + 1) != filename.length())) {
            String fileExtension = filename.substring(indexOfBeginExtension + 1, filename.length());
            return fileExtension;
        }
        else {
            return null;
        }
    }
    
    public static String getFilenameWithoutExtension(String filename) {
        
        if ((filename == null) || filename.isEmpty()) {
            return null;
        }
        
        int indexOfBeginExtension = filename.lastIndexOf('.');

        if ((indexOfBeginExtension != -1) && ((indexOfBeginExtension + 1) != filename.length())) {
            String filenameWithoutExtension = filename.substring(0, indexOfBeginExtension);
            return filenameWithoutExtension;
        }
        else {
            return null;
        }
    }
    
    /**
     * This is a quiet method.
     */
    public static boolean saveStringToFile(String saveFilePath, String saveFilename, String saveString) {
        return saveStringToFile(saveFilePath + File.separator + saveFilename, saveString);
    }
    
    /**
     * This is a quiet method.
     */
    public static boolean saveStringToFile(String saveFilePathAndName, String saveString) {
        
        if ((saveFilePathAndName == null) || saveFilePathAndName.isEmpty() ||
                (saveString == null) || saveString.isEmpty()) {
            return false;
        }
        
        BufferedWriter writer = null;
        
        try {  
            File outputFile = new File(saveFilePathAndName);      
            writer = new BufferedWriter(new FileWriter(outputFile));
            
            writer.write(saveString);
            
            return true;
        }
        catch (Exception e) {
            logger.debug(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return false;
        }
        finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            }
            catch (Exception e){
                logger.debug(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            }
        }
    }
    
    public static String readFileToString(String filePath, String filename) {
        String filePathAndName = filePath + File.separator + filename;
        return readFileToString(filePathAndName);
    }
    
    public static String readFileToString(String filePathAndName) {
        
        if ((filePathAndName == null) || (filePathAndName.length() == 0)) {
            return null;
        }
        
        File file;
                
        try {
            file = new File(filePathAndName);
        }
        catch (Exception e) {
            logger.debug(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        
        return readFileToString(file);
    }
    
    /**
     * This is a quiet method.
     */
    public static String readFileToString(File file) {
        
        if (file == null) {
            return null;
        }
        
        BufferedReader reader = null;
        
        try {
            reader = new BufferedReader(new FileReader(file));
            
            StringBuilder fileContents = new StringBuilder();
            
            String currentLine = reader.readLine();
            while (currentLine != null) {
                fileContents.append(currentLine);
                fileContents.append(System.lineSeparator());
                
                currentLine = reader.readLine();
            } 
            
            return fileContents.toString();
        }
        catch (Exception e) {
            logger.debug(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            }
            catch (Exception e) {
                logger.debug(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            }
        }
    }

    public static String readFileToString(File file, int numRetries, int timeBetweenRetriesInMilliseconds) {
        
        if ((file == null) || (numRetries < 0) || (timeBetweenRetriesInMilliseconds < 0)) {
            return null;
        }
        
        String fileContents = null;
        
        for (int i = 0; (i <= numRetries) && (fileContents == null); i++) {
            fileContents = readFileToString(file);

            if (fileContents == null) {
                Threads.sleepMilliseconds(timeBetweenRetriesInMilliseconds);
            }
        }
        
        return fileContents;
    }
    
}
