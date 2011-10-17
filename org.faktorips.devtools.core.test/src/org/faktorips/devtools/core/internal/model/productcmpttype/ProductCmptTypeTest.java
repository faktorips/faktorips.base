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

package org.faktorips.devtools.core.internal.model.productcmpttype;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractDependencyTest;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsObjectPath;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsProjectRefEntry;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.internal.model.productcmpt.ProductCmpt;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.ContentsChangeListener;
import org.faktorips.devtools.core.model.DatatypeDependency;
import org.faktorips.devtools.core.model.DependencyType;
import org.faktorips.devtools.core.model.IDependency;
import org.faktorips.devtools.core.model.IpsObjectDependency;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsobject.Modifier;
import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPathEntry;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.AttributeType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptCategory;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.core.model.productcmpttype.ITableStructureUsage;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.core.model.type.IMethod;
import org.faktorips.devtools.core.model.type.IProductCmptProperty;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.model.type.ProductCmptPropertyType;
import org.faktorips.devtools.core.util.XmlUtil;
import org.faktorips.util.memento.Memento;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;

/**
 * 
 * @author Jan Ortmann
 */
public class ProductCmptTypeTest extends AbstractDependencyTest implements ContentsChangeListener {

    private ContentChangeEvent lastEvent;
    private IIpsProject ipsProject;
    private IPolicyCmptType policyCmptType;
    private IProductCmptType productCmptType;
    private IProductCmptType superProductCmptType;
    private IProductCmptType superSuperProductCmptType;

    @Override
    @Before
    public void setUp() throws Exception {
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

    @Override
    protected void tearDownExtension() throws Exception {
        ipsProject.getIpsModel().removeChangeListener(this);
    }

    @Test
    public void testValidateMustHaveSupertype() throws CoreException {
        PolicyCmptType superPolicyCmptType = newPolicyCmptType(ipsProject, "SuperPolicy");
        policyCmptType.setSupertype(superPolicyCmptType.getQualifiedName());
        superPolicyCmptType.setProductCmptType(superProductCmptType.getQualifiedName());
        superProductCmptType.setPolicyCmptType(superPolicyCmptType.getQualifiedName());

        productCmptType.setSupertype("");
        MessageList result = productCmptType.validate(ipsProject);
        assertNotNull(result.getMessageByCode(IProductCmptType.MSGCODE_MUST_HAVE_SUPERTYPE));

        productCmptType.setSupertype(superProductCmptType.getQualifiedName());
        result = productCmptType.validate(ipsProject);
        assertNull(result.getMessageByCode(IProductCmptType.MSGCODE_MUST_HAVE_SUPERTYPE));
    }

    @Test
    public void testValidateDuplicateFormulaName() throws CoreException {
        productCmptType.newFormulaSignature("formula");

        // formula in same type
        IProductCmptTypeMethod formula1 = productCmptType.newFormulaSignature("formula");
        MessageList result = productCmptType.validate(ipsProject);
        assertNotNull(result.getMessageByCode(IProductCmptType.MSGCODE_DUPLICATE_FORMULAS_NOT_ALLOWED_IN_SAME_TYPE));

        formula1.setFormulaName("formula1");
        result = productCmptType.validate(ipsProject);
        assertNull(result.getMessageByCode(IProductCmptType.MSGCODE_DUPLICATE_FORMULAS_NOT_ALLOWED_IN_SAME_TYPE));

        // formula in supertype
        IProductCmptTypeMethod formula2 = superProductCmptType.newFormulaSignature("formula");
        result = productCmptType.validate(ipsProject);
        assertNotNull(result.getMessageByCode(IProductCmptType.MSGCODE_DUPLICATE_FORMULA_NAME_IN_HIERARCHY));

        formula2.setFormulaName("formula2");
        result = productCmptType.validate(ipsProject);
        assertNull(result.getMessageByCode(IProductCmptType.MSGCODE_DUPLICATE_FORMULA_NAME_IN_HIERARCHY));
    }

    @Test
    public void testValidate_PolicyCmptTypeDoesNotSpecifyThisOneAsConfigurationType() throws CoreException {
        MessageList result = productCmptType.validate(ipsProject);
        assertNull(result.getMessageByCode(IProductCmptType.MSGCODE_POLICY_CMPT_TYPE_DOES_NOT_SPECIFY_THIS_TYPE));

        policyCmptType.setProductCmptType("OtherType");
        result = productCmptType.validate(ipsProject);
        assertNotNull(result.getMessageByCode(IProductCmptType.MSGCODE_POLICY_CMPT_TYPE_DOES_NOT_SPECIFY_THIS_TYPE));

        policyCmptType.setProductCmptType(superProductCmptType.getQualifiedName());
        superProductCmptType.setPolicyCmptType(policyCmptType.getQualifiedName());
        result = productCmptType.validate(ipsProject);
        assertNull(result.getMessageByCode(IProductCmptType.MSGCODE_POLICY_CMPT_TYPE_DOES_NOT_SPECIFY_THIS_TYPE));
    }

    @Test
    public void testValidate_PolicyCmptTypeNotMarkedAsConfigurable() throws CoreException {
        MessageList result = productCmptType.validate(ipsProject);
        assertNull(result.getMessageByCode(IProductCmptType.MSGCODE_POLICY_CMPT_TYPE_IS_NOT_MARKED_AS_CONFIGURABLE));

        policyCmptType.setConfigurableByProductCmptType(false);
        result = productCmptType.validate(ipsProject);
        assertNotNull(result.getMessageByCode(IProductCmptType.MSGCODE_POLICY_CMPT_TYPE_IS_NOT_MARKED_AS_CONFIGURABLE));
    }

    /**
     * Additional, product component type specific tests
     */
    @Test
    public void testValidate_DuplicatePropertyName() throws CoreException {
        IAttribute attr1 = productCmptType.newAttribute();
        attr1.setName("property");

        // table structure usage in same type
        ITableStructureUsage tsu1 = productCmptType.newTableStructureUsage();
        tsu1.setRoleName("property");
        MessageList result = productCmptType.validate(ipsProject);
        assertNotNull(result.getMessageByCode(IType.MSGCODE_DUPLICATE_PROPERTY_NAME));

        tsu1.setRoleName("table1");
        result = productCmptType.validate(ipsProject);
        assertNull(result.getMessageByCode(IType.MSGCODE_DUPLICATE_PROPERTY_NAME));

        // table structure usage in supertype
        ITableStructureUsage tsu2 = superProductCmptType.newTableStructureUsage();
        tsu2.setRoleName("property");
        result = productCmptType.validate(ipsProject);
        assertNotNull(result.getMessageByCode(IType.MSGCODE_DUPLICATE_PROPERTY_NAME));

        tsu2.setRoleName("table2");
        result = productCmptType.validate(ipsProject);
        assertNull(result.getMessageByCode(IType.MSGCODE_DUPLICATE_PROPERTY_NAME));
    }

    @Test
    public void testValidateMustHaveSameValueForConfigurationForPolicyCmptType() throws CoreException {
        productCmptType.setConfigurationForPolicyCmptType(true);
        superProductCmptType.setConfigurationForPolicyCmptType(true);
        MessageList result = productCmptType.validate(ipsProject);
        assertNull(result
                .getMessageByCode(IProductCmptType.MSGCODE_MUST_HAVE_SAME_VALUE_FOR_CONFIGURES_POLICY_CMPT_TYPE));

        productCmptType.setConfigurationForPolicyCmptType(false);
        result = productCmptType.validate(ipsProject);
        assertNotNull(result
                .getMessageByCode(IProductCmptType.MSGCODE_MUST_HAVE_SAME_VALUE_FOR_CONFIGURES_POLICY_CMPT_TYPE));

        superProductCmptType.setConfigurationForPolicyCmptType(false);
        result = productCmptType.validate(ipsProject);
        assertNull(result
                .getMessageByCode(IProductCmptType.MSGCODE_MUST_HAVE_SAME_VALUE_FOR_CONFIGURES_POLICY_CMPT_TYPE));

        productCmptType.setConfigurationForPolicyCmptType(false);
        result = productCmptType.validate(ipsProject);
        assertNull(result
                .getMessageByCode(IProductCmptType.MSGCODE_MUST_HAVE_SAME_VALUE_FOR_CONFIGURES_POLICY_CMPT_TYPE));

        superProductCmptType.setConfigurationForPolicyCmptType(false);
        result = productCmptType.validate(ipsProject);
        assertNull(result
                .getMessageByCode(IProductCmptType.MSGCODE_MUST_HAVE_SAME_VALUE_FOR_CONFIGURES_POLICY_CMPT_TYPE));
    }

    @Test
    public void testValidateTypeHierarchyMismatch() throws CoreException {
        IPolicyCmptType superPolicyCmptType = newPolicyCmptType(ipsProject, "SuperPolicy");
        policyCmptType.setSupertype(superPolicyCmptType.getQualifiedName());

        superProductCmptType.setConfigurationForPolicyCmptType(true);
        superProductCmptType.setPolicyCmptType(superPolicyCmptType.getQualifiedName());
        MessageList result = productCmptType.validate(ipsProject);
        assertNull(result.getMessageByCode(IProductCmptType.MSGCODE_HIERARCHY_MISMATCH));

        superProductCmptType.setPolicyCmptType(policyCmptType.getQualifiedName());
        superSuperProductCmptType.setConfigurationForPolicyCmptType(true);
        superSuperProductCmptType.setPolicyCmptType(superPolicyCmptType.getQualifiedName());
        result = productCmptType.validate(ipsProject);
        assertNull(result.getMessageByCode(IProductCmptType.MSGCODE_HIERARCHY_MISMATCH));

        // policy component type inherits from a type outside the hierarchy
        IPolicyCmptType otherType = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "SomeOtherType");
        superProductCmptType.setPolicyCmptType(otherType.getQualifiedName());
        result = productCmptType.validate(ipsProject);
        assertNotNull(result.getMessageByCode(IProductCmptType.MSGCODE_HIERARCHY_MISMATCH));

        // an intermediate type exists in the policy hierarchy
        IPolicyCmptType intermediateType = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "IntermediatePolicy");
        policyCmptType.setSupertype(intermediateType.getQualifiedName());
        intermediateType.setSupertype(superPolicyCmptType.getQualifiedName());
        superProductCmptType.setPolicyCmptType(superPolicyCmptType.getQualifiedName());
        result = productCmptType.validate(ipsProject);
        assertNotNull(result.getMessageByCode(IProductCmptType.MSGCODE_HIERARCHY_MISMATCH));
    }

    @Test
    public void testSetConfigurationForPolicyCmptType() {
        Boolean newValue = Boolean.valueOf(!productCmptType.isConfigurationForPolicyCmptType());
        testPropertyAccessReadWrite(IProductCmptType.class,
                IProductCmptType.PROPERTY_CONFIGURATION_FOR_POLICY_CMPT_TYPE, productCmptType, newValue);
    }

    @Test
    public void testFindProdDefProperties() throws CoreException {
        List<IProductCmptProperty> props = productCmptType.findProductCmptProperties(ipsProject);
        assertEquals(0, props.size());

        // attributes
        IProductCmptTypeAttribute supertypeAttr = superProductCmptType.newProductCmptTypeAttribute();
        supertypeAttr.setName("attrInSupertype");
        supertypeAttr.setDatatype("Money");

        IProductCmptTypeAttribute typeAttribute1 = productCmptType.newProductCmptTypeAttribute("attrInType1");
        IProductCmptTypeAttribute typeAttribute2 = productCmptType.newProductCmptTypeAttribute("attrInType2");

        props = superProductCmptType.findProductCmptProperties(ipsProject);
        assertEquals(1, props.size());
        assertEquals(supertypeAttr, props.get(0));
        props = productCmptType.findProductCmptProperties(ipsProject);
        assertEquals(3, props.size());
        assertEquals(supertypeAttr, props.get(0));
        assertEquals(typeAttribute1, props.get(1));
        assertEquals(typeAttribute2, props.get(2));

        // table structure usages
        ITableStructureUsage supertypeTsu = superProductCmptType.newTableStructureUsage();
        ITableStructureUsage typeTsu1 = productCmptType.newTableStructureUsage();
        ITableStructureUsage typeTsu2 = productCmptType.newTableStructureUsage();

        props = superProductCmptType.findProductCmptProperties(ipsProject);
        assertEquals(2, props.size());
        assertEquals(supertypeAttr, props.get(0));
        assertEquals(supertypeTsu, props.get(1));
        props = productCmptType.findProductCmptProperties(ipsProject);
        assertEquals(6, props.size());
        assertEquals(supertypeAttr, props.get(0));
        assertEquals(typeAttribute1, props.get(1));
        assertEquals(typeAttribute2, props.get(2));
        assertEquals(supertypeTsu, props.get(3));
        assertEquals(typeTsu1, props.get(4));
        assertEquals(typeTsu2, props.get(5));

        // formula signatures
        IProductCmptTypeMethod supertypeSignature = superProductCmptType.newProductCmptTypeMethod();
        supertypeSignature.setFormulaSignatureDefinition(true);
        supertypeSignature.setFormulaName("CalculatePremium");
        IProductCmptTypeMethod typeSignature1 = productCmptType.newFormulaSignature("CalculatePremium1");
        IProductCmptTypeMethod typeSignature2 = productCmptType.newFormulaSignature("CalculatePremium2");
        productCmptType.newProductCmptTypeMethod().setFormulaSignatureDefinition(false);// this
        // method is not a product def property as it is not a formula signature

        props = superProductCmptType.findProductCmptProperties(ipsProject);
        assertEquals(3, props.size());
        assertEquals(supertypeAttr, props.get(0));
        assertEquals(supertypeTsu, props.get(1));
        assertEquals(supertypeSignature, props.get(2));

        props = productCmptType.findProductCmptProperties(ipsProject);
        assertEquals(9, props.size());
        assertEquals(supertypeAttr, props.get(0));
        assertEquals(typeAttribute1, props.get(1));
        assertEquals(typeAttribute2, props.get(2));
        assertEquals(supertypeTsu, props.get(3));
        assertEquals(typeTsu1, props.get(4));
        assertEquals(typeTsu2, props.get(5));
        assertEquals(supertypeSignature, props.get(6));
        assertEquals(typeSignature1, props.get(7));
        assertEquals(typeSignature2, props.get(8));

        // default values and value sets
        IPolicyCmptType policyCmptSupertype = newPolicyCmptType(ipsProject, "SuperPolicy");
        superProductCmptType.setPolicyCmptType(policyCmptSupertype.getQualifiedName());
        IPolicyCmptTypeAttribute policyCmptSupertypeAttr = policyCmptSupertype.newPolicyCmptTypeAttribute();
        policyCmptSupertypeAttr.setProductRelevant(true);
        IPolicyCmptTypeAttribute policyCmptTypeAttr1 = policyCmptType.newPolicyCmptTypeAttribute();
        policyCmptTypeAttr1.setProductRelevant(true);
        IPolicyCmptTypeAttribute policyCmptTypeAttr2 = policyCmptType.newPolicyCmptTypeAttribute();
        policyCmptTypeAttr2.setProductRelevant(true);
        policyCmptType.newPolicyCmptTypeAttribute().setProductRelevant(false); // this attribute is
        // not a product def
        // property as it is
        // not product
        // relevant!
        IPolicyCmptTypeAttribute derivedAttr = policyCmptType.newPolicyCmptTypeAttribute();
        derivedAttr.setProductRelevant(true);
        derivedAttr.setAttributeType(AttributeType.DERIVED_ON_THE_FLY);

        props = superProductCmptType.findProductCmptProperties(ipsProject);
        assertEquals(4, props.size());
        assertEquals(supertypeAttr, props.get(0));
        assertEquals(supertypeTsu, props.get(1));
        assertEquals(supertypeSignature, props.get(2));
        assertEquals(policyCmptSupertypeAttr, props.get(3));

        props = productCmptType.findProductCmptProperties(ipsProject);
        assertEquals(12, props.size());
        assertEquals(supertypeAttr, props.get(0));
        assertEquals(typeAttribute1, props.get(1));
        assertEquals(typeAttribute2, props.get(2));
        assertEquals(supertypeTsu, props.get(3));
        assertEquals(typeTsu1, props.get(4));
        assertEquals(typeTsu2, props.get(5));
        assertEquals(supertypeSignature, props.get(6));
        assertEquals(typeSignature1, props.get(7));
        assertEquals(typeSignature2, props.get(8));
        assertEquals(policyCmptSupertypeAttr, props.get(9));
        assertEquals(policyCmptTypeAttr1, props.get(10));
        assertEquals(policyCmptTypeAttr2, props.get(11));
    }

    @Test
    public void testFindProdDefProperties_TwoProductCmptTypesConfigureSamePolicyCmptType() throws CoreException {
        IPolicyCmptTypeAttribute policyCmptTypeAttr1 = policyCmptType.newPolicyCmptTypeAttribute();
        policyCmptTypeAttr1.setProductRelevant(true);
        List<IProductCmptProperty> props = productCmptType.findProductCmptProperties(ipsProject);
        assertEquals(1, props.size()); // make sure, setup is ok

        IProductCmptType subtype = newProductCmptType(productCmptType, "Subtype");
        subtype.setConfigurationForPolicyCmptType(true);
        subtype.setPolicyCmptType(policyCmptType.getQualifiedName());
        props = subtype.findProductCmptProperties(ipsProject);
        assertEquals(1, props.size()); // attribute mustn't be inluded twice!!!
    }

    @Test
    public void testGetProdDefPropertiesMap() throws CoreException {
        // attributes
        IProductCmptTypeAttribute supertypeAttr = superProductCmptType.newProductCmptTypeAttribute();
        supertypeAttr.setName("attrInSupertype");
        supertypeAttr.setDatatype("Money");

        IProductCmptTypeAttribute typeAttribute = productCmptType.newProductCmptTypeAttribute();
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
        productCmptType.newProductCmptTypeMethod().setFormulaSignatureDefinition(false);// this
        // method is not a product def property as it is not a formula signature

        // default values and value sets
        IPolicyCmptType policyCmptSupertype = newPolicyCmptType(ipsProject, "SuperPolicy");
        superProductCmptType.setPolicyCmptType(policyCmptSupertype.getQualifiedName());
        org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute policyCmptSupertypeAttr = policyCmptSupertype
                .newPolicyCmptTypeAttribute();
        policyCmptSupertypeAttr.setName("PolicySuperAttribute");
        policyCmptSupertypeAttr.setProductRelevant(true);
        org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute policyCmptTypeAttr = policyCmptType
                .newPolicyCmptTypeAttribute();
        policyCmptTypeAttr.setName("PolicyAttribute");
        policyCmptTypeAttr.setProductRelevant(true);
        policyCmptType.newPolicyCmptTypeAttribute().setProductRelevant(false); // this attribute is
        // not a product def
        // property as it is
        // not product
        // relevant!

        // test property type = null
        Map<String, IProductCmptProperty> propertyMap = ((ProductCmptType)productCmptType).getProductCpmtPropertyMap(
                null, ipsProject);
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
        propertyMap = ((ProductCmptType)productCmptType).getProductCpmtPropertyMap(
                ProductCmptPropertyType.PRODUCT_CMPT_TYPE_ATTRIBUTE, ipsProject);
        assertEquals(2, propertyMap.size());
        assertEquals(supertypeAttr, propertyMap.get(supertypeAttr.getPropertyName()));
        assertEquals(typeAttribute, propertyMap.get(typeAttribute.getPropertyName()));

        propertyMap = ((ProductCmptType)productCmptType).getProductCpmtPropertyMap(
                ProductCmptPropertyType.TABLE_STRUCTURE_USAGE, ipsProject);
        assertEquals(2, propertyMap.size());
        assertEquals(supertypeTsu, propertyMap.get(supertypeTsu.getPropertyName()));
        assertEquals(typeTsu, propertyMap.get(typeTsu.getPropertyName()));

        propertyMap = ((ProductCmptType)productCmptType).getProductCpmtPropertyMap(
                ProductCmptPropertyType.FORMULA_SIGNATURE_DEFINITION, ipsProject);
        assertEquals(2, propertyMap.size());
        assertEquals(supertypeSignature, propertyMap.get(supertypeSignature.getPropertyName()));
        assertEquals(typeSignature, propertyMap.get(typeSignature.getPropertyName()));

        propertyMap = ((ProductCmptType)productCmptType).getProductCpmtPropertyMap(
                ProductCmptPropertyType.POLICY_CMPT_TYPE_ATTRIBUTE, ipsProject);
        assertEquals(2, propertyMap.size());
        assertEquals(policyCmptSupertypeAttr, propertyMap.get(policyCmptSupertypeAttr.getPropertyName()));
        assertEquals(policyCmptTypeAttr, propertyMap.get(policyCmptTypeAttr.getPropertyName()));

        // test if two product component types configure the same policy component type, that the
        // properties defined by the
        // policy component type aren't considered twice.
        IProductCmptType subtype = newProductCmptType(productCmptType, "Subtype");
        subtype.setPolicyCmptType(policyCmptType.getQualifiedName());
        propertyMap = ((ProductCmptType)subtype).getProductCpmtPropertyMap(
                ProductCmptPropertyType.POLICY_CMPT_TYPE_ATTRIBUTE, ipsProject);
        assertEquals(2, propertyMap.size());
        assertEquals(policyCmptSupertypeAttr, propertyMap.get(policyCmptSupertypeAttr.getPropertyName()));
        assertEquals(policyCmptTypeAttr, propertyMap.get(policyCmptTypeAttr.getPropertyName()));
    }

    @Test
    public void testFindProdDefProperty_ByTypeAndName() throws CoreException {
        // attributes
        IProductCmptTypeAttribute supertypeAttr = superProductCmptType.newProductCmptTypeAttribute();
        supertypeAttr.setName("attrInSupertype");
        supertypeAttr.setDatatype("Money");

        IProductCmptTypeAttribute typeAttribute = productCmptType.newProductCmptTypeAttribute();
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
        org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute policyCmptSupertypeAttr = policyCmptSupertype
                .newPolicyCmptTypeAttribute();
        policyCmptSupertypeAttr.setName("policySuperAttr");
        policyCmptSupertypeAttr.setProductRelevant(true);
        org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute policyCmptTypeAttr = policyCmptType
                .newPolicyCmptTypeAttribute();
        policyCmptTypeAttr.setName("policyAttr");
        policyCmptTypeAttr.setProductRelevant(true);

        assertEquals(typeAttribute, productCmptType.findProductCmptProperty(
                ProductCmptPropertyType.PRODUCT_CMPT_TYPE_ATTRIBUTE, typeAttribute.getName(), ipsProject));
        assertEquals(supertypeAttr, productCmptType.findProductCmptProperty(
                ProductCmptPropertyType.PRODUCT_CMPT_TYPE_ATTRIBUTE, supertypeAttr.getName(), ipsProject));
        assertNull(productCmptType.findProductCmptProperty(ProductCmptPropertyType.FORMULA_SIGNATURE_DEFINITION,
                typeAttribute.getName(), ipsProject));

        assertEquals(
                typeTsu,
                productCmptType.findProductCmptProperty(ProductCmptPropertyType.TABLE_STRUCTURE_USAGE,
                        typeTsu.getRoleName(), ipsProject));
        assertEquals(
                supertypeTsu,
                productCmptType.findProductCmptProperty(ProductCmptPropertyType.TABLE_STRUCTURE_USAGE,
                        supertypeTsu.getRoleName(), ipsProject));
        assertNull(productCmptType.findProductCmptProperty(ProductCmptPropertyType.PRODUCT_CMPT_TYPE_ATTRIBUTE,
                typeTsu.getRoleName(), ipsProject));

        assertEquals(typeSignature, productCmptType.findProductCmptProperty(
                ProductCmptPropertyType.FORMULA_SIGNATURE_DEFINITION, typeSignature.getFormulaName(), ipsProject));
        assertEquals(supertypeSignature, productCmptType.findProductCmptProperty(
                ProductCmptPropertyType.FORMULA_SIGNATURE_DEFINITION, supertypeSignature.getFormulaName(), ipsProject));
        assertNull(productCmptType.findProductCmptProperty(ProductCmptPropertyType.PRODUCT_CMPT_TYPE_ATTRIBUTE,
                typeSignature.getFormulaName(), ipsProject));

        assertEquals(policyCmptTypeAttr, productCmptType.findProductCmptProperty(
                ProductCmptPropertyType.POLICY_CMPT_TYPE_ATTRIBUTE, policyCmptTypeAttr.getName(), ipsProject));
        assertEquals(policyCmptSupertypeAttr, productCmptType.findProductCmptProperty(
                ProductCmptPropertyType.POLICY_CMPT_TYPE_ATTRIBUTE, policyCmptSupertypeAttr.getName(), ipsProject));
        assertNull(productCmptType.findProductCmptProperty(ProductCmptPropertyType.PRODUCT_CMPT_TYPE_ATTRIBUTE,
                policyCmptTypeAttr.getName(), ipsProject));

        productCmptType.setPolicyCmptType("");
        assertNull(productCmptType.findProductCmptProperty(ProductCmptPropertyType.POLICY_CMPT_TYPE_ATTRIBUTE,
                policyCmptTypeAttr.getName(), ipsProject));
    }

    @Test
    public void testFindProdDefProperty_ByName() throws CoreException {
        List<IProductCmptProperty> props = productCmptType.findProductCmptProperties(ipsProject);
        assertEquals(0, props.size());

        // attributes
        IProductCmptTypeAttribute supertypeAttr = superProductCmptType.newProductCmptTypeAttribute();
        supertypeAttr.setName("attrInSupertype");
        supertypeAttr.setDatatype("Money");

        IProductCmptTypeAttribute typeAttribute = productCmptType.newProductCmptTypeAttribute();
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
        org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute policyCmptSupertypeAttr = policyCmptSupertype
                .newPolicyCmptTypeAttribute();
        policyCmptSupertypeAttr.setName("policySuperAttr");
        policyCmptSupertypeAttr.setProductRelevant(true);
        org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute policyCmptTypeAttr = policyCmptType
                .newPolicyCmptTypeAttribute();
        policyCmptTypeAttr.setName("policyAttr");
        policyCmptTypeAttr.setProductRelevant(true);

        assertEquals(typeAttribute, productCmptType.findProductCmptProperty(typeAttribute.getName(), ipsProject));
        assertEquals(supertypeAttr, productCmptType.findProductCmptProperty(supertypeAttr.getName(), ipsProject));

        assertEquals(typeTsu, productCmptType.findProductCmptProperty(typeTsu.getRoleName(), ipsProject));
        assertEquals(supertypeTsu, productCmptType.findProductCmptProperty(supertypeTsu.getRoleName(), ipsProject));

        assertEquals(typeSignature, productCmptType.findProductCmptProperty(typeSignature.getFormulaName(), ipsProject));
        assertEquals(supertypeSignature,
                productCmptType.findProductCmptProperty(supertypeSignature.getFormulaName(), ipsProject));

        assertEquals(policyCmptTypeAttr,
                productCmptType.findProductCmptProperty(policyCmptTypeAttr.getName(), ipsProject));
        assertEquals(policyCmptSupertypeAttr,
                productCmptType.findProductCmptProperty(policyCmptSupertypeAttr.getName(), ipsProject));

        policyCmptTypeAttr.setProductRelevant(false);
        assertNull(productCmptType.findProductCmptProperty(policyCmptTypeAttr.getName(), ipsProject));
    }

    @Test
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

    @Test
    public void testValidatePolicyCmptType() throws CoreException {
        MessageList ml = productCmptType.validate(ipsProject);
        assertNull(ml.getMessageByCode(IProductCmptType.MSGCODE_POLICY_CMPT_TYPE_DOES_NOT_EXIST));

        productCmptType.setPolicyCmptType("Unknown");
        ml = productCmptType.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IProductCmptType.MSGCODE_POLICY_CMPT_TYPE_DOES_NOT_EXIST));

        productCmptType.setPolicyCmptType(superProductCmptType.getQualifiedName());
        ml = productCmptType.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IProductCmptType.MSGCODE_POLICY_CMPT_TYPE_DOES_NOT_EXIST));
    }

    @Test
    public void testNewMethod() {
        IMethod method = productCmptType.newMethod();
        assertNotNull(method);
        assertEquals(productCmptType, method.getParent());
        assertEquals(1, productCmptType.getNumOfMethods());
        assertEquals(method, productCmptType.getMethods().get(0));
        assertEquals(1, productCmptType.getMethods().size());
        assertEquals(1, productCmptType.getProductCmptTypeMethods().size());
    }

    @Test
    public void testNewProductCmptCategory() {
        IProductCmptCategory category = productCmptType.newProductCmptCategory();
        assertNotNull(category);
        assertEquals(productCmptType, category.getParent());
    }

    @Test
    public void testNewProductCmptCategoryWithName() {
        IProductCmptCategory category = productCmptType.newProductCmptCategory("foo");
        assertEquals("foo", category.getName());
    }

    @Test
    public void testNewProductCmptTypeMethod() {
        IProductCmptTypeMethod method = productCmptType.newProductCmptTypeMethod();
        assertNotNull(method);
        assertEquals(productCmptType, method.getParent());
        assertEquals(1, productCmptType.getNumOfMethods());
        assertEquals(method, productCmptType.getMethods().get(0));
        assertEquals(1, productCmptType.getMethods().size());
        assertEquals(1, productCmptType.getProductCmptTypeMethods().size());
    }

    @Test
    public void testNewTableStructureUsage() {
        ITableStructureUsage tsu = productCmptType.newTableStructureUsage();
        assertNotNull(tsu);
        assertEquals(productCmptType, tsu.getParent());
        assertEquals(1, productCmptType.getNumOfTableStructureUsages());
        assertEquals(tsu, productCmptType.getTableStructureUsages().get(0));
    }

    @Test
    public void testNewFormulaSignature() {
        IProductCmptTypeMethod formula = productCmptType.newFormulaSignature("premium");
        assertEquals("premium", formula.getFormulaName());
        assertEquals(formula.getDefaultMethodName(), formula.getName());
        assertTrue(formula.isFormulaSignatureDefinition());
    }

    @Test
    public void testFindSupertype() throws CoreException {
        assertEquals(superProductCmptType, productCmptType.findSupertype(ipsProject));
        assertNull(superSuperProductCmptType.findSupertype(ipsProject));
        productCmptType.setSupertype("unknownType");
        assertNull(productCmptType.findSupertype(ipsProject));
    }

    @Test
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

    @Test
    public void testNewMemento() {
        Memento memento = productCmptType.newMemento();
        assertNotNull(memento);

        productCmptType.newProductCmptTypeAttribute();
        memento = productCmptType.newMemento();
        assertNotNull(memento);
    }

    @Test
    public void testSetPolicyCmptType() {
        testPropertyAccessReadWrite(ProductCmptType.class, IProductCmptType.PROPERTY_POLICY_CMPT_TYPE, productCmptType,
                "NewPolicy");
    }

    @Test
    public void testFindPolicyCmptType() throws CoreException {
        productCmptType.setConfigurationForPolicyCmptType(true);
        productCmptType.setPolicyCmptType("");
        assertNull(productCmptType.findPolicyCmptType(ipsProject));

        productCmptType.setPolicyCmptType("UnknownType");
        assertNull(productCmptType.findPolicyCmptType(ipsProject));

        productCmptType.setPolicyCmptType(policyCmptType.getQualifiedName());
        assertEquals(policyCmptType, productCmptType.findPolicyCmptType(ipsProject));

        productCmptType.setConfigurationForPolicyCmptType(false);
        assertNull(productCmptType.findPolicyCmptType(ipsProject));
    }

    @Test
    public void testNewAttribute() {
        IProductCmptTypeAttribute a1 = productCmptType.newProductCmptTypeAttribute();
        assertEquals(1, productCmptType.getNumOfAttributes());
        assertEquals(a1, productCmptType.getProductCmptTypeAttributes().get(0));
        assertEquals(productCmptType, a1.getProductCmptType());

        assertEquals(a1, lastEvent.getPart());
    }

    @Test
    public void testGetAttribute() {
        assertNull(productCmptType.getProductCmptTypeAttribute("a"));

        IProductCmptTypeAttribute a1 = productCmptType.newProductCmptTypeAttribute();
        productCmptType.newProductCmptTypeAttribute();
        IProductCmptTypeAttribute a3 = productCmptType.newProductCmptTypeAttribute();
        a1.setName("a1");
        a3.setName("a3");

        assertEquals(a1, productCmptType.getProductCmptTypeAttribute("a1"));
        assertEquals(a3, productCmptType.getProductCmptTypeAttribute("a3"));
        assertNull(productCmptType.getProductCmptTypeAttribute("unkown"));

        assertNull(productCmptType.getProductCmptTypeAttribute(null));
    }

    @Test
    public void testGetAttributes() {
        assertEquals(0, productCmptType.getProductCmptTypeAttributes().size());

        IProductCmptTypeAttribute a1 = productCmptType.newProductCmptTypeAttribute();
        List<IProductCmptTypeAttribute> attributes = productCmptType.getProductCmptTypeAttributes();
        assertEquals(a1, attributes.get(0));

        IProductCmptTypeAttribute a2 = productCmptType.newProductCmptTypeAttribute();
        attributes = productCmptType.getProductCmptTypeAttributes();
        assertEquals(a1, attributes.get(0));
        assertEquals(a2, attributes.get(1));
    }

    @Test
    public void testGetNumOfAttributes() {
        assertEquals(0, productCmptType.getNumOfAttributes());

        productCmptType.newProductCmptTypeAttribute();
        assertEquals(1, productCmptType.getNumOfAttributes());

        productCmptType.newProductCmptTypeAttribute();
        assertEquals(2, productCmptType.getNumOfAttributes());
    }

    @Test
    public void testDependsOn() throws CoreException {
        IPolicyCmptType a = newPolicyCmptType(ipsProject, "A");
        IPolicyCmptType b = newPolicyCmptType(ipsProject, "B");

        IProductCmptType aProductType = a.findProductCmptType(ipsProject);
        IProductCmptType bProductType = b.findProductCmptType(ipsProject);

        List<IDependency> dependencies = Arrays.asList(aProductType.dependsOn());
        assertEquals(1, dependencies.size());
        assertTrue(dependencies.contains(IpsObjectDependency.create(aProductType.getQualifiedNameType(),
                new QualifiedNameType(aProductType.getQualifiedName(), IpsObjectType.POLICY_CMPT_TYPE),
                DependencyType.REFERENCE)));

        aProductType.setPolicyCmptType(a.getQualifiedName());

        dependencies = Arrays.asList(aProductType.dependsOn());
        assertEquals(2, dependencies.size());
        assertTrue(dependencies.contains(IpsObjectDependency.create(aProductType.getQualifiedNameType(),
                new QualifiedNameType(aProductType.getQualifiedName(), IpsObjectType.POLICY_CMPT_TYPE),
                DependencyType.REFERENCE)));

        IDependency dependency = IpsObjectDependency.create(aProductType.getQualifiedNameType(),
                a.getQualifiedNameType(), DependencyType.REFERENCE);
        assertTrue(dependencies.contains(dependency));
        assertSingleDependencyDetail(aProductType, dependency, aProductType, IProductCmptType.PROPERTY_POLICY_CMPT_TYPE);

        IAssociation aProductTypeTobProductType = aProductType.newAssociation();
        aProductTypeTobProductType.setTarget(bProductType.getQualifiedName());

        dependencies = Arrays.asList(aProductType.dependsOn());
        assertEquals(3, dependencies.size());
        assertTrue(dependencies.contains(IpsObjectDependency.create(aProductType.getQualifiedNameType(),
                new QualifiedNameType(aProductType.getQualifiedName(), IpsObjectType.POLICY_CMPT_TYPE),
                DependencyType.REFERENCE)));

        dependency = IpsObjectDependency.create(aProductType.getQualifiedNameType(), a.getQualifiedNameType(),
                DependencyType.REFERENCE);
        assertTrue(dependencies.contains(dependency));
        assertSingleDependencyDetail(aProductType, dependency, aProductType, IProductCmptType.PROPERTY_POLICY_CMPT_TYPE);

        dependency = IpsObjectDependency.create(aProductType.getQualifiedNameType(),
                bProductType.getQualifiedNameType(), DependencyType.REFERENCE);
        assertTrue(dependencies.contains(dependency));
        assertSingleDependencyDetail(aProductType, dependency, aProductTypeTobProductType, IAssociation.PROPERTY_TARGET);

        IAttribute aAttr = aProductType.newAttribute();
        aAttr.setDatatype(Datatype.MONEY.getQualifiedName());

        dependencies = Arrays.asList(aProductType.dependsOn());
        assertEquals(4, dependencies.size());
        assertTrue(dependencies.contains(IpsObjectDependency.create(aProductType.getQualifiedNameType(),
                new QualifiedNameType(aProductType.getQualifiedName(), IpsObjectType.POLICY_CMPT_TYPE),
                DependencyType.REFERENCE)));

        dependency = IpsObjectDependency.create(aProductType.getQualifiedNameType(), a.getQualifiedNameType(),
                DependencyType.REFERENCE);
        assertTrue(dependencies.contains(dependency));
        assertSingleDependencyDetail(aProductType, dependency, aProductType, IProductCmptType.PROPERTY_POLICY_CMPT_TYPE);

        dependency = IpsObjectDependency.create(aProductType.getQualifiedNameType(),
                bProductType.getQualifiedNameType(), DependencyType.REFERENCE);
        assertTrue(dependencies.contains(dependency));
        assertSingleDependencyDetail(aProductType, dependency, aProductTypeTobProductType, IAssociation.PROPERTY_TARGET);

        dependency = new DatatypeDependency(aProductType.getQualifiedNameType(), Datatype.MONEY.getQualifiedName());
        assertTrue(dependencies.contains(dependency));
        assertSingleDependencyDetail(aProductType, dependency, aAttr, IAttribute.PROPERTY_DATATYPE);

        IMethod aMethod = aProductType.newMethod();
        aMethod.setDatatype(Datatype.DECIMAL.getQualifiedName());

        dependencies = Arrays.asList(aProductType.dependsOn());
        assertEquals(5, dependencies.size());
        assertTrue(dependencies.contains(IpsObjectDependency.create(aProductType.getQualifiedNameType(),
                new QualifiedNameType(aProductType.getQualifiedName(), IpsObjectType.POLICY_CMPT_TYPE),
                DependencyType.REFERENCE)));

        dependency = IpsObjectDependency.create(aProductType.getQualifiedNameType(), a.getQualifiedNameType(),
                DependencyType.REFERENCE);
        assertTrue(dependencies.contains(dependency));
        assertSingleDependencyDetail(aProductType, dependency, aProductType, IProductCmptType.PROPERTY_POLICY_CMPT_TYPE);

        dependency = IpsObjectDependency.create(aProductType.getQualifiedNameType(),
                bProductType.getQualifiedNameType(), DependencyType.REFERENCE);
        assertTrue(dependencies.contains(dependency));
        assertSingleDependencyDetail(aProductType, dependency, aProductTypeTobProductType, IAssociation.PROPERTY_TARGET);

        dependency = new DatatypeDependency(aProductType.getQualifiedNameType(), Datatype.MONEY.getQualifiedName());
        assertTrue(dependencies.contains(dependency));
        assertSingleDependencyDetail(aProductType, dependency, aAttr, IAttribute.PROPERTY_DATATYPE);

        dependency = new DatatypeDependency(aProductType.getQualifiedNameType(), Datatype.DECIMAL.getQualifiedName());
        assertTrue(dependencies.contains(dependency));
        assertSingleDependencyDetail(aProductType, dependency, aMethod, IMethod.PROPERTY_DATATYPE);
    }

    @Test
    public void testMoveAttributes() {
        IProductCmptTypeAttribute a1 = productCmptType.newProductCmptTypeAttribute();
        IProductCmptTypeAttribute a2 = productCmptType.newProductCmptTypeAttribute();
        IProductCmptTypeAttribute a3 = productCmptType.newProductCmptTypeAttribute();

        productCmptType.moveAttributes(new int[] { 1, 2 }, true);
        List<IProductCmptTypeAttribute> attributes = productCmptType.getProductCmptTypeAttributes();
        assertEquals(a2, attributes.get(0));
        assertEquals(a3, attributes.get(1));
        assertEquals(a1, attributes.get(2));

        assertTrue(lastEvent.isAffected(a1));
        assertTrue(lastEvent.isAffected(a2));
        assertTrue(lastEvent.isAffected(a3));
    }

    @Test
    public void testInitFromXml() {
        Element rootEl = getTestDocument().getDocumentElement();
        productCmptType.setPolicyCmptType("Bla");
        productCmptType.setConfigurationForPolicyCmptType(false);

        productCmptType.initFromXml(XmlUtil.getElement(rootEl, 0));
        assertEquals("Policy", productCmptType.getPolicyCmptType());
        assertTrue(productCmptType.isConfigurationForPolicyCmptType());
        assertEquals(1, productCmptType.getNumOfAttributes());
        assertEquals(1, productCmptType.getNumOfAssociations());
        assertEquals(1, productCmptType.getNumOfTableStructureUsages());
        assertEquals(1, productCmptType.getNumOfMethods());
    }

    @Test
    public void testToXml() throws CoreException {
        productCmptType.setConfigurationForPolicyCmptType(true);
        productCmptType.setPolicyCmptType(policyCmptType.getQualifiedName());
        productCmptType.newProductCmptTypeAttribute().setName("attr");
        productCmptType.newProductCmptTypeAssociation().setTargetRoleSingular("role");
        productCmptType.newTableStructureUsage().setRoleName("roleTsu");
        productCmptType.newMethod().setName("method1");

        Element el = productCmptType.toXml(newDocument());
        productCmptType = newProductCmptType(ipsProject, "Copy");
        productCmptType.setConfigurationForPolicyCmptType(false);
        productCmptType.initFromXml(el);

        assertEquals(policyCmptType.getQualifiedName(), productCmptType.getPolicyCmptType());
        assertTrue(productCmptType.isConfigurationForPolicyCmptType());
        assertEquals(1, productCmptType.getNumOfAttributes());
        assertEquals(1, productCmptType.getNumOfAssociations());
        assertEquals(1, productCmptType.getNumOfTableStructureUsages());
        assertEquals(1, productCmptType.getNumOfMethods());
    }

    @Test
    public void testValidateOtherTypeWithSameNameTypeInIpsObjectPath() throws CoreException {
        IIpsProject a = newIpsProject("aProject");
        IPolicyCmptType aPolicyProjectA = newPolicyCmptType(a, "faktorzehn.example.APolicy");
        IIpsProject b = newIpsProject("bProject");
        IProductCmptType aProductTypeProjectB = newProductCmptType(b, "faktorzehn.example.APolicy");

        IIpsObjectPath bPath = b.getIpsObjectPath();
        IIpsObjectPathEntry[] bPathEntries = bPath.getEntries();
        ArrayList<IIpsObjectPathEntry> newbPathEntries = new ArrayList<IIpsObjectPathEntry>();
        newbPathEntries.add(new IpsProjectRefEntry((IpsObjectPath)bPath, a));
        for (IIpsObjectPathEntry bPathEntrie : bPathEntries) {
            newbPathEntries.add(bPathEntrie);
        }
        bPath.setEntries(newbPathEntries.toArray(new IIpsObjectPathEntry[newbPathEntries.size()]));
        b.setIpsObjectPath(bPath);

        MessageList msgList = aPolicyProjectA.validate(a);
        assertNull(msgList.getMessageByCode(IType.MSGCODE_OTHER_TYPE_WITH_SAME_NAME_IN_DEPENDENT_PROJECT_EXISTS));

        msgList = aProductTypeProjectB.validate(b);
        assertNotNull(msgList.getMessageByCode(IType.MSGCODE_OTHER_TYPE_WITH_SAME_NAME_IN_DEPENDENT_PROJECT_EXISTS));
    }

    @Test
    public void testValidateOtherTypeWithSameNameTypeInIpsObjectPath2() throws CoreException {

        IIpsProject a = newIpsProject("aProject");
        IPolicyCmptType aPolicyProjectA = newPolicyCmptType(a, "faktorzehn.example.APolicy");
        IProductCmptType aProductTypeProjectB = newProductCmptType(a, "faktorzehn.example.APolicy");

        MessageList msgList = aPolicyProjectA.validate(a);
        assertNotNull(msgList.getMessageByCode(IType.MSGCODE_OTHER_TYPE_WITH_SAME_NAME_EXISTS));

        msgList = aProductTypeProjectB.validate(a);
        assertNotNull(msgList.getMessageByCode(IType.MSGCODE_OTHER_TYPE_WITH_SAME_NAME_EXISTS));
    }

    @Test
    public void testValidateProductTypeAbstractWhenPcTypeAbstract() throws Exception {
        IPolicyCmptType a = newPolicyAndProductCmptType(ipsProject, "A", "AConfig");
        IProductCmptType aConfig = a.findProductCmptType(ipsProject);
        a.setAbstract(false);
        aConfig.setAbstract(false);
        MessageList msgList = aConfig.validate(ipsProject);
        assertNull(msgList
                .getMessageByCode(IProductCmptType.MSGCODE_PRODUCTCMPTTYPE_ABSTRACT_WHEN_POLICYCMPTTYPE_ABSTRACT));

        a.setAbstract(true);
        msgList = aConfig.validate(ipsProject);
        assertNotNull(msgList
                .getMessageByCode(IProductCmptType.MSGCODE_PRODUCTCMPTTYPE_ABSTRACT_WHEN_POLICYCMPTTYPE_ABSTRACT));

        aConfig.setAbstract(true);
        msgList = aConfig.validate(ipsProject);
        assertNull(msgList
                .getMessageByCode(IProductCmptType.MSGCODE_PRODUCTCMPTTYPE_ABSTRACT_WHEN_POLICYCMPTTYPE_ABSTRACT));
    }

    @Test
    public void testValidateNoDefaultCategoryForFormulaSignatureDefinitionsExists() throws CoreException {
        IProductCmptCategory category = productCmptType.newProductCmptCategory("foo");
        category.setDefaultForPolicyCmptTypeAttributes(true);
        category.setDefaultForProductCmptTypeAttributes(true);
        category.setDefaultForTableStructureUsages(true);
        category.setDefaultForValidationRules(true);

        MessageList validationMessageList = productCmptType.validate(ipsProject);
        assertOneValidationMessage(validationMessageList,
                IProductCmptType.MSGCODE_NO_DEFAULT_FOR_FORMULA_SIGNATURE_DEFINITIONS, productCmptType, null,
                Message.ERROR);
    }

    @Test
    public void testValidateNoDefaultCategoryForPolicyCmptTypeAttributesExists() throws CoreException {
        IProductCmptCategory category = productCmptType.newProductCmptCategory("foo");
        category.setDefaultForFormulaSignatureDefinitions(true);
        category.setDefaultForProductCmptTypeAttributes(true);
        category.setDefaultForTableStructureUsages(true);
        category.setDefaultForValidationRules(true);

        MessageList validationMessageList = productCmptType.validate(ipsProject);
        assertOneValidationMessage(validationMessageList,
                IProductCmptType.MSGCODE_NO_DEFAULT_FOR_POLICY_CMPT_TYPE_ATTRIBUTES, productCmptType, null,
                Message.ERROR);
    }

    @Test
    public void testValidateNoDefaultCategoryForProductCmptTypeAttributesExists() throws CoreException {
        IProductCmptCategory category = productCmptType.newProductCmptCategory("foo");
        category.setDefaultForFormulaSignatureDefinitions(true);
        category.setDefaultForPolicyCmptTypeAttributes(true);
        category.setDefaultForTableStructureUsages(true);
        category.setDefaultForValidationRules(true);

        MessageList validationMessageList = productCmptType.validate(ipsProject);
        assertOneValidationMessage(validationMessageList,
                IProductCmptType.MSGCODE_NO_DEFAULT_FOR_PRODUCT_CMPT_TYPE_ATTRIBUTES, productCmptType, null,
                Message.ERROR);
    }

    @Test
    public void testValidateNoDefaultCategoryForTableStructureUsagesExists() throws CoreException {
        IProductCmptCategory category = productCmptType.newProductCmptCategory("foo");
        category.setDefaultForFormulaSignatureDefinitions(true);
        category.setDefaultForPolicyCmptTypeAttributes(true);
        category.setDefaultForProductCmptTypeAttributes(true);
        category.setDefaultForValidationRules(true);

        MessageList validationMessageList = productCmptType.validate(ipsProject);
        assertOneValidationMessage(validationMessageList,
                IProductCmptType.MSGCODE_NO_DEFAULT_FOR_TABLE_STRUCTURE_USAGES, productCmptType, null, Message.ERROR);
    }

    @Test
    public void testValidateNoDefaultCategoryForValidationRulesExists() throws CoreException {
        IProductCmptCategory category = productCmptType.newProductCmptCategory("foo");
        category.setDefaultForFormulaSignatureDefinitions(true);
        category.setDefaultForPolicyCmptTypeAttributes(true);
        category.setDefaultForProductCmptTypeAttributes(true);
        category.setDefaultForTableStructureUsages(true);

        MessageList validationMessageList = productCmptType.validate(ipsProject);
        assertOneValidationMessage(validationMessageList, IProductCmptType.MSGCODE_NO_DEFAULT_FOR_VALIDATION_RULES,
                productCmptType, null, Message.ERROR);
    }

    @Test
    public void testFindSignaturesOfOverloadedFormulas() throws Exception {
        IProductCmptType aType = newProductCmptType(ipsProject, "AType");

        // formula method that will be overloaded by subclass
        IProductCmptTypeMethod aMethod = aType.newProductCmptTypeMethod();
        aMethod.setName("calculate");
        aMethod.setDatatype(Datatype.STRING.toString());
        aMethod.setFormulaName("formula");
        aMethod.setFormulaSignatureDefinition(true);
        aMethod.setModifier(Modifier.PUBLIC);
        aMethod.newParameter(Datatype.STRING.toString(), "param1");
        aMethod.newParameter(Datatype.INTEGER.toString(), "param2");

        // formula method that is no exp
        IProductCmptTypeMethod a2Method = aType.newProductCmptTypeMethod();
        a2Method.setName("calculate2");
        a2Method.setDatatype(Datatype.STRING.toString());
        a2Method.setFormulaName("formula2");
        a2Method.setFormulaSignatureDefinition(true);
        a2Method.setModifier(Modifier.PUBLIC);
        a2Method.newParameter(Datatype.STRING.toString(), "param1");
        a2Method.newParameter(Datatype.INTEGER.toString(), "param2");

        IProductCmptType bType = newProductCmptType(ipsProject, "BType");
        bType.setSupertype(aType.getQualifiedName());
        IProductCmptTypeMethod bMethod = bType.newProductCmptTypeMethod();
        bMethod.setName("calculate");
        bMethod.setDatatype(Datatype.STRING.toString());
        bMethod.setFormulaName("formula");
        bMethod.setFormulaSignatureDefinition(true);
        bMethod.setModifier(Modifier.PUBLIC);
        bMethod.newParameter(Datatype.STRING.toString(), "param1");
        bMethod.newParameter(Datatype.INTEGER.toString(), "param2");
        bMethod.setOverloadsFormula(true);

        IProductCmptTypeMethod bMethod2 = bType.newProductCmptTypeMethod();
        bMethod2 = bType.newProductCmptTypeMethod();
        bMethod2.setName("doCalculate");
        bMethod2.setDatatype(Datatype.STRING.toString());
        bMethod2.setFormulaName("formula");
        bMethod2.setFormulaSignatureDefinition(true);
        bMethod2.setModifier(Modifier.PUBLIC);
        bMethod2.newParameter(Datatype.STRING.toString(), "param1");
        bMethod2.newParameter(Datatype.INTEGER.toString(), "param2");
        bMethod2.setOverloadsFormula(false);

        List<IProductCmptTypeMethod> methods = bType.findSignaturesOfOverloadedFormulas(ipsProject);
        assertEquals(1, methods.size());
    }

    @Test
    public void testFindOverrideMethodCandidates() throws Exception {
        IProductCmptType aType = newProductCmptType(ipsProject, "AType");

        IProductCmptTypeMethod aMethod = aType.newProductCmptTypeMethod();
        aMethod.setName("calculate");
        aMethod.setDatatype(Datatype.STRING.toString());
        aMethod.setFormulaName("formula");
        aMethod.setFormulaSignatureDefinition(true);
        aMethod.setModifier(Modifier.PUBLIC);
        aMethod.newParameter(Datatype.STRING.toString(), "param1");
        aMethod.newParameter(Datatype.INTEGER.toString(), "param2");

        // formula method that is no exp
        IProductCmptTypeMethod a2Method = aType.newProductCmptTypeMethod();
        a2Method.setName("calculate2");
        a2Method.setDatatype(Datatype.STRING.toString());
        a2Method.setFormulaName("formula2");
        a2Method.setFormulaSignatureDefinition(true);
        a2Method.setModifier(Modifier.PUBLIC);
        a2Method.newParameter(Datatype.STRING.toString(), "param1");
        a2Method.newParameter(Datatype.INTEGER.toString(), "param2");

        IProductCmptType bType = newProductCmptType(ipsProject, "BType");
        bType.setSupertype(aType.getQualifiedName());

        List<IMethod> methods = bType.findOverrideMethodCandidates(false, ipsProject);
        assertEquals(2, methods.size());

        IProductCmptTypeMethod bMethod = bType.newProductCmptTypeMethod();
        bMethod.setName("calculate");
        bMethod.setDatatype(Datatype.STRING.toString());
        bMethod.setFormulaName("formula");
        bMethod.setFormulaSignatureDefinition(true);
        bMethod.setModifier(Modifier.PUBLIC);
        bMethod.newParameter(Datatype.STRING.toString(), "param1");
        bMethod.newParameter(Datatype.INTEGER.toString(), "param2");
        bMethod.newParameter(Datatype.INTEGER.toString(), "param2");
        bMethod.setOverloadsFormula(true);

        methods = bType.findOverrideMethodCandidates(false, ipsProject);
        assertEquals(1, methods.size());
    }

    @Override
    public void contentsChanged(ContentChangeEvent event) {
        lastEvent = event;
    }

    @Test
    public void testFindAllMetaObjects() throws CoreException {
        String productCmptTypeQName = "pack.MyProductCmptType";
        String productCmptTypeProj2QName = "otherpack.MyProductCmptTypeProj2";
        String productCmpt1QName = "pack.MyProductCmpt1";
        String productCmpt2QName = "pack.MyProductCmpt2";
        String productCmpt3QName = "pack.MyProductCmpt3";
        String productCmptProj2QName = "otherpack.MyProductCmptProj2";

        IIpsProject referencingProject = newIpsProject("referencingProject");
        IIpsObjectPath path = referencingProject.getIpsObjectPath();
        path.newIpsProjectRefEntry(ipsProject);
        referencingProject.setIpsObjectPath(path);

        IIpsProject independentProject = newIpsProject("independentProject");

        /*
         * leaveProject1 and leaveProject2 are not directly integrated in any test. But the tested
         * instance search methods have to search in all project that holds a reference to the
         * project of the object. So the search for a Object in e.g. ipsProject have to search for
         * instances in leaveProject1 and leaveProject2. The tests implicit that no duplicates are
         * found.
         */

        IIpsProject leaveProject1 = newIpsProject("LeaveProject1");
        path = leaveProject1.getIpsObjectPath();
        path.newIpsProjectRefEntry(referencingProject);
        leaveProject1.setIpsObjectPath(path);

        IIpsProject leaveProject2 = newIpsProject("LeaveProject2");
        path = leaveProject2.getIpsObjectPath();
        path.newIpsProjectRefEntry(referencingProject);
        leaveProject2.setIpsObjectPath(path);

        ProductCmptType productCmptType = newProductCmptType(ipsProject, productCmptTypeQName);
        ProductCmpt productCmpt1 = newProductCmpt(productCmptType, productCmpt1QName);
        ProductCmpt productCmpt2 = newProductCmpt(productCmptType, productCmpt2QName);
        ProductCmpt productCmpt3 = newProductCmpt(ipsProject, productCmpt3QName);

        Collection<IIpsSrcFile> resultList = productCmptType.searchMetaObjectSrcFiles(true);
        assertEquals(2, resultList.size());
        assertTrue(resultList.contains(productCmpt1.getIpsSrcFile()));
        assertTrue(resultList.contains(productCmpt2.getIpsSrcFile()));
        assertFalse(resultList.contains(productCmpt3.getIpsSrcFile()));

        ProductCmpt productCmptProj2 = newProductCmpt(referencingProject, productCmptProj2QName);
        productCmptProj2.setProductCmptType(productCmptTypeQName);

        resultList = productCmptType.searchMetaObjectSrcFiles(true);
        assertEquals(3, resultList.size());
        assertTrue(resultList.contains(productCmpt1.getIpsSrcFile()));
        assertTrue(resultList.contains(productCmpt2.getIpsSrcFile()));
        assertTrue(resultList.contains(productCmptProj2.getIpsSrcFile()));
        assertFalse(resultList.contains(productCmpt3.getIpsSrcFile()));

        ProductCmptType productCmptTypeProj2 = newProductCmptType(independentProject, productCmptTypeProj2QName);

        resultList = productCmptTypeProj2.searchMetaObjectSrcFiles(true);
        assertEquals(0, resultList.size());

        ProductCmptType superProductCmpt = newProductCmptType(ipsProject, "superProductCmpt");
        superProductCmpt.setAbstract(true);
        productCmptType.setSupertype(superProductCmpt.getQualifiedName());

        resultList = productCmptTypeProj2.searchMetaObjectSrcFiles(false);
        assertEquals(0, resultList.size());

        resultList = superProductCmpt.searchMetaObjectSrcFiles(true);
        assertEquals(3, resultList.size());
        assertTrue(resultList.contains(productCmpt1.getIpsSrcFile()));
        assertTrue(resultList.contains(productCmpt2.getIpsSrcFile()));
        assertTrue(resultList.contains(productCmptProj2.getIpsSrcFile()));
        assertFalse(resultList.contains(productCmpt3.getIpsSrcFile()));
    }

    @Test
    public void testGetProductCmptCategory() {
        IProductCmptCategory category1 = productCmptType.newProductCmptCategory("foo");
        IProductCmptCategory category2 = productCmptType.newProductCmptCategory("bar");
        category2.setInherited(true);

        assertEquals(category1, productCmptType.getProductCmptCategory("foo"));
        assertNull(productCmptType.getProductCmptCategory("bar"));
    }

    @Test
    public void testGetProductCmptCategoryIncludeSupertypeCopies() {
        IProductCmptCategory category = productCmptType.newProductCmptCategory("foo");
        IProductCmptCategory inheritedCategory = productCmptType.newProductCmptCategory("bar");
        inheritedCategory.setInherited(true);

        assertEquals(category, productCmptType.getProductCmptCategoryIncludeSupertypeCopies("foo"));
        assertEquals(inheritedCategory, productCmptType.getProductCmptCategoryIncludeSupertypeCopies("bar"));
    }

    @Test
    public void testGetProductCmptCategories() {
        IProductCmptCategory category = productCmptType.newProductCmptCategory("foo");
        IProductCmptCategory inheritedCategory = productCmptType.newProductCmptCategory("bar");
        inheritedCategory.setInherited(true);

        assertEquals(category, productCmptType.getProductCmptCategories().get(0));
        assertFalse(productCmptType.getProductCmptCategories().contains(inheritedCategory));
        assertEquals(1, productCmptType.getProductCmptCategories().size());
    }

    @Test
    public void testGetProductCmptCategoriesIncludeSupertypeCopies() {
        IProductCmptCategory category = productCmptType.newProductCmptCategory("foo");
        IProductCmptCategory inheritedCategory = productCmptType.newProductCmptCategory("bar");
        inheritedCategory.setInherited(true);

        assertEquals(category, productCmptType.getProductCmptCategoriesIncludeSupertypeCopies().get(0));
        assertEquals(inheritedCategory, productCmptType.getProductCmptCategoriesIncludeSupertypeCopies().get(1));
        assertEquals(2, productCmptType.getProductCmptCategoriesIncludeSupertypeCopies().size());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetProductCmptCategoriesIncludeSupertypeCopiesUnmodifiable() {
        productCmptType.newProductCmptCategory("foo");
        productCmptType.getProductCmptCategoriesIncludeSupertypeCopies().remove(0);
    }

    @Test
    public void testFindAllProductCmptCategories() throws CoreException {
        IProductCmptType superSuperProductCmptType = newProductCmptType(ipsProject, "SuperSuperProductCmptType");
        superProductCmptType.setSupertype(superSuperProductCmptType.getQualifiedName());

        IProductCmptCategory superSuperCategory = superSuperProductCmptType.newProductCmptCategory();
        IProductCmptCategory superCategory = superProductCmptType.newProductCmptCategory();
        IProductCmptCategory category = productCmptType.newProductCmptCategory();
        IProductCmptCategory inheritedCategory = productCmptType.newProductCmptCategory();
        inheritedCategory.setInherited(true);

        List<IProductCmptCategory> allCategories = productCmptType.findAllProductCmptCategories(ipsProject);
        assertEquals(superSuperCategory, allCategories.get(0));
        assertEquals(superCategory, allCategories.get(1));
        assertEquals(category, allCategories.get(2));
        assertFalse(allCategories.contains(inheritedCategory));
        assertEquals(3, allCategories.size());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testFindAllProductCmptCategoriesUnmodifiable() throws CoreException {
        productCmptType.newProductCmptCategory();
        productCmptType.findAllProductCmptCategories(ipsProject).remove(0);
    }

    @Test
    public void testFindProductCmptCategory() throws CoreException {
        IProductCmptCategory superCategory = superProductCmptType.newProductCmptCategory("foo");
        IProductCmptCategory category = productCmptType.newProductCmptCategory("foo");
        category.setInherited(true);

        assertEquals(superCategory, productCmptType.findProductCmptCategory("foo", ipsProject));
    }

    @Test
    public void testMoveProductCmptCategories() {
        IProductCmptCategory category1 = productCmptType.newProductCmptCategory();
        IProductCmptCategory category2 = productCmptType.newProductCmptCategory();
        IProductCmptCategory category3 = productCmptType.newProductCmptCategory();

        productCmptType.moveProductCmptCategories(new int[] { 1, 2 }, true);
        List<IProductCmptCategory> categories = productCmptType.getProductCmptCategories();
        assertEquals(category2, categories.get(0));
        assertEquals(category3, categories.get(1));
        assertEquals(category1, categories.get(2));

        assertTrue(lastEvent.isAffected(category1));
        assertTrue(lastEvent.isAffected(category2));
        assertTrue(lastEvent.isAffected(category3));
    }

    @Test
    public void testExistsPersistedProductCmptPropertyReference() {
        IProductCmptCategory category = productCmptType.newProductCmptCategory();
        IProductCmptTypeAttribute attribute = productCmptType.newProductCmptTypeAttribute();

        assertFalse(productCmptType.existsPersistedProductCmptPropertyReference(attribute));

        category.newProductCmptPropertyReference(attribute);
        assertTrue(productCmptType.existsPersistedProductCmptPropertyReference(attribute));
    }

}
