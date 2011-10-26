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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.lang.reflect.Field;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsObjectPart;
import org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptPropertyReference.SourceType;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptPropertyReference;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.core.model.productcmpttype.ITableStructureUsage;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ProductCmptPropertyReferenceTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;

    private IPolicyCmptType policyType;

    private IProductCmptType productType;

    private IPolicyCmptTypeAttribute attributeProperty;

    private IProductCmptPropertyReference attributeReference;

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
        attributeReference.setReferencedProperty(attributeProperty);
    }

    @Test
    public void testSetReferencedProperty() {
        IProductCmptTypeAttribute productAttribute = productType.newProductCmptTypeAttribute("productAttribute");
        attributeReference.setReferencedProperty(productAttribute);

        assertEquals(productAttribute.getId(), ((ProductCmptPropertyReference)attributeReference).getReferencedPartId());
        assertEquals(SourceType.PRODUCT, ((ProductCmptPropertyReference)attributeReference).getSourceType());
        assertWholeContentChangedEvent(attributeReference.getIpsSrcFile());
    }

    @Test
    public void testIsReferencingProperty() throws SecurityException, IllegalArgumentException, NoSuchFieldException,
            IllegalAccessException {

        IProductCmptTypeAttribute productAttribute = productType.newProductCmptTypeAttribute("productAttribute");
        setId(productAttribute, "foo");
        IProductCmptPropertyReference productReference = new ProductCmptPropertyReference(productType, "a");
        productReference.setReferencedProperty(productAttribute);

        IPolicyCmptTypeAttribute policyAttribute = policyType.newPolicyCmptTypeAttribute("policyAttribute");
        policyAttribute.setProductRelevant(true);
        setId(policyAttribute, "foo");
        IProductCmptPropertyReference policyReference = new ProductCmptPropertyReference(productType, "b");
        policyReference.setReferencedProperty(policyAttribute);

        assertTrue(productReference.isReferencingProperty(productAttribute));
        assertFalse(productReference.isReferencingProperty(policyAttribute));
        assertTrue(policyReference.isReferencingProperty(policyAttribute));
        assertFalse(policyReference.isReferencingProperty(productAttribute));
    }

    @Test
    public void testFindProductCmptProperty() throws CoreException {
        IPolicyCmptTypeAttribute policyAttribute = policyType.newPolicyCmptTypeAttribute("policyAttribute");
        policyAttribute.setProductRelevant(true);
        IValidationRule validationRule = policyType.newRule();
        validationRule.setName("validationRule");
        validationRule.setConfigurableByProductComponent(true);
        IProductCmptTypeMethod formula = productType.newProductCmptTypeMethod();
        formula.setName("formula");
        formula.setFormulaName("formula");
        formula.setFormulaSignatureDefinition(true);
        ITableStructureUsage tsu = productType.newTableStructureUsage();
        tsu.setRoleName("tsu");
        IProductCmptTypeAttribute productAttribute = productType.newProductCmptTypeAttribute("productAttribute");

        IProductCmptPropertyReference policyAttributeReference = new ProductCmptPropertyReference(productType, "id1");
        policyAttributeReference.setReferencedProperty(policyAttribute);

        IProductCmptPropertyReference validationRuleReference = new ProductCmptPropertyReference(productType, "id2");
        validationRuleReference.setReferencedProperty(validationRule);

        IProductCmptPropertyReference formulaReference = new ProductCmptPropertyReference(productType, "id3");
        formulaReference.setReferencedProperty(formula);

        IProductCmptPropertyReference tsuReference = new ProductCmptPropertyReference(productType, "id4");
        tsuReference.setReferencedProperty(tsu);

        IProductCmptPropertyReference productAttributeReference = new ProductCmptPropertyReference(productType, "id5");
        productAttributeReference.setReferencedProperty(productAttribute);

        assertEquals(policyAttribute, policyAttributeReference.findProductCmptProperty(ipsProject));
        assertEquals(validationRule, validationRuleReference.findProductCmptProperty(ipsProject));
        assertEquals(formula, formulaReference.findProductCmptProperty(ipsProject));
        assertEquals(tsu, tsuReference.findProductCmptProperty(ipsProject));
        assertEquals(productAttribute, productAttributeReference.findProductCmptProperty(ipsProject));
    }

    @Test
    public void testFindProductCmptPropertyPolicyCmptTypeNotFound() throws CoreException {
        IPolicyCmptTypeAttribute policyAttribute = policyType.newPolicyCmptTypeAttribute("policyAttribute");
        policyAttribute.setProductRelevant(true);

        IProductCmptPropertyReference policyAttributeReference = new ProductCmptPropertyReference(productType, "id1");
        policyAttributeReference.setReferencedProperty(policyAttribute);

        policyType.delete();

        // Null should be returned, but no exception may be thrown
        assertNull(policyAttributeReference.findProductCmptProperty(ipsProject));
    }

    @Test
    public void testFindProductCmptPropertySameIdInPolicyTypeAndProductType() throws CoreException, SecurityException,
            IllegalArgumentException, NoSuchFieldException, IllegalAccessException {

        IPolicyCmptTypeAttribute policyAttribute = policyType.newPolicyCmptTypeAttribute("policyAttribute");
        policyAttribute.setName("policyAttribute");
        policyAttribute.setProductRelevant(true);
        setId(policyAttribute, "foo");
        IProductCmptTypeAttribute productAttribute = productType.newProductCmptTypeAttribute("productAttribute");
        productAttribute.setName("productAttribute");
        setId(productAttribute, "foo");

        IProductCmptPropertyReference policyAttributeReference = new ProductCmptPropertyReference(productType, "id1");
        policyAttributeReference.setReferencedProperty(policyAttribute);
        IProductCmptPropertyReference productAttributeReference = new ProductCmptPropertyReference(productType, "id1");
        productAttributeReference.setReferencedProperty(productAttribute);

        assertEquals(policyAttribute, policyAttributeReference.findProductCmptProperty(ipsProject));
        assertEquals(productAttribute, productAttributeReference.findProductCmptProperty(ipsProject));
    }

    private void setId(IIpsObjectPart part, String id) throws SecurityException, NoSuchFieldException,
            IllegalArgumentException, IllegalAccessException {

        Field field = IpsObjectPart.class.getDeclaredField("id");
        field.setAccessible(true);
        field.set(part, id);
    }

    @Test
    public void testXml() throws ParserConfigurationException {
        Element xmlElement = attributeReference.toXml(createXmlDocument(IProductCmptPropertyReference.XML_TAG_NAME));
        IProductCmptPropertyReference loadedReference = new ProductCmptPropertyReference(productType, "blub");
        loadedReference.initFromXml(xmlElement);

        assertEquals(attributeProperty.getId(), ((ProductCmptPropertyReference)loadedReference).getReferencedPartId());
        assertEquals(SourceType.POLICY, ((ProductCmptPropertyReference)loadedReference).getSourceType());
    }

    @Test
    public void testCreateElement() {
        Document document = mock(Document.class);
        ((ProductCmptPropertyReference)attributeReference).createElement(document);
        verify(document).createElement(IProductCmptPropertyReference.XML_TAG_NAME);
    }

}
