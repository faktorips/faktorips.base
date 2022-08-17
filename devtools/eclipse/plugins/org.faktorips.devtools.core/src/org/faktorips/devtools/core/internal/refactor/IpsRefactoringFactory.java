/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.refactor;

import java.util.Set;

import org.faktorips.devtools.core.internal.model.enums.refactor.PullUpEnumAttributeProcessor;
import org.faktorips.devtools.core.internal.model.enums.refactor.RenameEnumAttributeProcessor;
import org.faktorips.devtools.core.internal.model.enums.refactor.RenameEnumLiteralNameAttributeValueProcessor;
import org.faktorips.devtools.core.internal.model.ipsobject.refactor.MoveIpsObjectProcessor;
import org.faktorips.devtools.core.internal.model.ipsobject.refactor.RenameIpsObjectProcessor;
import org.faktorips.devtools.core.internal.model.type.refactor.PullUpAttributeProcessor;
import org.faktorips.devtools.core.internal.model.type.refactor.RenameAssociationProcessor;
import org.faktorips.devtools.core.internal.model.type.refactor.RenameAttributeProcessor;
import org.faktorips.devtools.core.internal.model.type.refactor.RenameValidationRuleProcessor;
import org.faktorips.devtools.core.refactor.IIpsCompositeMoveRefactoring;
import org.faktorips.devtools.core.refactor.IIpsProcessorBasedRefactoring;
import org.faktorips.devtools.core.refactor.IIpsRefactoringFactory;
import org.faktorips.devtools.core.refactor.IpsMoveProcessor;
import org.faktorips.devtools.core.refactor.IpsPullUpProcessor;
import org.faktorips.devtools.core.refactor.IpsRenameProcessor;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.enums.IEnumAttribute;
import org.faktorips.devtools.model.enums.IEnumLiteralNameAttributeValue;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.pctype.IValidationRule;
import org.faktorips.devtools.model.type.IAssociation;
import org.faktorips.devtools.model.type.IAttribute;

/**
 * @author Alexander Weickmann
 */
public final class IpsRefactoringFactory implements IIpsRefactoringFactory {

    @Override
    public IIpsProcessorBasedRefactoring createRenameRefactoring(IIpsElement ipsElement,
            String newName,
            String newPluralName,
            boolean adaptRuntimeId) {

        IIpsProcessorBasedRefactoring ipsRenameRefactoring = createRenameRefactoring(ipsElement);
        IpsRenameProcessor ipsRenameProcessor = (IpsRenameProcessor)ipsRenameRefactoring.getIpsRefactoringProcessor();
        ipsRenameProcessor.setNewName(newName);
        if (newPluralName != null) {
            ipsRenameProcessor.setNewPluralName(newPluralName);
        }
        ipsRenameProcessor.setAdaptRuntimeId(adaptRuntimeId);
        return ipsRenameRefactoring;
    }

    @Override
    public IIpsProcessorBasedRefactoring createRenameRefactoring(IIpsElement ipsElement) {
        IpsRenameProcessor ipsRenameProcessor = null;
        if (ipsElement instanceof IEnumLiteralNameAttributeValue) {
            ipsRenameProcessor = new RenameEnumLiteralNameAttributeValueProcessor(
                    (IEnumLiteralNameAttributeValue)ipsElement);
        } else if (ipsElement instanceof IEnumAttribute) {
            ipsRenameProcessor = new RenameEnumAttributeProcessor((IEnumAttribute)ipsElement);
        } else if (ipsElement instanceof IAttribute) {
            ipsRenameProcessor = new RenameAttributeProcessor((IAttribute)ipsElement);
        } else if (ipsElement instanceof IAssociation) {
            ipsRenameProcessor = new RenameAssociationProcessor((IAssociation)ipsElement);
        } else if (ipsElement instanceof IIpsObject) {
            ipsRenameProcessor = new RenameIpsObjectProcessor((IIpsObject)ipsElement);
        } else if (ipsElement instanceof IIpsPackageFragment) {
            ipsRenameProcessor = new RenameIpsPackageFragmentProcessor((IIpsPackageFragment)ipsElement);
        } else if (ipsElement instanceof IValidationRule) {
            ipsRenameProcessor = new RenameValidationRuleProcessor((IValidationRule)ipsElement);
        } else {
            return null;
        }
        return new IpsProcessorBasedRefactoring(ipsRenameProcessor);
    }

    @Override
    public IIpsProcessorBasedRefactoring createMoveRefactoring(IIpsElement ipsElement,
            IIpsPackageFragment targetIpsPackageFragment) {

        IIpsProcessorBasedRefactoring ipsMoveRefactoring = createMoveRefactoring(ipsElement);
        if (targetIpsPackageFragment != null) {
            if (ipsMoveRefactoring.getIpsRefactoringProcessor() instanceof IpsMoveProcessor) {
                IpsMoveProcessor ipsMoveProcessor = (IpsMoveProcessor)ipsMoveRefactoring.getIpsRefactoringProcessor();
                ipsMoveProcessor.setTargetIpsPackageFragment(targetIpsPackageFragment);
            }
        }
        return ipsMoveRefactoring;
    }

    @Override
    public IIpsProcessorBasedRefactoring createMoveRefactoring(IIpsElement ipsElement) {
        if (ipsElement instanceof IIpsObject) {
            IpsMoveProcessor ipsMoveProcessor = new MoveIpsObjectProcessor((IIpsObject)ipsElement);
            return new IpsProcessorBasedRefactoring(ipsMoveProcessor);
        } else if (ipsElement instanceof IIpsPackageFragment) {
            IpsMoveProcessor ipsMoveProcessor = new MoveIpsPackageFragmentProcessor((IIpsPackageFragment)ipsElement);
            return new IpsProcessorBasedRefactoring(ipsMoveProcessor);
        } else {
            return null;
        }
    }

    @Override
    public IIpsCompositeMoveRefactoring createCompositeMoveRefactoring(Set<IIpsElement> ipsElements,
            IIpsPackageFragment targetIpsPackageFragment) {

        IIpsCompositeMoveRefactoring ipsCompositeMoveRefactoring = createCompositeMoveRefactoring(ipsElements);
        ipsCompositeMoveRefactoring.setTargetIpsPackageFragment(targetIpsPackageFragment);
        return ipsCompositeMoveRefactoring;
    }

    @Override
    public IIpsCompositeMoveRefactoring createCompositeMoveRefactoring(Set<IIpsElement> ipsElements) {
        return new IpsCompositeMoveRefactoring(ipsElements);
    }

    @Override
    public IIpsProcessorBasedRefactoring createPullUpRefactoring(IIpsObjectPart ipsObjectPart) {
        IpsPullUpProcessor ipsPullUpProcessor = null;
        if (ipsObjectPart instanceof IAttribute) {
            ipsPullUpProcessor = new PullUpAttributeProcessor((IAttribute)ipsObjectPart);
        } else if (ipsObjectPart instanceof IEnumAttribute) {
            ipsPullUpProcessor = new PullUpEnumAttributeProcessor((IEnumAttribute)ipsObjectPart);
        } else {
            return null;
        }
        return new IpsProcessorBasedRefactoring(ipsPullUpProcessor);
    }

    @Override
    public IIpsProcessorBasedRefactoring createPullUpRefactoring(IIpsObjectPart ipsObjectPart,
            IIpsObjectPartContainer targetIpsObjectPartContainer) {

        IIpsProcessorBasedRefactoring ipsPullUpRefactoring = createPullUpRefactoring(ipsObjectPart);
        IpsPullUpProcessor ipsPullUpProcessor = (IpsPullUpProcessor)ipsPullUpRefactoring.getIpsRefactoringProcessor();
        ipsPullUpProcessor.setTarget(targetIpsObjectPartContainer);
        return ipsPullUpRefactoring;
    }
}
