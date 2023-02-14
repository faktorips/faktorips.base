/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.pctype;

import static org.faktorips.testsupport.IpsMatchers.hasInvalidObject;
import static org.faktorips.testsupport.IpsMatchers.hasMessageCode;
import static org.faktorips.testsupport.IpsMatchers.hasSeverity;
import static org.faktorips.testsupport.IpsMatchers.isEmpty;
import static org.faktorips.testsupport.IpsMatchers.lacksMessageCode;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.faktorips.abstracttest.AbstractDependencyTest;
import org.faktorips.abstracttest.builder.TestArtefactBuilderSetInfo;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.model.ContentChangeEvent;
import org.faktorips.devtools.model.DependencyType;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.dependency.IDependency;
import org.faktorips.devtools.model.enums.IEnumType;
import org.faktorips.devtools.model.enums.IEnumValue;
import org.faktorips.devtools.model.internal.IpsModel;
import org.faktorips.devtools.model.internal.builder.EmptyBuilderSet;
import org.faktorips.devtools.model.internal.dependency.DatatypeDependency;
import org.faktorips.devtools.model.internal.dependency.IpsObjectDependency;
import org.faktorips.devtools.model.internal.enums.EnumType;
import org.faktorips.devtools.model.internal.ipsobject.IpsSrcFile;
import org.faktorips.devtools.model.internal.ipsproject.IpsObjectPath;
import org.faktorips.devtools.model.internal.ipsproject.IpsProjectRefEntry;
import org.faktorips.devtools.model.ipsobject.IDescription;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsobject.Modifier;
import org.faktorips.devtools.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilderSetInfo;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPathEntry;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.model.method.IParameter;
import org.faktorips.devtools.model.pctype.AttributeType;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.model.pctype.IValidationRule;
import org.faktorips.devtools.model.pctype.persistence.IPersistentTypeInfo.PersistentType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.type.AssociationType;
import org.faktorips.devtools.model.type.IAssociation;
import org.faktorips.devtools.model.type.IAttribute;
import org.faktorips.devtools.model.type.IMethod;
import org.faktorips.devtools.model.type.IProductCmptProperty;
import org.faktorips.devtools.model.type.IType;
import org.faktorips.devtools.model.type.ITypeHierarchy;
import org.faktorips.devtools.model.type.ProductCmptPropertyType;
import org.faktorips.devtools.model.value.ValueFactory;
import org.faktorips.devtools.model.valueset.ValueSetType;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.Severity;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;

public class PolicyCmptTypeTest extends AbstractDependencyTest {

    private static final String ABSTRACT_PARENT_ENUM = "SuperEnumType";
    private static final String CONCRETE_CHILD_ENUM = "EnumType";
    private static final String ATTR1 = "attr1";

    private IIpsPackageFragment pack;
    private IIpsSrcFile sourceFile;
    private IIpsProject ipsProject;

    private PolicyCmptType policyCmptType;
    private PolicyCmptType superPolicyCmptType;
    private PolicyCmptType superSuperPolicyCmptType;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = this.newIpsProject();

        policyCmptType = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "TestPolicy");
        policyCmptType.setConfigurableByProductCmptType(false);
        superPolicyCmptType = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "SuperTestPolicy");
        superPolicyCmptType.setConfigurableByProductCmptType(false);
        policyCmptType.setSupertype(superPolicyCmptType.getQualifiedName());
        superSuperPolicyCmptType = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "SuperSuperTestPolicy");
        superSuperPolicyCmptType.setConfigurableByProductCmptType(false);
        superPolicyCmptType.setSupertype(superSuperPolicyCmptType.getQualifiedName());

        EnumType superEnum = newEnumType(ipsProject, ABSTRACT_PARENT_ENUM);
        superEnum.setAbstract(true);
        EnumType enumType = newEnumType(ipsProject, CONCRETE_CHILD_ENUM);
        enumType.setSuperEnumType(CONCRETE_CHILD_ENUM);

        sourceFile = policyCmptType.getIpsSrcFile();
        pack = policyCmptType.getIpsPackageFragment();
    }

    @Test
    public void testGetChildren() {
        IPolicyCmptTypeAttribute a1 = policyCmptType.newPolicyCmptTypeAttribute();
        IMethod m1 = policyCmptType.newMethod();
        IPolicyCmptTypeAssociation r1 = policyCmptType.newPolicyCmptTypeAssociation();
        IValidationRule rule1 = policyCmptType.newRule();
        policyCmptType.setConfigurableByProductCmptType(true);

        IIpsElement[] elements = policyCmptType.getChildren();
        List<IIpsElement> childrenList = Arrays.asList(elements);
        assertTrue(childrenList.contains(a1));
        assertTrue(childrenList.contains(r1));
        assertTrue(childrenList.contains(m1));
        assertTrue(childrenList.contains(rule1));
    }

    @Test
    public void testValidateProductCmptTypeDoesNotConfigureThisType() {
        IPolicyCmptType polType = newPolicyAndProductCmptType(ipsProject, "Policy", "Product");
        IProductCmptType productType = polType.findProductCmptType(ipsProject);

        MessageList result = polType.validate(ipsProject);
        assertNull(result.getMessageByCode(IPolicyCmptType.MSGCODE_PRODUCT_CMPT_TYPE_DOES_NOT_CONFIGURE_THIS_TYPE));

        productType.setPolicyCmptType(policyCmptType.getQualifiedName());
        result = polType.validate(ipsProject);
        assertNotNull(result.getMessageByCode(IPolicyCmptType.MSGCODE_PRODUCT_CMPT_TYPE_DOES_NOT_CONFIGURE_THIS_TYPE));

        productType.setConfigurationForPolicyCmptType(false);
        result = polType.validate(ipsProject);
        assertNotNull(result.getMessageByCode(IPolicyCmptType.MSGCODE_PRODUCT_CMPT_TYPE_DOES_NOT_CONFIGURE_THIS_TYPE));
    }

    @Test
    public void testValidateSupertypeConfigurableForcesThisTypeConfigurable() throws Exception {
        IPolicyCmptType superType = newPolicyAndProductCmptType(ipsProject, "SuperPolicy", "SuperProduct");

        IPolicyCmptType type = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "Policy");
        type.setSupertype(superType.getQualifiedName());

        MessageList msgList = type.validate(ipsProject);
        assertNotNull(msgList
                .getMessageByCode(IPolicyCmptType.MSGCODE_SUPERTYPE_CONFIGURABLE_FORCES_THIS_TYPE_IS_CONFIGURABLE));

        IProductCmptType productType = newProductCmptType(ipsProject, "Product");
        type.setConfigurableByProductCmptType(true);
        type.setProductCmptType(productType.getQualifiedName());

        msgList = type.validate(ipsProject);
        assertNull(msgList
                .getMessageByCode(IPolicyCmptType.MSGCODE_SUPERTYPE_CONFIGURABLE_FORCES_THIS_TYPE_IS_CONFIGURABLE));
    }

    @Test
    public void testGetOverrideCandidates() {
        assertEquals(0, policyCmptType.findOverrideMethodCandidates(false, ipsProject).size());

        // create two more types that act as supertype and supertype's supertype
        IIpsSrcFile file1 = pack.createIpsFile(IpsObjectType.POLICY_CMPT_TYPE, "Supertype", true, null);
        IPolicyCmptType supertype = (PolicyCmptType)file1.getIpsObject();
        IIpsSrcFile file2 = pack.createIpsFile(IpsObjectType.POLICY_CMPT_TYPE, "Supersupertype", true, null);
        IPolicyCmptType supersupertype = (PolicyCmptType)file2.getIpsObject();
        policyCmptType.setSupertype(supertype.getQualifiedName());
        supertype.setSupertype(supersupertype.getQualifiedName());

        IMethod m1 = policyCmptType.newMethod();
        m1.setName("calc");

        // supertype methods
        IMethod m2 = supertype.newMethod();
        m2.setName("calc");
        IMethod m3 = supertype.newMethod();
        m3.setName("calc");
        m3.newParameter("Decimal", "p1");

        // supersupertype methods
        IMethod m4 = supersupertype.newMethod();
        m4.setName("calc");
        m4.newParameter("Decimal", "p1");

        IMethod m5 = supersupertype.newMethod();
        m5.setName("calc");
        m5.setAbstract(true);
        m5.newParameter("Money", "p1");

        List<IMethod> candidates = policyCmptType.findOverrideMethodCandidates(false, ipsProject);
        assertEquals(2, candidates.size());
        assertEquals(m3, candidates.get(0));
        assertEquals(m5, candidates.get(1));
        // notes:
        // m2 is not a candidate because it is already overridden by m1
        // m4 is not a candidate because it is overridden by m3 and m3 comes first in the hierarchy

        // only not implemented abstract methods
        candidates = policyCmptType.findOverrideMethodCandidates(true, ipsProject);
        assertEquals(1, candidates.size());
        assertEquals(m5, candidates.get(0));
        // note: now only m5 is a candidate as it's abstract, m3 is not.

        // override the supersupertype method m5 in the supertype
        // => now also m5 is not a candidate any more, if only not implemented abstract methods are
        // requested.
        supertype.overrideMethods(Arrays.asList(m5));
        candidates = policyCmptType.findOverrideMethodCandidates(true, ipsProject);
        assertEquals(0, candidates.size());
    }

    @Test
    public void testFindProductCmptType() {
        policyCmptType.setProductCmptType("");
        assertNull(policyCmptType.findProductCmptType(ipsProject));

        policyCmptType.setProductCmptType("MotorProduct");
        policyCmptType.setConfigurableByProductCmptType(false);
        assertNull(policyCmptType.findProductCmptType(ipsProject));

        policyCmptType.setConfigurableByProductCmptType(true);
        policyCmptType.setProductCmptType("Unkown");
        assertNull(policyCmptType.findProductCmptType(ipsProject));

        IProductCmptType productCmptType = newProductCmptType(ipsProject,
                policyCmptType.getIpsPackageFragment().getName() + ".Product");
        policyCmptType.setProductCmptType(productCmptType.getQualifiedName());
        assertSame(productCmptType, policyCmptType.findProductCmptType(ipsProject));
    }

    @Test
    public void testFindAttributeInSupertypeHierarchy() {
        assertNull(policyCmptType.findPolicyCmptTypeAttribute("unkown", ipsProject));
        IPolicyCmptTypeAttribute a1 = policyCmptType.newPolicyCmptTypeAttribute();
        a1.setName("a1");
        assertNull(policyCmptType.findPolicyCmptTypeAttribute("unkown", ipsProject));
        assertEquals(a1, policyCmptType.findPolicyCmptTypeAttribute("a1", ipsProject));

        IPolicyCmptType supertype = newPolicyCmptType(ipsProject, "Supertype");
        IPolicyCmptTypeAttribute a2 = supertype.newPolicyCmptTypeAttribute();
        a2.setName("a2");
        policyCmptType.setSupertype(supertype.getQualifiedName());

        assertNull(policyCmptType.findPolicyCmptTypeAttribute("unkown", ipsProject));
        assertEquals(a1, policyCmptType.findPolicyCmptTypeAttribute("a1", ipsProject));
        assertEquals(a2, policyCmptType.findPolicyCmptTypeAttribute("a2", ipsProject));
    }

    @Test
    public void testIsExtensionCompilationUnitGenerated() {
        assertFalse(policyCmptType.isExtensionCompilationUnitGenerated());

        // force generation
        policyCmptType.setForceExtensionCompilationUnitGeneration(true);
        assertTrue(policyCmptType.isExtensionCompilationUnitGenerated());

        // validation rule
        policyCmptType.setForceExtensionCompilationUnitGeneration(false);
        assertFalse(policyCmptType.isExtensionCompilationUnitGenerated());
        IValidationRule rule = policyCmptType.newRule();
        assertTrue(policyCmptType.isExtensionCompilationUnitGenerated());

        // method
        rule.delete();
        assertFalse(policyCmptType.isExtensionCompilationUnitGenerated());
        IMethod method = policyCmptType.newMethod();
        assertTrue(policyCmptType.isExtensionCompilationUnitGenerated());
        method.setAbstract(true);
        assertFalse(policyCmptType.isExtensionCompilationUnitGenerated());
        method.delete();

        // attribute
        IPolicyCmptTypeAttribute attribute = policyCmptType.newPolicyCmptTypeAttribute();
        attribute.setAttributeType(AttributeType.DERIVED_BY_EXPLICIT_METHOD_CALL);
        attribute.setValueSetConfiguredByProduct(true);
        assertFalse(policyCmptType.isExtensionCompilationUnitGenerated());
        attribute.setValueSetConfiguredByProduct(false);
        assertTrue(policyCmptType.isExtensionCompilationUnitGenerated());
        attribute.setAttributeType(AttributeType.DERIVED_ON_THE_FLY);
        assertTrue(policyCmptType.isExtensionCompilationUnitGenerated());
        attribute.setAttributeType(AttributeType.CHANGEABLE);
        assertFalse(policyCmptType.isExtensionCompilationUnitGenerated());
        attribute.setAttributeType(AttributeType.CONSTANT);
        assertFalse(policyCmptType.isExtensionCompilationUnitGenerated());

    }

    @Test
    public void testNewAttribute() {
        IPolicyCmptTypeAttribute a = policyCmptType.newPolicyCmptTypeAttribute();
        assertSame(policyCmptType, a.getIpsObject());
        assertEquals(1, policyCmptType.getNumOfAttributes());
        assertTrue(sourceFile.isDirty());
        assertEquals(sourceFile, getLastContentChangeEvent().getIpsSrcFile());
        assertEquals(a, getLastContentChangeEvent().getPart());
        assertEquals(ContentChangeEvent.TYPE_PART_ADDED, getLastContentChangeEvent().getEventType());
        String aId = a.getId();
        assertNotNull(aId);

        IMethod m = policyCmptType.newMethod();
        String mId = m.getId();
        assertNotNull(mId);
        assertFalse(mId.equals(aId));
        IPolicyCmptTypeAttribute a2 = policyCmptType.newPolicyCmptTypeAttribute();
        String a2Id = a2.getId();
        assertNotNull(a2Id);
        assertFalse(a2Id.equals(aId));
        assertFalse(mId.equals(a2Id));
    }

    @Test
    public void testNewPolicyCmptTypeAttributeWithName() {
        IPolicyCmptTypeAttribute attribute = policyCmptType.newPolicyCmptTypeAttribute("foo");
        assertEquals("foo", attribute.getName());
    }

    @Test
    public void testGetAttributes() {
        assertEquals(0, policyCmptType.getPolicyCmptTypeAttributes().size());
        IPolicyCmptTypeAttribute a1 = policyCmptType.newPolicyCmptTypeAttribute();
        IPolicyCmptTypeAttribute a2 = policyCmptType.newPolicyCmptTypeAttribute();
        assertSame(a1, policyCmptType.getPolicyCmptTypeAttributes().get(0));
        assertSame(a2, policyCmptType.getPolicyCmptTypeAttributes().get(1));

        // make sure a defensive copy is returned.
        policyCmptType.getPolicyCmptTypeAttributes().clear();
        assertNotNull(policyCmptType.getPolicyCmptTypeAttributes().get(0));
    }

    @Test
    public void testGetProductCmptProperties() {
        policyCmptType.setConfigurableByProductCmptType(true);

        policyCmptType.newPolicyCmptTypeAttribute("noAttributeProperty");
        IPolicyCmptTypeAttribute attributeProperty = policyCmptType.newPolicyCmptTypeAttribute("attributeProeprty");
        attributeProperty.setValueSetConfiguredByProduct(true);
        attributeProperty.setAttributeType(AttributeType.CHANGEABLE);

        IValidationRule noRuleProperty = policyCmptType.newRule();
        noRuleProperty.setConfigurableByProductComponent(false);
        IValidationRule ruleProperty = policyCmptType.newRule();
        ruleProperty.setConfigurableByProductComponent(true);

        List<IProductCmptProperty> allProperties = policyCmptType.getProductCmptProperties(null);
        assertTrue(allProperties.contains(attributeProperty));
        assertTrue(allProperties.contains(ruleProperty));
        assertEquals(2, allProperties.size());

        List<IProductCmptProperty> attributeProperties = policyCmptType
                .getProductCmptProperties(ProductCmptPropertyType.POLICY_CMPT_TYPE_ATTRIBUTE);
        assertTrue(attributeProperties.contains(attributeProperty));
        assertEquals(1, attributeProperties.size());

        List<IProductCmptProperty> ruleProperties = policyCmptType
                .getProductCmptProperties(ProductCmptPropertyType.VALIDATION_RULE);
        assertTrue(ruleProperties.contains(ruleProperty));
        assertEquals(1, ruleProperties.size());
    }

    @Test
    public void testGetAttribute() {
        IPolicyCmptTypeAttribute a1 = policyCmptType.newPolicyCmptTypeAttribute();
        a1.setName("a1");
        IPolicyCmptTypeAttribute a2 = policyCmptType.newPolicyCmptTypeAttribute();
        a2.setName("a2");
        IPolicyCmptTypeAttribute a3 = policyCmptType.newPolicyCmptTypeAttribute();
        a3.setName("a2"); // same name!

        assertEquals(a1, policyCmptType.getPolicyCmptTypeAttribute("a1"));
        assertEquals(a2, policyCmptType.getPolicyCmptTypeAttribute("a2"));
        assertNull(policyCmptType.getPolicyCmptTypeAttribute("b"));
        assertNull(policyCmptType.getPolicyCmptTypeAttribute(null));
    }

    @Test
    public void testNewMethod() {
        IMethod m = policyCmptType.newMethod();
        String mId = m.getId();
        assertNotNull(mId);
        assertSame(policyCmptType, m.getIpsObject());
        assertEquals(1, policyCmptType.getNumOfMethods());
        assertTrue(sourceFile.isDirty());
        assertEquals(sourceFile, getLastContentChangeEvent().getIpsSrcFile());

        IMethod m2 = policyCmptType.newMethod();
        String m2Id = m2.getId();
        assertNotNull(m2Id);
        assertFalse(m2Id.equals(mId));
    }

    @Test
    public void testGetMethods() {
        assertEquals(0, policyCmptType.getMethods().size());
        IMethod m1 = policyCmptType.newMethod();
        IMethod m2 = policyCmptType.newMethod();
        assertSame(m1, policyCmptType.getMethods().get(0));
        assertSame(m2, policyCmptType.getMethods().get(1));

        // make sure a defensive copy is returned.
        policyCmptType.getMethods().clear();
        assertNotNull(policyCmptType.getMethods().get(0));
    }

    @Test
    public void testNewRule() {
        IValidationRule r = policyCmptType.newRule();
        String rId = r.getId();
        assertNotNull(rId);
        assertSame(policyCmptType, r.getIpsObject());
        assertEquals(1, policyCmptType.getNumOfRules());
        assertTrue(sourceFile.isDirty());
        assertEquals(sourceFile, getLastContentChangeEvent().getIpsSrcFile());

        IValidationRule r2 = policyCmptType.newRule();
        String r2Id = r2.getId();
        assertNotNull(r2Id);
        assertFalse(r2Id.equals(rId));
    }

    @Test
    public void testGetValidationRules() {
        assertEquals(0, policyCmptType.getValidationRules().size());
        IValidationRule r1 = policyCmptType.newRule();
        IValidationRule r2 = policyCmptType.newRule();
        assertSame(r1, policyCmptType.getValidationRules().get(0));
        assertSame(r2, policyCmptType.getValidationRules().get(1));

        // make sure a defensive copy is returned.
        policyCmptType.getValidationRules().clear();
        assertNotNull(policyCmptType.getValidationRules().get(0));
    }

    @Test
    public void testNewRelation() {
        IPolicyCmptTypeAssociation r = policyCmptType.newPolicyCmptTypeAssociation();
        String rId = r.getId();
        assertNotNull(rId);
        assertSame(policyCmptType, r.getIpsObject());
        assertEquals(1, policyCmptType.getNumOfAssociations());
        assertTrue(sourceFile.isDirty());
        assertEquals(sourceFile, getLastContentChangeEvent().getIpsSrcFile());

        IPolicyCmptTypeAssociation r2 = policyCmptType.newPolicyCmptTypeAssociation();
        String r2Id = r2.getId();
        assertNotNull(r2Id);
        assertFalse(r2Id.equals(rId));
    }

    @Test
    public void testGetRelations() {
        assertEquals(0, policyCmptType.getPolicyCmptTypeAssociations().size());
        IPolicyCmptTypeAssociation r1 = policyCmptType.newPolicyCmptTypeAssociation();
        IPolicyCmptTypeAssociation r2 = policyCmptType.newPolicyCmptTypeAssociation();
        assertSame(r1, policyCmptType.getPolicyCmptTypeAssociations().get(0));
        assertSame(r2, policyCmptType.getPolicyCmptTypeAssociations().get(1));

        // make sure a defensive copy is returned.
        policyCmptType.getPolicyCmptTypeAssociations().clear();
        assertNotNull(policyCmptType.getPolicyCmptTypeAssociations().get(0));
    }

    @Test
    public void testDependsOnAssociation() {
        IPolicyCmptType a = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "A");
        IPolicyCmptType b = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "B");
        IPolicyCmptType c = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "C");

        IAssociation aToB = a.newAssociation();
        aToB.setAssociationType(AssociationType.ASSOCIATION);
        aToB.setTarget(b.getQualifiedName());

        IAssociation bToC = b.newAssociation();
        bToC.setAssociationType(AssociationType.ASSOCIATION);
        bToC.setTarget(c.getQualifiedName());

        List<IDependency> dependencyList = Arrays.asList(a.dependsOn());
        assertEquals(2, dependencyList.size());
        IDependency dependency = IpsObjectDependency.createReferenceDependency(a.getQualifiedNameType(),
                b.getQualifiedNameType());
        assertTrue(dependencyList.contains(dependency));

        assertSingleDependencyDetail(a, dependency, aToB, IAssociation.PROPERTY_TARGET);
    }

    @Test
    public void testDependsOnMethodParameterDatatypes() {
        IPolicyCmptType a = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "A");
        IPolicyCmptType b = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "B");
        IPolicyCmptType c = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "C");

        IMethod aMethod = a.newMethod();
        aMethod.setDatatype(b.getQualifiedName());
        aMethod.setModifier(Modifier.PUBLIC);
        aMethod.setName("aMethod");
        IParameter aMethodParam = aMethod.newParameter(c.getQualifiedName(), "cP");

        List<IDependency> dependencyList = Arrays.asList(a.dependsOn());
        assertEquals(3, dependencyList.size());
        IDependency dependency = new DatatypeDependency(a.getQualifiedNameType(), b.getQualifiedName());
        assertTrue(dependencyList.contains(dependency));
        assertSingleDependencyDetail(a, dependency, aMethod, IMethod.PROPERTY_DATATYPE);

        dependency = new DatatypeDependency(a.getQualifiedNameType(), c.getQualifiedName());
        assertTrue(dependencyList.contains(dependency));
        assertSingleDependencyDetail(a, dependency, aMethodParam, IParameter.PROPERTY_DATATYPE);
    }

    @Test
    public void testDependsOn() throws Exception {
        IPolicyCmptType a = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "A");
        IPolicyCmptType b = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "B");
        IPolicyCmptType c = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "C");
        IPolicyCmptTypeAssociation cToB = c.newPolicyCmptTypeAssociation();

        cToB.setTarget(b.getQualifiedName());

        c.setSupertype(a.getQualifiedName());
        List<IDependency> dependencyList = Arrays.asList(c.dependsOn());
        assertEquals(3, dependencyList.size());
        IDependency dependency = IpsObjectDependency.createSubtypeDependency(c.getQualifiedNameType(),
                a.getQualifiedNameType());
        assertTrue(dependencyList.contains(dependency));
        assertSingleDependencyDetail(c, dependency, c, IPolicyCmptType.PROPERTY_SUPERTYPE);

        dependency = IpsObjectDependency.createReferenceDependency(c.getQualifiedNameType(), b.getQualifiedNameType());
        assertTrue(dependencyList.contains(dependency));
        assertSingleDependencyDetail(c, dependency, cToB, IAssociation.PROPERTY_TARGET);

        // test if a circle in the type hierarchy does not lead to a stack overflow exception
        c.setSupertype(c.getQualifiedName());
        dependencyList = Arrays.asList(c.dependsOn());
        assertEquals(3, dependencyList.size());
        dependency = IpsObjectDependency.createReferenceDependency(c.getQualifiedNameType(), b.getQualifiedNameType());
        assertTrue(dependencyList.contains(dependency));
        assertSingleDependencyDetail(c, dependency, cToB, IAssociation.PROPERTY_TARGET);

        // this is actually not possible
        c.setSupertype(a.getQualifiedName());
        a.setSupertype(c.getQualifiedName());
        dependencyList = Arrays.asList(c.dependsOn());
        assertEquals(3, dependencyList.size());
        dependency = IpsObjectDependency.createSubtypeDependency(c.getQualifiedNameType(), a.getQualifiedNameType());
        assertTrue(dependencyList.contains(dependency));
        assertSingleDependencyDetail(c, dependency, c, IPolicyCmptType.PROPERTY_SUPERTYPE);
        dependency = IpsObjectDependency.createReferenceDependency(c.getQualifiedNameType(), b.getQualifiedNameType());
        assertTrue(dependencyList.contains(dependency));
        assertSingleDependencyDetail(c, dependency, cToB, IAssociation.PROPERTY_TARGET);
    }

    @Test
    public void testDependsOnEnumType() throws Exception {
        IEnumType enumType = newDefaultEnumType(ipsProject, "Gender");

        IEnumValue male = enumType.newEnumValue();
        male.getEnumAttributeValues().get(0).setValue(ValueFactory.createStringValue("MALE"));
        male.getEnumAttributeValues().get(1).setValue(ValueFactory.createStringValue("1"));
        male.getEnumAttributeValues().get(2).setValue(ValueFactory.createStringValue("male"));

        IEnumValue female = enumType.newEnumValue();
        female.getEnumAttributeValues().get(0).setValue(ValueFactory.createStringValue("FEMALE"));
        female.getEnumAttributeValues().get(1).setValue(ValueFactory.createStringValue("2"));
        female.getEnumAttributeValues().get(2).setValue(ValueFactory.createStringValue("female"));

        IPolicyCmptType type = newPolicyCmptType(ipsProject, "A");
        type.setConfigurableByProductCmptType(false);
        PolicyCmptTypeAttribute aAttr = (PolicyCmptTypeAttribute)type.newPolicyCmptTypeAttribute();
        aAttr.setAttributeType(AttributeType.CHANGEABLE);
        aAttr.setDatatype("Gender");
        aAttr.setModifier(Modifier.PUBLIC);
        aAttr.setName("aAttr");

        // make sure the policy component type is valid
        assertTrue(type.validate(ipsProject).isEmpty());

        // make sure datatype is available
        Datatype datatype = ipsProject.findDatatype("Gender");
        assertNotNull(datatype);

        // expect dependency on the TableContents defined above. Dependency on the Tablestructure is
        // no longer expected since we have introduced a DatatypeDependency
        IDependency[] dependencies = type.dependsOn();
        List<IDependency> nameTypeList = Arrays.asList(dependencies);
        IDependency dependency = new DatatypeDependency(type.getQualifiedNameType(),
                enumType.getQualifiedNameType().getName());
        assertTrue(nameTypeList.contains(dependency));
        assertSingleDependencyDetail(type, dependency, aAttr, IAttribute.PROPERTY_DATATYPE);
    }

    @Test
    public void testDependsOnComposition() throws Exception {
        IPolicyCmptType a = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "AggregateRoot");
        IPolicyCmptType d1 = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "Detail1");
        IPolicyCmptType d2 = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "Detail2");
        IPolicyCmptType s2 = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "SupertypeOfDetail2");

        IPolicyCmptTypeAssociation aToD1 = a.newPolicyCmptTypeAssociation();
        aToD1.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        aToD1.setTarget(d1.getQualifiedName());

        IPolicyCmptTypeAssociation d1ToD2 = d1.newPolicyCmptTypeAssociation();
        d1ToD2.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        d1ToD2.setTarget(d2.getQualifiedName());

        d2.setSupertype(s2.getQualifiedName());

        assertEquals(2, a.dependsOn().length);

        IIpsProjectProperties props = ipsProject.getProperties();
        props.setBuilderSetId(AggregateRootBuilderSet.ID);
        ipsProject.setProperties(props);
        AggregateRootBuilderSet builderSet = new AggregateRootBuilderSet();
        builderSet.setIpsProject(ipsProject);
        ((IpsModel)ipsProject.getIpsModel()).setIpsArtefactBuilderSetInfos(
                new IIpsArtefactBuilderSetInfo[] { new TestArtefactBuilderSetInfo(builderSet) });

        List<IDependency> dependsOn = Arrays.asList(a.dependsOn());
        IDependency dependency = IpsObjectDependency.createCompostionMasterDetailDependency(a.getQualifiedNameType(),
                d1.getQualifiedNameType());
        assertTrue(dependsOn.contains(dependency));
        assertSingleDependencyDetail(a, dependency, aToD1, IAssociation.PROPERTY_TARGET);

        dependsOn = Arrays.asList(d1.dependsOn());
        dependency = IpsObjectDependency.createCompostionMasterDetailDependency(d1.getQualifiedNameType(),
                d2.getQualifiedNameType());
        assertTrue(dependsOn.contains(dependency));
        assertSingleDependencyDetail(d1, dependency, d1ToD2, IAssociation.PROPERTY_TARGET);

        dependsOn = Arrays.asList(d2.dependsOn());
        dependency = IpsObjectDependency.createSubtypeDependency(d2.getQualifiedNameType(), s2.getQualifiedNameType());
        assertTrue(dependsOn.contains(dependency));
        assertSingleDependencyDetail(d2, dependency, d2, IPolicyCmptType.PROPERTY_SUPERTYPE);
    }

    @Test
    public void testDependsOnProductCmptType() {
        IPolicyCmptType aPolicyCmptType = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "A");
        IProductCmptType aProductCmptType = newProductCmptType(ipsProject, "AProduct");

        List<IDependency> dependencies = Arrays.asList(aPolicyCmptType.dependsOn());
        assertEquals(1, dependencies.size());
        assertTrue(dependencies.contains(IpsObjectDependency.create(aPolicyCmptType.getQualifiedNameType(),
                new QualifiedNameType(aPolicyCmptType.getQualifiedName(), IpsObjectType.PRODUCT_CMPT_TYPE),
                DependencyType.VALIDATION)));

        aPolicyCmptType.setProductCmptType(aProductCmptType.getQualifiedName());

        dependencies = Arrays.asList(aPolicyCmptType.dependsOn());
        assertEquals(2, dependencies.size());
        assertTrue(dependencies.contains(IpsObjectDependency.create(aPolicyCmptType.getQualifiedNameType(),
                new QualifiedNameType(aPolicyCmptType.getQualifiedName(), IpsObjectType.PRODUCT_CMPT_TYPE),
                DependencyType.VALIDATION)));
        IDependency dependency = IpsObjectDependency.create(aPolicyCmptType.getQualifiedNameType(),
                aProductCmptType.getQualifiedNameType(), DependencyType.CONFIGUREDBY);
        assertTrue(dependencies.contains(dependency));
    }

    @Test
    public void testGetIpsObjectType() {
        assertEquals(IpsObjectType.POLICY_CMPT_TYPE, policyCmptType.getIpsObjectType());
    }

    @Test
    public void testInitFromXml() {
        Element element = getTestDocument().getDocumentElement();
        policyCmptType.setConfigurableByProductCmptType(false);
        policyCmptType.initFromXml(element);
        assertTrue(policyCmptType.isConfigurableByProductCmptType());
        assertEquals("Product", policyCmptType.getProductCmptType());
        assertEquals("SuperType", policyCmptType.getSupertype());
        assertTrue(policyCmptType.isAbstract());
        assertEquals("blabla", policyCmptType.getDescriptionText(Locale.US));

        List<IPolicyCmptTypeAttribute> a = policyCmptType.getPolicyCmptTypeAttributes();
        assertEquals(1, a.size());

        List<IMethod> m = policyCmptType.getMethods();
        assertEquals(1, m.size());

        List<IValidationRule> rules = policyCmptType.getValidationRules();
        assertEquals(1, rules.size());

        List<IPolicyCmptTypeAssociation> r = policyCmptType.getPolicyCmptTypeAssociations();
        assertEquals(1, r.size());

        policyCmptType.initFromXml(element);
        assertEquals(1, policyCmptType.getNumOfAttributes());
        assertEquals(1, policyCmptType.getNumOfMethods());
        assertEquals(1, policyCmptType.getNumOfAssociations());
        assertEquals(1, policyCmptType.getNumOfRules());

        // test if the object references have remained the same
        assertSame(a.get(0), policyCmptType.getPolicyCmptTypeAttributes().get(0));
        assertSame(r.get(0), policyCmptType.getPolicyCmptTypeAssociations().get(0));
        assertSame(m.get(0), policyCmptType.getMethods().get(0));
        assertSame(rules.get(0), policyCmptType.getValidationRules().get(0));
    }

    @Test
    public void testToXml() {
        policyCmptType.setConfigurableByProductCmptType(true);
        policyCmptType.setProductCmptType("Product");
        IDescription description = policyCmptType.getDescription(Locale.US);
        description.setText("blabla");
        policyCmptType.setAbstract(true);
        policyCmptType.setGenerateValidatorClass(true);
        policyCmptType.setSupertype("NewSuperType");
        IPolicyCmptTypeAttribute a1 = policyCmptType.newPolicyCmptTypeAttribute();
        a1.setName("a1");
        IPolicyCmptTypeAttribute a2 = policyCmptType.newPolicyCmptTypeAttribute();
        a2.setName("a2");
        IMethod m1 = policyCmptType.newMethod();
        m1.setName("m1");
        IMethod m2 = policyCmptType.newMethod();
        m2.setName("m2");
        IValidationRule rule1 = policyCmptType.newRule();
        rule1.setName("rule1");
        IValidationRule rule2 = policyCmptType.newRule();
        rule2.setName("rule2");
        IPolicyCmptTypeAssociation r1 = policyCmptType.newPolicyCmptTypeAssociation();
        r1.setTarget("t1");
        IPolicyCmptTypeAssociation r2 = policyCmptType.newPolicyCmptTypeAssociation();
        r2.setTarget("t2");

        Element element = policyCmptType.toXml(newDocument());

        PolicyCmptType copy = (PolicyCmptType)newIpsObject(ipsProject, IpsObjectType.POLICY_CMPT_TYPE, "Copy");
        copy.setConfigurableByProductCmptType(false);
        copy.initFromXml(element);
        assertTrue(copy.isConfigurableByProductCmptType());
        assertEquals("Product", copy.getProductCmptType());
        assertEquals("NewSuperType", copy.getSupertype());
        assertTrue(copy.isAbstract());
        assertTrue(copy.isGenerateValidatorClass());
        assertEquals("blabla", copy.getDescriptionText(Locale.US));
        List<IPolicyCmptTypeAttribute> attributes = copy.getPolicyCmptTypeAttributes();
        assertEquals(2, attributes.size());
        assertEquals("a1", attributes.get(0).getName());
        assertEquals("a2", attributes.get(1).getName());
        List<IMethod> methods = copy.getMethods();
        assertEquals("m1", methods.get(0).getName());
        assertEquals("m2", methods.get(1).getName());
        List<IValidationRule> rules = copy.getValidationRules();
        assertEquals("rule1", rules.get(0).getName());
        assertEquals("rule2", rules.get(1).getName());
        List<IPolicyCmptTypeAssociation> relations = copy.getPolicyCmptTypeAssociations();
        assertEquals("t1", relations.get(0).getTarget());
        assertEquals("t2", relations.get(1).getTarget());
    }

    @Test
    public void testGetSupertypeHierarchy() {
        ITypeHierarchy hierarchy = policyCmptType.getSupertypeHierarchy();
        assertNotNull(hierarchy);
    }

    @Test
    public void testGetSubtypeHierarchy() {
        ITypeHierarchy hierarchy = policyCmptType.getSubtypeHierarchy();
        assertNotNull(hierarchy);
    }

    @Test
    public void testSetProductCmptType() {
        super.testPropertyAccessReadWrite(IPolicyCmptType.class, IPolicyCmptType.PROPERTY_PRODUCT_CMPT_TYPE,
                policyCmptType, "NewProduct");
    }

    @Test
    public void testNewPart() {
        assertNotNull(policyCmptType.newPart(PolicyCmptTypeAttribute.class));
        assertNotNull(policyCmptType.newPart(PolicyCmptTypeMethod.class));
        assertNotNull(policyCmptType.newPart(PolicyCmptTypeAssociation.class));
        assertNotNull(policyCmptType.newPart(ValidationRule.class));
    }

    @Test
    public void testIsAggregateRoot() {
        assertTrue(policyCmptType.isAggregateRoot());

        policyCmptType.newPolicyCmptTypeAssociation().setAssociationType(AssociationType.ASSOCIATION);
        assertTrue(policyCmptType.isAggregateRoot());

        policyCmptType.newPolicyCmptTypeAssociation().setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        assertTrue(policyCmptType.isAggregateRoot());

        policyCmptType.newPolicyCmptTypeAssociation().setAssociationType(AssociationType.COMPOSITION_DETAIL_TO_MASTER);
        assertFalse(policyCmptType.isAggregateRoot());

        // create a supertype
        IPolicyCmptType subtype = newPolicyCmptType(ipsProject, "Subtype");
        IPolicyCmptType supertype = newPolicyCmptType(ipsProject, "Supertype");
        subtype.setSupertype(supertype.getQualifiedName());
        subtype.getIpsSrcFile().save(null);

        supertype.newPolicyCmptTypeAssociation().setAssociationType(AssociationType.ASSOCIATION);
        assertTrue(subtype.isAggregateRoot());
        supertype.newPolicyCmptTypeAssociation().setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        assertTrue(subtype.isAggregateRoot());
        supertype.newPolicyCmptTypeAssociation().setAssociationType(AssociationType.COMPOSITION_DETAIL_TO_MASTER);
        assertFalse(subtype.isAggregateRoot());

        IPolicyCmptType invalidType = newPolicyCmptType(ipsProject, "InvalidType");
        invalidType.setSupertype(invalidType.getQualifiedName());
        assertTrue(invalidType.isAggregateRoot());
    }

    @Test
    public void testValidate_ProductCmptTypeNameMissing() throws Exception {
        MessageList ml = policyCmptType.validate(ipsProject);
        assertThat(ml, lacksMessageCode(IPolicyCmptType.MSGCODE_PRODUCT_CMPT_TYPE_NAME_MISSING));
        policyCmptType.setConfigurableByProductCmptType(true);
        policyCmptType.setProductCmptType("");
        ml = policyCmptType.validate(ipsProject);
        assertThat(ml, hasMessageCode(IPolicyCmptType.MSGCODE_PRODUCT_CMPT_TYPE_NAME_MISSING));
    }

    @Test
    public void testValidateSupertypeNotConfigurable() throws Exception {
        IPolicyCmptType superPcType = newPolicyCmptType(ipsProject, "Super");
        policyCmptType.setSupertype(superPcType.getQualifiedName());

        superPcType.setConfigurableByProductCmptType(false);
        policyCmptType.setConfigurableByProductCmptType(true);

        MessageList ml = superPcType.validate(superPcType.getIpsProject());
        assertThat(ml, lacksMessageCode(
                IPolicyCmptType.MSGCODE_SUPERTYPE_NOT_PRODUCT_RELEVANT_IF_THE_TYPE_IS_PRODUCT_RELEVANT));

        ml = policyCmptType.validate(ipsProject);
        assertThat(ml, lacksMessageCode(
                IPolicyCmptType.MSGCODE_SUPERTYPE_NOT_PRODUCT_RELEVANT_IF_THE_TYPE_IS_PRODUCT_RELEVANT));

        superPcType.setConfigurableByProductCmptType(true);
        ml = policyCmptType.validate(ipsProject);
        assertThat(ml, lacksMessageCode(
                IPolicyCmptType.MSGCODE_SUPERTYPE_NOT_PRODUCT_RELEVANT_IF_THE_TYPE_IS_PRODUCT_RELEVANT));
    }

    @Test
    public void testValidateOtherTypeWithSameNameTypeInIpsObjectPath() {
        IIpsProject a = newIpsProject();
        IProductCmptType aProductTypeProjectA = newProductCmptType(a, "faktorzehn.example.APolicy");
        IIpsProject b = newIpsProject();
        IPolicyCmptType aPolicyProjectB = newPolicyCmptTypeWithoutProductCmptType(b, "faktorzehn.example.APolicy");

        IIpsObjectPath bPath = b.getIpsObjectPath();
        IIpsObjectPathEntry[] bPathEntries = bPath.getEntries();
        ArrayList<IIpsObjectPathEntry> newbPathEntries = new ArrayList<>();
        newbPathEntries.add(new IpsProjectRefEntry((IpsObjectPath)bPath, a));
        for (IIpsObjectPathEntry bPathEntrie : bPathEntries) {
            newbPathEntries.add(bPathEntrie);
        }
        bPath.setEntries(newbPathEntries.toArray(new IIpsObjectPathEntry[newbPathEntries.size()]));
        b.setIpsObjectPath(bPath);

        MessageList msgList = aProductTypeProjectA.validate(a);
        assertNull(msgList.getMessageByCode(IType.MSGCODE_OTHER_TYPE_WITH_SAME_NAME_IN_DEPENDENT_PROJECT_EXISTS));

        msgList = aPolicyProjectB.validate(b);
        assertNotNull(msgList.getMessageByCode(IType.MSGCODE_OTHER_TYPE_WITH_SAME_NAME_IN_DEPENDENT_PROJECT_EXISTS));
    }

    @Test
    public void testValidateDuplicateRulesNames() throws Exception {
        IValidationRule rule1 = policyCmptType.newRule();
        rule1.setName("aRule");
        MessageList msgList = policyCmptType.validate(ipsProject);
        assertNull(msgList.getMessageByCode(IValidationRule.MSGCODE_DUPLICATE_RULE_NAME));
        IValidationRule rule2 = policyCmptType.newRule();
        rule2.setName("aRule");
        msgList = policyCmptType.validate(ipsProject);
        assertNotNull(msgList.getMessageByCode(IValidationRule.MSGCODE_DUPLICATE_RULE_NAME));

        rule2.delete();
        msgList = policyCmptType.validate(ipsProject);
        assertNull(msgList.getMessageByCode(IValidationRule.MSGCODE_DUPLICATE_RULE_NAME));

        IMethod method = policyCmptType.newMethod();
        method.setName("aRule");
        method.setDatatype(Datatype.VOID.getName());
        msgList = policyCmptType.validate(ipsProject);
        assertNotNull(msgList.getMessageByCode(IValidationRule.MSGCODE_VALIDATION_RULE_METHOD_NAME_CONFLICT));

        method.newParameter(Datatype.STRING.getQualifiedName(), "aParam");
        msgList = policyCmptType.validate(ipsProject);
        assertNull(msgList.getMessageByCode(IValidationRule.MSGCODE_VALIDATION_RULE_METHOD_NAME_CONFLICT));
    }

    @Test
    public void testPersistenceSupport() {
        assertFalse(policyCmptType.getIpsProject().isPersistenceSupportEnabled());
        IIpsProjectProperties properties = policyCmptType.getIpsProject().getProperties();
        properties.setPersistenceSupport(true);
        ipsProject.setProperties(properties);
        assertTrue(policyCmptType.getIpsProject().isPersistenceSupportEnabled());

        policyCmptType = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "TestPolicyWithPerstence");
        assertNotNull(policyCmptType.getPersistenceTypeInfo());
        policyCmptType.getPersistenceTypeInfo().setPersistentType(PersistentType.ENTITY);

        // per default the policy component type should persist
        assertTrue(policyCmptType.isPersistentEnabled());
        policyCmptType.getPersistenceTypeInfo().setPersistentType(PersistentType.NONE);
        assertFalse(policyCmptType.isPersistentEnabled());
        policyCmptType.getPersistenceTypeInfo().setPersistentType(PersistentType.NONE);
    }

    @Test
    public void testValidateDuplicateAssociationSpecialCase() {
        // MTB#357 special case: a detail-to-master association that is a subset of a derived-union
        // could have the same name as the corresponding derived union association

        IPolicyCmptType supertype = newPolicyCmptType(ipsProject, "supertype");
        IPolicyCmptType target = newPolicyCmptType(ipsProject, "target");
        IPolicyCmptType superTarget = newPolicyCmptType(ipsProject, "superTarget");

        IPolicyCmptTypeAssociation derivedUnion = supertype.newPolicyCmptTypeAssociation();
        derivedUnion.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        derivedUnion.setTargetRoleSingular("aSuperTarget");
        derivedUnion.setDerivedUnion(true);
        derivedUnion.setTarget(superTarget.getQualifiedName());

        IPolicyCmptTypeAssociation superInverse = derivedUnion.newInverseAssociation();
        superInverse.setTargetRoleSingular("abc");

        IPolicyCmptTypeAssociation subset = policyCmptType.newPolicyCmptTypeAssociation();
        subset.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        subset.setTargetRoleSingular("aTarget");
        subset.setSubsettedDerivedUnion(derivedUnion.getTargetRoleSingular());
        subset.setTarget(target.getQualifiedName());
        IPolicyCmptTypeAssociation inverse = subset.newInverseAssociation();
        inverse.setTargetRoleSingular("abc");

        MessageList result = target.validate(ipsProject);
        assertNull(result.getMessageByCode(IType.MSGCODE_DUPLICATE_PROPERTY_NAME));

        // TODO Test with expected error message:
        // - beide Assoziationen am selben typ
        // - Maser-Detail-Association
        // - Not derived union
        // - not subset of derived union

    }

    @Test
    public void testFindAssociationsForTargetAndAssociationType() {
        IPolicyCmptType baseMotor = newPolicyCmptType(ipsProject, "BaseMotor");
        IPolicyCmptType injection = newPolicyCmptType(ipsProject, "Injection");

        List<IAssociation> associations = policyCmptType.findAssociationsForTargetAndAssociationType(
                injection.getQualifiedName(), AssociationType.COMPOSITION_MASTER_TO_DETAIL, ipsProject, false);
        assertEquals(0, associations.size());

        // Association: motor -> injection
        IAssociation association = policyCmptType.newAssociation();
        association.setTarget(injection.getQualifiedName());
        association.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);

        // Association: baseMotor -> injection
        IAssociation associationInBase = baseMotor.newAssociation();
        associationInBase.setTarget(injection.getQualifiedName());
        associationInBase.setAssociationType(AssociationType.ASSOCIATION);

        // result = 1, because super not set
        associations = policyCmptType.findAssociationsForTargetAndAssociationType(injection.getQualifiedName(),
                AssociationType.COMPOSITION_MASTER_TO_DETAIL, ipsProject, false);
        assertEquals(1, associations.size());

        policyCmptType.setSupertype(baseMotor.getQualifiedName());

        // result = 1, because association type of super type association not equal
        associations = policyCmptType.findAssociationsForTargetAndAssociationType(injection.getQualifiedName(),
                AssociationType.COMPOSITION_MASTER_TO_DETAIL, ipsProject, false);
        assertEquals(1, associations.size());

        // result = 1 using search without supertype
        associationInBase.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        associations = policyCmptType.findAssociationsForTargetAndAssociationType(injection.getQualifiedName(),
                AssociationType.COMPOSITION_MASTER_TO_DETAIL, ipsProject, false);
        assertEquals(1, associations.size());

        // result = 1 using search with supertype included
        associationInBase.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        associations = policyCmptType.findAssociationsForTargetAndAssociationType(injection.getQualifiedName(),
                AssociationType.COMPOSITION_MASTER_TO_DETAIL, ipsProject, true);
        assertEquals(2, associations.size());

        // shared association
        IIpsProjectProperties properties = ipsProject.getProperties();
        properties.setSharedDetailToMasterAssociations(true);
        ipsProject.setProperties(properties);

        IPolicyCmptType baseInjection = newPolicyCmptType(ipsProject, "baseInjection");
        injection.setSupertype(baseInjection.getQualifiedName());

        IPolicyCmptTypeAssociation sharedAsso = (IPolicyCmptTypeAssociation)injection.newAssociation();
        sharedAsso.setTarget(baseMotor.getQualifiedName());
        sharedAsso.setTargetRoleSingular("sharedAsso");

        associations = policyCmptType.findAssociationsForTargetAndAssociationType(injection.getQualifiedName(),
                AssociationType.COMPOSITION_DETAIL_TO_MASTER, ipsProject, false);
        assertEquals(0, associations.size());
        associations = policyCmptType.findAssociationsForTargetAndAssociationType(injection.getQualifiedName(),
                AssociationType.COMPOSITION_DETAIL_TO_MASTER, ipsProject, true);
        assertEquals(0, associations.size());

        sharedAsso.setSharedAssociation(true);
        sharedAsso.setAssociationType(AssociationType.COMPOSITION_DETAIL_TO_MASTER);

        associations = policyCmptType.findAssociationsForTargetAndAssociationType(injection.getQualifiedName(),
                AssociationType.COMPOSITION_DETAIL_TO_MASTER, ipsProject, false);
        assertEquals(0, associations.size());
        associations = policyCmptType.findAssociationsForTargetAndAssociationType(injection.getQualifiedName(),
                AssociationType.COMPOSITION_DETAIL_TO_MASTER, ipsProject, true);
        assertEquals(0, associations.size());

        associations = injection.findAssociationsForTargetAndAssociationType(policyCmptType.getQualifiedName(),
                AssociationType.COMPOSITION_DETAIL_TO_MASTER, ipsProject, false);
        assertEquals(1, associations.size());
        associations = injection.findAssociationsForTargetAndAssociationType(policyCmptType.getQualifiedName(),
                AssociationType.COMPOSITION_DETAIL_TO_MASTER, ipsProject, true);
        assertEquals(1, associations.size());
    }

    @Test
    public void testOverrideAttributes() {
        IPolicyCmptTypeAttribute attribute = policyCmptType.newPolicyCmptTypeAttribute();
        attribute.setName("override");
        attribute.setDatatype(Datatype.STRING.getQualifiedName());
        attribute.setValueSetConfiguredByProduct(true);
        attribute.setDefaultValue("defaultValue");
        attribute.setValueSetType(ValueSetType.ENUM);
        attribute.setAttributeType(AttributeType.DERIVED_BY_EXPLICIT_METHOD_CALL);
        for (IDescription description : attribute.getDescriptions()) {
            description.setText("Description");
        }

        IPolicyCmptType overridingType = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "OverridingType");
        overridingType.overrideAttributes(Arrays.asList(attribute));

        IPolicyCmptTypeAttribute overriddenAttribute = overridingType.getPolicyCmptTypeAttribute(attribute.getName());
        assertEquals(attribute.getDatatype(), overriddenAttribute.getDatatype());
        assertEquals(attribute.isProductRelevant(), overriddenAttribute.isProductRelevant());
        assertEquals(attribute.getDefaultValue(), overriddenAttribute.getDefaultValue());
        assertEquals(attribute.getValueSet().getValueSetType(), overriddenAttribute.getValueSet().getValueSetType());
        for (IDescription element : overriddenAttribute.getDescriptions()) {
            assertEquals(StringUtils.EMPTY, element.getText());
        }
        assertTrue(overriddenAttribute.isOverwrite());
    }

    @Test
    public void testFindOverrideAttributeCandidates() {
        IPolicyCmptType superPcType = newPolicyCmptType(ipsProject, "Super");

        // 1. Attribute
        IPolicyCmptTypeAttribute attribute = superPcType.newPolicyCmptTypeAttribute();
        attribute.setName("ToOverride");
        attribute.setDatatype(Datatype.STRING.getQualifiedName());
        attribute.setValueSetConfiguredByProduct(true);
        attribute.setDefaultValue("defaultValue");
        attribute.setValueSetType(ValueSetType.ENUM);
        attribute.setAttributeType(AttributeType.DERIVED_BY_EXPLICIT_METHOD_CALL);
        for (IDescription description : attribute.getDescriptions()) {
            description.setText("Overridden Description");
        }

        // 2. Attribute
        IPolicyCmptTypeAttribute attribute2 = superPcType.newPolicyCmptTypeAttribute();
        attribute2.setName("NotOverride");
        attribute2.setDatatype(Datatype.STRING.getQualifiedName());
        attribute2.setValueSetConfiguredByProduct(true);
        attribute2.setDefaultValue("defaultValue");
        attribute2.setValueSetType(ValueSetType.ENUM);
        attribute2.setAttributeType(AttributeType.DERIVED_BY_EXPLICIT_METHOD_CALL);

        IPolicyCmptType overridingType = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "OverridingType");
        overridingType.setSupertype(superPcType.getQualifiedName());

        List<IAttribute> findOverrideAttributeCandidates = overridingType.findOverrideAttributeCandidates(ipsProject);
        // 2 to override
        assertEquals(2, findOverrideAttributeCandidates.size());

        // now override the first
        overridingType.overrideAttributes(Arrays.asList(attribute));

        findOverrideAttributeCandidates = overridingType.findOverrideAttributeCandidates(ipsProject);
        // only the second to find
        assertEquals(1, findOverrideAttributeCandidates.size());
        assertEquals("NotOverride", findOverrideAttributeCandidates.get(0).getName());
    }

    @Test
    public void testValidateAbstractAttributes_abstractSubtype() throws Exception {
        IAttribute superAttr1 = superPolicyCmptType.newAttribute();
        superAttr1.setName(ATTR1);
        superAttr1.setDatatype(ABSTRACT_PARENT_ENUM);
        policyCmptType.setAbstract(true);

        MessageList list = new MessageList();
        policyCmptType.validateAbstractAttributes(list, ipsProject);

        assertThat(list, isEmpty());
    }

    @Test
    public void testValidateAbstractAttributes_noErrors() throws Exception {
        IAttribute superAttr1 = superPolicyCmptType.newAttribute();
        superAttr1.setName(ATTR1);
        superAttr1.setDatatype(ABSTRACT_PARENT_ENUM);
        IAttribute attr1 = policyCmptType.newAttribute();
        attr1.setName(ATTR1);
        attr1.setOverwrite(true);
        attr1.setDatatype(CONCRETE_CHILD_ENUM);

        MessageList list = new MessageList();
        policyCmptType.validateAbstractAttributes(list, ipsProject);

        assertThat(list, isEmpty());
    }

    @Test
    public void testValidateAbstractAttributes_noErrors_MultiSubclass() throws Exception {
        superSuperPolicyCmptType.newAttribute();
        IAttribute superAttr1 = superSuperPolicyCmptType.newAttribute();
        superAttr1.setName(ATTR1);
        superAttr1.setDatatype(ABSTRACT_PARENT_ENUM);
        IAttribute attr1 = superPolicyCmptType.newAttribute();
        attr1.setName(ATTR1);
        attr1.setOverwrite(true);
        attr1.setDatatype(CONCRETE_CHILD_ENUM);

        MessageList list = new MessageList();
        policyCmptType.validateAbstractAttributes(list, ipsProject);

        assertThat(list, isEmpty());
    }

    @Test
    public void testValidateAbstractAttributes_notOverwritten() throws Exception {
        IAttribute superAttr1 = superPolicyCmptType.newAttribute();
        superAttr1.setName(ATTR1);
        superAttr1.setDatatype(ABSTRACT_PARENT_ENUM);
        MessageList list = new MessageList();

        policyCmptType.validateAbstractAttributes(list, ipsProject);

        Message message = list.getMessageByCode(IType.MSGCODE_ABSTRACT_MISSING);
        assertThat(message, hasInvalidObject(policyCmptType, IType.PROPERTY_ABSTRACT));
    }

    @Test
    public void testValidateAbstractAttributes_overwrittenAbstractType() throws Exception {
        IAttribute superAttr1 = superPolicyCmptType.newAttribute();
        superAttr1.setName(ATTR1);
        superAttr1.setDatatype(ABSTRACT_PARENT_ENUM);
        IAttribute attr1 = policyCmptType.newAttribute();
        attr1.setName(ATTR1);
        attr1.setOverwrite(true);
        attr1.setDatatype(ABSTRACT_PARENT_ENUM);

        MessageList list = policyCmptType.validate(ipsProject);

        Message message = list.getMessageByCode(IType.MSGCODE_ABSTRACT_MISSING);
        assertThat(message, is(nullValue()));
    }

    @Test
    public void testValidateAbstractAttributes_overwrittenAbstractType_ProductRelevant() throws Exception {
        IAttribute superAttr1 = superPolicyCmptType.newPolicyCmptTypeAttribute();
        superAttr1.setName(ATTR1);
        superAttr1.setDatatype(ABSTRACT_PARENT_ENUM);
        IPolicyCmptTypeAttribute attr1 = policyCmptType.newPolicyCmptTypeAttribute();
        attr1.setName(ATTR1);
        attr1.setOverwrite(true);
        attr1.setDatatype(ABSTRACT_PARENT_ENUM);
        attr1.setValueSetConfiguredByProduct(true);

        MessageList list = policyCmptType.validate(ipsProject);

        Message message = list.getMessageByCode(IType.MSGCODE_ABSTRACT_MISSING);
        assertThat(message, hasInvalidObject(attr1, IAttribute.PROPERTY_DATATYPE));
        assertThat(message, hasInvalidObject(policyCmptType, IType.PROPERTY_ABSTRACT));
    }

    @Test
    public void testConstructor_NoIpsProject() {
        IpsSrcFile file = new IpsSrcFile(null, IpsObjectType.POLICY_CMPT_TYPE.getFileName("file"));
        PolicyCmptType cmpt = new PolicyCmptType(file);

        assertFalse(cmpt.isGenerateValidatorClass());
    }

    public void testValidateSameGenerateValidatorClassSetting_sameSetting() {
        superSuperPolicyCmptType.setGenerateValidatorClass(false);
        superPolicyCmptType.setGenerateValidatorClass(false);
        policyCmptType.setGenerateValidatorClass(false);

        MessageList messageList = superPolicyCmptType.validate(ipsProject);

        assertThat(messageList,
                lacksMessageCode(IPolicyCmptType.MSGCODE_DIFFERENT_GENERATE_VALIDATOR_CLASS_SETTING_IN_HIERARCHY));
    }

    @Test
    public void testValidateSameGenerateValidatorClassSetting_differentSettingInSuperClass() {
        superSuperPolicyCmptType.setGenerateValidatorClass(true);
        superPolicyCmptType.setGenerateValidatorClass(false);
        policyCmptType.setGenerateValidatorClass(false);

        MessageList messageList = superPolicyCmptType.validate(ipsProject);

        assertThat(messageList,
                hasMessageCode(
                        IPolicyCmptType.MSGCODE_DIFFERENT_GENERATE_VALIDATOR_CLASS_SETTING_IN_HIERARCHY));
        Message message = messageList
                .getMessageByCode(IPolicyCmptType.MSGCODE_DIFFERENT_GENERATE_VALIDATOR_CLASS_SETTING_IN_HIERARCHY);
        assertThat(message, hasSeverity(Severity.ERROR));
        assertThat(message,
                hasInvalidObject(superPolicyCmptType, IPolicyCmptType.PROPERTY_GENERATE_VALIDATOR_CLASS));
        assertThat(message.getText(), CoreMatchers.containsString(superSuperPolicyCmptType.getQualifiedName()));
    }

    @Test
    public void testGetDescriptionFromThisOrSuper() {
        superPolicyCmptType.setDescriptionText(Locale.ENGLISH, "english description");
        assertEquals("english description", policyCmptType.getDescriptionTextFromThisOrSuper(Locale.ENGLISH));
        policyCmptType.setDescriptionText(Locale.ENGLISH, "overwritten description");
        assertEquals("overwritten description", policyCmptType.getDescriptionTextFromThisOrSuper(Locale.ENGLISH));
    }

    private static class AggregateRootBuilderSet extends EmptyBuilderSet {

        public static final String ID = "AggregateRootBuilderSet";

        @Override
        public boolean containsAggregateRootBuilder() {
            return true;
        }

        @Override
        public String getId() {
            return ID;
        }

    }

}
