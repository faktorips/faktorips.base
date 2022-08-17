/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.refactor;

import java.util.Set;

import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;

/**
 * Allows the creation of {@link IIpsRefactoring}s.
 * 
 * @author Alexander Weickmann
 */
public interface IIpsRefactoringFactory {

    /**
     * Creates a fully configured Faktor-IPS "Rename" refactoring capable of renaming the given
     * {@link IIpsElement}.
     * <p>
     * Returns null if the "Rename" refactoring is not supported for the given {@link IIpsElement}.
     * 
     * @param ipsElement The {@link IIpsElement} to rename
     * @param newName The new name for the {@link IIpsElement}
     * @param newPluralName The new plural name for the {@link IIpsElement} or null if not
     *            applicable
     * @param adaptRuntimeId Flag indicating whether the runtime id should be adapted if the
     *            {@link IIpsElement} to rename is an {@link IProductCmpt}
     */
    IIpsProcessorBasedRefactoring createRenameRefactoring(IIpsElement ipsElement,
            String newName,
            String newPluralName,
            boolean adaptRuntimeId);

    /**
     * Creates a Faktor-IPS "Rename" refactoring that is capable of renaming the given
     * {@link IIpsElement}.
     * <p>
     * Returns null if the "Rename" refactoring is not supported for the given {@link IIpsElement}.
     * 
     * @param ipsElement The {@link IIpsElement} to rename
     */
    IIpsProcessorBasedRefactoring createRenameRefactoring(IIpsElement ipsElement);

    /**
     * Creates a fully configured Faktor-IPS "Move" refactoring capable of moving the given
     * {@link IIpsElement}.
     * 
     * @param ipsElement The {@link IIpsElement} to move
     * @param targetIpsPackageFragment The target {@link IIpsPackageFragment} to move the
     *            {@link IIpsElement} to
     */
    IIpsProcessorBasedRefactoring createMoveRefactoring(IIpsElement ipsElement,
            IIpsPackageFragment targetIpsPackageFragment);

    /**
     * Creates a Faktor-IPS "Move" refactoring that is capable of moving the given
     * {@link IIpsElement}.
     * 
     * @param ipsElement The {@link IIpsElement} to move
     */
    IIpsProcessorBasedRefactoring createMoveRefactoring(IIpsElement ipsElement);

    /**
     * Creates a fully configured composite Faktor-IPS refactoring that is capable of batch-moving
     * all given {@link IIpsElement}s.
     * 
     * @param ipsElement The {@link IIpsElement}s to move
     * @param targetIpsPackageFragment The target {@link IIpsPackageFragment} to move the
     *            {@link IIpsElement}s to
     */
    IIpsCompositeMoveRefactoring createCompositeMoveRefactoring(Set<IIpsElement> ipsElement,
            IIpsPackageFragment targetIpsPackageFragment);

    /**
     * Creates a composite Faktor-IPS refactoring that is capable of batch-moving all given
     * {@link IIpsElement}s.
     * 
     * @param ipsElement The {@link IIpsElement}s to move
     */
    IIpsCompositeMoveRefactoring createCompositeMoveRefactoring(Set<IIpsElement> ipsElement);

    /**
     * Creates a Faktor-IPS "Pull Up" refactoring capable of pulling up the given
     * {@link IIpsObjectPart}.
     * <p>
     * Returns null if the "Pull Up" refactoring is not supported for the given
     * {@link IIpsObjectPart}.
     * 
     * @param ipsObjectPart The {@link IIpsObjectPart} to pull up
     */
    IIpsProcessorBasedRefactoring createPullUpRefactoring(IIpsObjectPart ipsObjectPart);

    /**
     * Creates a fully configured Faktor-IPS "Pull Up" refactoring capable of pulling up the given
     * {@link IIpsObjectPart} to the given target {@link IIpsObjectPartContainer}.
     * <p>
     * Returns null if the "Pull Up" refactoring is not supported for the given
     * {@link IIpsObjectPart}.
     * 
     * @param ipsObjectPart The {@link IIpsObjectPart} to pull up
     * @param targetIpsObjectPartContainer The target {@link IIpsObjectPartContainer} to pull up to
     */
    IIpsProcessorBasedRefactoring createPullUpRefactoring(IIpsObjectPart ipsObjectPart,
            IIpsObjectPartContainer targetIpsObjectPartContainer);

}
