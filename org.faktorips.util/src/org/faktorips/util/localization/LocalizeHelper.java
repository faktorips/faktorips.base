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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;

/**
 * Simple class to support developers in internationalisation-process.
 * 
 * All files named "messages.properties" in the source dir (recursively scanned)
 * are compared to all files named "message_ll_RR.properties" (ll means language,
 * e.g. de for german; RR means region, eg. AT for austria; the String ll_RR is given
 * as parameter). If the target file does not exist, it is created with the same content
 * as the source file. If the target exists, the properties contained in the source, 
 * but not in the target, are copied to the target and marked with the text #TRANSLATE_ME#
 * as value-Prefix.
 * <p>
 * Note: No translation is done by this class!
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
     */
    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println(LocalizeHelper.class.getName() + " <source-dir> <target-dir> <target-language>");
            return;
        }

        LocalizeHelper helper = new LocalizeHelper(args[0], args[1], args[2]);
        helper.run();
    }

    /**
     * Creates a new LocalizeHelper woring on the given directories and with the
     * given language.
     * 
     * @param sourceName
     * @param targetName
     * @param targetLang
     */
    public LocalizeHelper(String sourceName, String targetName, String targetLang) {
        sourceRoot = new File(sourceName);
        targetRoot = new File(targetName);
        this.targetLang = "_" + targetLang;
    }

    /**
     * Scan source- and target-dir recursively for files called messages.properites (or, for
     * the target-dir, files with language- and/or region-code appended). Differences are fixed.
     */
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

    /**
     * scan the given directory recursivley for files named messages[_langPostfix].properties. All files
     * found are put into the map <code>propertyFiles</code>, using the slightly modiefied name of the 
     * file as key. The name is prepared for later compere by cutting of the language postfix and file 
     * extension. The first part of the filename of the given length is cut off the name, too. 
     *  
     * @param dir The direcotry to scan. If not a directory, this method returns silently.
     * @param propertyFiles The map to store found files.
     * @param ignorePathPrefixLength The leght of the prefix to be cut off the filename. This is used
     * to cut off the different base-pathnames of source- and target-files.
     * @param langPostfix The language postfix used. Can be the empty string, but not <code>null</code>.
     */
    private void findProperties(File dir, Map propertyFiles, int ignorePathPrefixLength, String langPostfix) {
        if (!dir.isDirectory()) {
            return;
        }

        File[] files = dir.listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].getName().equals("messages" + langPostfix + ".properties")) {
                String name = files[i].getAbsolutePath().substring(ignorePathPrefixLength);

                name = name.substring(0, name.lastIndexOf(langPostfix + ".properties"));
                propertyFiles.put(name, files[i]);
            } else if (files[i].isDirectory()) {
                findProperties(files[i], propertyFiles, ignorePathPrefixLength, langPostfix);
            }
        }
    }

    /**
     * Syncronizes all properties found in the target map with the properties found
     * in the source map.
     * <p>
     * This means to copy the entire file, if not contained in the target map or to 
     * insert all keys missing in the target file.
     * <p>
     * Note: At the moment, no remove of keys contained in target but not in source 
     * is supported.
     *  
     * @param source Map of all found sources. Keys have to be the name of the files
     * without language postfix, file-extension and base-path.
     * @param target Map of all found targets, for keys see sources.
     * @throws IOException If an error occurs during handling the properties files.
     */
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
            } else {
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
                        targetProps.setProperty(key, "#TRANSLATE_ME#" + sourceProps.getProperty(key));
                        modified = true;
                    }
                }

                if (modified) {
                    FileOutputStream out = new FileOutputStream(targetFile);
                    targetProps.store(out, "File modified by LocalizationHelper");
                    out.close();
                }
            }
        }
    }

    /**
     * Creates a new file and, if neccessary, the parent directories, too. If the file
     * allready exists, this method returns silently.
     * 
     * @param file The file to create
     * @throws IOException if an error during file creation occurs.
     */
    private void createFile(File file) throws IOException {
        if (file.exists()) {
            return;
        }

        file.getParentFile().mkdirs();
        file.createNewFile();
    }
}
