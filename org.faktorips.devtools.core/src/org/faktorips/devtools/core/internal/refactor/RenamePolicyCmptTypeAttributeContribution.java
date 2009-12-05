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

package org.faktorips.devtools.core.internal.refactor;

import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.participants.RenameProcessor;
import org.eclipse.ltk.core.refactoring.participants.RenameRefactoring;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.refactor.IIpsRefactorings;
import org.faktorips.devtools.core.refactor.IpsRefactoringContribution;
import org.faktorips.devtools.core.refactor.IpsRefactoringDescriptor;
import org.faktorips.devtools.core.refactor.RenameIpsElementDescriptor;
import org.faktorips.util.ArgumentCheck;

/**
 * Contributes the "Rename Policy Component Type Attribute" - refactoring to the platform.
 * 
 * @author Alexander Weickmann
 */
public final class RenamePolicyCmptTypeAttributeContribution extends IpsRefactoringContribution {

    @SuppressWarnings("unchecked")
    // Unchecked inherited from LTK, can't do anything here
    @Override
    public RefactoringDescriptor createDescriptor(String id,
            String project,
            String description,
            String comment,
            Map arguments,
            int flags) throws IllegalArgumentException {

        return new RenameIpsElementDescriptor(id, project, description, comment, flags);
    }

    @Override
    public RefactoringDescriptor createDescriptor() {
        return new RenameIpsElementDescriptor(IIpsRefactorings.RENAME_POLICY_CMPT_TYPE_ATTRIBUTE);
    }

    @Override
    public Refactoring createRefactoring(IpsRefactoringDescriptor descriptor) throws CoreException {
        ArgumentCheck.notNull(descriptor);
        ArgumentCheck.isInstanceOf(descriptor, RenameIpsElementDescriptor.class);
        descriptor.internalInit();
        IPolicyCmptTypeAttribute policyCmptTypeAttribute = (IPolicyCmptTypeAttribute)((RenameIpsElementDescriptor)descriptor)
                .getIpsElement();
        RenameProcessor renameProcessor = new RenamePolicyCmptTypeAttributeProcessor(policyCmptTypeAttribute);
        return new RenameRefactoring(renameProcessor);
    }

}
