/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.stdbuilder.policycmpttype;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.faktorips.devtools.stdbuilder.StdBuilderPlugin;
import org.faktorips.util.IoUtil;

public class ValidationMessages {

    private final Properties properties;

    private boolean modified = false;

    /**
     * Default constructor creating a new {@link Properties} object.
     */
    public ValidationMessages() {
        this(new Properties());
    }

    /**
     * Constructor using an existing {@link Properties} object.
     */
    public ValidationMessages(Properties properties) {
        this.properties = properties;
    }

    /**
     * Manually set the modification state
     * 
     * @param modified the new modification state
     */
    private void setModified(boolean modified) {
        this.modified = modified;
    }

    public boolean isModified() {
        return modified;
    }

    /**
     * Putting a message text for a message key in map of messages and setting the modification
     * state if map has changed.
     * 
     * @param messageKey the key of the message
     * @param messageText the text of the message
     */
    public void put(String messageKey, String messageText) {
        if (!messageText.equals(properties.put(messageKey, messageText))) {
            setModified(true);
        }
    }

    /**
     * Getting the message stored for the given key or null if there is no message for this key
     * 
     * @param key The key of the message you want to get
     * @return the message stored for the key or null if there is none
     */
    public String getMessage(String key) {
        return properties.getProperty(key);
    }

    /**
     * Removing the message with the given key and setting the modification state if the map
     * changed.
     * 
     * @param key the key of the message to be removed.
     */
    public void remove(String key) {
        if (properties.remove(key) != null) {
            setModified(true);
        }
    }

    /**
     * Clear all existing elements and load new properties form stream.
     * 
     * @param stream The {@link InputStream} to load, @see {@link Properties#load(InputStream)}
     */
    public void load(InputStream stream) {
        properties.clear();
        try {
            properties.load(stream);
            setModified(false);
        } catch (IOException e) {
            StdBuilderPlugin.log(new Status(IStatus.ERROR, StdBuilderPlugin.PLUGIN_ID,
                    "Error occured while reading validation messages file", e));
        } finally {
            IoUtil.close(stream);
        }
    }

    public void store(OutputStream outputStream, String comments) {
        try {
            properties.store(outputStream, comments);
            setModified(false);
        } catch (IOException e) {
            StdBuilderPlugin.log(new Status(IStatus.ERROR, StdBuilderPlugin.PLUGIN_ID,
                    "Error occured while saving validation messages file", e));
        } finally {
            IoUtil.close(outputStream);
        }
    }

    public int size() {
        return properties.size();
    }

    public void clear() {
        properties.clear();
    }

}
