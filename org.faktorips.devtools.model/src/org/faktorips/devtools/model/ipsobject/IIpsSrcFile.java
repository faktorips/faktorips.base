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
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.abstraction.AFile;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
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
    public IIpsPackageFragment getIpsPackageFragment();

    /**
     * Returns the corresponding platform file. This method returns <code>null</code> if the IPS
     * source file is not stored in it's own dedicated platform file. This is the case if the IPS
     * source file is contained in an IPS archive or is constructed from an input stream (e.g. to
     * display an old revision/history in an editor).
     */
    public AFile getCorrespondingFile();

    /**
     * Returns <code>true</code> if the file contains unsaved changes to it's contents, otherwise
     * <code>false</code>.
     */
    public boolean isDirty();

    /**
     * Marks the file as containing no unsaved changed.
     */
    public void markAsClean();

    /**
     * Marks the file as containing unsaved changes.
     */
    public void markAsDirty();

    /**
     * Discard all changes that haven't been saved yet. If the file is not dirty, nothing happens.
     */
    public void discardChanges();

    /**
     * Returns a new memento with the file's content and dirty state.
     */
    public IIpsSrcFileMemento newMemento() throws CoreRuntimeException;

    /**
     * Update the file's state with the information from the memento.
     * 
     * @throws CoreRuntimeException if the memento wasn't taken from this file.
     */
    public void setMemento(IIpsSrcFileMemento memento) throws CoreRuntimeException;

    /**
     * Saves the file's content to the file system.
     */
    public void save(boolean force, IProgressMonitor monitor) throws CoreRuntimeException;

    /**
     * Returns true if the content can be parsed and a IpsObject can be created based on the
     * content. Returns false, if the contents can't be parsed (e.g. the XML isn't properly
     * formatted).
     * 
     * @throws CoreRuntimeException if an error occurs while reading the contents.
     */
    public boolean isContentParsable() throws CoreRuntimeException;

    /**
     * Returns <code>true</code> if the file has been read from the resource history.
     */
    public boolean isHistoric();

    /**
     * Returns <code>true</code> if this file is contained in an existing
     * {@link IIpsPackageFragmentRoot} of the project. Returns <code>false</code> if this file is
     * either not part of any project (for example {@link #isHistoric()}) or if it corresponds to an
     * resource that is in an non-IPS folder.
     * 
     * @return <code>true</code> if this file is contained in an existing
     *         {@link IIpsPackageFragmentRoot}, otherwise <code>false</code>
     */
    public boolean isContainedInIpsRoot();

    /**
     * Returns the IPS object stored in the file.
     * 
     */
    public IIpsObject getIpsObject();

    /**
     * Returns the given property of the source file.Returns <code>null</code> if the given property
     * wasn't found.<br>
     * Performance hint: this method reads only the properties of the IPS source file (attributes of
     * the first XML node) and returns the value of the given property, the IPS object is not
     * completely read until {@link #getIpsObject()} method is called.
     * 
     */
    public String getPropertyValue(String name);

    /**
     * Returns the IpsObjectType that is contain in this IpsSrcFile.
     */
    public IpsObjectType getIpsObjectType();

    /**
     * Returns the qualified name type of the IPS object contained within this IpsSrcFile.
     */
    public QualifiedNameType getQualifiedNameType();

    /**
     * Returns the name of the IPS object stored in the file.
     */
    public String getIpsObjectName();

    /**
     * Returns whether this file is mutable or not. An IPS source file is immutable in the following
     * cases:
     * <ul>
     * <li>The underlying file does not exists.</li>
     * <li>The underlying file is read-only.</li>
     * <li>The IPS source file is contained in an IPS archive.</li>
     * <li>The IPS source file is created from an input stream to show an old revision /
     * history.</li>
     * </ul>
     */
    public boolean isMutable();

    /**
     * Returns whether this file is read-only or not. An IPS source file is read-only in the
     * following cases:
     * <ul>
     * <li>The underlying file does not exists.</li>
     * <li>The underlying file is read-only.</li>
     * <li>The IPS source file is contained in an IPS archive.</li>
     * <li>The IPS source file is created from an input stream to show an old revision /
     * history.</li>
     * </ul>
     */
    public boolean isReadOnly();

    /**
     * Reads the content from the enclosing resource.
     * 
     * @throws CoreRuntimeException if an error occurs while reading the contents.
     */
    public InputStream getContentFromEnclosingResource() throws CoreRuntimeException;

    /**
     * Returns the name of the base package for the generated artifacts that are mergable. All
     * generated, mergeable artifacts are contained in this package or one of the child packages.
     */
    public String getBasePackageNameForMergableArtefacts() throws CoreRuntimeException;

    /**
     * Returns the name of the base package for the generated artifacts that are 100%derived/not
     * mergable. All generated, 100% derived artifacts are contained in this package or one of the
     * child packages.
     */
    public String getBasePackageNameForDerivedArtefacts() throws CoreRuntimeException;

    /**
     * Deletes this source file by deleting the corresponding resource.
     * <p>
     * Advises the {@link IIpsModel} to remove the source file from the cache.
     * 
     * @throws UnsupportedOperationException If the source file is stored in an archive
     */
    @Override
    public void delete() throws CoreRuntimeException;

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
