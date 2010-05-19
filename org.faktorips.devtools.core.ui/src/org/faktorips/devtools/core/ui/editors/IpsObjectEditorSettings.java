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

package org.faktorips.devtools.core.ui.editors;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.ISaveContext;
import org.eclipse.core.resources.ISaveParticipant;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.util.ArgumentCheck;

/**
 * 
 * @author Jan Ortmann
 */
public class IpsObjectEditorSettings implements IIpsObjectEditorSettings, ISaveParticipant {

    final static String FILE_NAME = "ips-editor-settings.txt"; //$NON-NLS-1$

    private HashMap<String, Map<String, String>> settings = new HashMap<String, Map<String, String>>();

    /**
     * {@inheritDoc}
     */
    @Override
    public void put(IIpsSrcFile file, String key, String value) {
        if (file == null) {
            return;
        }
        ArgumentCheck.notNull(key);
        if (key.indexOf(' ') != -1) {
            throw new IllegalArgumentException("Key must not contain space!"); //$NON-NLS-1$
        }
        if (value == null) {
            remove(file, key);
            return;
        }
        Map<String, String> keyValues = settings.get(getKey(file));
        if (keyValues == null) {
            keyValues = new HashMap<String, String>();
            settings.put(getKey(file), keyValues);
        }
        keyValues.put(key, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String get(IIpsSrcFile file, String key) {
        if (file == null) {
            return null;
        }
        Map<String, String> keyValues = settings.get(getKey(file));
        if (keyValues == null) {
            return null;
        }
        return keyValues.get(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void put(IIpsSrcFile file, String key, boolean value) {
        put(file, key, "" + value); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getBoolean(IIpsSrcFile file, String key) {
        String s = get(file, key);
        if (s == null) {
            return false;
        }
        return Boolean.valueOf(s).booleanValue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void remove(IIpsSrcFile file) {
        if (file == null) {
            return;
        }
        settings.remove(getKey(file));
    }

    private String getKey(IIpsSrcFile file) {
        return file.getQualifiedNameType().toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void remove(IIpsSrcFile file, String key) {
        if (file == null) {
            return;
        }
        Map<String, String> keyValues = settings.get(getKey(file));
        if (keyValues != null) {
            keyValues.remove(key);
        }
    }

    /**
     * Loads the settings from the saved state. Should only be called by
     * {@link IpsPlugin#start(org.osgi.framework.BundleContext)}
     */
    public void load(IPath stateLocation) {
        File file = stateLocation.append(new Path(FILE_NAME)).toFile();
        try {
            load(file);
        } catch (Exception e) {
            // if the settigns are lost, this causes just a small inconveniance for the user, so we
            // just log it
            settings = new HashMap<String, Map<String, String>>();
            IpsPlugin.log(e);
        }
    }

    /**
     * Loads the settings from the given file.
     */
    public void load(File file) throws IOException {
        if (!file.exists()) {
            return;
        }
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String line = reader.readLine();
            while (line != null) {
                line = load(reader, line);
            }
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

    private String load(BufferedReader reader, String line) throws IOException {
        HashMap<String, String> keyValues = new HashMap<String, String>();
        settings.put(line, keyValues);
        line = reader.readLine();
        while (line != null) {
            int index = line.indexOf(' ');
            if (index != 0) {
                return line; // next ips source file (source file lines start with a /, key/value
                // pair lines with a blank
            }
            index = line.indexOf(' ', index + 1);
            String key = line.substring(1, index);
            String value = line.substring(index + 1);
            keyValues.put(key, value);
            line = reader.readLine();
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void saving(ISaveContext context) throws CoreException {
        if (context.getKind() != ISaveContext.FULL_SAVE) {
            return;
        }
        File file = IpsUIPlugin.getDefault().getStateLocation().append(FILE_NAME).toFile();
        try {
            save(file);
        } catch (IOException e) {
            IpsPlugin.log(e);
        }
    }

    public void save(File file) throws IOException {
        PrintWriter out = null;
        try {
            out = new PrintWriter(new FileOutputStream(file));
            for (String ipsSrcFile : settings.keySet()) {
                out.println(ipsSrcFile);
                writeKeyValuePairs(out, settings.get(ipsSrcFile));

            }
        } finally {
            out.close();
        }
    }

    private void writeKeyValuePairs(PrintWriter out, Map<String, String> keyValuePairs) {
        for (String key : keyValuePairs.keySet()) {
            String value = keyValuePairs.get(key);
            out.println(" " + key + " " + value); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doneSaving(ISaveContext context) {
        // nothing to do
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void prepareToSave(ISaveContext context) throws CoreException {
        // nothing to do
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void rollback(ISaveContext context) {
        // nothing to do
    }

}
