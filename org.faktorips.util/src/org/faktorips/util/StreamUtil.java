/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * A utility class for streams.
 * 
 * @author Peter Erzberger
 */
public class StreamUtil {

    /**
     * Copies the content of the provided InputStream into a ByteArrayInputStream and closes the
     * provided stream.
     * 
     * @param is the InputStream that is to copy
     * @param increments the additional size that is added to the capacity of the byte array when
     *            necessary
     * @return the new ByteArrayInputStream with the copied content
     * @throws IOException exceptions that occur during reading from and writing to a stream or
     *             closing the provided stream are delegated
     */
    public final static ByteArrayInputStream copy(InputStream is, int increments) throws IOException {
        if (increments < 1) {
            throw new IllegalArgumentException("The increments must be greater equal to 1.");
        }
        if (is == null) {
            return null;
        }
        try {
            int value = is.read();
            if (value == -1) {
                return new ByteArrayInputStream(new byte[0]);
            }
            byte[] bytes = new byte[increments];
            int counter = 0;
            int absoluteCounter = 0;
            while (value != -1) {
                bytes[absoluteCounter] = (byte)value;
                value = is.read();
                counter++;
                absoluteCounter++;
                if (counter > increments - 1) {
                    byte[] current = bytes;
                    bytes = new byte[bytes.length + increments];
                    System.arraycopy(current, 0, bytes, 0, current.length);
                    counter = 0;
                }
            }
            byte[] current = bytes;
            bytes = new byte[absoluteCounter];
            System.arraycopy(current, 0, bytes, 0, absoluteCounter);
            return new ByteArrayInputStream(bytes);
        } finally {
            is.close();
        }
    }

    private StreamUtil() {
        // Utility class not to be instantiated.
    }

}
