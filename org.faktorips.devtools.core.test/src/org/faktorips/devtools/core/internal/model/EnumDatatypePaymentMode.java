/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model;

import org.faktorips.datatype.AbstractDatatype;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.datatype.ValueDatatype;

public class EnumDatatypePaymentMode extends AbstractDatatype implements EnumDatatype {

    public final static String ANNUAL = "annual";
    public final static String MONTHLY = "monthly";
    
	/**
	 * {@inheritDoc}
	 */
	public String[] getAllValueIds(boolean includeNull) {
		return new String[]{ANNUAL, MONTHLY};
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
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getName() {
		return null;
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
	public boolean isValueDatatype() {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getJavaClassName() {
		return null;
	}


	public boolean isSupportingNames() {
		return false;
	}


	public String getValueName(String id) {
		throw new RuntimeException("Not supported");
	}


    public boolean isNull(String value) {
        return false;
    }


    public boolean supportsCompare() {
        return false;
    }


    public int compare(String valueA, String valueB) throws UnsupportedOperationException {
        return 0;
    }


    public boolean areValuesEqual(String valueA, String valueB) {
        return false;
    }

}