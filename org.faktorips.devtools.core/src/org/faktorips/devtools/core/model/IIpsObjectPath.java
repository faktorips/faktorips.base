/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.model;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.util.message.MessageList;

/**
 * The IPS object path defines where IPS objects can be found. It is the same concept as the Java classpath.
 * 
 * @author Jan Ortmann
 */
public interface IIpsObjectPath {

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
     * Returns true if the output folder and base package are defined per source folder, otherwise false.
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
     * Returns the name of the base package for the generated Java source files. All generated Java types
     * are contained in this package or one of the child packages.
     */
    public String getBasePackageNameForGeneratedJavaClasses();

    /**
     * Sets the name of the base package for the generated Java source files. All generated Java types
     * are contained in this package or one of the child packages.
     */
    public void setBasePackageNameForGeneratedJavaClasses(String name);
    
    /**
     * Returns the output folder for extension Java files used for all source folders.
     * Extension Java files are the files where the developer adds it's own code. 
     */
    public IFolder getOutputFolderForExtensionJavaFiles();
    
    /**
     * Sets the output folder where the extenstion Java source files of all source folders are generated into.
     * Extension Java files are the files where the developer adds it's own code. 
     */
    public void setOutputFolderForExtensionJavaFiles(IFolder outputFolder);
    
    /**
     * Returns the name of the base package for the extension Java source files. All generated Java types
     * are contained in this package or one of the child packages.
     * Extension Java files are the files where the developer adds it's own code.
     */
    public String getBasePackageNameForExtensionJavaClasses();

    /**
     * Sets the name of the base package for the generated Java source files. All generated Java types
     * are contained in this package or one of the child packages.
     * Extension Java files are the files where the developer adds it's own code.
     */
    public void setBasePackageNameForExtensionJavaClasses(String name);
    
    /**
     * Validates the object path and returns the result as list of messages.
     */
    public MessageList validate() throws CoreException;    
}
