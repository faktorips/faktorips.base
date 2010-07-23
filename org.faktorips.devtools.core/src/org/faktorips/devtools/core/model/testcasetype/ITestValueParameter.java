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

package org.faktorips.devtools.core.model.testcasetype;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;

/**
 * Specification of a test value parameter.
 * 
 * @author Joerg Ortmann
 */
public interface ITestValueParameter extends ITestParameter {

    public final static String PROPERTY_VALUEDATATYPE = "valueDatatype"; //$NON-NLS-1$

    /**
     * Prefix for all message codes of this class.
     */
    public final static String MSGCODE_PREFIX = "TESTVALUEPARAMETER-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the value data type not exists.
     */
    public final static String MSGCODE_VALUEDATATYPE_NOT_FOUND = MSGCODE_PREFIX + "ValueDatatypeNotFound"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that there is an unsupported type.
     */
    public final static String MSGCODE_WRONG_TYPE = MSGCODE_PREFIX + "WrongType"; //$NON-NLS-1$

    /**
     * Returns the data type.
     */
    public String getValueDatatype();

    /**
     * Sets the data type.
     */
    public void setValueDatatype(String datatype);

    /**
     * Returns the data type or <code>null</code> if the object does not exists.
     * 
     * @param ipsProject The IPS project which object path is used to search.
     * 
     * @throws CoreException If an error occurs while searching for the data type.
     */
    public ValueDatatype findValueDatatype(IIpsProject ipsProject) throws CoreException;

}
