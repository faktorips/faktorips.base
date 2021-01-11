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

import java.io.Closeable;
import java.io.IOException;

import org.faktorips.annotation.UtilityClass;

@UtilityClass
public class IoUtil {

    private IoUtil() {
        // utility class
    }

    /**
     * Tries to close the given {@link Closeable}.
     * 
     * @param closeable The {@link Closeable} to close or null
     * 
     * @throws RuntimeException If the parameter is not null and an {@link IOException} occurred
     *             while closing the resource
     */
    public static void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                throw new RuntimeException("Failed to close resource.", e);
            }
        }
    }

}
