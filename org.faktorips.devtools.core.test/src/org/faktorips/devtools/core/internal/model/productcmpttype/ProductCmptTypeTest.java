/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.productcmpttype;

import static org.faktorips.abstracttest.matcher.Matchers.hasMessageCode;
import static org.faktorips.abstracttest.matcher.Matchers.isEmpty;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.resource.DeleteResourceChange;
import org.faktorips.abstracttest.AbstractDependencyTest;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.internal.model.enums.EnumType;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsObjectPath;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsProjectRefEntry;
import org.faktorips.devtools.core.internal.model.productcmpt.ProductCmpt;
import org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptCategory.ProductCmptPropertyComparator;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.DatatypeDependency;
import org.faktorips.devtools.core.model.DependencyType;
import org.faktorips.devtools.core.model.IDependency;
import org.faktorips.devtools.core.model.IpsObjectDependency;
import org.faktorips.devtools.core.model.ipsobject.IDescription;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsobject.Modifier;
import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPathEntry;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.method.IBaseMethod;
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
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.core.model.type.IMethod;
import org.faktorips.devtools.core.model.type.IProductCmptProperty;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.model.type.ProductCmptPropertyType;
import org.faktorips.devtools.core.model.valueset.ValueSetType;
import org.faktorips.devtools.core.util.XmlUtil;
import org.faktorips.util.memento.Memento;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.faktorips.util.message.ObjectProperty;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;

/**
 * 
 * @author Jan Ortmann
 */
public class ProductCmptTypeTest extends AbstractDependencyTest {

    private static final String SUPER_ENUM_TYPE = "SuperEnumType";

    private static final String ENUM_TYPE = "EnumType";

    private static final String ATTR1 = "attr1";

    private IIpsProject ipsProject;

    private IPolicyCmptType policyCmptType;

    private ProductCmptType productCmptType;

    private IPolicyCmptType superPolicyCmptType;

    private ProductCmptType superProductCmptType;

    private IProductCmptType superSuperProductCmptType;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        ipsProject = newIpsProject();
        policyCmptType = newPolicyAndProductCmptType(ipsProject, "Policy", "Product");
        productCmptType = (ProductCmptType)policyCmptType.findProductCmptType(ipsProject);
        superPolicyCmptType = newPolicyAndProductCmptType(ipsProject, "SuperPolicy", "SuperProduct");
        superProductCmptType = (ProductCmptType)superPolicyCmptType.findProductCmptType(ipsProject);
        productCmptType.setSupertype(superProductCmptType.getQualifiedName());
        policyCmptType.setSupertype(superPolicyCmptType.getQualifiedName());
        superSuperProductCmptType = newProductCmptType(ipsProject, "SuperSuperProduct");
        superProductCmptType.setSupertype(superSuperProductCmptType.getQualifiedName());
        EnumType superEnum = newEnumType(ipsProject, SUPER_ENUM_TYPE);
        superEnum.setAbstract(true);
        EnumType enumType = newEnumType(ipsProject, ENUM_TYPE);
        enumType.setSuperEnumType(ENUM_TYPE);
        // remove categories because they are inherited from the supertype
        for (IProductCmptCategory category : productCmptType.getCategories()) {
            category.delete();
        }
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

    @Test
    public void testValidateMustHaveSameValueForConfigurationForPolicyCmptType() throws CoreException {
        productCmptType.setConfigurationForPolicyCmptType(true);
        superProductCmptType.setConfigurationForPolicyCmptType(true);
        MessageList result = productCmptType.validate(ipsProject);
        assertNull(
                result.getMessageByCode(IProductCmptType.MSGCODE_MUST_HAVE_SAME_VALUE_FOR_CONFIGURES_POLICY_CMPT_TYPE));

        productCmptType.setConfigurationForPolicyCmptType(false);
        result = productCmptType.validate(ipsProject);
        assertNotNull(
                result.getMessageByCode(IProductCmptType.MSGCODE_MUST_HAVE_SAME_VALUE_FOR_CONFIGURES_POLICY_CMPT_TYPE));

        superProductCmptType.setConfigurationForPolicyCmptType(false);
        result = productCmptType.validate(ipsProject);
        assertNull(
                result.getMessageByCode(IProductCmptType.MSGCODE_MUST_HAVE_SAME_VALUE_FOR_CONFIGURES_POLICY_CMPT_TYPE));

        productCmptType.setConfigurationForPolicyCmptType(false);
        result = productCmptType.validate(ipsProject);
        assertNull(
                result.getMessageByCode(IProductCmptType.MSGCODE_MUST_HAVE_SAME_VALUE_FOR_CONFIGURES_POLICY_CMPT_TYPE));

        superProductCmptType.setConfigurationForPolicyCmptType(false);
        result = productCmptType.validate(ipsProject);
        assertNull(
                result.getMessageByCode(IProductCmptType.MSGCODE_MUST_HAVE_SAME_VALUE_FOR_CONFIGURES_POLICY_CMPT_TYPE));
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
    public void testValidateLayerSupertype() throws CoreException {
        MessageList result = productCmptType.validate(ipsProject);
        assertNull(result.getMessageByCode(IProductCmptType.MSGCODE_SUPERTYPE_NOT_MARKED_AS_LAYER_SUPERTYPE));

        productCmptType.setLayerSupertype(true);
        result = productCmptType.validate(ipsProject);
        assertNotNull(result.getMessageByCode(IProductCmptType.MSGCODE_SUPERTYPE_NOT_MARKED_AS_LAYER_SUPERTYPE));

        superProductCmptType.setLayerSupertype(true);
        result = productCmptType.validate(ipsProject);
        assertNull(result.getMessageByCode(IProductCmptType.MSGCODE_SUPERTYPE_NOT_MARKED_AS_LAYER_SUPERTYPE));
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
        Map<String, IProductCmptProperty> propertyMap = productCmptType.findProductCmptPropertyMap(ipsProject);
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
        propertyMap = ((ProductCmptType)subtype)
                .findProductCmptPropertyMap(ProductCmptPropertyType.POLICY_CMPT_TYPE_ATTRIBUTE, ipsProject);
        assertEquals(2, propertyMap.size());
        assertEquals(policyCmptSupertypeAttr, propertyMap.get(policyCmptSupertypeAttr.getPropertyName()));
        assertEquals(policyCmptTypeAttr, propertyMap.get(policyCmptTypeAttr.getPropertyName()));
    }

    /**
     * <strong>Scenario:</strong><br>
     * There is a product component type that configures a policy component type, both types possess
     * a super type. The policy component type contains an attribute which overwrites an attribute
     * from it's super type. The attribute in the super type is marked <em>productRelevant</em>,
     * whereas the attribute in the sub type is <em><strong>not</strong></em>.
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * The map should not contain any attribute because only the overwritten attribute should be
     * considered. The overwritten attribute however is not marked <em>productRelevant</em>. That
     * means it does not qualify as product component property.
     */
    @Test
    public void testFindProductCmptPropertyMap_ConsiderOverwrittenPolicyCmptTypeAttributesThatAreNotProductRelevant()
            throws CoreException {

        // Create types
        IPolicyCmptType superPolicyType = newPolicyAndProductCmptType(ipsProject, "PolicyTypeA", "ProductTypeA");
        IProductCmptType superProductType = superPolicyType.findProductCmptType(ipsProject);
        IPolicyCmptType policyType = newPolicyAndProductCmptType(ipsProject, "PolicyTypeB", "ProductTypeB");
        IProductCmptType productType = policyType.findProductCmptType(ipsProject);
        policyType.setSupertype(superPolicyType.getQualifiedName());
        productType.setSupertype(superProductType.getQualifiedName());

        // Create attributes
        IPolicyCmptTypeAttribute superAttribute = superPolicyType.newPolicyCmptTypeAttribute("test");
        superAttribute.setDatatype(Datatype.STRING.getQualifiedName());
        superAttribute.setProductRelevant(true);

        IPolicyCmptTypeAttribute overriddingAttribute = policyType.newPolicyCmptTypeAttribute("test");
        overriddingAttribute.setDatatype(Datatype.STRING.getQualifiedName());
        overriddingAttribute.setProductRelevant(false);
        overriddingAttribute.setOverwrite(true);

        // Verify
        Map<String, IProductCmptProperty> propertyMap = ((ProductCmptType)productType)
                .findProductCmptPropertyMap(ProductCmptPropertyType.POLICY_CMPT_TYPE_ATTRIBUTE, ipsProject);
        assertTrue(propertyMap.isEmpty());
    }

    /**
     * <strong>Scenario:</strong><br>
     * Same as
     * {@link #testFindProductCmptPropertyMap_ConsiderOverwrittenPolicyCmptTypeAttributesThatAreNotProductRelevant()}
     * , but with multiple hierarchy levels. In each hierarchy level the attribute is overwritten
     * and the <em>productRelevant</em> flag is changed. The lowest level sets the flag to
     * {@code false}.
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * Same as
     * {@link #testFindProductCmptPropertyMap_ConsiderOverwrittenPolicyCmptTypeAttributesThatAreNotProductRelevant()}.
     */
    @Test
    public void testFindProductCmptPropertyMap_ConsiderOverwrittenPolicyCmptTypeAttributesThatAreNotProductRelevant_MultipleHierarchyLevels()
            throws CoreException {

        // Create types
        IPolicyCmptType policyTypeA = newPolicyAndProductCmptType(ipsProject, "PolicyTypeA", "ProductTypeA");
        IProductCmptType productTypeA = policyTypeA.findProductCmptType(ipsProject);
        IPolicyCmptType policyTypeB = newPolicyAndProductCmptType(ipsProject, "PolicyTypeB", "ProductTypeB");
        IProductCmptType productTypeB = policyTypeB.findProductCmptType(ipsProject);
        IPolicyCmptType policyTypeC = newPolicyAndProductCmptType(ipsProject, "PolicyTypeC", "ProductTypeC");
        IProductCmptType productTypeC = policyTypeC.findProductCmptType(ipsProject);
        IPolicyCmptType policyTypeD = newPolicyAndProductCmptType(ipsProject, "PolicyTypeD", "ProductTypeD");
        IProductCmptType productTypeD = policyTypeD.findProductCmptType(ipsProject);

        policyTypeB.setSupertype(policyTypeA.getQualifiedName());
        productTypeB.setSupertype(productTypeA.getQualifiedName());
        policyTypeC.setSupertype(policyTypeB.getQualifiedName());
        productTypeC.setSupertype(productTypeB.getQualifiedName());
        policyTypeD.setSupertype(policyTypeC.getQualifiedName());
        productTypeD.setSupertype(productTypeC.getQualifiedName());

        // Create attributes
        IPolicyCmptTypeAttribute attributeA = policyTypeA.newPolicyCmptTypeAttribute("test");
        attributeA.setDatatype(Datatype.STRING.getQualifiedName());
        attributeA.setProductRelevant(true);

        IPolicyCmptTypeAttribute attributeB = policyTypeB.newPolicyCmptTypeAttribute("test");
        attributeB.setDatatype(Datatype.STRING.getQualifiedName());
        attributeB.setProductRelevant(false);
        attributeB.setOverwrite(true);

        IPolicyCmptTypeAttribute attributeC = policyTypeC.newPolicyCmptTypeAttribute("test");
        attributeC.setDatatype(Datatype.STRING.getQualifiedName());
        attributeC.setProductRelevant(true);
        attributeC.setOverwrite(true);

        IPolicyCmptTypeAttribute attributeD = policyTypeD.newPolicyCmptTypeAttribute("test");
        attributeD.setDatatype(Datatype.STRING.getQualifiedName());
        attributeD.setProductRelevant(false);
        attributeD.setOverwrite(true);

        // Verify
        Map<String, IProductCmptProperty> propertyMap = ((ProductCmptType)productTypeD)
                .findProductCmptPropertyMap(ProductCmptPropertyType.POLICY_CMPT_TYPE_ATTRIBUTE, ipsProject);
        assertTrue(propertyMap.isEmpty());
    }

    /**
     * <strong>Scenario:</strong><br>
     * There is a product component type that configures a policy component type, both types possess
     * a super type. The policy component type contains an attribute which overwrites an attribute
     * from it's super type. The attribute in the super type is <em><strong>not</strong></em> marked
     * <em>productRelevant</em>, whereas the attribute in the sub type is.
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * The map should contain only the overwritten attribute.
     */
    @Test
    public void testFindProductCmptPropertyMap_ConsiderOverwrittenPolicyCmptTypeAttributesThatAreProductRelevant()
            throws CoreException {

        // Create types
        IPolicyCmptType policyTypeA = newPolicyAndProductCmptType(ipsProject, "PolicyTypeA", "ProductTypeA");
        IProductCmptType productTypeA = policyTypeA.findProductCmptType(ipsProject);
        IPolicyCmptType policyTypeB = newPolicyAndProductCmptType(ipsProject, "PolicyTypeB", "ProductTypeB");
        IProductCmptType productTypeB = policyTypeB.findProductCmptType(ipsProject);
        policyTypeB.setSupertype(policyTypeA.getQualifiedName());
        productTypeB.setSupertype(productTypeA.getQualifiedName());

        // Create attributes
        IPolicyCmptTypeAttribute attributeA = policyTypeA.newPolicyCmptTypeAttribute("test");
        attributeA.setDatatype(Datatype.STRING.getQualifiedName());
        attributeA.setProductRelevant(false);

        IPolicyCmptTypeAttribute attributeB = policyTypeB.newPolicyCmptTypeAttribute("test");
        attributeB.setDatatype(Datatype.STRING.getQualifiedName());
        attributeB.setProductRelevant(true);
        attributeB.setOverwrite(true);

        // Verify
        Map<String, IProductCmptProperty> propertyMap = ((ProductCmptType)productTypeB)
                .findProductCmptPropertyMap(ProductCmptPropertyType.POLICY_CMPT_TYPE_ATTRIBUTE, ipsProject);
        assertSame(attributeB, propertyMap.get("test"));
    }

    /**
     * <strong>Scenario:</strong><br>
     * Same as
     * {@link #testFindProductCmptPropertyMap_ConsiderOverwrittenPolicyCmptTypeAttributesThatAreProductRelevant()}
     * , but with multiple hierarchy levels. In each hierarchy level the attribute is overwritten
     * and the <em>productRelevant</em> flag is changed. The lowest level sets the flag to
     * {@code true}.
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * Same as
     * {@link #testFindProductCmptPropertyMap_ConsiderOverwrittenPolicyCmptTypeAttributesThatAreProductRelevant()}.
     */
    @Test
    public void testFindProductCmptPropertyMap_ConsiderOverwrittenPolicyCmptTypeAttributesThatAreProductRelevant_MultipleHierarchyLevels()
            throws CoreException {

        // Create types
        IPolicyCmptType policyTypeA = newPolicyAndProductCmptType(ipsProject, "PolicyTypeA", "ProductTypeA");
        IProductCmptType productTypeA = policyTypeA.findProductCmptType(ipsProject);
        IPolicyCmptType policyTypeB = newPolicyAndProductCmptType(ipsProject, "PolicyTypeB", "ProductTypeB");
        IProductCmptType productTypeB = policyTypeB.findProductCmptType(ipsProject);
        IPolicyCmptType policyTypeC = newPolicyAndProductCmptType(ipsProject, "PolicyTypeC", "ProductTypeC");
        IProductCmptType productTypeC = policyTypeC.findProductCmptType(ipsProject);
        IPolicyCmptType policyTypeD = newPolicyAndProductCmptType(ipsProject, "PolicyTypeD", "ProductTypeD");
        IProductCmptType productTypeD = policyTypeD.findProductCmptType(ipsProject);

        policyTypeB.setSupertype(policyTypeA.getQualifiedName());
        productTypeB.setSupertype(productTypeA.getQualifiedName());
        policyTypeC.setSupertype(policyTypeB.getQualifiedName());
        productTypeC.setSupertype(productTypeB.getQualifiedName());
        policyTypeD.setSupertype(policyTypeC.getQualifiedName());
        productTypeD.setSupertype(productTypeC.getQualifiedName());

        // Create attributes
        IPolicyCmptTypeAttribute attributeA = policyTypeA.newPolicyCmptTypeAttribute("test");
        attributeA.setDatatype(Datatype.STRING.getQualifiedName());
        attributeA.setProductRelevant(false);

        IPolicyCmptTypeAttribute attributeB = policyTypeB.newPolicyCmptTypeAttribute("test");
        attributeB.setDatatype(Datatype.STRING.getQualifiedName());
        attributeB.setProductRelevant(true);
        attributeB.setOverwrite(true);

        IPolicyCmptTypeAttribute attributeC = policyTypeC.newPolicyCmptTypeAttribute("test");
        attributeC.setDatatype(Datatype.STRING.getQualifiedName());
        attributeC.setProductRelevant(false);
        attributeC.setOverwrite(true);

        IPolicyCmptTypeAttribute attributeD = policyTypeD.newPolicyCmptTypeAttribute("test");
        attributeD.setDatatype(Datatype.STRING.getQualifiedName());
        attributeD.setProductRelevant(true);
        attributeD.setOverwrite(true);

        // Verify
        Map<String, IProductCmptProperty> propertyMap = ((ProductCmptType)productTypeD)
                .findProductCmptPropertyMap(ProductCmptPropertyType.POLICY_CMPT_TYPE_ATTRIBUTE, ipsProject);
        assertSame(attributeD, propertyMap.get("test"));
    }

    /**
     * <strong>Scenario:</strong><br>
     * An {@link IPolicyCmptTypeAttribute} is overwritten in a sub type, setting the
     * <em>productRelevant</em> flag from {@code false} to {@code true}. In addition, there also is
     * an {@link IValidationRule}.
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * The returned map should only contain the attribute or only contain the validation rule
     * depending on the provided {@link ProductCmptPropertyType}.
     */
    @Test
    public void testFindProductCmptPropertyMap_OverwrittenAttributeToProductRelevantPlusValidationRule()
            throws CoreException {

        // Create types
        IPolicyCmptType policyTypeA = newPolicyAndProductCmptType(ipsProject, "PolicyTypeA", "ProductTypeA");
        IProductCmptType productTypeA = policyTypeA.findProductCmptType(ipsProject);
        IPolicyCmptType policyTypeB = newPolicyAndProductCmptType(ipsProject, "PolicyTypeB", "ProductTypeB");
        IProductCmptType productTypeB = policyTypeB.findProductCmptType(ipsProject);
        policyTypeB.setSupertype(policyTypeA.getQualifiedName());
        productTypeB.setSupertype(productTypeA.getQualifiedName());

        // Create attributes
        IPolicyCmptTypeAttribute attributeA = policyTypeA.newPolicyCmptTypeAttribute("attribute");
        attributeA.setDatatype(Datatype.STRING.getQualifiedName());
        attributeA.setProductRelevant(false);

        IPolicyCmptTypeAttribute attributeB = policyTypeB.newPolicyCmptTypeAttribute("attribute");
        attributeB.setDatatype(Datatype.STRING.getQualifiedName());
        attributeB.setProductRelevant(true);
        attributeB.setOverwrite(true);

        // Create validation rule
        IValidationRule validationRule = policyTypeB.newRule();
        validationRule.setName("validationRule");
        validationRule.setConfigurableByProductComponent(true);

        // Verify
        Map<String, IProductCmptProperty> attributeMap = ((ProductCmptType)productTypeB)
                .findProductCmptPropertyMap(ProductCmptPropertyType.POLICY_CMPT_TYPE_ATTRIBUTE, ipsProject);
        assertSame(attributeB, attributeMap.get("attribute"));
        assertEquals(1, attributeMap.size());

        Map<String, IProductCmptProperty> validationRuleMap = ((ProductCmptType)productTypeB)
                .findProductCmptPropertyMap(ProductCmptPropertyType.VALIDATION_RULE, ipsProject);
        assertSame(validationRule, validationRuleMap.get("validationRule"));
        assertEquals(1, validationRuleMap.size());

        Map<String, IProductCmptProperty> allPropertiesMap = ((ProductCmptType)productTypeB)
                .findProductCmptPropertyMap(ipsProject);
        assertSame(attributeB, allPropertiesMap.get("attribute"));
        assertSame(validationRule, allPropertiesMap.get("validationRule"));
        assertEquals(2, allPropertiesMap.size());
    }

    @Test
    public void testFindProductCmptProperty_ByTypeAndName() {
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

        assertEquals(typeTsu, productCmptType.findProductCmptProperty(ProductCmptPropertyType.TABLE_STRUCTURE_USAGE,
                typeTsu.getRoleName(), ipsProject));
        assertEquals(supertypeTsu, productCmptType.findProductCmptProperty(
                ProductCmptPropertyType.TABLE_STRUCTURE_USAGE, supertypeTsu.getRoleName(), ipsProject));
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
    public void testFindProductCmptProperty_ByName() {
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

        assertEquals(typeSignature,
                productCmptType.findProductCmptProperty(typeSignature.getFormulaName(), ipsProject));
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
    public void testFindProductCmptProperty_ByReference() throws CoreException {
        IPolicyCmptTypeAttribute policyAttribute = policyCmptType.newPolicyCmptTypeAttribute("policyAttribute");
        policyAttribute.setProductRelevant(true);
        IValidationRule validationRule = policyCmptType.newRule();
        validationRule.setName("validationRule");
        validationRule.setConfigurableByProductComponent(true);
        IProductCmptTypeMethod formula = productCmptType.newProductCmptTypeMethod();
        formula.setName("formula");
        formula.setFormulaName("formula");
        formula.setFormulaSignatureDefinition(true);
        ITableStructureUsage tsu = productCmptType.newTableStructureUsage();
        tsu.setRoleName("tsu");
        IProductCmptTypeAttribute productAttribute = productCmptType.newProductCmptTypeAttribute("productAttribute");

        IProductCmptPropertyReference policyAttributeReference = new ProductCmptPropertyReference(productCmptType,
                "id1");
        policyAttributeReference.setReferencedProperty(policyAttribute);

        IProductCmptPropertyReference validationRuleReference = new ProductCmptPropertyReference(productCmptType,
                "id2");
        validationRuleReference.setReferencedProperty(validationRule);

        IProductCmptPropertyReference formulaReference = new ProductCmptPropertyReference(productCmptType, "id3");
        formulaReference.setReferencedProperty(formula);

        IProductCmptPropertyReference tsuReference = new ProductCmptPropertyReference(productCmptType, "id4");
        tsuReference.setReferencedProperty(tsu);

        IProductCmptPropertyReference productAttributeReference = new ProductCmptPropertyReference(productCmptType,
                "id5");
        productAttributeReference.setReferencedProperty(productAttribute);

        assertEquals(policyAttribute, productCmptType.findProductCmptProperty(policyAttributeReference, ipsProject));
        assertEquals(validationRule, productCmptType.findProductCmptProperty(validationRuleReference, ipsProject));
        assertEquals(formula, productCmptType.findProductCmptProperty(formulaReference, ipsProject));
        assertEquals(tsu, productCmptType.findProductCmptProperty(tsuReference, ipsProject));
        assertEquals(productAttribute, productCmptType.findProductCmptProperty(productAttributeReference, ipsProject));
    }

    @Test
    public void testFindProductCmptProperty_ByReferencePolicyCmptTypeNotFound() throws CoreException {
        IPolicyCmptTypeAttribute policyAttribute = policyCmptType.newPolicyCmptTypeAttribute("policyAttribute");
        policyAttribute.setProductRelevant(true);

        IProductCmptPropertyReference policyAttributeReference = new ProductCmptPropertyReference(productCmptType,
                "id1");
        policyAttributeReference.setReferencedProperty(policyAttribute);

        policyCmptType.delete();

        // Null should be returned, but no exception may be thrown
        assertNull(productCmptType.findProductCmptProperty(policyAttributeReference, ipsProject));
    }

    @Test
    public void testFindProductCmptProperty_ByReferenceSameIdInPolicyTypeAndProductType() throws CoreException,
            SecurityException, IllegalArgumentException, NoSuchFieldException, IllegalAccessException {

        IPolicyCmptTypeAttribute policyAttribute = policyCmptType.newPolicyCmptTypeAttribute("policyAttribute");
        policyAttribute.setName("policyAttribute");
        policyAttribute.setProductRelevant(true);
        setPartId(policyAttribute, "foo");
        IProductCmptTypeAttribute productAttribute = productCmptType.newProductCmptTypeAttribute("productAttribute");
        productAttribute.setName("productAttribute");
        setPartId(productAttribute, "foo");

        IProductCmptPropertyReference policyAttributeReference = new ProductCmptPropertyReference(productCmptType,
                "id1");
        policyAttributeReference.setReferencedProperty(policyAttribute);
        IProductCmptPropertyReference productAttributeReference = new ProductCmptPropertyReference(productCmptType,
                "id1");
        productAttributeReference.setReferencedProperty(productAttribute);

        assertEquals(policyAttribute, productCmptType.findProductCmptProperty(policyAttributeReference, ipsProject));
        assertEquals(productAttribute, productCmptType.findProductCmptProperty(productAttributeReference, ipsProject));
    }

    @Test
    public void testFindFormulaSignature() {
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
    public void testValidatePolicyCmptTypeHasErrors() throws CoreException {
        MessageList ml = productCmptType.validate(ipsProject);
        assertThat(ml, isEmpty());

        policyCmptType.setSupertype("foo");
        ml = productCmptType.validate(ipsProject);
        assertThat(ml, hasMessageCode(IProductCmptType.MSGCODE_POLICY_CMPT_TYPE_NOT_VALID));
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
    public void testNewCategory() {
        IProductCmptCategory category = productCmptType.newCategory();
        assertNotNull(category);
        assertEquals(productCmptType, category.getParent());
    }

    @Test
    public void testNewCategory_WithName() {
        IProductCmptCategory category = productCmptType.newCategory("foo");
        assertEquals("foo", category.getName());
    }

    @Test
    public void testNewCategory_WithNameAndPosition() {
        IProductCmptCategory category = productCmptType.newCategory("foo", Position.RIGHT);
        assertEquals("foo", category.getName());
        assertEquals(Position.RIGHT, category.getPosition());
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
    public void testFindSupertype() {
        assertEquals(superProductCmptType, productCmptType.findSupertype(ipsProject));
        assertNull(superSuperProductCmptType.findSupertype(ipsProject));
        productCmptType.setSupertype("unknownType");
        assertNull(productCmptType.findSupertype(ipsProject));
    }

    @Test
    public void testFindTableStructureUsageInSupertypeHierarchy() {
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
    public void testFindPolicyCmptType() {
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

        assertEquals(a1, getLastContentChangeEvent().getPart());
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

        aProductType.setPolicyCmptType(null);

        List<IDependency> dependencies = Arrays.asList(aProductType.dependsOn());
        assertEquals(1, dependencies.size());
        assertTrue(dependencies.contains(IpsObjectDependency.create(aProductType.getQualifiedNameType(),
                new QualifiedNameType(aProductType.getQualifiedName(), IpsObjectType.POLICY_CMPT_TYPE),
                DependencyType.VALIDATION)));

        aProductType.setPolicyCmptType(a.getQualifiedName());

        dependencies = Arrays.asList(aProductType.dependsOn());
        assertEquals(2, dependencies.size());
        assertTrue(dependencies.contains(IpsObjectDependency.create(aProductType.getQualifiedNameType(),
                new QualifiedNameType(aProductType.getQualifiedName(), IpsObjectType.POLICY_CMPT_TYPE),
                DependencyType.VALIDATION)));

        IDependency dependency = IpsObjectDependency.create(aProductType.getQualifiedNameType(),
                a.getQualifiedNameType(), DependencyType.CONFIGURES);
        assertTrue(dependencies.contains(dependency));
        assertSingleDependencyDetail(aProductType, dependency, aProductType,
                IProductCmptType.PROPERTY_POLICY_CMPT_TYPE);

        IAssociation aProductTypeTobProductType = aProductType.newAssociation();
        aProductTypeTobProductType.setTarget(bProductType.getQualifiedName());

        dependencies = Arrays.asList(aProductType.dependsOn());
        assertEquals(3, dependencies.size());
        assertTrue(dependencies.contains(IpsObjectDependency.create(aProductType.getQualifiedNameType(),
                new QualifiedNameType(aProductType.getQualifiedName(), IpsObjectType.POLICY_CMPT_TYPE),
                DependencyType.VALIDATION)));

        dependency = IpsObjectDependency.create(aProductType.getQualifiedNameType(), a.getQualifiedNameType(),
                DependencyType.CONFIGURES);
        assertTrue(dependencies.contains(dependency));
        assertSingleDependencyDetail(aProductType, dependency, aProductType,
                IProductCmptType.PROPERTY_POLICY_CMPT_TYPE);

        dependency = IpsObjectDependency.create(aProductType.getQualifiedNameType(),
                bProductType.getQualifiedNameType(), DependencyType.REFERENCE);
        assertTrue(dependencies.contains(dependency));
        assertSingleDependencyDetail(aProductType, dependency, aProductTypeTobProductType,
                IAssociation.PROPERTY_TARGET);

        IAttribute aAttr = aProductType.newAttribute();
        aAttr.setDatatype(Datatype.MONEY.getQualifiedName());

        dependencies = Arrays.asList(aProductType.dependsOn());
        assertEquals(4, dependencies.size());
        assertTrue(dependencies.contains(IpsObjectDependency.create(aProductType.getQualifiedNameType(),
                new QualifiedNameType(aProductType.getQualifiedName(), IpsObjectType.POLICY_CMPT_TYPE),
                DependencyType.VALIDATION)));

        dependency = IpsObjectDependency.create(aProductType.getQualifiedNameType(), a.getQualifiedNameType(),
                DependencyType.CONFIGURES);
        assertTrue(dependencies.contains(dependency));
        assertSingleDependencyDetail(aProductType, dependency, aProductType,
                IProductCmptType.PROPERTY_POLICY_CMPT_TYPE);

        dependency = IpsObjectDependency.create(aProductType.getQualifiedNameType(),
                bProductType.getQualifiedNameType(), DependencyType.REFERENCE);
        assertTrue(dependencies.contains(dependency));
        assertSingleDependencyDetail(aProductType, dependency, aProductTypeTobProductType,
                IAssociation.PROPERTY_TARGET);

        dependency = new DatatypeDependency(aProductType.getQualifiedNameType(), Datatype.MONEY.getQualifiedName());
        assertTrue(dependencies.contains(dependency));
        assertSingleDependencyDetail(aProductType, dependency, aAttr, IAttribute.PROPERTY_DATATYPE);

        IMethod aMethod = aProductType.newMethod();
        aMethod.setDatatype(Datatype.DECIMAL.getQualifiedName());

        dependencies = Arrays.asList(aProductType.dependsOn());
        assertEquals(5, dependencies.size());
        assertTrue(dependencies.contains(IpsObjectDependency.create(aProductType.getQualifiedNameType(),
                new QualifiedNameType(aProductType.getQualifiedName(), IpsObjectType.POLICY_CMPT_TYPE),
                DependencyType.VALIDATION)));

        dependency = IpsObjectDependency.create(aProductType.getQualifiedNameType(), a.getQualifiedNameType(),
                DependencyType.CONFIGURES);
        assertTrue(dependencies.contains(dependency));
        assertSingleDependencyDetail(aProductType, dependency, aProductType,
                IProductCmptType.PROPERTY_POLICY_CMPT_TYPE);

        dependency = IpsObjectDependency.create(aProductType.getQualifiedNameType(),
                bProductType.getQualifiedNameType(), DependencyType.REFERENCE);
        assertTrue(dependencies.contains(dependency));
        assertSingleDependencyDetail(aProductType, dependency, aProductTypeTobProductType,
                IAssociation.PROPERTY_TARGET);

        dependency = new DatatypeDependency(aProductType.getQualifiedNameType(), Datatype.MONEY.getQualifiedName());
        assertTrue(dependencies.contains(dependency));
        assertSingleDependencyDetail(aProductType, dependency, aAttr, IAttribute.PROPERTY_DATATYPE);

        dependency = new DatatypeDependency(aProductType.getQualifiedNameType(), Datatype.DECIMAL.getQualifiedName());
        assertTrue(dependencies.contains(dependency));
        assertSingleDependencyDetail(aProductType, dependency, aMethod, IBaseMethod.PROPERTY_DATATYPE);
    }

    @Test
    public void testDependsOn_Tables() throws CoreException {
        ITableStructure a = newTableStructure(ipsProject, "A");
        ITableStructure b = newTableStructure(ipsProject, "B");
        ITableStructure c = newTableStructure(ipsProject, "C");

        IProductCmptType aProductType = newProductCmptType(ipsProject, "D");
        IProductCmptType bProductType = newProductCmptType(ipsProject, "E");

        List<IDependency> dependencies0 = Arrays.asList(aProductType.dependsOn());
        assertEquals(1, dependencies0.size());

        ITableStructureUsage structureUsage = aProductType.newTableStructureUsage();
        structureUsage.setRoleName("Table");
        structureUsage.addTableStructure(a.getQualifiedName());

        ITableStructureUsage structureUsage2 = bProductType.newTableStructureUsage();
        structureUsage2.setRoleName("Table2");
        structureUsage2.addTableStructure(b.getQualifiedName());

        List<IDependency> dependencies = Arrays.asList(aProductType.dependsOn());
        // dependencies always have a reference to the policy type of the product component type,
        // hence, assertEquals is the number of found referenced tables +1.
        assertEquals(2, dependencies.size());
        assertTrue(dependencies.contains(IpsObjectDependency.create(aProductType.getQualifiedNameType(),
                new QualifiedNameType(a.getQualifiedName(), IpsObjectType.TABLE_STRUCTURE), DependencyType.REFERENCE)));

        List<IDependency> dependencies2 = Arrays.asList(bProductType.dependsOn());
        assertEquals(2, dependencies2.size());
        assertTrue(dependencies2.contains(IpsObjectDependency.create(bProductType.getQualifiedNameType(),
                b.getQualifiedNameType(), DependencyType.REFERENCE)));

        ITableStructureUsage structureUsage3 = aProductType.newTableStructureUsage();
        structureUsage3.setRoleName("Table3");
        structureUsage3.addTableStructure(c.getQualifiedName());

        dependencies = Arrays.asList(aProductType.dependsOn());
        assertEquals(3, dependencies.size());
        assertTrue(dependencies.contains(IpsObjectDependency.create(aProductType.getQualifiedNameType(),
                new QualifiedNameType(a.getQualifiedName(), IpsObjectType.TABLE_STRUCTURE), DependencyType.REFERENCE)));
        assertTrue(dependencies.contains(IpsObjectDependency.create(aProductType.getQualifiedNameType(),
                new QualifiedNameType(c.getQualifiedName(), IpsObjectType.TABLE_STRUCTURE), DependencyType.REFERENCE)));

    }

    @Test
    public void testGetPropertyReferences() throws CoreException {
        IProductCmptProperty p1 = createProductAttributeProperty(productCmptType, "p1");
        IProductCmptProperty p2 = createProductAttributeProperty(productCmptType, "p2");

        // If no move has been performed, the references are empty
        assertTrue(productCmptType.getPropertyReferences().isEmpty());

        // As soon as a move is performed, the references are created
        productCmptType.movePropertyReferences(new int[] { 0 }, Arrays.asList(p1, p2), false);
        List<IProductCmptPropertyReference> propertyReferences = productCmptType.getPropertyReferences();
        assertEquals(p2, propertyReferences.get(0).findProductCmptProperty(ipsProject));
        assertEquals(p1, propertyReferences.get(1).findProductCmptProperty(ipsProject));
        assertEquals(2, propertyReferences.size());
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

        assertTrue(getLastContentChangeEvent().isAffected(a1));
        assertTrue(getLastContentChangeEvent().isAffected(a2));
        assertTrue(getLastContentChangeEvent().isAffected(a3));
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
        assertTrue(productCmptType.isChangingOverTime());
    }

    @Test
    public void testToXml() throws CoreException {
        productCmptType.setConfigurationForPolicyCmptType(true);
        productCmptType.setPolicyCmptType(policyCmptType.getQualifiedName());
        productCmptType.newProductCmptTypeAttribute().setName("attr");
        productCmptType.newProductCmptTypeAssociation().setTargetRoleSingular("role");
        productCmptType.newTableStructureUsage().setRoleName("roleTsu");
        productCmptType.newMethod().setName("method1");
        productCmptType.setChangingOverTime(false);

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
        assertFalse(productCmptType.isChangingOverTime());
    }

    @Test
    public void testSavePropertyReferencesToXml() throws CoreException {
        IProductCmptProperty property1 = productCmptType.newProductCmptTypeAttribute("p1");
        IProductCmptProperty property2 = productCmptType.newProductCmptTypeAttribute("p2");
        IProductCmptProperty property3 = productCmptType.newProductCmptTypeAttribute("p3");
        productCmptType.movePropertyReferences(new int[] { 1 }, Arrays.asList(property1, property2, property3), true);

        Element xmlElement = productCmptType.toXml(newDocument());
        productCmptType.delete();
        productCmptType = newProductCmptType(ipsProject, productCmptType.getQualifiedName());
        productCmptType.initFromXml(xmlElement);

        List<IProductCmptProperty> properties = findProductCmptPropertiesInOrder(productCmptType, false, ipsProject);
        assertEquals(property2.getName(), properties.get(0).getName());
        assertEquals(property1.getName(), properties.get(1).getName());
        assertEquals(property3.getName(), properties.get(2).getName());
    }

    @Test
    public void testDoNotSaveObsoletePropertyReferencesToXml() throws CoreException {
        IProductCmptProperty property1 = productCmptType.newProductCmptTypeAttribute("p1");
        IProductCmptProperty property2 = productCmptType.newProductCmptTypeAttribute("p2");
        IProductCmptProperty property3 = productCmptType.newProductCmptTypeAttribute("p3");
        productCmptType.movePropertyReferences(new int[] { 2, 1 }, Arrays.asList(property1, property2, property3),
                true);

        // Make reference obsolete by deleting the property
        property2.delete();

        Element xmlElement = productCmptType.toXml(newDocument());
        productCmptType.delete();
        productCmptType = newProductCmptType(ipsProject, productCmptType.getQualifiedName());
        productCmptType.initFromXml(xmlElement);

        // Re-create the property to test whether it is listed at the end again
        property2 = productCmptType.newProductCmptTypeAttribute("p2");

        List<IProductCmptProperty> properties = findProductCmptPropertiesInOrder(productCmptType, false, ipsProject);
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
    public void testValidate_NoDefaultCategoryForFormulaSignatureDefinitionsExistsButThereAlsoExistsNoFormulaSignatureDefinition()
            throws CoreException {

        productCmptType.findDefaultCategoryForFormulaSignatureDefinitions(ipsProject).delete();
        policyCmptType.setSupertype("");
        productCmptType.setSupertype("");

        assertTrue(productCmptType.isValid());
    }

    @Test
    public void testValidate_NoDefaultCategoryForFormulaSignatureDefinitionsExistsEvenThoughAFormulaSignatureDefinitionExists()
            throws CoreException {

        productCmptType.findDefaultCategoryForFormulaSignatureDefinitions(ipsProject).delete();
        policyCmptType.setSupertype("");
        productCmptType.setSupertype("");

        IProductCmptTypeMethod formula = productCmptType.newFormulaSignature("myFormula");
        formula.setDatatype(Datatype.INTEGER.getQualifiedName());

        MessageList validationMessageList = productCmptType.validate(ipsProject);
        assertOneValidationMessage(validationMessageList,
                IProductCmptType.MSGCODE_NO_DEFAULT_CATEGORY_FOR_FORMULA_SIGNATURE_DEFINITIONS, productCmptType, null,
                Message.ERROR);
    }

    @Test
    public void testValidate_NoDefaultCategoryForPolicyCmptTypeAttributesExistsButThereAlsoExistsNoProductRelevantPolicyCmptTypeAttribute()
            throws CoreException {

        productCmptType.findDefaultCategoryForPolicyCmptTypeAttributes(ipsProject).delete();
        policyCmptType.setSupertype("");
        productCmptType.setSupertype("");

        IPolicyCmptTypeAttribute policyAttribute = policyCmptType
                .newPolicyCmptTypeAttribute("notAProductRelevantAttribute");
        policyAttribute.setDatatype(Datatype.INTEGER.getQualifiedName());

        assertTrue(productCmptType.isValid());
    }

    @Test
    public void testValidate_NoDefaultCategoryForPolicyCmptTypeAttributesExistsEvenThoughAProductRelevantPolicyCmptTypeAttributeExists()
            throws CoreException {

        productCmptType.findDefaultCategoryForPolicyCmptTypeAttributes(ipsProject).delete();
        policyCmptType.setSupertype("");
        productCmptType.setSupertype("");

        IPolicyCmptTypeAttribute policyAttribute = policyCmptType
                .newPolicyCmptTypeAttribute("productRelevantAttribute");
        policyAttribute.setDatatype(Datatype.INTEGER.getQualifiedName());
        policyAttribute.setProductRelevant(true);

        MessageList validationMessageList = productCmptType.validate(ipsProject);
        assertOneValidationMessage(validationMessageList,
                IProductCmptType.MSGCODE_NO_DEFAULT_CATEGORY_FOR_POLICY_CMPT_TYPE_ATTRIBUTES, productCmptType, null,
                Message.ERROR);
    }

    @Test
    public void testValidate_NoDefaultCategoryForProductCmptTypeAttributesExistsButThereAlsoExistsNoProductCmptTypeAttribute()
            throws CoreException {

        productCmptType.findDefaultCategoryForProductCmptTypeAttributes(ipsProject).delete();
        policyCmptType.setSupertype("");
        productCmptType.setSupertype("");

        assertTrue(productCmptType.isValid());
    }

    @Test
    public void testValidate_NoDefaultCategoryForProductCmptTypeAttributesExistsEvenThoughAProductCmptTypeAttributeExists()
            throws CoreException {

        productCmptType.findDefaultCategoryForProductCmptTypeAttributes(ipsProject).delete();
        policyCmptType.setSupertype("");
        productCmptType.setSupertype("");

        IProductCmptTypeAttribute productAttribute = productCmptType.newProductCmptTypeAttribute("productAttribute");
        productAttribute.setDatatype(Datatype.INTEGER.getQualifiedName());

        MessageList validationMessageList = productCmptType.validate(ipsProject);
        assertOneValidationMessage(validationMessageList,
                IProductCmptType.MSGCODE_NO_DEFAULT_CATEGORY_FOR_PRODUCT_CMPT_TYPE_ATTRIBUTES, productCmptType, null,
                Message.ERROR);
    }

    @Test
    public void testValidate_NoDefaultCategoryForTableStructureUsagesExistsButThereAlsoExistsNoTableStructureUsage()
            throws CoreException {

        productCmptType.findDefaultCategoryForTableStructureUsages(ipsProject).delete();
        policyCmptType.setSupertype("");
        productCmptType.setSupertype("");

        assertTrue(productCmptType.isValid());
    }

    @Test
    public void testValidate_NoDefaultCategoryForTableStructureUsagesExistsEvenThoughATableStructureUsageExists()
            throws CoreException {

        productCmptType.findDefaultCategoryForTableStructureUsages(ipsProject).delete();
        policyCmptType.setSupertype("");
        productCmptType.setSupertype("");

        ITableStructure tableStructure = newTableStructure(ipsProject, "MyTableStructure");
        ITableStructureUsage tsu = productCmptType.newTableStructureUsage();
        tsu.setRoleName("myTable");
        tsu.addTableStructure(tableStructure.getQualifiedName());

        MessageList validationMessageList = productCmptType.validate(ipsProject);
        assertOneValidationMessage(validationMessageList,
                IProductCmptType.MSGCODE_NO_DEFAULT_CATEGORY_FOR_TABLE_STRUCTURE_USAGES, productCmptType, null,
                Message.ERROR);
    }

    @Test
    public void testValidate_NoDefaultCategoryForValidationRulesExistsButThereAlsoExistsNoValidationRule()
            throws CoreException {

        productCmptType.findDefaultCategoryForValidationRules(ipsProject).delete();
        policyCmptType.setSupertype("");
        productCmptType.setSupertype("");

        IValidationRule rule = policyCmptType.newRule();
        rule.setName("myRule");

        assertTrue(productCmptType.isValid());
    }

    @Test
    public void testValidate_NoDefaultCategoryForValidationRulesExistsEvenThoughAConfigurableValidationRuleExsits()
            throws CoreException {

        productCmptType.findDefaultCategoryForValidationRules(ipsProject).delete();
        policyCmptType.setSupertype("");
        productCmptType.setSupertype("");

        IValidationRule rule = policyCmptType.newRule();
        rule.setName("myRule");
        rule.setMessageCode("myCode");
        rule.setConfigurableByProductComponent(true);

        MessageList validationMessageList = productCmptType.validate(ipsProject);
        assertOneValidationMessage(validationMessageList,
                IProductCmptType.MSGCODE_NO_DEFAULT_CATEGORY_FOR_VALIDATION_RULES, productCmptType, null,
                Message.ERROR);
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
    public void testFindProductCmptProperties() {
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
    public void testFindProductCmptProperties_SearchForSpecificPropertyType() {
        createProductAttributeProperty(productCmptType, "productAttribute");
        createTableStructureUsageProperty(productCmptType, "tsu");
        createPolicyAttributeProperty(policyCmptType, "policyAttribute");
        createValidationRuleProperty(policyCmptType, "validationRule");
        IProductCmptProperty formulaSignature = createFormulaSignatureProperty(productCmptType, "formula");

        List<IProductCmptProperty> properties = productCmptType
                .findProductCmptProperties(ProductCmptPropertyType.FORMULA_SIGNATURE_DEFINITION, true, ipsProject);
        assertTrue(properties.contains(formulaSignature));
        assertEquals(1, properties.size());
    }

    @Test
    public void testFindProductCmptProperties_SearchSupertypeHierarchy() {
        IProductCmptProperty productAttribute = createProductAttributeProperty(superProductCmptType,
                "productAttribute");
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
    public void testFindProductCmptProperties_DoNotSearchSupertypeHierarchy() {
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
    public void testFindProductCmptProperties_IgnoreNonProductRelevantProperties() {
        productCmptType.newProductCmptTypeMethod().setFormulaSignatureDefinition(false);
        policyCmptType.newPolicyCmptTypeAttribute();
        policyCmptType.newRule();

        List<IProductCmptProperty> properties = productCmptType.findProductCmptProperties(true, ipsProject);
        assertTrue(properties.isEmpty());
    }

    @Test
    public void testFindProductCmptPropertiesForCategory() throws CoreException {
        ProductCmptCategory category = (ProductCmptCategory)superProductCmptType.newCategory("category");

        IProductCmptProperty superProperty = superProductCmptType.newProductCmptTypeAttribute("superProperty");
        superProperty.setCategory(category.getName());
        IProductCmptProperty property = productCmptType.newProductCmptTypeAttribute("property");
        property.setCategory(category.getName());

        List<IProductCmptProperty> properties = category.findProductCmptProperties(productCmptType, true, ipsProject);
        assertEquals(superProperty, properties.get(0));
        assertEquals(property, properties.get(1));
        assertEquals(2, properties.size());
    }

    /**
     * <strong>Scenario:</strong><br>
     * The client wants to know all property assignments made by the {@link IProductCmptType}
     * itself, excluding all assignments made in the supertype hierarchy.
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * Only the assignments made by the {@link IProductCmptType} itself should be contained in the
     * returned list.
     */
    @Test
    public void testFindProductCmptPropertiesForCategory_NotSearchingSupertypeHierarchy() throws CoreException {
        ProductCmptCategory category = (ProductCmptCategory)superProductCmptType.newCategory("category");

        IProductCmptProperty superProperty = superProductCmptType.newProductCmptTypeAttribute("superProperty");
        superProperty.setCategory(category.getName());
        IProductCmptProperty property = productCmptType.newProductCmptTypeAttribute("property");
        property.setCategory(category.getName());

        List<IProductCmptProperty> properties = category.findProductCmptProperties(productCmptType, false, ipsProject);
        assertEquals(property, properties.get(0));
        assertEquals(1, properties.size());
    }

    /**
     * <strong>Scenario:</strong><br>
     * No properties are specifically assigned to a given {@link IProductCmptCategory}. However,
     * this {@link IProductCmptCategory} is marked to be a default {@link IProductCmptCategory} for
     * a {@link ProductCmptPropertyType} for which one {@link IProductCmptProperty} exists that has
     * no {@link IProductCmptCategory}.
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * The {@link IProductCmptProperty} should be contained in the returned list as properties with
     * no {@link IProductCmptCategory} are automatically assigned to the corresponding default
     * {@link IProductCmptCategory}.
     */
    @Test
    public void testFindProductCmptPropertiesForCategory_DefaultCategory() throws CoreException {
        ProductCmptCategory defaultAttributeCategory = (ProductCmptCategory)productCmptType
                .findDefaultCategoryForProductCmptTypeAttributes(ipsProject);
        IProductCmptProperty attribute = productCmptType.newProductCmptTypeAttribute("foo");

        assertEquals(attribute,
                defaultAttributeCategory.findProductCmptProperties(productCmptType, false, ipsProject).get(0));
    }

    /**
     * <strong>Scenario:</strong><br>
     * An {@link IPolicyCmptTypeAttribute} marked as <em>overwrite</em> is assigned to an
     * {@link IProductCmptCategory}.
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * The original {@link IPolicyCmptTypeAttribute} should not be returned.
     */
    @Test
    public void testFindProductCmptPropertiesForCategory_FilterOverwrittenAttributesForPolicyCmptTypeAttribute()
            throws CoreException {
        IProductCmptProperty superAttribute = createPolicyAttributeProperty(superPolicyCmptType,
                "overwrittenAttribute");
        IPolicyCmptTypeAttribute attribute = (IPolicyCmptTypeAttribute)createPolicyAttributeProperty(policyCmptType,
                "overwrittenAttribute");
        attribute.setOverwrite(true);

        ProductCmptCategory category = (ProductCmptCategory)superProductCmptType.newCategory("myCategory");
        superAttribute.setCategory(category.getName());
        attribute.setCategory(category.getName());

        List<IProductCmptProperty> properties = category.findProductCmptProperties(productCmptType, true, ipsProject);
        assertTrue(properties.contains(attribute));
        assertFalse(properties.contains(superAttribute));
    }

    /**
     * <strong>Scenario:</strong><br>
     * An {@link IProductCmptTypeAttribute} marked as <em>overwrite</em> is assigned to an
     * {@link IProductCmptCategory}.
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * The original {@link IProductCmptTypeAttribute} should not be returned.
     */
    @Test
    public void testFindProductCmptPropertiesForCategory_FilterOverwrittenAttributesForProductCmptTypeAttribute()
            throws CoreException {
        IProductCmptProperty superAttribute = createProductAttributeProperty(superProductCmptType,
                "overwrittenAttribute");
        IProductCmptTypeAttribute attribute = (IProductCmptTypeAttribute)createProductAttributeProperty(productCmptType,
                "overwrittenAttribute");
        attribute.setOverwrite(true);

        ProductCmptCategory category = (ProductCmptCategory)superProductCmptType.newCategory("myCategory");
        superAttribute.setCategory(category.getName());
        attribute.setCategory(category.getName());

        List<IProductCmptProperty> properties = category.findProductCmptProperties(productCmptType, true, ipsProject);
        assertTrue(properties.contains(attribute));
        assertFalse(properties.contains(superAttribute));
    }

    /**
     * <strong>Scenario:</strong><br>
     * An {@link IPolicyCmptTypeAttribute} marked as <em>overwrite</em> is assigned to an
     * {@link IProductCmptCategory}. The {@link IProductCmptCategory} of the overwriting
     * {@link IPolicyCmptTypeAttribute} is different from the {@link IProductCmptCategory} of the
     * original {@link IPolicyCmptTypeAttribute}.
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * The original {@link IPolicyCmptTypeAttribute} should not be returned as property of it's
     * {@link IProductCmptCategory} with the subtype as context type. However, it must still be
     * returned if the context type is the supertype.
     */
    @Test
    public void testFindProductCmptPropertiesForCategory_FilterOverwrittenAttributesAcrossDifferentCategoriesForPolicyCmptTypeAttribute()
            throws CoreException {

        IProductCmptProperty superAttribute = createPolicyAttributeProperty(superPolicyCmptType,
                "overwrittenAttribute");
        IPolicyCmptTypeAttribute attribute = (IPolicyCmptTypeAttribute)createPolicyAttributeProperty(policyCmptType,
                "overwrittenAttribute");
        attribute.setOverwrite(true);

        ProductCmptCategory category1 = (ProductCmptCategory)superProductCmptType.newCategory("category1");
        ProductCmptCategory category2 = (ProductCmptCategory)superProductCmptType.newCategory("category2");
        superAttribute.setCategory(category1.getName());
        attribute.setCategory(category2.getName());

        assertTrue(
                category1.findProductCmptProperties(superProductCmptType, true, ipsProject).contains(superAttribute));
        assertFalse(category1.findProductCmptProperties(productCmptType, true, ipsProject).contains(superAttribute));
        assertTrue(category2.findProductCmptProperties(productCmptType, true, ipsProject).contains(attribute));
        assertFalse(category2.findProductCmptProperties(productCmptType, true, ipsProject).contains(superAttribute));
    }

    /**
     * <strong>Scenario:</strong><br>
     * An {@link IProductCmptTypeAttribute} marked as <em>overwrite</em> is assigned to an
     * {@link IProductCmptCategory}. The {@link IProductCmptCategory} of the overwriting
     * {@link IProductCmptTypeAttribute} is different from the {@link IProductCmptCategory} of the
     * original {@link IProductCmptTypeAttribute}.
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * The original {@link IProductCmptTypeAttribute} should not be returned as property of it's
     * {@link IProductCmptCategory} with the subtype as context type. However, it must still be
     * returned if the context type is the supertype.
     */
    @Test
    public void testFindProductCmptPropertiesForCategory_FilterOverwrittenAttributesAcrossDifferentCategoriesForProductCmptTypeAttribute()
            throws CoreException {

        IProductCmptProperty superAttribute = createProductAttributeProperty(superProductCmptType,
                "overwrittenAttribute");
        IProductCmptTypeAttribute attribute = (IProductCmptTypeAttribute)createProductAttributeProperty(productCmptType,
                "overwrittenAttribute");
        attribute.setOverwrite(true);

        ProductCmptCategory category1 = (ProductCmptCategory)superProductCmptType.newCategory("category1");
        ProductCmptCategory category2 = (ProductCmptCategory)superProductCmptType.newCategory("category2");
        superAttribute.setCategory(category1.getName());
        attribute.setCategory(category2.getName());

        assertTrue(
                category1.findProductCmptProperties(superProductCmptType, true, ipsProject).contains(superAttribute));
        assertFalse(category1.findProductCmptProperties(productCmptType, true, ipsProject).contains(superAttribute));
        assertTrue(category2.findProductCmptProperties(productCmptType, true, ipsProject).contains(attribute));
        assertFalse(category2.findProductCmptProperties(productCmptType, true, ipsProject).contains(superAttribute));
    }

    /**
     * <strong>Scenario:</strong><br>
     * An {@link IProductCmptTypeMethod} marked as <em>overloaded formula signature</em> is assigned
     * to an {@link IProductCmptCategory}.
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * The original {@link IProductCmptTypeMethod} should not be returned.
     */
    @Test
    public void testFindProductCmptPropertiesForCategory_FilterOverloadedFormulas() throws CoreException {
        IProductCmptProperty superFormula = createFormulaSignatureProperty(superProductCmptType, "overloadedFormula");
        IProductCmptTypeMethod formula = (IProductCmptTypeMethod)createFormulaSignatureProperty(productCmptType,
                "overloadedFormula");
        formula.setOverloadsFormula(true);

        ProductCmptCategory category = (ProductCmptCategory)superProductCmptType.newCategory("myCategory");
        superFormula.setCategory(category.getName());
        formula.setCategory(category.getName());

        List<IProductCmptProperty> properties = category.findProductCmptProperties(productCmptType, true, ipsProject);
        assertTrue(properties.contains(formula));
        assertFalse(properties.contains(superFormula));
    }

    /**
     * <strong>Scenario:</strong><br>
     * An {@link IProductCmptTypeMethod} marked as <em>overloaded formula signature</em> is assigned
     * to an {@link IProductCmptCategory}. The {@link IProductCmptCategory} of the overloaded
     * {@link IProductCmptTypeMethod} is different from the {@link IProductCmptCategory} of the
     * original {@link IProductCmptTypeMethod}.
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * The original {@link IProductCmptTypeMethod} should not be returned as property of it's
     * {@link IProductCmptCategory} with the subtype as context type. However, it must still be
     * returned if the context type is the supertype.
     */
    @Test
    public void testFindProductCmptPropertiesForCategory_FilterOverloadedFormulasAcrossDifferentCategories()
            throws CoreException {

        IProductCmptProperty superFormula = createFormulaSignatureProperty(superProductCmptType, "overloadedFormula");
        IProductCmptTypeMethod formula = (IProductCmptTypeMethod)createFormulaSignatureProperty(productCmptType,
                "overloadedFormula");
        formula.setOverloadsFormula(true);

        ProductCmptCategory category1 = (ProductCmptCategory)superProductCmptType.newCategory("category1");
        ProductCmptCategory category2 = (ProductCmptCategory)superProductCmptType.newCategory("category2");
        superFormula.setCategory(category1.getName());
        formula.setCategory(category2.getName());

        assertTrue(category1.findProductCmptProperties(superProductCmptType, true, ipsProject).contains(superFormula));
        assertFalse(category1.findProductCmptProperties(productCmptType, true, ipsProject).contains(superFormula));
        assertTrue(category2.findProductCmptProperties(productCmptType, true, ipsProject).contains(formula));
        assertFalse(category2.findProductCmptProperties(productCmptType, true, ipsProject).contains(superFormula));
    }

    /**
     * <strong>Scenario:</strong><br>
     * An {@link IProductCmptCategory} is marked as default for a specific
     * {@link ProductCmptPropertyType}. Then, an {@link IProductCmptProperty} of this
     * {@link ProductCmptPropertyType} is assigned to an {@link IProductCmptCategory} of a sub type.
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * The {@link IProductCmptProperty} should not be assigned to the default
     * {@link IProductCmptCategory} of the supertype.
     */
    @Test
    public void testFindProductCmptPropertiesForCategory_DoNotAssignToDefaultIfPropertyInSubtypeCategory()
            throws CoreException {

        deleteAllCategories(productCmptType, superProductCmptType);

        IProductCmptCategory superCategory = superProductCmptType.newCategory("superCategory");
        superCategory.setDefaultForPolicyCmptTypeAttributes(true);

        IProductCmptCategory category = productCmptType.newCategory("category");
        IProductCmptProperty property = createPolicyAttributeProperty(policyCmptType, "policyAttribute");
        property.setCategory(category.getName());

        assertTrue(superCategory.findProductCmptProperties(productCmptType, true, ipsProject).isEmpty());
    }

    /**
     * <strong>Scenario:</strong><br>
     * The {@link IProductCmptCategory} of an {@link IProductCmptProperty} that belongs to an
     * {@link IPolicyCmptType} is changed, using the method
     * {@link IProductCmptType#changeCategoryAndDeferPolicyChange(IProductCmptProperty, String)}.
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * The finder must return the {@link IProductCmptProperty} for the property's new
     * {@link IProductCmptCategory} but not it's old {@link IProductCmptCategory}, even tough
     * {@link IProductCmptProperty#getCategory()} will always return the old
     * {@link IProductCmptCategory}.
     */
    @Test
    public void testFindProductCmptPropertiesForCategory_ConsiderChangedPolicyProperties() throws CoreException {
        IProductCmptCategory oldCategory = productCmptType.newCategory("oldCategory");
        IProductCmptCategory newCategory = productCmptType.newCategory("newCategory");

        IProductCmptProperty policyProperty = createPolicyAttributeProperty(policyCmptType, "policyProperty");
        policyProperty.setCategory(oldCategory.getName());

        productCmptType.changeCategoryAndDeferPolicyChange(policyProperty, newCategory.getName());

        assertFalse(oldCategory.findProductCmptProperties(productCmptType, false, ipsProject).contains(policyProperty));
        assertTrue(newCategory.findProductCmptProperties(productCmptType, false, ipsProject).contains(policyProperty));
    }

    /**
     * <strong>Scenario:</strong><br>
     * The {@link IProductCmptCategory} of an {@link IProductCmptProperty} that belongs to an
     * {@link IPolicyCmptType} is changed in the supertype, using the method
     * {@link IProductCmptType#changeCategoryAndDeferPolicyChange(IProductCmptProperty, String)}.
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * In the subtype, the finder must return the {@link IProductCmptProperty} for the property's
     * new {@link IProductCmptCategory} as well.
     */
    @Test
    public void testFindProductCmptPropertiesForCategory_ConsiderChangedPolicyPropertiesFromSupertypes()
            throws CoreException {

        ProductCmptCategory oldCategory = (ProductCmptCategory)superProductCmptType.newCategory("oldCategory");
        ProductCmptCategory newCategory = (ProductCmptCategory)superProductCmptType.newCategory("newCategory");

        IProductCmptProperty superPolicyProperty = createPolicyAttributeProperty(superPolicyCmptType,
                "superPolicyProperty");
        superPolicyProperty.setCategory(oldCategory.getName());

        superProductCmptType.changeCategoryAndDeferPolicyChange(superPolicyProperty, newCategory.getName());

        assertFalse(
                oldCategory.findProductCmptProperties(productCmptType, true, ipsProject).contains(superPolicyProperty));
        assertTrue(
                newCategory.findProductCmptProperties(productCmptType, true, ipsProject).contains(superPolicyProperty));
    }

    @Test
    public void testFindProductCmptPropertiesForCategory_SortPropertiesAccordingToReferenceList() throws CoreException {
        ProductCmptCategory category = (ProductCmptCategory)superProductCmptType.newCategory("category");

        IProductCmptProperty s1 = createProductAttributeProperty(superProductCmptType, "s1");
        IProductCmptProperty s2 = createProductAttributeProperty(superProductCmptType, "s2");
        IProductCmptProperty p1 = createProductAttributeProperty(productCmptType, "p1");
        IProductCmptProperty p2 = createProductAttributeProperty(productCmptType, "p2");

        s1.setCategory(category.getName());
        s2.setCategory(category.getName());
        p1.setCategory(category.getName());
        p2.setCategory(category.getName());

        superProductCmptType.movePropertyReferences(new int[] { 1 }, Arrays.asList(s1, s2), true);
        productCmptType.movePropertyReferences(new int[] { 1 }, Arrays.asList(p1, p2), true);

        List<IProductCmptProperty> allProperties = category.findProductCmptProperties(productCmptType, true,
                ipsProject);
        assertEquals(s2, allProperties.get(0));
        assertEquals(s1, allProperties.get(1));
        assertEquals(p2, allProperties.get(2));
        assertEquals(p1, allProperties.get(3));

        List<IProductCmptProperty> properties = category.findProductCmptProperties(productCmptType, false, ipsProject);
        assertEquals(p2, properties.get(0));
        assertEquals(p1, properties.get(1));
        assertEquals(2, properties.size());
    }

    /**
     * <strong>Scenario:</strong><br>
     * An {@link IProductCmptCategory} has some properties assigned. Then, a new
     * {@link IProductCmptProperty} is created which is not yet in the
     * {@link IProductCmptPropertyReference} list of the {@link IProductCmptType}.
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * The new {@link IProductCmptProperty} should be at the very end of the category's properties.
     */
    @Test
    public void testFindProductCmptPropertiesForCategory_SortPropertiesThatAreNotYetInReferenceListTowardsTheEnd()
            throws CoreException {

        ProductCmptCategory category = (ProductCmptCategory)productCmptType.newCategory("myCategory");

        IProductCmptProperty p1 = createProductAttributeProperty(productCmptType, "p1");
        IProductCmptProperty p2 = createProductAttributeProperty(productCmptType, "p2");
        IProductCmptProperty p3 = createPolicyAttributeProperty(policyCmptType, "p3");

        p1.setCategory(category.getName());
        p2.setCategory(category.getName());
        p3.setCategory(category.getName());

        // Create reference objects by moving
        productCmptType.movePropertyReferences(new int[] { 0 }, Arrays.asList(p1, p2, p3), false);

        // Create a new property
        IProductCmptProperty newProperty = productCmptType.newProductCmptTypeAttribute("newProperty");
        newProperty.setCategory(category.getName());

        List<IProductCmptProperty> properties = category.findProductCmptProperties(productCmptType, false, ipsProject);
        assertEquals(p2, properties.get(0));
        assertEquals(p1, properties.get(1));
        assertEquals(p3, properties.get(2));
        assertEquals(newProperty, properties.get(3));
    }

    @Test
    public void testFindProductCmptPropertiesInOrder() throws CoreException {
        IProductCmptProperty a1 = productCmptType.newProductCmptTypeAttribute("a1");
        IProductCmptProperty a2 = productCmptType.newProductCmptTypeAttribute("a2");

        productCmptType.movePropertyReferences(new int[] { 1 }, Arrays.asList(a1, a2), true);

        List<IProductCmptProperty> properties = findProductCmptPropertiesInOrder(productCmptType, true, ipsProject);
        assertEquals(a2, properties.get(0));
        assertEquals(a1, properties.get(1));
    }

    @Test
    public void testFindProductCmptPropertiesInOrder_SuperTypeNotFound() throws CoreException {
        superProductCmptType.newProductCmptTypeAttribute("s");
        IProductCmptProperty a = productCmptType.newProductCmptTypeAttribute("a");

        productCmptType.setSupertype("foo");

        List<IProductCmptProperty> properties = findProductCmptPropertiesInOrder(productCmptType, true, ipsProject);
        assertEquals(a, properties.get(0));
        assertEquals(1, properties.size());
    }

    @Test
    public void testFindProductCmptPropertiesInOrder_ProductCmptTypeNotFoundFromPolicyCmptType() throws CoreException {
        IPolicyCmptTypeAttribute a1 = policyCmptType.newPolicyCmptTypeAttribute("a1");
        a1.setProductRelevant(true);
        IPolicyCmptTypeAttribute a2 = policyCmptType.newPolicyCmptTypeAttribute("a2");
        a2.setProductRelevant(true);

        policyCmptType.setProductCmptType("foo");

        List<IProductCmptProperty> properties = findProductCmptPropertiesInOrder(productCmptType, true, ipsProject);
        assertEquals(a1, properties.get(0));
        assertEquals(a2, properties.get(1));
        assertEquals(2, properties.size());
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

        IIpsProject referencingProject = newIpsProject();
        IIpsObjectPath path = referencingProject.getIpsObjectPath();
        path.newIpsProjectRefEntry(ipsProject);
        referencingProject.setIpsObjectPath(path);

        IIpsProject independentProject = newIpsProject();

        /*
         * leaveProject1 and leaveProject2 are not directly integrated in any test. But the tested
         * instance search methods have to search in all project that holds a reference to the
         * project of the object. So the search for a Object in e.g. ipsProject have to search for
         * instances in leaveProject1 and leaveProject2. The tests implicit that no duplicates are
         * found.
         */

        IIpsProject leaveProject1 = newIpsProject();
        path = leaveProject1.getIpsObjectPath();
        path.newIpsProjectRefEntry(referencingProject);
        leaveProject1.setIpsObjectPath(path);

        IIpsProject leaveProject2 = newIpsProject();
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
    public void testGetCategory() {
        IProductCmptCategory category1 = productCmptType.newCategory("foo");
        IProductCmptCategory category2 = productCmptType.newCategory("bar");

        assertEquals(category1, productCmptType.getCategory("foo"));
        assertEquals(category2, productCmptType.getCategory("bar"));
    }

    @Test
    public void testGetCategories() {
        deleteAllCategories(productCmptType);

        IProductCmptCategory category1 = productCmptType.newCategory("foo");
        IProductCmptCategory category2 = productCmptType.newCategory("bar");

        assertEquals(category1, productCmptType.getCategories().get(0));
        assertEquals(category2, productCmptType.getCategories().get(1));
        assertEquals(2, productCmptType.getCategories().size());
    }

    @Test
    public void testGetCategories_ByPosition() {
        deleteAllCategories(productCmptType);

        IProductCmptCategory leftCategory = productCmptType.newCategory("left", Position.LEFT);
        IProductCmptCategory rightCategory = productCmptType.newCategory("right", Position.RIGHT);

        List<IProductCmptCategory> categoriesLeft = productCmptType.getCategories(Position.LEFT);
        List<IProductCmptCategory> categoriesRight = productCmptType.getCategories(Position.RIGHT);
        assertEquals(leftCategory, categoriesLeft.get(0));
        assertEquals(1, categoriesLeft.size());
        assertEquals(rightCategory, categoriesRight.get(0));
        assertEquals(1, categoriesRight.size());
    }

    @Test
    public void testFindCategories() throws CoreException {
        deleteAllCategories(superSuperProductCmptType);
        deleteAllCategories(superProductCmptType);
        deleteAllCategories(productCmptType);

        IProductCmptCategory superSuperCategory = superSuperProductCmptType.newCategory("superSuperCategory");
        IProductCmptCategory superCategory = superProductCmptType.newCategory("superCategory");
        IProductCmptCategory category = productCmptType.newCategory("category");

        List<IProductCmptCategory> categories = productCmptType.findCategories(ipsProject);
        assertEquals(superSuperCategory, categories.get(0));
        assertEquals(superCategory, categories.get(1));
        assertEquals(category, categories.get(2));
        assertEquals(3, categories.size());
    }

    @Test
    public void testHasCategory() {
        productCmptType.newCategory("foo");
        assertTrue(productCmptType.hasCategory("foo"));
        assertFalse(productCmptType.hasCategory("bar"));
    }

    @Test
    public void testFindHasCategory() throws CoreException {
        superProductCmptType.newCategory("foo");
        assertTrue(productCmptType.findHasCategory("foo", ipsProject));
        assertFalse(productCmptType.findHasCategory("bar", ipsProject));
    }

    @Test
    public void testMoveCategories() {
        deleteAllCategories(productCmptType);

        IProductCmptCategory category1 = productCmptType.newCategory();
        IProductCmptCategory category2 = productCmptType.newCategory();
        IProductCmptCategory category3 = productCmptType.newCategory();

        assertTrue(productCmptType.moveCategories(Arrays.asList(category2, category3), true));
        List<IProductCmptCategory> categories = productCmptType.getCategories();
        assertEquals(category2, categories.get(0));
        assertEquals(category3, categories.get(1));
        assertEquals(category1, categories.get(2));

        assertTrue(getLastContentChangeEvent().isAffected(category1));
        assertTrue(getLastContentChangeEvent().isAffected(category2));
        assertTrue(getLastContentChangeEvent().isAffected(category3));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMoveCategories_CategoryFromForeignProductCmptType() {
        deleteAllCategories(productCmptType, superProductCmptType);

        IProductCmptCategory superCategory = superProductCmptType.newCategory();

        productCmptType.moveCategories(Arrays.asList(superCategory), true);
    }

    /**
     * <strong>Scenario:</strong><br>
     * There are many categories with mixed positions. Some of these categories are moved. The moved
     * categories contain at least one {@link IProductCmptCategory} of each {@link Position}.
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * The moved categories should be swapped only with other categories with the same
     * {@link Position}.
     */
    @Test
    public void testMoveCategories_MoveCategoriesWithDifferentPosition() {
        deleteAllCategories(productCmptType);

        IProductCmptCategory categoryLeft1 = productCmptType.newCategory("categoryLeft1", Position.LEFT);
        IProductCmptCategory categoryLeft2 = productCmptType.newCategory("categoryLeft2", Position.LEFT);
        IProductCmptCategory categoryRight1 = productCmptType.newCategory("categoryRight1", Position.RIGHT);
        IProductCmptCategory categoryRight2 = productCmptType.newCategory("categoryRight2", Position.RIGHT);

        assertTrue(productCmptType.moveCategories(Arrays.asList(categoryLeft2, categoryRight2), true));

        List<IProductCmptCategory> categories = productCmptType.getCategories();
        assertEquals(categoryLeft2, categories.get(0));
        assertEquals(categoryLeft1, categories.get(1));
        assertEquals(categoryRight2, categories.get(2));
        assertEquals(categoryRight1, categories.get(3));
        assertEquals(4, categories.size());
    }

    @Test
    public void testFindDefaultCategoryForFormulaSignatureDefinitions() throws CoreException {
        deleteAllCategories(productCmptType);
        deleteAllCategories(superProductCmptType);

        assertEquals(superSuperProductCmptType.getCategory(DEFAULT_CATEGORY_NAME_FORMULA_SIGNATURE_DEFINITIONS),
                productCmptType.findDefaultCategoryForFormulaSignatureDefinitions(ipsProject));
    }

    @Test
    public void testFindDefaultCategoryForPolicyCmptTypeAttributes() throws CoreException {
        deleteAllCategories(productCmptType);
        deleteAllCategories(superProductCmptType);

        assertEquals(superSuperProductCmptType.getCategory(DEFAULT_CATEGORY_NAME_POLICY_CMPT_TYPE_ATTRIBUTES),
                productCmptType.findDefaultCategoryForPolicyCmptTypeAttributes(ipsProject));
    }

    @Test
    public void testFindDefaultCategoryForProductCmptTypeAttributes() throws CoreException {
        deleteAllCategories(productCmptType);
        deleteAllCategories(superProductCmptType);

        assertEquals(superSuperProductCmptType.getCategory(DEFAULT_CATEGORY_NAME_PRODUCT_CMPT_TYPE_ATTRIBUTES),
                productCmptType.findDefaultCategoryForProductCmptTypeAttributes(ipsProject));
    }

    @Test
    public void testFindDefaultCategoryForTableStructureUsages() throws CoreException {
        deleteAllCategories(productCmptType);
        deleteAllCategories(superProductCmptType);

        assertEquals(superSuperProductCmptType.getCategory(DEFAULT_CATEGORY_NAME_TABLE_STRUCTURE_USAGES),
                productCmptType.findDefaultCategoryForTableStructureUsages(ipsProject));
    }

    @Test
    public void testFindDefaultCategoryForValidationRules() throws CoreException {
        deleteAllCategories(productCmptType);
        deleteAllCategories(superProductCmptType);

        assertEquals(superSuperProductCmptType.getCategory(DEFAULT_CATEGORY_NAME_VALIDATION_RULES),
                productCmptType.findDefaultCategoryForValidationRules(ipsProject));
    }

    @Test
    public void testMoveProductCmptPropertyReferences_MoveUp() throws CoreException {
        IProductCmptCategory category = productCmptType.newCategory("category");

        IProductCmptProperty property1 = productCmptType.newProductCmptTypeAttribute("property1");
        property1.setCategory(category.getName());
        IProductCmptProperty property2 = productCmptType.newProductCmptTypeAttribute("property2");
        property2.setCategory(category.getName());
        IProductCmptProperty property3 = productCmptType.newProductCmptTypeAttribute("property3");
        property3.setCategory(category.getName());

        assertArrayEquals(new int[] { 1, 0 }, productCmptType.movePropertyReferences(new int[] { 2, 1 },
                Arrays.asList(property1, property2, property3), true));
        List<IProductCmptProperty> properties = findProductCmptPropertiesInOrder(productCmptType, false, ipsProject);
        assertEquals(property2, properties.get(0));
        assertEquals(property3, properties.get(1));
        assertEquals(property1, properties.get(2));
    }

    @Test
    public void testMoveProductCmptPropertyReferences_MoveDown() throws CoreException {
        IProductCmptCategory category = productCmptType.newCategory("category");

        IProductCmptProperty property1 = productCmptType.newProductCmptTypeAttribute("property1");
        property1.setCategory(category.getName());
        IProductCmptProperty property2 = productCmptType.newProductCmptTypeAttribute("property2");
        property2.setCategory(category.getName());
        IProductCmptProperty property3 = productCmptType.newProductCmptTypeAttribute("property3");
        property3.setCategory(category.getName());

        assertArrayEquals(new int[] { 1, 2 }, productCmptType.movePropertyReferences(new int[] { 0, 1 },
                Arrays.asList(property1, property2, property3), false));
        List<IProductCmptProperty> properties = findProductCmptPropertiesInOrder(productCmptType, false, ipsProject);
        assertEquals(property3, properties.get(0));
        assertEquals(property1, properties.get(1));
        assertEquals(property2, properties.get(2));
    }

    /**
     * <strong>Scenario:</strong><br>
     * An {@link IProductCmptPropertyReference} is moved up, but in-between the logically affected
     * properties, an {@link IProductCmptProperty} of another {@link IProductCmptCategory} is
     * located.
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * The move operation should skip the in-between {@link IProductCmptPropertyReference}, so that
     * only properties belonging to an {@link IProductCmptCategory} are swapped with each other, and
     * return true.
     */
    @Test
    public void testMoveProductCmptPropertyReferences_PropertyOfOtherCategoryInBetweenOnMoveUp() throws CoreException {
        IProductCmptCategory category1 = productCmptType.newCategory("category1");
        IProductCmptCategory category2 = productCmptType.newCategory("category2");

        IProductCmptProperty property1 = productCmptType.newProductCmptTypeAttribute("property1");
        property1.setCategory(category1.getName());
        IProductCmptProperty inBetweenProperty = productCmptType.newProductCmptTypeAttribute("inBetweenProperty");
        inBetweenProperty.setCategory(category2.getName());
        IProductCmptProperty property2 = productCmptType.newProductCmptTypeAttribute("property2");
        property2.setCategory(category1.getName());

        assertArrayEquals(new int[] { 0 },
                productCmptType.movePropertyReferences(new int[] { 1 }, Arrays.asList(property1, property2), true));
        List<IProductCmptProperty> properties = findProductCmptPropertiesInOrder(productCmptType, false, ipsProject);
        assertEquals(property2, properties.get(0));
        assertEquals(inBetweenProperty, properties.get(1));
        assertEquals(property1, properties.get(2));
    }

    /**
     * <strong>Scenario:</strong><br>
     * A {@link IProductCmptPropertyReference} is moved down, but in-between the logically affected
     * properties, an {@link IProductCmptProperty} assigned to another {@link IProductCmptCategory}
     * is located.
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * The move operation should skip the in-between {@link IProductCmptPropertyReference}, so that
     * only properties belonging to an {@link IProductCmptCategory} are swapped with each other, and
     * return true.
     */
    @Test
    public void testMoveProductCmptPropertyReferences_PropertyOfOtherCategoryInBetweenOnMoveDown()
            throws CoreException {
        IProductCmptCategory category1 = productCmptType.newCategory("category1");
        IProductCmptCategory category2 = productCmptType.newCategory("category2");

        IProductCmptProperty property1 = productCmptType.newProductCmptTypeAttribute("property1");
        property1.setCategory(category1.getName());
        IProductCmptProperty inBetweenProperty = productCmptType.newProductCmptTypeAttribute("inBetweenProperty");
        inBetweenProperty.setCategory(category2.getName());
        IProductCmptProperty property2 = productCmptType.newProductCmptTypeAttribute("property2");
        property2.setCategory(category1.getName());

        assertArrayEquals(new int[] { 1 },
                productCmptType.movePropertyReferences(new int[] { 0 }, Arrays.asList(property1, property2), false));
        List<IProductCmptProperty> properties = findProductCmptPropertiesInOrder(productCmptType, false, ipsProject);
        assertEquals(property2, properties.get(0));
        assertEquals(inBetweenProperty, properties.get(1));
        assertEquals(property1, properties.get(2));
    }

    /**
     * <strong>Scenario:</strong><br>
     * There exist multiple property references in an {@link IProductCmptType}. One reference is
     * moved.
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * Only a single change event of type {@link ContentChangeEvent#TYPE_WHOLE_CONTENT_CHANGED}
     * should be fired.
     */
    @Test
    public void testMoveProductCmptPropertyReferences_FireOnlySingleChangeEvent() throws CoreException {
        IProductCmptProperty p1 = createProductAttributeProperty(productCmptType, "p1");
        IProductCmptProperty p2 = createProductAttributeProperty(productCmptType, "p2");
        IProductCmptProperty p3 = createProductAttributeProperty(productCmptType, "p3");

        resetLastContentChangeEvent();
        resetNumberContentChangeEvents();

        productCmptType.movePropertyReferences(new int[] { 1 }, Arrays.asList(p1, p2, p3), true);

        assertWholeContentChangedEvent(productCmptType.getIpsSrcFile());
        assertSingleContentChangeEvent();
    }

    @Test
    public void testFindIsCategoryNameUsedTwiceInSupertypeHierarchy() throws CoreException {
        superProductCmptType.newCategory("foo");
        assertFalse(productCmptType.findIsCategoryNameUsedTwiceInSupertypeHierarchy("foo", ipsProject));

        superProductCmptType.newCategory("foo");
        assertTrue(productCmptType.findIsCategoryNameUsedTwiceInSupertypeHierarchy("foo", ipsProject));
    }

    @Test
    public void testFindIsCategoryNameUsedTwiceInSupertypeHierarchy_OnceInTypeAndOnceInSupertype()
            throws CoreException {
        superProductCmptType.newCategory("foo");
        productCmptType.newCategory("foo");
        assertTrue(productCmptType.findIsCategoryNameUsedTwiceInSupertypeHierarchy("foo", ipsProject));
    }

    @Test
    public void testFindIsCategoryNameUsedTwiceInSupertypeHierarchy_NoSupertype() throws CoreException {
        productCmptType.setSupertype("");
        productCmptType.newCategory("foo");
        productCmptType.newCategory("foo");

        assertTrue(productCmptType.findIsCategoryNameUsedTwiceInSupertypeHierarchy("foo", ipsProject));
    }

    @Test
    public void testGetFirstCategory() {
        deleteAllCategories(productCmptType);

        IProductCmptCategory firstLeft = productCmptType.newCategory("firstLeft", Position.LEFT);
        productCmptType.newCategory("secondLeft", Position.LEFT);

        IProductCmptCategory firstRight = productCmptType.newCategory("firstRight", Position.RIGHT);
        productCmptType.newCategory("secondRight", Position.RIGHT);

        assertEquals(firstLeft, productCmptType.getFirstCategory(Position.LEFT));
        assertEquals(firstRight, productCmptType.getFirstCategory(Position.RIGHT));
    }

    @Test
    public void testGetFirstCategory_NoCategoriesDefined() {
        deleteAllCategories(productCmptType);
        assertNull(productCmptType.getFirstCategory(Position.LEFT));
        assertNull(productCmptType.getFirstCategory(Position.RIGHT));
    }

    @Test
    public void testGetFirstCategory_NoCategoryWithRequestedPositionDefined() {
        deleteAllCategories(productCmptType);

        IProductCmptCategory rightCategory = productCmptType.newCategory("rightCategory", Position.RIGHT);
        assertNull(productCmptType.getFirstCategory(Position.LEFT));

        productCmptType.newCategory("leftCategory", Position.LEFT);
        rightCategory.delete();
        assertNull(productCmptType.getFirstCategory(Position.RIGHT));
    }

    @Test
    public void testGetLastCategory() {
        deleteAllCategories(productCmptType);

        productCmptType.newCategory("firstLeft", Position.LEFT);
        IProductCmptCategory secondLeft = productCmptType.newCategory("secondLeft", Position.LEFT);

        productCmptType.newCategory("firstRight", Position.RIGHT);
        IProductCmptCategory secondRight = productCmptType.newCategory("secondRight", Position.RIGHT);

        assertEquals(secondLeft, productCmptType.getLastCategory(Position.LEFT));
        assertEquals(secondRight, productCmptType.getLastCategory(Position.RIGHT));
    }

    @Test
    public void testGetLastCategory_NoCategoriesDefined() {
        deleteAllCategories(productCmptType);
        assertNull(productCmptType.getLastCategory(Position.LEFT));
        assertNull(productCmptType.getLastCategory(Position.RIGHT));
    }

    @Test
    public void testGetLastCategory_NoCategoryWithRequestedPositionDefined() {
        deleteAllCategories(productCmptType);

        IProductCmptCategory rightCategory = productCmptType.newCategory("rightCategory", Position.RIGHT);
        assertNull(productCmptType.getLastCategory(Position.LEFT));

        productCmptType.newCategory("leftCategory", Position.LEFT);
        rightCategory.delete();
        assertNull(productCmptType.getLastCategory(Position.RIGHT));
    }

    @Test
    public void testIsFirstCategory() {
        deleteAllCategories(productCmptType);

        IProductCmptCategory category1 = productCmptType.newCategory("category1");
        IProductCmptCategory category2 = productCmptType.newCategory("category2");

        assertTrue(productCmptType.isFirstCategory(category1));
        assertFalse(productCmptType.isFirstCategory(category2));
    }

    @Test
    public void testIsLastCategory() {
        deleteAllCategories(productCmptType);

        IProductCmptCategory category1 = productCmptType.newCategory("category1");
        IProductCmptCategory category2 = productCmptType.newCategory("category2");

        assertTrue(productCmptType.isLastCategory(category2));
        assertFalse(productCmptType.isLastCategory(category1));
    }

    @Test
    public void testIsDefining() {
        IProductCmptCategory superCategory = superProductCmptType.newCategory("superCategory");
        IProductCmptCategory category = productCmptType.newCategory("category");

        assertFalse(productCmptType.isDefining(superCategory));
        assertTrue(productCmptType.isDefining(category));
    }

    /**
     * <strong>Scenario:</strong><br>
     * The super {@link IProductCmptType} features an {@link IProductCmptCategory} that has the same
     * name as the {@link IProductCmptCategory} under test.
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * The {@link IProductCmptType} should not define the {@link IProductCmptCategory} of the super
     * {@link IProductCmptType}.
     */
    @Test
    public void testIsDefining_SameCategoryNameInSupertype() {
        IProductCmptCategory superCategory = superProductCmptType.newCategory("category");
        IProductCmptCategory category = productCmptType.newCategory("category");

        assertFalse(productCmptType.isDefining(superCategory));
        assertTrue(productCmptType.isDefining(category));
    }

    @Test
    public void testSortCategoriesAccordingToPosition() {
        deleteAllCategories(productCmptType);

        IProductCmptCategory left1 = productCmptType.newCategory("left1", Position.LEFT);
        IProductCmptCategory right1 = productCmptType.newCategory("right1", Position.RIGHT);
        IProductCmptCategory left2 = productCmptType.newCategory("left2", Position.LEFT);
        IProductCmptCategory right2 = productCmptType.newCategory("right2", Position.RIGHT);

        productCmptType.sortCategoriesAccordingToPosition();

        List<IProductCmptCategory> categories = productCmptType.getCategories();
        assertEquals(left1, categories.get(0));
        assertEquals(left2, categories.get(1));
        assertEquals(right1, categories.get(2));
        assertEquals(right2, categories.get(3));
        assertEquals(4, categories.size());
    }

    @Test
    public void testChangeCategoryAndDeferPolicyChange_ImmediatelyChangeProductCmptTypeProperty() {
        IProductCmptProperty productProperty = createProductAttributeProperty(productCmptType, "productAttribute");

        productCmptType.changeCategoryAndDeferPolicyChange(productProperty, "otherCategory");

        assertEquals("otherCategory", productProperty.getCategory());
    }

    @Test
    public void testChangeCategoryAndDeferPolicyChange_OnlyChangePolicyTypeUponProductTypeSave() throws CoreException {
        IProductCmptProperty policyProperty = createPolicyAttributeProperty(policyCmptType, "policyAttribute");
        policyProperty.setCategory("beforeCategory");

        productCmptType.changeCategoryAndDeferPolicyChange(policyProperty, "otherCategory");

        assertEquals("beforeCategory", policyProperty.getCategory());

        productCmptType.getIpsSrcFile().save(true, null);
        assertEquals("otherCategory", policyProperty.getCategory());
    }

    @Test
    public void testChangeCategoryAndDeferPolicyChange_DoNotChangePolicyTypeUponProductTypeSaveIfThePolicySourceFileIsImmutable()
            throws CoreException {

        IProductCmptProperty policyProperty = createPolicyAttributeProperty(policyCmptType, "policyAttribute");
        policyProperty.setCategory("beforeCategory");
        policyCmptType.getIpsSrcFile().delete();

        productCmptType.changeCategoryAndDeferPolicyChange(policyProperty, "otherCategory");
        productCmptType.getIpsSrcFile().save(true, null);

        assertEquals("beforeCategory", policyProperty.getCategory());
    }

    /**
     * <strong>Scenario:</strong><br>
     * If an {@link IProductCmptProperty} originating from an {@link IPolicyCmptType} is moved to
     * another {@link IProductCmptCategory}. It happens that the {@link IIpsSrcFile} of the
     * {@link IPolicyCmptType} is not mutable at the time the change is made.
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * The {@link IProductCmptProperty} should not be moved to the other
     * {@link IProductCmptCategory}.
     */
    @Test
    public void testChangeCategoryAndDeferPolicyChange_DoNotAddPolicyChangeToPendingChangesIfPolicySrcFileImmutable()
            throws CoreException {

        ProductCmptCategory beforeCategory = (ProductCmptCategory)productCmptType.newCategory("beforeCategory");
        ProductCmptCategory otherCategory = (ProductCmptCategory)productCmptType.newCategory("otherCategory");

        IProductCmptProperty policyProperty = createPolicyAttributeProperty(policyCmptType, "policyAttribute");
        policyProperty.setCategory(beforeCategory.getName());

        /*
         * Delete the policy component type so that the source file becomes immutable, then change
         * the category of the policy property, and then undo the delete.
         */
        policyCmptType.getIpsSrcFile().save(true, null);
        Change undoDeleteResourceChange = performDeleteResourceChange(policyCmptType.getIpsSrcFile(), null);
        productCmptType.changeCategoryAndDeferPolicyChange(policyProperty, otherCategory.getName());
        undoDeleteResourceChange.perform(new NullProgressMonitor());

        List<IProductCmptProperty> beforeCategoryProperties = beforeCategory.findProductCmptProperties(productCmptType,
                false, ipsProject);
        assertEquals(policyProperty.getId(), beforeCategoryProperties.get(0).getId());
        assertEquals(1, beforeCategoryProperties.size());

        List<IProductCmptProperty> otherCategoryProperties = otherCategory.findProductCmptProperties(productCmptType,
                false, ipsProject);
        assertTrue(otherCategoryProperties.isEmpty());
    }

    /**
     * <strong>Scenario:</strong><br>
     * The {@link IProductCmptCategory} of an {@link IProductCmptProperty} belonging to an
     * {@link IPolicyCmptType} is changed. Then, the {@link IProductCmptType} is reloaded without
     * first saving it.
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * The {@link IProductCmptProperty} should again belong to the property's initial
     * {@link IProductCmptCategory}.
     */
    @Test
    public void testChangeCategoryAndDeferPolicyChange_ClearPendingPolicyChangesUponXmlInitialization()
            throws CoreException, ParserConfigurationException {

        IProductCmptCategory initialCategory = productCmptType.newCategory("initialCategory");
        IProductCmptProperty policyProperty = createPolicyAttributeProperty(policyCmptType, "policyAttribute");

        IProductCmptCategory otherCategory = productCmptType.newCategory("otherCategory");
        productCmptType.changeCategoryAndDeferPolicyChange(policyProperty, otherCategory.getName());

        Element xmlElement = productCmptType.toXml(createXmlDocument("SomeTag"));
        productCmptType.initFromXml(xmlElement);
        /*
         * As the pending change is made persistent in toXml, we have to test that the pending
         * changes are cleared on re-initialization by manually setting the property's category back
         * to the initial category.
         */
        policyProperty.setCategory(initialCategory.getName());

        /*
         * If the pending changes are cleared upon XML initialization, the property now again
         * belongs to the initial category.
         */
        assertTrue(
                initialCategory.findProductCmptProperties(productCmptType, false, ipsProject).contains(policyProperty));
        assertFalse(
                otherCategory.findProductCmptProperties(productCmptType, false, ipsProject).contains(policyProperty));
    }

    /**
     * <strong>Scenario:</strong><br>
     * The {@link IProductCmptCategory} of an {@link IProductCmptProperty} belonging to an
     * {@link IPolicyCmptType} is changed. Then, the {@link IProductCmptType} is saved.
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * The {@link IProductCmptProperty} should belong to the new {@link IProductCmptCategory}.
     * Afterwards, directly setting the property's {@link IProductCmptCategory} should take effect
     * once again as there no longer are any pending policy changes.
     */
    @Test
    public void testChangeCategoryAndDeferPolicyChange_ClearPendingPolicyChangesUponSave()
            throws ParserConfigurationException, CoreException {

        IProductCmptCategory initialCategory = productCmptType.newCategory("initialCategory");
        IProductCmptProperty policyProperty = createPolicyAttributeProperty(policyCmptType, "policyAttribute");

        IProductCmptCategory otherCategory = productCmptType.newCategory("otherCategory");
        productCmptType.changeCategoryAndDeferPolicyChange(policyProperty, otherCategory.getName());

        productCmptType.toXml(createXmlDocument("SomeTag"));

        /*
         * If the pending changes are cleared upon save, setting the category now directly should
         * take immediate effect.
         */
        policyProperty.setCategory(initialCategory.getName());
        assertTrue(
                initialCategory.findProductCmptProperties(productCmptType, false, ipsProject).contains(policyProperty));
        assertFalse(
                otherCategory.findProductCmptProperties(productCmptType, false, ipsProject).contains(policyProperty));
    }

    /**
     * <strong>Scenario:</strong><br>
     * When saving the {@link IProductCmptType}, category changes of {@link IProductCmptProperty
     * product component properties} originating from the {@link IPolicyCmptType} are saved.
     * However, now it happens that the {@link IIpsSrcFile} of the {@link IPolicyCmptType} is
     * immutable.
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * All category changes for the {@link IPolicyCmptType} should be reverted.
     */
    @Test
    public void testChangeCategoryAndDeferPolicyChange_ClearPendingPolicyChangesUponSaveEvenIfPolicySrcFileIsImmutable()
            throws CoreException {

        ProductCmptCategory beforeCategory = (ProductCmptCategory)productCmptType.newCategory("beforeCategory");
        ProductCmptCategory afterCategory = (ProductCmptCategory)productCmptType.newCategory("afterCategory");
        IProductCmptProperty policyProperty = createPolicyAttributeProperty(policyCmptType, "policyAttribute");
        policyProperty.setCategory(beforeCategory.getName());

        productCmptType.changeCategoryAndDeferPolicyChange(policyProperty, afterCategory.getName());

        /*
         * Delete the policy component type so that the source file becomes immutable, then save the
         * product component type, and then undo the delete.
         */
        policyCmptType.getIpsSrcFile().save(true, null);
        Change undoDeleteResourceChange = performDeleteResourceChange(policyCmptType.getIpsSrcFile(), null);
        productCmptType.getIpsSrcFile().save(true, null);
        undoDeleteResourceChange.perform(new NullProgressMonitor());

        List<IProductCmptProperty> beforeCategoryProperties = beforeCategory.findProductCmptProperties(productCmptType,
                false, ipsProject);
        assertEquals(policyProperty.getId(), beforeCategoryProperties.get(0).getId());
        assertEquals(1, beforeCategoryProperties.size());

        List<IProductCmptProperty> afterCategoryProperties = afterCategory.findProductCmptProperties(productCmptType,
                false, ipsProject);
        assertTrue(afterCategoryProperties.isEmpty());
    }

    /**
     * <strong>Scenario:</strong><br>
     * Normally, if {@link IProductCmptProperty product component properties} originating from the
     * {@link IPolicyCmptType} are moved to another {@link IProductCmptCategory}, this change is
     * saved in the {@link IPolicyCmptType} as soon as the {@link IProductCmptType} is saved. Now it
     * happens that the {@link IPolicyCmptType} already is <em>dirty</em> at the moment the
     * {@link IProductCmptType} is saved.
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * The {@link IPolicyCmptType} should not be saved but the pending policy changes should be
     * transferred to the policy properties.
     */
    @Test
    public void testChangeCategoryAndDeferPolicyChange_OnlySavePolicyUponProductSaveIfPolicyIsNotDirty()
            throws CoreException {

        IProductCmptCategory beforeCategory = productCmptType.newCategory("beforeCategory");
        IProductCmptCategory afterCategory = productCmptType.newCategory("afterCategory");
        IProductCmptProperty policyProperty = createPolicyAttributeProperty(policyCmptType, "policyAttribute");
        policyProperty.setCategory(beforeCategory.getName());

        productCmptType.changeCategoryAndDeferPolicyChange(policyProperty, afterCategory.getName());
        policyCmptType.getIpsSrcFile().markAsDirty();
        productCmptType.getIpsSrcFile().save(true, null);

        // 1) the policy component type's source file must not have been saved
        assertTrue(policyCmptType.getIpsSrcFile().isDirty());

        // 2) the pending policy changes must still be pending
        assertEquals(afterCategory.getName(), policyProperty.getCategory());
    }

    @Test
    public void testChangeCategoryAndDeferPolicyChange_FireChangeEvent() {
        IProductCmptProperty policyProperty = createPolicyAttributeProperty(policyCmptType, "policyAttribute");

        productCmptType.changeCategoryAndDeferPolicyChange(policyProperty, "otherCategory");

        assertWholeContentChangedEvent(productCmptType.getIpsSrcFile());
    }

    @Test
    public void testOverrideAttributes() throws CoreException {
        IProductCmptTypeAttribute attribute = productCmptType.newProductCmptTypeAttribute();
        attribute.setName("override");
        attribute.setDatatype(Datatype.STRING.getQualifiedName());
        attribute.setDefaultValue("defaultValue");
        attribute.setValueSetType(ValueSetType.ENUM);
        for (IDescription description : attribute.getDescriptions()) {
            description.setText("Overridden Description");
        }

        IProductCmptType overridingType = newProductCmptType(ipsProject, "OverridingType");
        overridingType.overrideAttributes(Arrays.asList(attribute));

        IProductCmptTypeAttribute overriddenAttribute = overridingType.getProductCmptTypeAttribute(attribute.getName());
        assertEquals(attribute.getDatatype(), overriddenAttribute.getDatatype());
        assertEquals(attribute.getDefaultValue(), overriddenAttribute.getDefaultValue());
        assertEquals(attribute.getValueSet().getValueSetType(), overriddenAttribute.getValueSet().getValueSetType());
        for (int i = 0; i < overriddenAttribute.getDescriptions().size(); i++) {
            assertEquals(attribute.getDescriptions().get(i).getText(),
                    overriddenAttribute.getDescriptions().get(i).getText());
        }
        assertTrue(overriddenAttribute.isOverwrite());
    }

    @Test
    public void testFindOverrideAttributeCandidates() throws CoreException {
        IProductCmptType superPcType = newProductCmptType(ipsProject, "Super");

        // 1. Attribute
        IProductCmptTypeAttribute attribute = superPcType.newProductCmptTypeAttribute();
        attribute.setName("ToOverride");
        attribute.setDatatype(Datatype.STRING.getQualifiedName());
        attribute.setDefaultValue("defaultValue");
        attribute.setValueSetType(ValueSetType.ENUM);
        for (IDescription description : attribute.getDescriptions()) {
            description.setText("Overridden Description");
        }

        // 2. Attribute
        IProductCmptTypeAttribute attribute2 = superPcType.newProductCmptTypeAttribute();
        attribute2.setName("NotOverride");
        attribute2.setDatatype(Datatype.STRING.getQualifiedName());
        attribute2.setDefaultValue("defaultValue");
        attribute2.setValueSetType(ValueSetType.ENUM);

        IProductCmptType overridingType = newProductCmptType(ipsProject, "OverridingType");
        overridingType.setSupertype(superPcType.getQualifiedName());

        List<IAttribute> findOverrideAttributeCandidates = overridingType.findOverrideAttributeCandidates(ipsProject);
        // 2 to Override
        assertEquals(2, findOverrideAttributeCandidates.size());

        // now override the first
        overridingType.overrideAttributes(Arrays.asList(attribute));

        findOverrideAttributeCandidates = overridingType.findOverrideAttributeCandidates(ipsProject);
        // only the second to find
        assertEquals(1, findOverrideAttributeCandidates.size());
        assertEquals("NotOverride", findOverrideAttributeCandidates.get(0).getName());
    }

    @Test
    public void testValidateSuperProductCmptTypeHasSameChangingOverTimeSetting_returnEmptyListIfChangingOverTimeSettingsAreBothTrue()
            throws CoreException {
        productCmptType.setChangingOverTime(true);
        superProductCmptType.setChangingOverTime(true);

        MessageList result = productCmptType.validate(ipsProject);

        assertNull(result.getMessageByCode(IProductCmptType.MSGCODE_SETTING_CHANGING_OVER_TIME_DIFFERS_FROM_SUPERTYPE));
    }

    @Test
    public void testValidateSuperProductCmptTypeHasSameChangingOverTimeSetting_returnMessageListIfChangingOverTimeSettingsAreDifferent()
            throws CoreException {
        productCmptType.setChangingOverTime(true);
        superProductCmptType.setChangingOverTime(false);

        MessageList result = productCmptType.validate(ipsProject);

        Message message = result
                .getMessageByCode(IProductCmptType.MSGCODE_SETTING_CHANGING_OVER_TIME_DIFFERS_FROM_SUPERTYPE);
        assertEquals(productCmptType, message.getInvalidObjectProperties()[0].getObject());
        assertEquals(IProductCmptType.PROPERTY_CHANGING_OVER_TIME,
                message.getInvalidObjectProperties()[0].getProperty());
        assertEquals(Message.ERROR, message.getSeverity());
    }

    private Change performDeleteResourceChange(IIpsSrcFile ipsSrcFile, IProgressMonitor pm) throws CoreException {
        IPath resourcePath = ipsSrcFile.getCorrespondingResource().getFullPath();
        DeleteResourceChange deleteResourceChange = new DeleteResourceChange(resourcePath, true);
        return deleteResourceChange.perform(pm);
    }

    /**
     * Deletes all {@link IProductCmptCategory}s of the provided {@link IProductCmptType}s.
     * <p>
     * This is often necessary to create a clean test environment as default
     * {@link IProductCmptCategory}s are created by the {@code newProductCmptType} methods.
     */
    private void deleteAllCategories(IProductCmptType... productCmptTypes) {
        for (IProductCmptType productCmptType : productCmptTypes) {
            for (IProductCmptCategory category : productCmptType.getCategories()) {
                category.delete();
            }
        }
    }

    /**
     * Returns a list containing the product component properties belonging to this
     * {@link IProductCmptType} or the configured {@link IPolicyCmptType} in the order they are
     * referenced by the categories of this {@link IProductCmptType}.
     * 
     * @param searchSupertypeHierarchy flag indicating whether to include product component
     *            properties defined in the supertype hierarchy
     * 
     * @throws CoreException if an error occurs during the search
     */
    private List<IProductCmptProperty> findProductCmptPropertiesInOrder(final ProductCmptType contextType,
            boolean searchSupertypeHierarchy,
            IIpsProject ipsProject) throws CoreException {

        List<IProductCmptProperty> properties = contextType.findProductCmptProperties(searchSupertypeHierarchy,
                ipsProject);
        Collections.sort(properties, new ProductCmptPropertyComparator(contextType));
        return properties;
    }

    @Test
    public void testValidateAbstractAttributes_abstractType() throws Exception {
        productCmptType.setAbstract(true);
        IAttribute superAttr1 = superProductCmptType.newAttribute();
        superAttr1.setName(ATTR1);
        superAttr1.setDatatype(SUPER_ENUM_TYPE);

        MessageList list = new MessageList();
        productCmptType.validateAbstractAttributes(list, ipsProject);

        assertTrue(list.isEmpty());
    }

    @Test
    public void testValidateAbstractAttributes_abstractSubtype() throws Exception {
        IAttribute superAttr1 = superProductCmptType.newAttribute();
        superAttr1.setName(ATTR1);
        superAttr1.setDatatype(SUPER_ENUM_TYPE);
        productCmptType.setAbstract(true);

        MessageList list = new MessageList();
        productCmptType.validateAbstractAttributes(list, ipsProject);

        assertTrue(list.isEmpty());
    }

    @Test
    public void testValidateAbstractAttributes_correct() throws Exception {
        IAttribute superAttr1 = superProductCmptType.newAttribute();
        superAttr1.setName(ATTR1);
        superAttr1.setDatatype(SUPER_ENUM_TYPE);
        IAttribute attr1 = productCmptType.newAttribute();
        attr1.setName(ATTR1);
        attr1.setOverwrite(true);
        attr1.setDatatype(ENUM_TYPE);

        MessageList list = new MessageList();
        productCmptType.validateAbstractAttributes(list, ipsProject);

        assertTrue(list.isEmpty());
    }

    @Test
    public void testValidateAbstractAttributes_correct_MultiSubclass() throws Exception {
        superSuperProductCmptType.newAttribute();
        IAttribute superAttr1 = superSuperProductCmptType.newAttribute();
        superAttr1.setName(ATTR1);
        superAttr1.setDatatype(SUPER_ENUM_TYPE);
        IAttribute attr1 = superProductCmptType.newAttribute();
        attr1.setName(ATTR1);
        attr1.setOverwrite(true);
        attr1.setDatatype(ENUM_TYPE);

        MessageList list = new MessageList();
        productCmptType.validateAbstractAttributes(list, ipsProject);

        assertTrue(list.isEmpty());
    }

    @Test
    public void testValidateAbstractAttributes_notOverwritten() throws Exception {
        IAttribute superAttr1 = superProductCmptType.newAttribute();
        superAttr1.setName(ATTR1);
        superAttr1.setDatatype(SUPER_ENUM_TYPE);

        MessageList list = new MessageList();
        productCmptType.validateAbstractAttributes(list, ipsProject);

        Message message = list.getMessageByCode(IType.MSGCODE_ABSTRACT_MISSING);
        assertNotNull(message);
        assertEquals(new ObjectProperty(productCmptType, IType.PROPERTY_ABSTRACT),
                message.getInvalidObjectProperties()[0]);
    }

    @Test
    public void testValidateAbstractAttributes_overwrittenAbstractType() throws Exception {
        IAttribute superAttr1 = superProductCmptType.newAttribute();
        superAttr1.setName(ATTR1);
        superAttr1.setDatatype(SUPER_ENUM_TYPE);
        IAttribute attr1 = productCmptType.newAttribute();
        attr1.setName(ATTR1);
        attr1.setOverwrite(true);
        attr1.setDatatype(SUPER_ENUM_TYPE);

        MessageList list = productCmptType.validate(ipsProject);

        Message message = list.getMessageByCode(IType.MSGCODE_ABSTRACT_MISSING);
        assertNotNull(message);
        assertEquals(new ObjectProperty(attr1, IAttribute.PROPERTY_DATATYPE), message.getInvalidObjectProperties()[0]);
        assertEquals(new ObjectProperty(productCmptType, IType.PROPERTY_ABSTRACT),
                message.getInvalidObjectProperties()[1]);
    }

}
