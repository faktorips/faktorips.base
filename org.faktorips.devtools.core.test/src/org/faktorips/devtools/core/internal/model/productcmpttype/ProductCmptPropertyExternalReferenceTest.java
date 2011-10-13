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
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.ContentsChangeListener;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptCategory;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptPropertyExternalReference;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptPropertyReference;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.type.IProductCmptProperty;
import org.faktorips.devtools.core.model.type.ProductCmptPropertyType;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;

public class ProductCmptPropertyExternalReferenceTest extends AbstractIpsPluginTest implements ContentsChangeListener {

    private ContentChangeEvent lastEvent;

    private IIpsProject ipsProject;

    private IPolicyCmptType policyType;

    private IProductCmptType productType;

    private IProductCmptCategory category;

    private IPolicyCmptTypeAttribute attributeProperty;

    private IProductCmptPropertyExternalReference attributeReference;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        ipsProject = newIpsProject();
        ipsProject.getIpsModel().addChangeListener(this);

        policyType = newPolicyAndProductCmptType(ipsProject, "PolicyCmptType", "ProductCmptType");
        productType = policyType.findProductCmptType(ipsProject);
        category = productType.newProductCmptCategory();
        attributeProperty = policyType.newPolicyCmptTypeAttribute();
        attributeProperty.setName("attribute");
        attributeProperty.setProductRelevant(true);
        attributeReference = category.newProductCmptPropertyReference(attributeProperty);
    }

    @Override
    protected void tearDownExtension() throws Exception {
        ipsProject.getIpsModel().removeChangeListener(this);
    }

    @Test
    public void shouldAllowToSetName() {
        attributeReference.setName("foo");

        assertEquals("foo", attributeReference.getName());
        assertPropertyChangedEvent();
    }

    @Test
    public void shouldAllowToSetPropertyTypeToPolicyCmptTypeAttribute() {
        attributeReference.setProductCmptPropertyType(ProductCmptPropertyType.POLICY_CMPT_TYPE_ATTRIBUTE);

        assertEquals(ProductCmptPropertyType.POLICY_CMPT_TYPE_ATTRIBUTE,
                attributeReference.getProductCmptPropertyType());
        assertPropertyChangedEvent();
    }

    @Test
    public void shouldAllowToSetPropertyTypeToValidationRule() {
        attributeReference.setProductCmptPropertyType(ProductCmptPropertyType.VALIDATION_RULE);

        assertEquals(ProductCmptPropertyType.VALIDATION_RULE, attributeReference.getProductCmptPropertyType());
        assertPropertyChangedEvent();
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
        IPolicyCmptTypeAttribute policyCmptTypeAttribute = policyType.newPolicyCmptTypeAttribute();
        policyCmptTypeAttribute.setName("policyCmptTypeAttribute");
        policyCmptTypeAttribute.setProductRelevant(true);
        IProductCmptPropertyExternalReference attributeReference = category
                .newProductCmptPropertyReference(policyCmptTypeAttribute);

        shouldBePersistedToXml(attributeReference, policyCmptTypeAttribute,
                ProductCmptPropertyType.POLICY_CMPT_TYPE_ATTRIBUTE);
    }

    @Test
    public void shouldBePersistedToXmlReferencingValidationRule() throws ParserConfigurationException {
        IValidationRule validationRule = policyType.newRule();
        validationRule.setName("validationRule");
        IProductCmptPropertyExternalReference validationRuleReference = category
                .newProductCmptPropertyReference(validationRule);

        shouldBePersistedToXml(validationRuleReference, validationRule, ProductCmptPropertyType.VALIDATION_RULE);
    }

    @Test
    public void shouldGenerateValidationErrorIfReferencedPropertyIsDeleted() throws CoreException {
        IProductCmptPropertyReference reference = category.newProductCmptPropertyReference(attributeProperty);

        attributeProperty.delete();
        MessageList validationMessageList = reference.validate(reference.getIpsProject());

        assertEquals(IProductCmptPropertyExternalReference.MSGCODE_REFERENCED_PROPERTY_COULD_NOT_BE_FOUND,
                validationMessageList.getFirstMessage(Message.ERROR).getCode());
        assertEquals(1, validationMessageList.size());
    }

    @Test
    public void shouldGenerateValidationErrorIfReferencedPropertyCannotBeFoundBecausePolicyCmptTypeIsNotFound()
            throws CoreException {

        IProductCmptPropertyReference reference = category.newProductCmptPropertyReference(attributeProperty);

        productType.setPolicyCmptType("");
        MessageList validationMessageList = reference.validate(reference.getIpsProject());

        assertEquals(IProductCmptPropertyExternalReference.MSGCODE_REFERENCED_PROPERTY_COULD_NOT_BE_FOUND,
                validationMessageList.getFirstMessage(Message.ERROR).getCode());
        assertEquals(1, validationMessageList.size());
    }

    public void shouldGenerateValidationErrorIfReferencedPolicyCmptTypeAttributeIsNotProductRelevant()
            throws CoreException {

        attributeProperty.setProductRelevant(false);
        IProductCmptPropertyReference reference = category.newProductCmptPropertyReference(attributeProperty);

        MessageList validationMessageList = reference.validate(reference.getIpsProject());

        assertEquals(IProductCmptPropertyExternalReference.MSGCODE_REFERENCED_PROPERTY_COULD_NOT_BE_FOUND,
                validationMessageList.getFirstMessage(Message.ERROR).getCode());
        assertEquals(1, validationMessageList.size());
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

    @Test
    public void shouldReturnTrueForExternalReferenceQuery() {
        assertTrue(attributeReference.isExternalReference());
    }

    @Override
    public void contentsChanged(ContentChangeEvent event) {
        lastEvent = event;
    }

    private void assertPropertyChangedEvent() {
        assertEquals(attributeReference, lastEvent.getPart());
        assertEquals(ContentChangeEvent.TYPE_PROPERTY_CHANGED, lastEvent.getEventType());
    }

}
