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

package org.faktorips.devtools.core.ui.controller.fields;

import org.faktorips.datatype.AbstractDatatype;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.datatype.ValueDatatype;

public class PaymentMode extends AbstractDatatype implements EnumDatatype {

    public final static String ANNUAL_ID = "1";
    public final static String MONTHLY_ID = "12";

    public final static String ANNUAL_NAME = "annual";
    public final static String MONTHLY_NAME = "monthly";

    /**
     * {@inheritDoc}
     */
    public String[] getAllValueIds(boolean includeNull) {
        if (includeNull) {
            return new String[] { null, ANNUAL_ID, MONTHLY_ID };
        }
        return new String[] { ANNUAL_ID, MONTHLY_ID };
    }

    /**
     * {@inheritDoc}
     */
    public ValueDatatype getWrapperType() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isParsable(String value) {
        if (value == null) {
            return true;
        }
        return value.equals(ANNUAL_ID) || value.equals(MONTHLY_ID);
    }

    /**
     * {@inheritDoc}
     */
    public String getName() {
        return "PaymentMode";
    }

    /**
     * {@inheritDoc}
     */
    public String getDefaultValue() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public String getQualifiedName() {
        return null;
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
        return true;
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
    public boolean isSupportingNames() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public String getValueName(String id) {
        if (id == null) {
            return null;
        }
        if (id.equals(ANNUAL_ID)) {
            return ANNUAL_NAME;
        }
        if (id.equals(MONTHLY_ID)) {
            return MONTHLY_NAME;
        }
        throw new IllegalArgumentException();
    }

    /**
     * {@inheritDoc}
     */
    public boolean isNull(String value) {
        if (value == null) {
            return true;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean supportsCompare() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public int compare(String valueA, String valueB) throws UnsupportedOperationException {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    public boolean areValuesEqual(String valueA, String valueB) {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isImmutable() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isMutable() {
        return false;
    }

}
