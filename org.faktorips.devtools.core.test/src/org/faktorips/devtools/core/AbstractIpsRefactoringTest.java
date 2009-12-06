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
import org.faktorips.devtools.core.refactor.IIpsRefactorings;
import org.faktorips.devtools.core.refactor.IpsRefactoringContribution;
import org.faktorips.devtools.core.refactor.RenameIpsElementDescriptor;
import org.faktorips.devtools.core.refactor.RenameRefactoringProcessor;

/**
 * Provides convenient methods to start Faktor-IPS refactorings.
 * 
 * @author Alexander Weickmann
 */
public abstract class AbstractIpsRefactoringTest extends AbstractIpsPluginTest {

    protected final void renamePolicyCmptTypeAttribute(IPolicyCmptTypeAttribute policyCmptTypeAttribute, String newName)
            throws CoreException {

        IpsRefactoringContribution contribution = (IpsRefactoringContribution)RefactoringCore
                .getRefactoringContribution(IIpsRefactorings.RENAME_POLICY_CMPT_TYPE_ATTRIBUTE);
        RenameIpsElementDescriptor renameDescriptor = (RenameIpsElementDescriptor)contribution.createDescriptor();
        renameDescriptor.setProject(policyCmptTypeAttribute.getIpsProject().getName());
        renameDescriptor.setTypeArgument(policyCmptTypeAttribute.getPolicyCmptType());
        renameDescriptor.setPartArgument(policyCmptTypeAttribute);
        ProcessorBasedRefactoring renameRefactoring = (ProcessorBasedRefactoring)renameDescriptor
                .createRefactoring(new RefactoringStatus());
        RenameRefactoringProcessor processor = (RenameRefactoringProcessor)renameRefactoring.getProcessor();
        processor.setNewElementName(newName);
        PerformRefactoringOperation operation = new PerformRefactoringOperation(renameRefactoring,
                CheckConditionsOperation.ALL_CONDITIONS);
        ResourcesPlugin.getWorkspace().run(operation, new NullProgressMonitor());
    }

}
