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

package org.faktorips.devtools.core.refactor;

import java.util.Set;

import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;

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
    public IIpsProcessorBasedRefactoring createRenameRefactoring(IIpsElement ipsElement,
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
    public IIpsProcessorBasedRefactoring createRenameRefactoring(IIpsElement ipsElement);

    /**
     * Creates a fully configured Faktor-IPS "Move" refactoring capable of moving the given
     * {@link IIpsObject}.
     * 
     * @param ipsObject The {@link IIpsObject} to move
     * @param targetIpsPackageFragment The target {@link IIpsPackageFragment} to move the
     *            {@link IIpsObject} to
     */
    public IIpsProcessorBasedRefactoring createMoveRefactoring(IIpsObject ipsObject,
            IIpsPackageFragment targetIpsPackageFragment);

    /**
     * Creates a Faktor-IPS "Move" refactoring that is capable of moving the given
     * {@link IIpsObject}.
     * 
     * @param ipsObject The {@link IIpsObject} to move
     */
    public IIpsProcessorBasedRefactoring createMoveRefactoring(IIpsObject ipsObject);

    /**
     * Creates a fully configured composite Faktor-IPS refactoring that is capable of batch-moving
     * all given {@link IIpsObject}s.
     * 
     * @param ipsObjects The {@link IIpsObject}s to move
     * @param targetIpsPackageFragment The target {@link IIpsPackageFragment} to move the
     *            {@link IIpsElement}s to
     */
    public IIpsCompositeMoveRefactoring createCompositeMoveRefactoring(Set<IIpsObject> ipsObjects,
            IIpsPackageFragment targetIpsPackageFragment);

    /**
     * Creates a composite Faktor-IPS refactoring that is capable of batch-moving all given
     * {@link IIpsObject}s.
     * 
     * @param ipsObjects The {@link IIpsObject}s to move
     */
    public IIpsCompositeMoveRefactoring createCompositeMoveRefactoring(Set<IIpsObject> ipsObjects);

}
