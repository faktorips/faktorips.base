/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.ipsproject;

import java.util.NoSuchElementException;

/**
 * Defines which format is used to save table contents.
 *
 * @since 20.12
 */
public enum TableContentFormat {

    XML("XML"), //$NON-NLS-1$

    CSV("CSV"); //$NON-NLS-1$

    private final String id;

    TableContentFormat(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public static TableContentFormat valueById(String id) {
        for (TableContentFormat format : values()) {
            if (format.id.equals(id)) {
                return format;
            }
        }
        throw new NoSuchElementException("No TableContentFormat found for ID " + id); //$NON-NLS-1$
    }

}
