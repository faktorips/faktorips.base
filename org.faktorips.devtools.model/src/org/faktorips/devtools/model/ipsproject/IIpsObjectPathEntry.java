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
import java.util.List;

import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsobject.QualifiedNameType;
import org.faktorips.runtime.MessageList;

/**
 * An entry in an IPS object path.
 * 
 * @author Jan Ortmann
 */
public interface IIpsObjectPathEntry {

    /**
     * Prefix for all message codes of this class.
     */
    public static final String MSGCODE_PREFIX = "IIPSOBJECTPATHENTRY-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that a related folder is missing.
     */
    public static final String MSGCODE_MISSING_FOLDER = MSGCODE_PREFIX + "MissingFolder"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the related project is missing.
     */
    public static final String MSGCODE_PROJECT_NOT_SPECIFIED = MSGCODE_PREFIX + "ProjectNotSpecified"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the related project is missing.
     */
    public static final String MSGCODE_MISSING_PROJECT = MSGCODE_PREFIX + "MissingProject"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that a related archive is missing.
     */
    public static final String MSGCODE_MISSING_ARCHVE = MSGCODE_PREFIX + "MissingArchive"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that a related bundle is missing.
     */
    public static final String MSGCODE_MISSING_BUNDLE = MSGCODE_PREFIX + "MissingBundle"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that a related archive is invalid.
     */
    public static final String MSGCODE_INVALID_ARCHVE = MSGCODE_PREFIX + "InvalidArchive"; //$NON-NLS-1$

    /**
     * Warning message used to mark invalid cast attempts instead of leaving the path empty. See
     * FIPS-6417
     */
    public static final String ERROR_CAST_EXCEPTION_PATH = "Operation failed: Cannot cast to IpsSourceFolderEntry"; //$NON-NLS-1$

    /**
     * Type constant indicating a source folder entry.
     */
    public static final String TYPE_SRC_FOLDER = "src"; //$NON-NLS-1$

    /**
     * Type constant indicating a project reference entry.
     */
    public static final String TYPE_PROJECT_REFERENCE = "project"; //$NON-NLS-1$

    /**
     * Type constant indicating a archive (library) containing the model files.
     */
    public static final String TYPE_ARCHIVE = "archive"; //$NON-NLS-1$

    /**
     * Type constant indicating a container entry.
     */
    public static final String TYPE_CONTAINER = "container"; //$NON-NLS-1$

    /**
     * Type constant indicating a jar bundle entry.
     * 
     */
    public static final String TYPE_BUNDLE = "bundle"; //$NON-NLS-1$

    /**
     * Returns the object path this is an entry of.
     */
    public IIpsObjectPath getIpsObjectPath();

    /**
     * Returns the IPS project this project belongs to.
     */
    public IIpsProject getIpsProject();

    /**
     * Returns the type of this entry as one of the type constant defined in this interface.
     * 
     * @see #TYPE_SRC_FOLDER
     * @see #TYPE_ARCHIVE
     * @see #TYPE_PROJECT_REFERENCE
     * @see #TYPE_CONTAINER
     */
    public String getType();

    /**
     * Returns <code>true</code> if this is a container entry that can be resolved to "real"
     * entries.
     */
    public boolean isContainer();

    /**
     * Returns the (zero based) index of this entry in the path.
     */
    public int getIndex();

    /**
     * Returns the name of the IPS package fragment root this entry defines or <code>null</code> if
     * this is a project reference entry.
     */
    public abstract String getIpsPackageFragmentRootName();

    /**
     * Returns the package fragment root this entry defines or <code>null</code> if this is a
     * project reference entry. Note that is not guaranteed that the returned package fragment root
     * exists.
     */
    public IIpsPackageFragmentRoot getIpsPackageFragmentRoot();

    /**
     * Validates the object path entry and returns the result as list of messages.
     */
    public MessageList validate();

    /**
     * Returns the IPS source file with the indicated qualified name type.
     */
    public IIpsSrcFile findIpsSrcFile(QualifiedNameType nameType);

    /**
     * Returns IPS source files with the {@link IpsObjectType}.
     */
    public List<IIpsSrcFile> findIpsSrcFiles(IpsObjectType ipsObjectType);

    /**
     * This method checks whether this entry has a resource with the specified path.
     * {@link IIpsProjectRefEntry IIpsProjectRefEntrys} and {@link IIpsContainerEntry
     * IIpsContainerEntrys} return always <code>false</code> as they can not directly contain a
     * resource with a specified path. The path is relative to the entry's resource root.
     * 
     * 
     * @param resourcePath The path of the requested resource
     * @return <code>true</code> if the resource could be found in this entry, <code>false</code> if
     *         not
     */
    public boolean containsResource(String resourcePath);

    /**
     * Returns an {@link InputStream} that provides a resource's/file's contents. The given path is
     * interpreted as a relative path in respect to the path-entry's resource.
     * <p>
     * Callers of this method are responsible for closing the stream after use.
     * <p>
     * This method may throw a {@link RuntimeException} or {@link IpsException} if there occur any
     * exception while searching the requested resource. To avoid exceptions first check whether the
     * resource exists in this entry by calling {@link #containsResource(String)}. As
     * {@link IIpsProjectRefEntry IpsProjectRefEntrys} and {@link IIpsContainerEntry} always return
     * <code>false</code> when calling calling {@link #containsResource(String)}, this method
     * returns null for those entries.
     * 
     * @param path The path of the requested resource
     * @return The {@link InputStream} of the resource. Make sure to close the input stream after
     *         reading.
     */
    public InputStream getResourceAsStream(String path);

    /**
     * Returns <code>true</code> if this entry should be reexported.
     */
    public boolean isReexported();

    /**
     * Sets the flag that this entry should reexported.
     */
    void setReexported(boolean reexported);

}
