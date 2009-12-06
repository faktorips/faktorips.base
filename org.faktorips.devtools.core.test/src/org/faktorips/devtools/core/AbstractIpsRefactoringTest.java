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

package org.faktorips.devtools.core;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ltk.core.refactoring.CheckConditionsOperation;
import org.eclipse.ltk.core.refactoring.PerformRefactoringOperation;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.ProcessorBasedRefactoring;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.refactor.IIpsRefactorings;
import org.faktorips.devtools.core.refactor.IpsRefactoringContribution;
import org.faktorips.devtools.core.refactor.RenameIpsElementDescriptor;
import org.faktorips.devtools.core.refactor.RenameRefactoringProcessor;
import org.faktorips.util.ArgumentCheck;

/**
 * Provides convenient methods to start Faktor-IPS refactorings.
 * 
 * @author Alexander Weickmann
 */
public abstract class AbstractIpsRefactoringTest extends AbstractIpsPluginTest {

    /**
     * Starts the "Rename Policy Component Type Attribute" - refactoring.
     * 
     * @param policyCmptTypeAttribute The <tt>IPolicyCmptTypeAttribute</tt> to be renamed.
     * @param newAttributeName The new name for the <tt>IPolicyCmptTypeAttribute</tt>.
     * 
     * @throws CoreException If an error occurs while creating or running the refactoring.
     * @throws NullPointerException If any parameter is <tt>null</tt>.
     */
    protected final void renamePolicyCmptTypeAttribute(IPolicyCmptTypeAttribute policyCmptTypeAttribute,
            String newAttributeName) throws CoreException {

        ArgumentCheck.notNull(new Object[] { policyCmptTypeAttribute, newAttributeName });

        RenameIpsElementDescriptor renameDescriptor = createRenameIpsElementDescriptor(IIpsRefactorings.RENAME_POLICY_CMPT_TYPE_ATTRIBUTE);
        renameDescriptor.setProject(policyCmptTypeAttribute.getIpsProject().getName());
        renameDescriptor.setTypeArgument(policyCmptTypeAttribute.getPolicyCmptType());
        renameDescriptor.setPartArgument(policyCmptTypeAttribute);

        createAndRunRenameRefactoring(renameDescriptor, newAttributeName);
    }

    /**
     * Starts the "Rename Product Component Type Attribute" - refactoring.
     * 
     * @param productCmptTypeAttribute The <tt>IProductCmptTypeAttribute</tt> to be renamed.
     * @param newAttributeName The new name for the <tt>IProductCmptTypeAttribute</tt>.
     * 
     * @throws CoreException If an error occurs while creating or running the refactoring.
     * @throws NullPointerException If any parameter is <tt>null</tt>.
     */
    protected final void renameProductCmptTypeAttribute(IProductCmptTypeAttribute productCmptTypeAttribute,
            String newAttributeName) throws CoreException {

        ArgumentCheck.notNull(new Object[] { productCmptTypeAttribute, newAttributeName });

        RenameIpsElementDescriptor renameDescriptor = createRenameIpsElementDescriptor(IIpsRefactorings.RENAME_PRODUCT_CMPT_TYPE_ATTRIBUTE);
        renameDescriptor.setProject(productCmptTypeAttribute.getIpsProject().getName());
        renameDescriptor.setTypeArgument(productCmptTypeAttribute.getProductCmptType());
        renameDescriptor.setPartArgument(productCmptTypeAttribute);

        createAndRunRenameRefactoring(renameDescriptor, newAttributeName);
    }

    private RenameIpsElementDescriptor createRenameIpsElementDescriptor(String contributionId) {
        IpsRefactoringContribution contribution = (IpsRefactoringContribution)RefactoringCore
                .getRefactoringContribution(contributionId);
        return (RenameIpsElementDescriptor)contribution.createDescriptor();
    }

    private void createAndRunRenameRefactoring(RenameIpsElementDescriptor renameDescriptor, String newElementName)
            throws CoreException {

        ProcessorBasedRefactoring renameRefactoring = (ProcessorBasedRefactoring)renameDescriptor
                .createRefactoring(new RefactoringStatus());

        RenameRefactoringProcessor processor = (RenameRefactoringProcessor)renameRefactoring.getProcessor();
        processor.setNewElementName(newElementName);

        PerformRefactoringOperation operation = new PerformRefactoringOperation(renameRefactoring,
                CheckConditionsOperation.ALL_CONDITIONS);
        ResourcesPlugin.getWorkspace().run(operation, new NullProgressMonitor());
    }

}
