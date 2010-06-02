/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.type;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.ipsobject.Modifier;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.core.model.pctype.AssociationType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.core.model.type.IMethod;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.faktorips.util.message.ObjectProperty;

/**
 * 
 * @author Jan Ortmann
 */
public class TypeTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;
    private IType type;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject();
        type = newProductCmptType(ipsProject, "MotorProduct");
    }

    public void testIsSubtype() throws CoreException {
        IType subtype = newProductCmptType((IProductCmptType)type, "Subtype");
        IType subsubtype = newProductCmptType((IProductCmptType)type, "SubSubtype");

        assertTrue(subtype.isSubtypeOf(type, ipsProject));
        assertTrue(subsubtype.isSubtypeOf(type, ipsProject));

        // test with circle
        type.setSupertype(subsubtype.getQualifiedName());
        assertTrue(subtype.isSubtypeOf(type, ipsProject));

        IType anotherType = newProductCmptType(ipsProject, "AnotherType");
        assertFalse(subtype.isSubtypeOf(anotherType, ipsProject));
    }

    public void testIsSubtypeDifferentProject() throws CoreException {
        IPolicyCmptType policyCmptType = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "TestPolicy");

        assertFalse(policyCmptType.isSubtypeOf(null, ipsProject));

        IPolicyCmptType supertype = newPolicyCmptType(ipsProject, "Supertype");
        assertFalse(policyCmptType.isSubtypeOf(supertype, ipsProject));
        policyCmptType.setSupertype(supertype.getQualifiedName());
        assertTrue(policyCmptType.isSubtypeOf(supertype, ipsProject));

        IPolicyCmptType supersupertype = newPolicyCmptType(ipsProject, "SuperSupertype");
        assertFalse(policyCmptType.isSubtypeOf(supersupertype, ipsProject));
        supertype.setSupertype(supersupertype.getQualifiedName());
        assertTrue(policyCmptType.isSubtypeOf(supersupertype, ipsProject));

        assertFalse(supertype.isSubtypeOf(policyCmptType, ipsProject));

        IIpsProject project2 = newIpsProject("testProjekt2");
        IIpsObjectPath ipsObjectPath = ipsProject.getIpsObjectPath();
        ipsObjectPath.newIpsProjectRefEntry(project2);
        ipsProject.setIpsObjectPath(ipsObjectPath);

        IPolicyCmptType supersupersupertype = newPolicyCmptType(project2, "SuperSuperSupertype");

        policyCmptType.setSupertype("Supertype");
        supertype.setSupertype("SuperSupertype");
        supersupertype.setSupertype("SuperSuperSupertype");

        assertTrue(policyCmptType.isSubtypeOf(supersupersupertype, ipsProject));
        // project2 doesn't see types in ipsProject
        assertFalse(policyCmptType.isSubtypeOf(supersupersupertype, project2));
    }

    public void testValidate_DuplicatePropertyName() throws CoreException {
        type = newPolicyCmptType(ipsProject, "Policy");
        IType supertype = newPolicyCmptType(ipsProject, "Supertype");
        type.setSupertype(supertype.getQualifiedName());

        IAttribute attr1 = type.newAttribute();
        attr1.setName("property");

        // attribute in same type
        IAttribute attr2 = type.newAttribute();
        attr2.setName("property");

        MessageList result = type.validate(ipsProject);
        Message msg = result.getMessageByCode(IType.MSGCODE_DUPLICATE_PROPERTY_NAME);
        assertNotNull(msg);
        ObjectProperty[] op = msg.getInvalidObjectProperties();
        List<Object> invalidObjects = new ArrayList<Object>();
        for (ObjectProperty element : op) {
            invalidObjects.add(element.getObject());
        }
        assertEquals(2, invalidObjects.size());
        assertTrue(invalidObjects.contains(attr1)); // this has once been a bug
        assertTrue(invalidObjects.contains(attr2));

        attr2.setName("attr2");
        result = type.validate(ipsProject);
        assertNull(result.getMessageByCode(IType.MSGCODE_DUPLICATE_PROPERTY_NAME));

        // attribute in supertype
        IAttribute attr3 = supertype.newAttribute();
        attr3.setName("property");
        result = type.validate(ipsProject);
        assertNotNull(result.getMessageByCode(IType.MSGCODE_DUPLICATE_PROPERTY_NAME));
        attr3.setName("attr3");
        result = type.validate(ipsProject);
        assertNull(result.getMessageByCode(IType.MSGCODE_DUPLICATE_PROPERTY_NAME));

        // association (singular) in same type
        IAssociation association1 = type.newAssociation();
        association1.setTargetRoleSingular("property");
        association1.setMaxCardinality(1);
        result = type.validate(ipsProject);
        assertNotNull(result.getMessageByCode(IType.MSGCODE_DUPLICATE_PROPERTY_NAME));
        association1.setTargetRoleSingular("role1");
        result = type.validate(ipsProject);
        assertNull(result.getMessageByCode(IType.MSGCODE_DUPLICATE_PROPERTY_NAME));

        // association (plural) in same type
        association1.setTargetRolePlural("property");
        association1.setMaxCardinality(10);
        result = type.validate(ipsProject);
        assertNotNull(result.getMessageByCode(IType.MSGCODE_DUPLICATE_PROPERTY_NAME));
        association1.setTargetRolePlural("rolePlural");
        result = type.validate(ipsProject);
        assertNull(result.getMessageByCode(IType.MSGCODE_DUPLICATE_PROPERTY_NAME));

        // association (singular) in supertype
        IAssociation association2 = supertype.newAssociation();
        association2.setTargetRoleSingular("property");
        association2.setMaxCardinality(1);
        result = type.validate(ipsProject);
        assertNotNull(result.getMessageByCode(IType.MSGCODE_DUPLICATE_PROPERTY_NAME));
        association2.setTargetRoleSingular("role2");
        result = type.validate(ipsProject);
        assertNull(result.getMessageByCode(IType.MSGCODE_DUPLICATE_PROPERTY_NAME));

        // association (plural) in supertype
        association1.setTargetRolePlural("property");
        association1.setMaxCardinality(10);
        result = type.validate(ipsProject);
        assertNotNull(result.getMessageByCode(IType.MSGCODE_DUPLICATE_PROPERTY_NAME));
        association1.setTargetRolePlural("rolePlural2");
        result = type.validate(ipsProject);
        assertNull(result.getMessageByCode(IType.MSGCODE_DUPLICATE_PROPERTY_NAME));
    }

    public void testSetAbstract() {
        testPropertyAccessReadWrite(IType.class, IType.PROPERTY_ABSTRACT, type, Boolean.valueOf(!type.isAbstract()));
    }

    public void testSetSupertype() {
        testPropertyAccessReadWrite(IType.class, IType.PROPERTY_SUPERTYPE, type, "NewSupertype");
    }

    public void testValidate_MustOverrideAbstractMethod() throws CoreException {
        // a supertype with a method and connect the type to it
        IType superType = this.newProductCmptType(ipsProject, "Supertype");
        superType.setAbstract(true);
        type.setSupertype(superType.getQualifiedName());
        IMethod superMethod = superType.newMethod();
        superMethod.setName("calc");

        // method is not abstract so no error message should be returned.
        MessageList list = type.validate(ipsProject);
        assertNull(list.getMessageByCode(IType.MSGCODE_MUST_OVERRIDE_ABSTRACT_METHOD));

        // set method to abstract, now the error should be reported
        superMethod.setAbstract(true);
        list = type.validate(ipsProject);
        assertNotNull(list.getMessageByCode(IType.MSGCODE_MUST_OVERRIDE_ABSTRACT_METHOD));

        // "implement" the method in pcType => error should no be reported anymore
        type.overrideMethods(new IMethod[] { superMethod });
        list = type.validate(ipsProject);
        assertNull(list.getMessageByCode(IType.MSGCODE_MUST_OVERRIDE_ABSTRACT_METHOD));

        // create another level in the supertype hierarchy with an abstract method on the new
        // supersupertype.
        // an error should be reported
        IType supersuperType = this.newProductCmptType(ipsProject, "Supersupertype");
        supersuperType.setAbstract(true);
        superType.setSupertype(supersuperType.getQualifiedName());
        IMethod supersuperMethod = supersuperType.newMethod();
        supersuperMethod.setName("calc2");
        supersuperMethod.setAbstract(true);
        list = type.validate(ipsProject);
        assertNotNull(list.getMessageByCode(IType.MSGCODE_MUST_OVERRIDE_ABSTRACT_METHOD));

        // "implement" the method in the supertype => error should no be reported anymore
        superType.overrideMethods(new IMethod[] { supersuperMethod });
        list = type.validate(ipsProject);
        assertNull(list.getMessageByCode(IType.MSGCODE_MUST_OVERRIDE_ABSTRACT_METHOD));
    }

    public void testValidate_MustImplementInverseDerivedUnion() throws Exception {
        IPolicyCmptType source = newPolicyCmptType(ipsProject, "SourceType");
        IPolicyCmptType target = newPolicyCmptType(ipsProject, "TargetType");

        MessageList ml = new MessageList();
        // ml = target.validate(ipsProject);
        assertNull(ml.getMessageByCode(IType.MSGCODE_MUST_SPECIFY_INVERSE_OF_DERIVED_UNION));

        PolicyCmptTypeAssociation union = (PolicyCmptTypeAssociation)source.newAssociation();
        union.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        union.setDerivedUnion(true);
        union.setTargetRoleSingular("Target");
        union.setTarget(target.getQualifiedName());

        ml = target.validate(ipsProject);
        assertNull(ml.getMessageByCode(IType.MSGCODE_MUST_SPECIFY_INVERSE_OF_DERIVED_UNION));

        PolicyCmptTypeAssociation inverseOfUnion = (PolicyCmptTypeAssociation)target.newAssociation();
        inverseOfUnion.setAssociationType(AssociationType.COMPOSITION_DETAIL_TO_MASTER);
        inverseOfUnion.setTargetRoleSingular(source.getUnqualifiedName());
        inverseOfUnion.setTarget(source.getQualifiedName());

        union.setInverseAssociation(inverseOfUnion.getName());
        inverseOfUnion.setInverseAssociation(union.getName());

        ml = target.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IType.MSGCODE_MUST_SPECIFY_INVERSE_OF_DERIVED_UNION));

        // test if the rule is not executed when disabled
        IIpsProjectProperties props = ipsProject.getProperties();
        props.setDerivedUnionIsImplementedRuleEnabled(false);
        ipsProject.setProperties(props);
        ml = target.validate(ipsProject);
        assertNull(ml.getMessageByCode(IType.MSGCODE_MUST_SPECIFY_INVERSE_OF_DERIVED_UNION));
        props.setDerivedUnionIsImplementedRuleEnabled(true);
        ipsProject.setProperties(props);

        // type is valid, if it is abstract
        target.setAbstract(true);
        ml = target.validate(ipsProject);
        assertNull(ml.getMessageByCode(IType.MSGCODE_MUST_SPECIFY_INVERSE_OF_DERIVED_UNION));
        target.setAbstract(false);

        // implement the derived union in the same type
        PolicyCmptTypeAssociation associationM2D = (PolicyCmptTypeAssociation)source.newAssociation();
        associationM2D.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        associationM2D.setDerivedUnion(false);
        associationM2D.setSubsettedDerivedUnion(union.getName());
        associationM2D.setTarget(target.getQualifiedName());
        associationM2D.setTargetRoleSingular("M2D");

        PolicyCmptTypeAssociation associationD2M = (PolicyCmptTypeAssociation)target.newAssociation();
        associationD2M.setAssociationType(AssociationType.COMPOSITION_DETAIL_TO_MASTER);
        associationD2M.setTarget(source.getQualifiedName());
        associationD2M.setTargetRoleSingular("D2M");

        associationM2D.setInverseAssociation(associationD2M.getName());
        associationD2M.setInverseAssociation(associationM2D.getName());

        ml = target.validate(ipsProject);
        assertNull(ml.getMessageByCode(IType.MSGCODE_MUST_SPECIFY_INVERSE_OF_DERIVED_UNION));
    }

    /**
     * Test if the target must be marked as abstract because there is no inverse of a subset of a
     * derived union association. The source must not be marked as abstract because it defines a
     * subset of the derived union.
     */
    public void testValidate_MustImplementInverseDerivedUnionOnlyTarget() throws Exception {
        IPolicyCmptType source = newPolicyCmptType(ipsProject, "SourceType");
        IPolicyCmptType target = newPolicyCmptType(ipsProject, "TargetType");
        IPolicyCmptType subTarget = newPolicyCmptType(ipsProject, "SubTargetType");
        subTarget.setSupertype(target.getQualifiedName());

        MessageList ml = new MessageList();
        // ml = target.validate(ipsProject);
        assertNull(ml.getMessageByCode(IType.MSGCODE_MUST_SPECIFY_INVERSE_OF_DERIVED_UNION));

        PolicyCmptTypeAssociation union = (PolicyCmptTypeAssociation)source.newAssociation();
        union.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        union.setDerivedUnion(true);
        union.setTargetRoleSingular("Target");
        union.setTargetRolePlural("Targets");
        union.setTarget(target.getQualifiedName());

        ml = target.validate(ipsProject);
        assertNull(ml.getMessageByCode(IType.MSGCODE_MUST_SPECIFY_INVERSE_OF_DERIVED_UNION));

        PolicyCmptTypeAssociation inverseOfUnion = (PolicyCmptTypeAssociation)target.newAssociation();
        inverseOfUnion.setAssociationType(AssociationType.COMPOSITION_DETAIL_TO_MASTER);
        inverseOfUnion.setTargetRoleSingular(source.getUnqualifiedName());
        inverseOfUnion.setTargetRolePlural(source.getUnqualifiedName() + "s");
        inverseOfUnion.setTarget(source.getQualifiedName());

        union.setInverseAssociation(inverseOfUnion.getName());
        inverseOfUnion.setInverseAssociation(union.getName());

        // test both source and target must be marked as abstract
        ml = target.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IType.MSGCODE_MUST_SPECIFY_INVERSE_OF_DERIVED_UNION));
        ml = source.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IType.MSGCODE_MUST_SPECIFY_DERIVED_UNION));

        // now implement a derived union from source to a subtype of target

        // create derived union implementation to subtype of target
        PolicyCmptTypeAssociation subsetOfUnion = (PolicyCmptTypeAssociation)source.newAssociation();
        subsetOfUnion.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        subsetOfUnion.setTargetRoleSingular("SubTarget");
        subsetOfUnion.setTargetRolePlural("SubTargets");
        subsetOfUnion.setTarget(subTarget.getQualifiedName());
        subsetOfUnion.setSubsettedDerivedUnion(union.getName());

        PolicyCmptTypeAssociation inverseSubsetOfUnion = (PolicyCmptTypeAssociation)subTarget.newAssociation();
        inverseSubsetOfUnion.setAssociationType(AssociationType.COMPOSITION_DETAIL_TO_MASTER);
        inverseSubsetOfUnion.setTargetRoleSingular("Source2");
        inverseSubsetOfUnion.setTarget(source.getQualifiedName());
        inverseSubsetOfUnion.setTargetRolePlural(source.getUnqualifiedName() + "2s");

        subsetOfUnion.setInverseAssociation(inverseSubsetOfUnion.getName());
        inverseSubsetOfUnion.setInverseAssociation(subsetOfUnion.getName());

        source.getIpsSrcFile().save(true, null);
        target.getIpsSrcFile().save(true, null);
        subTarget.getIpsSrcFile().save(true, null);

        ml = source.validate(ipsProject);
        assertNull(ml.getMessageByCode(IType.MSGCODE_MUST_SPECIFY_DERIVED_UNION));

        // test that in this case the super type (target) needs to be abstract, but the source not

        ml = subTarget.validate(ipsProject);
        assertNull(ml.getMessageByCode(IType.MSGCODE_MUST_SPECIFY_INVERSE_OF_DERIVED_UNION));

        ml = target.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IType.MSGCODE_MUST_SPECIFY_INVERSE_OF_DERIVED_UNION));

    }

    public void testValidate_MustImplementDerivedUnion() throws Exception {
        IPolicyCmptType target = newPolicyCmptType(ipsProject, "TargetType");

        MessageList ml = type.validate(ipsProject);
        assertNull(ml.getMessageByCode(IType.MSGCODE_MUST_SPECIFY_DERIVED_UNION));

        IAssociation union = type.newAssociation();
        union.setDerivedUnion(true);
        union.setTargetRoleSingular("Target");
        union.setTarget(target.getQualifiedName());

        ml = type.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IType.MSGCODE_MUST_SPECIFY_DERIVED_UNION));

        // test if the rule is not executed when disabled
        IIpsProjectProperties props = ipsProject.getProperties();
        props.setDerivedUnionIsImplementedRuleEnabled(false);
        ipsProject.setProperties(props);
        ml = type.validate(ipsProject);
        assertNull(ml.getMessageByCode(IType.MSGCODE_MUST_SPECIFY_DERIVED_UNION));
        props.setDerivedUnionIsImplementedRuleEnabled(true);
        ipsProject.setProperties(props);

        // type is valid, if it is abstract
        type.setAbstract(true);
        ml = type.validate(ipsProject);
        assertNull(ml.getMessageByCode(IType.MSGCODE_MUST_SPECIFY_DERIVED_UNION));
        type.setAbstract(false);

        // implement the derived union in the same type
        IAssociation association = type.newAssociation();
        association.setDerivedUnion(false);
        association.setSubsettedDerivedUnion(union.getName());
        association.setTarget(target.getQualifiedName());

        ml = type.validate(ipsProject);
        assertNull(ml.getMessageByCode(IType.MSGCODE_MUST_SPECIFY_DERIVED_UNION));

        // delete the relation, now same thing for a subtype
        association.delete();
        IType subtype = newProductCmptType(ipsProject, "Subtype");
        subtype.setSupertype(type.getQualifiedName());
        ml = subtype.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IType.MSGCODE_MUST_SPECIFY_DERIVED_UNION));

        // type is valid, if it is abstract
        subtype.setAbstract(true);
        ml = subtype.validate(ipsProject);
        assertNull(ml.getMessageByCode(IType.MSGCODE_MUST_SPECIFY_DERIVED_UNION));
        subtype.setAbstract(false);

        association = subtype.newAssociation();
        association.setDerivedUnion(false);
        association.setSubsettedDerivedUnion(union.getName());
        association.setTarget(target.getQualifiedName());

        ml = subtype.validate(ipsProject);
        assertNull(ml.getMessageByCode(IType.MSGCODE_MUST_SPECIFY_DERIVED_UNION));

        // now same thing for subtype of subtype
        association.delete();
        IType subsubtype = newProductCmptType(ipsProject, "SubSubtype");
        subsubtype.setSupertype(subtype.getQualifiedName());
        ml = subsubtype.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IType.MSGCODE_MUST_SPECIFY_DERIVED_UNION));

        association = subtype.newAssociation();
        association.setDerivedUnion(false);
        association.setSubsettedDerivedUnion(union.getName());
        association.setTarget(target.getQualifiedName());

        ml = subtype.validate(ipsProject);
        assertNull(ml.getMessageByCode(IType.MSGCODE_MUST_SPECIFY_DERIVED_UNION));
    }

    public void testValidate_AbstractMissing() throws Exception {
        MessageList ml = type.validate(type.getIpsProject());
        assertNull(ml.getMessageByCode(IType.MSGCODE_ABSTRACT_MISSING));

        type.newMethod().setAbstract(true);
        type.setAbstract(false);
        ml = type.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IType.MSGCODE_ABSTRACT_MISSING));
    }

    public void testGetMethod() {
        assertNull(type.getMethod("SomeName", new String[0]));

        IMethod method1 = type.newMethod();
        method1.setName("calc");

        IMethod method2 = type.newMethod();
        method2.setName("calc");
        method2.newParameter("Integer", "i");
        method2.newParameter("String", "s");

        IMethod method3 = type.newMethod();
        method3.setName("calc");
        method3.newParameter("Integer", "i");
        method3.newParameter("Decimal", "d");

        IMethod method4 = type.newMethod();
        method4.setName("getAge");

        assertEquals(method1, type.getMethod("calc", new String[0]));
        assertEquals(method2, type.getMethod("calc", new String[] { "Integer", "String" }));
        assertEquals(method3, type.getMethod("calc", new String[] { "Integer", "Decimal" }));
        assertEquals(method4, type.getMethod("getAge", new String[0]));
        assertNull(type.getMethod("unknown", new String[0]));
    }

    public void testFindMethod() throws CoreException {
        assertNull(type.findMethod("SomeName", new String[0], ipsProject));

        IType supertype = newProductCmptType(ipsProject, "Product");
        type.setSupertype(supertype.getQualifiedName());

        IMethod method1 = type.newMethod();
        method1.setName("calc");

        IMethod method2 = type.newMethod();
        method2.setName("calc");
        method2.newParameter("Integer", "i");
        method2.newParameter("String", "s");

        IMethod method3 = supertype.newMethod();
        method3.setName("calc");
        method3.newParameter("Integer", "i");
        method3.newParameter("String", "d");

        IMethod method4 = supertype.newMethod();
        method4.setName("getAge");

        assertEquals(method1, type.findMethod("calc", new String[0], ipsProject));
        assertEquals(method2, type.findMethod("calc", new String[] { "Integer", "String" }, ipsProject));
        assertEquals(method3, supertype.findMethod("calc", new String[] { "Integer", "String" }, ipsProject));
        assertEquals(method4, type.findMethod("getAge", new String[0], ipsProject));
        assertNull(type.findMethod("unknown", new String[0], ipsProject));
    }

    public void testFindAllMethods() throws Exception {
        assertTrue(type.findAllMethods(ipsProject).isEmpty());

        IType supertype = newProductCmptType(ipsProject, "Product");
        type.setSupertype(supertype.getQualifiedName());

        IMethod method1 = type.newMethod();
        method1.setName("aMethod");

        IMethod method2 = type.newMethod();
        method2.setName("aMethod");
        method2.newParameter("Integer", "i");
        method2.newParameter("String", "s");

        IMethod method3 = supertype.newMethod();
        method3.setName("aMethod");
        method3.newParameter("Integer", "i");
        method3.newParameter("String", "d");

        IMethod method4 = supertype.newMethod();
        method4.setName("bMethod");
        List<IMethod> methods = type.findAllMethods(ipsProject);
        assertEquals(3, methods.size());
        assertTrue(methods.contains(method1));
        assertTrue(methods.contains(method2));
        assertTrue(methods.contains(method4));

        methods = supertype.findAllMethods(ipsProject);
        assertEquals(2, methods.size());
        assertTrue(methods.contains(method3));
        assertTrue(methods.contains(method4));

    }

    public void testFindAttribute() throws CoreException {
        assertNull(type.findAttribute("unknown", ipsProject));

        IAttribute a1 = type.newAttribute();
        a1.setName("a1");

        IType supertype = newProductCmptType(ipsProject, "Supertype");
        type.setSupertype(supertype.getQualifiedName());

        IAttribute a2 = supertype.newAttribute();
        a2.setName("a2");

        IType superSupertype = newProductCmptType(ipsProject, "SuperSupertype");
        supertype.setSupertype(superSupertype.getQualifiedName());
        IAttribute a3 = superSupertype.newAttribute();
        a3.setName("a3");

        assertSame(a1, type.findAttribute("a1", ipsProject));
        assertSame(a2, type.findAttribute("a2", ipsProject));
        assertSame(a3, type.findAttribute("a3", ipsProject));

        IAttribute a1b = supertype.newAttribute();
        a1b.setName("a1b");
        assertSame(a1, type.findAttribute("a1", ipsProject));

        assertNull(type.findAttribute("unknown", ipsProject));
    }

    /**
     * Test find associations by target and association type
     */
    public void testFindAssociationsForTargetAndAssociationType() throws CoreException {
        IProductCmptType baseMotor = newProductCmptType(ipsProject, "BaseMotorProduct");
        IProductCmptType motor = (IProductCmptType)type;
        IProductCmptType injection = newProductCmptType(ipsProject, "InjectionProduct");

        IAssociation[] associations = motor.findAssociationsForTargetAndAssociationType(injection.getQualifiedName(),
                AssociationType.COMPOSITION_MASTER_TO_DETAIL, ipsProject, false);
        assertEquals(0, associations.length);

        // Association: motor -> injection
        IAssociation association = motor.newAssociation();
        association.setTarget(injection.getQualifiedName());
        association.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);

        // Association: baseMotor -> injection
        IAssociation associationInBase = baseMotor.newAssociation();
        associationInBase.setTarget(injection.getQualifiedName());
        associationInBase.setAssociationType(AssociationType.ASSOCIATION);

        // result = 1, because super not set
        associations = motor.findAssociationsForTargetAndAssociationType(injection.getQualifiedName(),
                AssociationType.COMPOSITION_MASTER_TO_DETAIL, ipsProject, false);
        assertEquals(1, associations.length);

        motor.setSupertype(baseMotor.getQualifiedName());

        // result = 1, because association type of super type association not equal
        associations = motor.findAssociationsForTargetAndAssociationType(injection.getQualifiedName(),
                AssociationType.COMPOSITION_MASTER_TO_DETAIL, ipsProject, false);
        assertEquals(1, associations.length);

        // result = 1 using search without supertype
        associationInBase.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        associations = motor.findAssociationsForTargetAndAssociationType(injection.getQualifiedName(),
                AssociationType.COMPOSITION_MASTER_TO_DETAIL, ipsProject, false);
        assertEquals(1, associations.length);

        // result = 1 using search with supertype included
        associationInBase.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        associations = motor.findAssociationsForTargetAndAssociationType(injection.getQualifiedName(),
                AssociationType.COMPOSITION_MASTER_TO_DETAIL, ipsProject, true);
        assertEquals(2, associations.length);
    }

    public void testFindAllAttributes() throws Exception {
        assertNull(type.findAttribute("unknown", ipsProject));

        IAttribute a1 = type.newAttribute();
        a1.setName("a1");

        IType supertype = newProductCmptType(ipsProject, "Supertype");
        type.setSupertype(supertype.getQualifiedName());

        IAttribute a2 = supertype.newAttribute();
        a2.setName("a2");

        IType superSupertype = newProductCmptType(ipsProject, "SuperSupertype");
        supertype.setSupertype(superSupertype.getQualifiedName());
        IAttribute a3 = superSupertype.newAttribute();
        a3.setName("a3");

        List<IAttribute> all = Arrays.asList(type.findAllAttributes(ipsProject));
        assertEquals(a3, all.get(0));
        assertEquals(a2, all.get(1));
        assertEquals(a1, all.get(2));

        IAttribute a1Supertype = supertype.newAttribute();
        a1Supertype.setName("a1");

        all = Arrays.asList(type.findAllAttributes(ipsProject));
        assertEquals(a3, all.get(0));
        assertEquals(a2, all.get(1));
        assertEquals(a1, all.get(2));

        for (IAttribute attribute : all) {
            if (attribute == a1Supertype) {
                fail("the attribute is expected to be overridden.");
            }
        }

    }

    public void testNewAttribute() {
        IAttribute attr = type.newAttribute();
        assertEquals(1, type.getAttributes().length);
        assertEquals(attr, type.getAttributes()[0]);
    }

    public void testGetAttribute() {
        assertNull(type.getAttribute("a"));

        IAttribute a1 = type.newAttribute();
        type.newAttribute();
        IAttribute a3 = type.newAttribute();
        a1.setName("a1");
        a3.setName("a3");

        assertEquals(a1, type.getAttribute("a1"));
        assertEquals(a3, type.getAttribute("a3"));
        assertNull(type.getAttribute("unkown"));

        assertNull(type.getAttribute(null));
    }

    public void testGetAttributes() {
        assertEquals(0, type.getAttributes().length);

        IAttribute a1 = type.newAttribute();
        IAttribute[] attributes = type.getAttributes();
        assertEquals(a1, attributes[0]);

        IAttribute a2 = type.newAttribute();
        attributes = type.getAttributes();
        assertEquals(a1, attributes[0]);
        assertEquals(a2, attributes[1]);
    }

    public void testGetNumOfAttributes() {
        assertEquals(0, type.getNumOfAttributes());

        type.newAttribute();
        assertEquals(1, type.getNumOfAttributes());

        type.newAttribute();
        assertEquals(2, type.getNumOfAttributes());
    }

    public void testMoveAttributes() {
        IAttribute a1 = type.newAttribute();
        IAttribute a2 = type.newAttribute();
        IAttribute a3 = type.newAttribute();

        type.moveAttributes(new int[] { 1, 2 }, true);
        IAttribute[] attributes = type.getAttributes();
        assertEquals(a2, attributes[0]);
        assertEquals(a3, attributes[1]);
        assertEquals(a1, attributes[2]);
    }

    public void testGetAssociation() {
        assertNull(type.getAssociation(null));
        assertNull(type.getAssociation("OtherType"));

        IAssociation ass1 = type.newAssociation();
        ass1.setTargetRoleSingular("OtherType");
        assertEquals(ass1, type.getAssociation("OtherType"));

        IAssociation ass2 = type.newAssociation();
        ass2.setTargetRoleSingular("AnotherType");
        assertEquals(ass1, type.getAssociation("OtherType"));
        assertEquals(ass2, type.getAssociation("AnotherType"));
        assertNull(type.getAssociation("UnknownRole"));
    }

    public void testGetAssociationByRoleNamePlural() {
        assertNull(type.getAssociationByRoleNamePlural(null));
        assertNull(type.getAssociationByRoleNamePlural("OtherTypes"));

        IAssociation ass1 = type.newAssociation();
        ass1.setTargetRolePlural("OtherTypes");
        assertEquals(ass1, type.getAssociationByRoleNamePlural("OtherTypes"));

        IAssociation ass2 = type.newAssociation();
        ass2.setTargetRolePlural("MoreTypes");
        assertEquals(ass1, type.getAssociationByRoleNamePlural("OtherTypes"));
        assertEquals(ass2, type.getAssociationByRoleNamePlural("MoreTypes"));
        assertNull(type.getAssociationByRoleNamePlural("UnknownRole"));
    }

    public void testFindAssociationByRoleNamePlural() throws CoreException {
        assertNull(type.findAssociationByRoleNamePlural(null, ipsProject));
        assertNull(type.findAssociationByRoleNamePlural("OtherTypes", ipsProject));

        IAssociation ass1 = type.newAssociation();
        ass1.setTargetRolePlural("OtherTypes");
        assertEquals(ass1, type.findAssociationByRoleNamePlural("OtherTypes", ipsProject));

        IAssociation ass2 = type.newAssociation();
        ass2.setTargetRolePlural("MoreTypes");
        assertEquals(ass1, type.findAssociationByRoleNamePlural("OtherTypes", ipsProject));
        assertEquals(ass2, type.findAssociationByRoleNamePlural("MoreTypes", ipsProject));
        assertNull(type.findAssociationByRoleNamePlural("UnknownRole", ipsProject));

        IType subtype = newProductCmptType(ipsProject, "Subtype");
        subtype.setSupertype(type.getQualifiedName());
        assertEquals(ass1, subtype.findAssociationByRoleNamePlural("OtherTypes", ipsProject));
        assertEquals(ass2, subtype.findAssociationByRoleNamePlural("MoreTypes", ipsProject));
        assertNull(subtype.findAssociationByRoleNamePlural("UnknownRole", ipsProject));
    }

    public void testGetAssociationsForTarget() {
        assertEquals(0, type.getAssociationsForTarget(null).length);

        IAssociation ass1 = type.newAssociation();
        ass1.setTarget("Target1");
        IAssociation ass2 = type.newAssociation();
        ass2.setTarget("Target2");
        IAssociation ass3 = type.newAssociation();
        ass3.setTarget("Target1");

        IAssociation[] ass = type.getAssociationsForTarget("Target1");
        assertEquals(2, ass.length);
        assertEquals(ass1, ass[0]);
        assertEquals(ass3, ass[1]);

        ass = type.getAssociationsForTarget("UnknownTarget");
        assertEquals(0, ass.length);
    }

    public void testGetMethods() {
        assertEquals(0, type.getMethods().length);
        IMethod m1 = type.newMethod();
        IMethod m2 = type.newMethod();
        assertSame(m1, type.getMethods()[0]);
        assertSame(m2, type.getMethods()[1]);

        // make sure a defensive copy is returned.
        type.getMethods()[0] = null;
        assertNotNull(type.getMethods()[0]);
    }

    public void testGetOverrideCandidates() throws CoreException {
        assertEquals(0, type.findOverrideMethodCandidates(false, ipsProject).length);

        // create two more types that act as supertype and supertype's supertype
        IType supertype = newProductCmptType(ipsProject, "Supertype");
        IType supersupertype = newProductCmptType(ipsProject, "Supersupertype");
        type.setSupertype(supertype.getQualifiedName());
        supertype.setSupertype(supersupertype.getQualifiedName());

        IMethod m1 = type.newMethod();
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

        IMethod[] candidates = type.findOverrideMethodCandidates(false, ipsProject);
        assertEquals(2, candidates.length);
        assertEquals(m3, candidates[0]);
        assertEquals(m5, candidates[1]);
        // notes:
        // m2 is not a candidate because it is already overridden by m1
        // m4 is not a candidate because it is overridden by m3 and m3 comes first in the hierarchy

        // only not implemented abstract methods
        candidates = type.findOverrideMethodCandidates(true, ipsProject);
        assertEquals(1, candidates.length);
        assertEquals(m5, candidates[0]);
        // note: now only m5 is a candidate as it's abstract, m3 is not.

        // override the supersupertype method m5 in the supertype
        // => now also m5 is not a candidate any more, if only not implemented abstract methods are
        // requested.
        supertype.overrideMethods(new IMethod[] { m5 });
        candidates = type.findOverrideMethodCandidates(true, ipsProject);
        assertEquals(0, candidates.length);
    }

    public void testValidate_SupertypeNotFound() throws Exception {
        MessageList ml = type.validate(ipsProject);
        assertNull(ml.getMessageByCode(IType.MSGCODE_SUPERTYPE_NOT_FOUND));

        type.setSupertype("abc");
        ml = type.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IType.MSGCODE_SUPERTYPE_NOT_FOUND));

        IType supertype = newProductCmptType(ipsProject, "Product");
        type.setSupertype(supertype.getQualifiedName());
        ml = type.validate(ipsProject);
        assertNull(ml.getMessageByCode(IType.MSGCODE_SUPERTYPE_NOT_FOUND));
    }

    public void testValidate_CycleInTypeHirarchy() throws Exception {
        // create two more types that act as supertype and supertype's supertype
        IType supertype = newProductCmptType(ipsProject, "Product");
        IType supersupertype = newProductCmptType(ipsProject, "BaseProduct");

        type.setSupertype(supertype.getQualifiedName());
        supertype.setSupertype(supersupertype.getQualifiedName());

        MessageList ml = type.validate(type.getIpsProject());
        Message msg = ml.getMessageByCode(IType.MSGCODE_CYCLE_IN_TYPE_HIERARCHY);
        assertNull(msg);

        supersupertype.setSupertype(type.getQualifiedName());

        ml = type.validate(ipsProject);
        msg = ml.getMessageByCode(IType.MSGCODE_CYCLE_IN_TYPE_HIERARCHY);

        assertNotNull(msg);
        assertEquals(1, msg.getInvalidObjectProperties().length);
        assertEquals(IPolicyCmptType.PROPERTY_SUPERTYPE, msg.getInvalidObjectProperties()[0].getProperty());
        assertEquals(type, msg.getInvalidObjectProperties()[0].getObject());

        type.setSupertype(type.getQualifiedName());
        ml = type.validate(ipsProject);
        msg = ml.getMessageByCode(IType.MSGCODE_CYCLE_IN_TYPE_HIERARCHY);
        assertNotNull(msg);
    }

    public void testValidate_InconsistentTypeHirachy() throws Exception {
        // create two more types that act as supertype and supertype's supertype
        // create two more types that act as supertype and supertype's supertype
        IType supertype = newProductCmptType(ipsProject, "Product");
        IType supersupertype = newProductCmptType(ipsProject, "BaseProduct");

        type.setSupertype(supertype.getQualifiedName());
        supertype.setSupertype(supersupertype.getQualifiedName());

        type.setSupertype(supertype.getQualifiedName());
        supertype.setSupertype(supersupertype.getQualifiedName());

        MessageList ml = type.validate(type.getIpsProject());
        assertNull(ml.getMessageByCode(IType.MSGCODE_INCONSISTENT_TYPE_HIERARCHY));

        supersupertype.setSupertype("abc");
        ml = type.validate(type.getIpsProject());
        assertNotNull(ml.getMessageByCode(IType.MSGCODE_INCONSISTENT_TYPE_HIERARCHY));
    }

    public void testFindOverrideMethodCandidates() throws CoreException {
        assertEquals(0, type.findOverrideMethodCandidates(false, ipsProject).length);

        // create two more types that act as supertype and supertype's supertype
        IType supertype = newProductCmptType(ipsProject, "Product");
        IType supersupertype = newProductCmptType(ipsProject, "BaseProduct");
        type.setSupertype(supertype.getQualifiedName());
        supertype.setSupertype(supersupertype.getQualifiedName());

        IMethod m1 = type.newMethod();
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

        IMethod[] candidates = type.findOverrideMethodCandidates(false, ipsProject);
        assertEquals(2, candidates.length);
        assertEquals(m3, candidates[0]);
        assertEquals(m5, candidates[1]);
        // notes:
        // m2 is not a candidate because it is already overridden by m1
        // m4 is not a candidate because it is the same as m3

        // only abstract methods
        candidates = type.findOverrideMethodCandidates(true, ipsProject);
        assertEquals(1, candidates.length);
        assertEquals(m5, candidates[0]);
        // note: now only m5 is a candidate as it's abstract, m2 is not.
    }

    public void testOverrideMethods() throws CoreException {
        IType supertype = newProductCmptType(ipsProject, "Product");
        type.setSupertype(supertype.getQualifiedName());
        IMethod m1 = supertype.newMethod();
        m1.setModifier(Modifier.PUBLISHED);
        m1.setAbstract(true);
        m1.setDatatype("int");
        m1.setName("m1");
        m1.newParameter("int", "p");

        IMethod m2 = supertype.newMethod();
        m1.setName("m2");

        type.overrideMethods(new IMethod[] { m1, m2 });
        assertEquals(2, type.getNumOfMethods());
        IMethod[] methods = type.getMethods();
        assertTrue(methods[0].overrides(m1));
        assertEquals("int", methods[0].getDatatype());
        assertEquals(Modifier.PUBLISHED, methods[0].getModifier());
        assertEquals("p", methods[0].getParameters()[0].getName());
        assertTrue(methods[1].overrides(m2));

        IType otherType = newProductCmptType(ipsProject, "OtherType");
        otherType.overrideMethods(new IMethod[] { m1, m2 });
        assertEquals(2, otherType.getNumOfMethods());
        methods = otherType.getMethods();
        assertFalse(methods[0].overrides(m1));
        assertFalse(methods[1].overrides(m2));
    }

    public void testFindAllAssociations() throws CoreException {
        IType superSuperType = newProductCmptType(ipsProject, "AbstractProduct");
        IType superType = newProductCmptType(ipsProject, "Product");
        type.setSupertype(superType.getQualifiedName());
        superType.setSupertype(superSuperType.getQualifiedName());
        IType targetType = newProductCmptType(ipsProject, "Target");

        IAssociation typeAssoc = type.newAssociation();
        typeAssoc.setTarget(targetType.getQualifiedName());
        typeAssoc.setTargetRoleSingular("Ziel");
        typeAssoc.setTargetRolePlural("Ziele");

        IAssociation superTypeAssoc = superType.newAssociation();
        superTypeAssoc.setTarget(targetType.getQualifiedName());
        superTypeAssoc.setTargetRoleSingular("SuperZiel");
        superTypeAssoc.setTargetRolePlural("SuperZiele");

        IAssociation superSuperTypeAssoc = superSuperType.newAssociation();
        superSuperTypeAssoc.setTarget(targetType.getQualifiedName());
        superSuperTypeAssoc.setTargetRoleSingular("SuperSuperZiel");
        superSuperTypeAssoc.setTargetRolePlural("SuperSuperZiele");

        IAssociation[] assocs = type.findAllAssociations(ipsProject);
        assertEquals(3, assocs.length);
        assertEquals("SuperSuperZiel", assocs[0].getTargetRoleSingular());
        assertEquals("SuperZiel", assocs[1].getTargetRoleSingular());
        assertEquals("Ziel", assocs[2].getTargetRoleSingular());
    }

    public void testHasSupertype() {
        assertFalse(type.hasSupertype());
        type.setSupertype("xyz");
        assertTrue(type.hasSupertype());
    }

    public void testHasExistingSupertype() throws CoreException {
        assertFalse(type.hasExistingSupertype(ipsProject));

        type.setSupertype("xyz");
        assertFalse(type.hasExistingSupertype(ipsProject));

        IType superType = newProductCmptType(ipsProject, "Product");
        type.setSupertype(superType.getQualifiedName());
        assertTrue(type.hasExistingSupertype(ipsProject));
    }

}
