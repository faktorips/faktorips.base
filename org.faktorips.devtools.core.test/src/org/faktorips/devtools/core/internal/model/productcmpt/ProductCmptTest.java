/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.productcmpt;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.matchers.JUnitMatchers.hasItem;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptTypeAttribute;
import org.faktorips.devtools.core.internal.model.pctype.ValidationRule;
import org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptType;
import org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptTypeAttribute;
import org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptTypeMethod;
import org.faktorips.devtools.core.internal.model.productcmpttype.TableStructureUsage;
import org.faktorips.devtools.core.model.IDependency;
import org.faktorips.devtools.core.model.IDependencyDetail;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectGeneration;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.core.model.pctype.AttributeType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpt.DeltaType;
import org.faktorips.devtools.core.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.core.model.productcmpt.IConfigElement;
import org.faktorips.devtools.core.model.productcmpt.IDeltaEntry;
import org.faktorips.devtools.core.model.productcmpt.IDeltaEntryForProperty;
import org.faktorips.devtools.core.model.productcmpt.IFormula;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptKind;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptNamingStrategy;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValueContainerToTypeDelta;
import org.faktorips.devtools.core.model.productcmpt.ITableContentUsage;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptCategory;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.core.model.productcmpttype.ITableStructureUsage;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.model.type.ProductCmptPropertyType;
import org.faktorips.util.message.MessageList;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;

public class ProductCmptTest extends AbstractIpsPluginTest {

    private ProductCmpt productCmpt;
    private IIpsPackageFragmentRoot root;
    private IIpsPackageFragment pack;
    private IIpsSrcFile srcFile;
    private IIpsProject ipsProject;
    private IPolicyCmptType policyCmptType;
    private IProductCmptTypeAttribute attr1;
    private IProductCmptTypeAttribute attr2;
    private IProductCmptType productCmptType;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject(new ArrayList<Locale>());
        root = ipsProject.getIpsPackageFragmentRoots()[0];
        pack = root.createPackageFragment("products.folder", true, null);
        srcFile = pack.createIpsFile(IpsObjectType.PRODUCT_CMPT, "TestProduct", true, null);
        productCmpt = (ProductCmpt)srcFile.getIpsObject();

        productCmptType = newProductCmptType(ipsProject, "ProdType");
        policyCmptType = newPolicyCmptType(ipsProject, "PolType");
        policyCmptType.setProductCmptType("ProdType");
        attr1 = new ProductCmptTypeAttribute(productCmptType, "IDAttr1");
        attr1.setName("TypeAttr1");
        attr2 = new ProductCmptTypeAttribute(productCmptType, "IDAttr2");
        attr2.setName("TypeAttr2");
    }

    @Test
    public void testGetChildrenThis_generationsAreAllowed() {
        productCmpt.setProductCmptType(productCmptType.getQualifiedName());

        IPropertyValue property1 = productCmpt.newPropertyValue(attr1);
        IPropertyValue property2 = productCmpt.newPropertyValue(attr2);

        IProductCmptTypeAssociation association = productCmptType.newProductCmptTypeAssociation();
        association.setTarget(productCmptType.getQualifiedName());
        association.setTargetRoleSingular("association");

        IProductCmptLink link = productCmpt.newLink("association");
        link.setTarget(productCmpt.getQualifiedName());

        IIpsObjectGeneration generation = productCmpt.newGeneration();

        // Verify
        List<IIpsElement> children = Arrays.asList(productCmpt.getChildrenThis());
        assertTrue(children.contains(property1));
        assertTrue(children.contains(property2));
        assertTrue(children.contains(link));
        assertTrue(children.contains(generation));
    }

    @Test
    public void testGetChildrenThis_generationsAreNotAllowed() {
        productCmptType.setChangingOverTime(false);
        productCmpt.setProductCmptType(productCmptType.getQualifiedName());

        IIpsObjectGeneration generation = productCmpt.newGeneration();

        List<IIpsElement> children = Arrays.asList(productCmpt.getChildrenThis());
        assertFalse(children.contains(generation));
    }

    @Test
    public void testDependsOn() throws Exception {
        IProductCmptTypeAssociation association = productCmptType.newProductCmptTypeAssociation();
        association.setChangingOverTime(false);
        association.setTargetRoleSingular("testAsso");
        association.setTarget(productCmptType.getQualifiedName());
        ProductCmpt targetProductCmpt = newProductCmpt(productCmptType, "referenced");
        IProductCmptLink link = productCmpt.newLink(association);
        link.setTarget(targetProductCmpt.getQualifiedName());

        IDependency[] dependsOn = productCmpt.dependsOn();

        assertEquals(1, dependsOn.length);
        assertEquals(targetProductCmpt.getQualifiedNameType(), dependsOn[0].getTarget());
    }

    @Test
    public void testValidate_ProductCmptTypeIsMissing() throws Exception {
        IProductCmptType type = newProductCmptType(ipsProject, "Product");
        productCmpt.setProductCmptType(type.getQualifiedName());

        MessageList list = productCmpt.validate(ipsProject);
        assertNull(list.getMessageByCode(IProductCmpt.MSGCODE_MISSINGG_PRODUCT_CMPT_TYPE));

        productCmpt.setProductCmptType("UnknownType");
        list = productCmpt.validate(ipsProject);
        assertNotNull(list.getMessageByCode(IProductCmpt.MSGCODE_MISSINGG_PRODUCT_CMPT_TYPE));

        productCmpt.setProductCmptType("");
        list = productCmpt.validate(ipsProject);
        assertNotNull(list.getMessageByCode(IProductCmpt.MSGCODE_MISSINGG_PRODUCT_CMPT_TYPE));

        // this has once been a bug (NPE in validation of the generation!)
        IFormula ce = ((IProductCmptGeneration)productCmpt.newGeneration()).newFormula();
        ce.setFormulaSignature("SomeFormula");
        ce.setExpression("42");
        list = productCmpt.validate(ipsProject);
        assertNotNull(list.getMessageByCode(IProductCmpt.MSGCODE_MISSINGG_PRODUCT_CMPT_TYPE));
    }

    @Test
    public void testValidate_ProductCmptTypeIsNotAbstract() throws Exception {
        IProductCmptType type = newProductCmptType(ipsProject, "Product");
        productCmpt.setProductCmptType(type.getQualifiedName());

        MessageList list = productCmpt.validate(ipsProject);
        assertNull(list.getMessageByCode(IProductCmpt.MSGCODE_ABSTRACT_PRODUCT_CMPT_TYPE));

        type.setAbstract(true);
        list = productCmpt.validate(ipsProject);
        assertNotNull(list.getMessageByCode(IProductCmpt.MSGCODE_ABSTRACT_PRODUCT_CMPT_TYPE));
    }

    @Test
    public void testValidate_ProductTemplate_TypeMayBeAbstract() throws Exception {
        IProductCmptType type = newProductCmptType(ipsProject, "Product");
        ProductCmpt productTemplate = newProductTemplate(ipsProject, "MyTemplate");
        productTemplate.setProductCmptType(type.getQualifiedName());

        MessageList list = productTemplate.validate(ipsProject);
        assertNull(list.getMessageByCode(IProductCmpt.MSGCODE_ABSTRACT_PRODUCT_CMPT_TYPE));

        type.setAbstract(true);
        list = productTemplate.validate(ipsProject);
        assertNull(list.getMessageByCode(IProductCmpt.MSGCODE_ABSTRACT_PRODUCT_CMPT_TYPE));
    }

    @Test
    public void testValidate_InconsitencyInTypeHierarch() throws Exception {
        IProductCmptType type = newProductCmptType(ipsProject, "Product");
        ProductCmpt product = newProductCmpt(type, "products.Testproduct");

        MessageList ml = product.validate(type.getIpsProject());
        assertNull(ml.getMessageByCode(IProductCmpt.MSGCODE_INCONSISTENT_TYPE_HIERARCHY));

        IProductCmptType supertype = newProductCmptType(ipsProject, "SuperProduct");
        IProductCmptType supersupertype = newProductCmptType(ipsProject, "SuperSuperProduct");

        type.setSupertype(supertype.getQualifiedName());
        supertype.setSupertype(supersupertype.getQualifiedName());
        supersupertype.setSupertype("abc");

        ml = type.validate(type.getIpsProject());
        assertNotNull(ml.getMessageByCode(IType.MSGCODE_INCONSISTENT_TYPE_HIERARCHY));

        ml = product.validate(type.getIpsProject());
        assertNotNull(ml.getMessageByCode(IProductCmpt.MSGCODE_INCONSISTENT_TYPE_HIERARCHY));

        supersupertype.setSupertype("");
        ml = type.validate(type.getIpsProject());
        assertNull(ml.getMessageByCode(IType.MSGCODE_INCONSISTENT_TYPE_HIERARCHY));
        ml = product.validate(type.getIpsProject());
        assertNull(ml.getMessageByCode(IProductCmpt.MSGCODE_INCONSISTENT_TYPE_HIERARCHY));

        supersupertype.setSupertype(type.getQualifiedName());
        ml = type.validate(type.getIpsProject());
        assertNotNull(ml.getMessageByCode(IType.MSGCODE_CYCLE_IN_TYPE_HIERARCHY));
        ml = product.validate(type.getIpsProject());
        assertNotNull(ml.getMessageByCode(IProductCmpt.MSGCODE_INCONSISTENT_TYPE_HIERARCHY));

        type.setSupertype("Unkown");
        ml = type.validate(type.getIpsProject());
        assertNotNull(ml.getMessageByCode(IType.MSGCODE_SUPERTYPE_NOT_FOUND));
        ml = product.validate(type.getIpsProject());
        assertNotNull(ml.getMessageByCode(IProductCmpt.MSGCODE_INCONSISTENT_TYPE_HIERARCHY));
    }

    @Test
    public void testValidate_NameDoesNotComplyToNamingStrategy() throws CoreException {
        IProductCmptType type = newProductCmptType(ipsProject, "ProductType");
        ProductCmpt product = newProductCmpt(type, "Product");
        IIpsProjectProperties projectProperties = ipsProject.getProperties();
        projectProperties.setProductCmptNamingStrategy(new DateBasedProductCmptNamingStrategy());
        ipsProject.setProperties(projectProperties);

        MessageList validationMessages = product.validate(ipsProject);
        assertNotNull(validationMessages.getMessageByCode(IProductCmptNamingStrategy.MSGCODE_ILLEGAL_VERSION_ID));
    }

    @Test
    public void testValidate_RuntimeIdDoesNotComplyToNamingStrategy() throws CoreException {
        IProductCmptType type = newProductCmptType(ipsProject, "ProductType");
        ProductCmpt product = newProductCmpt(type, "Product");
        product.setRuntimeId("");

        MessageList validationMessages = product.validate(ipsProject);
        assertNotNull(validationMessages.getMessageByCode(IProductCmptNamingStrategy.MSGCODE_INVALID_RUNTIME_ID_FORMAT));
    }

    @Test
    public void testValidate_DuplicateRuntimeIds() throws CoreException {
        IProductCmptType type = newProductCmptType(ipsProject, "ProductType");
        ProductCmpt product1 = newProductCmpt(type, "Product1");
        ProductCmpt product2 = newProductCmpt(type, "Product2");
        product1.setRuntimeId("Product");
        product2.setRuntimeId("Product");

        MessageList validationMessages = product1.validate(ipsProject);
        assertNotNull(validationMessages.getMessageByCode(IIpsProject.MSGCODE_RUNTIME_ID_COLLISION));
    }

    @Test
    public void testValidate_DuplicateRuntimeIdIgnoresTemplate() throws CoreException {
        IProductCmptType type = newProductCmptType(ipsProject, "ProductType");
        ProductCmpt product = newProductCmpt(type, "Product1");
        ProductCmpt template = newProductTemplate(type, "Product2");
        product.setRuntimeId("Product");
        template.setRuntimeId("Product");

        MessageList validationMessages = product.validate(ipsProject);
        MessageList validationMessages2 = template.validate(ipsProject);

        assertNull(validationMessages.getMessageByCode(IIpsProject.MSGCODE_RUNTIME_ID_COLLISION));
        assertNull(validationMessages2.getMessageByCode(IIpsProject.MSGCODE_RUNTIME_ID_COLLISION));

    }

    @Test
    public void testValidate_PropertyNotConfigured() throws CoreException {
        IProductCmptType type = newProductCmptType(ipsProject, "Product");
        IProductCmptTypeAttribute attribute = type.newProductCmptTypeAttribute("attribtue");
        attribute.setChangingOverTime(true);
        ProductCmpt product = newProductCmpt(type, "products.Testproduct");

        MessageList ml = product.validate(type.getIpsProject());
        assertNull(ml.getMessageByCode(IProductCmpt.MSGCODE_PROPERTY_NOT_CONFIGURED));

        attribute.setChangingOverTime(false);
        ml = product.validate(type.getIpsProject());
        assertNotNull(ml.getMessageByCode(IProductCmpt.MSGCODE_PROPERTY_NOT_CONFIGURED));
    }

    @Test
    public void testValidate_InvalidGenerations() throws CoreException {
        IProductCmptType type = newProductCmptType(ipsProject, "Product");
        type.setChangingOverTime(true);
        ProductCmpt product = newProductCmpt(type, "products.Testproduct");
        product.newGeneration(new GregorianCalendar(2015, 0, 1));
        product.newGeneration(new GregorianCalendar(2015, 1, 1));
        product.newGeneration(new GregorianCalendar(2016, 7, 28));

        MessageList ml = product.validate(type.getIpsProject());
        assertNull(ml.getMessageByCode(IProductCmpt.MSGCODE_INVALID_GENERATIONS));

        type.setChangingOverTime(false);
        ml = product.validate(type.getIpsProject());
        assertNotNull(ml.getMessageByCode(IProductCmpt.MSGCODE_INVALID_GENERATIONS));
    }

    @Test
    // Suppressed "unused" warning for improved readability
    @SuppressWarnings("unused")
    public void testFindPropertyValues() throws CoreException {
        IProductCmpt productCmpt = newProductCmpt(productCmptType, "MyProduct");

        // Create some properties on the product component
        IProductCmptTypeAttribute productAttribute1 = productCmptType.newProductCmptTypeAttribute("productAttribute1");
        IProductCmptTypeAttribute productAttribute2 = productCmptType.newProductCmptTypeAttribute("productAttribute2");
        IProductCmptTypeAttribute productAttribute3 = productCmptType.newProductCmptTypeAttribute("productAttribute3");

        // Create some properties on the generation
        IProductCmptTypeAttribute genAttribute1 = productCmptType.newProductCmptTypeAttribute("g1");
        IProductCmptTypeAttribute genAttribute2 = productCmptType.newProductCmptTypeAttribute("g2");

        // Create a category and assign some properties
        IProductCmptCategory category = productCmptType.newCategory("myCategory");
        productAttribute1.setCategory(category.getName());
        productAttribute3.setCategory(category.getName());
        genAttribute2.setCategory(category.getName());

        // Create two generations
        GregorianCalendar validFrom1 = createValidFromDate(1);
        GregorianCalendar validFrom2 = createValidFromDate(2);
        IProductCmptGeneration generation1 = (IProductCmptGeneration)productCmpt.newGeneration(validFrom1);
        IProductCmptGeneration generation2 = (IProductCmptGeneration)productCmpt.newGeneration(validFrom2);

        // Create the corresponding property values
        IPropertyValue productValue1 = productCmpt.newPropertyValue(productAttribute1);
        IPropertyValue productValue2 = productCmpt.newPropertyValue(productAttribute2);
        IPropertyValue productValue3 = productCmpt.newPropertyValue(productAttribute3);
        IPropertyValue gen1Value1 = generation1.newAttributeValue(genAttribute1);
        IPropertyValue gen1Value2 = generation1.newAttributeValue(genAttribute2);
        IPropertyValue gen2Value1 = generation2.newAttributeValue(genAttribute1);
        IPropertyValue gen2Value2 = generation2.newAttributeValue(genAttribute2);

        // Check for generation 1
        List<IPropertyValue> propertyValuesGen1 = productCmpt.findPropertyValues(category, validFrom1, ipsProject);
        assertEquals(productValue1, propertyValuesGen1.get(0));
        assertEquals(productValue3, propertyValuesGen1.get(1));
        assertEquals(gen1Value2, propertyValuesGen1.get(2));
        assertEquals(3, propertyValuesGen1.size());

        // Check for generation 2
        List<IPropertyValue> propertyValuesGen2 = productCmpt.findPropertyValues(category, validFrom2, ipsProject);
        assertEquals(productValue1, propertyValuesGen2.get(0));
        assertEquals(productValue3, propertyValuesGen2.get(1));
        assertEquals(gen2Value2, propertyValuesGen2.get(2));
        assertEquals(3, propertyValuesGen2.size());
    }

    /**
     * <strong>Scenario:</strong><br>
     * The {@link IPropertyValue property values} are requested but the {@link IProductCmptType} the
     * {@link IProductCmpt} is an instance of cannot be found.
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * An empty list should be returned but no exception may be thrown.
     */
    @Test
    public void testFindPropertyValues_ProductCmptTypeCannotBeFound() throws CoreException {
        GregorianCalendar validFrom = new GregorianCalendar(2011, 12, 12);
        productCmpt.newGeneration(validFrom);

        assertTrue(productCmpt.findPropertyValues(null, validFrom, ipsProject).isEmpty());
    }

    /**
     * 
     * <strong>Scenario:</strong><br>
     * The {@link IPropertyValue property values} are requested but no
     * {@link IProductCmptGeneration} exists for the indicated effective date.
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * Only the {@link IPropertyValue property values} belonging to the {@link IProductCmpt} itself
     * should be returned.
     */
    @Test
    public void testFindPropertyValues_NoGenerationWithTheIndicatedEffectiveDate_ForProductAttribute()
            throws CoreException {
        IProductCmptCategory category = productCmptType.newCategory("myCategory");
        IProductCmptTypeAttribute productAttribute = productCmptType.newProductCmptTypeAttribute("productAttribute");
        productAttribute.setCategory(category.getName());

        IProductCmpt productCmpt = newProductCmpt(productCmptType, "MyProduct");
        IPropertyValue productValue = productCmpt.newPropertyValue(productAttribute);

        List<IPropertyValue> propertyValues = productCmpt.findPropertyValues(category,
                new GregorianCalendar(2070, 1, 1), ipsProject);
        assertEquals(productValue, propertyValues.get(0));
        assertEquals(1, propertyValues.size());
    }

    /**
     * 
     * <strong>Scenario:</strong><br>
     * The {@link IPropertyValue property values} are requested but no
     * {@link IProductCmptGeneration} exists for the indicated effective date.
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * Only the {@link IPropertyValue property values} belonging to the {@link IProductCmpt} itself
     * should be returned.
     */
    @Test
    public void testFindPropertyValues_NoGenerationWithTheIndicatedEffectiveDateForPolicyAttribute()
            throws CoreException {
        IProductCmptCategory category = productCmptType.newCategory("myCategory");
        IPolicyCmptTypeAttribute policyAttribute = policyCmptType.newPolicyCmptTypeAttribute("policyAttribute");
        policyAttribute.setProductRelevant(true);
        policyAttribute.setAttributeType(AttributeType.CHANGEABLE);
        policyAttribute.setCategory(category.getName());
        productCmptType.setPolicyCmptType("PolType");

        IProductCmpt productCmpt = newProductCmpt(productCmptType, "MyProduct");
        IPropertyValue productValue = productCmpt.newPropertyValue(policyAttribute);

        List<IPropertyValue> propertyValues = productCmpt.findPropertyValues(category,
                new GregorianCalendar(2070, 1, 1), ipsProject);
        assertEquals(productValue, propertyValues.get(0));
        assertEquals(1, propertyValues.size());
    }

    /**
     * <strong>Scenario:</strong><br>
     * The {@link IPropertyValue property values} are requested but no specific
     * {@link IProductCmptCategory} is given.
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * The {@link IPropertyValue property values} for all {@link IProductCmptCategory categories}
     * should be returned.
     */
    @Test
    public void testFindPropertyValues_NoCategoryGiven() throws CoreException {
        IProductCmptCategory category1 = productCmptType.newCategory("category1");
        IProductCmptCategory category2 = productCmptType.newCategory("category2");

        IProductCmptTypeAttribute productAttribute1 = productCmptType.newProductCmptTypeAttribute("productAttribute1");
        productAttribute1.setCategory(category1.getName());
        IProductCmptTypeAttribute productAttribute2 = productCmptType.newProductCmptTypeAttribute("productAttribute2");
        productAttribute2.setCategory(category2.getName());

        IProductCmpt productCmpt = newProductCmpt(productCmptType, "MyProduct");
        GregorianCalendar validFrom = createValidFromDate(1);
        IProductCmptGeneration generation = (IProductCmptGeneration)productCmpt.newGeneration(validFrom);
        IPropertyValue productValue1 = generation.newPropertyValue(productAttribute1);
        IPropertyValue productValue2 = generation.newPropertyValue(productAttribute2);

        List<IPropertyValue> propertyValues = productCmpt.findPropertyValues(null, validFrom, ipsProject);
        assertEquals(productValue1, propertyValues.get(0));
        assertEquals(productValue2, propertyValues.get(1));
        assertEquals(2, propertyValues.size());
    }

    @Test
    public void testGetKindId() throws CoreException {
        IProductCmptKind kind = productCmpt.getKindId();
        assertEquals("TestProduct", kind.getName());
        assertEquals("TestProduct", kind.getRuntimeId());

        IProductCmptNamingStrategy strategy = new DateBasedProductCmptNamingStrategy(" ", "yyyy-MM", false);
        IIpsProjectProperties props = ipsProject.getProperties();
        props.setProductCmptNamingStrategy(strategy);
        ipsProject.setProperties(props);
        productCmpt = newProductCmpt(ipsProject, "motor.MotorProduct 2005-10");
        kind = productCmpt.getKindId();
        assertEquals("MotorProduct", kind.getName());
        assertEquals("MotorProduct", kind.getRuntimeId());
    }

    @Test
    public void testGetKindIdWithIllegalName() throws CoreException {
        IProductCmptNamingStrategy strategy = new DateBasedProductCmptNamingStrategy(" ", "yyyy-MM", false);
        IIpsProjectProperties props = ipsProject.getProperties();
        props.setProductCmptNamingStrategy(strategy);
        ipsProject.setProperties(props);

        productCmpt = newProductCmpt(ipsProject, "motor.MotorProduct 2011-11");
        productCmpt.setName("motor.MotorProduct");
        assertNull(productCmpt.getKindId());
    }

    @Test
    public void testSetProductCmptType() {
        productCmpt.setProductCmptType("newType");
        assertEquals("newType", productCmpt.getProductCmptType());
        assertTrue(srcFile.isDirty());
    }

    @Test
    public void testInitFromXml() {
        productCmpt.initFromXml(getTestDocument().getDocumentElement());
        assertEquals("MotorProduct", productCmpt.getProductCmptType());
        assertEquals("MotorProductId", productCmpt.getRuntimeId());
        assertEquals("MyLittleTemplate", productCmpt.getTemplate());
        assertEquals(2, productCmpt.getNumOfGenerations());
        IProductCmptGeneration gen = (IProductCmptGeneration)productCmpt.getGenerationsOrderedByValidDate()[0];
        assertEquals(1, gen.getNumOfConfigElements());
        IConfigElement ce = gen.getConfigElements()[0];
        assertEquals("1.5", ce.getValue());

        assertEquals(2, productCmpt.getNumOfLinks());
        assertEquals("staticCoverage", productCmpt.getLinksAsList().get(0).getAssociation());
        assertEquals("staticIDontKnow", productCmpt.getLinksAsList().get(1).getAssociation());
    }

    @Test
    public void testToXml() throws CoreException {
        productCmpt.setProductCmptType("MotorProduct");
        productCmpt.setRuntimeId("MotorProductId");
        productCmpt.setTemplate("MeinTemplate");
        IProductCmptGeneration gen1 = (IProductCmptGeneration)productCmpt.newGeneration();
        IConfigElement ce1 = gen1.newConfigElement();
        ce1.setValue("0.15");
        productCmpt.newGeneration();

        Element element = productCmpt.toXml(newDocument());
        ProductCmpt copy = newProductCmpt(ipsProject, "TestProductCopy");
        copy.initFromXml(element);
        assertEquals("MotorProduct", copy.getProductCmptType());
        assertEquals("MotorProductId", copy.getRuntimeId());
        assertEquals("MeinTemplate", productCmpt.getTemplate());
        assertEquals(2, copy.getNumOfGenerations());
        IProductCmptGeneration genCopy = (IProductCmptGeneration)copy.getGenerationsOrderedByValidDate()[0];
        assertEquals(1, genCopy.getConfigElements().length);
        assertEquals("0.15", genCopy.getConfigElements()[0].getValue());
    }

    @Test
    public void testInitFromXml_AttributeValues() {
        productCmpt.initFromXml(getTestDocument().getDocumentElement());
        IAttributeValue attributeValue = productCmpt.getAttributeValue("bezeichnung");
        assertNotNull(attributeValue);
        assertEquals("testtesttest", attributeValue.getPropertyValue());
    }

    @Test
    public void testToXml_AttributeValues() throws CoreException {
        attr2.setChangingOverTime(false);
        IPropertyValue propertyValue = productCmpt.newPropertyValue(attr2);
        Element xml = productCmpt.toXml(newDocument());

        ProductCmpt copy = newProductCmpt(ipsProject, "TestProductCopy");
        copy.initFromXml(xml);
        IAttributeValue copyAttributeValue = copy.getAttributeValue(attr2.getName());
        assertNotNull(copyAttributeValue);
        assertEquals(propertyValue.getName(), copyAttributeValue.getName());
    }

    @Test
    public void testInitFromXml_TableContentUsage() {
        productCmpt.initFromXml(getTestDocument().getDocumentElement());
        assertEquals(1, productCmpt.getTableContentUsages().length);
        assertNotNull(productCmpt.getTableContentUsage("staticTable"));
    }

    @Test
    public void testToXml_TableContentUsage() throws CoreException {
        productCmpt.newPropertyValue(new TableStructureUsage(mock(IProductCmptType.class), "tc"));
        Element xml = productCmpt.toXml(newDocument());

        ProductCmpt copy = newProductCmpt(ipsProject, "TestProductCopy");
        copy.initFromXml(xml);
        assertEquals(1, productCmpt.getTableContentUsages().length);
    }

    @Test
    public void testInitFromXml_Formula() {
        productCmpt.initFromXml(getTestDocument().getDocumentElement());
        assertEquals(1, productCmpt.getFormulas().length);
        assertNotNull(productCmpt.getFormula("PremiumCalculation"));
    }

    @Test
    public void testToXml_Formula() throws CoreException {
        IFormula newFormula = (IFormula)productCmpt.newPropertyValue(new ProductCmptTypeMethod(
                mock(IProductCmptType.class), "Id"));
        newFormula.setExpression("anyExpression");
        Element xml = productCmpt.toXml(newDocument());

        ProductCmpt copy = newProductCmpt(ipsProject, "TestProductCopy");
        copy.initFromXml(xml);
        assertEquals(1, productCmpt.getFormulas().length);
    }

    @Test
    public void testToXml_Links() throws CoreException {
        attr2.setChangingOverTime(false);
        IProductCmptLink newLink = productCmpt.newLink("newLink");
        newLink.setTarget("target");
        Element xml = productCmpt.toXml(newDocument());

        ProductCmpt copy = newProductCmpt(ipsProject, "TestProductCopy");
        copy.initFromXml(xml);
        List<IProductCmptLink> linksCopy = copy.getLinksAsList("newLink");
        assertNotNull(linksCopy);
        assertEquals(1, linksCopy.size());
        assertEquals(newLink.getTarget(), linksCopy.get(0).getTarget());
    }

    @Test
    public void testContainsGenerationFormula() {
        assertFalse(productCmpt.containsGenerationFormula());
        IProductCmptGeneration gen1 = (IProductCmptGeneration)productCmpt.newGeneration(new GregorianCalendar(2000, 1,
                1));
        IProductCmptGeneration gen2 = (IProductCmptGeneration)productCmpt.newGeneration(new GregorianCalendar(2010, 1,
                1));
        gen1.newFormula();
        assertTrue(productCmpt.containsGenerationFormula());

        for (IFormula formula : gen1.getFormulas()) {
            formula.delete();
        }
        assertFalse(productCmpt.containsGenerationFormula());

        gen2.newFormula();
        assertTrue(productCmpt.containsGenerationFormula());
    }

    @Test
    public void testContainsDifferenceToModel() throws CoreException {
        PolicyCmptType testType = newPolicyAndProductCmptType(ipsProject, "TestPolicyType", "TestProductType");
        IPolicyCmptTypeAttribute a1 = testType.newPolicyCmptTypeAttribute();
        a1.setName("A1");
        a1.setProductRelevant(true);

        IProductCmptType productCmptType = testType.findProductCmptType(ipsProject);
        IProductCmpt product = newProductCmpt(productCmptType, "TestProduct");
        IProductCmptGeneration gen = product.getProductCmptGeneration(0);
        IConfigElement ce1 = gen.newConfigElement();
        ce1.setPolicyCmptTypeAttribute("A1");

        IPolicyCmptTypeAttribute a2 = testType.newPolicyCmptTypeAttribute();
        a2.setName("A2");
        a2.setProductRelevant(true);

        IProductCmpt product2 = newProductCmpt(productCmptType, "TestProduct2");
        gen = product2.getProductCmptGeneration(0);
        ce1 = gen.newConfigElement();
        ce1.setPolicyCmptTypeAttribute("A1");
        IConfigElement ce2 = gen.newConfigElement();
        ce2.setPolicyCmptTypeAttribute("A2");

        assertEquals(true, product.containsDifferenceToModel(ipsProject));
        assertEquals(false, product2.containsDifferenceToModel(ipsProject));
        testType.getPolicyCmptTypeAttribute("A2").delete();
        assertEquals(false, product.containsDifferenceToModel(ipsProject));
        assertEquals(true, product2.containsDifferenceToModel(ipsProject));
    }

    @Test
    public void testContainsDifferencesToModel_productCmptTypeAttribute() throws Exception {
        ProductCmptType newProductCmptType = newProductCmptType(ipsProject, "TestProductCmptType");
        IProductCmptTypeAttribute newAttribute = newProductCmptType.newProductCmptTypeAttribute("testAttr");
        newAttribute.setChangingOverTime(false);

        ProductCmpt newProductCmpt = newProductCmpt(newProductCmptType, "Cmpt1");
        assertTrue(newProductCmpt.containsDifferenceToModel(ipsProject));

        IPropertyValue wrongAttributeValue = newProductCmpt.getProductCmptGeneration(0).newPropertyValue(newAttribute);
        assertTrue(newProductCmpt.containsDifferenceToModel(ipsProject));

        IPropertyValue correctAttributeValue = newProductCmpt.newPropertyValue(newAttribute);
        assertTrue(newProductCmpt.containsDifferenceToModel(ipsProject));

        wrongAttributeValue.delete();
        assertFalse(newProductCmpt.containsDifferenceToModel(ipsProject));

        newAttribute.setChangingOverTime(true);
        assertTrue(newProductCmpt.containsDifferenceToModel(ipsProject));

        newAttribute.setChangingOverTime(false);
        assertFalse(newProductCmpt.containsDifferenceToModel(ipsProject));

        newAttribute.delete();
        assertTrue(newProductCmpt.containsDifferenceToModel(ipsProject));

        correctAttributeValue.delete();
        assertFalse(newProductCmpt.containsDifferenceToModel(ipsProject));
    }

    @Test
    public void testFixDifferencesToModel_productCmptTypeAttribute() throws Exception {
        ProductCmptType newProductCmptType = newProductCmptType(ipsProject, "TestProductCmptType");
        IProductCmptTypeAttribute newAttribute = newProductCmptType.newProductCmptTypeAttribute("testAttr");
        newAttribute.setChangingOverTime(false);

        ProductCmpt newProductCmpt = newProductCmpt(newProductCmptType, "Cmpt1");

        assertTrue(newProductCmpt.containsDifferenceToModel(ipsProject));
        IAttributeValue attributeValue = newProductCmpt.getAttributeValue(newAttribute.getName());
        assertNull(attributeValue);

        newProductCmpt.fixAllDifferencesToModel(ipsProject);
        assertFalse(newProductCmpt.containsDifferenceToModel(ipsProject));
        attributeValue = newProductCmpt.getAttributeValue(newAttribute.getName());
        assertNotNull(attributeValue);

        newAttribute.setChangingOverTime(true);
        newProductCmpt.fixAllDifferencesToModel(ipsProject);
        assertTrue(attributeValue.isDeleted());
        IAttributeValue attributeValue1 = newProductCmpt.getAttributeValue(newAttribute.getName());
        assertNull(attributeValue1);

        IAttributeValue attributeValue2 = newProductCmpt.getFirstGeneration().getAttributeValue(newAttribute.getName());
        assertNotNull(attributeValue2);

        assertNotSame(attributeValue.getId().intern(), attributeValue2.getId().intern());

        newAttribute.setChangingOverTime(false);
        newProductCmpt.fixAllDifferencesToModel(ipsProject);
        IAttributeValue attributeValue3 = newProductCmpt.getAttributeValue(newAttribute.getName());
        assertNotNull(attributeValue3);

        assertNotSame(attributeValue.getId().intern(), attributeValue2.getId().intern());
        assertNotSame(attributeValue.getId().intern(), attributeValue3.getId().intern());

        newAttribute.delete();
        newProductCmpt.fixAllDifferencesToModel(ipsProject);
        assertTrue(attributeValue3.isDeleted());
        IAttributeValue attributeValue4 = newProductCmpt.getAttributeValue(newAttribute.getName());
        assertNull(attributeValue4);

        assertNotSame(attributeValue.getId().intern(), attributeValue2.getId().intern());
        assertNotSame(attributeValue.getId().intern(), attributeValue3.getId().intern());
    }

    @Test
    public void testComputeDeltaToModel_AttributeValues() throws Exception {
        ProductCmptType newProductCmptType = newProductCmptType(ipsProject, "TestProductCmptType");
        IProductCmptTypeAttribute newAttribute = newProductCmptType.newProductCmptTypeAttribute("testAttr");
        newAttribute.setChangingOverTime(false);

        ProductCmpt newProductCmpt = newProductCmpt(newProductCmptType, "Cmpt1");

        IPropertyValueContainerToTypeDelta computeDeltaToModel = newProductCmpt.computeDeltaToModel(ipsProject);

        assertEquals(newProductCmpt, computeDeltaToModel.getPropertyValueContainer());
        IDeltaEntry[] entries = computeDeltaToModel.getEntries();
        assertEquals(1, entries.length);
        assertEquals(DeltaType.MISSING_PROPERTY_VALUE, entries[0].getDeltaType());
        assertEquals(newAttribute.getName(), ((IDeltaEntryForProperty)entries[0]).getPropertyName());

        newAttribute.setChangingOverTime(true);
        computeDeltaToModel = newProductCmpt.computeDeltaToModel(ipsProject);
        entries = computeDeltaToModel.getEntries();
        assertEquals(0, entries.length);

        newAttribute.setChangingOverTime(false);
        newProductCmpt.fixAllDifferencesToModel(ipsProject);

        newAttribute.setChangingOverTime(true);
        computeDeltaToModel = newProductCmpt.computeDeltaToModel(ipsProject);
        entries = computeDeltaToModel.getEntries();
        assertEquals(1, entries.length);
        assertEquals(DeltaType.VALUE_WITHOUT_PROPERTY, entries[0].getDeltaType());
        assertEquals(newAttribute.getName(), ((IDeltaEntryForProperty)entries[0]).getPropertyName());
    }

    @Test
    public void testContainsDifferencesToModel_productCmptTypeTableContent() throws Exception {
        ProductCmptType newProductCmptType = newProductCmptType(ipsProject, "TestProductCmptType");
        ITableStructureUsage tableStructureUsage = newProductCmptType.newTableStructureUsage();
        tableStructureUsage.setRoleName("TSU");
        tableStructureUsage.setChangingOverTime(false);

        ProductCmpt newProductCmpt = newProductCmpt(newProductCmptType, "Cmpt1");
        assertTrue(newProductCmpt.containsDifferenceToModel(ipsProject));

        newProductCmpt.newPropertyValue(tableStructureUsage);
        assertFalse(newProductCmpt.containsDifferenceToModel(ipsProject));
    }

    @Test
    public void testFixDifferencesToModel_productCmptTypeTableContent() throws Exception {
        ProductCmptType newProductCmptType = newProductCmptType(ipsProject, "TestProductCmptType");
        ITableStructureUsage tableStructureUsage = newProductCmptType.newTableStructureUsage();
        tableStructureUsage.setRoleName("TSU");
        tableStructureUsage.setChangingOverTime(false);

        ProductCmpt newProductCmpt = newProductCmpt(newProductCmptType, "Cmpt1");

        assertTrue(newProductCmpt.containsDifferenceToModel(ipsProject));

        newProductCmpt.fixAllDifferencesToModel(ipsProject);
        assertFalse(newProductCmpt.containsDifferenceToModel(ipsProject));
    }

    @Test
    public void testComputeDeltaToModel_productCmptTypeTableContent() throws Exception {
        ProductCmptType newProductCmptType = newProductCmptType(ipsProject, "TestProductCmptType");
        ITableStructureUsage tableStructureUsage = newProductCmptType.newTableStructureUsage();
        tableStructureUsage.setRoleName("TSU");
        tableStructureUsage.setChangingOverTime(false);

        ProductCmpt newProductCmpt = newProductCmpt(newProductCmptType, "Cmpt1");

        IPropertyValueContainerToTypeDelta computeDeltaToModel = newProductCmpt.computeDeltaToModel(ipsProject);

        assertEquals(newProductCmpt, computeDeltaToModel.getPropertyValueContainer());
        IDeltaEntry[] entries = computeDeltaToModel.getEntries();
        assertEquals(1, entries.length);
        assertEquals(DeltaType.MISSING_PROPERTY_VALUE, entries[0].getDeltaType());
        assertEquals(tableStructureUsage.getRoleName(), ((IDeltaEntryForProperty)entries[0]).getPropertyName());

        tableStructureUsage.setChangingOverTime(true);
        computeDeltaToModel = newProductCmpt.computeDeltaToModel(ipsProject);
        entries = computeDeltaToModel.getEntries();
        assertEquals(0, entries.length);

        tableStructureUsage.setChangingOverTime(false);
        newProductCmpt.fixAllDifferencesToModel(ipsProject);

        tableStructureUsage.setChangingOverTime(true);
        computeDeltaToModel = newProductCmpt.computeDeltaToModel(ipsProject);
        entries = computeDeltaToModel.getEntries();
        assertEquals(1, entries.length);
        assertEquals(DeltaType.VALUE_WITHOUT_PROPERTY, entries[0].getDeltaType());
        assertEquals(tableStructureUsage.getRoleName(), ((IDeltaEntryForProperty)entries[0]).getPropertyName());
    }

    /**
     * Test method for
     * {@link org.faktorips.devtools.core.model.ipsobject.IFixDifferencesToModelSupport#fixAllDifferencesToModel(IIpsProject)}
     * .
     */
    @Test
    public void testFixAllDifferencesToModel() throws CoreException {
        IPolicyCmptType testType = newPolicyAndProductCmptType(ipsProject, "TestPolicyType", "TestProductType");
        IProductCmptType productCmptType = testType.findProductCmptType(ipsProject);
        IPolicyCmptTypeAttribute a1 = testType.newPolicyCmptTypeAttribute();
        a1.setName("A1");
        a1.setProductRelevant(true);

        IProductCmpt product = newProductCmpt(productCmptType, "TestProduct");
        IProductCmptGeneration gen = product.getProductCmptGeneration(0);
        IConfigElement ce1 = gen.newConfigElement();
        ce1.setPolicyCmptTypeAttribute("A1");

        IPolicyCmptTypeAttribute a2 = testType.newPolicyCmptTypeAttribute();
        a2.setName("A2");
        a2.setProductRelevant(true);

        IProductCmpt product2 = newProductCmpt(productCmptType, "TestProduct2");
        gen = product2.getProductCmptGeneration(0);
        ce1 = gen.newConfigElement();
        ce1.setPolicyCmptTypeAttribute("A1");
        IConfigElement ce2 = gen.newConfigElement();
        ce2.setPolicyCmptTypeAttribute("A2");

        assertEquals(true, product.containsDifferenceToModel(ipsProject));
        product.fixAllDifferencesToModel(ipsProject);
        assertEquals(false, product.containsDifferenceToModel(ipsProject));

        assertEquals(false, product2.containsDifferenceToModel(ipsProject));
        product2.fixAllDifferencesToModel(ipsProject);
        assertEquals(false, product2.containsDifferenceToModel(ipsProject));
    }

    @Test
    public void testSetValidTo() {
        productCmpt.setValidTo(new GregorianCalendar(2000, 1, 1));
        assertEquals(new GregorianCalendar(2000, 1, 1), productCmpt.getValidTo());
    }

    @Test
    public void testFindMetaClass() throws CoreException {
        IProductCmptType type = newProductCmptType(ipsProject, "Product");
        productCmpt.setProductCmptType(type.getQualifiedName());

        IIpsSrcFile typeSrcFile = productCmpt.findMetaClassSrcFile(ipsProject);
        assertEquals(type.getIpsSrcFile(), typeSrcFile);
    }

    @Test
    public void testNewPropertyValue() throws Exception {
        assertEquals(0,
                productCmpt.getPropertyValues(ProductCmptPropertyType.PRODUCT_CMPT_TYPE_ATTRIBUTE.getValueClass())
                        .size());
        productCmpt.newPropertyValue(attr1);
        assertEquals(1,
                productCmpt.getPropertyValues(ProductCmptPropertyType.PRODUCT_CMPT_TYPE_ATTRIBUTE.getValueClass())
                        .size());
        productCmpt.newPropertyValue(attr2);
        assertEquals(2,
                productCmpt.getPropertyValues(ProductCmptPropertyType.PRODUCT_CMPT_TYPE_ATTRIBUTE.getValueClass())
                        .size());

        productCmpt.newPropertyValue(new ValidationRule(mock(IPolicyCmptType.class), ""));
        assertEquals(2,
                productCmpt.getPropertyValues(ProductCmptPropertyType.PRODUCT_CMPT_TYPE_ATTRIBUTE.getValueClass())
                        .size());
        productCmpt.newPropertyValue(new PolicyCmptTypeAttribute(policyCmptType, "pcTypeAttribute"));
        assertEquals(2,
                productCmpt.getPropertyValues(ProductCmptPropertyType.PRODUCT_CMPT_TYPE_ATTRIBUTE.getValueClass())
                        .size());
        productCmpt.newPropertyValue(new TableStructureUsage(mock(IProductCmptType.class), ""));
        assertEquals(1, productCmpt.getPropertyValues(ProductCmptPropertyType.TABLE_STRUCTURE_USAGE.getValueClass())
                .size());
        assertEquals(2,
                productCmpt.getPropertyValues(ProductCmptPropertyType.PRODUCT_CMPT_TYPE_ATTRIBUTE.getValueClass())
                        .size());
        productCmpt.newPropertyValue(new ProductCmptTypeMethod(productCmptType, "BaseMethod"));
        assertEquals(2,
                productCmpt.getPropertyValues(ProductCmptPropertyType.PRODUCT_CMPT_TYPE_ATTRIBUTE.getValueClass())
                        .size());
        assertEquals(1,
                productCmpt.getPropertyValues(ProductCmptPropertyType.FORMULA_SIGNATURE_DEFINITION.getValueClass())
                        .size());
    }

    @Test
    public void testGetAttributeValue() {
        productCmpt.newPropertyValue(attr1);
        assertNotNull(productCmpt.getAttributeValue("TypeAttr1"));
        assertNull(productCmpt.getAttributeValue("NonExistentAttr"));
    }

    @Test
    public void testHasPropertyValue() {
        assertFalse(productCmpt.hasPropertyValue(attr1));

        productCmpt.newPropertyValue(attr1);
        assertTrue(productCmpt.hasPropertyValue(attr1));
    }

    @Test
    public void testNewPartThis() {
        Element element = mock(Element.class);
        when(element.getNodeName()).thenReturn(IProductCmptGeneration.TAG_NAME);
        IIpsObjectPart part = productCmpt.newPartThis(element, "genID");
        assertNotNull(part);

        when(element.getNodeName()).thenReturn(AttributeValue.TAG_NAME);
        part = productCmpt.newPartThis(element, "attrID");
        assertNotNull(part);

        when(element.getNodeName()).thenReturn(ValidationRule.TAG_NAME);
        part = productCmpt.newPartThis(element, "vRuleID");
        assertNull(part);

        when(element.getNodeName()).thenReturn(TableContentUsage.TAG_NAME);
        part = productCmpt.newPartThis(element, "TCUID");
        assertNotNull(part);

    }

    @Test
    public void testGetLatestProductCmptGeneration() {
        GregorianCalendar today = new GregorianCalendar();
        today = new GregorianCalendar(today.get(Calendar.YEAR), today.get(Calendar.MONTH) + 1,
                today.get(Calendar.DAY_OF_MONTH));
        GregorianCalendar tomorrow = (GregorianCalendar)today.clone();
        tomorrow.add(Calendar.DAY_OF_MONTH, 1);
        GregorianCalendar yesterday = (GregorianCalendar)today.clone();
        yesterday.add(Calendar.DAY_OF_MONTH, -1);

        IIpsObjectGeneration firstGeneration = productCmpt.newGeneration(yesterday);
        IIpsObjectGeneration secondGeneration = productCmpt.newGeneration(tomorrow);
        IIpsObjectGeneration thirdGeneration = productCmpt.newGeneration(today);

        assertSame(firstGeneration, productCmpt.getProductCmptGeneration(0));
        assertSame(secondGeneration, productCmpt.getProductCmptGeneration(1));
        assertSame(thirdGeneration, productCmpt.getProductCmptGeneration(2));
        assertSame(firstGeneration, productCmpt.getFirstGeneration());
        assertSame(secondGeneration, productCmpt.getLatestProductCmptGeneration());
    }

    private GregorianCalendar createValidFromDate(int offsetToCurrentDay) {
        int currentYear = GregorianCalendar.getInstance().get(Calendar.YEAR);
        int currentMonth = GregorianCalendar.getInstance().get(Calendar.MONTH);
        int currentDay = GregorianCalendar.getInstance().get(Calendar.DAY_OF_MONTH);
        return new GregorianCalendar(currentYear, currentMonth, currentDay + offsetToCurrentDay);
    }

    @Test
    public void testIsContainerForChangingAssociation() {
        IProductCmptTypeAssociation changingAssoc = productCmptType.newProductCmptTypeAssociation();
        changingAssoc.setChangingOverTime(true);

        assertFalse(productCmpt.isContainerFor(changingAssoc));
    }

    @Test
    public void testIsContainerForStaticAssociation() {
        IProductCmptTypeAssociation staticAssoc = productCmptType.newProductCmptTypeAssociation();
        staticAssoc.setChangingOverTime(false);

        assertTrue(productCmpt.isContainerFor(staticAssoc));
    }

    @Test
    public void testGetLinksIncludingGenerations() throws Exception {
        IProductCmptGeneration generation1 = (IProductCmptGeneration)productCmpt.newGeneration(new GregorianCalendar(
                2010, 0, 1));
        IProductCmptGeneration generation2 = (IProductCmptGeneration)productCmpt.newGeneration(new GregorianCalendar(
                2011, 0, 1));
        ArrayList<IProductCmptLink> links = new ArrayList<IProductCmptLink>();
        links.add(productCmpt.newLink("asdff"));
        links.add(productCmpt.newLink("asdff2"));
        links.add(generation1.newLink("asd1"));
        links.add(generation1.newLink("asd2"));
        links.add(generation2.newLink("asd3"));
        links.add(generation2.newLink("asd4"));

        List<IProductCmptLink> linksIncludingGenerations = productCmpt.getLinksIncludingGenerations();
        assertEquals(links, linksIncludingGenerations);
    }

    @Test
    public void testGetTableContentUsages() throws Exception {
        ITableContentUsage contentUsagePC = (ITableContentUsage)productCmpt.newPropertyValue(new TableStructureUsage(
                mock(IProductCmptType.class), ""));
        assertNotNull(contentUsagePC);

        assertEquals(1, productCmpt.getTableContentUsages().length);
        assertEquals(contentUsagePC, productCmpt.getTableContentUsages()[0]);
    }

    @Test
    public void testGetFormulas() {
        IFormula formula = (IFormula)productCmpt.newPropertyValue(new ProductCmptTypeMethod(
                mock(IProductCmptType.class), "Id"));
        assertNotNull(formula);

        assertEquals(1, productCmpt.getFormulas().length);
        assertEquals(formula, productCmpt.getFormulas()[0]);
    }

    @Test
    public void testAddDependenciesFromFormulaExpressions() throws Exception {
        ProductCmpt productCmptSpy = spy(productCmpt);
        IDependency dependency = mock(IDependency.class);
        ExpressionDependencyDetail dependencyDetail1 = mock(ExpressionDependencyDetail.class);
        ExpressionDependencyDetail dependencyDetail2 = mock(ExpressionDependencyDetail.class);
        IFormula formula1 = mock(IFormula.class);
        IFormula formula2 = mock(IFormula.class);
        when(productCmptSpy.getFormulas()).thenReturn(new IFormula[] { formula1, formula2 });
        Map<IDependency, ExpressionDependencyDetail> dependencyMap1 = new HashMap<IDependency, ExpressionDependencyDetail>();
        dependencyMap1.put(dependency, dependencyDetail1);
        Map<IDependency, ExpressionDependencyDetail> dependencyMap2 = new HashMap<IDependency, ExpressionDependencyDetail>();
        dependencyMap2.put(dependency, dependencyDetail2);
        when(formula1.dependsOn()).thenReturn(dependencyMap1);
        when(formula2.dependsOn()).thenReturn(dependencyMap2);

        Map<IDependency, List<IDependencyDetail>> detailsResult = new HashMap<IDependency, List<IDependencyDetail>>();
        productCmptSpy.dependsOn(detailsResult);

        assertEquals(1, detailsResult.size());
        assertThat(detailsResult.keySet(), hasItem(dependency));
        List<? extends IDependencyDetail> detailList = detailsResult.get(dependency);
        assertEquals(2, detailList.size());
        assertEquals(dependencyDetail1, detailList.get(0));
        assertEquals(dependencyDetail2, detailList.get(1));
    }

    @Test
    public void testContainsDifferencesToModel_productCmptTypeFormula() throws Exception {
        ProductCmptType newProductCmptType = newProductCmptType(ipsProject, "TestProductCmptType");
        IProductCmptTypeMethod newFormulaSignature = newProductCmptType.newFormulaSignature("newFormula");
        newFormulaSignature.setChangingOverTime(false);

        ProductCmpt newProductCmpt = newProductCmpt(newProductCmptType, "Cmpt1");
        assertTrue(newProductCmpt.containsDifferenceToModel(ipsProject));

        newProductCmpt.newPropertyValue(newFormulaSignature);
        assertFalse(newProductCmpt.containsDifferenceToModel(ipsProject));
    }

    @Test
    public void testFixDifferencesToModel_productCmptTypeFormula() throws Exception {
        ProductCmptType newProductCmptType = newProductCmptType(ipsProject, "TestProductCmptType");
        IProductCmptTypeMethod newFormulaSignature = newProductCmptType.newFormulaSignature("newFormula");
        newFormulaSignature.setChangingOverTime(false);

        ProductCmpt newProductCmpt = newProductCmpt(newProductCmptType, "Cmpt1");

        assertTrue(newProductCmpt.containsDifferenceToModel(ipsProject));

        newProductCmpt.fixAllDifferencesToModel(ipsProject);
        assertFalse(newProductCmpt.containsDifferenceToModel(ipsProject));
    }

    @Test
    public void testComputeDeltaToModel_productCmptTypeFormula() throws Exception {
        ProductCmptType newProductCmptType = newProductCmptType(ipsProject, "TestProductCmptType");
        IProductCmptTypeMethod newFormulaSignature = newProductCmptType.newFormulaSignature("newFormula");
        newFormulaSignature.setChangingOverTime(false);

        ProductCmpt newProductCmpt = newProductCmpt(newProductCmptType, "Cmpt1");

        IPropertyValueContainerToTypeDelta computeDeltaToModel = newProductCmpt.computeDeltaToModel(ipsProject);

        assertEquals(newProductCmpt, computeDeltaToModel.getPropertyValueContainer());
        IDeltaEntry[] entries = computeDeltaToModel.getEntries();
        assertEquals(1, entries.length);
        assertEquals(DeltaType.MISSING_PROPERTY_VALUE, entries[0].getDeltaType());
        assertEquals(newFormulaSignature.getFormulaName(), ((IDeltaEntryForProperty)entries[0]).getPropertyName());

        newFormulaSignature.setChangingOverTime(true);
        computeDeltaToModel = newProductCmpt.computeDeltaToModel(ipsProject);
        entries = computeDeltaToModel.getEntries();
        assertEquals(0, entries.length);

        newFormulaSignature.setChangingOverTime(false);
        newProductCmpt.fixAllDifferencesToModel(ipsProject);

        newFormulaSignature.setChangingOverTime(true);
        computeDeltaToModel = newProductCmpt.computeDeltaToModel(ipsProject);
        entries = computeDeltaToModel.getEntries();
        assertEquals(1, entries.length);
        assertEquals(DeltaType.VALUE_WITHOUT_PROPERTY, entries[0].getDeltaType());
        assertEquals(newFormulaSignature.getFormulaName(), ((IDeltaEntryForProperty)entries[0]).getPropertyName());
    }

    @Test
    public void testNewFormula() throws CoreException {
        ProductCmptType newProductCmptType = newProductCmptType(ipsProject, "TestProductCmptType");
        IProductCmptTypeMethod formulaSignature = newProductCmptType.newFormulaSignature("newFormula");

        ProductCmpt productCmpt = newProductCmpt(newProductCmptType, "Cmpt1");
        IFormula formula = (IFormula)productCmpt.newPropertyValue(formulaSignature);

        assertNotNull(formula);
        assertEquals(formulaSignature.getFormulaName(), formula.getFormulaSignature());
    }

    @Test
    public void testNewFormula2() throws CoreException {
        ProductCmptType newProductCmptType = newProductCmptType(ipsProject, "TestProductCmptType");
        ProductCmpt productCmpt = newProductCmpt(newProductCmptType, "Cmpt1");
        IFormula formula = productCmpt.newPart(IFormula.class);

        assertNotNull(formula);
        assertEquals("", formula.getFormulaSignature());
    }

    @Test
    public void testAllowGenerations_changingOverTimeEnabled() throws CoreException {
        ProductCmptType newProductCmptType = newProductCmptType(ipsProject, "TestProductCmptType");
        newProductCmptType.setChangingOverTime(true);
        ProductCmpt productCmpt = newProductCmpt(newProductCmptType, "Cmpt1");

        assertTrue(productCmpt.allowGenerations());
    }

    @Test
    public void testAllowGenerations_changingOverTimeDisabled() throws CoreException {
        ProductCmptType newProductCmptType = newProductCmptType(ipsProject, "TestProductCmptType");
        newProductCmptType.setChangingOverTime(false);
        ProductCmpt productCmpt = newProductCmpt(newProductCmptType, "Cmpt1");

        assertFalse(productCmpt.allowGenerations());
    }

    @Test
    public void testAllowGenerations_productCmptTypeCanNotBeFound() throws CoreException {
        ProductCmptType newProductCmptType = newProductCmptType(ipsProject, "TestProductCmptType");
        ProductCmpt productCmpt = newProductCmpt(newProductCmptType, "Cmpt1");
        productCmpt = spy(productCmpt);
        when(productCmpt.findProductCmptType(ipsProject)).thenReturn(null);

        assertTrue(productCmpt.allowGenerations());
    }

    @Test
    public void testIsTemplate_noTemplate() throws Exception {
        ProductCmpt product = newProductCmpt(ipsProject, "AnyProdCmpt");

        assertFalse(product.isTemplate());
    }

    @Test
    public void testIsTemplate_isTemplate() throws Exception {
        IIpsObject template = newIpsObject(ipsProject, IpsObjectType.PRODUCT_TEMPLATE, "AnyProdCmpt");

        assertThat(template, instanceOf(IProductCmpt.class));
        assertTrue(((IProductCmpt)template).isTemplate());
    }

    @Test
    public void testIsUsingExistingTemplate_missingTemplate() throws Exception {
        ProductCmpt product = newProductCmpt(ipsProject, "AnyProdCmpt");
        product.setTemplate("Template");

        assertFalse(product.isUsingExistingTemplate(ipsProject));
    }

    @Test
    public void testIsUsingExistingTemplate_existingTemplate() throws Exception {
        ProductCmpt product = newProductCmpt(ipsProject, "AnyProdCmpt");
        newProductTemplate(ipsProject, "Template");
        product.setTemplate("Template");

        assertTrue(product.isUsingExistingTemplate(ipsProject));
    }

}
