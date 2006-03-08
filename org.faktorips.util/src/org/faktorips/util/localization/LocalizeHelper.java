/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.util.localization;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;

/**
 * Simple class to support developers in internationalisation-process.
 * 
 * All files named "messages.properties" in the source dir (recursively scanned)
 * are compared to all files named "message_ll_RR.properties" (ll means language,
 * e.g. de for german; RR means region, eg. AT for austria; the String ll_RR is given
 * as parameter). Does the target file not exist, it is created with the same content
 * as the source file. If the target exists, the properties contained in the source, 
 * but not in the target are copied.
 * 
 * @author Thorsten Guenther
 */
public class LocalizeHelper {
    File sourceRoot;
    File targetRoot;
    String targetLang;

    /**
     * First argument:<br>
     * Path to the base directory, where the source-language files are stored.<br>
     * Second argument:<br>
     * Path to the base directory, where the target-language files are stored.<br>
     * Third argument:<br>
     * The target language (and, optional, the region) as de_RR (e.g. de_AT for 
     * german language with austrian region or only de for german with no special 
     * region information.  
     * 
     * 
     * 
     * @param args
     */
    public static void main(String[] args) {
        LocalizeHelper helper = new LocalizeHelper(args[0], args[1], args[2]);
        helper.run();      
    }

    /**
     * 
     */
    public LocalizeHelper(String sourceName, String targetName, String targetLang) {
        sourceRoot = new File(sourceName);
        targetRoot = new File(targetName);
        this.targetLang = "_" + targetLang;
    }

    private void run() {
        Hashtable sourceProperties = new Hashtable();
        Hashtable targetProperties = new Hashtable();
        findProperties(sourceRoot, sourceProperties, sourceRoot.getAbsolutePath().length(), "");
        findProperties(targetRoot, targetProperties, targetRoot.getAbsolutePath().length(), targetLang);
        
        try {
            sync(sourceProperties, targetProperties);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void findProperties(File dir, Hashtable propertyFiles, int ignorePathPrefixLength, String langPostfix) {
        if (!dir.isDirectory()) {
            return;
        }
        
        File[] files = dir.listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].getName().equals("messages" + langPostfix + ".properties")) {
                String name = files[i].getAbsolutePath().substring(ignorePathPrefixLength);
                
                name = name.substring(0, name.lastIndexOf(langPostfix + ".properties"));
                propertyFiles.put(name, files[i]);
            }
            else if (files[i].isDirectory()) {
                findProperties(files[i], propertyFiles, ignorePathPrefixLength, langPostfix);
            }
        }
    }

    private void sync(Hashtable source, Hashtable target) throws IOException {
        Enumeration sourceNames = source.keys();
        
        while (sourceNames.hasMoreElements()) {
            String name = (String)sourceNames.nextElement();
            File targetFile = (File)target.get(name);
            if (targetFile == null) {
                File sourceFile = (File)source.get(name);
                String newName = targetRoot.getAbsolutePath() + name + targetLang + ".properties";
                targetFile = new File(newName);
                createFile(targetFile);
                FileWriter writer = new FileWriter(targetFile);
                FileReader reader = new FileReader(sourceFile);
                for (int c = reader.read(); c != -1; c = reader.read()) {
                    writer.write(c);
                }
                reader.close();
                writer.close();
            }
            else {
                Properties sourceProps = new Properties();
                File srcFile = (File)source.get(name);
                sourceProps.load(new FileInputStream(srcFile));
                
                Properties targetProps = new Properties();
                targetProps.load(new FileInputStream(targetFile));
                
                Enumeration srcKeys = sourceProps.keys();
                boolean modified = false;
                while (srcKeys.hasMoreElements()) {
                    String key = (String)srcKeys.nextElement();
                    if (targetProps.getProperty(key) == null) {
                        targetProps.setProperty(key, sourceProps.getProperty(key));
                        modified = true;
                    }
                }
                
                if (modified) {
                    FileOutputStream out = new FileOutputStream(targetFile);
                    OutputStreamWriter outW = new OutputStreamWriter(out, "UTF-8");
                    targetProps.store(out, "File modified by LocalizationHelper");

                    out.close();
                }
            }
        }
    }
    
    private void createFile(File file) throws IOException {
        if (file.exists()) {
            return;
        }

        file.getParentFile().mkdirs();
        file.createNewFile();
    }
}
