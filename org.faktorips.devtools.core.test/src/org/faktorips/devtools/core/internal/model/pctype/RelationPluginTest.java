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

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.IpsPluginTest;
import org.faktorips.devtools.core.model.IIpsPackageFragment;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IRelation;
import org.faktorips.devtools.core.model.pctype.RelationType;

/**
 * 
 * @author Jan Ortmann
 */
public class RelationPluginTest extends IpsPluginTest {

	private IIpsProject ipsProject;
    private IIpsPackageFragmentRoot root;
    private IIpsPackageFragment pack;
    private IIpsSrcFile sourceFile;
    private PolicyCmptType pcType;
    private Relation relation;
    
    protected void setUp() throws Exception {
        super.setUp();
        ipsProject = this.newIpsProject("TestProject");
        root = ipsProject.getIpsPackageFragmentRoots()[0];
        pack = root.createPackageFragment("products.folder", true, null);
        sourceFile = pack.createIpsFile(IpsObjectType.POLICY_CMPT_TYPE, "TestPolicy", true, null);
        pcType = (PolicyCmptType)sourceFile.getIpsObject();
        relation = (Relation)pcType.newRelation();
    }
    
    public void testFindReverseRelation_ContainerCase() throws CoreException {
        
    	// create policy and coverage type with an container relation inkl reverse
    	IPolicyCmptType policyType = newPolicyCmptType(ipsProject, "Policy");
        policyType.setConfigurableByProductCmptType(false);
        policyType.setAbstract(true);
        IPolicyCmptType coverageType = newPolicyCmptType(ipsProject, "Coverage");
        coverageType.setConfigurableByProductCmptType(false);
        coverageType.setAbstract(false);
        
        IRelation policyToCoverage = policyType.newRelation();
        policyToCoverage.setRelationType(RelationType.COMPOSITION);
        policyToCoverage.setReadOnlyContainer(true);
    	
        IRelation coverageToPolicy = coverageType.newRelation();
        coverageToPolicy.setRelationType(RelationType.REVERSE_COMPOSITION);
        coverageToPolicy.setReadOnlyContainer(true);
        
        // wire the relations between policy and coverage
        policyToCoverage.setTarget(coverageType.getQualifiedName());
        policyToCoverage.setTargetRoleSingular(coverageType.getName());
        coverageToPolicy.setTarget(policyType.getQualifiedName());
        coverageToPolicy.setTargetRoleSingular(policyType.getName());
        policyToCoverage.setReverseRelation(coverageToPolicy.getName());
        coverageToPolicy.setReverseRelation(policyToCoverage.getName());
        
    	// now create motorpolicy and tplcoverage and collision coverage type that inherit from the above types
        // and create two relations implementing the container relation.
    	IPolicyCmptType motorPolicyType = newPolicyCmptType(ipsProject, "MotorPolicy");
    	motorPolicyType.setSupertype(policyType.getQualifiedName());
    	motorPolicyType.setConfigurableByProductCmptType(false);
        IPolicyCmptType tplCoverageType = newPolicyCmptType(ipsProject, "TplCoverage");
        tplCoverageType.setConfigurableByProductCmptType(false);
        tplCoverageType.setSupertype(coverageType.getQualifiedName());
        IPolicyCmptType collisionCoverageType = newPolicyCmptType(ipsProject, "CollisionCoverage");
        collisionCoverageType.setConfigurableByProductCmptType(false);
        collisionCoverageType.setSupertype(coverageType.getQualifiedName());
        
        IRelation motorPolicyToTplCoverage = motorPolicyType.newRelation();
        motorPolicyToTplCoverage.setRelationType(RelationType.COMPOSITION);
        motorPolicyToTplCoverage.setReadOnlyContainer(false);
        motorPolicyToTplCoverage.setContainerRelation(policyToCoverage.getName());
        
        IRelation tplCoverageToMotorPolicy = tplCoverageType.newRelation();
        tplCoverageToMotorPolicy.setRelationType(RelationType.REVERSE_COMPOSITION);
        tplCoverageToMotorPolicy.setReadOnlyContainer(false);
        tplCoverageToMotorPolicy.setContainerRelation(coverageToPolicy.getName());
        
        IRelation motorPolicyToCollisionCoverage = motorPolicyType.newRelation();
        motorPolicyToCollisionCoverage.setRelationType(RelationType.COMPOSITION);
        motorPolicyToCollisionCoverage.setReadOnlyContainer(false);
        motorPolicyToCollisionCoverage.setContainerRelation(policyToCoverage.getName());
        
        IRelation collisionCoverageToMotorPolicy = collisionCoverageType.newRelation();
        collisionCoverageToMotorPolicy.setRelationType(RelationType.REVERSE_COMPOSITION);
        collisionCoverageToMotorPolicy.setReadOnlyContainer(false);
        collisionCoverageToMotorPolicy.setContainerRelation(coverageToPolicy.getName());

        // set targets and rolenames, but don't wire them!!!
        motorPolicyToTplCoverage.setTarget(tplCoverageType.getQualifiedName());
        motorPolicyToTplCoverage.setTargetRoleSingular(tplCoverageType.getName());
        motorPolicyToCollisionCoverage.setTarget(collisionCoverageType.getQualifiedName());
        motorPolicyToCollisionCoverage.setTargetRoleSingular(collisionCoverageType.getName());
        tplCoverageToMotorPolicy.setTarget(motorPolicyType.getQualifiedName());
        tplCoverageToMotorPolicy.setTargetRoleSingular(motorPolicyType.getName());
        collisionCoverageToMotorPolicy.setTarget(motorPolicyType.getQualifiedName());
        collisionCoverageToMotorPolicy.setTargetRoleSingular(motorPolicyType.getName());
        
        // save all files 
        policyType.getIpsSrcFile().save(true, null);
        coverageType.getIpsSrcFile().save(true, null);
        motorPolicyType.getIpsSrcFile().save(true, null);
        tplCoverageType.getIpsSrcFile().save(true, null);
        collisionCoverageType.getIpsSrcFile().save(true, null);
        
        // now we can already do the asserts :-)
        assertEquals(tplCoverageToMotorPolicy, motorPolicyToTplCoverage.findReverseRelation());
        assertEquals(collisionCoverageToMotorPolicy, motorPolicyToCollisionCoverage.findReverseRelation());
        assertEquals(motorPolicyToTplCoverage, tplCoverageToMotorPolicy.findReverseRelation());
        assertEquals(motorPolicyToCollisionCoverage, collisionCoverageToMotorPolicy.findReverseRelation());
        
    }

    public void testFindReverseRelation() throws CoreException {
        relation.setReverseRelation("");
        assertNull(relation.findReverseRelation());

        relation.setReverseRelation("reverseRelation");
        assertNull(relation.findReverseRelation());
        
        IPolicyCmptType targetType = (IPolicyCmptType)newIpsObject(root, IpsObjectType.POLICY_CMPT_TYPE, "pack2.MotorPolicy");
        relation.setTarget(targetType.getQualifiedName());
        assertNull(relation.findReverseRelation());
        
        IRelation relation2 = targetType.newRelation();
        relation2.setTargetRoleSingular("reverseRelation");
        assertEquals(relation2, relation.findReverseRelation());
    }
    
    public void testFindForwardRelations() throws CoreException {
        relation.setRelationType(RelationType.REVERSE_COMPOSITION);
        relation.setTarget("Unknown");
        assertEquals(0, relation.findForwardCompositions().length);
        
        // 
        
        relation.setReverseRelation("reverseRelation");
        assertNull(relation.findReverseRelation());
        
        IPolicyCmptType targetType = (IPolicyCmptType)newIpsObject(root, IpsObjectType.POLICY_CMPT_TYPE, "pack2.MotorPolicy");
        relation.setTarget(targetType.getQualifiedName());
        assertNull(relation.findReverseRelation());
        
        IRelation relation2 = targetType.newRelation();
        relation2.setTargetRoleSingular("reverseRelation");
        assertEquals(relation2, relation.findReverseRelation());
    }
    
}
