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
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptCategory;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptPropertyDirectReference;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.core.model.productcmpttype.ITableStructureUsage;
import org.faktorips.devtools.core.model.type.IProductCmptProperty;
import org.faktorips.devtools.core.model.type.ProductCmptPropertyType;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;

public class ProductCmptPropertyDirectReferenceTest extends AbstractIpsPluginTest {

    private IProductCmptType type;

    private IProductCmptCategory category;

    private IProductCmptTypeAttribute attributeProperty;

    private IProductCmptPropertyDirectReference attributeReference;

    @Override
    @Before
    public void setUp() throws CoreException {
        IIpsProject ipsProject = newIpsProject();
        type = newProductCmptType(ipsProject, "ProductCmptType");
        category = type.newProductCmptCategory();
        attributeProperty = type.newProductCmptTypeAttribute("attribute");
        attributeReference = category.newProductCmptPropertyReference(attributeProperty);
    }

    @Test
    public void shouldRetrieveNameFromReferencedProperty() {
        assertEquals(attributeProperty.getName(), attributeReference.getName());
    }

    @Test
    public void shouldRetrievePropertyTypeFromReferencedProperty() {
        assertEquals(ProductCmptPropertyType.PRODUCT_CMPT_TYPE_ATTRIBUTE,
                attributeReference.getProductCmptPropertyType());
    }

    @Test
    public void shouldIdentifyReferencedProperty() {
        IProductCmptTypeMethod methodProperty = type.newProductCmptTypeMethod();
        methodProperty.setName("method");
        methodProperty.setFormulaSignatureDefinition(true);

        assertTrue(attributeReference.isReferencingProperty(attributeProperty));
        assertFalse(attributeReference.isReferencingProperty(methodProperty));
    }

    @Test
    public void shouldFindReferencedProperty() throws CoreException {
        assertEquals(attributeProperty,
                attributeReference.findReferencedProductCmptProperty(attributeReference.getIpsProject()));
    }

    @Test
    public void shouldBePersistedToXmlReferencingAttribute() throws ParserConfigurationException {
        shouldBePersistedToXml(attributeReference, attributeProperty,
                ProductCmptPropertyType.PRODUCT_CMPT_TYPE_ATTRIBUTE);
    }

    @Test
    public void shouldBePersistedToXmlReferencingMethod() throws ParserConfigurationException {
        IProductCmptTypeMethod methodProperty = type.newProductCmptTypeMethod();
        methodProperty.setName("methodProperty");
        methodProperty.setFormulaName(methodProperty.getName());
        methodProperty.isFormulaSignatureDefinition();
        IProductCmptPropertyDirectReference methodReference = category.newProductCmptPropertyReference(methodProperty);

        shouldBePersistedToXml(methodReference, methodProperty, ProductCmptPropertyType.FORMULA_SIGNATURE_DEFINITION);
    }

    @Test
    public void shouldBePersistedToXmlReferencingTableStructureUsage() throws ParserConfigurationException {
        ITableStructureUsage tsuProperty = type.newTableStructureUsage();
        tsuProperty.setRoleName("tsuProperty");
        IProductCmptPropertyDirectReference tsuReference = category.newProductCmptPropertyReference(tsuProperty);

        shouldBePersistedToXml(tsuReference, tsuProperty, ProductCmptPropertyType.TABLE_STRUCTURE_USAGE);
    }

    private void shouldBePersistedToXml(IProductCmptPropertyDirectReference reference,
            IProductCmptProperty property,
            ProductCmptPropertyType propertyType) throws ParserConfigurationException {

        IProductCmptTypeAttribute otherProperty = type.newProductCmptTypeAttribute("otherProperty");

        Element xmlElement = reference.toXml(createXmlDocument(IProductCmptPropertyDirectReference.XML_TAG_NAME));
        IProductCmptPropertyDirectReference loadedReference = category.newProductCmptPropertyReference(otherProperty);
        loadedReference.initFromXml(xmlElement);

        assertEquals(property.getName(), loadedReference.getName());
        assertEquals(propertyType, loadedReference.getProductCmptPropertyType());
    }

}
