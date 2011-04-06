/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
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
        this.dataModifiedException = e;
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
