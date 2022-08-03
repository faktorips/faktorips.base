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

import java.util.HashSet;
import java.util.Set;

import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;

/**
 * A {@link IpsRefactoringModificationSet} holds a set of modifications done during a model
 * refactoring and have a reference to the {@link IIpsElement} that was refactored as well as a
 * reference to the {@link IIpsElement} after the refactoring (if it is another one).
 * <p>
 * For example a pull-up refactoring deletes an attribute on one type and creates a new attribute on
 * the super type.
 * <p>
 * Depending on the state of the refactoring the target element may be null.
 * 
 * @author dirmeier
 */
public class IpsRefactoringModificationSet {

    private final IIpsElement originalElement;

    private IIpsElement targetElement;

    private final Set<IpsSrcFileModification> modifications;

    /**
     * Creates a new modification set holding all modifications done during a refactoring. The
     * original element may change or a new element may be created. Per default the target element
     * is the same as the original element. If you create a new element and delete the original
     * element make sure you also set the correct target element using
     * {@link #setTargetElement(IIpsElement)}
     * 
     * @param originalElement The original element that should be refactored.
     */
    public IpsRefactoringModificationSet(IIpsElement originalElement) {
        this.originalElement = originalElement;
        targetElement = originalElement;
        modifications = new HashSet<>();
    }

    /**
     * Returns all the modifications stored in this modification set.
     * 
     * @return All the modifications.
     */
    public Set<IpsSrcFileModification> getModifications() {
        return modifications;
    }

    /**
     * Append all the modifications of another {@link IpsRefactoringModificationSet} to this
     * modification set. Other information as original or target element of the given modification
     * set will be discarded.
     * 
     * @param ipsSrcFileModificationSet The modification set which modifications should be appended
     *            to this one
     */
    public void append(IpsRefactoringModificationSet ipsSrcFileModificationSet) {
        modifications.addAll(ipsSrcFileModificationSet.getModifications());
    }

    /**
     * Add a single modification.
     * 
     * @param modification The new modification you want to add
     */
    public void add(IpsSrcFileModification modification) {
        modifications.add(modification);
    }

    /**
     * Check if there are any modifications for the given {@link IIpsSrcFile} that means the
     * original element of the modification needs to be a part of the specified {@link IIpsElement}.
     * 
     * @param ipsSrcFile The {@link IIpsSrcFile} you want to know whether there are any
     *            modifications for
     * 
     * @return true if there are any modifications for the given {@link IIpsSrcFile}
     */
    public boolean containsModification(IIpsSrcFile ipsSrcFile) {
        return getModification(ipsSrcFile) != null;
    }

    private IpsSrcFileModification getModification(IIpsSrcFile ipsSrcFile) {
        for (IpsSrcFileModification ipsSrcFileModification : modifications) {
            if (ipsSrcFileModification.getOriginalIpsSrcFile().equals(ipsSrcFile)) {
                return ipsSrcFileModification;
            }
        }
        return null;
    }

    /**
     * Create and add a new modification for the specified {@link IIpsSrcFile}. This method needs to
     * be called before any modifications are done to the specified file.
     * 
     * @param ipsSrcFile The {@link IIpsSrcFile} before any changes were made.
     */
    public void addBeforeChanged(IIpsSrcFile ipsSrcFile) {
        if (!containsModification(ipsSrcFile)) {
            add(IpsSrcFileModification.createBeforeModification(ipsSrcFile));
        }
    }

    /**
     * Create and add a new rename modification. The target file should not exists yet.
     * 
     * @param original The original {@link IIpsSrcFile} that should be renamed.
     * @param target The {@link IIpsSrcFile} that would exists after renaming the original one.
     */
    public void addRenameModification(IIpsSrcFile original, IIpsSrcFile target) {
        IpsSrcFileModification modification = getModification(original);
        IpsSrcFileModification ipsSrcFileModification = modification;
        if (modification != null) {
            add(IpsSrcFileModification.createRename(original, target, ipsSrcFileModification.getOriginalContent()));
        } else {
            add(IpsSrcFileModification.createBeforeRename(original, target));
        }
    }

    /**
     * Undo all changes by calling {@link IpsSrcFileModification#undo()} on every modification.
     */
    public void undo() {
        for (IpsSrcFileModification modification : modifications) {
            modification.undo();
        }
    }

    /**
     * Getting the element that is created by the refactoring.
     * 
     * @return The {@link IIpsElement} after the refactoring.
     */
    public IIpsElement getTargetElement() {
        return targetElement;
    }

    /**
     * Setting the element that was created by the refactoring.
     * 
     * @param targetElement The refactored element.
     */
    public void setTargetElement(IIpsElement targetElement) {
        this.targetElement = targetElement;
    }

    /**
     * Returns the orginal element before the refactoring startet. If the refactoring does not
     * create a new element but modifies the original element, this may be the same as the target
     * element if refactoring is done. The element will not be copied!
     * 
     * @return The element that was stored at beginning of the refactoring.
     */
    public IIpsElement getOriginalElement() {
        return originalElement;
    }

}
