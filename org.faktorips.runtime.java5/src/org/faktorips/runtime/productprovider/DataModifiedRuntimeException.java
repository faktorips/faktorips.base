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

package org.faktorips.runtime.productprovider;

/**
 * Exception thrown if the requested data has been modified since last correct modification check.
 * This exception encapsulates a {@link DataModifiedException} in a runtime exception.
 * 
 * @author dirmeier
 */
public class DataModifiedRuntimeException extends RuntimeException {

    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 1L;
    private final DataModifiedException dataModifiedException;

    public DataModifiedRuntimeException(DataModifiedException e) {
        super(e);
        this.dataModifiedException = e;
    }

    public DataModifiedException getDataModifiedRuntimeException() {
        return dataModifiedException;
    }

}
