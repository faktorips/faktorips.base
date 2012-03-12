/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

import java.util.Set;

import org.faktorips.devtools.core.internal.model.enums.refactor.PullUpEnumAttributeProcessor;
import org.faktorips.devtools.core.internal.model.enums.refactor.RenameEnumAttributeProcessor;
import org.faktorips.devtools.core.internal.model.enums.refactor.RenameEnumLiteralNameAttributeValueProcessor;
import org.faktorips.devtools.core.internal.model.ipsobject.refactor.MoveIpsObjectProcessor;
import org.faktorips.devtools.core.internal.model.ipsobject.refactor.RenameIpsObjectProcessor;
import org.faktorips.devtools.core.internal.model.type.refactor.PullUpAttributeProcessor;
import org.faktorips.devtools.core.internal.model.type.refactor.RenameAssociationProcessor;
import org.faktorips.devtools.core.internal.model.type.refactor.RenameAttributeProcessor;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.enums.IEnumAttribute;
import org.faktorips.devtools.core.model.enums.IEnumLiteralNameAttributeValue;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.core.refactor.IIpsCompositeMoveRefactoring;
import org.faktorips.devtools.core.refactor.IIpsProcessorBasedRefactoring;
import org.faktorips.devtools.core.refactor.IIpsRefactoringFactory;
import org.faktorips.devtools.core.refactor.IpsMoveProcessor;
import org.faktorips.devtools.core.refactor.IpsPullUpProcessor;
import org.faktorips.devtools.core.refactor.IpsRenameProcessor;

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
        } else {
            return null;
        }
        return new IpsProcessorBasedRefactoring(ipsRenameProcessor);
    }

    @Override
    public IIpsProcessorBasedRefactoring createMoveRefactoring(IIpsObject ipsObject,
            IIpsPackageFragment targetIpsPackageFragment) {

        IIpsProcessorBasedRefactoring ipsMoveRefactoring = createMoveRefactoring(ipsObject);
        IpsMoveProcessor ipsMoveProcessor = (IpsMoveProcessor)ipsMoveRefactoring.getIpsRefactoringProcessor();
        ipsMoveProcessor.setTargetIpsPackageFragment(targetIpsPackageFragment);
        return ipsMoveRefactoring;
    }

    @Override
    public IIpsProcessorBasedRefactoring createMoveRefactoring(IIpsObject ipsObject) {
        IpsMoveProcessor ipsMoveProcessor = new MoveIpsObjectProcessor(ipsObject);
        return new IpsProcessorBasedRefactoring(ipsMoveProcessor);
    }

    @Override
    public IIpsCompositeMoveRefactoring createCompositeMoveRefactoring(Set<IIpsObject> ipsObjects,
            IIpsPackageFragment targetIpsPackageFragment) {

        IIpsCompositeMoveRefactoring ipsCompositeMoveRefactoring = createCompositeMoveRefactoring(ipsObjects);
        ipsCompositeMoveRefactoring.setTargetIpsPackageFragment(targetIpsPackageFragment);
        return ipsCompositeMoveRefactoring;
    }

    @Override
    public IIpsCompositeMoveRefactoring createCompositeMoveRefactoring(Set<IIpsObject> ipsObjects) {
        return new IpsCompositeMoveRefactoring(ipsObjects);
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
