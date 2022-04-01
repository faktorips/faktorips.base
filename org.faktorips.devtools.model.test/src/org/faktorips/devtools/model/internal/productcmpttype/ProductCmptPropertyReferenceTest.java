/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.productcmpttype;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import javax.xml.parsers.ParserConfigurationException;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.model.productcmpttype.IProductCmptPropertyReference;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.model.type.IProductCmptProperty;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @deprecated for removal since 22.6; Use {@link IProductCmptProperty#getCategoryPosition()}
 *             instead.
 */
@Deprecated(forRemoval = true, since = "22.6")
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
        attributeProperty.setValueSetConfiguredByProduct(true);

        attributeReference = new ProductCmptPropertyReference(productType, "id");
        attributeReference.setReferencedProperty(attributeProperty);
    }

    @Test
    public void testSetReferencedProperty() {
        IProductCmptTypeAttribute productAttribute = productType.newProductCmptTypeAttribute("productAttribute");
        attributeReference.setReferencedProperty(productAttribute);

        assertEquals(productAttribute.getId(),
                ((ProductCmptPropertyReference)attributeReference).getReferencedPartId());
        assertEquals(productType.getIpsObjectType(), attributeReference.getReferencedIpsObjectType());
        assertWholeContentChangedEvent(attributeReference.getIpsSrcFile());
    }

    @Test
    public void testSetReferencedPartId() {
        attributeReference.setReferencedPartId("foo");

        assertEquals("foo", attributeReference.getReferencedPartId());
        assertPropertyChangedEvent(attributeReference, IProductCmptPropertyReference.PROPERTY_REFERENCED_PART_ID,
                attributeProperty.getId(), "foo");
    }

    @Test
    public void testSetReferencedIpsObjectType() {
        attributeReference.setReferencedIpsObjectType(IpsObjectType.PRODUCT_CMPT_TYPE);

        assertEquals(IpsObjectType.PRODUCT_CMPT_TYPE, attributeReference.getReferencedIpsObjectType());
        assertPropertyChangedEvent(attributeReference,
                IProductCmptPropertyReference.PROPERTY_REFERENCED_IPS_OBJECT_TYPE, policyType.getIpsObjectType(),
                IpsObjectType.PRODUCT_CMPT_TYPE);
    }

    @Test
    public void testIsReferencedProperty() throws SecurityException, IllegalArgumentException, NoSuchFieldException,
            IllegalAccessException {

        IProductCmptTypeAttribute productAttribute = productType.newProductCmptTypeAttribute("productAttribute");
        setPartId(productAttribute, "foo");
        IProductCmptPropertyReference productReference = new ProductCmptPropertyReference(productType, "a");
        productReference.setReferencedProperty(productAttribute);

        IPolicyCmptTypeAttribute policyAttribute = policyType.newPolicyCmptTypeAttribute("policyAttribute");
        policyAttribute.setValueSetConfiguredByProduct(true);
        setPartId(policyAttribute, "foo");
        IProductCmptPropertyReference policyReference = new ProductCmptPropertyReference(productType, "b");
        policyReference.setReferencedProperty(policyAttribute);

        assertTrue(productReference.isReferencedProperty(productAttribute));
        assertFalse(productReference.isReferencedProperty(policyAttribute));
        assertTrue(policyReference.isReferencedProperty(policyAttribute));
        assertFalse(policyReference.isReferencedProperty(productAttribute));
    }

    /**
     * <strong>Scenario:</strong><br>
     * The {@link IProductCmptPropertyReference} is asked to identify the
     * {@link IProductCmptProperty} it references, even tough
     * {@link IProductCmptPropertyReference#setReferencedProperty(IProductCmptProperty)} has not yet
     * been called.
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * False should be returned, but an exception must not be thrown.
     */
    @Test
    public void testIsReferencedProperty_IncompleteReference() {
        IProductCmptPropertyReference reference = new ProductCmptPropertyReference(productType, "");
        assertFalse(reference.isReferencedProperty(attributeProperty));
    }

    /**
     * <strong>Scenario:</strong><br>
     * In a type hierarchy, two properties with the same id exist.
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * The {@link IProductCmptPropertyReference} must be able to correctly identify it's referenced
     * {@link IProductCmptProperty} nevertheless.
     */
    @Test
    public void testIsReferencedProperty_PropertyWithSameIdInSupertype() throws IpsException, SecurityException,
            IllegalArgumentException, NoSuchFieldException, IllegalAccessException {

        IProductCmptType superProductType = newProductCmptType(ipsProject, "SuperProductCmptType");
        productType.setSupertype(superProductType.getQualifiedName());

        IProductCmptTypeAttribute productAttribute = productType.newProductCmptTypeAttribute("productAttribute");
        setPartId(productAttribute, "foo");
        IProductCmptTypeAttribute superProductAttribute = superProductType
                .newProductCmptTypeAttribute("superProductAttribute");
        setPartId(superProductAttribute, "foo");

        IProductCmptPropertyReference reference = new ProductCmptPropertyReference(productType, "id");
        reference.setReferencedProperty(productAttribute);

        assertTrue(reference.isReferencedProperty(productAttribute));
        assertFalse(reference.isReferencedProperty(superProductAttribute));
    }

    @Test
    public void testFindProductCmptProperty() {
        IPolicyCmptTypeAttribute policyAttribute = policyType.newPolicyCmptTypeAttribute("policyAttribute");
        policyAttribute.setValueSetConfiguredByProduct(true);

        IProductCmptPropertyReference policyAttributeReference = new ProductCmptPropertyReference(productType, "id1");
        policyAttributeReference.setReferencedProperty(policyAttribute);

        assertEquals(((ProductCmptType)productType).findProductCmptProperty(policyAttributeReference, ipsProject),
                policyAttributeReference.findProductCmptProperty(ipsProject));
    }

    @Test
    public void testXml() throws ParserConfigurationException {
        Element xmlElement = attributeReference.toXml(createXmlDocument(ProductCmptPropertyReference.XML_TAG_NAME));
        IProductCmptPropertyReference loadedReference = new ProductCmptPropertyReference(productType, "blub");
        loadedReference.initFromXml(xmlElement);

        assertEquals(attributeProperty.getId(), ((ProductCmptPropertyReference)loadedReference).getReferencedPartId());
        assertEquals(attributeProperty.getType().getIpsObjectType(), loadedReference.getReferencedIpsObjectType());
    }

    @Test
    public void testCreateElement() {
        Document document = mock(Document.class);
        ((ProductCmptPropertyReference)attributeReference).createElement(document);
        verify(document).createElement(ProductCmptPropertyReference.XML_TAG_NAME);
    }

}
