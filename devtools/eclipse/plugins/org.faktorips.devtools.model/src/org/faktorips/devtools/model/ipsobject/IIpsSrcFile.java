/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.ipsobject;

import java.io.InputStream;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.abstraction.AFile;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;

/**
 * A handle for files containing sourcecode for IPS objects.
 */
public interface IIpsSrcFile extends IIpsElement {

    /**
     * Returns the package fragment the file belongs to. If this IPS source file is constructed from
     * an input stream (for example to show an old revision/history) this method returns null!
     */
    IIpsPackageFragment getIpsPackageFragment();

    /**
     * Returns the corresponding platform file. This method returns <code>null</code> if the IPS
     * source file is not stored in it's own dedicated platform file. This is the case if the IPS
     * source file is contained in an IPS archive or is constructed from an input stream (e.g. to
     * display an old revision/history in an editor).
     */
    AFile getCorrespondingFile();

    /**
     * Returns <code>true</code> if the file contains unsaved changes to it's contents, otherwise
     * <code>false</code>.
     */
    boolean isDirty();

    /**
     * Marks the file as containing no unsaved changed.
     */
    void markAsClean();

    /**
     * Marks the file as containing unsaved changes.
     */
    void markAsDirty();

    /**
     * Discard all changes that haven't been saved yet. If the file is not dirty, nothing happens.
     */
    void discardChanges();

    /**
     * Returns a new memento with the file's content and dirty state.
     */
    IIpsSrcFileMemento newMemento() throws IpsException;

    /**
     * Update the file's state with the information from the memento.
     * 
     * @throws IpsException if the memento wasn't taken from this file.
     */
    void setMemento(IIpsSrcFileMemento memento) throws IpsException;

    /**
     * Saves the file's content to the file system.
     * 
     * @deprecated since 22.6 for removal; use {@link #save(IProgressMonitor)} instead, as the
     *                 {@code force} parameter is ignored anyways.
     */
    @Deprecated(forRemoval = true, since = "22.6")
    void save(boolean force, IProgressMonitor monitor) throws IpsException;

    /**
     * Saves the file's content to the file system.
     */
    void save(IProgressMonitor monitor) throws IpsException;

    /**
     * Returns true if the content can be parsed and a IpsObject can be created based on the
     * content. Returns false, if the contents can't be parsed (e.g. the XML isn't properly
     * formatted).
     * 
     * @throws IpsException if an error occurs while reading the contents.
     */
    boolean isContentParsable() throws IpsException;

    /**
     * Returns <code>true</code> if the file has been read from the resource history.
     */
    boolean isHistoric();

    /**
     * Returns <code>true</code> if this file is contained in an existing
     * {@link IIpsPackageFragmentRoot} of the project. Returns <code>false</code> if this file is
     * either not part of any project (for example {@link #isHistoric()}) or if it corresponds to an
     * resource that is in an non-IPS folder.
     * 
     * @return <code>true</code> if this file is contained in an existing
     *             {@link IIpsPackageFragmentRoot}, otherwise <code>false</code>
     */
    boolean isContainedInIpsRoot();

    /**
     * Returns the IPS object stored in the file.
     * 
     */
    IIpsObject getIpsObject();

    /**
     * Returns the given property of the source file.Returns <code>null</code> if the given property
     * wasn't found.<br>
     * Performance hint: this method reads only the properties of the IPS source file (attributes of
     * the first XML node) and returns the value of the given property, the IPS object is not
     * completely read until {@link #getIpsObject()} method is called.
     * 
     */
    String getPropertyValue(String name);

    /**
     * Returns the IpsObjectType that is contain in this IpsSrcFile.
     */
    IpsObjectType getIpsObjectType();

    /**
     * Returns the qualified name type of the IPS object contained within this IpsSrcFile.
     */
    QualifiedNameType getQualifiedNameType();

    /**
     * Returns the name of the IPS object stored in the file.
     */
    String getIpsObjectName();

    /**
     * Returns whether this file is mutable or not. An IPS source file is immutable in the following
     * cases:
     * <ul>
     * <li>The underlying file does not exist.</li>
     * <li>The underlying file is read-only.</li>
     * <li>The IPS source file is contained in an IPS archive.</li>
     * <li>The IPS source file is created from an input stream to show an old revision /
     * history.</li>
     * </ul>
     */
    boolean isMutable();

    /**
     * Returns whether this file is read-only or not. An IPS source file is read-only in the
     * following cases:
     * <ul>
     * <li>The underlying file does not exist.</li>
     * <li>The underlying file is read-only.</li>
     * <li>The IPS source file is contained in an IPS archive.</li>
     * <li>The IPS source file is created from an input stream to show an old revision /
     * history.</li>
     * </ul>
     */
    boolean isReadOnly();

    /**
     * Reads the content from the enclosing resource.
     * 
     * @throws IpsException if an error occurs while reading the contents.
     */
    InputStream getContentFromEnclosingResource() throws IpsException;

    /**
     * Returns the name of the base package for the generated artifacts that are mergable. All
     * generated, mergeable artifacts are contained in this package or one of the child packages.
     */
    String getBasePackageNameForMergableArtefacts() throws IpsException;

    /**
     * Returns the name of the base package for the generated artifacts that are 100%derived/not
     * mergable. All generated, 100% derived artifacts are contained in this package or one of the
     * child packages.
     */
    String getBasePackageNameForDerivedArtefacts() throws IpsException;

    /**
     * Deletes this source file by deleting the corresponding resource.
     * <p>
     * Advises the {@link IIpsModel} to remove the source file from the cache.
     * 
     * @throws UnsupportedOperationException If the source file is stored in an archive
     */
    @Override
    void delete() throws IpsException;

    /**
     * If {@code validateIpsSchema} is used this set may contain error from the XML parser.
     * 
     * @return A set with error messages
     */
    Set<String> getXsdValidationErrors();

    /**
     * If {@code validateIpsSchema} is used this set may contain warnings from the XML parser.
     * 
     * @return A set with warnings
     */
    Set<String> getXsdValidationWarnings();
}
