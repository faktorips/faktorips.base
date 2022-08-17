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

import org.eclipse.core.runtime.NullProgressMonitor;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFileMemento;
import org.faktorips.devtools.model.util.RefactorUtil;

/**
 * This class is used to remember the state of {@link IIpsSrcFile} for undo some modifications.
 * These modifications may be done within an {@link IIpsSrcFile} or it may rename the
 * {@link IIpsSrcFile} (and also may modify the renamed file).
 * <p>
 * The modification needs to be created before any modification was done to the {@link IIpsSrcFile}.
 * It stores the state and the name of the {@link IIpsSrcFile} and you are able to call
 * {@link #undo()} to revert any changes. Note that object references to parts of the changed
 * {@link IIpsObject} may not be valid after undo because of the recreation of some objects!
 * 
 * @author dirmeier
 */
public class IpsSrcFileModification {

    private final IIpsSrcFile originalIpsSrcFile;

    private final IIpsSrcFile targetIpsSrcFile;

    private final IIpsSrcFileMemento originalContent;

    private IpsSrcFileModification(IIpsSrcFile affectedIpsSrcFile, IIpsSrcFileMemento originalContent) {
        this(affectedIpsSrcFile, affectedIpsSrcFile, originalContent);
    }

    private IpsSrcFileModification(IIpsSrcFile originalIpsSrcFile, IIpsSrcFile targetIpsSrcFile,
            IIpsSrcFileMemento originalContent) {
        this.originalIpsSrcFile = originalIpsSrcFile;
        this.targetIpsSrcFile = targetIpsSrcFile;
        this.originalContent = originalContent;
    }

    /**
     * This creates a modification class holding the state of the {@link IIpsSrcFile} before it was
     * modified.
     * 
     * @param ipsSrcFile The {@link IIpsSrcFile} before it is modified
     * 
     * @return The modification instance holding the state of the {@link IIpsSrcFile}
     */
    public static IpsSrcFileModification createBeforeModification(IIpsSrcFile ipsSrcFile) {
        return new IpsSrcFileModification(ipsSrcFile, ipsSrcFile.newMemento());
    }

    /**
     * This creates a rename (or move) modification. You have to specify the old and the new
     * {@link IIpsSrcFile}. The source file may also be modified.
     * 
     * @param source The source file you want to rename
     * @param target The target of rename (not existing yet).
     * 
     * @return The modification instance to undo the changes you want to make.
     */
    public static IpsSrcFileModification createBeforeRename(IIpsSrcFile source, IIpsSrcFile target) {
        return new IpsSrcFileModification(source, target, source.newMemento());
    }

    /**
     * Creates a rename modification but do not use the current state of the original
     * {@link IIpsSrcFile} but another state. This is useful if you have already a modification for
     * this {@link IIpsSrcFile} and you want to expand the existing modification with a rename
     * modification.
     * 
     * @param original The original {@link IIpsSrcFile} before rename
     * @param target The target {@link IIpsSrcFile} (not existing yet)
     * @param originalContent The original content of the {@link IIpsSrcFile} before it was
     *            modified.
     * 
     * @return the modification instance to undo any changes.
     */
    public static IpsSrcFileModification createRename(IIpsSrcFile original,
            IIpsSrcFile target,
            IIpsSrcFileMemento originalContent) {
        return new IpsSrcFileModification(original, target, originalContent);
    }

    /**
     * Getting the target {@link IIpsSrcFile} in case of a rename modification. In case of a normal
     * modification the target file is the same as the original file.
     */
    public IIpsSrcFile getTargetIpsSrcFile() {
        return targetIpsSrcFile;
    }

    /**
     * The original {@link IIpsSrcFile} before any modifications were done.
     */
    public IIpsSrcFile getOriginalIpsSrcFile() {
        return originalIpsSrcFile;
    }

    /**
     * The original content of the original {@link IIpsSrcFile} before any modifications
     * 
     */
    public IIpsSrcFileMemento getOriginalContent() {
        return originalContent;
    }

    /**
     * Undo any renames, discard any changes and set the original content. Some object reference may
     * be different after undo all changes because we cannot undo the changes in any single object
     * but need to reinitialize the whole object.
     * <p>
     * For example you have the reference to an attribute of a type. First before any changes you
     * have created a {@link IpsSrcFileModification}. Second delete the attribute. Third you undo
     * the changes using the {@link IpsSrcFileModification#undo()}. Now your reference still points
     * to the deleted attribute. But using the name of the attribute you will find a new attribute
     * that matches exactly the content of your attribute before it was deleted.
     */
    public void undo() {
        if (originalIpsSrcFile.exists()) {
            resetChanges(originalIpsSrcFile);
        }
        if (!targetIpsSrcFile.equals(originalIpsSrcFile) && targetIpsSrcFile.exists()) {
            targetIpsSrcFile.discardChanges();
            move(targetIpsSrcFile, originalIpsSrcFile);
            resetChanges(originalIpsSrcFile);
        }
    }

    protected void move(IIpsSrcFile from, IIpsSrcFile to) {
        RefactorUtil.moveIpsSrcFile(from, to.getIpsPackageFragment(), to.getIpsObjectName(), new NullProgressMonitor());
    }

    protected void resetChanges(IIpsSrcFile ipsSrcFile) {
        ipsSrcFile.setMemento(getOriginalContent());
    }

}
