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

package org.faktorips.datatype.classtypes;

import org.faktorips.datatype.ValueClassDatatype;

/**
 * Datatype for Strings.
 * 
 * @author Jan Ortmann
 */
public class StringDatatype extends ValueClassDatatype {

    public StringDatatype() {
        super(String.class);
    }

    public StringDatatype(String name) {
        super(String.class, name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isParsable(String value) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getValue(String value) {
        return value;
    }

    /**
     * {@inheritDoc}
     */
    public boolean supportsCompare() {
        return true;
    }
}
