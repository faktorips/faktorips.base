/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.product;

import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.model.IIpsPackageFragment;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsProjectProperties;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.QualifiedNameType;
import org.faktorips.devtools.core.model.pctype.AttributeType;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IRelation;
import org.faktorips.devtools.core.model.pctype.Parameter;
import org.faktorips.devtools.core.model.product.ConfigElementType;
import org.faktorips.devtools.core.model.product.IConfigElement;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.devtools.core.model.product.IProductCmptKind;
import org.faktorips.devtools.core.model.product.IProductCmptNamingStrategy;
import org.faktorips.devtools.core.model.product.IProductCmptRelation;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.util.CollectionUtil;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Element;


/**
 *
 */
public class ProductCmptTest extends AbstractIpsPluginTest {
    
    private ProductCmpt productCmpt;
    private IIpsPackageFragmentRoot root;
    private IIpsPackageFragment pack;
    private IIpsSrcFile srcFile;
    private IIpsProject ipsProject;
    
    /*
     * @see PluginTest#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        ipsProject = this.newIpsProject("TestProject");
        root = ipsProject.getIpsPackageFragmentRoots()[0];
        pack = root.createPackageFragment("products.folder", true, null);
        srcFile = pack.createIpsFile(IpsObjectType.PRODUCT_CMPT, "TestProduct", true, null);
        productCmpt = (ProductCmpt)srcFile.getIpsObject();
    }
    
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
    
    public void testSetPolicyCmptType() {
        productCmpt.setPolicyCmptType("newType");
        assertEquals("newType", productCmpt.getPolicyCmptType());
        assertTrue(srcFile.isDirty());
    }
    
    public void testInitFromXml() {
        productCmpt.initFromXml(getTestDocument().getDocumentElement());
        assertEquals("MotorPolicy", productCmpt.getPolicyCmptType());
        assertEquals(2, productCmpt.getNumOfGenerations());
        IProductCmptGeneration gen = (IProductCmptGeneration)productCmpt.getGenerations()[0];
        assertEquals(1, gen.getNumOfConfigElements());
        IConfigElement ce = gen.getConfigElements()[0];
        assertEquals("1.5", ce.getValue());
    }
    
    public void testToXml() {
        productCmpt.setPolicyCmptType("MotorPolicy");
        IProductCmptGeneration gen1 = (IProductCmptGeneration)productCmpt.newGeneration();
        IConfigElement ce1 = gen1.newConfigElement();
        ce1.setValue("0.15");
        productCmpt.newGeneration();
        Element element = productCmpt.toXml(newDocument());
        ProductCmpt copy = new ProductCmpt();
        copy.initFromXml(element);
        assertEquals("MotorPolicy", copy.getPolicyCmptType());
        assertEquals(2, copy.getNumOfGenerations());
        IProductCmptGeneration genCopy = (IProductCmptGeneration)copy.getGenerations()[0];
        assertEquals(1, genCopy.getConfigElements().length);
        assertEquals("0.15", genCopy.getConfigElements()[0].getValue());
    }
    
    public void testContainsFormula() {
        IProductCmptGeneration gen1 = (IProductCmptGeneration)productCmpt.newGeneration();
        IConfigElement ce1 = gen1.newConfigElement();
        ce1.setType(ConfigElementType.POLICY_ATTRIBUTE);
        assertFalse(productCmpt.containsFormula());

        IConfigElement ce2 = gen1.newConfigElement();
        ce2.setType(ConfigElementType.FORMULA);
        assertTrue(productCmpt.containsFormula());
    }
    
    public void testDependsOn() throws Exception{
        IPolicyCmptType a = newPolicyCmptType(root, "A");
        IPolicyCmptType b = newPolicyCmptType(root, "B");
        IRelation relation = a.newRelation();
        relation.setTarget(b.getQualifiedName());
        IPolicyCmptType c = newPolicyCmptType(root, "C");
        c.setSupertype(a.getQualifiedName());
        IPolicyCmptType d = newPolicyCmptType(root, "D");
        relation = c.newRelation();
        relation.setTargetRoleSingular("relationD");
        relation.setTarget(d.getQualifiedName());
        IProductCmpt productCmptC = (IProductCmpt)newIpsObject(root, IpsObjectType.PRODUCT_CMPT, "productC");
        productCmptC.setPolicyCmptType(c.getQualifiedName());
        QualifiedNameType[] dependsOn = productCmptC.dependsOn();
        List dependsOnAsList = CollectionUtil.toArrayList(dependsOn);
        assertTrue(dependsOnAsList.contains(c.getQualifiedNameType()));
        assertTrue(dependsOnAsList.contains(a.getQualifiedNameType()));
        assertTrue(dependsOnAsList.contains(d.getQualifiedNameType()));
        
        a.getRelations()[0].setProductRelevant(false);
        c.getRelations()[0].setProductRelevant(false);
        dependsOn = productCmptC.dependsOn();
        dependsOnAsList = CollectionUtil.toArrayList(dependsOn);
        dependsOnAsList.contains(a.getQualifiedNameType());
        dependsOnAsList.contains(c.getQualifiedNameType());
        assertEquals(2, dependsOn.length);
        
        IProductCmpt productCmptTmp = (IProductCmpt)newIpsObject(root, IpsObjectType.PRODUCT_CMPT, "deckung");
        dependsOn = productCmptTmp.dependsOn();
        assertEquals(0, dependsOn.length);
        
        // test dependency to product cmpts in all generations
        IProductCmpt productCmptD = (IProductCmpt)newIpsObject(root, IpsObjectType.PRODUCT_CMPT, "productD");
        productCmptD.setPolicyCmptType(d.getQualifiedName());
        IProductCmpt productCmptD2 = (IProductCmpt)newIpsObject(root, IpsObjectType.PRODUCT_CMPT, "productD2");
        productCmptD2.setPolicyCmptType(d.getQualifiedName());
        c.getRelations()[0].setProductRelevant(true);

        // generation1
        IProductCmptGeneration generation1 = (IProductCmptGeneration) productCmptC.newGeneration();
        generation1.setValidFrom(IpsPlugin.getDefault().getIpsPreferences().getWorkingDate());

        IProductCmptRelation productCmptRelation = generation1.newRelation("relationD");
        productCmptRelation.setTarget(productCmptD.getQualifiedName());
        // generation2
        IProductCmptGeneration generation2 = (IProductCmptGeneration) productCmptC.newGeneration(new GregorianCalendar(1990, 1, 1));
        productCmptRelation = generation2.newRelation("relationD");
        productCmptRelation.setTarget(productCmptD2.getQualifiedName());
        
        dependsOnAsList = CollectionUtil.toArrayList(productCmptC.dependsOn());
        assertEquals(5, dependsOnAsList.size());
        assertTrue(dependsOnAsList.contains(c.getQualifiedNameType()));
        assertTrue(dependsOnAsList.contains(a.getQualifiedNameType()));
        assertTrue(dependsOnAsList.contains(d.getQualifiedNameType()));        
        assertTrue(dependsOnAsList.contains(productCmptD.getQualifiedNameType()));
        assertTrue(dependsOnAsList.contains(productCmptD2.getQualifiedNameType()));
        
        // test dependency to table content usage
        ITableContents tableContents1 = (ITableContents)newIpsObject(root, IpsObjectType.TABLE_CONTENTS, "table1");
        ITableContents tableContents2 = (ITableContents)newIpsObject(root, IpsObjectType.TABLE_CONTENTS, "table2");
        generation1.newTableContentUsage().setTableContentName(tableContents1.getQualifiedName());
        
        dependsOnAsList = CollectionUtil.toArrayList(productCmptC.dependsOn());
        assertEquals(6, dependsOnAsList.size());
        assertTrue(dependsOnAsList.contains(tableContents1.getQualifiedNameType()));
        
        generation2.newTableContentUsage().setTableContentName(tableContents2.getQualifiedName());
        
        dependsOnAsList = CollectionUtil.toArrayList(productCmptC.dependsOn());
        assertEquals(7, dependsOnAsList.size());
        assertTrue(dependsOnAsList.contains(tableContents2.getQualifiedNameType()));
    }

    public void testDependsOnWithFormula() throws CoreException{
    	IPolicyCmptType a = newPolicyCmptType(root, "A");
    	IPolicyCmptType b = newPolicyCmptType(root, "B");
    	IPolicyCmptType c = newPolicyCmptType(root, "C");
    	IAttribute attrA = a.newAttribute();
    	attrA.setName("attrA");
    	attrA.setDatatype(Datatype.STRING.getQualifiedName());
    	attrA.setProductRelevant(true);
    	attrA.setAttributeType(AttributeType.DERIVED_BY_EXPLICIT_METHOD_CALL);
    	Parameter[] parameter = new Parameter[2];
    	parameter[0] = new Parameter(0, "paraB", b.getQualifiedName());
    	parameter[1] = new Parameter(1, "paraC", c.getQualifiedName());
    	attrA.setFormulaParameters(parameter);
    	IProductCmpt productA = newProductCmpt(root, "productA");
    	productA.setPolicyCmptType(a.getQualifiedName());
    	IProductCmptGeneration genProductA = (IProductCmptGeneration)productA.newGeneration();
    	IConfigElement configElAttrA = genProductA.newConfigElement();
    	configElAttrA.setPcTypeAttribute(attrA.getName());
    	configElAttrA.setType(ConfigElementType.FORMULA);
    	List dependsOnList = Arrays.asList(productA.dependsOn());
    	
    	assertTrue(dependsOnList.contains(new QualifiedNameType(a.getQualifiedName(), a.getIpsObjectType())));
    	assertTrue(dependsOnList.contains(new QualifiedNameType(b.getQualifiedName(), b.getIpsObjectType())));
    	assertTrue(dependsOnList.contains(new QualifiedNameType(c.getQualifiedName(), b.getIpsObjectType())));
    }
    /**
     * Tests for the correct type of excetion to be thrown - no part of any type could ever be created.
     */
    public void testNewPart() {
    	try {
    		productCmpt.newPart(IAttribute.class);
			fail();
		} catch (IllegalArgumentException e) {
			//nothing to do :-)
		}
    }
    
    public void testValidate_InconsitencyInTypeHierarch() throws Exception {
        PolicyCmptType type = super.newPolicyCmptType(root, "Type"); 
        PolicyCmptType supertype = super.newPolicyCmptType(root, "Supertype"); 
        PolicyCmptType supersupertype = super.newPolicyCmptType(root, "Supersupertype"); 

        type.setSupertype(supertype.getQualifiedName());
        supertype.setSupertype(supersupertype.getQualifiedName());
    	supersupertype.setSupertype("abc");

    	MessageList ml = type.validate();
    	assertNotNull(ml.getMessageByCode(IPolicyCmptType.MSGCODE_INCONSISTENT_TYPE_HIERARCHY));
    	
    	ProductCmpt product = super.newProductCmpt(root, "products.Testproduct");
    	product.setPolicyCmptType(type.getQualifiedName());
    	
    	ml = product.validate();
    	assertNotNull(ml.getMessageByCode(IProductCmpt.MSGCODE_INCONSISTENCY_IN_POLICY_CMPT_TYPE_HIERARCHY));
    	
    	supersupertype.setSupertype("");
    	ml = type.validate();
    	assertNull(ml.getMessageByCode(IPolicyCmptType.MSGCODE_INCONSISTENT_TYPE_HIERARCHY));
    	ml = product.validate();
    	assertNull(ml.getMessageByCode(IProductCmpt.MSGCODE_INCONSISTENCY_IN_POLICY_CMPT_TYPE_HIERARCHY));

    	supersupertype.setSupertype(type.getQualifiedName());
    	ml = type.validate();
    	assertNotNull(ml.getMessageByCode(IPolicyCmptType.MSGCODE_CYCLE_IN_TYPE_HIERARCHY));
    	ml = product.validate();
    	assertNotNull(ml.getMessageByCode(IProductCmpt.MSGCODE_INCONSISTENCY_IN_POLICY_CMPT_TYPE_HIERARCHY));
    	
    }

    /**
     * Test method for {@link org.faktorips.devtools.core.model.IFixDifferencesToModelSupport#containsDifferenceToModel()}.
     * @throws CoreException 
     */
    public void testContainsDifferenceToModel() throws CoreException {
        PolicyCmptType testType = newPolicyCmptType(ipsProject, "TestType");
        IAttribute a1 = testType.newAttribute();
        a1.setName("A1");
        a1.setProductRelevant(true);
        
        IProductCmpt product = newProductCmpt(ipsProject, "TestProduct");
        product.setPolicyCmptType(testType.getQualifiedName());
        IProductCmptGeneration gen = (IProductCmptGeneration)product.newGeneration();
        IConfigElement ce1 = gen.newConfigElement();
        ce1.setPcTypeAttribute("A1");
        ce1.setType(ConfigElementType.POLICY_ATTRIBUTE);
        
        IAttribute a2 = testType.newAttribute();
        a2.setName("A2");
        a2.setProductRelevant(true);
        
        IProductCmpt product2 = newProductCmpt(ipsProject, "TestProduct2");
        product2.setPolicyCmptType(testType.getQualifiedName());
        gen = (IProductCmptGeneration)product2.newGeneration();
        ce1 = gen.newConfigElement();
        ce1.setPcTypeAttribute("A1");
        ce1.setType(ConfigElementType.POLICY_ATTRIBUTE);
        IConfigElement ce2 = gen.newConfigElement();
        ce2.setPcTypeAttribute("A2");
        ce2.setType(ConfigElementType.POLICY_ATTRIBUTE);
        
        assertEquals(true, product.containsDifferenceToModel());
        assertEquals(false, product2.containsDifferenceToModel());
        testType.getAttribute("A2").delete();
        assertEquals(false, product.containsDifferenceToModel());
        assertEquals(true, product2.containsDifferenceToModel());       
    }

    /**
     * Test method for {@link org.faktorips.devtools.core.model.IFixDifferencesToModelSupport#fixAllDifferencesToModel()}.
     * @throws CoreException 
     */
    public void testFixAllDifferencesToModel() throws CoreException {
        PolicyCmptType testType = newPolicyCmptType(ipsProject, "TestType");
        IAttribute a1 = testType.newAttribute();
        a1.setName("A1");
        a1.setProductRelevant(true);

        IProductCmpt product = newProductCmpt(ipsProject, "TestProduct");
        product.setPolicyCmptType(testType.getQualifiedName());
        IProductCmptGeneration gen = (IProductCmptGeneration)product.newGeneration();
        IConfigElement ce1 = gen.newConfigElement();
        ce1.setPcTypeAttribute("A1");
        ce1.setType(ConfigElementType.POLICY_ATTRIBUTE);
        
        IAttribute a2 = testType.newAttribute();
        a2.setName("A2");
        a2.setProductRelevant(true);
        
        IProductCmpt product2 = newProductCmpt(ipsProject, "TestProduct2");
        product2.setPolicyCmptType(testType.getQualifiedName());
        gen = (IProductCmptGeneration)product2.newGeneration();
        ce1 = gen.newConfigElement();
        ce1.setPcTypeAttribute("A1");
        ce1.setType(ConfigElementType.POLICY_ATTRIBUTE);
        IConfigElement ce2 = gen.newConfigElement();
        ce2.setPcTypeAttribute("A2");
        ce2.setType(ConfigElementType.POLICY_ATTRIBUTE);
        
        assertEquals(true, product.containsDifferenceToModel());
        product.fixAllDifferencesToModel();
        assertEquals(false, product.containsDifferenceToModel());
        
        assertEquals(false, product2.containsDifferenceToModel());
        product2.fixAllDifferencesToModel();
        assertEquals(false, product2.containsDifferenceToModel());
    }
    
    public void testSetValidTo(){
        productCmpt.setValidTo(new GregorianCalendar(2000,1,1));
        assertEquals(new GregorianCalendar(2000,1,1), productCmpt.getValidTo());
    }
    
    /**
     * Test if a runtime id change will be correctly updated in the product component which 
     * referenced the product cmpt on which the runtime id was changed.
     */
//    public void testRuntimeIdDependency() throws CoreException{
//        IPolicyCmptType c = newPolicyCmptType(root, "C");
//        IPolicyCmptType d = newPolicyCmptType(root, "D");
//        IRelation relation = c.newRelation();
//        relation.setProductRelevant(true);
//        relation.setTargetRoleSingular("relationD");
//        relation.setTarget(d.getQualifiedName());
//        IProductCmpt productCmptC = (IProductCmpt)newIpsObject(root, IpsObjectType.PRODUCT_CMPT, "tests.productC");
//        productCmptC.setPolicyCmptType(c.getQualifiedName());
//        
//        IProductCmpt productCmptD = (IProductCmpt)newIpsObject(root, IpsObjectType.PRODUCT_CMPT, "tests.productD");
//        productCmptD.setPolicyCmptType(d.getQualifiedName());
//
//        IProductCmptGeneration generation1 = (IProductCmptGeneration) productCmptC.newGeneration();
//        generation1.setValidFrom(IpsPlugin.getDefault().getIpsPreferences().getWorkingDate());
//        IProductCmptRelation productCmptRelation = generation1.newRelation("relationD");
//        productCmptRelation.setTarget(productCmptD.getQualifiedName());
//        
//        incrementalBuild();
//        
//        // product cmpt C depends on product D
//        // change the runtime id of product D and assert that the target runtime id in product C 
//        // was updated after rebuild
//        productCmptD.setRuntimeId("newRuntimeId");
//        productCmptD.getIpsSrcFile().save(true, null);
//        
//        incrementalBuild();
//        
//        // check if the target runtime id was updated in product cmpt c runtime xml
//        String packageOfProductC = ipsProject.getIpsArtefactBuilderSet().getPackage(
//                DefaultBuilderSet.KIND_PRODUCT_CMPT_GENERATION_IMPL, productCmptC.getIpsSrcFile());
//        String productCXmlFile = packageOfProductC + "." + "productC";
//        productCXmlFile = productCXmlFile.replaceAll("\\.", "/");
//        productCXmlFile += ".xml";
//        IFile file = ipsProject.getProject().getFile("bin//" + productCXmlFile);
//        try {
//            InputStream is = file.getContents();
//            if (is==null) {
//                throw new RuntimeException("Can't find resource " + productCXmlFile);
//            }
//            String generatedXml = getDocumentBuilder().parse(is).getDocumentElement().toString();
//            String patternStr = ".*targetRuntimeId=\"(.*)\".*";
//            Pattern pattern = Pattern.compile(patternStr);
//            Matcher matcher = pattern.matcher(generatedXml);
//            assertTrue(matcher.find());
//            assertEquals("newRuntimeId", matcher.group(matcher.groupCount()));
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }    
}
