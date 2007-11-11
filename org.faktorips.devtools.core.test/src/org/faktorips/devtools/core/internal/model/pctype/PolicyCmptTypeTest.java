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

package org.faktorips.devtools.core.internal.model.pctype;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.builder.EmptyBuilderSet;
import org.faktorips.devtools.core.internal.model.IpsModel;
import org.faktorips.devtools.core.internal.model.tablecontents.Row;
import org.faktorips.devtools.core.internal.model.tablecontents.TableContents;
import org.faktorips.devtools.core.internal.model.tablecontents.TableContentsGeneration;
import org.faktorips.devtools.core.internal.model.tablestructure.Column;
import org.faktorips.devtools.core.internal.model.tablestructure.TableStructure;
import org.faktorips.devtools.core.internal.model.tablestructure.TableStructureType;
import org.faktorips.devtools.core.internal.model.tablestructure.UniqueKey;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.ContentsChangeListener;
import org.faktorips.devtools.core.model.Dependency;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsobject.Modifier;
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
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.type.IMethod;
import org.faktorips.devtools.core.util.CollectionUtil;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Element;


/**
 *
 */
public class PolicyCmptTypeTest extends AbstractIpsPluginTest implements ContentsChangeListener {
    
    private IIpsPackageFragment pack;
    private IIpsSrcFile sourceFile;
    private PolicyCmptType policyCmptType;
    private ContentChangeEvent lastEvent;
    private IIpsProject ipsProject;
    
    protected void setUp() throws Exception {
        super.setUp();
        ipsProject = this.newIpsProject();
        policyCmptType = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "TestPolicy");
        policyCmptType.setConfigurableByProductCmptType(false);
        sourceFile = policyCmptType.getIpsSrcFile();
        pack=policyCmptType.getIpsPackageFragment();
    }
    
    public void testGetChildren() {
        IPolicyCmptTypeAttribute a1 = policyCmptType.newPolicyCmptTypeAttribute();
        IMethod m1 = policyCmptType.newMethod();
        IPolicyCmptTypeAssociation r1 = policyCmptType.newPolicyCmptTypeAssociation();
        IValidationRule rule1 = policyCmptType.newRule();
        policyCmptType.setConfigurableByProductCmptType(true);
        
        IIpsElement[] elements = policyCmptType.getChildren();
        assertEquals(4, elements.length);
        assertEquals(a1, elements[0]);
        assertEquals(r1, elements[1]);
        assertEquals(m1, elements[2]);
        assertEquals(rule1, elements[3]);
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
        // => now also m5 is not a candidate any more, if only not implemented abstract methods are requested.
        supertype.overrideMethods(new IMethod[]{m5});
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
        
        IProductCmptType productCmptType = newProductCmptType(ipsProject, policyCmptType.getIpsPackageFragment().getName() + ".Product");
        policyCmptType.setProductCmptType(productCmptType.getQualifiedName());
        assertSame(productCmptType, policyCmptType.findProductCmptType(ipsProject));
    }
    
    public void testFindAttributeInSupertypeHierarchy() throws CoreException {
        assertNull(policyCmptType.findAttributeInSupertypeHierarchy("unkown"));
        IPolicyCmptTypeAttribute a1 = policyCmptType.newPolicyCmptTypeAttribute();
        a1.setName("a1");
        assertNull(policyCmptType.findAttributeInSupertypeHierarchy("unkown"));
        assertEquals(a1, policyCmptType.findAttributeInSupertypeHierarchy("a1"));
        
        IPolicyCmptType supertype = newPolicyCmptType(ipsProject, "Supertype");
        IPolicyCmptTypeAttribute a2 = supertype.newPolicyCmptTypeAttribute();
        a2.setName("a2");
        policyCmptType.setSupertype(supertype.getQualifiedName());
        
        assertNull(policyCmptType.findAttributeInSupertypeHierarchy("unkown"));
        assertEquals(a1, policyCmptType.findAttributeInSupertypeHierarchy("a1"));
        assertEquals(a2, policyCmptType.findAttributeInSupertypeHierarchy("a2"));
    }
    
    public void testIsSubtype() throws CoreException {
        assertFalse(policyCmptType.isSubtypeOf(null));
        
        IPolicyCmptType supertype = newPolicyCmptType(ipsProject, "Supertype");
        assertFalse(policyCmptType.isSubtypeOf(supertype));
        policyCmptType.setSupertype(supertype.getQualifiedName());
        assertTrue(policyCmptType.isSubtypeOf(supertype));
        
        IPolicyCmptType supersupertype = newPolicyCmptType(ipsProject, "SuperSupertype");
        assertFalse(policyCmptType.isSubtypeOf(supersupertype));
        supertype.setSupertype(supersupertype.getQualifiedName());
        assertTrue(policyCmptType.isSubtypeOf(supersupertype));
        
        assertFalse(supertype.isSubtypeOf(policyCmptType));
    }
    
    public void testIsSubtypeOrSameType() throws CoreException {
        assertFalse(policyCmptType.isSubtypeOrSameType(null));
        
        assertTrue(policyCmptType.isSubtypeOrSameType(policyCmptType));

        IPolicyCmptType supertype = newPolicyCmptType(ipsProject, "Supertype");
        assertFalse(policyCmptType.isSubtypeOrSameType(supertype));
        policyCmptType.setSupertype(supertype.getQualifiedName());
        assertTrue(policyCmptType.isSubtypeOrSameType(supertype));
        
        IPolicyCmptType supersupertype = newPolicyCmptType(ipsProject, "SuperSupertype");
        assertFalse(policyCmptType.isSubtypeOrSameType(supersupertype));
        supertype.setSupertype(supersupertype.getQualifiedName());
        assertTrue(policyCmptType.isSubtypeOrSameType(supersupertype));
        
        assertFalse(supertype.isSubtypeOf(policyCmptType));
    }

    public void testIsExtensionCompilationUnitGenerated() throws CoreException {
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
        assertEquals(0, a.getId());

        IMethod m = policyCmptType.newMethod();
        assertEquals(1, m.getId());
        IPolicyCmptTypeAttribute a2 = policyCmptType.newPolicyCmptTypeAttribute();
        assertEquals(2, a2.getId());
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
        assertEquals(0, m.getId());
        assertSame(policyCmptType, m.getIpsObject());
        assertEquals(1, policyCmptType.getNumOfMethods());
        assertTrue(sourceFile.isDirty());
        assertEquals(sourceFile, lastEvent.getIpsSrcFile());

        IMethod m2 = policyCmptType.newMethod();
        assertEquals(1, m2.getId());    
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
        assertEquals(0, r.getId());    
        assertSame(policyCmptType, r.getIpsObject());
        assertEquals(1, policyCmptType.getNumOfRules());
        assertTrue(sourceFile.isDirty());
        assertEquals(sourceFile, lastEvent.getIpsSrcFile());
        
        IValidationRule r2 = policyCmptType.newRule();
        assertEquals(1, r2.getId());    
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
        assertEquals(0, r.getId());    
        assertSame(policyCmptType, r.getIpsObject());
        assertEquals(1, policyCmptType.getNumOfAssociations());
        assertTrue(sourceFile.isDirty());
        assertEquals(sourceFile, lastEvent.getIpsSrcFile());
        
        IPolicyCmptTypeAssociation r2 = policyCmptType.newPolicyCmptTypeAssociation();
        assertEquals(1, r2.getId());    
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
    
    public void testGetRelation() {
        assertNull(policyCmptType.getRelation("unkown"));
        IPolicyCmptTypeAssociation r1 = policyCmptType.newPolicyCmptTypeAssociation();
        r1.setTargetRoleSingular("r1");
        IPolicyCmptTypeAssociation r2 = policyCmptType.newPolicyCmptTypeAssociation();
        r2.setTargetRoleSingular("r2");
        IPolicyCmptTypeAssociation r3 = policyCmptType.newPolicyCmptTypeAssociation();
        r3.setTargetRoleSingular("r2");
        assertEquals(r2, policyCmptType.getRelation("r2"));
    }
    
    public void testDependsOn() throws Exception {
        IPolicyCmptType a = newPolicyCmptType(ipsProject, "A");
        IPolicyCmptType b = newPolicyCmptType(ipsProject, "B");
        IPolicyCmptType c = newPolicyCmptType(ipsProject, "C");
        c.setSupertype(a.getQualifiedName());
        c.newPolicyCmptTypeAssociation().setTarget(b.getQualifiedName());
        List dependencyList = CollectionUtil.toArrayList(c.dependsOn());
        assertEquals(2, dependencyList.size());
        assertTrue(dependencyList.contains(Dependency.createSubtypeDependency(c.getQualifiedNameType(), a.getQualifiedNameType())));
        assertTrue(dependencyList.contains(Dependency.createReferenceDependency(c.getQualifiedNameType(), b.getQualifiedNameType())));
        
        // test if a cicle in the type hierarchy does not lead to a stack overflow exception
        c.setSupertype(c.getQualifiedName());
        dependencyList = CollectionUtil.toArrayList(c.dependsOn());
        assertEquals(2, dependencyList.size());
        assertTrue(dependencyList.contains(Dependency.createReferenceDependency(c.getQualifiedNameType(), b.getQualifiedNameType())));
        
        // this is actually not possible
        c.setSupertype(a.getQualifiedName());
        a.setSupertype(c.getQualifiedName());
        dependencyList = CollectionUtil.toArrayList(c.dependsOn());
        assertEquals(2, dependencyList.size());
        assertTrue(dependencyList.contains(Dependency.createSubtypeDependency(c.getQualifiedNameType(), a.getQualifiedNameType())));
        assertTrue(dependencyList.contains(Dependency.createReferenceDependency(c.getQualifiedNameType(), b.getQualifiedNameType())));
    }
    
    public void testDependsOnTableBasedEnums() throws Exception{
        
        TableStructure structure = (TableStructure)newIpsObject(ipsProject, IpsObjectType.TABLE_STRUCTURE, "TestEnumType");
        structure.setTableStructureType(TableStructureType.ENUMTYPE_MODEL);
        Column idColumn = (Column)structure.newColumn();
        idColumn.setDatatype(Datatype.STRING.getQualifiedName());
        idColumn.setName("id");
        Column nameColumn = (Column)structure.newColumn();
        nameColumn.setDatatype(Datatype.STRING.getQualifiedName());
        nameColumn.setName("name");
        UniqueKey uniqueKey = (UniqueKey)structure.newUniqueKey();
        uniqueKey.addKeyItem("id");
        uniqueKey.addKeyItem("name");
        
        TableContents contents = (TableContents)newIpsObject(ipsProject, IpsObjectType.TABLE_CONTENTS, "TestGender");
        contents.setTableStructure(structure.getQualifiedName());
        contents.newColumn("1");
        contents.newColumn("male");
        TableContentsGeneration generation = (TableContentsGeneration)contents.newGeneration();
        Row row = (Row)generation.newRow();
        row.setValue(0, "1");
        row.setValue(1, "male");
        row = (Row)generation.newRow();
        row.setValue(0, "2");
        row.setValue(1, "female");
        
        IPolicyCmptType a = newPolicyCmptType(ipsProject, "A");
        a.setConfigurableByProductCmptType(false);
        PolicyCmptTypeAttribute aAttr = (PolicyCmptTypeAttribute)a.newPolicyCmptTypeAttribute();
        aAttr.setAttributeType(AttributeType.CHANGEABLE);
        aAttr.setDatatype("TestGender");
        aAttr.setModifier(Modifier.PUBLIC);
        aAttr.setName("aAttr");
        
        //make sure the policy component type is valid
        assertTrue(a.validate(ipsProject).isEmpty());
        
        //make sure datatype is available
        Datatype datatype = contents.getIpsProject().findDatatype("TestGender");
        assertNotNull(datatype);

        //expect dependency on the TableContents and Tablestructure defined above
        Dependency[] dependencies = a.dependsOn();
        List nameTypeList = Arrays.asList(dependencies);
        assertTrue(nameTypeList.contains(Dependency.createReferenceDependency(a.getQualifiedNameType(), contents.getQualifiedNameType())));
        assertTrue(nameTypeList.contains(Dependency.createReferenceDependency(a.getQualifiedNameType(), structure.getQualifiedNameType())));
        
    }
    
    public void testDependsOnComposition() throws Exception {
        IPolicyCmptType a = newPolicyCmptType(ipsProject, "AggregateRoot");
        IPolicyCmptType d1 = newPolicyCmptType(ipsProject, "Detail1");
        IPolicyCmptType d2 = newPolicyCmptType(ipsProject, "Detail2");
        IPolicyCmptType s2 = newPolicyCmptType(ipsProject, "SupertypeOfDetail2");
       
        IPolicyCmptTypeAssociation rel = a.newPolicyCmptTypeAssociation();
        rel.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        rel.setTarget(d1.getQualifiedName());
        
        rel = d1.newPolicyCmptTypeAssociation();
        rel.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        rel.setTarget(d2.getQualifiedName());
        
        d2.setSupertype(s2.getQualifiedName());
        
        assertEquals(1, a.dependsOn().length);

        IIpsProjectProperties props = ipsProject.getProperties();
        props.setBuilderSetId(AggregateRootBuilderSet.ID);
        ipsProject.setProperties(props);
        ((IpsModel)ipsProject.getIpsModel()).setIpsArtefactBuilderSet(ipsProject, new AggregateRootBuilderSet());

        List dependsOn = Arrays.asList(a.dependsOn());
        assertTrue(dependsOn.contains(Dependency.createCompostionMasterDetailDependency(a.getQualifiedNameType(), d1.getQualifiedNameType())));

        dependsOn = Arrays.asList(d1.dependsOn());
        assertTrue(dependsOn.contains(Dependency.createCompostionMasterDetailDependency(d1.getQualifiedNameType(), d2.getQualifiedNameType())));
        
        dependsOn = Arrays.asList(d2.dependsOn());
        assertTrue(dependsOn.contains(Dependency.createSubtypeDependency(d2.getQualifiedNameType(), s2.getQualifiedNameType())));
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
        assertEquals("blabla", policyCmptType.getDescription());
        
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
        policyCmptType.setDescription("blabla");
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
        
        Element element = policyCmptType.toXml(this.newDocument());
        
        PolicyCmptType copy = (PolicyCmptType)newIpsObject(ipsProject, IpsObjectType.POLICY_CMPT_TYPE, "Copy");
        copy.setConfigurableByProductCmptType(false);
        copy.initFromXml(element);
        assertTrue(copy.isConfigurableByProductCmptType());
        assertEquals("Product", copy.getProductCmptType());
        assertEquals("NewSuperType", copy.getSupertype());
        assertTrue(copy.isAbstract());
        assertEquals("blabla", copy.getDescription());
        IPolicyCmptTypeAttribute[] attributes = copy.getPolicyCmptTypeAttributes();
        assertEquals(2, attributes.length);
        assertEquals("a1", attributes[0].getName());
        assertEquals("a2", attributes[1].getName());
        IMethod[] methods = copy.getMethods();
        assertEquals("m1", methods[0].getName());
        assertEquals("m2", methods[1].getName());
        IValidationRule[] rules= copy.getRules();
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
        
    public void testSetProductCmptType() throws CoreException {
        super.testPropertyAccessReadWrite(IPolicyCmptType.class, IPolicyCmptType.PROPERTY_PRODUCT_CMPT_TYPE, policyCmptType, "NewProduct");
    }
    
    /** 
     * Overridden.
     */
    public void contentsChanged(ContentChangeEvent event) {
        lastEvent = event;
    }

    public void testNewPart() {
		assertTrue(policyCmptType.newPart(IPolicyCmptTypeAttribute.class) instanceof IPolicyCmptTypeAttribute);
		assertTrue(policyCmptType.newPart(IMethod.class) instanceof IMethod);
		assertTrue(policyCmptType.newPart(IPolicyCmptTypeAssociation.class) instanceof IPolicyCmptTypeAssociation);
		assertTrue(policyCmptType.newPart(IValidationRule.class) instanceof IValidationRule);
    		
        try {
    		policyCmptType.newPart(Object.class);
			fail();
		} catch (IllegalArgumentException e) {
			//nothing to do :-)
		}
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
    
    public void testSupertypeNotProductRelevantIfTheTypeIsProductRelevant() throws Exception{
        IPolicyCmptType superPcType = newPolicyCmptType(ipsProject, "Super");
        policyCmptType.setSupertype(superPcType.getQualifiedName());
        
        superPcType.setConfigurableByProductCmptType(false);
        policyCmptType.setConfigurableByProductCmptType(true);
        
        MessageList ml = superPcType.validate(superPcType.getIpsProject());
        assertNull(ml.getMessageByCode(IPolicyCmptType.MSGCODE_SUPERTYPE_NOT_PRODUCT_RELEVANT_IF_THE_TYPE_IS_PRODUCT_RELEVANT));
        
        ml = policyCmptType.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IPolicyCmptType.MSGCODE_SUPERTYPE_NOT_PRODUCT_RELEVANT_IF_THE_TYPE_IS_PRODUCT_RELEVANT));
        
        superPcType.setConfigurableByProductCmptType(true);
        ml = policyCmptType.validate(ipsProject);
        assertNull(ml.getMessageByCode(IPolicyCmptType.MSGCODE_SUPERTYPE_NOT_PRODUCT_RELEVANT_IF_THE_TYPE_IS_PRODUCT_RELEVANT));
    }
        
    private class AggregateRootBuilderSet extends EmptyBuilderSet {

        public final static String ID = "AggregateRootBuilderSet";
        
        public boolean containsAggregateRootBuilder() {
            return true;
        }
        
        public String getId() {
            return ID;
        }
        
    }
}