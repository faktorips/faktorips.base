/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.faktorips.util.IoUtil;

public class MessagesProperties {

    private final SortedProperties properties;

    private boolean modified = false;

    /**
     * Default constructor creating a new {@link Properties} object.
     */
    public MessagesProperties() {
        properties = new SortedProperties();
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
        if (!messageText.equals(properties.getProperty(messageKey))) {
            properties.setProperty(messageKey, messageText);
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
                    "Error occured while reading file", e));
        } finally {
            IoUtil.close(stream);
        }
    }

    public void store(OutputStream outputStream) {
        try {
            properties.store(outputStream, null);
            setModified(false);
        } catch (IOException e) {
            StdBuilderPlugin.log(new Status(IStatus.ERROR, StdBuilderPlugin.PLUGIN_ID,
                    "Error occured while saving message file", e));
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

    public Set<String> keySet() {
        HashSet<String> result = new HashSet<String>();
        Set<Object> keySet = properties.keySet();
        for (Object object : keySet) {
            result.add(object.toString());
        }
        return result;
    }

    @Override
    public String toString() {
        return properties.toString();
    }

    private static class SortedProperties extends Properties {

        private static final long serialVersionUID = 7627392983212145038L;

        @Override
        public synchronized Enumeration<Object> keys() {
            return Collections.enumeration(new TreeSet<Object>(super.keySet()));
        }

        @Override
        public void store(final OutputStream out, final String comments) throws IOException {
            // The comment is ignored as the benefit from the comment is questionable. Additionally,
            // the first line being the time stamp makes removing the time stamp a lot easier.
            super.store(new StripFirstLineStream(out), null);
        }

        /**
         * @deprecated not supported; use {@link #store(OutputStream, String)} instead
         */
        @Deprecated
        @Override
        public void store(Writer writer, String comments) throws IOException {
            throw new UnsupportedOperationException();
        }

        private static class StripFirstLineStream extends FilterOutputStream {

            private static final String LINE_SEP = System.getProperty("line.separator");
            private static final char LAST_CHAR_LINE_SEP = LINE_SEP.charAt(LINE_SEP.length() - 1);

            private boolean firstlineSeen = false;

            public StripFirstLineStream(final OutputStream out) {
                super(out);
            }

            @Override
            public void write(final int b) throws IOException {
                if (firstlineSeen) {
                    super.write(b);
                } else if (b == LAST_CHAR_LINE_SEP) {
                    firstlineSeen = true;
                }
            }

        }

    }
}
