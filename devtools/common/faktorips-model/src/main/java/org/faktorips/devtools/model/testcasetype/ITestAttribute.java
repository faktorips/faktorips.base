/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.testcasetype;

import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.ipsobject.IDescribedElement;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;

/**
 * Specification of a test attribute.
 * 
 * @author Joerg Ortmann
 */
public interface ITestAttribute extends IIpsObjectPart, IDescribedElement {

    String PROPERTY_ATTRIBUTE = "attribute"; //$NON-NLS-1$

    String PROPERTY_DATATYPE = "datatype"; //$NON-NLS-1$

    String PROPERTY_POLICYCMPTTYPE_OF_ATTRIBUTE = "policyCmptType"; //$NON-NLS-1$

    String PROPERTY_TEST_ATTRIBUTE_TYPE = "testAttributeType"; //$NON-NLS-1$

    /**
     * Prefix for all message codes of this class.
     */
    String MSGCODE_PREFIX = "TESTATTRIBUTE-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the attribute which is related by the test attribute
     * not exists.
     */
    String MSGCODE_ATTRIBUTE_NOT_FOUND = MSGCODE_PREFIX + "AttributeNotFound"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that there is an unsupported type.
     */
    String MSGCODE_WRONG_TYPE = MSGCODE_PREFIX + "WrongType"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the type doesn't matches the parent type.<br>
     * E.g. the parent defines the input type and the attribute has the expected result type.
     */
    String MSGCODE_TYPE_DOES_NOT_MATCH_PARENT_TYPE = MSGCODE_PREFIX + "TypeDoesNotMatchParentType"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the attribute name is duplicate.
     */
    String MSGCODE_DUPLICATE_TEST_ATTRIBUTE_NAME = MSGCODE_PREFIX + "DuplicateTestAttributeName"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that derived (computed on the fly) attributes are not
     * supported.
     */
    String MSGCODE_DERIVED_ON_THE_FLY_ATTRIBUTES_NOT_SUPPORTED = MSGCODE_PREFIX
            + "DerivedAttributesOnTheFlyNotSupported"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that abstract attributes are not supported.
     */
    String MSGCODE_ABSTRACT_ATTRIBUTES_NOT_SUPPORTED = MSGCODE_PREFIX
            + "AbstractAttributesNotSupported"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the attribute and the attribute type is already
     * given.<br>
     * E.g. The attribute "a" could be added as input and expected result but not as input twice.
     */
    String MSGCODE_DUPLICATE_ATTRIBUTE_AND_TYPE = MSGCODE_PREFIX + "DuplicateAttributeAndType"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the attribute name must not be empty.
     */
    String MSGCODE_ATTRIBUTE_NAME_IS_EMPTY = MSGCODE_PREFIX + "AttributeNameIsEmpty"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the data type not exist.
     */
    String MSGCODE_DATATYPE_NOT_FOUND = MSGCODE_PREFIX + "DatatypeNotFound"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the attribute and the data type are given. This is
     * an validation error, because the data type will be searched using the attribute, thus when
     * the attribute is given then the data type should be empty and vice versa.
     */
    String MSGCODE_DATATYPE_AND_ATTRIBUTE_GIVEN = MSGCODE_PREFIX + "DatatypeAndAttributeAreGiven"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the name of the test attribute is not a valid Java
     * field identifier.
     */
    String MSGCODE_INVALID_TEST_ATTRIBUTE_NAME = MSGCODE_PREFIX + "InvalidTestAttributeName"; //$NON-NLS-1$

    /**
     * Sets the attribute's name.
     */
    void setName(String newName);

    /**
     * Returns the attribute's name. {@inheritDoc}
     */
    @Override
    String getName();

    /**
     * Returns the attribute.
     */
    String getAttribute();

    /**
     * Sets the given attribute.
     * <p>
     * Note that this method is not recommended, because the additional storing of the policy
     * component type must be done manually. Is is recommended to use the method
     * {@link #setAttribute(IPolicyCmptTypeAttribute)} instead, because here the policy component
     * type will be stored if necessary.
     */
    void setAttribute(String attribute);

    /**
     * Sets the given attribute. Furthermore if the given attribute belongs to a different type the
     * parent test policy component type parameter belongs to then store the policy component type
     * in the test attribute, thus finding this kind of subtype attributes can be performed without
     * searching the whole subtype hierarchy of the policy component type. If the policy component
     * type of the attribute and the policy component type of the parent test policy component type
     * parameter are the same then the policy component type property of this attribute will be left
     * empty.
     */
    void setAttribute(IPolicyCmptTypeAttribute attribute);

    /**
     * Returns the test attribute's data type. Note that only value data types are allowed as
     * attribute data type.
     */
    String getDatatype();

    /**
     * Sets the test attribute's data type. Note that only value data types are allowed as attribute
     * data type.
     */
    void setDatatype(String newDatatype);

    /**
     * Returns the policy component type the attribute belongs to.
     * 
     */
    String getCorrespondingPolicyCmptType();

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
    String getPolicyCmptType();

    /**
     * Sets the policy component type the attribute belongs to.
     */
    void setPolicyCmptType(String policyCmptType);

    /**
     * Returns the test policy component type parameter this test attribute belongs to.
     */
    ITestPolicyCmptTypeParameter getTestPolicyCmptTypeParameter();

    /**
     * Returns the model attribute or <code>null</code> if the attribute does not exist.
     * 
     * @param ipsProject The IPS project which object path is used to search.
     */
    IPolicyCmptTypeAttribute findAttribute(IIpsProject ipsProject);

    /**
     * Returns the test attribute's value data type.
     * 
     * @param project The project which IPS object path is used for the search. This is not
     *            necessarily the project this type is part of.
     * 
     * @see #getDatatype() #getAttribute()
     */
    ValueDatatype findDatatype(IIpsProject project);

    /**
     * Returns <code>true</code> if the test attribute is an input attribute, otherwise
     * <code>false</code>.
     */
    boolean isInputAttribute();

    /**
     * Returns the parameter type of the test attribute.
     */
    TestParameterType getTestAttributeType();

    /**
     * Returns <code>true</code> if the test attribute is an expected result attribute, otherwise
     * <code>false</code>.
     */
    boolean isExpextedResultAttribute();

    /**
     * Sets the type of the test attribute. The following types could be set.
     * <ul>
     * <li>INPUT: The test attribute specifies test attribute input objects.
     * <li>EXPECTED_RESULT: The test attribute specifies test attribute expected result objects.
     * </ul>
     */
    void setTestAttributeType(TestParameterType type);

    /**
     * Returns <code>true</code> if the attribute is relevant for the given product component.
     * Checks if the attribute exists in the supertype hierarchy of the policy component type of the
     * given product component. If the attribute exists in the supertype hierarchy then return
     * <code>true</code>, otherwise <code>false</code>. Returns <code>true</code> if the attributes
     * type is not product relevant, in this case the attribute is always relevant.
     * 
     * @param ipsProject The IPS project which object path is used to search.
     * 
     * @throws IpsException If an error occurs.
     */
    boolean isAttributeRelevantByProductCmpt(IProductCmpt productCmpt, IIpsProject ipsProject)
            throws IpsException;

    /**
     * Returns <code>true</code> if the test attribute based on a model attribute (policy component
     * type attribute). This kind of attributes are also known as test extension attributes.
     */
    boolean isBasedOnModelAttribute();

}
