/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.productcmpt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptTypeAttribute;
import org.faktorips.devtools.core.internal.model.pctype.ValidationRule;
import org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptType;
import org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptTypeAttribute;
import org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptTypeMethod;
import org.faktorips.devtools.core.internal.model.productcmpttype.TableStructureUsage;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectGeneration;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
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
import org.faktorips.devtools.core.model.productcmpt.IProductCmptNamingStrategy;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValueContainerToTypeDelta;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptCategory;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
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
    private IProductCmptTypeAttribute attr;
    private IProductCmptTypeAttribute attr2;
    private IProductCmptType type;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject(new ArrayList<Locale>());
        root = ipsProject.getIpsPackageFragmentRoots()[0];
        pack = root.createPackageFragment("products.folder", true, null);
        srcFile = pack.createIpsFile(IpsObjectType.PRODUCT_CMPT, "TestProduct", true, null);
        productCmpt = (ProductCmpt)srcFile.getIpsObject();

        type = newProductCmptType(ipsProject, "ProdType");
        policyCmptType = newPolicyCmptType(ipsProject, "PolType");
        attr = new ProductCmptTypeAttribute(type, "IDAttr1");
        attr.setName("TypeAttr1");
        attr2 = new ProductCmptTypeAttribute(type, "IDAttr2");
        attr2.setName("TypeAttr2");

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
    // Suppressed "unused" warning for improved readability
    @SuppressWarnings("unused")
    public void testFindPropertyValues() throws CoreException {
        IProductCmpt productCmpt = newProductCmpt(type, "MyProduct");

        // Create some properties on the product component
        IProductCmptTypeAttribute productAttribute1 = type.newProductCmptTypeAttribute("productAttribute1");
        IProductCmptTypeAttribute productAttribute2 = type.newProductCmptTypeAttribute("productAttribute2");
        IProductCmptTypeAttribute productAttribute3 = type.newProductCmptTypeAttribute("productAttribute3");

        // Create some properties on the generation
        IProductCmptTypeAttribute genAttribute1 = type.newProductCmptTypeAttribute("g1");
        IProductCmptTypeAttribute genAttribute2 = type.newProductCmptTypeAttribute("g2");

        // Create a category and assign some properties
        IProductCmptCategory category = type.newCategory("myCategory");
        productAttribute1.setCategory(category.getName());
        productAttribute3.setCategory(category.getName());
        genAttribute2.setCategory(category.getName());

        // Create two generations
        GregorianCalendar validFrom1 = new GregorianCalendar(2011, 12, 12);
        GregorianCalendar validFrom2 = new GregorianCalendar(2012, 12, 12);
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
    public void testFindPropertyValues_NoGenerationWithTheIndicatedEffectiveDate() throws CoreException {
        IProductCmptCategory category = type.newCategory("myCategory");
        IProductCmptTypeAttribute productAttribute = type.newProductCmptTypeAttribute("productAttribute");
        productAttribute.setCategory(category.getName());

        IProductCmpt productCmpt = newProductCmpt(type, "MyProduct");
        IPropertyValue productValue = productCmpt.newPropertyValue(productAttribute);

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
        IProductCmptCategory category1 = type.newCategory("category1");
        IProductCmptCategory category2 = type.newCategory("category2");

        IProductCmptTypeAttribute productAttribute1 = type.newProductCmptTypeAttribute("productAttribute1");
        productAttribute1.setCategory(category1.getName());
        IProductCmptTypeAttribute productAttribute2 = type.newProductCmptTypeAttribute("productAttribute2");
        productAttribute2.setCategory(category2.getName());

        IProductCmpt productCmpt = newProductCmpt(type, "MyProduct");
        GregorianCalendar validFrom = new GregorianCalendar(2011, 12, 12);
        IProductCmptGeneration generation = (IProductCmptGeneration)productCmpt.newGeneration(validFrom);
        IPropertyValue productValue1 = generation.newPropertyValue(productAttribute1);
        IPropertyValue productValue2 = generation.newPropertyValue(productAttribute2);

        List<IPropertyValue> propertyValues = productCmpt.findPropertyValues(null, validFrom, ipsProject);
        assertEquals(productValue1, propertyValues.get(0));
        assertEquals(productValue2, propertyValues.get(1));
        assertEquals(2, propertyValues.size());
    }

    @Test
    public void testFindProductCmptKind() throws CoreException {
        IProductCmptKind kind = productCmpt.findProductCmptKind();
        assertEquals("TestProduct", kind.getName());
        assertEquals("TestProduct", kind.getRuntimeId());

        IProductCmptNamingStrategy strategy = new DateBasedProductCmptNamingStrategy(" ", "yyyy-MM", false);
        IIpsProjectProperties props = ipsProject.getProperties();
        props.setProductCmptNamingStrategy(strategy);
        ipsProject.setProperties(props);
        productCmpt = newProductCmpt(ipsProject, "motor.MotorProduct 2005-10");
        kind = productCmpt.findProductCmptKind();
        assertEquals("MotorProduct", kind.getName());
        assertEquals("MotorProduct", kind.getRuntimeId());
    }

    @Test
    public void testFindProductCmptKindWithIllegalName() throws CoreException {
        IProductCmptNamingStrategy strategy = new DateBasedProductCmptNamingStrategy(" ", "yyyy-MM", false);
        IIpsProjectProperties props = ipsProject.getProperties();
        props.setProductCmptNamingStrategy(strategy);
        ipsProject.setProperties(props);

        productCmpt = newProductCmpt(ipsProject, "motor.MotorProduct 2011-11");
        productCmpt.setName("motor.MotorProduct");
        assertNull(productCmpt.findProductCmptKind());
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
        assertEquals(2, productCmpt.getNumOfGenerations());
        IProductCmptGeneration gen = (IProductCmptGeneration)productCmpt.getGenerationsOrderedByValidDate()[0];
        assertEquals(1, gen.getNumOfConfigElements());
        IConfigElement ce = gen.getConfigElements()[0];
        assertEquals("1.5", ce.getValue());
    }

    @Test
    public void testToXml() {
        productCmpt.setProductCmptType("MotorProduct");
        productCmpt.setRuntimeId("MotorProductId");
        IProductCmptGeneration gen1 = (IProductCmptGeneration)productCmpt.newGeneration();
        IConfigElement ce1 = gen1.newConfigElement();
        ce1.setValue("0.15");
        productCmpt.newGeneration();
        Element element = productCmpt.toXml(newDocument());
        ProductCmpt copy = new ProductCmpt();
        copy.initFromXml(element);
        assertEquals("MotorProduct", copy.getProductCmptType());
        assertEquals("MotorProductId", copy.getRuntimeId());
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
        assertEquals("testtesttest", attributeValue.getValue());
    }

    @Test
    public void testToXml_AttributeValues() {
        attr2.setChangingOverTime(false);
        IPropertyValue propertyValue = productCmpt.newPropertyValue(attr2);
        Element xml = productCmpt.toXml(newDocument());

        ProductCmpt copy = new ProductCmpt();
        copy.initFromXml(xml);
        IAttributeValue copyAttributeValue = copy.getAttributeValue(attr2.getName());
        assertNotNull(copyAttributeValue);
        assertEquals(propertyValue.getName(), copyAttributeValue.getName());
    }

    @Test
    public void testContainsFormula() {
        assertFalse(productCmpt.containsFormula());
        IProductCmptGeneration gen1 = (IProductCmptGeneration)productCmpt.newGeneration(new GregorianCalendar(2000, 1,
                1));
        IProductCmptGeneration gen2 = (IProductCmptGeneration)productCmpt.newGeneration(new GregorianCalendar(2010, 1,
                1));
        gen1.newFormula();
        assertTrue(productCmpt.containsFormula());

        for (IFormula formula : gen1.getFormulas()) {
            formula.delete();
        }
        assertFalse(productCmpt.containsFormula());

        gen2.newFormula();
        assertTrue(productCmpt.containsFormula());
    }

    @Test
    public void testContainsFormulaTest() {
        assertFalse(productCmpt.containsFormula());
        IProductCmptGeneration gen1 = (IProductCmptGeneration)productCmpt.newGeneration();
        IProductCmptGeneration gen2 = (IProductCmptGeneration)productCmpt.newGeneration();
        gen1.newFormula().newFormulaTestCase();
        assertTrue(productCmpt.containsFormulaTest());

        for (IFormula formula : gen1.getFormulas()) {
            formula.delete();
        }
        assertFalse(productCmpt.containsFormulaTest());

        gen2.newFormula().newFormulaTestCase();
        assertTrue(productCmpt.containsFormulaTest());
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

        IAttributeValue attributeValue2 = ((IProductCmptGeneration)newProductCmpt.getFirstGeneration())
                .getAttributeValue(newAttribute.getName());
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
    public void testNewPropertyValue() {
        assertEquals(0,
                productCmpt.getPropertyValues(ProductCmptPropertyType.PRODUCT_CMPT_TYPE_ATTRIBUTE.getValueClass())
                        .size());
        productCmpt.newPropertyValue(attr);
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
        assertEquals(2,
                productCmpt.getPropertyValues(ProductCmptPropertyType.PRODUCT_CMPT_TYPE_ATTRIBUTE.getValueClass())
                        .size());
        productCmpt.newPropertyValue(new ProductCmptTypeMethod(type, "Method"));
        assertEquals(2,
                productCmpt.getPropertyValues(ProductCmptPropertyType.PRODUCT_CMPT_TYPE_ATTRIBUTE.getValueClass())
                        .size());
    }

    @Test
    public void testGetAttributeValue() {
        productCmpt.newPropertyValue(attr);
        assertNotNull(productCmpt.getAttributeValue("TypeAttr1"));
        assertNull(productCmpt.getAttributeValue("NonExistentAttr"));
    }

    @Test
    public void testHasPropertyValue() {
        assertFalse(productCmpt.hasPropertyValue(attr));

        productCmpt.newPropertyValue(attr);
        assertTrue(productCmpt.hasPropertyValue(attr));
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
    }

    @Test
    public void returnLatestGenerationNull() {
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
}
