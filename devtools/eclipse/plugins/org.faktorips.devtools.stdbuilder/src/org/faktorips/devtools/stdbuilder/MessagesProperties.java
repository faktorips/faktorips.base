/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.faktorips.devtools.model.builder.plugin.IpsBuilderPlugin;
import org.faktorips.devtools.model.plugin.IpsLog;
import org.faktorips.util.IoUtil;

public class MessagesProperties {

    private final SortedProperties properties;

    private boolean modified = false;

    /**
     * Default constructor creating a new {@link Properties} object.
     */
    public MessagesProperties(String lineSeparator) {
        properties = new SortedProperties(lineSeparator);
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
            IpsLog.get().log(new Status(IStatus.ERROR, IpsBuilderPlugin.PLUGIN_ID,
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
            IpsLog.get().log(new Status(IStatus.ERROR, IpsBuilderPlugin.PLUGIN_ID,
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
        HashSet<String> result = new HashSet<>();
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

        /** A table of hex digits */
        private static final char[] HEX_DIGITS = {
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
        };

        private final String lineSeparator;

        public SortedProperties(String lineSeparator) {
            this.lineSeparator = lineSeparator;
        }

        @Override
        public synchronized Enumeration<Object> keys() {
            return Collections.enumeration(new TreeSet<>(super.keySet()));
        }

        @Override
        public Set<java.util.Map.Entry<Object, Object>> entrySet() {
            @SuppressWarnings({ "rawtypes" })
            TreeSet<java.util.Map.Entry<Object, Object>> treeSet = new TreeSet<>(
                    Comparator.comparing(e -> (Comparable)e.getKey()));
            treeSet.addAll(super.entrySet());
            return treeSet;
        }

        @Override
        public void store(final OutputStream out, final String comments) throws IOException {
            store0(new BufferedWriter(new OutputStreamWriter(out, "8859_1")));
        }

        /*
         * Copied from Properties#store0(BufferedWriter, boolean, boolean), removed comment and date
         */
        private void store0(BufferedWriter bw)
                throws IOException {
            synchronized (this) {
                for (Map.Entry<Object, Object> e : entrySet()) {
                    String key = (String)e.getKey();
                    String val = (String)e.getValue();
                    key = saveConvert(key, true);
                    /*
                     * No need to escape embedded and trailing spaces for value, hence pass false to
                     * flag.
                     */
                    val = saveConvert(val, false);
                    bw.write(key + "=" + val);
                    bw.write(lineSeparator);
                }
            }
            bw.flush();
        }

        /*
         * Converts Unicode characters to encoded &#92;uxxxx and escapes special characters with a
         * preceding slash.
         *
         * Copied from Properties#saveConvert(String, boolean, boolean)
         */
        // CSOFF: CyclomaticComplexity
        private String saveConvert(String theString,
                boolean escapeSpace) {
            int len = theString.length();
            int bufLen = len * 2;
            if (bufLen < 0) {
                bufLen = Integer.MAX_VALUE;
            }
            StringBuilder outBuffer = new StringBuilder(bufLen);

            for (int x = 0; x < len; x++) {
                char aChar = theString.charAt(x);
                // Handle common case first, selecting largest block that
                // avoids the specials below
                if ((aChar > 61) && (aChar < 127)) {
                    if (aChar == '\\') {
                        outBuffer.append('\\');
                        outBuffer.append('\\');
                        continue;
                    }
                    outBuffer.append(aChar);
                    continue;
                }
                switch (aChar) {
                    case ' ':
                        if (x == 0 || escapeSpace) {
                            outBuffer.append('\\');
                        }
                        outBuffer.append(' ');
                        break;
                    case '\t':
                        outBuffer.append('\\');
                        outBuffer.append('t');
                        break;
                    case '\n':
                        outBuffer.append('\\');
                        outBuffer.append('n');
                        break;
                    case '\r':
                        outBuffer.append('\\');
                        outBuffer.append('r');
                        break;
                    case '\f':
                        outBuffer.append('\\');
                        outBuffer.append('f');
                        break;
                    case '=':
                        // Fall through
                    case ':':
                        // Fall through
                    case '#':
                        // Fall through
                    case '!':
                        outBuffer.append('\\');
                        outBuffer.append(aChar);
                        break;
                    default:
                        if (((aChar < 0x0020) || (aChar > 0x007e))) {
                            outBuffer.append('\\');
                            outBuffer.append('u');
                            outBuffer.append(toHex((aChar >> 12) & 0xF));
                            outBuffer.append(toHex((aChar >> 8) & 0xF));
                            outBuffer.append(toHex((aChar >> 4) & 0xF));
                            outBuffer.append(toHex(aChar & 0xF));
                        } else {
                            outBuffer.append(aChar);
                        }
                }
            }
            return outBuffer.toString();
        }
        // CSON: CyclomaticComplexity

        /**
         * Convert a nibble to a hex character.
         * <p>
         * Copied from Properties#toHex(int)
         *
         * @param nibble the nibble to convert.
         */
        private static char toHex(int nibble) {
            return HEX_DIGITS[(nibble & 0xF)];
        }

        /**
         * @deprecated not supported; use {@link #store(OutputStream, String)} instead
         */
        @Deprecated
        @Override
        public void store(Writer writer, String comments) throws IOException {
            throw new UnsupportedOperationException();
        }

    }
}
