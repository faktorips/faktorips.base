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

package org.faktorips.devtools.core.internal.model.ipsobject.refactor;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.ProcessorBasedRefactoring;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.refactor.IIpsMoveProcessor;

/**
 * 
 * 
 * @author Alexander Weickmann
 */
public class MoveIpsObjectProcessorTest extends MoveRenameIpsObjectTest {

    private static final String TARGET_PACKAGE_NAME = "level.targetipspackage";

    private IIpsPackageFragment targetIpsPackageFragment;

    private IIpsPackageFragment originalIpsPackageFragment;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        IIpsPackageFragmentRoot fragmentRoot = policyCmptType.getIpsPackageFragment().getRoot();
        originalIpsPackageFragment = fragmentRoot.getIpsPackageFragment(PACKAGE);
        targetIpsPackageFragment = fragmentRoot.createPackageFragment(TARGET_PACKAGE_NAME, true, null);
    }

    @Override
    public void testCheckInitialConditionsValid() throws CoreException {
        super.testCheckInitialConditionsValid();
    }

    @Override
    public void testCheckInitialConditionsInvalid() throws CoreException {
        super.testCheckInitialConditionsInvalid();
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
        newProductCmptType(ipsProject, TARGET_PACKAGE_NAME + "." + POLICY_CMPT_TYPE_NAME);
        RefactoringStatus status = refactoring.getProcessor().checkFinalConditions(new NullProgressMonitor(),
                new CheckConditionsContext());
        assertTrue(status.hasFatalError());
    }

    public void testMovePolicyCmptType() throws CoreException {
        performMoveRefactoring(policyCmptType, targetIpsPackageFragment);

        checkIpsSrcFiles(POLICY_CMPT_TYPE_NAME, POLICY_CMPT_TYPE_NAME, originalIpsPackageFragment,
                targetIpsPackageFragment, IpsObjectType.POLICY_CMPT_TYPE);

        checkPolicyCmptTypeReferences(TARGET_PACKAGE_NAME + "." + POLICY_CMPT_TYPE_NAME);
    }

    public void testMoveSuperPolicyCmptType() throws CoreException {
        performMoveRefactoring(superPolicyCmptType, targetIpsPackageFragment);

        checkSuperPolicyCmptTypeReferences(TARGET_PACKAGE_NAME + "." + SUPER_POLICY_CMPT_TYPE_NAME);
    }

    public void testMoveProductCmptType() throws CoreException {
        performMoveRefactoring(productCmptType, targetIpsPackageFragment);

        checkIpsSrcFiles(PRODUCT_CMPT_TYPE_NAME, PRODUCT_CMPT_TYPE_NAME, originalIpsPackageFragment,
                targetIpsPackageFragment, IpsObjectType.PRODUCT_CMPT_TYPE);

        checkProductCmptTypeReferences(TARGET_PACKAGE_NAME + "." + PRODUCT_CMPT_TYPE_NAME);
    }

    public void testMoveSuperProductCmptType() throws CoreException {
        performMoveRefactoring(superProductCmptType, targetIpsPackageFragment);

        checkSuperProductCmptTypeReferences(TARGET_PACKAGE_NAME + "." + SUPER_PRODUCT_CMPT_TYPE_NAME);
    }

    public void testMoveProductCmpt() throws CoreException {
        performMoveRefactoring(productCmpt, targetIpsPackageFragment);

        checkIpsSrcFiles(PRODUCT_NAME, PRODUCT_NAME, originalIpsPackageFragment, targetIpsPackageFragment,
                IpsObjectType.PRODUCT_CMPT);

        checkProductCmptReferences(TARGET_PACKAGE_NAME + "." + productCmpt.getName());
    }

    @Override
    protected ProcessorBasedRefactoring getRefactoring(IIpsElement ipsElement) {
        return ipsElement.getMoveRefactoring();
    }

}
