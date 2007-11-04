/***************************************************************************************************
 *  * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.  *  * Alle Rechte vorbehalten.  *  *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,  * Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der  * Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community)  * genutzt werden, die Bestandteil der Auslieferung ist und auch
 * unter  *   http://www.faktorips.org/legal/cl-v01.html  * eingesehen werden kann.  *  *
 * Mitwirkende:  *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de  *  
 **************************************************************************************************/

package org.faktorips.devtools.core.model.ipsproject;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.util.message.MessageList;

/**
 * The IPS object path defines where IPS objects can be found. It is the same concept as the Java
 * classpath.
 * 
 * @author Jan Ortmann
 */
public interface IIpsObjectPath {

    /**
     * Message code constant identifying the message of a validation rule. 
     */
    public final static String MSGCODE_SRC_FOLDER_ENTRY_MISSING  = "SourceFolderEntryMissing"; //$NON-NLS-1$
    
    /**
     * Returns the path' entries.
     */
    public IIpsObjectPathEntry[] getEntries();

    /**
     * Returns the source folder entries of this ips project path.
     */
    public IIpsSrcFolderEntry[] getSourceFolderEntries();

    /**
     * Returns the project reference entries of this ips project path.
     */
    public IIpsProjectRefEntry[] getProjectRefEntries();

    /**
     * Returns the ips archive entries of this ips project path.
     */
    public IIpsArchiveEntry[] getArchiveEntries();
    
    /**
     * Sets the path' entries.
     */
    public void setEntries(IIpsObjectPathEntry[] newEntries);

    /**
     * Returns the ips projects referenced by the object path.
     */
    public IIpsProject[] getReferencedIpsProjects();

    /**
     * Factory method that creates a new source folder entry and adds it to the list of entries.
     */
    public IIpsSrcFolderEntry newSourceFolderEntry(IFolder srcFolder);

    /**
     * Factory method that creates a new archiv entry and adds it to the list of entries.
     */
    public IIpsArchiveEntry newArchiveEntry(IFile archiveFile) throws CoreException;

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
     * Returns true if the output folder and base package are defined per source folder, otherwise
     * false.
     */
    public boolean isOutputDefinedPerSrcFolder();

    /**
     * Sets if the output folder and base package are defined per source folder.
     */
    public void setOutputDefinedPerSrcFolder(boolean newValue);

    /**
     * Returns the output folder for generated Java files used for all source folders.
     */
    public IFolder getOutputFolderForGeneratedJavaFiles();

    /**
     * Sets the output folder where the Java source files of all source folders are generated into.
     */
    public void setOutputFolderForGeneratedJavaFiles(IFolder outputFolder);

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
     * Returns the output folder for generated artefacts that are marked as derived. More precise
     * this folder will be marked as derived and hence all resources within are considered derived.
     * Derived artefacts are not managed by the resource management system (e.g. CVS). During the
     * clean build phase all resources in this folder will be deleted.
     */
    public IFolder getOutputFolderForDerivedSources();

    /**
     * Sets the output folder for derived sources.
     */
    public void setOutputFolderForDerivedSources(IFolder outputFolder);

    /**
     * Returns the name of the base package for generated Java source files that are considered derived.
     * 
     * @see #getOutputFolderForDerivedSources()
     */
    public String getBasePackageNameForDerivedJavaClasses();

    /**
     * Sets the name of the base package for generated Java source files that are considered derived.
     * 
     * @see #getOutputFolderForDerivedSources()
     */
    public void setBasePackageNameForDerivedJavaClasses(String name);

    /**
     * Validates the object path and returns the result as list of messages.
     */
    public MessageList validate() throws CoreException;
}
