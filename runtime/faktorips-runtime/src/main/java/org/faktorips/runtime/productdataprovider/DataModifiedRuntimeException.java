/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.productdataprovider;

/**
 * Exception thrown if the requested data has been modified since last correct modification check.
 * This exception encapsulates a {@link DataModifiedException} in a runtime exception.
 * 
 * @author dirmeier
 */
public class DataModifiedRuntimeException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    private final DataModifiedException dataModifiedException;

    public DataModifiedRuntimeException(DataModifiedException e) {
        super(e);
        dataModifiedException = e;
    }

    /**
     * Constructs a new {@link DataModifiedRuntimeException} with a nested
     * {@link DataModifiedException}
     */
    public DataModifiedRuntimeException(String message, String oldVersion, String newVersion) {
        this(new DataModifiedException(message, oldVersion, newVersion));
    }

    /**
     * Getting the nested {@link DataModifiedException}
     */
    public DataModifiedException getDataModifiedException() {
        return dataModifiedException;
    }

}
