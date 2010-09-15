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

package org.faktorips.devtools.core.internal.model.pctype;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractDependencyTest;
import org.faktorips.abstracttest.builder.TestArtefactBuilderSetInfo;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.builder.EmptyBuilderSet;
import org.faktorips.devtools.core.internal.model.IpsModel;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsObjectPath;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsProjectRefEntry;
import org.faktorips.devtools.core.internal.model.type.Method;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.ContentsChangeListener;
import org.faktorips.devtools.core.model.DatatypeDependency;
import org.faktorips.devtools.core.model.IDependency;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IpsObjectDependency;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.enums.IEnumValue;
import org.faktorips.devtools.core.model.ipsobject.IDescription;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsobject.Modifier;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSetInfo;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPathEntry;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.core.model.pctype.AssociationType;
import org.faktorips.devtools.core.model.pctype.AttributeType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.pctype.ITypeHierarchy;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.model.pctype.IPersistentTypeInfo.PersistentType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.core.model.type.IMethod;
import org.faktorips.devtools.core.model.type.IParameter;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Element;

public class PolicyCmptTypeTest extends AbstractDependencyTest implements ContentsChangeListener {

    private IIpsPackageFragment pack;
    private IIpsSrcFile sourceFile;
    private PolicyCmptType policyCmptType;
    private ContentChangeEvent lastEvent;
    private IIpsProject ipsProject;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ipsProject = this.newIpsProject();
        policyCmptType = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "TestPolicy");
        policyCmptType.setConfigurableByProductCmptType(false);
        sourceFile = policyCmptType.getIpsSrcFile();
        pack = policyCmptType.getIpsPackageFragment();
    }

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

    public void testValidateProductCmptTypeDoesNotConfigureThisType() throws CoreException {
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

    public void testGetOverrideCandidates() throws CoreException {
        assertEquals(0, policyCmptType.findOverrideMethodCandidates(false, ipsProject).length);

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

        IMethod[] candidates = policyCmptType.findOverrideMethodCandidates(false, ipsProject);
        assertEquals(2, candidates.length);
        assertEquals(m3, candidates[0]);
        assertEquals(m5, candidates[1]);
        // notes:
        // m2 is not a candidate because it is already overridden by m1
        // m4 is not a candidate because it is overridden by m3 and m3 comes first in the hierarchy

        // only not implemented abstract methods
        candidates = policyCmptType.findOverrideMethodCandidates(true, ipsProject);
        assertEquals(1, candidates.length);
        assertEquals(m5, candidates[0]);
        // note: now only m5 is a candidate as it's abstract, m3 is not.

        // override the supersupertype method m5 in the supertype
        // => now also m5 is not a candidate any more, if only not implemented abstract methods are
        // requested.
        supertype.overrideMethods(new IMethod[] { m5 });
        candidates = policyCmptType.findOverrideMethodCandidates(true, ipsProject);
        assertEquals(0, candidates.length);
    }

    public void testFindProductCmptType() throws CoreException {
        policyCmptType.setProductCmptType("");
        assertNull(policyCmptType.findProductCmptType(ipsProject));

        policyCmptType.setProductCmptType("MotorProduct");
        policyCmptType.setConfigurableByProductCmptType(false);
        assertNull(policyCmptType.findProductCmptType(ipsProject));

        policyCmptType.setConfigurableByProductCmptType(true);
        policyCmptType.setProductCmptType("Unkown");
        assertNull(policyCmptType.findProductCmptType(ipsProject));

        IProductCmptType productCmptType = newProductCmptType(ipsProject, policyCmptType.getIpsPackageFragment()
                .getName()
                + ".Product");
        policyCmptType.setProductCmptType(productCmptType.getQualifiedName());
        assertSame(productCmptType, policyCmptType.findProductCmptType(ipsProject));
    }

    public void testFindAttributeInSupertypeHierarchy() throws CoreException {
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
        attribute.setProductRelevant(true);
        assertFalse(policyCmptType.isExtensionCompilationUnitGenerated());
        attribute.setProductRelevant(false);
        assertTrue(policyCmptType.isExtensionCompilationUnitGenerated());
        attribute.setAttributeType(AttributeType.DERIVED_ON_THE_FLY);
        assertTrue(policyCmptType.isExtensionCompilationUnitGenerated());
        attribute.setAttributeType(AttributeType.CHANGEABLE);
        assertFalse(policyCmptType.isExtensionCompilationUnitGenerated());
        attribute.setAttributeType(AttributeType.CONSTANT);
        assertFalse(policyCmptType.isExtensionCompilationUnitGenerated());

    }

    public void testNewAttribute() {
        sourceFile.getIpsModel().addChangeListener(this);
        IPolicyCmptTypeAttribute a = policyCmptType.newPolicyCmptTypeAttribute();
        assertSame(policyCmptType, a.getIpsObject());
        assertEquals(1, policyCmptType.getNumOfAttributes());
        assertTrue(sourceFile.isDirty());
        assertEquals(sourceFile, lastEvent.getIpsSrcFile());
        assertEquals(a, lastEvent.getPart());
        assertEquals(ContentChangeEvent.TYPE_PART_ADDED, lastEvent.getEventType());
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

    public void testGetAttributes() {
        assertEquals(0, policyCmptType.getPolicyCmptTypeAttributes().length);
        IPolicyCmptTypeAttribute a1 = policyCmptType.newPolicyCmptTypeAttribute();
        IPolicyCmptTypeAttribute a2 = policyCmptType.newPolicyCmptTypeAttribute();
        assertSame(a1, policyCmptType.getPolicyCmptTypeAttributes()[0]);
        assertSame(a2, policyCmptType.getPolicyCmptTypeAttributes()[1]);

        // make sure a defensive copy is returned.
        policyCmptType.getPolicyCmptTypeAttributes()[0] = null;
        assertNotNull(policyCmptType.getPolicyCmptTypeAttributes()[0]);
    }

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

    public void testNewMethod() {
        sourceFile.getIpsModel().addChangeListener(this);
        IMethod m = policyCmptType.newMethod();
        String mId = m.getId();
        assertNotNull(mId);
        assertSame(policyCmptType, m.getIpsObject());
        assertEquals(1, policyCmptType.getNumOfMethods());
        assertTrue(sourceFile.isDirty());
        assertEquals(sourceFile, lastEvent.getIpsSrcFile());

        IMethod m2 = policyCmptType.newMethod();
        String m2Id = m2.getId();
        assertNotNull(m2Id);
        assertFalse(m2Id.equals(mId));
    }

    public void testGetMethods() {
        assertEquals(0, policyCmptType.getMethods().length);
        IMethod m1 = policyCmptType.newMethod();
        IMethod m2 = policyCmptType.newMethod();
        assertSame(m1, policyCmptType.getMethods()[0]);
        assertSame(m2, policyCmptType.getMethods()[1]);

        // make sure a defensive copy is returned.
        policyCmptType.getMethods()[0] = null;
        assertNotNull(policyCmptType.getMethods()[0]);
    }

    public void testNewRule() {
        sourceFile.getIpsModel().addChangeListener(this);
        IValidationRule r = policyCmptType.newRule();
        String rId = r.getId();
        assertNotNull(rId);
        assertSame(policyCmptType, r.getIpsObject());
        assertEquals(1, policyCmptType.getNumOfRules());
        assertTrue(sourceFile.isDirty());
        assertEquals(sourceFile, lastEvent.getIpsSrcFile());

        IValidationRule r2 = policyCmptType.newRule();
        String r2Id = r2.getId();
        assertNotNull(r2Id);
        assertFalse(r2Id.equals(rId));
    }

    public void testGetRules() {
        assertEquals(0, policyCmptType.getRules().length);
        IValidationRule r1 = policyCmptType.newRule();
        IValidationRule r2 = policyCmptType.newRule();
        assertSame(r1, policyCmptType.getRules()[0]);
        assertSame(r2, policyCmptType.getRules()[1]);

        // make sure a defensive copy is returned.
        policyCmptType.getRules()[0] = null;
        assertNotNull(policyCmptType.getRules()[0]);
    }

    public void testNewRelation() {
        sourceFile.getIpsModel().addChangeListener(this);
        IPolicyCmptTypeAssociation r = policyCmptType.newPolicyCmptTypeAssociation();
        String rId = r.getId();
        assertNotNull(rId);
        assertSame(policyCmptType, r.getIpsObject());
        assertEquals(1, policyCmptType.getNumOfAssociations());
        assertTrue(sourceFile.isDirty());
        assertEquals(sourceFile, lastEvent.getIpsSrcFile());

        IPolicyCmptTypeAssociation r2 = policyCmptType.newPolicyCmptTypeAssociation();
        String r2Id = r2.getId();
        assertNotNull(r2Id);
        assertFalse(r2Id.equals(rId));
    }

    public void testGetRelations() {
        assertEquals(0, policyCmptType.getPolicyCmptTypeAssociations().length);
        IPolicyCmptTypeAssociation r1 = policyCmptType.newPolicyCmptTypeAssociation();
        IPolicyCmptTypeAssociation r2 = policyCmptType.newPolicyCmptTypeAssociation();
        assertSame(r1, policyCmptType.getPolicyCmptTypeAssociations()[0]);
        assertSame(r2, policyCmptType.getPolicyCmptTypeAssociations()[1]);

        // make sure a defensive copy is returned.
        policyCmptType.getPolicyCmptTypeAssociations()[0] = null;
        assertNotNull(policyCmptType.getPolicyCmptTypeAssociations()[0]);
    }

    public void testDependsOnAssociation() throws CoreException {
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
        IDependency dependency = IpsObjectDependency.createReferenceDependency(a.getQualifiedNameType(), b
                .getQualifiedNameType());
        assertTrue(dependencyList.contains(dependency));

        assertSingleDependencyDetail(a, dependency, aToB, IAssociation.PROPERTY_TARGET);
    }

    public void testDependsOnMethodParameterDatatypes() throws CoreException {
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

    public void testDependsOn() throws Exception {
        IPolicyCmptType a = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "A");
        IPolicyCmptType b = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "B");
        IPolicyCmptType c = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "C");
        IPolicyCmptTypeAssociation cToB = c.newPolicyCmptTypeAssociation();

        cToB.setTarget(b.getQualifiedName());

        c.setSupertype(a.getQualifiedName());
        List<IDependency> dependencyList = Arrays.asList(c.dependsOn());
        assertEquals(3, dependencyList.size());
        IDependency dependency = IpsObjectDependency.createSubtypeDependency(c.getQualifiedNameType(), a
                .getQualifiedNameType());
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

    public void testDependsOnEnumType() throws Exception {
        IEnumType enumType = newDefaultEnumType(ipsProject, "Gender");

        IEnumValue male = enumType.newEnumValue();
        male.getEnumAttributeValues().get(0).setValue("MALE");
        male.getEnumAttributeValues().get(1).setValue("1");
        male.getEnumAttributeValues().get(2).setValue("male");

        IEnumValue female = enumType.newEnumValue();
        female.getEnumAttributeValues().get(0).setValue("FEMALE");
        female.getEnumAttributeValues().get(1).setValue("2");
        female.getEnumAttributeValues().get(2).setValue("female");

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
        IDependency dependency = new DatatypeDependency(type.getQualifiedNameType(), enumType.getQualifiedNameType()
                .getName());
        assertTrue(nameTypeList.contains(dependency));
        assertSingleDependencyDetail(type, dependency, aAttr, IAttribute.PROPERTY_DATATYPE);
    }

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
        ((IpsModel)ipsProject.getIpsModel())
                .setIpsArtefactBuilderSetInfos(new IIpsArtefactBuilderSetInfo[] { new TestArtefactBuilderSetInfo(
                        builderSet) });

        List<IDependency> dependsOn = Arrays.asList(a.dependsOn());
        IDependency dependency = IpsObjectDependency.createCompostionMasterDetailDependency(a.getQualifiedNameType(),
                d1.getQualifiedNameType());
        assertTrue(dependsOn.contains(dependency));
        assertSingleDependencyDetail(a, dependency, aToD1, IAssociation.PROPERTY_TARGET);

        dependsOn = Arrays.asList(d1.dependsOn());
        dependency = IpsObjectDependency.createCompostionMasterDetailDependency(d1.getQualifiedNameType(), d2
                .getQualifiedNameType());
        assertTrue(dependsOn.contains(dependency));
        assertSingleDependencyDetail(d1, dependency, d1ToD2, IAssociation.PROPERTY_TARGET);

        dependsOn = Arrays.asList(d2.dependsOn());
        dependency = IpsObjectDependency.createSubtypeDependency(d2.getQualifiedNameType(), s2.getQualifiedNameType());
        assertTrue(dependsOn.contains(dependency));
        assertSingleDependencyDetail(d2, dependency, d2, IPolicyCmptType.PROPERTY_SUPERTYPE);
    }

    public void testGetIpsObjectType() {
        assertEquals(IpsObjectType.POLICY_CMPT_TYPE, policyCmptType.getIpsObjectType());
    }

    public void testInitFromXml() {
        Element element = getTestDocument().getDocumentElement();
        policyCmptType.setConfigurableByProductCmptType(false);
        policyCmptType.initFromXml(element);
        assertTrue(policyCmptType.isConfigurableByProductCmptType());
        assertEquals("Product", policyCmptType.getProductCmptType());
        assertEquals("SuperType", policyCmptType.getSupertype());
        assertTrue(policyCmptType.isAbstract());
        assertEquals("blabla", policyCmptType.getDescriptionText(Locale.US));

        IPolicyCmptTypeAttribute[] a = policyCmptType.getPolicyCmptTypeAttributes();
        assertEquals(1, a.length);

        IMethod[] m = policyCmptType.getMethods();
        assertEquals(1, m.length);

        IValidationRule[] rules = policyCmptType.getRules();
        assertEquals(1, rules.length);

        IPolicyCmptTypeAssociation[] r = policyCmptType.getPolicyCmptTypeAssociations();
        assertEquals(1, r.length);

        policyCmptType.initFromXml(element);
        assertEquals(1, policyCmptType.getNumOfAttributes());
        assertEquals(1, policyCmptType.getNumOfMethods());
        assertEquals(1, policyCmptType.getNumOfAssociations());
        assertEquals(1, policyCmptType.getNumOfRules());

        // test if the object references have remained the same
        assertSame(a[0], policyCmptType.getPolicyCmptTypeAttributes()[0]);
        assertSame(r[0], policyCmptType.getPolicyCmptTypeAssociations()[0]);
        assertSame(m[0], policyCmptType.getMethods()[0]);
        assertSame(rules[0], policyCmptType.getRules()[0]);
    }

    public void testToXml() throws CoreException {
        policyCmptType.setConfigurableByProductCmptType(true);
        policyCmptType.setProductCmptType("Product");
        IDescription description = policyCmptType.getDescription(Locale.US);
        description.setText("blabla");
        policyCmptType.setAbstract(true);
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
        assertEquals("blabla", copy.getDescriptionText(Locale.US));
        IPolicyCmptTypeAttribute[] attributes = copy.getPolicyCmptTypeAttributes();
        assertEquals(2, attributes.length);
        assertEquals("a1", attributes[0].getName());
        assertEquals("a2", attributes[1].getName());
        IMethod[] methods = copy.getMethods();
        assertEquals("m1", methods[0].getName());
        assertEquals("m2", methods[1].getName());
        IValidationRule[] rules = copy.getRules();
        assertEquals("rule1", rules[0].getName());
        assertEquals("rule2", rules[1].getName());
        IPolicyCmptTypeAssociation[] relations = copy.getPolicyCmptTypeAssociations();
        assertEquals("t1", relations[0].getTarget());
        assertEquals("t2", relations[1].getTarget());
    }

    public void testGetSupertypeHierarchy() throws CoreException {
        ITypeHierarchy hierarchy = policyCmptType.getSupertypeHierarchy();
        assertNotNull(hierarchy);
    }

    public void testGetSubtypeHierarchy() throws CoreException {
        ITypeHierarchy hierarchy = policyCmptType.getSubtypeHierarchy();
        assertNotNull(hierarchy);
    }

    public void testSetProductCmptType() {
        super.testPropertyAccessReadWrite(IPolicyCmptType.class, IPolicyCmptType.PROPERTY_PRODUCT_CMPT_TYPE,
                policyCmptType, "NewProduct");
    }

    @Override
    public void contentsChanged(ContentChangeEvent event) {
        lastEvent = event;
    }

    public void testNewPart() {
        assertTrue(policyCmptType.newPart(PolicyCmptTypeAttribute.class) instanceof IPolicyCmptTypeAttribute);
        assertTrue(policyCmptType.newPart(Method.class) instanceof IMethod);
        assertTrue(policyCmptType.newPart(PolicyCmptTypeAssociation.class) instanceof IPolicyCmptTypeAssociation);
        assertTrue(policyCmptType.newPart(ValidationRule.class) instanceof IValidationRule);
    }

    public void testIsAggregateRoot() throws CoreException {
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
        subtype.getIpsSrcFile().save(true, null);

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

    public void testValidate_ProductCmptTypeNameMissing() throws Exception {
        MessageList ml = policyCmptType.validate(ipsProject);
        assertNull(ml.getMessageByCode(IPolicyCmptType.MSGCODE_PRODUCT_CMPT_TYPE_NAME_MISSING));
        policyCmptType.setConfigurableByProductCmptType(true);
        policyCmptType.setProductCmptType("");
        ml = policyCmptType.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IPolicyCmptType.MSGCODE_PRODUCT_CMPT_TYPE_NAME_MISSING));
    }

    public void testSupertypeNotProductRelevantIfTheTypeIsProductRelevant() throws Exception {
        IPolicyCmptType superPcType = newPolicyCmptType(ipsProject, "Super");
        policyCmptType.setSupertype(superPcType.getQualifiedName());

        superPcType.setConfigurableByProductCmptType(false);
        policyCmptType.setConfigurableByProductCmptType(true);

        MessageList ml = superPcType.validate(superPcType.getIpsProject());
        assertNull(ml
                .getMessageByCode(IPolicyCmptType.MSGCODE_SUPERTYPE_NOT_PRODUCT_RELEVANT_IF_THE_TYPE_IS_PRODUCT_RELEVANT));

        ml = policyCmptType.validate(ipsProject);
        assertNotNull(ml
                .getMessageByCode(IPolicyCmptType.MSGCODE_SUPERTYPE_NOT_PRODUCT_RELEVANT_IF_THE_TYPE_IS_PRODUCT_RELEVANT));

        superPcType.setConfigurableByProductCmptType(true);
        ml = policyCmptType.validate(ipsProject);
        assertNull(ml
                .getMessageByCode(IPolicyCmptType.MSGCODE_SUPERTYPE_NOT_PRODUCT_RELEVANT_IF_THE_TYPE_IS_PRODUCT_RELEVANT));
    }

    public void testValidateOtherTypeWithSameNameTypeInIpsObjectPath() throws CoreException {
        IIpsProject a = newIpsProject("aProject");
        IProductCmptType aProductTypeProjectA = newProductCmptType(a, "faktorzehn.example.APolicy");
        IIpsProject b = newIpsProject("bProject");
        IPolicyCmptType aPolicyProjectB = newPolicyCmptTypeWithoutProductCmptType(b, "faktorzehn.example.APolicy");

        IIpsObjectPath bPath = b.getIpsObjectPath();
        IIpsObjectPathEntry[] bPathEntries = bPath.getEntries();
        ArrayList<IIpsObjectPathEntry> newbPathEntries = new ArrayList<IIpsObjectPathEntry>();
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
        assertNotNull(msgList.getMessageByCode(IValidationRule.MSGCODE_VALIDATION_RULE_METHOD_NAME_COLLISION));

        method.newParameter(Datatype.STRING.getQualifiedName(), "aParam");
        msgList = policyCmptType.validate(ipsProject);
        assertNull(msgList.getMessageByCode(IValidationRule.MSGCODE_VALIDATION_RULE_METHOD_NAME_COLLISION));
    }

    public void testPersistenceSupport() throws CoreException {
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

    private class AggregateRootBuilderSet extends EmptyBuilderSet {

        public final static String ID = "AggregateRootBuilderSet";

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
