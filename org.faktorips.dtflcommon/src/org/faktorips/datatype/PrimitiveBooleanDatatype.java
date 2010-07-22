/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

/**
 * Datatype for the primitive <code>boolean</code>.
 */
public class PrimitiveBooleanDatatype extends AbstractPrimitiveDatatype {

    public String getName() {
        return "boolean"; //$NON-NLS-1$
    }

    public String getQualifiedName() {
        return "boolean"; //$NON-NLS-1$
    }

    public String getDefaultValue() {
        return Boolean.FALSE.toString();
    }

    public ValueDatatype getWrapperType() {
        return Datatype.BOOLEAN;
    }

    public String getJavaClassName() {
        return "boolean"; //$NON-NLS-1$
    }

    @Override
    public Object getValue(String value) {
        return Boolean.valueOf(value);
    }

    public boolean supportsCompare() {
        return false;
    }

}
