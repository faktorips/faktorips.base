/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.model.type;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.model.ipsobject.IDescribedElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.ILabeledElement;
import org.faktorips.devtools.core.model.ipsobject.Modifier;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.valueset.IValueSet;

/**
 * A type's attribute.
 */
public interface IAttribute extends IIpsObjectPart, IDescribedElement, ILabeledElement {

    public final static String PROPERTY_DATATYPE = "datatype"; //$NON-NLS-1$

    public final static String PROPERTY_MODIFIER = "modifier"; //$NON-NLS-1$

    public final static String PROPERTY_DEFAULT_VALUE = "defaultValue"; //$NON-NLS-1$

    /**
     * Prefix for all message codes of this class.
     */
    public final static String MSGCODE_PREFIX = "ATTRIBUTE-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the name of the attribute is not a valid java field
     * identifier.
     */
    public final static String MSGCODE_INVALID_ATTRIBUTE_NAME = MSGCODE_PREFIX + "InvalidAttributeName"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the data type of this attribute is not set.
     */
    public final static String MSGCODE_DEFAULT_NOT_PARSABLE_UNKNOWN_DATATYPE = MSGCODE_PREFIX
            + "DefaultNotParsableUnknownDatatype"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the data type of this attribute is not a valid data
     * type.
     */
    public final static String MSGCODE_DEFAULT_NOT_PARSABLE_INVALID_DATATYPE = MSGCODE_PREFIX
            + "ValueNotParsableInvalidDatatype"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the default value of this attribute can not be
     * parsed by the data type of this attribute.
     */
    public final static String MSGCODE_VALUE_NOT_PARSABLE = MSGCODE_PREFIX + "ValueTypeMissmatch"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the data type provided for a parameter is not valid.
     */
    public final static String MSGCODE_DATATYPE_NOT_FOUND = MSGCODE_PREFIX + "DatatypeNotFound"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the default value of this attribute is not contained
     * in the value set of this attribute.
     */
    public final static String MSGCODE_DEFAULT_NOT_IN_VALUESET = MSGCODE_PREFIX + "DefaultNotInValueSet"; //$NON-NLS-1$

    /**
     * Returns the type this attribute belongs to. This method never returns <code>null</code>.
     */
    public IType getType();

    /**
     * Sets the attribute's name.
     */
    public void setName(String newName);

    /**
     * Returns the attribute's data type. Note that only value data types are allowed as attribute
     * data type.
     */
    public String getDatatype();

    /**
     * Sets the attribute's data type. Note that only value data types are allowed as attribute data
     * type.
     */
    public void setDatatype(String newDatatype);

    /**
     * Returns the attribute's value data type. If this attribute is linked to a policy component
     * type attribute, the policy component type's value data type is returned. If the attribute is
     * not linked, the attribute's *own* value data type is returned.
     * 
     * @param project The project which IPS object path is used for the search. This is not
     *            necessarily the project this type is part of.
     * 
     * @see #getDatatype()
     */
    public ValueDatatype findDatatype(IIpsProject project) throws CoreException;

    /**
     * Returns the attribute's modifier.
     */
    public Modifier getModifier();

    /**
     * Sets the attribute's modifier.
     */
    public void setModifier(Modifier newModifier);

    /**
     * Returns the attribute's default value.
     */
    public String getDefaultValue();

    /**
     * Sets the attribute's default value.
     */
    public void setDefaultValue(String newValue);

    /**
     * Returns <code>true</code> if this attribute is a derived one, otherwise <code>false</code>.
     */
    public boolean isDerived();

    /**
     * Returns <code>true</code> if this attribute is marked to overwrite an attribute with the same
     * name somewhere up the supertype hierarchy, <code>false</code> otherwise.
     */
    public boolean isOverwrite();

    public IValueSet getValueSet() throws CoreException;

}
