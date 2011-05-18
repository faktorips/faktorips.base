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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Locale;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptTypeAttribute;
import org.faktorips.devtools.core.internal.model.pctype.ValidationRule;
import org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptTypeAttribute;
import org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptTypeMethod;
import org.faktorips.devtools.core.internal.model.productcmpttype.TableStructureUsage;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpt.IConfigElement;
import org.faktorips.devtools.core.model.productcmpt.IFormula;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptKind;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptNamingStrategy;
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

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject(new ArrayList<Locale>());
        root = ipsProject.getIpsPackageFragmentRoots()[0];
        pack = root.createPackageFragment("products.folder", true, null);
        srcFile = pack.createIpsFile(IpsObjectType.PRODUCT_CMPT, "TestProduct", true, null);
        productCmpt = (ProductCmpt)srcFile.getIpsObject();
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

        productCmpt = newProductCmpt(ipsProject, "motor.MotorProduct");
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
    public void testDependsOn() throws Exception {
        // TODO v2 - fix test
        // IPolicyCmptType a = newPolicyAndProductCmptType(ipsProject, "A", "AConfigType");
        // IPolicyCmptType b = newPolicyAndProductCmptType(ipsProject, "B", "BConfigType");
        // IRelation relation = a.newRelation();
        // relation.setTarget(b.getQualifiedName());
        //
        // IPolicyCmptType c = newPolicyAndProductCmptType(ipsProject, "C", "Config");
        // c.setSupertype(a.getQualifiedName());
        //
        // IPolicyCmptType d = newPolicyCmptType(root, "D");
        // relation = c.newRelation();
        // relation.setTargetRoleSingular("relationD");
        // relation.setTarget(d.getQualifiedName());
        //
        // IProductCmpt productCmptC = newProductCmpt(c.findProductCmptType(ipsProject),
        // "productC");
        // QualifiedNameType[] dependsOn = productCmptC.dependsOn();
        // List dependsOnAsList = CollectionUtil.toArrayList(dependsOn);
        // assertTrue(dependsOnAsList.contains(c.getQualifiedNameType()));
        // assertTrue(dependsOnAsList.contains(a.getQualifiedNameType()));
        // assertTrue(dependsOnAsList.contains(d.getQualifiedNameType()));
        //
        // a.getRelations()[0].setProductRelevant(false);
        // c.getRelations()[0].setProductRelevant(false);
        // dependsOn = productCmptC.dependsOn();
        // dependsOnAsList = CollectionUtil.toArrayList(dependsOn);
        // dependsOnAsList.contains(a.getQualifiedNameType());
        // dependsOnAsList.contains(c.getQualifiedNameType());
        // assertEquals(2, dependsOn.length);
        //
        // IProductCmpt productCmptTmp = (IProductCmpt)newIpsObject(root,
        // IpsObjectType.PRODUCT_CMPT, "deckung");
        // dependsOn = productCmptTmp.dependsOn();
        // assertEquals(0, dependsOn.length);
        //
        // // test dependency to product cmpts in all generations
        // IProductCmpt productCmptD = (IProductCmpt)newIpsObject(root, IpsObjectType.PRODUCT_CMPT,
        // "productD");
        // productCmptD.setPolicyCmptType(d.getQualifiedName());
        // IProductCmpt productCmptD2 = (IProductCmpt)newIpsObject(root, IpsObjectType.PRODUCT_CMPT,
        // "productD2");
        // productCmptD2.setPolicyCmptType(d.getQualifiedName());
        // c.getRelations()[0].setProductRelevant(true);
        //
        // // generation1
        // IProductCmptGeneration generation1 = (IProductCmptGeneration)
        // productCmptC.newGeneration();
        // generation1.setValidFrom(IpsPlugin.getDefault().getIpsPreferences().getWorkingDate());
        //
        // IProductCmptRelation productCmptRelation = generation1.newRelation("relationD");
        // productCmptRelation.setTarget(productCmptD.getQualifiedName());
        // // generation2
        // IProductCmptGeneration generation2 = (IProductCmptGeneration)
        // productCmptC.newGeneration(new GregorianCalendar(1990, 1, 1));
        // productCmptRelation = generation2.newRelation("relationD");
        // productCmptRelation.setTarget(productCmptD2.getQualifiedName());
        //
        // dependsOnAsList = CollectionUtil.toArrayList(productCmptC.dependsOn());
        // assertEquals(5, dependsOnAsList.size());
        // assertTrue(dependsOnAsList.contains(c.getQualifiedNameType()));
        // assertTrue(dependsOnAsList.contains(a.getQualifiedNameType()));
        // assertTrue(dependsOnAsList.contains(d.getQualifiedNameType()));
        // assertTrue(dependsOnAsList.contains(productCmptD.getQualifiedNameType()));
        // assertTrue(dependsOnAsList.contains(productCmptD2.getQualifiedNameType()));
        //
        // // test dependency to table content usage
        // ITableContents tableContents1 = (ITableContents)newIpsObject(root,
        // IpsObjectType.TABLE_CONTENTS, "table1");
        // ITableContents tableContents2 = (ITableContents)newIpsObject(root,
        // IpsObjectType.TABLE_CONTENTS, "table2");
        // generation1.newTableContentUsage().setTableContentName(tableContents1.getQualifiedName());
        //
        // dependsOnAsList = CollectionUtil.toArrayList(productCmptC.dependsOn());
        // assertEquals(6, dependsOnAsList.size());
        // assertTrue(dependsOnAsList.contains(tableContents1.getQualifiedNameType()));
        //
        // generation2.newTableContentUsage().setTableContentName(tableContents2.getQualifiedName());
        //
        // dependsOnAsList = CollectionUtil.toArrayList(productCmptC.dependsOn());
        // assertEquals(7, dependsOnAsList.size());
        // assertTrue(dependsOnAsList.contains(tableContents2.getQualifiedNameType()));
    }

    @Test
    public void testDependsOnWithFormula() {
        // TODO v2 - fix test
        // IPolicyCmptType a = newPolicyCmptType(root, "A");
        // IPolicyCmptType b = newPolicyCmptType(root, "B");
        // IPolicyCmptType c = newPolicyCmptType(root, "C");
        // IAttribute attrA = a.newAttribute();
        // attrA.setName("attrA");
        // attrA.setDatatype(Datatype.STRING.getQualifiedName());
        // attrA.setProductRelevant(true);
        // attrA.setAttributeType(AttributeType.DERIVED_BY_EXPLICIT_METHOD_CALL);
        // Parameter[] parameter = new Parameter[2];
        // parameter[0] = new Parameter(0, "paraB", b.getQualifiedName());
        // parameter[1] = new Parameter(1, "paraC", c.getQualifiedName());
        // attrA.setFormulaParameters(parameter);
        // IProductCmpt productA = newProductCmpt(root, "productA");
        // productA.setPolicyCmptType(a.getQualifiedName());
        // IProductCmptGeneration genProductA = (IProductCmptGeneration)productA.newGeneration();
        // IConfigElement configElAttrA = genProductA.newConfigElement();
        // configElAttrA.setPcTypeAttribute(attrA.getName());
        // configElAttrA.setType(ConfigElementType.FORMULA);
        // List dependsOnList = Arrays.asList(productA.dependsOn());
        //
        // assertTrue(dependsOnList.contains(new QualifiedNameType(a.getQualifiedName(),
        // a.getIpsObjectType())));
        // assertTrue(dependsOnList.contains(new QualifiedNameType(b.getQualifiedName(),
        // b.getIpsObjectType())));
        // assertTrue(dependsOnList.contains(new QualifiedNameType(c.getQualifiedName(),
        // b.getIpsObjectType())));
    }

    /**
     * Test method for
     * {@link org.faktorips.devtools.core.model.ipsobject.IFixDifferencesToModelSupport#containsDifferenceToModel(IIpsProject)}
     * .
     */
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
    public void testNewPropertyValue() throws CoreException {
        IProductCmptType type = newProductCmptType(ipsProject, "ProdType");
        IPolicyCmptType policyCmptType = newPolicyCmptType(ipsProject, "PolType");
        IProductCmptTypeAttribute attr = new ProductCmptTypeAttribute(type, "TypeAttr1");
        IProductCmptTypeAttribute attr2 = new ProductCmptTypeAttribute(type, "TypeAttr2");

        assertEquals(0, productCmpt.getPropertyValues(ProductCmptPropertyType.VALUE).size());
        productCmpt.newPropertyValue(attr);
        assertEquals(1, productCmpt.getPropertyValues(ProductCmptPropertyType.VALUE).size());
        productCmpt.newPropertyValue(attr2);
        assertEquals(2, productCmpt.getPropertyValues(ProductCmptPropertyType.VALUE).size());

        productCmpt.newPropertyValue(new ValidationRule());
        assertEquals(2, productCmpt.getPropertyValues(ProductCmptPropertyType.VALUE).size());
        productCmpt.newPropertyValue(new PolicyCmptTypeAttribute(policyCmptType, "pcTypeAttribute"));
        assertEquals(2, productCmpt.getPropertyValues(ProductCmptPropertyType.VALUE).size());
        productCmpt.newPropertyValue(new TableStructureUsage());
        assertEquals(2, productCmpt.getPropertyValues(ProductCmptPropertyType.VALUE).size());
        productCmpt.newPropertyValue(new ProductCmptTypeMethod(type, "Method"));
        assertEquals(2, productCmpt.getPropertyValues(ProductCmptPropertyType.VALUE).size());
    }

}
