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
        IpsRenameProcessor ipsRenameProcessor = switch (ipsElement) {
            case IEnumLiteralNameAttributeValue v -> new RenameEnumLiteralNameAttributeValueProcessor(v);
            case IEnumAttribute a -> new RenameEnumAttributeProcessor(a);
            case IAttribute a -> new RenameAttributeProcessor(a);
            case IAssociation a -> new RenameAssociationProcessor(a);
            case IIpsObject o -> new RenameIpsObjectProcessor(o);
            case IIpsPackageFragment f -> new RenameIpsPackageFragmentProcessor(f);
            case IValidationRule r -> new RenameValidationRuleProcessor(r);
            default -> null;
        };
        return ipsRenameProcessor == null ? null : new IpsProcessorBasedRefactoring(ipsRenameProcessor);
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
        IpsMoveProcessor ipsMoveProcessor = switch (ipsElement) {
            case IIpsObject o -> new MoveIpsObjectProcessor(o);
            case IIpsPackageFragment f -> new MoveIpsPackageFragmentProcessor(f);
            default -> null;
        };
        return ipsMoveProcessor == null ? null : new IpsProcessorBasedRefactoring(ipsMoveProcessor);
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
        IpsPullUpProcessor ipsPullUpProcessor = switch (ipsObjectPart) {
            case IAttribute a -> new PullUpAttributeProcessor(a);
            case IEnumAttribute ea -> new PullUpEnumAttributeProcessor(ea);
            default -> null;
        };
        return ipsPullUpProcessor == null ? null : new IpsProcessorBasedRefactoring(ipsPullUpProcessor);
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
