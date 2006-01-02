package org.faktorips.devtools.core.internal.model.pctype;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.faktorips.devtools.core.PluginTest;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.IIpsPackageFragment;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IRelation;

/**
 * 
 * @author Jan Ortmann
 */
public class RelationPluginTest extends PluginTest {

    private IIpsPackageFragmentRoot root;
    private IIpsPackageFragment pack;
    private IIpsSrcFile sourceFile;
    private PolicyCmptType pcType;
    private Relation relation;
    
    protected void setUp() throws Exception {
        super.setUp();
        IIpsProject pdProject = this.newIpsProject("TestProject");
        root = pdProject.getIpsPackageFragmentRoots()[0];
        pack = root.createPackageFragment("products.folder", true, null);
        sourceFile = pack.createIpsFile(IpsObjectType.POLICY_CMPT_TYPE, "TestPolicy", true, null);
        pcType = (PolicyCmptType)sourceFile.getIpsObject();
        relation = (Relation)pcType.newRelation();
    }
    
    public void testGetJavaField() throws CoreException {
        IIpsSrcFile file = pack.createIpsFile(IpsObjectType.POLICY_CMPT_TYPE, "Target", true, null);
        IPolicyCmptType target = (IPolicyCmptType)file.getIpsObject();
        relation.setTarget(target.getQualifiedName());
        relation.setTargetRoleSingular("TargetRole");
        relation.setTargetRolePlural("TargetRoles");
        relation.setMaxCardinality("1");
        IType pcTypeImpl = pcType.getJavaType(IPolicyCmptType.JAVA_POLICY_CMPT_IMPLEMENTATION_TYPE);
        IType productCmptImpl = pcType.getJavaType(IPolicyCmptType.JAVA_PRODUCT_CMPT_IMPLEMENTATION_TYPE);
        
        // getter method for policy component, 1 to 1 cardinality
        IField field = relation.getJavaField(IRelation.JAVA_PCTYPE_FIELD);
        assertEquals("targetRole", field.getElementName());
        assertEquals(pcTypeImpl, field.getParent());
        
        // getter method for product component, 1 to 1 cardinality
        field = relation.getJavaField(IRelation.JAVA_PRODUCTCMPT_FIELD);
        assertEquals("targetRolePk", field.getElementName());
        assertEquals(productCmptImpl, field.getParent());
        
        // getter method for policy component, 1 to many cardinality
        relation.setMaxCardinality("2");
        field = relation.getJavaField(IRelation.JAVA_PCTYPE_FIELD);
        assertEquals("targetRoles", field.getElementName());
        assertEquals(pcTypeImpl, field.getParent());

        // getter method for product component, 1 to many cardinality
        field = relation.getJavaField(IRelation.JAVA_PRODUCTCMPT_FIELD);
        assertEquals("targetRolesPks", field.getElementName());
        assertEquals(productCmptImpl, field.getParent());
    }
    
    public void testGetJavaMethod() throws CoreException {
        IIpsSrcFile file = pack.createIpsFile(IpsObjectType.POLICY_CMPT_TYPE, "Target", true, null);
        IPolicyCmptType target = (IPolicyCmptType)file.getIpsObject();
        relation.setTarget(target.getQualifiedName());
        relation.setTargetRoleSingular("TargetRole");
        relation.setTargetRolePlural("TargetRoles");
        relation.setMaxCardinality("1");
        IType pcTypeImpl = pcType.getJavaType(IPolicyCmptType.JAVA_POLICY_CMPT_IMPLEMENTATION_TYPE);
        IType pcTypeInterface = pcType.getJavaType(IPolicyCmptType.JAVA_POLICY_CMPT_PUBLISHED_INTERFACE_TYPE);
        IType productCmptImpl = pcType.getJavaType(IPolicyCmptType.JAVA_PRODUCT_CMPT_IMPLEMENTATION_TYPE);
        IType productCmptInterface = pcType.getJavaType(IPolicyCmptType.JAVA_PRODUCT_CMPT_PUBLISHED_INTERFACE_TYPE);
        
        // getter method
        IMethod method = relation.getJavaMethod(IRelation.JAVA_PCTYPE_GETTER_METHOD_IMPLEMENATION);
        assertEquals("getTargetRole", method.getElementName());
        assertEquals(pcTypeImpl, method.getParent());
        assertEquals(0, method.getNumberOfParameters());
        method = relation.getJavaMethod(IRelation.JAVA_PCTYPE_GETTER_METHOD_INTERFACE);
        assertEquals("getTargetRole", method.getElementName());
        assertEquals(pcTypeInterface, method.getParent());
        assertEquals(0, method.getNumberOfParameters());
        method = relation.getJavaMethod(IRelation.JAVA_PRODUCTCMPT_GETTER_METHOD_IMPLEMENTATION);
        assertEquals("getTargetRolePk", method.getElementName());
        assertEquals(productCmptImpl, method.getParent());
        assertEquals(0, method.getNumberOfParameters());
        method = relation.getJavaMethod(IRelation.JAVA_PRODUCTCMPT_GETTER_METHOD_INTERFACE);
        assertEquals("getTargetRolePk", method.getElementName());
        assertEquals(productCmptInterface, method.getParent());
        assertEquals(0, method.getNumberOfParameters());
        
        // if 1 to many relation, getter has a String parameter
        relation.setMaxCardinality("*");
        method = relation.getJavaMethod(IRelation.JAVA_PRODUCTCMPT_GETTER_METHOD_IMPLEMENTATION);
        assertEquals(1, method.getNumberOfParameters());
        method = relation.getJavaMethod(IRelation.JAVA_PRODUCTCMPT_GETTER_METHOD_INTERFACE);
        assertEquals(1, method.getNumberOfParameters());
        
        
        // setter method
        method = relation.getJavaMethod(IRelation.JAVA_PCTYPE_SETTER_METHOD_IMPLEMENATION);
        assertEquals("setTargetRole", method.getElementName());
        assertEquals(pcTypeImpl, method.getParent());
        method = relation.getJavaMethod(IRelation.JAVA_PCTYPE_SETTER_METHOD_INTERFACE);
        assertEquals("setTargetRole", method.getElementName());
        assertEquals(pcTypeInterface, method.getParent());
        
        // add method
        method = relation.getJavaMethod(IRelation.JAVA_PCTYPE_ADD_METHOD_IMPLEMENATION);
        assertEquals("addTargetRole", method.getElementName());
        assertEquals(pcTypeImpl, method.getParent());
        method = relation.getJavaMethod(IRelation.JAVA_PCTYPE_ADD_METHOD_INTERFACE);
        assertEquals("addTargetRole", method.getElementName());
        assertEquals(pcTypeInterface, method.getParent());

        // remove method
        method = relation.getJavaMethod(IRelation.JAVA_PCTYPE_REMOVE_METHOD_IMPLEMENATION);
        assertEquals("removeTargetRole", method.getElementName());
        assertEquals(pcTypeImpl, method.getParent());
        method = relation.getJavaMethod(IRelation.JAVA_PCTYPE_REMOVE_METHOD_INTERFACE);
        assertEquals("removeTargetRole", method.getElementName());
        assertEquals(pcTypeInterface, method.getParent());

        // getAll method
        method = relation.getJavaMethod(IRelation.JAVA_PCTYPE_GETALL_METHOD_IMPLEMENATION);
        assertEquals("getTargetRoles", method.getElementName());
        assertEquals(pcTypeImpl, method.getParent());
        method = relation.getJavaMethod(IRelation.JAVA_PCTYPE_GETALL_METHOD_INTERFACE);
        assertEquals("getTargetRoles", method.getElementName());
        assertEquals(pcTypeInterface, method.getParent());
        method = relation.getJavaMethod(IRelation.JAVA_PRODUCTCMPT_GETALL_METHOD_IMPLEMENATION);
        assertEquals("getTargetRolesPk", method.getElementName());
        assertEquals(productCmptImpl, method.getParent());
        method = relation.getJavaMethod(IRelation.JAVA_PRODUCTCMPT_GETALL_METHOD_INTERFACE);
        assertEquals("getTargetRolesPk", method.getElementName());
        assertEquals(productCmptInterface, method.getParent());

        // getNumOf method
        method = relation.getJavaMethod(IRelation.JAVA_PCTYPE_NUMOF_METHOD_IMPLEMENATION);
        assertEquals("getAnzahlTargetRoles", method.getElementName());
        assertEquals(pcTypeImpl, method.getParent());
        method = relation.getJavaMethod(IRelation.JAVA_PCTYPE_NUMOF_METHOD_INTERFACE);
        assertEquals("getAnzahlTargetRoles", method.getElementName());
        assertEquals(pcTypeInterface, method.getParent());
        method = relation.getJavaMethod(IRelation.JAVA_PRODUCTCMPT_NUMOF_METHOD_IMPLEMENATION);
        assertEquals("getAnzahlTargetRolesPk", method.getElementName());
        assertEquals(productCmptImpl, method.getParent());
        method = relation.getJavaMethod(IRelation.JAVA_PRODUCTCMPT_NUMOF_METHOD_INTERFACE);
        assertEquals("getAnzahlTargetRolesPk", method.getElementName());
        assertEquals(productCmptInterface, method.getParent());
    }

    public void findReverseRelation() throws CoreException {
        relation.setReverseRelation("");
        assertNull(relation.findReverseRelation());

        relation.setReverseRelation("reverseRelation");
        assertNull(relation.findReverseRelation());
        
        IPolicyCmptType refType = (IPolicyCmptType)newIpsObject(root, IpsObjectType.POLICY_CMPT_TYPE, "pack2.MotorPolicy");
        relation.setTarget(refType.getQualifiedName());
        assertNull(relation.findReverseRelation());
        
        IRelation relation2 = refType.newRelation();
        relation2.setTargetRoleSingular("reverseRelation");
        assertEquals(relation2, relation.findReverseRelation());
    }
    
}
