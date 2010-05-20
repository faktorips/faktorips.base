/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.util.localization;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

/**
 * Simple class to support developers in internationalisation-process.
 * 
 * All files named "messages.properties" in the source dir (recursively scanned) are compared to all
 * files named "message_ll_RR.properties" (ll means language, e.g. de for german; RR means region,
 * eg. AT for austria; the String ll_RR is given as parameter). If the target file does not exist,
 * it is created with the same content as the source file. If the target exists, the properties
 * contained in the source, but not in the target, are copied to the target and marked with the text
 * >TRANSLATE_ME< as value-Prefix.
 * <p>
 * Note: No translation is done by this class!
 * 
 * @author Thorsten Guenther
 */
@SuppressWarnings("unchecked")
public class LocalizeHelper {

    File sourceRoot;
    File targetRoot;
    String targetLang;
    List modifiedFiles = new ArrayList();

    /**
     * Class which supports properties with predictable iteration order.
     */
    private class SortedProperties extends Properties {

        private static final long serialVersionUID = 1L;

        LinkedHashMap content = new LinkedHashMap();

        @Override
        public String getProperty(String arg0) {
            return (String)content.get(arg0);
        }

        @Override
        public synchronized Object setProperty(String arg0, String arg1) {
            return content.put(arg0, arg1);
        }

        @Override
        public synchronized void load(InputStream is) throws IOException {
            BufferedReader br = new BufferedReader(new InputStreamReader(is, "8859_1"));
            String line = null;
            while ((line = br.readLine()) != null) {
                if (line.indexOf("=") > 0) {
                    String[] props = StringUtils.split(line, "=", 2);
                    if (props.length == 2) {
                        content.put(props[0], props[1]);
                    }
                }
            }
        }

        @Override
        public synchronized void store(OutputStream os, String comments) throws IOException {
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os, "8859_1"));
            if (comments != null) {
                writeln(bw, "#" + comments);
            }
            writeln(bw, "#" + new Date().toString());
            for (Iterator iter = content.keySet().iterator(); iter.hasNext();) {
                String key = (String)iter.next();
                bw.write(key);
                bw.write("=");
                bw.write((String)content.get(key));
                bw.newLine();
            }
            bw.flush();
        }

        private void writeln(BufferedWriter bw, String s) throws IOException {
            bw.write(s);
            bw.newLine();
        }

        @Override
        public Set keySet() {
            return content.keySet();
        }

        @Override
        public synchronized void clear() {
            content.clear();
        }

        @Override
        public synchronized boolean contains(Object arg0) {
            return content.containsValue(arg0);
        }

        @Override
        public synchronized boolean containsKey(Object arg0) {
            return content.containsKey(arg0);
        }

        @Override
        public boolean containsValue(Object arg0) {
            return content.containsValue(arg0);
        }

        @Override
        public Set entrySet() {
            return content.entrySet();
        }

        @Override
        public synchronized boolean equals(Object arg0) {
            return content.equals(arg0);
        }

        @Override
        public synchronized Object get(Object arg0) {
            return content.get(arg0);
        }

        @Override
        public synchronized int hashCode() {
            return content.hashCode();
        }

        @Override
        public synchronized boolean isEmpty() {
            return content.isEmpty();
        }

        @Override
        public synchronized Object put(Object arg0, Object arg1) {
            return content.put(arg0, arg1);
        }

        @Override
        public synchronized void putAll(Map arg0) {
            content.putAll(arg0);
        }

        @Override
        public synchronized Object remove(Object arg0) {
            return content.remove(arg0);
        }

        @Override
        public synchronized int size() {
            return content.size();
        }

        @Override
        public synchronized String toString() {
            return content.toString();
        }

        @Override
        public Collection values() {
            return content.values();
        }

        @Override
        public synchronized Object clone() {
            throw new UnsupportedOperationException();
        }

        @Override
        protected void rehash() {
            throw new UnsupportedOperationException();
        }

        @Override
        public synchronized Enumeration keys() {
            throw new UnsupportedOperationException();
        }

        @Override
        public synchronized Enumeration elements() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getProperty(String arg0, String arg1) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void list(PrintStream arg0) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void list(PrintWriter arg0) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Enumeration propertyNames() {
            throw new UnsupportedOperationException();
        }

        @Override
        public synchronized void save(OutputStream arg0, String arg1) {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * First argument:<br>
     * Path to the base directory, where the source-language files are stored.<br>
     * Second argument:<br>
     * Path to the base directory, where the target-language files are stored.<br>
     * Third argument:<br>
     * The target language (and, optional, the region) as de_RR (e.g. de_AT for german language with
     * austrian region or only de for german with no special region information.
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
     * Creates a new LocalizeHelper working on the given directories and with the given language.
     */
    public LocalizeHelper(String sourceName, String targetName, String targetLang) {
        sourceRoot = new File(sourceName);
        targetRoot = new File(targetName);
        this.targetLang = "_" + targetLang;
    }

    /**
     * Scan source- and target-dir recursively for files called messages.properites (or, for the
     * target-dir, files with language- and/or region-code appended). Differences are fixed.
     */
    private void run() {
        System.out.println("<source-dir> " + sourceRoot);
        System.out.println("<target-dir> " + targetRoot);
        System.out.println("<target-lang> " + targetLang);
        Hashtable sourceProperties = new Hashtable();
        Hashtable targetProperties = new Hashtable();
        findProperties(sourceRoot, sourceProperties, sourceRoot.getAbsolutePath().length(), "");
        findProperties(targetRoot, targetProperties, targetRoot.getAbsolutePath().length(), targetLang);

        try {
            sync(sourceProperties, targetProperties);
            if (modifiedFiles.size() > 0) {
                System.out.println("Modified files:");
                for (Iterator iter = modifiedFiles.iterator(); iter.hasNext();) {
                    System.out.print("  ");
                    System.out.println(iter.next());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * scan the given directory recursively for files named messages[_langPostfix].properties. All
     * files found are put into the map <code>propertyFiles</code>, using the slightly modified name
     * of the file as key. The name is prepared for later compare by cutting of the language postfix
     * and file extension. The first part of the filename of the given length is cut off the name,
     * too.
     * 
     * @param dir The directory to scan. If not a directory, this method returns silently.
     * @param propertyFiles The map to store found files.
     * @param ignorePathPrefixLength The length of the prefix to be cut off the filename. This is
     *            used to cut off the different base-pathnames of source- and target-files.
     * @param langPostfix The language postfix used. Can be the empty string, but not
     *            <code>null</code>.
     */
    private void findProperties(File dir, Map propertyFiles, int ignorePathPrefixLength, String langPostfix) {
        if (!dir.isDirectory()) {
            return;
        }

        File[] files = dir.listFiles();
        for (File file : files) {
            if (file.getName().equalsIgnoreCase("messages" + langPostfix + ".properties")) {
                String name = file.getAbsolutePath().substring(ignorePathPrefixLength);

                name = name.substring(0, name.lastIndexOf(langPostfix + ".properties"));
                propertyFiles.put(name, file);
            } else if (file.isDirectory()) {
                findProperties(file, propertyFiles, ignorePathPrefixLength, langPostfix);
            }
        }
    }

    /**
     * Synchronizes all properties found in the target map with the properties found in the source
     * map.
     * <p>
     * This means to copy the entire file, if not contained in the target map or to insert all keys
     * missing in the target file.
     * <p>
     * Note: At the moment, no remove of keys contained in target but not in source is supported.
     * 
     * @param source Map of all found sources. Keys have to be the name of the files without
     *            language postfix, file-extension and base-path.
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
                modifiedFiles.add(targetFile);
                FileWriter writer = new FileWriter(targetFile);
                FileReader reader = new FileReader(sourceFile);
                for (int c = reader.read(); c != -1; c = reader.read()) {
                    writer.write(c);
                }
                reader.close();
                writer.close();
            } else {
                SortedProperties sourceProps = new SortedProperties();
                File srcFile = (File)source.get(name);
                sourceProps.load(new FileInputStream(srcFile));

                SortedProperties targetProps = new SortedProperties();
                targetProps.load(new FileInputStream(targetFile));

                Set srcKeys = sourceProps.keySet();
                boolean modified = false;

                for (Iterator iter = srcKeys.iterator(); iter.hasNext();) {
                    String key = (String)iter.next();
                    if (targetProps.getProperty(key) == null) {
                        targetProps.setProperty(key, ">TRANSLATE_ME<" + sourceProps.getProperty(key));
                        modified = true;
                    }
                }

                // remove keys in target that don't exist in the source file
                Set targetKeys = targetProps.keySet();
                for (Iterator it = targetKeys.iterator(); it.hasNext();) {
                    String key = (String)it.next();
                    if (sourceProps.get(key) == null) {
                        it.remove();
                        modified = true;
                    }
                }

                if (modified) {
                    modifiedFiles.add(targetFile);
                    // store the target in same order as the source
                    SortedProperties newTargetProps = new SortedProperties();
                    Set srcKeySet = sourceProps.keySet();
                    for (Iterator iter = srcKeySet.iterator(); iter.hasNext();) {
                        String key = (String)iter.next();
                        String property = targetProps.getProperty(key);
                        newTargetProps.setProperty(key, property);
                    }
                    FileOutputStream out = new FileOutputStream(targetFile);
                    newTargetProps.store(out, "File modified by LocalizationHelper");
                    out.close();
                }
            }
        }
    }

    /**
     * Creates a new file and, if necessary, the parent directories, too. If the file already
     * exists, this method returns silently.
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
