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
import org.faktorips.devtools.core.internal.model.productcmpt.ProductCmpt;
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
import org.faktorips.devtools.core.model.pctype.IValidationRule;
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
public class ProductCmptTypeTest extends AbstractDependencyTest {

    private IIpsProject ipsProject;

    private IPolicyCmptType policyCmptType;

    private ProductCmptType productCmptType;

    private IPolicyCmptType superPolicyCmptType;

    private IProductCmptType superProductCmptType;

    private IProductCmptType superSuperProductCmptType;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        ipsProject = newIpsProject();
        policyCmptType = newPolicyAndProductCmptType(ipsProject, "Policy", "Product");
        productCmptType = (ProductCmptType)policyCmptType.findProductCmptType(ipsProject);
        superPolicyCmptType = newPolicyAndProductCmptType(ipsProject, "SuperPolicy", "SuperProduct");
        superProductCmptType = superPolicyCmptType.findProductCmptType(ipsProject);
        productCmptType.setSupertype(superProductCmptType.getQualifiedName());
        policyCmptType.setSupertype(superPolicyCmptType.getQualifiedName());
        superSuperProductCmptType = newProductCmptType(ipsProject, "SuperSuperProduct");
        superProductCmptType.setSupertype(superSuperProductCmptType.getQualifiedName());
    }

    @Test
    public void testValidateMustHaveSupertype() throws CoreException {
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
    public void testFindProductCmptPropertyMap() throws CoreException {
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
        org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute policyCmptSupertypeAttr = superPolicyCmptType
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
        Map<String, IProductCmptProperty> propertyMap = productCmptType.findProductCmptPropertyMap(null, ipsProject);
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
        propertyMap = productCmptType.findProductCmptPropertyMap(ProductCmptPropertyType.PRODUCT_CMPT_TYPE_ATTRIBUTE,
                ipsProject);
        assertEquals(2, propertyMap.size());
        assertEquals(supertypeAttr, propertyMap.get(supertypeAttr.getPropertyName()));
        assertEquals(typeAttribute, propertyMap.get(typeAttribute.getPropertyName()));

        propertyMap = productCmptType.findProductCmptPropertyMap(ProductCmptPropertyType.TABLE_STRUCTURE_USAGE,
                ipsProject);
        assertEquals(2, propertyMap.size());
        assertEquals(supertypeTsu, propertyMap.get(supertypeTsu.getPropertyName()));
        assertEquals(typeTsu, propertyMap.get(typeTsu.getPropertyName()));

        propertyMap = productCmptType.findProductCmptPropertyMap(ProductCmptPropertyType.FORMULA_SIGNATURE_DEFINITION,
                ipsProject);
        assertEquals(2, propertyMap.size());
        assertEquals(supertypeSignature, propertyMap.get(supertypeSignature.getPropertyName()));
        assertEquals(typeSignature, propertyMap.get(typeSignature.getPropertyName()));

        propertyMap = productCmptType.findProductCmptPropertyMap(ProductCmptPropertyType.POLICY_CMPT_TYPE_ATTRIBUTE,
                ipsProject);
        assertEquals(2, propertyMap.size());
        assertEquals(policyCmptSupertypeAttr, propertyMap.get(policyCmptSupertypeAttr.getPropertyName()));
        assertEquals(policyCmptTypeAttr, propertyMap.get(policyCmptTypeAttr.getPropertyName()));

        // test if two product component types configure the same policy component type, that the
        // properties defined by the
        // policy component type aren't considered twice.
        IProductCmptType subtype = newProductCmptType(productCmptType, "Subtype");
        subtype.setPolicyCmptType(policyCmptType.getQualifiedName());
        propertyMap = ((ProductCmptType)subtype).findProductCmptPropertyMap(
                ProductCmptPropertyType.POLICY_CMPT_TYPE_ATTRIBUTE, ipsProject);
        assertEquals(2, propertyMap.size());
        assertEquals(policyCmptSupertypeAttr, propertyMap.get(policyCmptSupertypeAttr.getPropertyName()));
        assertEquals(policyCmptTypeAttr, propertyMap.get(policyCmptTypeAttr.getPropertyName()));
    }

    @Test
    public void testFindProductCmptProperty_ByTypeAndName() throws CoreException {
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
        org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute policyCmptSupertypeAttr = superPolicyCmptType
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
    public void testFindProductCmptProperty_ByName() throws CoreException {
        List<IProductCmptProperty> props = productCmptType.findProductCmptProperties(true, ipsProject);
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
        org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute policyCmptSupertypeAttr = superPolicyCmptType
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

        assertEquals(a1, lastContentChangeEvent.getPart());
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

        assertTrue(lastContentChangeEvent.isAffected(a1));
        assertTrue(lastContentChangeEvent.isAffected(a2));
        assertTrue(lastContentChangeEvent.isAffected(a3));
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
    public void testSavePropertyReferencesToXml() throws CoreException {
        IProductCmptProperty property1 = productCmptType.newProductCmptTypeAttribute("p1");
        IProductCmptProperty property2 = productCmptType.newProductCmptTypeAttribute("p2");
        IProductCmptProperty property3 = productCmptType.newProductCmptTypeAttribute("p3");
        productCmptType.moveProductCmptPropertyReferences(Arrays.asList(property2), true);

        Element xmlElement = productCmptType.toXml(newDocument());
        productCmptType = newProductCmptType(ipsProject, "Copy");
        productCmptType.initFromXml(xmlElement);

        List<IProductCmptProperty> properties = productCmptType.findProductCmptPropertiesInReferencedOrder(false,
                ipsProject);
        assertEquals(property2.getName(), properties.get(0).getName());
        assertEquals(property1.getName(), properties.get(1).getName());
        assertEquals(property3.getName(), properties.get(2).getName());
    }

    @Test
    public void testDoNotSaveObsoletePropertyReferencesToXml() throws CoreException {
        IProductCmptProperty property1 = productCmptType.newProductCmptTypeAttribute("p1");
        IProductCmptProperty property2 = productCmptType.newProductCmptTypeAttribute("p2");
        IProductCmptProperty property3 = productCmptType.newProductCmptTypeAttribute("p3");
        productCmptType.moveProductCmptPropertyReferences(Arrays.asList(property3, property2), true);

        // Make reference obsolete by deleting the property
        property2.delete();

        Element xmlElement = productCmptType.toXml(newDocument());
        productCmptType = newProductCmptType(ipsProject, "Copy");
        productCmptType.initFromXml(xmlElement);

        // Re-create the property to test whether it is listed at the end again
        property2 = productCmptType.newProductCmptTypeAttribute("p2");

        List<IProductCmptProperty> properties = productCmptType.findProductCmptPropertiesInReferencedOrder(false,
                ipsProject);
        assertEquals(property3.getName(), properties.get(0).getName());
        assertEquals(property1.getName(), properties.get(1).getName());
        assertEquals(property2.getName(), properties.get(2).getName());
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
        productCmptType.findDefaultCategoryForFormulaSignatureDefinitions(ipsProject).delete();
        policyCmptType.setSupertype("");
        productCmptType.setSupertype("");

        MessageList validationMessageList = productCmptType.validate(ipsProject);
        assertOneValidationMessage(validationMessageList,
                IProductCmptType.MSGCODE_NO_DEFAULT_CATEGORY_FOR_FORMULA_SIGNATURE_DEFINITIONS, productCmptType, null,
                Message.ERROR);
    }

    @Test
    public void testValidateNoDefaultCategoryForPolicyCmptTypeAttributesExists() throws CoreException {
        productCmptType.findDefaultCategoryForPolicyCmptTypeAttributes(ipsProject).delete();
        policyCmptType.setSupertype("");
        productCmptType.setSupertype("");

        MessageList validationMessageList = productCmptType.validate(ipsProject);
        assertOneValidationMessage(validationMessageList,
                IProductCmptType.MSGCODE_NO_DEFAULT_CATEGORY_FOR_POLICY_CMPT_TYPE_ATTRIBUTES, productCmptType, null,
                Message.ERROR);
    }

    @Test
    public void testValidateNoDefaultCategoryForProductCmptTypeAttributesExists() throws CoreException {
        productCmptType.findDefaultCategoryForProductCmptTypeAttributes(ipsProject).delete();
        policyCmptType.setSupertype("");
        productCmptType.setSupertype("");

        MessageList validationMessageList = productCmptType.validate(ipsProject);
        assertOneValidationMessage(validationMessageList,
                IProductCmptType.MSGCODE_NO_DEFAULT_CATEGORY_FOR_PRODUCT_CMPT_TYPE_ATTRIBUTES, productCmptType, null,
                Message.ERROR);
    }

    @Test
    public void testValidateNoDefaultCategoryForTableStructureUsagesExists() throws CoreException {
        productCmptType.findDefaultCategoryForTableStructureUsages(ipsProject).delete();
        policyCmptType.setSupertype("");
        productCmptType.setSupertype("");

        MessageList validationMessageList = productCmptType.validate(ipsProject);
        assertOneValidationMessage(validationMessageList,
                IProductCmptType.MSGCODE_NO_DEFAULT_CATEGORY_FOR_TABLE_STRUCTURE_USAGES, productCmptType, null,
                Message.ERROR);
    }

    @Test
    public void testValidateNoDefaultCategoryForValidationRulesExists() throws CoreException {
        productCmptType.findDefaultCategoryForValidationRules(ipsProject).delete();
        policyCmptType.setSupertype("");
        productCmptType.setSupertype("");

        MessageList validationMessageList = productCmptType.validate(ipsProject);
        assertOneValidationMessage(validationMessageList,
                IProductCmptType.MSGCODE_NO_DEFAULT_CATEGORY_FOR_VALIDATION_RULES, productCmptType, null, Message.ERROR);
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

    @Test
    public void testFindProductCmptProperties() throws CoreException {
        IProductCmptProperty productAttribute = createProductAttributeProperty(productCmptType, "productAttribute");
        IProductCmptProperty formulaSignature = createFormulaSignatureProperty(productCmptType, "formula");
        IProductCmptProperty tsu = createTableStructureUsageProperty(productCmptType, "tsu");
        IProductCmptProperty policyAttribute = createPolicyAttributeProperty(policyCmptType, "policyAttribute");
        IProductCmptProperty validationRule = createValidationRuleProperty(policyCmptType, "validationRule");

        List<IProductCmptProperty> properties = productCmptType.findProductCmptProperties(true, ipsProject);
        assertTrue(properties.contains(productAttribute));
        assertTrue(properties.contains(formulaSignature));
        assertTrue(properties.contains(tsu));
        assertTrue(properties.contains(policyAttribute));
        assertTrue(properties.contains(validationRule));
        assertEquals(5, properties.size());
    }

    @Test
    public void testFindProductCmptProperties_SearchSupertypeHierarchy() throws CoreException {
        IProductCmptProperty productAttribute = createProductAttributeProperty(superProductCmptType, "productAttribute");
        IProductCmptProperty formulaSignature = createFormulaSignatureProperty(superProductCmptType, "formula");
        IProductCmptProperty tsu = createTableStructureUsageProperty(superProductCmptType, "tsu");
        IProductCmptProperty policyAttribute = createPolicyAttributeProperty(superPolicyCmptType, "policyAttribute");
        IProductCmptProperty validationRule = createValidationRuleProperty(superPolicyCmptType, "validationRule");

        List<IProductCmptProperty> properties = productCmptType.findProductCmptProperties(true, ipsProject);
        assertTrue(properties.contains(productAttribute));
        assertTrue(properties.contains(formulaSignature));
        assertTrue(properties.contains(tsu));
        assertTrue(properties.contains(policyAttribute));
        assertTrue(properties.contains(validationRule));
        assertEquals(5, properties.size());
    }

    @Test
    public void testFindProductCmptProperties_DoNotSearchSupertypeHierarchy() throws CoreException {
        createProductAttributeProperty(superProductCmptType, "productAttribute");
        createFormulaSignatureProperty(superProductCmptType, "formula");
        createTableStructureUsageProperty(superProductCmptType, "tsu");
        createPolicyAttributeProperty(superPolicyCmptType, "policyAttribute");
        createValidationRuleProperty(superPolicyCmptType, "validationRule");

        IProductCmptProperty subProductAttribute = createProductAttributeProperty(productCmptType,
                "subProductAttribute");

        List<IProductCmptProperty> properties = productCmptType.findProductCmptProperties(false, ipsProject);
        assertTrue(properties.contains(subProductAttribute));
        assertEquals(1, properties.size());
    }

    @Test
    public void testFindProductCmptPropertiesIgnoreNonProductRelevantProperties() throws CoreException {
        productCmptType.newProductCmptTypeMethod().setFormulaSignatureDefinition(false);
        policyCmptType.newPolicyCmptTypeAttribute();
        policyCmptType.newRule();

        List<IProductCmptProperty> properties = productCmptType.findProductCmptProperties(true, ipsProject);
        assertTrue(properties.isEmpty());
    }

    // TODO AW other test
    @Test
    public void testFindProdDefProperties() throws CoreException {
        List<IProductCmptProperty> props = productCmptType.findProductCmptProperties(true, ipsProject);
        assertEquals(0, props.size());

        // attributes
        IProductCmptTypeAttribute supertypeAttr = superProductCmptType.newProductCmptTypeAttribute();
        supertypeAttr.setName("attrInSupertype");
        supertypeAttr.setDatatype("Money");

        IProductCmptTypeAttribute typeAttribute1 = productCmptType.newProductCmptTypeAttribute("attrInType1");
        IProductCmptTypeAttribute typeAttribute2 = productCmptType.newProductCmptTypeAttribute("attrInType2");

        props = superProductCmptType.findProductCmptProperties(true, ipsProject);
        assertEquals(1, props.size());
        assertEquals(supertypeAttr, props.get(0));
        props = productCmptType.findProductCmptProperties(true, ipsProject);
        assertEquals(3, props.size());
        assertEquals(supertypeAttr, props.get(0));
        assertEquals(typeAttribute1, props.get(1));
        assertEquals(typeAttribute2, props.get(2));

        // table structure usages
        ITableStructureUsage supertypeTsu = superProductCmptType.newTableStructureUsage();
        ITableStructureUsage typeTsu1 = productCmptType.newTableStructureUsage();
        ITableStructureUsage typeTsu2 = productCmptType.newTableStructureUsage();

        props = superProductCmptType.findProductCmptProperties(true, ipsProject);
        assertEquals(2, props.size());
        assertEquals(supertypeAttr, props.get(0));
        assertEquals(supertypeTsu, props.get(1));
        props = productCmptType.findProductCmptProperties(true, ipsProject);
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

        props = superProductCmptType.findProductCmptProperties(true, ipsProject);
        assertEquals(3, props.size());
        assertEquals(supertypeAttr, props.get(0));
        assertEquals(supertypeTsu, props.get(1));
        assertEquals(supertypeSignature, props.get(2));

        props = productCmptType.findProductCmptProperties(true, ipsProject);
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
        IPolicyCmptTypeAttribute policyCmptSupertypeAttr = superPolicyCmptType.newPolicyCmptTypeAttribute();
        policyCmptSupertypeAttr.setProductRelevant(true);
        IPolicyCmptTypeAttribute policyCmptTypeAttr1 = policyCmptType.newPolicyCmptTypeAttribute();
        policyCmptTypeAttr1.setProductRelevant(true);
        IPolicyCmptTypeAttribute policyCmptTypeAttr2 = policyCmptType.newPolicyCmptTypeAttribute();
        policyCmptTypeAttr2.setProductRelevant(true);
        policyCmptType.newPolicyCmptTypeAttribute().setProductRelevant(false); // this attribute is
        // not a product def property as it is not product relevant!
        IPolicyCmptTypeAttribute derivedAttr = policyCmptType.newPolicyCmptTypeAttribute();
        derivedAttr.setProductRelevant(true);
        derivedAttr.setAttributeType(AttributeType.DERIVED_ON_THE_FLY);

        props = superProductCmptType.findProductCmptProperties(true, ipsProject);
        assertEquals(4, props.size());
        assertEquals(supertypeAttr, props.get(0));
        assertEquals(supertypeTsu, props.get(1));
        assertEquals(supertypeSignature, props.get(2));
        assertEquals(policyCmptSupertypeAttr, props.get(3));

        props = productCmptType.findProductCmptProperties(true, ipsProject);
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
    public void testFindProductCmptProperties_TwoProductCmptTypesConfigureSamePolicyCmptType() throws CoreException {
        IPolicyCmptTypeAttribute policyCmptTypeAttribute = policyCmptType.newPolicyCmptTypeAttribute();
        policyCmptTypeAttribute.setProductRelevant(true);

        IProductCmptType subtype = newProductCmptType(productCmptType, "Subtype");
        subtype.setConfigurationForPolicyCmptType(true);
        subtype.setPolicyCmptType(policyCmptType.getQualifiedName());
        List<IProductCmptProperty> props = subtype.findProductCmptProperties(true, ipsProject);
        assertEquals(1, props.size()); // Attribute mustn't be included twice!
    }

    private IProductCmptProperty createProductAttributeProperty(IProductCmptType productCmptType, String name) {
        return productCmptType.newProductCmptTypeAttribute(name);
    }

    private IProductCmptProperty createFormulaSignatureProperty(IProductCmptType productCmptType, String formulaName) {
        return productCmptType.newFormulaSignature(formulaName);
    }

    private IProductCmptProperty createTableStructureUsageProperty(IProductCmptType productCmptType, String roleName) {
        ITableStructureUsage tsu = productCmptType.newTableStructureUsage();
        tsu.setRoleName(roleName);
        return tsu;
    }

    private IProductCmptProperty createPolicyAttributeProperty(IPolicyCmptType policyCmptType, String name) {
        IPolicyCmptTypeAttribute attribute = policyCmptType.newPolicyCmptTypeAttribute(name);
        attribute.setProductRelevant(true);
        return attribute;
    }

    private IProductCmptProperty createValidationRuleProperty(IPolicyCmptType policyCmptType, String name) {
        IValidationRule validationRule = policyCmptType.newRule();
        validationRule.setName(name);
        validationRule.setConfigurableByProductComponent(true);
        return validationRule;
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

        assertEquals(category1, productCmptType.getProductCmptCategory("foo"));
        assertEquals(category2, productCmptType.getProductCmptCategory("bar"));
    }

    @Test
    public void testGetProductCmptCategories() {
        deleteAllProductCmptCategories(productCmptType);

        IProductCmptCategory category1 = productCmptType.newProductCmptCategory("foo");
        IProductCmptCategory category2 = productCmptType.newProductCmptCategory("bar");

        assertEquals(category1, productCmptType.getProductCmptCategories().get(0));
        assertEquals(category2, productCmptType.getProductCmptCategories().get(1));
        assertEquals(2, productCmptType.getProductCmptCategories().size());
    }

    @Test
    public void testFindProductCmptCategories() throws CoreException {
        deleteAllProductCmptCategories(superSuperProductCmptType);
        deleteAllProductCmptCategories(superProductCmptType);
        deleteAllProductCmptCategories(productCmptType);

        IProductCmptCategory superSuperCategory = superSuperProductCmptType
                .newProductCmptCategory("superSuperCategory");
        IProductCmptCategory superCategory = superProductCmptType.newProductCmptCategory("superCategory");
        IProductCmptCategory category = productCmptType.newProductCmptCategory("category");

        List<IProductCmptCategory> categories = productCmptType.findProductCmptCategories(ipsProject);
        assertEquals(superSuperCategory, categories.get(0));
        assertEquals(superCategory, categories.get(1));
        assertEquals(category, categories.get(2));
        assertEquals(3, categories.size());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testFindProductCmptCategoriesUnmodifiable() throws CoreException {
        productCmptType.newProductCmptCategory();
        productCmptType.findProductCmptCategories(ipsProject).remove(0);
    }

    @Test
    public void testMoveProductCmptCategories() {
        deleteAllProductCmptCategories(productCmptType);

        IProductCmptCategory category1 = productCmptType.newProductCmptCategory();
        IProductCmptCategory category2 = productCmptType.newProductCmptCategory();
        IProductCmptCategory category3 = productCmptType.newProductCmptCategory();

        productCmptType.moveProductCmptCategories(new int[] { 1, 2 }, true);
        List<IProductCmptCategory> categories = productCmptType.getProductCmptCategories();
        assertEquals(category2, categories.get(0));
        assertEquals(category3, categories.get(1));
        assertEquals(category1, categories.get(2));

        assertTrue(lastContentChangeEvent.isAffected(category1));
        assertTrue(lastContentChangeEvent.isAffected(category2));
        assertTrue(lastContentChangeEvent.isAffected(category3));
    }

    @Test
    public void testFindDefaultCategoryForFormulaSignatureDefinitions() throws CoreException {
        deleteAllProductCmptCategories(productCmptType);
        deleteAllProductCmptCategories(superProductCmptType);

        assertEquals(
                superSuperProductCmptType.getProductCmptCategory(DEFAULT_CATEGORY_NAME_FORMULA_SIGNATURE_DEFINITIONS),
                productCmptType.findDefaultCategoryForFormulaSignatureDefinitions(ipsProject));
    }

    @Test
    public void testFindDefaultCategoryForPolicyCmptTypeAttributes() throws CoreException {
        deleteAllProductCmptCategories(productCmptType);
        deleteAllProductCmptCategories(superProductCmptType);

        assertEquals(
                superSuperProductCmptType.getProductCmptCategory(DEFAULT_CATEGORY_NAME_POLICY_CMPT_TYPE_ATTRIBUTES),
                productCmptType.findDefaultCategoryForPolicyCmptTypeAttributes(ipsProject));
    }

    @Test
    public void testFindDefaultCategoryForProductCmptTypeAttributes() throws CoreException {
        deleteAllProductCmptCategories(productCmptType);
        deleteAllProductCmptCategories(superProductCmptType);

        assertEquals(
                superSuperProductCmptType.getProductCmptCategory(DEFAULT_CATEGORY_NAME_PRODUCT_CMPT_TYPE_ATTRIBUTES),
                productCmptType.findDefaultCategoryForProductCmptTypeAttributes(ipsProject));
    }

    @Test
    public void testFindDefaultCategoryForTableStructureUsages() throws CoreException {
        deleteAllProductCmptCategories(productCmptType);
        deleteAllProductCmptCategories(superProductCmptType);

        assertEquals(superSuperProductCmptType.getProductCmptCategory(DEFAULT_CATEGORY_NAME_TABLE_STRUCTURE_USAGES),
                productCmptType.findDefaultCategoryForTableStructureUsages(ipsProject));
    }

    @Test
    public void testFindDefaultCategoryForValidationRules() throws CoreException {
        deleteAllProductCmptCategories(productCmptType);
        deleteAllProductCmptCategories(superProductCmptType);

        assertEquals(superSuperProductCmptType.getProductCmptCategory(DEFAULT_CATEGORY_NAME_VALIDATION_RULES),
                productCmptType.findDefaultCategoryForValidationRules(ipsProject));
    }

    private void deleteAllProductCmptCategories(IProductCmptType productCmptType) {
        for (IProductCmptCategory category : productCmptType.getProductCmptCategories()) {
            category.delete();
        }
    }

}
