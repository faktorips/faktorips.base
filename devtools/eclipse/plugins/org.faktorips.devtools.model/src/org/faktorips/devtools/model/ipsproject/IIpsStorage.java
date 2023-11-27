/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.ipsproject;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.Set;

import org.faktorips.devtools.abstraction.AResource;
import org.faktorips.devtools.abstraction.AResourceDelta;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.ipsobject.QualifiedNameType;

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
    Path getLocation();

    /**
     * Returns the name of this storage. Represents the last segment of this storage path and does
     * not have to be unique.
     */
    String getName();

    /**
     * Returns <code>true</code> if the archive exists, otherwise <code>false</code>.
     */
    boolean exists();

    /**
     * Returns the names (in ascending order) of the non-empty packages contained in the archive.
     * <p>
     * A package is not empty, if it contains at least one IPS object.
     */
    String[] getNonEmptyPackages() throws IpsException;

    /**
     * Returns <code>true</code> if the archive contains the package (empty or not), otherwise
     * <code>false</code>.
     */
    boolean containsPackage(String name) throws IpsException;

    /**
     * Returns the names (in ascending order) of the non-empty direct sub packages for the given
     * parent package as list.
     */
    String[] getNonEmptySubpackages(String pack) throws IpsException;

    /**
     * Returns the set of qualified name types for the IPS objects stored in the archive
     */
    Set<QualifiedNameType> getQNameTypes() throws IpsException;

    /**
     * Returns the set of qualified name types for the IPS objects stored in the given package.
     * Returns an empty set if the archive does not contain an object for the given package or
     * packName is <code>null</code>.
     */
    Set<QualifiedNameType> getQNameTypes(String packName) throws IpsException;

    /**
     * Returns <code>true</code> if the storage contains the IPS object or resource identified by
     * the given path, otherwise <code>false</code>.
     */
    boolean contains(Path path);

    /**
     * Returns the content for the path or <code>null</code> if the archive does not contain the
     * given path. Returns <code>null</code> if path is <code>null</code>.
     * <p>
     * The path is relative to the IPS object root.
     */
    InputStream getContent(Path path);

    /**
     * Returns the content of a file with the given path. Returns <code>null</code> if path is
     * <code>null</code>. Throws a CoreException if no Entry with the given path is found within
     * this {@link IIpsArchive}.
     *
     * @throws IpsException if no Entry with the given path is found within this
     *             {@link IIpsArchive}, or if problems are encountered opening, reading or writing
     *             this archive.
     */
    InputStream getResourceAsStream(String path);

    /**
     * Returns the resource path of a given element path in a corresponding bundle.
     *
     * @param elementPath The {@link Path} of an element.
     *
     * @return The resource {@link Path} of an element.
     *
     */
    Path getResourcePath(Path elementPath);

    /**
     * Returns the name of the base package for the mergeable artifacts (XML-Files, Java source
     * files). All mergeable artifacts are contained in this package or one of the child packages.
     */
    String getBasePackageNameForMergableArtefacts(QualifiedNameType qnt) throws IpsException;

    /**
     * Returns the name of the base package for the derived artifacts (XML-Files, Java source
     * files). All derived artifacts are contained in this package or one of the child packages.
     */
    String getBasePackageNameForDerivedArtefacts(QualifiedNameType qnt) throws IpsException;

    /**
     * Check weather this archive is valid or not. A archive is valid if the corresponding file
     * exists and the file is a readable IPS archive.
     *
     * @return true if the archive exists and is readable
     */
    boolean isValid();

    /**
     * Returns true, if this archive is part of the provided delta or one of its children.
     *
     * @see IIpsArchiveEntry#isAffectedBy(AResourceDelta)
     */
    boolean isAffectedBy(AResourceDelta delta);

    /**
     * Returns an IResource only if the resource can be located in the workspace. If the path is
     * relative it have to be located in the roots project project. The file does not have exists
     * but have to be relative (to the project) or the first segment must match an existing project.
     *
     * @return The found {@link AResource} if the path is workspace or project relative. Returns
     *             null if the path is not valid.
     */
    AResource getCorrespondingResource();

    /**
     * returns true, if the IIpsStorage represents a folder and false, if not.
     */
    boolean isFolder();
}
