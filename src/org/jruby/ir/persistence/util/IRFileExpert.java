package org.jruby.ir.persistence.util;

import java.io.File;

public enum IRFileExpert {
    INSTANCE;

    private static final String IR_FILE_EXTENSION = ".ir";
    private static final String EXTENSION_SEPARATOR = ".";

    private static final String IR_FOLDER_NAME = "ir";
    private static final File IR_ROOT_FOLDER = new File(System.getProperty("user.home"), IR_FOLDER_NAME);

    public File getIRFileInIntendedPlace(final String fileName) {
        
        final String absolutePathToRbFile = getAbsolutePathToFile(fileName);

        final int startOfFileName = absolutePathToRbFile.lastIndexOf(File.separator) + 1;
        
        final File irFolder = createIRFolderHierarchy(absolutePathToRbFile, startOfFileName);
        final String irFileName = getIRFileName(absolutePathToRbFile, startOfFileName);

        return new File(irFolder, irFileName);
        
    }

    private String getAbsolutePathToFile(final String fileName) {
        final String normalizedFileName = fileName.replaceAll("file:", "");
        final File rbFile = new File(normalizedFileName);
        
        return rbFile.getAbsolutePath();
    }
    
    private File createIRFolderHierarchy(final String absolutePathToRbFile,
            final int startOfFileName) {
        File irFolder = IR_ROOT_FOLDER;
        if (startOfFileName > 0) {
            String fileFolderPath = absolutePathToRbFile.substring(0, startOfFileName);
            irFolder = new File(irFolder, fileFolderPath);
        }
        irFolder.mkdirs();
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
        final String irFileName = fileNameWithoutExtension + IR_FILE_EXTENSION;
        return irFileName;
    }

}
