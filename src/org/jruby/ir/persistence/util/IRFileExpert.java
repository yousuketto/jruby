package org.jruby.ir.persistence.util;

import java.io.File;

import org.jruby.platform.Platform;
import org.jruby.util.SafePropertyAccessor;

public class IRFileExpert {
    public static final IRFileExpert INSTANCE = new IRFileExpert();
    
    private static final String IR_FILE_EXTENSION = ".ir";
    private static final String EXTENSION_SEPARATOR = ".";

    private static final String USER_HOME = "user.home";
    private static final String ENV_VARIABLE_NAME = "IR_HOME";
    private static final String IR_FOLDER_NAME = "ir";
    
    private static final File IR_ROOT_FOLDER;
    
    static {
        /* Find out IR_ROOT_FOLDER */
        
        // Try to find among environment variables
        final String pathFromEnvVariable = SafePropertyAccessor.getenv(ENV_VARIABLE_NAME);
        
        final boolean correctEnvironmentVariableIsSet = ( pathFromEnvVariable != null ) && ( new File(pathFromEnvVariable).isDirectory() );
        final String irRootParentFolder;
        if (correctEnvironmentVariableIsSet) {
            irRootParentFolder = pathFromEnvVariable;
        } else { // Than ir folder will be situated in user home directory
            if (Platform.IS_WINDOWS) { // (see http://bugs.sun.com/view_bug.do?bug_id=4787931)
                String homeDrive = System.getenv("HOMEDRIVE");
                String homePath = System.getenv("HOMEPATH");
                if (homeDrive != null && homePath != null) {
                    irRootParentFolder = (homeDrive + homePath).replace('\\', '/');
                } else {
                    irRootParentFolder = SafePropertyAccessor.getProperty(USER_HOME);
                }
                
            } else {
                irRootParentFolder = SafePropertyAccessor.getProperty(USER_HOME);
            }
        }
        
        IR_ROOT_FOLDER = new File(irRootParentFolder, IR_FOLDER_NAME);
        
    }

    private IRFileExpert() {};
 
    /** 
     * Must be called after writing ir file to disk, otherwise it won't change mtime of file
     */
    public void rememberModificationTimeForIr(final String rbFilePath) {
        final File rbFile = getRbFile(rbFilePath);
        final File irFile = getIrFileByRbFile(rbFile, false);
        
        irFile.setLastModified(rbFile.lastModified());
    }
    
    public boolean persistedIrIsUpToDateForRbFile(final String fileName) {         
         final File rbFile = getRbFile(fileName);
         final File irFile = getIrFileByRbFile(rbFile, false);
         
         if(irFile.exists() && irFile.lastModified() == rbFile.lastModified()) {
             return true;
         } else {
             return false;
         }
    }
    
    public File getIrFileByRbFileForPersistence(final String fileName) {
        final File rbFile = getRbFile(fileName);
        
        return getIrFileByRbFile(rbFile, true);
    }
    
    public File getIrFileForReading(final String fileName) {
        final File rbFile = getRbFile(fileName);
        
        return getIrFileByRbFile(rbFile, false);
    }
    
    private File getIrFileByRbFile(final File rbFile, boolean isPersistence) {
        String absolutePathToRbFile = rbFile.getAbsolutePath();

        final int startOfFileName = absolutePathToRbFile.lastIndexOf(File.separator) + 1;
        
        final File irFolder;
        if (isPersistence) {
            irFolder = createIrFolderHierarchyIfNeeded(absolutePathToRbFile, startOfFileName);
        } else {
            irFolder = getIrFolderHierarchy(absolutePathToRbFile, startOfFileName);
        }
        
        final String irFileName = getIRFileName(absolutePathToRbFile, startOfFileName);
        
        final File irFile = new File(irFolder, irFileName);
        
        return irFile;
        
    }

    private File getRbFile(final String fileName) {
        final String normalizedFileName = normalizeFilePath(fileName);
        return new File(normalizedFileName);
    }

    private String normalizeFilePath(final String fileName) {
        if (fileName.startsWith("file:")) {
            // remove 'file:' marker
            final int indexOfFirstMeaningfulLetter = 5;
            return fileName.substring(indexOfFirstMeaningfulLetter);
        } else {
            return fileName;
        }
    }
    
    private File createIrFolderHierarchyIfNeeded(final String absolutePathToRbFile,
            final int startOfFileName) {
        
        File irFolder = getIrFolderHierarchy(absolutePathToRbFile, startOfFileName);
        
        if (!irFolder.exists()) {
            irFolder.mkdirs();
        }
        
        return irFolder;
    }

    private File getIrFolderHierarchy(final String absolutePathToRbFile, final int startOfFileName) {
        File irFolder = IR_ROOT_FOLDER;
        if (startOfFileName > 0) {
            String fileFolderPath = absolutePathToRbFile.substring(0, startOfFileName);
            irFolder = new File(irFolder, fileFolderPath);
        }
        return irFolder;
    }

    private String getIRFileName(final String absolutePathToRbFile, final int startOfFileName) {
        final int endOfFileName = absolutePathToRbFile.lastIndexOf(EXTENSION_SEPARATOR);
        String fileNameWithoutExtension;
        if (endOfFileName > 0) {
            fileNameWithoutExtension = absolutePathToRbFile.substring(startOfFileName, endOfFileName);
        } else {
            fileNameWithoutExtension = absolutePathToRbFile.substring(startOfFileName);
        }
        return fileNameWithoutExtension + IR_FILE_EXTENSION;
    }
}
