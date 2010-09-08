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
import org.faktorips.devtools.core.internal.model.testcasetype.TestPolicyCmptTypeParameter;
import org.faktorips.devtools.core.model.ipsobject.IDescribedElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;

/**
 * Specification of a test attribute.
 * 
 * @author Joerg Ortmann
 */
public interface ITestAttribute extends IIpsObjectPart, IDescribedElement {

    public final static String PROPERTY_ATTRIBUTE = "attribute"; //$NON-NLS-1$

    public final static String PROPERTY_DATATYPE = "datatype"; //$NON-NLS-1$

    public final static String PROPERTY_POLICYCMPTTYPE_OF_ATTRIBUTE = "policyCmptType"; //$NON-NLS-1$

    public final static String PROPERTY_TEST_ATTRIBUTE_TYPE = "testAttributeType"; //$NON-NLS-1$

    /**
     * Prefix for all message codes of this class.
     */
    public final static String MSGCODE_PREFIX = "TESTATTRIBUTE-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the attribute which is related by the test attribute
     * not exists.
     */
    public final static String MSGCODE_ATTRIBUTE_NOT_FOUND = MSGCODE_PREFIX + "AttributeNotFound"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that there is an unsupported type.
     */
    public final static String MSGCODE_WRONG_TYPE = MSGCODE_PREFIX + "WrongType"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the type doesn't matches the parent type.<br>
     * E.g. the parent defines the input type and the attribute has the expected result type.
     */
    public final static String MSGCODE_TYPE_DOES_NOT_MATCH_PARENT_TYPE = MSGCODE_PREFIX + "TypeDoesNotMatchParentType"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the attribute name is duplicate.
     */
    public final static String MSGCODE_DUPLICATE_TEST_ATTRIBUTE_NAME = MSGCODE_PREFIX + "DuplicateTestAttributeName"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that derived (computed on the fly) attributes are not
     * supported.
     */
    public final static String MSGCODE_DERIVED_ON_THE_FLY_ATTRIBUTES_NOT_SUPPORTED = MSGCODE_PREFIX
            + "DerivedAttributesOnTheFlyNotSupported"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the attribute and the attribute type is already
     * given.<br>
     * E.g. The attribute "a" could be added as input and expected result but not as input twice.
     */
    public final static String MSGCODE_DUPLICATE_ATTRIBUTE_AND_TYPE = MSGCODE_PREFIX + "DuplicateAttributeAndType"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the attribute name must not be empty.
     */
    public final static String MSGCODE_ATTRIBUTE_NAME_IS_EMPTY = MSGCODE_PREFIX + "AttributeNameIsEmpty"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the data type not exist.
     */
    public final static String MSGCODE_DATATYPE_NOT_FOUND = MSGCODE_PREFIX + "DatatypeNotFound"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the attribute and the data type are given. This is
     * an validation error, because the data type will be searched using the attribute, thus when
     * the attribute is given then the data type should be empty and vice versa.
     */
    public final static String MSGCODE_DATATYPE_AND_ATTRIBUTE_GIVEN = MSGCODE_PREFIX + "DatatypeAndAttributeAreGiven"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the name of the test attribute is not a valid Java
     * field identifier.
     */
    public final static String MSGCODE_INVALID_TEST_ATTRIBUTE_NAME = MSGCODE_PREFIX + "InvalidTestAttributeName"; //$NON-NLS-1$

    /**
     * Sets the attribute's name.
     */
    public void setName(String newName);

    /**
     * Returns the attribute's name. {@inheritDoc}
     */
    @Override
    public String getName();

    /**
     * Returns the attribute.
     */
    public String getAttribute();

    /**
     * Sets the given attribute.
     * <p>
     * Note that this method is not recommended, because the additional storing of the policy
     * component type must be done manually. Is is recommended to use the method
     * {@link #setAttribute(IPolicyCmptTypeAttribute)} instead, because here the policy component
     * type will be stored if necessary.
     */
    public void setAttribute(String attribute);

    /**
     * Sets the given attribute. Furthermore if the given attribute belongs to a different type the
     * parent test policy component type parameter belongs to then store the policy component type
     * in the test attribute, thus finding this kind of subtype attributes can be performed without
     * searching the whole subtype hierarchy of the policy component type. If the policy component
     * type of the attribute and the policy component type of the parent test policy component type
     * parameter are the same then the policy component type property of this attribute will be left
     * empty.
     */
    public void setAttribute(IPolicyCmptTypeAttribute attribute);

    /**
     * Returns the test attribute's data type. Note that only value data types are allowed as
     * attribute data type.
     */
    public String getDatatype();

    /**
     * Sets the test attribute's data type. Note that only value data types are allowed as attribute
     * data type.
     */
    public void setDatatype(String newDatatype);

    /**
     * Returns the policy component type the attribute belongs to.
     * 
     * @throws CoreException If there was an error while searching the corresponding policy
     *             component type.
     */
    public String getCorrespondingPolicyCmptType() throws CoreException;

    /**
     * Returns the policy component type which is stored beside the attribute.<br>
     * Note that if the policy component type of the parent test policy component type parameter is
     * the same as the attributes policy component type then this method returns an empty String.
     * Because the policy component type will only be stored in special case (see
     * setAttribute(IPolicyCmptTypeAttribute)). To get always the corresponding policy component
     * type of the attribute use #getCorrespondingPolicyCmptType() instead.
     * 
     * @see #setAttribute(IPolicyCmptTypeAttribute)
     * @see #getCorrespondingPolicyCmptType()
     */
    public String getPolicyCmptType();

    /**
     * Sets the policy component type the attribute belongs to.
     */
    public void setPolicyCmptType(String policyCmptType);

    /**
     * Returns the test policy component type parameter this test attribute belongs to.
     */
    public TestPolicyCmptTypeParameter getTestPolicyCmptTypeParameter();

    /**
     * Returns the model attribute or <code>null</code> if the attribute does not exists.
     * 
     * @param ipsProject The ips project which object path is used to search.
     * 
     * @throws CoreException if an error occurs while searching for the attribute.
     */
    public IPolicyCmptTypeAttribute findAttribute(IIpsProject ipsProject) throws CoreException;

    /**
     * Returns the test attribute's value data type.
     * 
     * @param project The project which IPS object path is used for the search. This is not
     *            necessarily the project this type is part of.
     * 
     * @see #getDatatype() #getAttribute()
     */
    public ValueDatatype findDatatype(IIpsProject project) throws CoreException;

    /**
     * Returns <code>true</code> if the test attribute is an input attribute, otherwise
     * <code>false</code>.
     */
    public boolean isInputAttribute();

    /**
     * Returns the parameter type of the test attribute.
     */
    public TestParameterType getTestAttributeType();

    /**
     * Returns <code>true</code> if the test attribute is an expected result attribute, otherwise
     * <code>false</code>.
     */
    public boolean isExpextedResultAttribute();

    /**
     * Sets the type of the test attribute. The following types could be set.
     * <p>
     * <ul>
     * <li>INPUT: The test attribute specifies test attribute input objects.
     * <li>EXPECTED_RESULT: The test attribute specifies test attribute expected result objects.
     * </ul>
     */
    public void setTestAttributeType(TestParameterType type);

    /**
     * Returns <code>true</code> if the attribute is relevant for the given product component.
     * Checks if the attribute exists in the supertype hierarchy of the policy component type of the
     * given product component. If the attribute exists in the supertype hierarchy then return
     * <code>true</code>, otherwise <code>false</code>. Returns <code>true</code> if the attributes
     * type is not product relevant, in this case the attribute is always relevant.
     * 
     * @param ipsProject The IPS project which object path is used to search.
     * 
     * @throws CoreException If an error occurs.
     */
    public boolean isAttributeRelevantByProductCmpt(IProductCmpt productCmpt, IIpsProject ipsProject)
            throws CoreException;

    /**
     * Returns <code>true</code> if the test attribute based on a model attribute (policy component
     * type attribute). This kind of attributes are also known as test extension attributes.
     */
    public boolean isBasedOnModelAttribute();

}
