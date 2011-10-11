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
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptCategory;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptCategory.Side;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.core.model.productcmpttype.ITableStructureUsage;
import org.faktorips.devtools.core.model.type.IProductCmptProperty;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ProductCmptCategoryTest extends AbstractIpsPluginTest {

    private static final String CATEGORY_NAME = "foo";

    private IIpsProject ipsProject;

    private IProductCmptType productType;

    private IProductCmptType superProductType;

    private IProductCmptType superSuperProductType;

    private IProductCmptTypeAttribute property;

    private IProductCmptTypeAttribute superProperty;

    private IProductCmptTypeAttribute superSuperProperty;

    private IProductCmptCategory category;

    private IProductCmptCategory superCategory;

    private IProductCmptCategory superSuperCategory;

    @Override
    @Before
    public void setUp() throws CoreException {
        ipsProject = newIpsProject();

        createTypeHierarchy();
    }

    private void createTypeHierarchy() throws CoreException {
        IPolicyCmptType superSuperPolicyType = newPolicyAndProductCmptType(ipsProject, "SuperSuperPolicyType",
                "SuperSuperProductType");
        IPolicyCmptType superPolicyType = newPolicyAndProductCmptType(ipsProject, "SuperPolicyType", "SuperProductType");
        IPolicyCmptType policyType = newPolicyAndProductCmptType(ipsProject, "PolicyType", "ProductType");

        superSuperProductType = superSuperPolicyType.findProductCmptType(ipsProject);
        superProductType = superPolicyType.findProductCmptType(ipsProject);
        productType = policyType.findProductCmptType(ipsProject);

        superPolicyType.setSupertype(superSuperPolicyType.getQualifiedName());
        policyType.setSupertype(superPolicyType.getQualifiedName());
        superProductType.setSupertype(superSuperProductType.getQualifiedName());
        productType.setSupertype(superProductType.getQualifiedName());

        superSuperCategory = superSuperProductType.newProductCmptCategory(CATEGORY_NAME);
        superCategory = superProductType.newProductCmptCategory(CATEGORY_NAME);
        superCategory.setInherited(true);
        category = productType.newProductCmptCategory(CATEGORY_NAME);
        category.setInherited(true);

        superSuperProperty = createProductCmptTypeAttributeProperty(superSuperProductType, "superSuperProperty");
        superProperty = createProductCmptTypeAttributeProperty(superProductType, "superProperty");
        property = createProductCmptTypeAttributeProperty(productType, "property");
    }

    @Test
    public void shouldInitializePropertiesToProperDefaultsOnCreation() {
        IProductCmptCategory category = productType.newProductCmptCategory();
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
        assertEquals(productType, category.getProductCmptType());
    }

    @Test
    public void shouldAllowToSetName() {
        category.setName("bar");
        assertEquals("bar", category.getName());
    }

    @Test
    public void shouldAllowToAssignProductCmptTypeAttribute() {
        IProductCmptTypeAttribute attributeProperty = createProductCmptTypeAttributeProperty(productType,
                "attributeProperty");
        boolean added = category.assignProductCmptProperty(attributeProperty);

        assertTrue(added);
        assertTrue(category.isAssignedProductCmptProperty(attributeProperty));
    }

    @Test
    public void shouldAllowToAssignPolicyCmptTypeAttribute() throws CoreException {
        IPolicyCmptTypeAttribute attributeProperty = createPolicyCmptTypeAttributeProperty(productType,
                "attributeProperty");
        boolean added = category.assignProductCmptProperty(attributeProperty);

        assertTrue(added);
        assertTrue(category.isAssignedProductCmptProperty(attributeProperty));
    }

    @Test
    public void shouldAllowToAssignProductCmptTypeMethod() {
        IProductCmptTypeMethod methodProperty = createProductCmptTypeMethodProperty(productType, "methodProperty");
        boolean added = category.assignProductCmptProperty(methodProperty);

        assertTrue(added);
        assertTrue(category.isAssignedProductCmptProperty(methodProperty));
    }

    @Test
    public void shouldAllowToAssignTableStructureUsage() {
        ITableStructureUsage tableStructureProperty = createTableStructureUsageProperty(productType,
                "tableStructureProperty");
        boolean added = category.assignProductCmptProperty(tableStructureProperty);

        assertTrue(added);
        assertTrue(category.isAssignedProductCmptProperty(tableStructureProperty));
    }

    @Test
    public void shouldAllowToAssignValidationRule() throws CoreException {
        IValidationRule validationRuleProperty = createValidationRuleProperty(productType, "validationRuleProperty");
        boolean added = category.assignProductCmptProperty(validationRuleProperty);

        assertTrue(added);
        assertTrue(category.isAssignedProductCmptProperty(validationRuleProperty));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionWhenAssigningProductCmptTypeAttributeFromForeignProductCmptType() {
        category.assignProductCmptProperty(superProperty);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionWhenAssigningPolicyCmptTypeAttributeFromForeignPolicyCmptType()
            throws CoreException {

        IPolicyCmptType policyCmptType = newPolicyCmptType(ipsProject, "ForeignPolicy");
        IPolicyCmptTypeAttribute attribute = policyCmptType.newPolicyCmptTypeAttribute();
        attribute.setProductRelevant(true);
        category.assignProductCmptProperty(attribute);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionWhenAssigningPolicyCmptTypeAttributeThatIsNotProductRelevant()
            throws CoreException {

        IPolicyCmptTypeAttribute attributeProperty = createPolicyCmptTypeAttributeProperty(productType,
                "attributeProperty");
        attributeProperty.setProductRelevant(false);
        category.assignProductCmptProperty(attributeProperty);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionWhenAssigningProductCmptTypeMethodFromForeignProductCmptType() {
        IProductCmptTypeMethod methodProperty = createProductCmptTypeMethodProperty(superProductType, "methodProperty");
        category.assignProductCmptProperty(methodProperty);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionWhenAssigningProductCmptTypeMethodThatIsNoFormulaSignatureDefinition() {
        IProductCmptTypeMethod methodProperty = createProductCmptTypeMethodProperty(productType, "methodProperty");
        methodProperty.setFormulaSignatureDefinition(false);
        category.assignProductCmptProperty(methodProperty);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionWhenAssigningValidationRuleFromForeignPolicyCmptType()
            throws CoreException {

        IPolicyCmptType policyCmptType = newPolicyCmptType(ipsProject, "ForeignPolicy");
        IValidationRule validationRule = policyCmptType.newRule();
        category.assignProductCmptProperty(validationRule);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionWhenAssigningTableStructureUsageFromForeignProductCmptType() {
        ITableStructureUsage tableStructureProperty = createTableStructureUsageProperty(superProductType,
                "tableStructureProperty");
        category.assignProductCmptProperty(tableStructureProperty);
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
        IProductCmptCategory category = productType.newProductCmptCategory();
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
    public void shouldThrowNullPointerExceptionWhenSettingNullAsSide() {
        category.setSide(null);
    }

    @Test
    public void shouldAllowToRetrieveAllAssignedProductCmptProperties() throws CoreException {
        IProductCmptTypeAttribute property2 = createProductCmptTypeAttributeProperty(productType, "property2");
        category.assignProductCmptProperty(property);
        category.assignProductCmptProperty(property2);

        List<IProductCmptProperty> assignedProperties = category.findAssignedProductCmptProperties(ipsProject);
        assertEquals(property, assignedProperties.get(0));
        assertEquals(property2, assignedProperties.get(1));
        assertEquals(2, assignedProperties.size());
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
    public void shouldNotRetrieveAssignedProductCmptPropertiesFromSupertypeHierarchyIfNotInherited()
            throws CoreException {

        superCategory.assignProductCmptProperty(superProperty);
        category.assignProductCmptProperty(property);

        category.setInherited(false);

        List<IProductCmptProperty> allProperties = category.findAllAssignedProductCmptProperties(ipsProject);
        assertEquals(property, allProperties.get(0));
        assertEquals(1, allProperties.size());
    }

    @Test
    public void shouldInformAboutPropertyAssignment() {
        IProductCmptProperty property2 = productType.newProductCmptTypeAttribute();
        category.assignProductCmptProperty(property);
        superCategory.assignProductCmptProperty(superProperty);

        assertTrue(category.isAssignedProductCmptProperty(property));
        assertFalse(category.isAssignedProductCmptProperty(property2));
        assertFalse(category.isAssignedProductCmptProperty(superProperty));
    }

    @Test
    public void shouldInformAboutPropertyAssignmentsMadeInSupertypeHierarchy() throws CoreException {
        IProductCmptProperty superSuperPropertyNotAssigned = createProductCmptTypeAttributeProperty(
                superSuperProductType, "superSuperPropertyNotAssigned");

        superSuperCategory.assignProductCmptProperty(superSuperProperty);
        superCategory.assignProductCmptProperty(superProperty);
        category.assignProductCmptProperty(property);

        assertTrue(category.findIsAssignedProductCmptProperty(superSuperProperty, ipsProject));
        assertTrue(category.findIsAssignedProductCmptProperty(superProperty, ipsProject));
        assertTrue(category.findIsAssignedProductCmptProperty(property, ipsProject));
        assertFalse(category.findIsAssignedProductCmptProperty(superSuperPropertyNotAssigned, ipsProject));
    }

    @Test
    public void shouldNotInformAboutPropertyAssignmentsMadeInSupertypeHierarchyIfNotInherited() throws CoreException {
        superCategory.assignProductCmptProperty(superProperty);
        category.assignProductCmptProperty(property);
        category.setInherited(false);

        assertFalse(category.findIsAssignedProductCmptProperty(superProperty, ipsProject));
    }

    @Test
    public void shouldCreateXmlElement() {
        ProductCmptCategory categoryImpl = (ProductCmptCategory)category;
        Document document = mock(Document.class);

        categoryImpl.createElement(document);

        verify(document).createElement(IProductCmptCategory.XML_TAG_NAME);
    }

    @Test
    public void shouldBePersistedToXml() throws ParserConfigurationException, CoreException {
        category.setDefaultForMethods(true);
        category.setDefaultForPolicyCmptTypeAttributes(true);
        category.setDefaultForProductCmptTypeAttributes(true);
        category.setDefaultForTableStructureUsages(true);
        category.setDefaultForValidationRules(true);
        category.setSide(Side.RIGHT);

        IProductCmptTypeAttribute productAttribute = createProductCmptTypeAttributeProperty(productType,
                "productAttribute");
        IPolicyCmptTypeAttribute policyAttribute = createPolicyCmptTypeAttributeProperty(productType, "policyAttribute");
        IProductCmptTypeMethod formula = createProductCmptTypeMethodProperty(productType, "formula");
        IValidationRule validationRule = createValidationRuleProperty(productType, "validationRule");
        ITableStructureUsage structureUsage = createTableStructureUsageProperty(productType, "tableStructureUsaage");
        category.assignProductCmptProperty(productAttribute);
        category.assignProductCmptProperty(policyAttribute);
        category.assignProductCmptProperty(formula);
        category.assignProductCmptProperty(validationRule);
        category.assignProductCmptProperty(structureUsage);

        Element xmlElement = category.toXml(createXmlDocument(IProductCmptCategory.XML_TAG_NAME));
        IProductCmptCategory loadedCategory = productType.newProductCmptCategory();
        loadedCategory.initFromXml(xmlElement);

        assertEquals(CATEGORY_NAME, loadedCategory.getName());
        assertTrue(loadedCategory.isDefaultForMethods());
        assertTrue(loadedCategory.isDefaultForPolicyCmptTypeAttributes());
        assertTrue(loadedCategory.isDefaultForProductCmptTypeAttributes());
        assertTrue(loadedCategory.isDefaultForTableStructureUsages());
        assertTrue(loadedCategory.isDefaultForValidationRules());
        assertEquals(Side.RIGHT, loadedCategory.getSide());

        List<IProductCmptProperty> assignedProperties = loadedCategory.findAssignedProductCmptProperties(ipsProject);
        assertEquals(productAttribute, assignedProperties.get(0));
        assertEquals(policyAttribute, assignedProperties.get(1));
        assertEquals(formula, assignedProperties.get(2));
        assertEquals(validationRule, assignedProperties.get(3));
        assertEquals(structureUsage, assignedProperties.get(4));
        assertEquals(5, assignedProperties.size());
    }

    private IProductCmptTypeAttribute createProductCmptTypeAttributeProperty(IProductCmptType type, String name) {
        return type.newProductCmptTypeAttribute(name);
    }

    private IProductCmptTypeMethod createProductCmptTypeMethodProperty(IProductCmptType type, String name) {
        IProductCmptTypeMethod method = type.newProductCmptTypeMethod();
        method.setName(name);
        method.setFormulaName(name);
        method.setFormulaSignatureDefinition(true);
        return method;
    }

    private ITableStructureUsage createTableStructureUsageProperty(IProductCmptType type, String name) {
        ITableStructureUsage usage = type.newTableStructureUsage();
        usage.setRoleName(name);
        return usage;
    }

    private IValidationRule createValidationRuleProperty(IProductCmptType type, String name) throws CoreException {
        IPolicyCmptType policyCmptType = type.findPolicyCmptType(type.getIpsProject());
        IValidationRule rule = policyCmptType.newRule();
        rule.setName(name);
        return rule;
    }

    private IPolicyCmptTypeAttribute createPolicyCmptTypeAttributeProperty(IProductCmptType type, String name)
            throws CoreException {

        IPolicyCmptType policyCmptType = type.findPolicyCmptType(type.getIpsProject());
        IPolicyCmptTypeAttribute attribute = policyCmptType.newPolicyCmptTypeAttribute();
        attribute.setName(name);
        attribute.setProductRelevant(true);
        return attribute;
    }

}
