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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.List;

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
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptCategory.Position;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptPropertyReference;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.core.model.productcmpttype.ITableStructureUsage;
import org.faktorips.devtools.core.model.type.IProductCmptProperty;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ProductCmptCategoryTest extends AbstractIpsPluginTest implements ContentsChangeListener {

    private static final String CATEGORY_NAME = "foo";

    private ContentChangeEvent lastEvent;

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
    public void setUp() throws Exception {
        super.setUp();

        lastEvent = null;
        ipsProject = newIpsProject();
        ipsProject.getIpsModel().addChangeListener(this);

        createTypeHierarchy();
    }

    @Override
    protected void tearDownExtension() throws Exception {
        ipsProject.getIpsModel().removeChangeListener(this);
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
        assertTrue(category.isAtLeftPosition());
    }

    @Test
    public void shouldReturnParentProductCmptType() {
        assertEquals(productType, category.getProductCmptType());
    }

    @Test
    public void shouldAllowToSetName() {
        category.setName("bar");

        assertEquals("bar", category.getName());
        assertPropertyChangedEvent();
    }

    @Test
    public void shouldAllowToReferenceProductCmptTypeAttribute() {
        IProductCmptTypeAttribute attributeProperty = createProductCmptTypeAttributeProperty(productType,
                "attributeProperty");

        assertNotNull(category.newProductCmptPropertyReference(attributeProperty));
        assertTrue(category.isReferencedProductCmptProperty(attributeProperty));
    }

    @Test
    public void shouldAllowToReferencePolicyCmptTypeAttribute() throws CoreException {
        IPolicyCmptTypeAttribute attributeProperty = createPolicyCmptTypeAttributeProperty(productType,
                "attributeProperty");

        assertNotNull(category.newProductCmptPropertyReference(attributeProperty));
        assertTrue(category.isReferencedProductCmptProperty(attributeProperty));
    }

    @Test
    public void shouldAllowToReferenceProductCmptTypeMethod() {
        IProductCmptTypeMethod methodProperty = createProductCmptTypeMethodProperty(productType, "methodProperty");

        assertNotNull(category.newProductCmptPropertyReference(methodProperty));
        assertTrue(category.isReferencedProductCmptProperty(methodProperty));
    }

    @Test
    public void shouldAllowToReferenceTableStructureUsage() {
        ITableStructureUsage tableStructureProperty = createTableStructureUsageProperty(productType,
                "tableStructureProperty");

        assertNotNull(category.newProductCmptPropertyReference(tableStructureProperty));
        assertTrue(category.isReferencedProductCmptProperty(tableStructureProperty));
    }

    @Test
    public void shouldAllowToReferenceValidationRule() throws CoreException {
        IValidationRule validationRuleProperty = createValidationRuleProperty(productType, "validationRuleProperty");

        assertNotNull(category.newProductCmptPropertyReference(validationRuleProperty));
        assertTrue(category.isReferencedProductCmptProperty(validationRuleProperty));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionWhenReferencingProductCmptTypeAttributeFromForeignProductCmptType() {
        category.newProductCmptPropertyReference(superProperty);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionWhenReferencingPolicyCmptTypeAttributeFromForeignPolicyCmptType()
            throws CoreException {

        IPolicyCmptType policyCmptType = newPolicyCmptType(ipsProject, "ForeignPolicy");
        IPolicyCmptTypeAttribute attribute = policyCmptType.newPolicyCmptTypeAttribute();
        attribute.setProductRelevant(true);
        category.newProductCmptPropertyReference(attribute);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionWhenReferencingPolicyCmptTypeAttributeThatIsNotProductRelevant()
            throws CoreException {

        IPolicyCmptTypeAttribute attributeProperty = createPolicyCmptTypeAttributeProperty(productType,
                "attributeProperty");
        attributeProperty.setProductRelevant(false);
        category.newProductCmptPropertyReference(attributeProperty);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionWhenReferencingProductCmptTypeMethodFromForeignProductCmptType() {
        IProductCmptTypeMethod methodProperty = createProductCmptTypeMethodProperty(superProductType, "methodProperty");
        category.newProductCmptPropertyReference(methodProperty);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionWhenReferencingProductCmptTypeMethodThatIsNoFormulaSignatureDefinition() {
        IProductCmptTypeMethod methodProperty = createProductCmptTypeMethodProperty(productType, "methodProperty");
        methodProperty.setFormulaSignatureDefinition(false);
        category.newProductCmptPropertyReference(methodProperty);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionWhenReferencingValidationRuleFromForeignPolicyCmptType()
            throws CoreException {

        IPolicyCmptType policyCmptType = newPolicyCmptType(ipsProject, "ForeignPolicy");
        IValidationRule validationRule = policyCmptType.newRule();
        category.newProductCmptPropertyReference(validationRule);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionWhenReferencingTableStructureUsageFromForeignProductCmptType() {
        ITableStructureUsage tableStructureProperty = createTableStructureUsageProperty(superProductType,
                "tableStructureProperty");
        category.newProductCmptPropertyReference(tableStructureProperty);
    }

    @Test
    public void shouldAllowToDeleteProductCmptPropertyReference() {
        category.newProductCmptPropertyReference(property);
        boolean removed = category.deleteProductCmptPropertyReference(property);

        assertTrue(removed);
        assertFalse(category.isReferencedProductCmptProperty(property));
    }

    @Test
    public void shouldOnlyDeleteOneReferencePerDeleteCall() {
        category.newProductCmptPropertyReference(property);
        category.newProductCmptPropertyReference(property);

        category.deleteProductCmptPropertyReference(property);

        assertTrue(category.isReferencedProductCmptProperty(property));
        assertEquals(1, category.getNumberOfProductCmptPropertyReferences());
    }

    @Test
    public void shouldReturnFalseWhenDeletingNotReferencedProductCmptProperty() {
        boolean removed = category.deleteProductCmptPropertyReference(property);
        assertFalse(removed);
    }

    @Test
    public void shouldReturnTheCorrectNumberOfProductCmptPropertyReferences() {
        category.newProductCmptPropertyReference(property);
        category.newProductCmptPropertyReference(property);
        category.newProductCmptPropertyReference(property);

        assertEquals(3, category.getNumberOfProductCmptPropertyReferences());
    }

    @Test
    public void shouldAllowToSetInheritedProperty() {
        category.setInherited(false);
        assertFalse(category.isInherited());
        assertPropertyChangedEvent();

        resetContentChangedEvent();

        category.setInherited(true);
        assertTrue(category.isInherited());
        assertPropertyChangedEvent();
    }

    @Test
    public void shouldAllowToBeMarkedAsDefaultForMethods() {
        category.setDefaultForMethods(true);
        assertTrue(category.isDefaultForMethods());
        assertPropertyChangedEvent();

        resetContentChangedEvent();

        category.setDefaultForMethods(false);
        assertFalse(category.isDefaultForMethods());
        assertPropertyChangedEvent();
    }

    @Test
    public void shouldAllowToBeMarkedAsDefaultForPolicyCmptTypeAttributes() {
        category.setDefaultForPolicyCmptTypeAttributes(true);
        assertTrue(category.isDefaultForPolicyCmptTypeAttributes());
        assertPropertyChangedEvent();

        resetContentChangedEvent();

        category.setDefaultForPolicyCmptTypeAttributes(false);
        assertFalse(category.isDefaultForPolicyCmptTypeAttributes());
        assertPropertyChangedEvent();
    }

    @Test
    public void shouldAllowToBeMarkedAsDefaultForProductCmptTypeAttributes() {
        category.setDefaultForProductCmptTypeAttributes(true);
        assertTrue(category.isDefaultForProductCmptTypeAttributes());
        assertPropertyChangedEvent();

        resetContentChangedEvent();

        category.setDefaultForProductCmptTypeAttributes(false);
        assertFalse(category.isDefaultForProductCmptTypeAttributes());
        assertPropertyChangedEvent();
    }

    @Test
    public void shouldAllowToBeMarkedAsDefaultForTableStructureUsages() {
        category.setDefaultForTableStructureUsages(true);
        assertTrue(category.isDefaultForTableStructureUsages());
        assertPropertyChangedEvent();

        resetContentChangedEvent();

        category.setDefaultForTableStructureUsages(false);
        assertFalse(category.isDefaultForTableStructureUsages());
        assertPropertyChangedEvent();
    }

    @Test
    public void shouldAllowToBeMarkedAsDefaultForValidationRules() {
        category.setDefaultForValidationRules(true);
        assertTrue(category.isDefaultForValidationRules());
        assertPropertyChangedEvent();

        resetContentChangedEvent();

        category.setDefaultForValidationRules(false);
        assertFalse(category.isDefaultForValidationRules());
        assertPropertyChangedEvent();
    }

    @Test
    public void shouldAllowToSetPosition() {
        category.setPosition(Position.RIGHT);
        assertEquals(Position.RIGHT, category.getPosition());
        assertTrue(category.isAtRightPosition());
        assertPropertyChangedEvent();

        resetContentChangedEvent();

        category.setPosition(Position.LEFT);
        assertEquals(Position.LEFT, category.getPosition());
        assertTrue(category.isAtLeftPosition());
        assertPropertyChangedEvent();
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowNullPointerExceptionWhenSettingNullAsPosition() {
        category.setPosition(null);
    }

    @Test
    public void shouldAllowToRetrieveAllReferencedProductCmptProperties() throws CoreException {
        IProductCmptTypeAttribute property2 = createProductCmptTypeAttributeProperty(productType, "property2");
        category.newProductCmptPropertyReference(property);
        category.newProductCmptPropertyReference(property2);

        List<IProductCmptProperty> assignedProperties = category.findReferencedProductCmptProperties(ipsProject);
        assertEquals(property, assignedProperties.get(0));
        assertEquals(property2, assignedProperties.get(1));
        assertEquals(2, assignedProperties.size());
    }

    @Test
    public void shouldAllowToRetrieveAllReferencedProductCmptPropertiesIncludingTheSupertypeHierarchy()
            throws CoreException {

        superSuperCategory.newProductCmptPropertyReference(superSuperProperty);
        superCategory.newProductCmptPropertyReference(superProperty);
        category.newProductCmptPropertyReference(property);

        List<IProductCmptProperty> allProperties = category.findAllReferencedProductCmptProperties(ipsProject);
        assertEquals(superSuperProperty, allProperties.get(0));
        assertEquals(superProperty, allProperties.get(1));
        assertEquals(property, allProperties.get(2));
        assertEquals(3, allProperties.size());
    }

    @Test
    public void shouldNotRetrieveReferencedProductCmptPropertiesFromSupertypeHierarchyIfNotInherited()
            throws CoreException {

        superCategory.newProductCmptPropertyReference(superProperty);
        category.newProductCmptPropertyReference(property);

        category.setInherited(false);

        List<IProductCmptProperty> allProperties = category.findAllReferencedProductCmptProperties(ipsProject);
        assertEquals(property, allProperties.get(0));
        assertEquals(1, allProperties.size());
    }

    @Test
    public void shouldInformAboutPropertyReference() {
        IProductCmptProperty property2 = productType.newProductCmptTypeAttribute();
        category.newProductCmptPropertyReference(property);
        superCategory.newProductCmptPropertyReference(superProperty);

        assertTrue(category.isReferencedProductCmptProperty(property));
        assertFalse(category.isReferencedProductCmptProperty(property2));
        assertFalse(category.isReferencedProductCmptProperty(superProperty));
    }

    @Test
    public void shouldInformAboutPropertyReferencesMadeInSupertypeHierarchy() throws CoreException {
        IProductCmptProperty superSuperPropertyNotAssigned = createProductCmptTypeAttributeProperty(
                superSuperProductType, "superSuperPropertyNotAssigned");

        superSuperCategory.newProductCmptPropertyReference(superSuperProperty);
        superCategory.newProductCmptPropertyReference(superProperty);
        category.newProductCmptPropertyReference(property);

        assertTrue(category.findIsReferencedProductCmptProperty(superSuperProperty, ipsProject));
        assertTrue(category.findIsReferencedProductCmptProperty(superProperty, ipsProject));
        assertTrue(category.findIsReferencedProductCmptProperty(property, ipsProject));
        assertFalse(category.findIsReferencedProductCmptProperty(superSuperPropertyNotAssigned, ipsProject));
    }

    @Test
    public void shouldNotInformAboutPropertyReferencesMadeInSupertypeHierarchyIfNotInherited() throws CoreException {
        superCategory.newProductCmptPropertyReference(superProperty);
        category.newProductCmptPropertyReference(property);
        category.setInherited(false);

        assertFalse(category.findIsReferencedProductCmptProperty(superProperty, ipsProject));
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
        category.setPosition(Position.RIGHT);

        IProductCmptTypeAttribute productAttribute = createProductCmptTypeAttributeProperty(productType,
                "productAttribute");
        IPolicyCmptTypeAttribute policyAttribute = createPolicyCmptTypeAttributeProperty(productType, "policyAttribute");
        IProductCmptTypeMethod formula = createProductCmptTypeMethodProperty(productType, "formula");
        IValidationRule validationRule = createValidationRuleProperty(productType, "validationRule");
        ITableStructureUsage structureUsage = createTableStructureUsageProperty(productType, "tableStructureUsaage");
        category.newProductCmptPropertyReference(productAttribute);
        category.newProductCmptPropertyReference(policyAttribute);
        category.newProductCmptPropertyReference(formula);
        category.newProductCmptPropertyReference(validationRule);
        category.newProductCmptPropertyReference(structureUsage);

        Element xmlElement = category.toXml(createXmlDocument(IProductCmptCategory.XML_TAG_NAME));
        IProductCmptCategory loadedCategory = productType.newProductCmptCategory();
        loadedCategory.initFromXml(xmlElement);

        assertEquals(CATEGORY_NAME, loadedCategory.getName());
        assertTrue(loadedCategory.isDefaultForMethods());
        assertTrue(loadedCategory.isDefaultForPolicyCmptTypeAttributes());
        assertTrue(loadedCategory.isDefaultForProductCmptTypeAttributes());
        assertTrue(loadedCategory.isDefaultForTableStructureUsages());
        assertTrue(loadedCategory.isDefaultForValidationRules());
        assertEquals(Position.RIGHT, loadedCategory.getPosition());

        List<IProductCmptProperty> assignedProperties = loadedCategory.findReferencedProductCmptProperties(ipsProject);
        assertEquals(productAttribute, assignedProperties.get(0));
        assertEquals(policyAttribute, assignedProperties.get(1));
        assertEquals(formula, assignedProperties.get(2));
        assertEquals(validationRule, assignedProperties.get(3));
        assertEquals(structureUsage, assignedProperties.get(4));
        assertEquals(5, assignedProperties.size());
    }

    @Test
    public void shouldAllowToMoveReferences() throws CoreException {
        IProductCmptTypeAttribute property1 = createProductCmptTypeAttributeProperty(productType, "property1");
        IProductCmptTypeAttribute property2 = createProductCmptTypeAttributeProperty(productType, "property2");
        IProductCmptTypeAttribute property3 = createProductCmptTypeAttributeProperty(productType, "property3");

        IProductCmptPropertyReference reference1 = category.newProductCmptPropertyReference(property1);
        IProductCmptPropertyReference reference2 = category.newProductCmptPropertyReference(property2);
        IProductCmptPropertyReference reference3 = category.newProductCmptPropertyReference(property3);

        category.moveProductCmptPropertyReferences(new int[] { 1, 2 }, true);
        List<IProductCmptProperty> references = category.findReferencedProductCmptProperties(ipsProject);
        assertEquals(property2, references.get(0));
        assertEquals(property3, references.get(1));
        assertEquals(property1, references.get(2));

        assertTrue(lastEvent.isAffected(reference1));
        assertTrue(lastEvent.isAffected(reference2));
        assertTrue(lastEvent.isAffected(reference3));
    }

    @Override
    public void contentsChanged(ContentChangeEvent event) {
        lastEvent = event;
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

    private void resetContentChangedEvent() {
        lastEvent = null;
    }

    private void assertPropertyChangedEvent() {
        assertEquals(category, lastEvent.getPart());
        assertEquals(ContentChangeEvent.TYPE_PROPERTY_CHANGED, lastEvent.getEventType());
    }

}
