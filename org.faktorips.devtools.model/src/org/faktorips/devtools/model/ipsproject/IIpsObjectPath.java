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

import org.eclipse.core.runtime.IPath;
import org.faktorips.devtools.abstraction.AFolder;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsobject.QualifiedNameType;
import org.faktorips.runtime.MessageList;

/**
 * The IPS object path defines where IPS objects can be found. It is the same concept as the Java
 * class path.
 * 
 * @author Jan Ortmann
 */
public interface IIpsObjectPath {

    /**
     * Message code constant identifying the message of a validation rule.
     */
    public static final String MSGCODE_SRC_FOLDER_ENTRY_MISSING = "SourceFolderEntryMissing"; //$NON-NLS-1$

    /**
     * Message code constant that indicates that the output folder for mergeable java sources is not
     * specified.
     */
    public static final String MSGCODE_MERGABLE_OUTPUT_FOLDER_NOT_SPECIFIED = "MergableOutputFolderNotSpecified"; //$NON-NLS-1$

    /**
     * Message code constant that indicates that the output folder for derived java sources is not
     * specified.
     */
    public static final String MSGCODE_DERIVED_OUTPUT_FOLDER_NOT_SPECIFIED = "DerivedOutputFolderNotSpecified"; //$NON-NLS-1$

    /**
     * Returns the IPS project this path belongs to.
     */
    public IIpsProject getIpsProject();

    /**
     * Returns the entry for the given IPS package fragment root name or <code>null</code> if no
     * such entry exists.
     */
    public IIpsObjectPathEntry getEntry(String rootName);

    /**
     * Returns the path' entries.
     */
    public IIpsObjectPathEntry[] getEntries();

    /**
     * Returns the source folder entries of this IPS project path.
     */
    public IIpsSrcFolderEntry[] getSourceFolderEntries();

    /**
     * Returns the project reference entries of this IPS project path.
     */
    public IIpsProjectRefEntry[] getProjectRefEntries();

    /**
     * Returns the IPS archive entries of this IPS project path.
     */
    public IIpsArchiveEntry[] getArchiveEntries();

    /**
     * Sets the path' entries.
     */
    public void setEntries(IIpsObjectPathEntry[] newEntries);

    /**
     * Returns the IPS projects directly referenced by this object path.
     */
    public List<IIpsProject> getDirectlyReferencedIpsProjects();

    /**
     * Returns all {@link IIpsProject IpsProjects} that are directly or indirectly referenced by
     * this object path.
     * 
     * #see {@link #getDirectlyReferencedIpsProjects()}
     */
    public List<IIpsProject> getAllReferencedIpsProjects();

    /**
     * Factory method that creates a new source folder entry and adds it to the list of entries.
     */
    public IIpsSrcFolderEntry newSourceFolderEntry(AFolder srcFolder);

    /**
     * Factory method that creates a new archive entry and adds it to the list of entries. If there
     * is already an archive entry with the given path no entry is created but the existing entry is
     * returned.
     */
    public IIpsArchiveEntry newArchiveEntry(IPath archivePath) throws CoreRuntimeException;

    /**
     * Factory method that creates a new project reference entry and adds it to the list of entries.
     */
    public IIpsProjectRefEntry newIpsProjectRefEntry(IIpsProject project);

    /**
     * @return true if this path contains a reference to the given project.
     */
    public boolean containsProjectRefEntry(IIpsProject ipsProject);

    /**
     * Removes the given project from the list of entries if contained.
     */
    public void removeProjectRefEntry(IIpsProject ipsProject);

    /**
     * @return true if this path contains the given archive.
     */
    public boolean containsArchiveEntry(IIpsArchive ipsArchive);

    /**
     * Removes the given archive from the list of entries if contained.
     */
    public void removeArchiveEntry(IIpsArchive ipsArchive);

    /**
     * @return true if this path contains a reference to the given source folder.
     */
    public boolean containsSrcFolderEntry(AFolder entry);

    /**
     * Removes the given source folder from the list of entries if contained.
     */
    public void removeSrcFolderEntry(AFolder srcFolder);

    /**
     * Returns true if the output folder and base package are defined per source folder, otherwise
     * false.
     */
    public boolean isOutputDefinedPerSrcFolder();

    /**
     * Sets if the output folder and base package are defined per source folder.
     */
    public void setOutputDefinedPerSrcFolder(boolean newValue);

    /**
     * Returns the output folder for generated but mergeable sources used for all source folders.
     */
    public AFolder getOutputFolderForMergableSources();

    /**
     * Sets the output folder for generated but mergeable sources. If the output folder is not
     * defined per source folder that all mergeable sources are generated into this directory.
     */
    public void setOutputFolderForMergableSources(AFolder outputFolder);

    /**
     * Returns all output folders specified in the path.
     */
    public AFolder[] getOutputFolders();

    /**
     * Returns the name of the base package for the generated Java source files that are to be
     * merged with the newly generated content during a build cycle.
     */
    public String getBasePackageNameForMergableJavaClasses();

    /**
     * Sets the name of the base package for the generated Java source files that are to be merged
     * with the newly generated content during a build cycle.
     */
    public void setBasePackageNameForMergableJavaClasses(String name);

    /**
     * Returns the output folder for generated artifacts that are marked as derived. More precise
     * this folder will be marked as derived and hence all resources within are considered derived.
     * Derived artifacts are not managed by the resource management system (e.g. CVS). During the
     * clean build phase all resources in this folder will be deleted.
     */
    public AFolder getOutputFolderForDerivedSources();

    /**
     * Sets the output folder for derived sources.
     */
    public void setOutputFolderForDerivedSources(AFolder outputFolder);

    /**
     * Returns the name of the base package for generated Java source files that are considered
     * derived.
     * 
     * @see #getOutputFolderForDerivedSources()
     */
    public String getBasePackageNameForDerivedJavaClasses();

    /**
     * Sets the name of the base package for generated Java source files that are considered
     * derived.
     * 
     * @see #getOutputFolderForDerivedSources()
     */
    public void setBasePackageNameForDerivedJavaClasses(String name);

    /**
     * Validates the object path and returns the result as list of messages.
     */
    public MessageList validate() throws CoreRuntimeException;

    /**
     * Moves the entries at at the given indices up/down and adjusts the positions of the elements
     * in between accordingly.
     * 
     * @param indices an array with indices of the entries to be moved. If it contains negative
     *            indices or indices greater than the number of entries the resulted operation is
     *            undefined.
     * @param up entries will be moved up one position if true, down otherwise
     * 
     * @return the indices of the entries' positions after the move operation
     */
    public int[] moveEntries(int[] indices, boolean up);

    /**
     * This method checks whether this object path has a resource with the specified path. The path
     * is relative to any entry's root.
     * 
     * @param path The path of the requested resource
     * @return <code>true</code> if the resource could be found in this entry, <code>false</code> if
     *         not
     */
    public boolean containsResource(String path);

    /**
     * Retrieves the contents of a file in the {@link IIpsObjectPath}. Returns <code>null</code> if
     * no resource is found at the given path. If the {@link IIpsObjectPath} contains multiple
     * resources with the same path the first find will be returned.
     */
    public InputStream getResourceAsStream(String path);

    /**
     * Returns true, if the {@link IIpsObjectPathEntry entries} of this path are defined in the
     * manifest.mf and false if not.
     */
    public boolean isUsingManifest();

    /**
     * Sets, if the {@link IIpsObjectPathEntry entries} are defined in the manifest.mf or in the
     * .ipsproject file.
     */
    public void setUsingManifest(boolean useManifest);

    /**
     * Factory method that creates a new container entry and adds it to the list of entries. If
     * there is already a container with the same configuration the existing container will be
     * returned.
     * 
     * @param containerTypeId The type id of the container as defined by a
     *            {@link IIpsObjectPathContainerType}
     * @param optionalPath The optional path or null if the container do not need an optional path
     * 
     * @return The created container or the existing one if there is already a container with the
     *         same configuration
     */
    public IIpsContainerEntry newContainerEntry(String containerTypeId, String optionalPath);

    /**
     * Returns the first {@link IIpsSrcFile} with the indicated qualified name type found on the
     * path. Returns <code>null</code> if no such object is found.
     * 
     * @param nameType representing the {@link QualifiedNameType} of the searched
     *            {@link IIpsSrcFile}
     * @return the found {@link IIpsSrcFile}
     */
    public IIpsSrcFile findIpsSrcFile(QualifiedNameType nameType);

    /**
     * Returns <code>true</code> if more than one {@link IIpsSrcFile} with the indicated qualified
     * name type found on the path. Returns <code>false</code> if no such object is found or just
     * one {@link IIpsSrcFile} was found.
     * 
     * @param nameType representing the {@link QualifiedNameType} of the searched
     *            {@link IIpsSrcFile}
     */
    public boolean findDuplicateIpsSrcFile(QualifiedNameType nameType);

    /**
     * Returns {@link IIpsSrcFile IPS source files} with the indicated {@link IpsObjectType}.
     * 
     * @param ipsObjectType representing the {@link IpsObjectType} of the searched
     *            {@link IIpsSrcFile IIpsSrcFiles}
     */
    public List<IIpsSrcFile> findIpsSrcFiles(IpsObjectType... ipsObjectType);
}
