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
 * Datatype for <code>Boolean</code>.
 * 
 * @author Jan Ortmann
 */
public class BooleanDatatype extends ValueClassDatatype {

    public BooleanDatatype() {
        super(Boolean.class);
    }

    public BooleanDatatype(String name) {
        super(Boolean.class, name);
    }

    @Override
    public Object getValue(String s) {
        if (s == null) {
            return null;
        }
        if (s.equalsIgnoreCase("false")) { //$NON-NLS-1$
            return Boolean.FALSE;
        }
        if (s.equalsIgnoreCase("true")) { //$NON-NLS-1$
            return Boolean.TRUE;
        }
        throw new IllegalArgumentException("Can't parse " + s + " to Boolean!"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    public boolean supportsCompare() {
        return false;
    }

}
