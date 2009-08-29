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

package org.faktorips.datatype;

import org.faktorips.util.message.MessageList;

/**
 * A datatype that is used to represent any type of data, similiar to <code>java.lang.Object.</code>
 * 
 * @author Jan Ortmann
 */
public class AnyDatatype implements Datatype {

    public final static AnyDatatype INSTANCE = new AnyDatatype();

    private AnyDatatype() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    public MessageList validate() {
        return new MessageList();
    }

    public String getName() {
        return "any"; //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    public String getQualifiedName() {
        return "any"; //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    public boolean isVoid() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isPrimitive() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isAbstract() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isValueDatatype() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isEnum() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public String getJavaClassName() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public int compareTo(Object o) {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasNullObject() {
        return false;
    }

}
