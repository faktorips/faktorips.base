/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.productcmpttype2;

import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.ContentsChangeListener;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.pctype.AttributeType;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.productcmpttype2.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpttype2.IProdDefProperty;
import org.faktorips.devtools.core.model.productcmpttype2.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype2.IProductCmptTypeMethod;
import org.faktorips.devtools.core.model.productcmpttype2.ITableStructureUsage;
import org.faktorips.devtools.core.model.productcmpttype2.ProdDefPropertyType;
import org.faktorips.devtools.core.model.type.IMethod;
import org.faktorips.devtools.core.util.XmlUtil;
import org.faktorips.util.memento.Memento;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Element;

/**
 * 
 * @author Jan Ortmann
 */
public class ProductCmptTypeTest extends AbstractIpsPluginTest implements ContentsChangeListener {

    private ContentChangeEvent lastEvent = null;
    private IIpsProject ipsProject;
    private IPolicyCmptType policyCmptType;
    private IProductCmptType productCmptType;
    private IProductCmptType superProductCmptType;
    private IProductCmptType superSuperProductCmptType;
    
    /**
     * {@inheritDoc}
     */
    protected void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject();
        policyCmptType = newPolicyAndProductCmptType(ipsProject, "Policy", "Product");
        productCmptType = policyCmptType.findProductCmptType(ipsProject);
        superProductCmptType = newProductCmptType(ipsProject, "SuperProduct");
        productCmptType.setSupertype(superProductCmptType.getQualifiedName());
        superSuperProductCmptType = newProductCmptType(ipsProject, "SuperSuperProduct");
        superProductCmptType.setSupertype(superSuperProductCmptType.getQualifiedName());
        ipsProject.getIpsModel().addChangeListener(this);
    }
    
    protected void tearDown() throws Exception {
        super.tearDown();
        ipsProject.getIpsModel().removeChangeListener(this);
    }
    
    public void testFindAttribute() throws CoreException {
        assertNull(productCmptType.findAttribute("unknown", ipsProject));
        
        IProductCmptTypeAttribute a1 = productCmptType.newAttribute();
        a1.setName("a1");
        IProductCmptTypeAttribute a2 = superProductCmptType.newAttribute();
        a2.setName("a2");
        IProductCmptTypeAttribute a3 = superSuperProductCmptType.newAttribute();
        a3.setName("a3");
        
        assertSame(a1, productCmptType.findAttribute("a1", ipsProject));
        assertSame(a2, productCmptType.findAttribute("a2", ipsProject));
        assertSame(a3, productCmptType.findAttribute("a3", ipsProject));
        
        IProductCmptTypeAttribute a1b = superProductCmptType.newAttribute();
        a1b.setName("a1b");
        assertSame(a1, productCmptType.findAttribute("a1", ipsProject));
        
        assertNull(productCmptType.findAttribute("unknown", ipsProject));
    }
    
    public void testFindProdDefProperties() throws CoreException {
        IProdDefProperty[] props = productCmptType.findProdDefProperties(ipsProject);
        assertEquals(0, props.length);

        // attributes
        IProductCmptTypeAttribute supertypeAttr  = superProductCmptType.newAttribute();
        supertypeAttr.setName("attrInSupertype");
        supertypeAttr.setDatatype("Money");

        IProductCmptTypeAttribute typeAttribute1 = productCmptType.newAttribute("attrInType1");
        IProductCmptTypeAttribute typeAttribute2 = productCmptType.newAttribute("attrInType2");
        
        props = superProductCmptType.findProdDefProperties(ipsProject);
        assertEquals(1, props.length);
        assertEquals(supertypeAttr, props[0]);
        props = productCmptType.findProdDefProperties(ipsProject);
        assertEquals(3, props.length);
        assertEquals(supertypeAttr, props[0]);
        assertEquals(typeAttribute1, props[1]);
        assertEquals(typeAttribute2, props[2]);

        // table structure usages
        ITableStructureUsage supertypeTsu = superProductCmptType.newTableStructureUsage();
        ITableStructureUsage typeTsu1 = productCmptType.newTableStructureUsage();
        ITableStructureUsage typeTsu2 = productCmptType.newTableStructureUsage();

        props = superProductCmptType.findProdDefProperties(ipsProject);
        assertEquals(2, props.length);
        assertEquals(supertypeAttr, props[0]);
        assertEquals(supertypeTsu, props[1]);
        props = productCmptType.findProdDefProperties(ipsProject);
        assertEquals(6, props.length);
        assertEquals(supertypeAttr, props[0]);
        assertEquals(typeAttribute1, props[1]);
        assertEquals(typeAttribute2, props[2]);
        assertEquals(supertypeTsu, props[3]);
        assertEquals(typeTsu1, props[4]);
        assertEquals(typeTsu2, props[5]);
        
        // formula signatures
        IProductCmptTypeMethod supertypeSignature = superProductCmptType.newProductCmptTypeMethod();
        supertypeSignature.setFormulaSignatureDefinition(true);
        supertypeSignature.setFormulaName("CalculatePremium");
        IProductCmptTypeMethod typeSignature1 = productCmptType.newFormulaSignature("CalculatePremium1");
        IProductCmptTypeMethod typeSignature2 = productCmptType.newFormulaSignature("CalculatePremium2");
        productCmptType.newProductCmptTypeMethod().setFormulaSignatureDefinition(false);// this method is not a product def property as it is not a formula signature
        
        props = superProductCmptType.findProdDefProperties(ipsProject);
        assertEquals(3, props.length);
        assertEquals(supertypeAttr, props[0]);
        assertEquals(supertypeTsu, props[1]);
        assertEquals(supertypeSignature, props[2]);

        props = productCmptType.findProdDefProperties(ipsProject);
        assertEquals(9, props.length);
        assertEquals(supertypeAttr, props[0]);
        assertEquals(typeAttribute1, props[1]);
        assertEquals(typeAttribute2, props[2]);
        assertEquals(supertypeTsu, props[3]);
        assertEquals(typeTsu1, props[4]);
        assertEquals(typeTsu2, props[5]);
        assertEquals(supertypeSignature, props[6]);
        assertEquals(typeSignature1, props[7]);
        assertEquals(typeSignature2, props[8]);
        
        // default values and value sets
        IPolicyCmptType policyCmptSupertype = newPolicyCmptType(ipsProject, "SuperPolicy");
        superProductCmptType.setPolicyCmptType(policyCmptSupertype.getQualifiedName());
        org.faktorips.devtools.core.model.pctype.IAttribute policyCmptSupertypeAttr = policyCmptSupertype.newAttribute();
        policyCmptSupertypeAttr.setProductRelevant(true);
        org.faktorips.devtools.core.model.pctype.IAttribute policyCmptTypeAttr1 = policyCmptType.newAttribute();
        policyCmptTypeAttr1.setProductRelevant(true);
        org.faktorips.devtools.core.model.pctype.IAttribute policyCmptTypeAttr2 = policyCmptType.newAttribute();
        policyCmptTypeAttr2.setProductRelevant(true);
        policyCmptType.newAttribute().setProductRelevant(false); // this attribute is not a product def property as it is not product relevant!
        IAttribute derivedAttr = policyCmptType.newAttribute();
        derivedAttr.setProductRelevant(true);
        derivedAttr.setAttributeType(AttributeType.DERIVED_ON_THE_FLY);
        
        props = superProductCmptType.findProdDefProperties(ipsProject);
        assertEquals(4, props.length);
        assertEquals(supertypeAttr, props[0]);
        assertEquals(supertypeTsu, props[1]);
        assertEquals(supertypeSignature, props[2]);
        assertEquals(policyCmptSupertypeAttr, props[3]);

        props = productCmptType.findProdDefProperties(ipsProject);
        assertEquals(12, props.length);
        assertEquals(supertypeAttr, props[0]);
        assertEquals(typeAttribute1, props[1]);
        assertEquals(typeAttribute2, props[2]);
        assertEquals(supertypeTsu, props[3]);
        assertEquals(typeTsu1, props[4]);
        assertEquals(typeTsu2, props[5]);
        assertEquals(supertypeSignature, props[6]);
        assertEquals(typeSignature1, props[7]);
        assertEquals(typeSignature2, props[8]);
        assertEquals(policyCmptSupertypeAttr, props[9]);
        assertEquals(policyCmptTypeAttr1, props[10]);
        assertEquals(policyCmptTypeAttr2, props[11]);
    }
    
    public void testGetProdDefPropertiesMap() throws CoreException {
        // attributes
        IProductCmptTypeAttribute supertypeAttr  = superProductCmptType.newAttribute();
        supertypeAttr.setName("attrInSupertype");
        supertypeAttr.setDatatype("Money");

        IProductCmptTypeAttribute typeAttribute = productCmptType.newAttribute();
        typeAttribute.setName("attrInType");
        typeAttribute.setDatatype("Money");
        
        // table structure usages
        ITableStructureUsage supertypeTsu = superProductCmptType.newTableStructureUsage();
        supertypeTsu.setRoleName("SuperTable");
        ITableStructureUsage typeTsu = productCmptType.newTableStructureUsage();
        typeTsu.setRoleName("Table");

        // formula signatures
        IProductCmptTypeMethod supertypeSignature = superProductCmptType.newFormulaSignature("CalculatePremium");
        IProductCmptTypeMethod typeSignature = productCmptType.newFormulaSignature("CalculatePremium2");
        productCmptType.newProductCmptTypeMethod().setFormulaSignatureDefinition(false);// this method is not a product def property as it is not a formula signature
        
        // default values and value sets
        IPolicyCmptType policyCmptSupertype = newPolicyCmptType(ipsProject, "SuperPolicy");
        superProductCmptType.setPolicyCmptType(policyCmptSupertype.getQualifiedName());
        org.faktorips.devtools.core.model.pctype.IAttribute policyCmptSupertypeAttr = policyCmptSupertype.newAttribute();
        policyCmptSupertypeAttr.setName("PolicySuperAttribute");
        policyCmptSupertypeAttr.setProductRelevant(true);
        org.faktorips.devtools.core.model.pctype.IAttribute policyCmptTypeAttr = policyCmptType.newAttribute();
        policyCmptTypeAttr.setName("PolicyAttribute");
        policyCmptTypeAttr.setProductRelevant(true);
        policyCmptType.newAttribute().setProductRelevant(false); // this attribute is not a product def property as it is not product relevant!

        // test property type = null
        Map propertyMap = ((ProductCmptType)productCmptType).getProdDefPropertiesMap(null, ipsProject);
        assertEquals(8, propertyMap.size());
        assertEquals(supertypeAttr, propertyMap.get(supertypeAttr.getPropertyName()));
        assertEquals(typeAttribute, propertyMap.get(typeAttribute.getPropertyName()));
        assertEquals(supertypeTsu, propertyMap.get(supertypeTsu.getPropertyName()));
        assertEquals(typeTsu, propertyMap.get(typeTsu.getPropertyName()));
        assertEquals(supertypeSignature, propertyMap.get(supertypeSignature.getPropertyName()));
        assertEquals(typeSignature, propertyMap.get(typeSignature.getPropertyName()));
        assertEquals(policyCmptSupertypeAttr, propertyMap.get(policyCmptSupertypeAttr.getPropertyName()));
        assertEquals(policyCmptTypeAttr, propertyMap.get(policyCmptTypeAttr.getPropertyName()));
        
        // test with specific property types
        propertyMap = ((ProductCmptType)productCmptType).getProdDefPropertiesMap(ProdDefPropertyType.VALUE, ipsProject);
        assertEquals(2, propertyMap.size());
        assertEquals(supertypeAttr, propertyMap.get(supertypeAttr.getPropertyName()));
        assertEquals(typeAttribute, propertyMap.get(typeAttribute.getPropertyName()));
        
        propertyMap = ((ProductCmptType)productCmptType).getProdDefPropertiesMap(ProdDefPropertyType.TABLE_CONTENT_USAGE, ipsProject);
        assertEquals(2, propertyMap.size());
        assertEquals(supertypeTsu, propertyMap.get(supertypeTsu.getPropertyName()));
        assertEquals(typeTsu, propertyMap.get(typeTsu.getPropertyName()));
        
        propertyMap = ((ProductCmptType)productCmptType).getProdDefPropertiesMap(ProdDefPropertyType.FORMULA, ipsProject);
        assertEquals(2, propertyMap.size());
        assertEquals(supertypeSignature, propertyMap.get(supertypeSignature.getPropertyName()));
        assertEquals(typeSignature, propertyMap.get(typeSignature.getPropertyName()));

        propertyMap = ((ProductCmptType)productCmptType).getProdDefPropertiesMap(ProdDefPropertyType.DEFAULT_VALUE_AND_VALUESET, ipsProject);
        assertEquals(2, propertyMap.size());
        assertEquals(policyCmptSupertypeAttr, propertyMap.get(policyCmptSupertypeAttr.getPropertyName()));
        assertEquals(policyCmptTypeAttr, propertyMap.get(policyCmptTypeAttr.getPropertyName()));
        
        
    }
    
    public void testFindProdDefProperty_ByTypeAndName() throws CoreException {
        // attributes
        IProductCmptTypeAttribute supertypeAttr  = superProductCmptType.newAttribute();
        supertypeAttr.setName("attrInSupertype");
        supertypeAttr.setDatatype("Money");

        IProductCmptTypeAttribute typeAttribute = productCmptType.newAttribute();
        typeAttribute.setName("attrInType");
        
        // table structure usages
        ITableStructureUsage supertypeTsu = superProductCmptType.newTableStructureUsage();
        supertypeTsu.setRoleName("SupertypeTsu");
        ITableStructureUsage typeTsu = productCmptType.newTableStructureUsage();
        typeTsu.setRoleName("TypeTsu");

        // formula signatures
        IProductCmptTypeMethod supertypeSignature = superProductCmptType.newProductCmptTypeMethod();
        supertypeSignature.setFormulaSignatureDefinition(true);
        supertypeSignature.setFormulaName("CalculatePremium");
        IProductCmptTypeMethod typeSignature = productCmptType.newProductCmptTypeMethod();
        typeSignature.setFormulaSignatureDefinition(true);
        typeSignature.setFormulaName("CalculatePremium2");
        
        // default values and value sets
        IPolicyCmptType policyCmptSupertype = newPolicyCmptType(ipsProject, "SuperPolicy");
        superProductCmptType.setPolicyCmptType(policyCmptSupertype.getQualifiedName());
        policyCmptType.setSupertype(policyCmptSupertype.getQualifiedName());
        org.faktorips.devtools.core.model.pctype.IAttribute policyCmptSupertypeAttr = policyCmptSupertype.newAttribute();
        policyCmptSupertypeAttr.setName("policySuperAttr");
        org.faktorips.devtools.core.model.pctype.IAttribute policyCmptTypeAttr = policyCmptType.newAttribute();
        policyCmptTypeAttr.setName("policyAttr");
        
        assertEquals(typeAttribute, productCmptType.findProdDefProperty(ProdDefPropertyType.VALUE, typeAttribute.getName(), ipsProject));
        assertEquals(supertypeAttr, productCmptType.findProdDefProperty(ProdDefPropertyType.VALUE, supertypeAttr.getName(), ipsProject));
        assertNull(productCmptType.findProdDefProperty(ProdDefPropertyType.FORMULA, typeAttribute.getName(), ipsProject));
        
        assertEquals(typeTsu, productCmptType.findProdDefProperty(ProdDefPropertyType.TABLE_CONTENT_USAGE, typeTsu.getRoleName(), ipsProject));
        assertEquals(supertypeTsu, productCmptType.findProdDefProperty(ProdDefPropertyType.TABLE_CONTENT_USAGE, supertypeTsu.getRoleName(), ipsProject));
        assertNull(productCmptType.findProdDefProperty(ProdDefPropertyType.VALUE, typeTsu.getRoleName(), ipsProject));
        
        assertEquals(typeSignature, productCmptType.findProdDefProperty(ProdDefPropertyType.FORMULA, typeSignature.getFormulaName(), ipsProject));
        assertEquals(supertypeSignature, productCmptType.findProdDefProperty(ProdDefPropertyType.FORMULA, supertypeSignature.getFormulaName(), ipsProject));
        assertNull(productCmptType.findProdDefProperty(ProdDefPropertyType.VALUE, typeSignature.getFormulaName(), ipsProject));
        
        assertEquals(policyCmptTypeAttr, productCmptType.findProdDefProperty(ProdDefPropertyType.DEFAULT_VALUE_AND_VALUESET, policyCmptTypeAttr.getName(), ipsProject));
        assertEquals(policyCmptSupertypeAttr, productCmptType.findProdDefProperty(ProdDefPropertyType.DEFAULT_VALUE_AND_VALUESET, policyCmptSupertypeAttr.getName(), ipsProject));
        assertNull(productCmptType.findProdDefProperty(ProdDefPropertyType.VALUE, policyCmptTypeAttr.getName(), ipsProject));
    }
    
    public void testFindProdDefProperty_ByName() throws CoreException {
        IProdDefProperty[] props = productCmptType.findProdDefProperties(ipsProject);
        assertEquals(0, props.length);

        // attributes
        IProductCmptTypeAttribute supertypeAttr  = superProductCmptType.newAttribute();
        supertypeAttr.setName("attrInSupertype");
        supertypeAttr.setDatatype("Money");

        IProductCmptTypeAttribute typeAttribute = productCmptType.newAttribute();
        typeAttribute.setName("attrInType");
        
        // table structure usages
        ITableStructureUsage supertypeTsu = superProductCmptType.newTableStructureUsage();
        supertypeTsu.setRoleName("SupertypeTsu");
        ITableStructureUsage typeTsu = productCmptType.newTableStructureUsage();
        typeTsu.setRoleName("TypeTsu");

        // formula signatures
        IProductCmptTypeMethod supertypeSignature = superProductCmptType.newProductCmptTypeMethod();
        supertypeSignature.setFormulaSignatureDefinition(true);
        supertypeSignature.setFormulaName("CalculatePremium");
        IProductCmptTypeMethod typeSignature = productCmptType.newProductCmptTypeMethod();
        typeSignature.setFormulaSignatureDefinition(true);
        typeSignature.setFormulaName("CalculatePremium2");
        
        // default values and value sets
        IPolicyCmptType policyCmptSupertype = newPolicyCmptType(ipsProject, "SuperPolicy");
        superProductCmptType.setPolicyCmptType(policyCmptSupertype.getQualifiedName());
        policyCmptType.setSupertype(policyCmptSupertype.getQualifiedName());
        org.faktorips.devtools.core.model.pctype.IAttribute policyCmptSupertypeAttr = policyCmptSupertype.newAttribute();
        policyCmptSupertypeAttr.setName("policySuperAttr");
        org.faktorips.devtools.core.model.pctype.IAttribute policyCmptTypeAttr = policyCmptType.newAttribute();
        policyCmptTypeAttr.setName("policyAttr");
        
        assertEquals(typeAttribute, productCmptType.findProdDefProperty(typeAttribute.getName(), ipsProject));
        assertEquals(supertypeAttr, productCmptType.findProdDefProperty(supertypeAttr.getName(), ipsProject));
        
        assertEquals(typeTsu, productCmptType.findProdDefProperty(typeTsu.getRoleName(), ipsProject));
        assertEquals(supertypeTsu, productCmptType.findProdDefProperty(supertypeTsu.getRoleName(), ipsProject));
        
        assertEquals(typeSignature, productCmptType.findProdDefProperty(typeSignature.getFormulaName(), ipsProject));
        assertEquals(supertypeSignature, productCmptType.findProdDefProperty(supertypeSignature.getFormulaName(), ipsProject));
        
        assertEquals(policyCmptTypeAttr, productCmptType.findProdDefProperty(policyCmptTypeAttr.getName(), ipsProject));
        assertEquals(policyCmptSupertypeAttr, productCmptType.findProdDefProperty(policyCmptSupertypeAttr.getName(), ipsProject));
    }
    
    public void testFindFormulaSignature() throws CoreException {
        IProductCmptTypeMethod method1 = superSuperProductCmptType.newProductCmptTypeMethod();
        method1.setFormulaSignatureDefinition(true);
        method1.setFormulaName("Premium Calculation");
        
        assertSame(method1, superSuperProductCmptType.findFormulaSignature("Premium Calculation", ipsProject));
        assertSame(method1, productCmptType.findFormulaSignature("Premium Calculation", ipsProject));
        
        
        method1.setFormulaSignatureDefinition(false);
        assertNull(superSuperProductCmptType.findFormulaSignature("Unknown", ipsProject));
        assertNull(productCmptType.findFormulaSignature("Unknown", ipsProject));

        method1.setFormulaSignatureDefinition(false);
        assertNull(superSuperProductCmptType.findFormulaSignature("Premium Calculation", ipsProject));
        assertNull(productCmptType.findFormulaSignature("Premium Calculation", ipsProject));

        // if the method is overloaded, make sure the first one is found.
        method1.setFormulaSignatureDefinition(true);
        IProductCmptTypeMethod method2 = productCmptType.newProductCmptTypeMethod();
        method2.setFormulaSignatureDefinition(true);
        method2.setFormulaName("Premium Calculation");
        assertSame(method2, productCmptType.findFormulaSignature("Premium Calculation", ipsProject));
        
        
    }
    
    public void testValidatePolicyCmptType() throws CoreException {
        MessageList ml = productCmptType.validate();
        assertNull(ml.getMessageByCode(IProductCmptType.MSGCODE_POLICY_CMPT_TYPE_DOES_NOT_EXIST));
        
        productCmptType.setPolicyCmptType("Unknown");
        ml = productCmptType.validate();
        assertNotNull(ml.getMessageByCode(IProductCmptType.MSGCODE_POLICY_CMPT_TYPE_DOES_NOT_EXIST));
        
        productCmptType.setPolicyCmptType(superProductCmptType.getQualifiedName());
        ml = productCmptType.validate();
        assertNotNull(ml.getMessageByCode(IProductCmptType.MSGCODE_POLICY_CMPT_TYPE_DOES_NOT_EXIST));
    }
    
    public void testNewMethod() {
        IMethod method = productCmptType.newMethod();
        assertNotNull(method);
        assertEquals(productCmptType, method.getParent());
        assertEquals(1, productCmptType.getNumOfMethods());
        assertEquals(method, productCmptType.getMethods()[0]);
        assertEquals(1, productCmptType.getMethods().length);
        assertEquals(1, productCmptType.getProductCmptTypeMethods().length);
    }    
    
    public void testNewProductCmptTypeMethod() {
        IProductCmptTypeMethod method = productCmptType.newProductCmptTypeMethod();
        assertNotNull(method);
        assertEquals(productCmptType, method.getParent());
        assertEquals(1, productCmptType.getNumOfMethods());
        assertEquals(method, productCmptType.getMethods()[0]);
        assertEquals(1, productCmptType.getMethods().length);
        assertEquals(1, productCmptType.getProductCmptTypeMethods().length);
    }
    
    public void testNewTableStructureUsage() {
        ITableStructureUsage tsu = productCmptType.newTableStructureUsage();
        assertNotNull(tsu);
        assertEquals(productCmptType, tsu.getParent());
        assertEquals(1, productCmptType.getNumOfTableStructureUsages());
        assertEquals(tsu, productCmptType.getTableStructureUsages()[0]);
    }
    
    public void testFindSupertype() throws CoreException {
        assertEquals(superProductCmptType, productCmptType.findSupertype(ipsProject));
        assertNull(superSuperProductCmptType.findSupertype(ipsProject));
        productCmptType.setSupertype("unknownType");
        assertNull(productCmptType.findSupertype(ipsProject));
    }
    
    public void testFindTableStructureUsageInSupertypeHierarchy() throws CoreException {
        assertNull(superSuperProductCmptType.findTableStructureUsage(null, ipsProject));
        assertNull(superSuperProductCmptType.findTableStructureUsage("someRole", ipsProject));
        
        assertNull(productCmptType.findTableStructureUsage("someRole", ipsProject));

        ITableStructureUsage tsu1 = productCmptType.newTableStructureUsage();
        tsu1.setRoleName("role1");
        assertEquals(tsu1, productCmptType.findTableStructureUsage("role1", ipsProject));
        assertNull(productCmptType.findTableStructureUsage("unkownRole", ipsProject));
        
        ITableStructureUsage tsu2 = superSuperProductCmptType.newTableStructureUsage();
        tsu2.setRoleName("role2");
        assertEquals(tsu2, productCmptType.findTableStructureUsage("role2", ipsProject));

        tsu2.setRoleName("role1");
        assertEquals(tsu1, productCmptType.findTableStructureUsage("role1", ipsProject));
        
    }
    
    public void testNewMemento() {
        Memento memento = productCmptType.newMemento();
        assertNotNull(memento);
        
        productCmptType.newAttribute();
        memento = productCmptType.newMemento();
        assertNotNull(memento);
    }

    public void testSetPolicyCmptType() {
        productCmptType.setPolicyCmptType("NewType");
        assertEquals("NewType", productCmptType.getPolicyCmptType());
        assertEquals(productCmptType.getIpsSrcFile(), lastEvent.getIpsSrcFile());
    }
    
    public void testConfiguresPolicyCmptType() {
        productCmptType.setPolicyCmptType(null);
        assertFalse(productCmptType.isConfigurationForPolicyCmptType());
       
        productCmptType.setPolicyCmptType("");
        assertFalse(productCmptType.isConfigurationForPolicyCmptType());
        
        productCmptType.setPolicyCmptType("NewType");
        assertTrue(productCmptType.isConfigurationForPolicyCmptType());
    }

    public void testFindPolicyCmptType() throws CoreException {
        productCmptType.setPolicyCmptType("");
        assertNull(productCmptType.findPolicyCmptType(true, ipsProject));
        assertNull(productCmptType.findPolicyCmptType(false, ipsProject));
        
        productCmptType.setPolicyCmptType("UnknownType");
        assertNull(productCmptType.findPolicyCmptType(false, ipsProject));
        assertNull(productCmptType.findPolicyCmptType(true, ipsProject));

        productCmptType.setPolicyCmptType("Policy");
        assertEquals(policyCmptType, productCmptType.findPolicyCmptType(false, ipsProject));
        assertEquals(policyCmptType, productCmptType.findPolicyCmptType(true, ipsProject));
        
        productCmptType.setPolicyCmptType("");
        superProductCmptType.setPolicyCmptType("Policy");
        assertNull(productCmptType.findPolicyCmptType(false, ipsProject));
        assertEquals(policyCmptType, productCmptType.findPolicyCmptType(true, ipsProject));
        
        productCmptType.setPolicyCmptType("Unkown");
        assertNull(productCmptType.findPolicyCmptType(false, ipsProject));
        assertNull(productCmptType.findPolicyCmptType(true, ipsProject));
    }

    public void testNewAttribute() {
        IProductCmptTypeAttribute a1 = productCmptType.newAttribute();
        assertEquals(1, productCmptType.getNumOfAttributes());
        assertEquals(a1, productCmptType.getAttributes()[0]);
        assertEquals(productCmptType, a1.getProductCmptType());
        
        assertEquals(a1, lastEvent.getPart());
    }

    public void testGetAttribute() {
        assertNull(productCmptType.getAttribute("a"));
        
        IProductCmptTypeAttribute a1 = productCmptType.newAttribute();
        productCmptType.newAttribute();
        IProductCmptTypeAttribute a3 = productCmptType.newAttribute();
        a1.setName("a1");
        a3.setName("a3");
        
        assertEquals(a1, productCmptType.getAttribute("a1"));
        assertEquals(a3, productCmptType.getAttribute("a3"));
        assertNull(productCmptType.getAttribute("unkown"));
        
        assertNull(productCmptType.getAttribute(null));
    }

    public void testGetAttributes() {
        assertEquals(0, productCmptType.getAttributes().length);

        IProductCmptTypeAttribute a1 = productCmptType.newAttribute();
        IProductCmptTypeAttribute[] attributes = productCmptType.getAttributes();
        assertEquals(a1, attributes[0]);
        
        IProductCmptTypeAttribute a2 = productCmptType.newAttribute();
        attributes = productCmptType.getAttributes();
        assertEquals(a1, attributes[0]);
        assertEquals(a2, attributes[1]);
    }

    public void testGetNumOfAttributes() {
        assertEquals(0, productCmptType.getNumOfAttributes());
        
        productCmptType.newAttribute();
        assertEquals(1, productCmptType.getNumOfAttributes());
        
        productCmptType.newAttribute();
        assertEquals(2, productCmptType.getNumOfAttributes());
    }

    public void testMoveAttributes() {
        IProductCmptTypeAttribute a1 = productCmptType.newAttribute();
        IProductCmptTypeAttribute a2 = productCmptType.newAttribute();
        IProductCmptTypeAttribute a3 = productCmptType.newAttribute();
        
        productCmptType.moveAttributes(new int[]{1, 2}, true);
        IProductCmptTypeAttribute[] attributes = productCmptType.getAttributes();
        assertEquals(a2, attributes[0]);
        assertEquals(a3, attributes[1]);
        assertEquals(a1, attributes[2]);
        
        assertTrue(lastEvent.isAffected(a1));
        assertTrue(lastEvent.isAffected(a2));
        assertTrue(lastEvent.isAffected(a3));
    }
    
    public void testInitFromXml() {
        Element rootEl = getTestDocument().getDocumentElement();
        productCmptType.setPolicyCmptType("Bla");

        productCmptType.initFromXml(XmlUtil.getElement(rootEl, 0));
        assertEquals("Policy", productCmptType.getPolicyCmptType());
        assertEquals(1, productCmptType.getNumOfAttributes());
        assertEquals(1, productCmptType.getNumOfAssociations());
        assertEquals(1, productCmptType.getNumOfTableStructureUsages());
        assertEquals(1, productCmptType.getNumOfMethods());
    }
    
    public void testToXml() throws CoreException {
        productCmptType.setPolicyCmptType(policyCmptType.getQualifiedName());
        productCmptType.newAttribute().setName("attr");
        productCmptType.newAssociation().setTargetRoleSingular("role");
        productCmptType.newTableStructureUsage().setRoleName("roleTsu");
        productCmptType.newMethod().setName("method1");
        
        Element el = productCmptType.toXml(newDocument());
        productCmptType = newProductCmptType(ipsProject, "Copy");
        productCmptType.initFromXml(el);
        
        assertEquals(policyCmptType.getQualifiedName(), productCmptType.getPolicyCmptType());
        assertEquals(1, productCmptType.getNumOfAttributes());
        assertEquals(1, productCmptType.getNumOfAssociations());
        assertEquals(1, productCmptType.getNumOfTableStructureUsages());
        assertEquals(1, productCmptType.getNumOfMethods());
    }
    
    /**
     * {@inheritDoc}
     */
    public void contentsChanged(ContentChangeEvent event) {
        lastEvent = event;
    }

}
