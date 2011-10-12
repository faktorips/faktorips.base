/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.internal.model.productcmpttype;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptCategory;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptPropertyExternalReference;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.type.IProductCmptProperty;
import org.faktorips.devtools.core.model.type.ProductCmptPropertyType;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;

public class ProductCmptPropertyExternalReferenceTest extends AbstractIpsPluginTest {

    private IPolicyCmptType policyType;

    private IProductCmptType productType;

    private IProductCmptCategory category;

    private IPolicyCmptTypeAttribute attributeProperty;

    private IProductCmptPropertyExternalReference attributeReference;

    @Override
    @Before
    public void setUp() throws CoreException {
        IIpsProject ipsProject = newIpsProject();
        policyType = newPolicyAndProductCmptType(ipsProject, "PolicyCmptType", "ProductCmptType");
        productType = policyType.findProductCmptType(ipsProject);
        category = productType.newProductCmptCategory();
        attributeProperty = policyType.newPolicyCmptTypeAttribute();
        attributeProperty.setName("attribute");
        attributeProperty.setProductRelevant(true);
        attributeReference = category.newProductCmptPropertyReference(attributeProperty);
    }

    @Test
    public void shouldAllowToSetName() {
        attributeReference.setName("foo");
        assertEquals("foo", attributeReference.getName());
        attributeReference.setName("bar");
        assertEquals("bar", attributeReference.getName());
    }

    @Test
    public void shouldAllowToSetPropertyTypeToPolicyCmptTypeAttribute() {
        attributeReference.setProductCmptPropertyType(ProductCmptPropertyType.POLICY_CMPT_TYPE_ATTRIBUTE);
        assertEquals(ProductCmptPropertyType.POLICY_CMPT_TYPE_ATTRIBUTE,
                attributeReference.getProductCmptPropertyType());
    }

    @Test
    public void shouldAllowToSetPropertyTypeToValidationRule() {
        attributeReference.setProductCmptPropertyType(ProductCmptPropertyType.VALIDATION_RULE);
        assertEquals(ProductCmptPropertyType.VALIDATION_RULE, attributeReference.getProductCmptPropertyType());
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionWhenSettingPropertyTypeToFormulaSignatureDefinition() {
        attributeReference.setProductCmptPropertyType(ProductCmptPropertyType.FORMULA_SIGNATURE_DEFINITION);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionWhenSettingPropertyTypeToProductCmptTypeAttribute() {
        attributeReference.setProductCmptPropertyType(ProductCmptPropertyType.PRODUCT_CMPT_TYPE_ATTRIBUTE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionWhenSettingPropertyTypeToTableStructureUsage() {
        attributeReference.setProductCmptPropertyType(ProductCmptPropertyType.TABLE_STRUCTURE_USAGE);
    }

    @Test
    public void shouldIdentifyReferencedProperty() {
        IValidationRule validationRuleProperty = policyType.newRule();
        validationRuleProperty.setName("validationRule");

        assertTrue(attributeReference.isReferencingProperty(attributeProperty));
        assertFalse(attributeReference.isReferencingProperty(validationRuleProperty));
    }

    @Test
    public void shouldFindReferencedProperty() throws CoreException {
        assertEquals(attributeProperty,
                attributeReference.findReferencedProductCmptProperty(attributeReference.getIpsProject()));
    }

    @Test
    public void shouldBePersistedToXmlReferencingPolicyCmptTypeAttribute() throws ParserConfigurationException {
        IPolicyCmptTypeAttribute attributeProperty = policyType.newPolicyCmptTypeAttribute();
        attributeProperty.setName("validationRuleProperty");
        attributeProperty.setProductRelevant(true);
        IProductCmptPropertyExternalReference attributeReference = category
                .newProductCmptPropertyReference(attributeProperty);

        shouldBePersistedToXml(attributeReference, attributeProperty,
                ProductCmptPropertyType.POLICY_CMPT_TYPE_ATTRIBUTE);
    }

    @Test
    public void shouldBePersistedToXmlReferencingValidationRule() throws ParserConfigurationException {
        IValidationRule validationRuleProperty = policyType.newRule();
        validationRuleProperty.setName("validationRuleProperty");
        IProductCmptPropertyExternalReference validationRuleReference = category
                .newProductCmptPropertyReference(validationRuleProperty);

        shouldBePersistedToXml(validationRuleReference, validationRuleProperty, ProductCmptPropertyType.VALIDATION_RULE);
    }

    private void shouldBePersistedToXml(IProductCmptPropertyExternalReference reference,
            IProductCmptProperty property,
            ProductCmptPropertyType propertyType) throws ParserConfigurationException {

        IPolicyCmptTypeAttribute otherProperty = policyType.newPolicyCmptTypeAttribute();
        otherProperty.setName("otherProperty");
        otherProperty.setProductRelevant(true);

        Element xmlElement = reference.toXml(createXmlDocument(IProductCmptPropertyExternalReference.XML_TAG_NAME));
        IProductCmptPropertyExternalReference loadedReference = category.newProductCmptPropertyReference(otherProperty);
        loadedReference.initFromXml(xmlElement);

        assertEquals(property.getName(), loadedReference.getName());
        assertEquals(propertyType, loadedReference.getProductCmptPropertyType());
    }

}
