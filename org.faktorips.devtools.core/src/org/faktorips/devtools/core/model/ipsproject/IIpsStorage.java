/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.model.ipsproject;

import java.io.InputStream;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;

/**
 * 
 * This {@link IIpsStorage} is an interface, which allows storing Faktor-IPS-Files outside of the
 * source folders of an {@link IIpsProject}. This interface has several methods to access such a
 * storage.
 * 
 * @author dicker
 */
public interface IIpsStorage {

    /**
     * Returns the absolute path in the local file system to this resource, or <code>null</code> if
     * no path can't be determined.
     */
    public IPath getLocation();

    /**
     * Returns the name of this storage. Represents the last segment of this storage path and does
     * not have to be unique.
     */
    public String getName();

    /**
     * Returns <code>true</code> if the archive exists, otherwise <code>false</code>.
     */
    public boolean exists();

    /**
     * Returns the names (in ascending order) of the non-empty packages contained in the archive.
     * <p>
     * A package is not empty, if it contains at least one IPS object.
     */
    public String[] getNonEmptyPackages() throws CoreException;

    /**
     * Returns <code>true</code> if the archive contains the package (empty or not), otherwise
     * <code>false</code>.
     */
    public boolean containsPackage(String name) throws CoreException;

    /**
     * Returns the names (in ascending order) of the non-empty direct sub packages for the given
     * parent package as list.
     */
    public String[] getNonEmptySubpackages(String pack) throws CoreException;

    /**
     * Returns the set of qualified name types for the IPS objects stored in the archive
     */
    public Set<QualifiedNameType> getQNameTypes() throws CoreException;

    /**
     * Returns the set of qualified name types for the IPS objects stored in the given package.
     * Returns an empty set if the archive does not contain an object for the given package or
     * packName is <code>null</code>.
     */
    public Set<QualifiedNameType> getQNameTypes(String packName) throws CoreException;

    /**
     * Returns <code>true</code> if the storage contains the IPS object or resource identified by the given
     * path, otherwise <code>false</code>.
     */
    public boolean contains(IPath path);

    /**
     * Returns the content for the path or <code>null</code> if the archive does not contain the
     * given path. Returns <code>null</code> if path is <code>null</code>.
     * <p>
     * The path is relative to the ips object root.
     */
    public InputStream getContent(IPath path);

    /**
     * Returns the content of a file with the given path. Returns <code>null</code> if path is
     * <code>null</code>. Throws a CoreException if no Entry with the given path is found within
     * this {@link IIpsArchive}.
     * 
     * @throws CoreRuntimeException if no Entry with the given path is found within this
     *             {@link IIpsArchive}, or if problems are encountered opening, reading or writing
     *             this archive.
     */
    public InputStream getResourceAsStream(String path);

    /**
     * Returns the name of the base package for the mergable artifacts (XML-Files, Java source
     * files). All mergable artifacts are contained in this package or one of the child packages.
     */
    public String getBasePackageNameForMergableArtefacts(QualifiedNameType qnt) throws CoreException;

    /**
     * Returns the name of the base package for the derived artifacts (XML-Files, Java source
     * files). All derived artifacts are contained in this package or one of the child packages.
     */
    public String getBasePackageNameForDerivedArtefacts(QualifiedNameType qnt) throws CoreException;

    /**
     * Check weather this archive is valid or not. A archive is valid if the corresponding file
     * exists and the file is a readable ips archive.
     * 
     * @return true if the archive exists and is readable
     */
    public boolean isValid();

    /**
     * Returns true, if this archive is part of the provided delta or one of its children.
     * 
     * @see IIpsArchiveEntry#isAffectedBy(IResourceDelta)
     */
    public boolean isAffectedBy(IResourceDelta delta);

    /**
     * Returns an IResource only if the resource can be located in the workspace. If the path is
     * relative it have to be located in the roots project project. The file does not have exists
     * but have to be relative (to the project) or the first segment must match an existing project.
     * 
     * @return The found {@link IResource} if the path is workspace or project relative. Returns
     *         null if the path is not valid.
     */
    public IResource getCorrespondingResource();

    /**
     * returns true, if the IIpsStorage represents a folder and false, if not.
     */
    public boolean isFolder();
}