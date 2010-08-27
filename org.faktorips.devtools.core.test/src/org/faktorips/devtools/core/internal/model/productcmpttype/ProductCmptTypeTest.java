/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

import java.util.ArrayList;
import java.util.Arrays;
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
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsobject.Modifier;
import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPathEntry;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.AttributeType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpttype.IProdDefProperty;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.core.model.productcmpttype.ITableStructureUsage;
import org.faktorips.devtools.core.model.productcmpttype.ProdDefPropertyType;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.core.model.type.IMethod;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.util.XmlUtil;
import org.faktorips.util.memento.Memento;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Element;

/**
 * 
 * @author Jan Ortmann
 */
public class ProductCmptTypeTest extends AbstractDependencyTest implements ContentsChangeListener {

    private ContentChangeEvent lastEvent = null;
    private IIpsProject ipsProject;
    private IPolicyCmptType policyCmptType;
    private IProductCmptType productCmptType;
    private IProductCmptType superProductCmptType;
    private IProductCmptType superSuperProductCmptType;

    @Override
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

    @Override
    protected void tearDownExtension() throws Exception {
        ipsProject.getIpsModel().removeChangeListener(this);
    }

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

    public void testSetConfigurationForPolicyCmptType() {
        Boolean newValue = Boolean.valueOf(!productCmptType.isConfigurationForPolicyCmptType());
        testPropertyAccessReadWrite(IProductCmptType.class,
                IProductCmptType.PROPERTY_CONFIGURATION_FOR_POLICY_CMPT_TYPE, productCmptType, newValue);
    }

    public void testFindProdDefProperties() throws CoreException {
        IProdDefProperty[] props = productCmptType.findProdDefProperties(ipsProject);
        assertEquals(0, props.length);

        // attributes
        IProductCmptTypeAttribute supertypeAttr = superProductCmptType.newProductCmptTypeAttribute();
        supertypeAttr.setName("attrInSupertype");
        supertypeAttr.setDatatype("Money");

        IProductCmptTypeAttribute typeAttribute1 = productCmptType.newProductCmptTypeAttribute("attrInType1");
        IProductCmptTypeAttribute typeAttribute2 = productCmptType.newProductCmptTypeAttribute("attrInType2");

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
        productCmptType.newProductCmptTypeMethod().setFormulaSignatureDefinition(false);// this
        // method is not a product def property as it is not a formula signature

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
        org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute policyCmptSupertypeAttr = policyCmptSupertype
                .newPolicyCmptTypeAttribute();
        policyCmptSupertypeAttr.setProductRelevant(true);
        org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute policyCmptTypeAttr1 = policyCmptType
                .newPolicyCmptTypeAttribute();
        policyCmptTypeAttr1.setProductRelevant(true);
        org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute policyCmptTypeAttr2 = policyCmptType
                .newPolicyCmptTypeAttribute();
        policyCmptTypeAttr2.setProductRelevant(true);
        policyCmptType.newPolicyCmptTypeAttribute().setProductRelevant(false); // this attribute is
        // not a product def
        // property as it is
        // not product
        // relevant!
        IPolicyCmptTypeAttribute derivedAttr = policyCmptType.newPolicyCmptTypeAttribute();
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

    public void testFindProdDefProperties_TwoProductCmptTypesConfigureSamePolicyCmptType() throws CoreException {
        IPolicyCmptTypeAttribute policyCmptTypeAttr1 = policyCmptType.newPolicyCmptTypeAttribute();
        policyCmptTypeAttr1.setProductRelevant(true);
        IProdDefProperty[] props = productCmptType.findProdDefProperties(ipsProject);
        assertEquals(1, props.length); // make sure, setup is ok

        IProductCmptType subtype = newProductCmptType(productCmptType, "Subtype");
        subtype.setConfigurationForPolicyCmptType(true);
        subtype.setPolicyCmptType(policyCmptType.getQualifiedName());
        props = subtype.findProdDefProperties(ipsProject);
        assertEquals(1, props.length); // attribute mustn't be inluded twice!!!
    }

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
        Map<String, IProdDefProperty> propertyMap = ((ProductCmptType)productCmptType).getProdDefPropertiesMap(null,
                ipsProject);
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

        propertyMap = ((ProductCmptType)productCmptType).getProdDefPropertiesMap(
                ProdDefPropertyType.TABLE_CONTENT_USAGE, ipsProject);
        assertEquals(2, propertyMap.size());
        assertEquals(supertypeTsu, propertyMap.get(supertypeTsu.getPropertyName()));
        assertEquals(typeTsu, propertyMap.get(typeTsu.getPropertyName()));

        propertyMap = ((ProductCmptType)productCmptType).getProdDefPropertiesMap(ProdDefPropertyType.FORMULA,
                ipsProject);
        assertEquals(2, propertyMap.size());
        assertEquals(supertypeSignature, propertyMap.get(supertypeSignature.getPropertyName()));
        assertEquals(typeSignature, propertyMap.get(typeSignature.getPropertyName()));

        propertyMap = ((ProductCmptType)productCmptType).getProdDefPropertiesMap(
                ProdDefPropertyType.DEFAULT_VALUE_AND_VALUESET, ipsProject);
        assertEquals(2, propertyMap.size());
        assertEquals(policyCmptSupertypeAttr, propertyMap.get(policyCmptSupertypeAttr.getPropertyName()));
        assertEquals(policyCmptTypeAttr, propertyMap.get(policyCmptTypeAttr.getPropertyName()));

        // test if two product component types configure the same policy component type, that the
        // properties defined by the
        // policy component type aren't considered twice.
        IProductCmptType subtype = newProductCmptType(productCmptType, "Subtype");
        subtype.setPolicyCmptType(policyCmptType.getQualifiedName());
        propertyMap = ((ProductCmptType)subtype).getProdDefPropertiesMap(
                ProdDefPropertyType.DEFAULT_VALUE_AND_VALUESET, ipsProject);
        assertEquals(2, propertyMap.size());
        assertEquals(policyCmptSupertypeAttr, propertyMap.get(policyCmptSupertypeAttr.getPropertyName()));
        assertEquals(policyCmptTypeAttr, propertyMap.get(policyCmptTypeAttr.getPropertyName()));
    }

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

        assertEquals(typeAttribute, productCmptType.findProdDefProperty(ProdDefPropertyType.VALUE, typeAttribute
                .getName(), ipsProject));
        assertEquals(supertypeAttr, productCmptType.findProdDefProperty(ProdDefPropertyType.VALUE, supertypeAttr
                .getName(), ipsProject));
        assertNull(productCmptType
                .findProdDefProperty(ProdDefPropertyType.FORMULA, typeAttribute.getName(), ipsProject));

        assertEquals(typeTsu, productCmptType.findProdDefProperty(ProdDefPropertyType.TABLE_CONTENT_USAGE, typeTsu
                .getRoleName(), ipsProject));
        assertEquals(supertypeTsu, productCmptType.findProdDefProperty(ProdDefPropertyType.TABLE_CONTENT_USAGE,
                supertypeTsu.getRoleName(), ipsProject));
        assertNull(productCmptType.findProdDefProperty(ProdDefPropertyType.VALUE, typeTsu.getRoleName(), ipsProject));

        assertEquals(typeSignature, productCmptType.findProdDefProperty(ProdDefPropertyType.FORMULA, typeSignature
                .getFormulaName(), ipsProject));
        assertEquals(supertypeSignature, productCmptType.findProdDefProperty(ProdDefPropertyType.FORMULA,
                supertypeSignature.getFormulaName(), ipsProject));
        assertNull(productCmptType.findProdDefProperty(ProdDefPropertyType.VALUE, typeSignature.getFormulaName(),
                ipsProject));

        assertEquals(policyCmptTypeAttr, productCmptType.findProdDefProperty(
                ProdDefPropertyType.DEFAULT_VALUE_AND_VALUESET, policyCmptTypeAttr.getName(), ipsProject));
        assertEquals(policyCmptSupertypeAttr, productCmptType.findProdDefProperty(
                ProdDefPropertyType.DEFAULT_VALUE_AND_VALUESET, policyCmptSupertypeAttr.getName(), ipsProject));
        assertNull(productCmptType.findProdDefProperty(ProdDefPropertyType.VALUE, policyCmptTypeAttr.getName(),
                ipsProject));

        productCmptType.setPolicyCmptType("");
        assertNull(productCmptType.findProdDefProperty(ProdDefPropertyType.DEFAULT_VALUE_AND_VALUESET,
                policyCmptTypeAttr.getName(), ipsProject));
    }

    public void testFindProdDefProperty_ByName() throws CoreException {
        IProdDefProperty[] props = productCmptType.findProdDefProperties(ipsProject);
        assertEquals(0, props.length);

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

        assertEquals(typeAttribute, productCmptType.findProdDefProperty(typeAttribute.getName(), ipsProject));
        assertEquals(supertypeAttr, productCmptType.findProdDefProperty(supertypeAttr.getName(), ipsProject));

        assertEquals(typeTsu, productCmptType.findProdDefProperty(typeTsu.getRoleName(), ipsProject));
        assertEquals(supertypeTsu, productCmptType.findProdDefProperty(supertypeTsu.getRoleName(), ipsProject));

        assertEquals(typeSignature, productCmptType.findProdDefProperty(typeSignature.getFormulaName(), ipsProject));
        assertEquals(supertypeSignature, productCmptType.findProdDefProperty(supertypeSignature.getFormulaName(),
                ipsProject));

        assertEquals(policyCmptTypeAttr, productCmptType.findProdDefProperty(policyCmptTypeAttr.getName(), ipsProject));
        assertEquals(policyCmptSupertypeAttr, productCmptType.findProdDefProperty(policyCmptSupertypeAttr.getName(),
                ipsProject));

        policyCmptTypeAttr.setProductRelevant(false);
        assertNull(productCmptType.findProdDefProperty(policyCmptTypeAttr.getName(), ipsProject));
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
        MessageList ml = productCmptType.validate(ipsProject);
        assertNull(ml.getMessageByCode(IProductCmptType.MSGCODE_POLICY_CMPT_TYPE_DOES_NOT_EXIST));

        productCmptType.setPolicyCmptType("Unknown");
        ml = productCmptType.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IProductCmptType.MSGCODE_POLICY_CMPT_TYPE_DOES_NOT_EXIST));

        productCmptType.setPolicyCmptType(superProductCmptType.getQualifiedName());
        ml = productCmptType.validate(ipsProject);
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

    public void testNewFormulaSignature() {
        IProductCmptTypeMethod formula = productCmptType.newFormulaSignature("premium");
        assertEquals("premium", formula.getFormulaName());
        assertEquals(formula.getDefaultMethodName(), formula.getName());
        assertTrue(formula.isFormulaSignatureDefinition());
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

        productCmptType.newProductCmptTypeAttribute();
        memento = productCmptType.newMemento();
        assertNotNull(memento);
    }

    public void testSetPolicyCmptType() {
        testPropertyAccessReadWrite(ProductCmptType.class, IProductCmptType.PROPERTY_POLICY_CMPT_TYPE, productCmptType,
                "NewPolicy");
    }

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

    public void testNewAttribute() {
        IProductCmptTypeAttribute a1 = productCmptType.newProductCmptTypeAttribute();
        assertEquals(1, productCmptType.getNumOfAttributes());
        assertEquals(a1, productCmptType.getProductCmptTypeAttributes()[0]);
        assertEquals(productCmptType, a1.getProductCmptType());

        assertEquals(a1, lastEvent.getPart());
    }

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

    public void testGetAttributes() {
        assertEquals(0, productCmptType.getProductCmptTypeAttributes().length);

        IProductCmptTypeAttribute a1 = productCmptType.newProductCmptTypeAttribute();
        IProductCmptTypeAttribute[] attributes = productCmptType.getProductCmptTypeAttributes();
        assertEquals(a1, attributes[0]);

        IProductCmptTypeAttribute a2 = productCmptType.newProductCmptTypeAttribute();
        attributes = productCmptType.getProductCmptTypeAttributes();
        assertEquals(a1, attributes[0]);
        assertEquals(a2, attributes[1]);
    }

    public void testGetNumOfAttributes() {
        assertEquals(0, productCmptType.getNumOfAttributes());

        productCmptType.newProductCmptTypeAttribute();
        assertEquals(1, productCmptType.getNumOfAttributes());

        productCmptType.newProductCmptTypeAttribute();
        assertEquals(2, productCmptType.getNumOfAttributes());
    }

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

        IDependency dependency = IpsObjectDependency.create(aProductType.getQualifiedNameType(), a
                .getQualifiedNameType(), DependencyType.REFERENCE);
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

        dependency = IpsObjectDependency.create(aProductType.getQualifiedNameType(), bProductType
                .getQualifiedNameType(), DependencyType.REFERENCE);
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

        dependency = IpsObjectDependency.create(aProductType.getQualifiedNameType(), bProductType
                .getQualifiedNameType(), DependencyType.REFERENCE);
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

        dependency = IpsObjectDependency.create(aProductType.getQualifiedNameType(), bProductType
                .getQualifiedNameType(), DependencyType.REFERENCE);
        assertTrue(dependencies.contains(dependency));
        assertSingleDependencyDetail(aProductType, dependency, aProductTypeTobProductType, IAssociation.PROPERTY_TARGET);

        dependency = new DatatypeDependency(aProductType.getQualifiedNameType(), Datatype.MONEY.getQualifiedName());
        assertTrue(dependencies.contains(dependency));
        assertSingleDependencyDetail(aProductType, dependency, aAttr, IAttribute.PROPERTY_DATATYPE);

        dependency = new DatatypeDependency(aProductType.getQualifiedNameType(), Datatype.DECIMAL.getQualifiedName());
        assertTrue(dependencies.contains(dependency));
        assertSingleDependencyDetail(aProductType, dependency, aMethod, IMethod.PROPERTY_DATATYPE);
    }

    public void testMoveAttributes() {
        IProductCmptTypeAttribute a1 = productCmptType.newProductCmptTypeAttribute();
        IProductCmptTypeAttribute a2 = productCmptType.newProductCmptTypeAttribute();
        IProductCmptTypeAttribute a3 = productCmptType.newProductCmptTypeAttribute();

        productCmptType.moveAttributes(new int[] { 1, 2 }, true);
        IProductCmptTypeAttribute[] attributes = productCmptType.getProductCmptTypeAttributes();
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
        productCmptType.setConfigurationForPolicyCmptType(false);

        productCmptType.initFromXml(XmlUtil.getElement(rootEl, 0));
        assertEquals("Policy", productCmptType.getPolicyCmptType());
        assertTrue(productCmptType.isConfigurationForPolicyCmptType());
        assertEquals(1, productCmptType.getNumOfAttributes());
        assertEquals(1, productCmptType.getNumOfAssociations());
        assertEquals(1, productCmptType.getNumOfTableStructureUsages());
        assertEquals(1, productCmptType.getNumOfMethods());
    }

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

    public void testValidateOtherTypeWithSameNameTypeInIpsObjectPath2() throws CoreException {

        IIpsProject a = newIpsProject("aProject");
        IPolicyCmptType aPolicyProjectA = newPolicyCmptType(a, "faktorzehn.example.APolicy");
        IProductCmptType aProductTypeProjectB = newProductCmptType(a, "faktorzehn.example.APolicy");

        MessageList msgList = aPolicyProjectA.validate(a);
        assertNotNull(msgList.getMessageByCode(IType.MSGCODE_OTHER_TYPE_WITH_SAME_NAME_EXISTS));

        msgList = aProductTypeProjectB.validate(a);
        assertNotNull(msgList.getMessageByCode(IType.MSGCODE_OTHER_TYPE_WITH_SAME_NAME_EXISTS));
    }

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

        IProductCmptTypeMethod[] methods = bType.findSignaturesOfOverloadedFormulas(ipsProject);
        assertEquals(1, methods.length);
    }

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

        IMethod[] methods = bType.findOverrideMethodCandidates(false, ipsProject);
        assertEquals(2, methods.length);

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
        assertEquals(1, methods.length);
    }

    @Override
    public void contentsChanged(ContentChangeEvent event) {
        lastEvent = event;
    }

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

        Object[] result = productCmptType.searchMetaObjectSrcFiles(true);
        List<Object> resultList = Arrays.asList(result);
        assertEquals(2, result.length);
        assertTrue(resultList.contains(productCmpt1.getIpsSrcFile()));
        assertTrue(resultList.contains(productCmpt2.getIpsSrcFile()));
        assertFalse(resultList.contains(productCmpt3.getIpsSrcFile()));

        ProductCmpt productCmptProj2 = newProductCmpt(referencingProject, productCmptProj2QName);
        productCmptProj2.setProductCmptType(productCmptTypeQName);

        result = productCmptType.searchMetaObjectSrcFiles(true);
        resultList = Arrays.asList(result);
        assertEquals(3, result.length);
        assertTrue(resultList.contains(productCmpt1.getIpsSrcFile()));
        assertTrue(resultList.contains(productCmpt2.getIpsSrcFile()));
        assertTrue(resultList.contains(productCmptProj2.getIpsSrcFile()));
        assertFalse(resultList.contains(productCmpt3.getIpsSrcFile()));

        ProductCmptType productCmptTypeProj2 = newProductCmptType(independentProject, productCmptTypeProj2QName);

        result = productCmptTypeProj2.searchMetaObjectSrcFiles(true);
        assertEquals(0, result.length);

        ProductCmptType superProductCmpt = newProductCmptType(ipsProject, "superProductCmpt");
        superProductCmpt.setAbstract(true);
        productCmptType.setSupertype(superProductCmpt.getQualifiedName());

        result = productCmptTypeProj2.searchMetaObjectSrcFiles(false);
        assertEquals(0, result.length);

        result = superProductCmpt.searchMetaObjectSrcFiles(true);
        resultList = Arrays.asList(result);
        assertEquals(3, result.length);
        assertTrue(resultList.contains(productCmpt1.getIpsSrcFile()));
        assertTrue(resultList.contains(productCmpt2.getIpsSrcFile()));
        assertTrue(resultList.contains(productCmptProj2.getIpsSrcFile()));
        assertFalse(resultList.contains(productCmpt3.getIpsSrcFile()));
    }
}
