/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.aokush.oasis.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author AKuseju
 */
public class Utilities {

    private static final Logger LOGGER = Logger.getLogger(Utilities.class.getName());
    
    public static final String[] SPECIAL_CHARS = {",", ":", " "};
    public static final String REPLACEMENT_CHAR = "_";
    
    /**
     * 
     * @param input
     * @param replacment
     * @return 
     */
    public static String replaceSpecialChars(String input, String replacment) {
        
        StringBuilder builder = new StringBuilder(input);
        int ndx;

        for (String x : SPECIAL_CHARS) {
            while ((ndx = builder.indexOf(x)) > -1) {
                builder.replace(ndx, ndx + 1, replacment);
            }
        }
        
        return builder.toString();
        
    }
    
    /**
     * 
     * @param input
     * @return 
     */
    public static String replaceSpecialChars(String input) {                        
        return replaceSpecialChars(input, REPLACEMENT_CHAR);
        
    }
    
    /**
     * 
     * @param folder 
     */
    public static void rmdir(final File folder) {
        // check if folder file is a real folder
        if (folder.exists() && folder.isDirectory()) {

            File[] list = folder.listFiles();
            if (list != null) {
                for (int i = 0; i < list.length; i++) {
                    File tmpF = list[i];
                    if (tmpF.isDirectory()) {
                        rmdir(tmpF);
                    }

                    if (!tmpF.delete()) {
                        tmpF.deleteOnExit();
                        //       System.out.println("can't delete file : " + tmpF);
                    }
                }
            }

            if (!folder.delete()) {
                  //   System.out.println("can't delete folder : " + folder);
                folder.deleteOnExit();
            }
        }
    }

    public static void deleteSegFiles(List<String> segmentToDel) {

        if (!segmentToDel.isEmpty()) {            
            segmentToDel.forEach(file -> {

                File toDelete = new File(file);
                try {
                    Files.delete(toDelete.toPath());
                } catch (IOException e) {
                    toDelete.deleteOnExit();
                    LOGGER.log(Level.WARNING,
                            "Unable to delete segment file {0}. Will try to delete on program termination ",
                            toDelete.getAbsolutePath());
                }

            });

        }

    }
}
