/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.faktorips.annotation.UtilityClass;

/**
 * A utility class for streams.
 */
@UtilityClass
public class StreamUtil {

    private StreamUtil() {
        // Utility class not to be instantiated.
    }

    /**
     * Copies the content of the provided InputStream into a ByteArrayInputStream and closes the
     * provided stream.
     * 
     * @param is the InputStream to be copied
     * @return the new ByteArrayInputStream with the copied content
     * @throws IOException exceptions that occur during reading from and writing to a stream or
     *             closing the provided stream are delegated
     */
    public static final ByteArrayInputStream copy(InputStream is) throws IOException {
        ByteArrayOutputStream buffer = toByteArrayOutputStream(is);
        try {
            return new ByteArrayInputStream(buffer == null ? new byte[0] : buffer.toByteArray());
        } finally {
            IoUtil.close(buffer);
        }
    }

    /**
     * Copies the content of the provided InputStream into a ByteArrayOutputStream and closes the
     * provided stream.
     * 
     * @param is the InputStream to be copied
     * @return the new ByteArrayInputStream with the copied content
     * @throws IOException exceptions that occur during reading from and writing to a stream or
     *             closing the provided stream are delegated
     */
    public static final ByteArrayOutputStream toByteArrayOutputStream(InputStream is) throws IOException {
        if (is == null) {
            return null;
        }
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try {
            byte[] data = new byte[1024];
            int nRead;
            while ((nRead = is.read(data)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();
        } finally {
            is.close();
        }
        return buffer;
    }
}
