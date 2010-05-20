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

package org.faktorips.devtools.core.model;

/**
 * This interface defines global validation message codes that are used by the validation of
 * different IPS objects / parts.
 * 
 * @author Joerg Ortmann
 */
public interface IValidationMsgCodesForInvalidValues {

    /**
     * Validation message code to indicate that a value can't be validated because the required
     * value data type can't be found.
     */
    public final static String MSGCODE_CANT_CHECK_VALUE_BECAUSE_VALUEDATATYPE_CANT_BE_FOUND = "CantCheckValueBecauseValueDatatypeCantBeFound"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that a value can't be validated because the required
     * value data type is invalid.
     */
    public final static String MSGCODE_CANT_CHECK_VALUE_BECAUSE_VALUEDATATYPE_IS_INVALID = "CantCheckValueBecauseValueDatatypeIsInvalid"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that a value is not an "instance" of the value data type,
     * it should be an instance of.
     */
    public final static String MSGCODE_VALUE_IS_NOT_INSTANCE_OF_VALUEDATATYPE = "ValueIsNotInstanceOfValueDatatype"; //$NON-NLS-1$

}
