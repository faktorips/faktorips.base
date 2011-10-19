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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptPropertyReference;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.core.model.productcmpttype.ITableStructureUsage;
import org.faktorips.devtools.core.model.type.ProductCmptPropertyType;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ProductCmptPropertyReferenceTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;

    private IPolicyCmptType policyType;

    private IProductCmptType productType;

    private IPolicyCmptTypeAttribute attributeProperty;

    private ProductCmptPropertyReference attributeReference;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        ipsProject = newIpsProject();

        policyType = newPolicyAndProductCmptType(ipsProject, "PolicyCmptType", "ProductCmptType");
        productType = policyType.findProductCmptType(ipsProject);
        attributeProperty = policyType.newPolicyCmptTypeAttribute();
        attributeProperty.setName("attribute");
        attributeProperty.setProductRelevant(true);

        attributeReference = new ProductCmptPropertyReference(productType, "id");
        attributeReference.setName(attributeProperty.getName());
        attributeReference.setProductCmptPropertyType(attributeProperty.getProductCmptPropertyType());
    }

    @Test
    public void testSetName() {
        attributeReference.setName("foo");

        assertEquals("foo", attributeReference.getName());
        assertPropertyChangedEvent(attributeReference, IProductCmptPropertyReference.PROPERTY_NAME,
                attributeProperty.getName(), "foo");
    }

    @Test
    public void testSetPropertyType() {
        attributeReference.setProductCmptPropertyType(ProductCmptPropertyType.PRODUCT_CMPT_TYPE_ATTRIBUTE);

        assertEquals(ProductCmptPropertyType.PRODUCT_CMPT_TYPE_ATTRIBUTE,
                attributeReference.getProductCmptPropertyType());
        assertPropertyChangedEvent(attributeReference, IProductCmptPropertyReference.PROPERTY_PROPERTY_TYPE,
                attributeProperty.getProductCmptPropertyType(), ProductCmptPropertyType.PRODUCT_CMPT_TYPE_ATTRIBUTE);
    }

    @Test
    public void testIsReferencingProperty() {
        IValidationRule validationRuleProperty = policyType.newRule();
        validationRuleProperty.setName("validationRule");

        assertTrue(attributeReference.isReferencingProperty(attributeProperty));
        assertFalse(attributeReference.isReferencingProperty(validationRuleProperty));
    }

    @Test
    public void testFindProductCmptProperty() throws CoreException {
        IPolicyCmptTypeAttribute policyAttribute = policyType.newPolicyCmptTypeAttribute("policyAttribute");
        IValidationRule validationRule = policyType.newRule();
        validationRule.setName("validationRule");
        IProductCmptTypeMethod formula = productType.newProductCmptTypeMethod();
        formula.setName("formula");
        formula.setFormulaName("formula");
        formula.setFormulaSignatureDefinition(true);
        ITableStructureUsage tsu = productType.newTableStructureUsage();
        tsu.setRoleName("tsu");
        IProductCmptTypeAttribute productAttribute = productType.newProductCmptTypeAttribute("productAttribute");

        IProductCmptPropertyReference policyAttributeReference = new ProductCmptPropertyReference(productType, "id1");
        policyAttributeReference.setName(policyAttribute.getName());
        policyAttributeReference.setProductCmptPropertyType(policyAttribute.getProductCmptPropertyType());

        IProductCmptPropertyReference validationRuleReference = new ProductCmptPropertyReference(productType, "id2");
        validationRuleReference.setName(validationRule.getName());
        validationRuleReference.setProductCmptPropertyType(validationRule.getProductCmptPropertyType());

        IProductCmptPropertyReference formulaReference = new ProductCmptPropertyReference(productType, "id3");
        formulaReference.setName(formula.getName());
        formulaReference.setProductCmptPropertyType(formula.getProductCmptPropertyType());

        IProductCmptPropertyReference tsuReference = new ProductCmptPropertyReference(productType, "id4");
        tsuReference.setName(tsu.getName());
        tsuReference.setProductCmptPropertyType(tsu.getProductCmptPropertyType());

        IProductCmptPropertyReference productAttributeReference = new ProductCmptPropertyReference(productType, "id5");
        productAttributeReference.setName(productAttribute.getName());
        productAttributeReference.setProductCmptPropertyType(productAttribute.getProductCmptPropertyType());

        assertEquals(policyAttribute, policyAttributeReference.findProductCmptProperty(ipsProject));
        assertEquals(validationRule, validationRuleReference.findProductCmptProperty(ipsProject));
        assertEquals(formula, formulaReference.findProductCmptProperty(ipsProject));
        assertEquals(tsu, tsuReference.findProductCmptProperty(ipsProject));
        assertEquals(productAttribute, productAttributeReference.findProductCmptProperty(ipsProject));
    }

    @Test
    public void testXml() throws ParserConfigurationException {
        IPolicyCmptTypeAttribute otherProperty = policyType.newPolicyCmptTypeAttribute();
        otherProperty.setName("otherProperty");
        otherProperty.setProductRelevant(true);

        Element xmlElement = attributeReference.toXml(createXmlDocument(IProductCmptPropertyReference.XML_TAG_NAME));
        IProductCmptPropertyReference loadedReference = new ProductCmptPropertyReference(productType, "blub");
        loadedReference.initFromXml(xmlElement);

        assertEquals(attributeProperty.getName(), loadedReference.getName());
        assertEquals(attributeProperty.getProductCmptPropertyType(), loadedReference.getProductCmptPropertyType());
    }

    @Test
    public void testCreateElement() {
        Document document = mock(Document.class);
        attributeReference.createElement(document);
        verify(document).createElement(IProductCmptPropertyReference.XML_TAG_NAME);
    }

}
