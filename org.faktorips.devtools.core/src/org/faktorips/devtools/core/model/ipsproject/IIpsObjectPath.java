/*******************************************************************************
 * Copyright © 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen, 
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 ******************************************************************************/
package org.faktorips.devtools.core.model.ipsproject;

import java.io.InputStream;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsObjectPath;
import org.faktorips.util.message.MessageList;

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
    public final static String MSGCODE_SRC_FOLDER_ENTRY_MISSING = "SourceFolderEntryMissing"; //$NON-NLS-1$

    /**
     * Message code constant that indicates that the output folder for mergable java sources is not
     * specified.
     */
    public final static String MSGCODE_MERGABLE_OUTPUT_FOLDER_NOT_SPECIFIED = "MergableOutputFolderNotSpecified"; //$NON-NLS-1$

    /**
     * Message code constant that indicates that the output folder for derived java sources is not
     * specified.
     */
    public final static String MSGCODE_DERIVED_OUTPUT_FOLDER_NOT_SPECIFIED = "DerivedOutputFolderNotSpecified"; //$NON-NLS-1$

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
     * Returns the IPS projects referenced by the object path.
     */
    public IIpsProject[] getReferencedIpsProjects();

    /**
     * Factory method that creates a new source folder entry and adds it to the list of entries.
     */
    public IIpsSrcFolderEntry newSourceFolderEntry(IFolder srcFolder);

    /**
     * Factory method that creates a new archive entry and adds it to the list of entries.
     */
    public IIpsArchiveEntry newArchiveEntry(IPath archivePath) throws CoreException;

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
    public boolean containsSrcFolderEntry(IFolder entry);

    /**
     * Removes the given source folder from the list of entries if contained.
     */
    public void removeSrcFolderEntry(IFolder srcFolder);

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
     * Returns the output folder for generated but mergable sources used for all source folders.
     */
    public IFolder getOutputFolderForMergableSources();

    /**
     * Sets the output folder for generated but mergable sources. If the output folder is not
     * defined per source folder that all mergable sources are generated into this directory.
     */
    public void setOutputFolderForMergableSources(IFolder outputFolder);

    /**
     * Returns all output folders specified in the path.
     */
    public IFolder[] getOutputFolders();

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
    public IFolder getOutputFolderForDerivedSources();

    /**
     * Sets the output folder for derived sources.
     */
    public void setOutputFolderForDerivedSources(IFolder outputFolder);

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
    public MessageList validate() throws CoreException;

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
     * Retrieves the contents of a file in the {@link IpsObjectPath}. Returns <code>null</code> if
     * no resource is found at the given path. If the {@link IpsObjectPath} contains multiple
     * resources with the same path the first find will be returned.
     */
    public InputStream getResourceAsStream(String path);

}
