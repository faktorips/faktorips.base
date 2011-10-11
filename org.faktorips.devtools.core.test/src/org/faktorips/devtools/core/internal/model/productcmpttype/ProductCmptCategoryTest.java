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

import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptCategory;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptCategory.Side;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.type.IProductCmptProperty;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ProductCmptCategoryTest extends AbstractIpsPluginTest {

    private static final String CATEGORY_NAME = "foo";

    private IIpsProject ipsProject;

    private IProductCmptType type;

    private IProductCmptType superType;

    private IProductCmptType superSuperType;

    private IProductCmptProperty property;

    private IProductCmptProperty superProperty;

    private IProductCmptProperty superSuperProperty;

    private IProductCmptCategory category;

    private IProductCmptCategory superCategory;

    private IProductCmptCategory superSuperCategory;

    @Override
    @Before
    public void setUp() throws CoreException {
        ipsProject = newIpsProject();
        type = newProductCmptType(ipsProject, "Type");
        category = type.newProductCmptCategory();
        category.setName(CATEGORY_NAME);
        property = type.newProductCmptTypeAttribute();

        createSupertypeHierarchy();
    }

    private void createSupertypeHierarchy() throws CoreException {
        superSuperType = newProductCmptType(ipsProject, "SuperSuperType");
        superType = newProductCmptType(ipsProject, "SuperType");

        superType.setSupertype(superSuperType.getQualifiedName());
        type.setSupertype(superType.getQualifiedName());

        superSuperCategory = superSuperType.newProductCmptCategory();
        superSuperCategory.setName(CATEGORY_NAME);
        superCategory = superType.newProductCmptCategory();
        superCategory.setName(CATEGORY_NAME);
        superCategory.setInherited(true);
        category.setInherited(true);

        superSuperProperty = superSuperType.newProductCmptTypeAttribute();
        superProperty = superType.newProductCmptTypeAttribute();
    }

    @Test
    public void shouldInitializePropertiesToProperDefaultsOnCreation() {
        IProductCmptCategory category = type.newProductCmptCategory();
        assertEquals("", category.getName());
        assertFalse(category.isInherited());
        assertFalse(category.isDefaultForMethods());
        assertFalse(category.isDefaultForPolicyCmptTypeAttributes());
        assertFalse(category.isDefaultForProductCmptTypeAttributes());
        assertFalse(category.isDefaultForTableStructureUsages());
        assertFalse(category.isDefaultForValidationRules());
        assertTrue(category.isAtLeftSide());
    }

    @Test
    public void shouldReturnParentProductCmptType() {
        assertEquals(type, category.getProductCmptType());
    }

    @Test
    public void shouldAllowToSetName() {
        category.setName("bar");
        assertEquals("bar", category.getName());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void shouldReturnUnmodifiableListWhenRequestingAssignedProductCmptProperties() {
        category.assignProductCmptProperty(property);

        category.getAssignedProductCmptProperties().remove(0);
    }

    @Test
    public void shouldAllowToAssignProductCmptProperty() {
        boolean added = category.assignProductCmptProperty(property);

        assertTrue(added);
        assertTrue(category.isAssignedProductCmptProperty(property));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionWhenTryingToAssignAPropertyFromAnotherType() {
        category.assignProductCmptProperty(superProperty);
    }

    @Test
    public void shouldAllowToRemoveProductCmptProperty() {
        category.assignProductCmptProperty(property);
        boolean removed = category.removeProductCmptProperty(property);

        assertTrue(removed);
        assertFalse(category.isAssignedProductCmptProperty(property));
    }

    @Test
    public void shouldReturnFalseWhenRemovingNotAssignedProductCmptProperty() {
        boolean removed = category.removeProductCmptProperty(property);
        assertFalse(removed);
    }

    @Test
    public void shouldReturnFalseWhenAssigningAlreadyAssignedProductCmptProperty() {
        category.assignProductCmptProperty(property);
        boolean added = category.assignProductCmptProperty(property);

        assertFalse(added);
    }

    @Test
    public void shouldAllowToSetInheritedProperty() {
        IProductCmptCategory category = type.newProductCmptCategory();
        category.setInherited(true);
        assertTrue(category.isInherited());
        category.setInherited(false);
        assertFalse(category.isInherited());
    }

    @Test
    public void shouldAllowToBeMarkedAsDefaultForMethods() {
        category.setDefaultForMethods(true);
        assertTrue(category.isDefaultForMethods());
        category.setDefaultForMethods(false);
        assertFalse(category.isDefaultForMethods());
    }

    @Test
    public void shouldAllowToBeMarkedAsDefaultForPolicyCmptTypeAttributes() {
        category.setDefaultForPolicyCmptTypeAttributes(true);
        assertTrue(category.isDefaultForPolicyCmptTypeAttributes());
        category.setDefaultForPolicyCmptTypeAttributes(false);
        assertFalse(category.isDefaultForPolicyCmptTypeAttributes());
    }

    @Test
    public void shouldAllowToBeMarkedAsDefaultForProductCmptTypeAttributes() {
        category.setDefaultForProductCmptTypeAttributes(true);
        assertTrue(category.isDefaultForProductCmptTypeAttributes());
        category.setDefaultForProductCmptTypeAttributes(false);
        assertFalse(category.isDefaultForProductCmptTypeAttributes());
    }

    @Test
    public void shouldAllowToBeMarkedAsDefaultForTableStructureUsages() {
        category.setDefaultForTableStructureUsages(true);
        assertTrue(category.isDefaultForTableStructureUsages());
        category.setDefaultForTableStructureUsages(false);
        assertFalse(category.isDefaultForTableStructureUsages());
    }

    @Test
    public void shouldAllowToBeMarkedAsDefaultForValidationRules() {
        category.setDefaultForValidationRules(true);
        assertTrue(category.isDefaultForValidationRules());
        category.setDefaultForValidationRules(false);
        assertFalse(category.isDefaultForValidationRules());
    }

    @Test
    public void shouldAllowToSetSide() {
        category.setSide(Side.LEFT);
        assertEquals(Side.LEFT, category.getSide());
        assertTrue(category.isAtLeftSide());

        category.setSide(Side.RIGHT);
        assertEquals(Side.RIGHT, category.getSide());
        assertTrue(category.isAtRightSide());
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowNullPointerExceptionWhenTryingToSetNullAsSide() {
        category.setSide(null);
    }

    @Test
    public void shouldAllowToRetrieveAllAssignedProductCmptProperties() {
        IProductCmptProperty property2 = type.newProductCmptTypeAttribute();
        category.assignProductCmptProperty(property);
        category.assignProductCmptProperty(property2);

        assertEquals(property, category.getAssignedProductCmptProperties().get(0));
        assertEquals(property2, category.getAssignedProductCmptProperties().get(1));
        assertEquals(2, category.getAssignedProductCmptProperties().size());
    }

    @Test
    public void shouldAllowToRetrieveAllAssignedProductCmptPropertiesIncludingTheSupertypeHierarchy()
            throws CoreException {

        superSuperCategory.assignProductCmptProperty(superSuperProperty);
        superCategory.assignProductCmptProperty(superProperty);
        category.assignProductCmptProperty(property);

        List<IProductCmptProperty> allProperties = category.findAllAssignedProductCmptProperties(ipsProject);
        assertEquals(superSuperProperty, allProperties.get(0));
        assertEquals(superProperty, allProperties.get(1));
        assertEquals(property, allProperties.get(2));
        assertEquals(3, allProperties.size());
    }

    @Test
    public void shouldInformAboutPropertyAssignment() {
        IProductCmptProperty property2 = type.newProductCmptTypeAttribute();
        category.assignProductCmptProperty(property);
        superCategory.assignProductCmptProperty(superProperty);

        assertTrue(category.isAssignedProductCmptProperty(property));
        assertFalse(category.isAssignedProductCmptProperty(property2));
        assertFalse(category.isAssignedProductCmptProperty(superProperty));
    }

    @Test
    public void shouldInformAboutPropertyAssignmentsMadeInSupertypeHierarchy() throws CoreException {
        IProductCmptProperty superSuperPropertyNotAssigned = superSuperType.newProductCmptTypeAttribute();

        superSuperCategory.assignProductCmptProperty(superSuperProperty);
        superCategory.assignProductCmptProperty(superProperty);
        category.assignProductCmptProperty(property);

        assertTrue(category.findIsAssignedProductCmptProperty(superSuperProperty, ipsProject));
        assertTrue(category.findIsAssignedProductCmptProperty(superProperty, ipsProject));
        assertTrue(category.findIsAssignedProductCmptProperty(property, ipsProject));
        assertFalse(category.findIsAssignedProductCmptProperty(superSuperPropertyNotAssigned, ipsProject));
    }

    @Test
    public void shouldCreateXmlElement() {
        ProductCmptCategory categoryImpl = (ProductCmptCategory)category;
        Document document = mock(Document.class);

        categoryImpl.createElement(document);

        verify(document).createElement(IProductCmptCategory.XML_TAG_NAME);
    }

    @Test
    public void shouldBePersistedToXml() throws ParserConfigurationException {
        category.setDefaultForMethods(true);
        category.setDefaultForPolicyCmptTypeAttributes(true);
        category.setDefaultForProductCmptTypeAttributes(true);
        category.setDefaultForTableStructureUsages(true);
        category.setDefaultForValidationRules(true);
        category.setSide(Side.RIGHT);

        Element xmlElement = category.toXml(createXmlDocument(IProductCmptCategory.XML_TAG_NAME));
        IProductCmptCategory loadedCategory = type.newProductCmptCategory();
        loadedCategory.initFromXml(xmlElement);

        assertEquals(CATEGORY_NAME, loadedCategory.getName());
        assertTrue(loadedCategory.isDefaultForMethods());
        assertTrue(loadedCategory.isDefaultForPolicyCmptTypeAttributes());
        assertTrue(loadedCategory.isDefaultForProductCmptTypeAttributes());
        assertTrue(loadedCategory.isDefaultForTableStructureUsages());
        assertTrue(loadedCategory.isDefaultForValidationRules());
        assertEquals(Side.RIGHT, loadedCategory.getSide());
    }

}
