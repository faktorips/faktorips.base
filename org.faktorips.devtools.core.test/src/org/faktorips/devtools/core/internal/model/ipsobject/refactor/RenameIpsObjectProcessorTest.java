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
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.refactor.IIpsRenameProcessor;

/**
 * 
 * 
 * @author Alexander Weickmann
 */
public class RenameIpsObjectProcessorTest extends MoveRenameIpsObjectTest {

    @Override
    public void testCheckInitialConditionsValid() throws CoreException {
        super.testCheckInitialConditionsValid();
    }

    @Override
    public void testCheckInitialConditionsInvalid() throws CoreException {
        super.testCheckInitialConditionsInvalid();
    }

    public void testCheckFinalConditionsValid() throws CoreException {
        ProcessorBasedRefactoring refactoring = policyCmptType.getRenameRefactoring();
        IIpsRenameProcessor renameProcessor = (IIpsRenameProcessor)refactoring.getProcessor();
        renameProcessor.setNewName("test");
        RefactoringStatus status = refactoring.getProcessor().checkFinalConditions(new NullProgressMonitor(),
                new CheckConditionsContext());
        assertFalse(status.hasError());
    }

    public void testCheckFinalConditionsFileAlreadyExists() throws CoreException {
        ProcessorBasedRefactoring refactoring = policyCmptType.getRenameRefactoring();
        IIpsRenameProcessor renameProcessor = (IIpsRenameProcessor)refactoring.getProcessor();
        renameProcessor.setNewName(PRODUCT_CMPT_TYPE_NAME);
        RefactoringStatus status = refactoring.getProcessor().checkFinalConditions(new NullProgressMonitor(),
                new CheckConditionsContext());
        assertTrue(status.hasFatalError());
    }

    public void testCheckFinalConditionsInvalidTypeName() throws CoreException {
        ProcessorBasedRefactoring refactoring = policyCmptType.getRenameRefactoring();
        IIpsRenameProcessor renameProcessor = (IIpsRenameProcessor)refactoring.getProcessor();
        renameProcessor.setNewName("$§§  $");
        RefactoringStatus status = refactoring.getProcessor().checkFinalConditions(new NullProgressMonitor(),
                new CheckConditionsContext());
        assertTrue(status.hasFatalError());
    }

    public void testRenamePolicyCmptType() throws CoreException {
        performRenamePolicyCmptType();
    }

    public void testRenameSuperPolicyCmptType() throws CoreException {
        String newElementName = "NewSuperPolicy";
        performRenameRefactoring(superPolicyCmptType, newElementName);

        checkSuperPolicyCmptTypeReferences(newElementName);
    }

    public void testRenamePolicyCmptTypeWithInverseAssociation() throws CoreException {
        IPolicyCmptTypeAssociation association = policyCmptType.newPolicyCmptTypeAssociation();
        association.setInverseAssociation(otherPolicyToPolicyAssociation.getName());
        association.setTarget(otherPolicyCmptType.getQualifiedName());
        association.setTargetRoleSingular("foo");
        association.setTargetRolePlural("foobar");
        otherPolicyToPolicyAssociation.setInverseAssociation(association.getName());

        performRenamePolicyCmptType();
    }

    private void performRenamePolicyCmptType() throws CoreException {
        String newElementName = "NewPolicy";
        performRenameRefactoring(policyCmptType, newElementName);

        checkIpsSrcFiles(POLICY_CMPT_TYPE_NAME, newElementName, policyCmptType.getIpsPackageFragment(), policyCmptType
                .getIpsPackageFragment(), IpsObjectType.POLICY_CMPT_TYPE);

        checkPolicyCmptTypeReferences(PACKAGE + "." + newElementName);
    }

    public void testRenameProductCmptType() throws CoreException {
        String newElementName = "NewProduct";
        performRenameRefactoring(productCmptType, newElementName);

        checkIpsSrcFiles(PRODUCT_CMPT_TYPE_NAME, newElementName, productCmptType.getIpsPackageFragment(),
                productCmptType.getIpsPackageFragment(), IpsObjectType.PRODUCT_CMPT_TYPE);

        checkProductCmptTypeReferences(PACKAGE + "." + newElementName);
    }

    public void testRenameSuperProductCmptType() throws CoreException {
        String newElementName = "NewSuperProduct";
        performRenameRefactoring(superProductCmptType, newElementName);

        checkSuperProductCmptTypeReferences(newElementName);
    }

    public void testRenameProductCmpt() throws CoreException {
        String newElementName = "NewProductCmptName";
        performRenameRefactoring(productCmpt, newElementName);

        checkIpsSrcFiles(PRODUCT_NAME, newElementName, productCmpt.getIpsPackageFragment(), productCmpt
                .getIpsPackageFragment(), IpsObjectType.PRODUCT_CMPT);

        checkProductCmptReferences(newElementName);
    }

    @Override
    protected ProcessorBasedRefactoring getRefactoring(IIpsElement ipsElement) {
        return ipsElement.getRenameRefactoring();
    }

}
