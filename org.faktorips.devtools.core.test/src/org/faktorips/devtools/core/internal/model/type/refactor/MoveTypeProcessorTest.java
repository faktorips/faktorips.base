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

package org.faktorips.devtools.core.internal.model.type.refactor;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.ProcessorBasedRefactoring;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.refactor.IIpsMoveProcessor;

/**
 * 
 * 
 * @author Alexander Weickmann
 */
public class MoveTypeProcessorTest extends RenameTypeMoveTypeTest {

    private static final String TARGET_PACKAGE_NAME = "level.targetipspackage";

    private IIpsPackageFragment targetIpsPackageFragment;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        IIpsPackageFragmentRoot fragmentRoot = policyCmptType.getIpsPackageFragment().getRoot();
        targetIpsPackageFragment = fragmentRoot.createPackageFragment(TARGET_PACKAGE_NAME, true, null);
    }

    public void testCheckInitialConditionsValid() throws CoreException {
        ProcessorBasedRefactoring refactoring = policyCmptType.getMoveRefactoring();
        RefactoringStatus status = refactoring.getProcessor().checkInitialConditions(new NullProgressMonitor());
        assertFalse(status.hasError());
    }

    public void testCheckInitialConditionsInvalid() throws CoreException {
        policyCmptType.setProductCmptType("abc");

        ProcessorBasedRefactoring refactoring = policyCmptType.getMoveRefactoring();
        RefactoringStatus status = refactoring.getProcessor().checkInitialConditions(new NullProgressMonitor());
        assertTrue(status.hasFatalError());
    }

    public void testCheckFinalConditionsValid() throws CoreException {
        ProcessorBasedRefactoring refactoring = policyCmptType.getMoveRefactoring();
        IIpsMoveProcessor moveProcessor = (IIpsMoveProcessor)refactoring.getProcessor();
        moveProcessor.setTargetIpsPackageFragment(targetIpsPackageFragment);
        RefactoringStatus status = refactoring.getProcessor().checkFinalConditions(new NullProgressMonitor(),
                new CheckConditionsContext());
        assertFalse(status.hasError());
    }

    public void testCheckFinalConditionsFileAlreadyExists() throws CoreException {
        ProcessorBasedRefactoring refactoring = policyCmptType.getMoveRefactoring();
        IIpsMoveProcessor moveProcessor = (IIpsMoveProcessor)refactoring.getProcessor();
        moveProcessor.setTargetIpsPackageFragment(targetIpsPackageFragment);
        newProductCmptType(ipsProject, TARGET_PACKAGE_NAME + "." + POLICY_NAME);
        RefactoringStatus status = refactoring.getProcessor().checkFinalConditions(new NullProgressMonitor(),
                new CheckConditionsContext());
        assertTrue(status.hasFatalError());
    }

    public void testMovePolicyCmptType() throws CoreException {
        performMoveRefactoring(policyCmptType, targetIpsPackageFragment);
        String movedQualifiedName = TARGET_PACKAGE_NAME + "." + POLICY_NAME;

        // Find the new policy component type.
        IIpsSrcFile ipsSrcFile = targetIpsPackageFragment.getIpsSrcFile(POLICY_NAME, policyCmptType.getIpsObjectType());
        assertTrue(ipsSrcFile.exists());
        IPolicyCmptType newPolicyCmptType = (IPolicyCmptType)ipsSrcFile.getIpsObject();
        assertEquals(targetIpsPackageFragment, newPolicyCmptType.getIpsPackageFragment());

        // Check for product component type configuration update.
        assertEquals(movedQualifiedName, productCmptType.getPolicyCmptType());

        // Check for test parameter and test attribute update.
        assertEquals(movedQualifiedName, testPolicyCmptTypeParameter.getPolicyCmptType());
        assertEquals(movedQualifiedName, testAttribute.getPolicyCmptType());
        assertEquals(movedQualifiedName, testParameterChild1.getPolicyCmptType());
        assertEquals(movedQualifiedName, testParameterChild2.getPolicyCmptType());
        assertEquals(movedQualifiedName, testParameterChild3.getPolicyCmptType());

        // Check for method parameter update.
        assertEquals(Datatype.INTEGER.getQualifiedName(), policyMethod.getParameters()[0].getDatatype());
        assertEquals(movedQualifiedName, policyMethod.getParameters()[1].getDatatype());
        assertEquals(movedQualifiedName, productMethod.getParameters()[2].getDatatype());

        // Check for association update.
        assertEquals(movedQualifiedName, otherPolicyToPolicyAssociation.getTarget());
    }

    public void testMoveSuperPolicyCmptType() throws CoreException {
        performMoveRefactoring(superPolicyCmptType, targetIpsPackageFragment);
        String movedQualifiedName = TARGET_PACKAGE_NAME + "." + SUPER_POLICY_NAME;

        // Check for test attribute update.
        assertEquals(movedQualifiedName, superTestAttribute.getPolicyCmptType());

        // Check for subtype update.
        assertEquals(movedQualifiedName, policyCmptType.getSupertype());
    }

    public void testMoveProductCmptType() throws CoreException {
        performMoveRefactoring(productCmptType, targetIpsPackageFragment);
        String movedQualifiedName = TARGET_PACKAGE_NAME + "." + PRODUCT_NAME;

        // Find the new product component type.
        IIpsSrcFile ipsSrcFile = targetIpsPackageFragment.getIpsSrcFile(PRODUCT_NAME, productCmptType
                .getIpsObjectType());
        assertTrue(ipsSrcFile.exists());
        IProductCmptType newProductCmptType = (IProductCmptType)ipsSrcFile.getIpsObject();
        assertEquals(targetIpsPackageFragment, newProductCmptType.getIpsPackageFragment());

        // Check for policy component type configuration update.
        assertEquals(movedQualifiedName, policyCmptType.getProductCmptType());

        // Check for product component reference update.
        assertEquals(movedQualifiedName, productCmpt.getProductCmptType());

        // Check for method parameter update.
        assertEquals(Datatype.INTEGER.getQualifiedName(), policyMethod.getParameters()[0].getDatatype());
        assertEquals(movedQualifiedName, productMethod.getParameters()[1].getDatatype());
        assertEquals(movedQualifiedName, policyMethod.getParameters()[2].getDatatype());

        // Check for association update.
        assertEquals(movedQualifiedName, otherProductToProductAssociation.getTarget());
    }

    public void testMoveSuperProductCmptType() throws CoreException {
        performMoveRefactoring(superProductCmptType, targetIpsPackageFragment);
        String movedQualifiedName = TARGET_PACKAGE_NAME + "." + SUPER_PRODUCT_NAME;

        // Check for subtype update.
        assertEquals(movedQualifiedName, productCmptType.getSupertype());
    }

}
