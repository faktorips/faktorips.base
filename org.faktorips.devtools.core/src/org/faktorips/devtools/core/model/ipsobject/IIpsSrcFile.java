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

package org.faktorips.devtools.core.model.ipsobject;

import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;

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
    public IFile getCorrespondingFile();

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
    public IIpsSrcFileMemento newMemento() throws CoreException;

    /**
     * Update the file's state with the information from the memento.
     * 
     * @throws CoreException if the memento wasn't taken from this file.
     */
    public void setMemento(IIpsSrcFileMemento memento) throws CoreException;

    /**
     * Saves the file's content to the file system.
     */
    public void save(boolean force, IProgressMonitor monitor) throws CoreException;

    /**
     * Returns true if the content can be parsed and a IpsObject can be created based on the
     * content. Returns false, if the contents can't be parsed (e.g. the XML isn't properly
     * formatted).
     * 
     * @throws CoreException if an error occurs while reading the contents.
     */
    public boolean isContentParsable() throws CoreException;

    /**
     * Returns <code>true</code> if the file has been read from the resource history.
     */
    public boolean isHistoric();

    /**
     * Returns the IPS object stored in the file.
     * 
     * @throws CoreException if the file can't be read or it's contents can't be parsed.
     */
    public IIpsObject getIpsObject() throws CoreException;

    /**
     * Returns the given property of the source file.Returns <code>null</code> if the given property
     * wasn't found.<br>
     * Performance hint: this method reads only the properties of the IPS source file (attributes of
     * the first XML node) and returns the value of the given property, the IPS object is not
     * completely read until {@link #getIpsObject()} method is called.
     * 
     * @throws CoreException if the file can't be read or it's contents can't be parsed.
     */
    public String getPropertyValue(String name) throws CoreException;

    /**
     * Returns the IpsObjectType that is contain in this IpsSrcFile.
     */
    public IpsObjectType getIpsObjectType();

    /**
     * Returns the qualified name type of the IPS object contained within this IpsSrcFile.
     */
    public QualifiedNameType getQualifiedNameType();

    /**
     * Returns the name of the ips object stored in the file.
     */
    public String getIpsObjectName();

    /**
     * Returns whether this file is mutable or not. An IPS source file is immutable in the following
     * cases:
     * <ul>
     * <li>The underlying file does not exists.</li>
     * <li>The underlying file is read-only.</li>
     * <li>The IPS source file is contained in an IPS archive.</li>
     * <li>The IPS source file is created from an input stream to show an old revision / history.</li>
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
     * <li>The IPS source file is created from an input stream to show an old revision / history.</li>
     * </ul>
     */
    public boolean isReadOnly();

    /**
     * Reads the content from the enclosing resource.
     * 
     * @throws CoreException if an error occurs while reading the contents.
     */
    public InputStream getContentFromEnclosingResource() throws CoreException;

    /**
     * Returns the name of the base package for the generated artifacts that are mergable. All
     * generated, mergable artifacts are contained in this package or one of the child packages.
     */
    public String getBasePackageNameForMergableArtefacts() throws CoreException;

    /**
     * Returns the name of the base package for the generated artifacts that are 100%derived/not
     * mergable. All generated, 100% derived artifacts are contained in this package or one of the
     * child packages.
     */
    public String getBasePackageNameForDerivedArtefacts() throws CoreException;

}
