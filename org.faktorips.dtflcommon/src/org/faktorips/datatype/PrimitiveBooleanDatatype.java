/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.datatype;

/**
 * Datatype for the primitive <code>boolean</code>.
 */
public class PrimitiveBooleanDatatype extends AbstractPrimitiveDatatype {

    /** 
     * {@inheritDoc}
     */
    public String getName() {
        return "boolean";
    }
    
    /**
     * {@inheritDoc}
     */
    public String getQualifiedName() {
        return "boolean";
    }
    
    /**
     * {@inheritDoc}
     */
    public String getDefaultValue() {
        return Boolean.FALSE.toString();
    }

    /**
     * {@inheritDoc}
     */
    public Datatype getWrapperType() {
        return Datatype.BOOLEAN;
    }
	
    /** 
     * {@inheritDoc}
     */
    public String getJavaClassName() {
        return "boolean";
    }

    /** 
     * {@inheritDoc}
     */
    public Object getValue(String value) {
        return Boolean.valueOf(value);
    }

    /**
     * {@inheritDoc}
     */
    public boolean supportsCompare() {
        return false;
    }

}
