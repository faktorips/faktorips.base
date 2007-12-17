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

package org.faktorips.devtools.core.internal.model.ipsproject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.SystemUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.core.model.ipsproject.IIpsArchiveEntry;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPathEntry;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectRefEntry;
import org.faktorips.devtools.core.model.ipsproject.IIpsSrcFolderEntry;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Implementation of IIpsObjectPath.
 * 
 * @author Jan Ortmann
 */
public class IpsObjectPath implements IIpsObjectPath {
    
    
    /**
     * Returns a description of the xml format.
     */
    public final static String getXmlFormatDescription() {
        return  XML_TAG_NAME + " : "  //$NON-NLS-1$
                + SystemUtils.LINE_SEPARATOR 
                + "The IpsObjectPath defines where Faktor-IPS searches for model and product definition files/objects for this project."  //$NON-NLS-1$
                + SystemUtils.LINE_SEPARATOR 
                + "Basically it is the same concept as the Java classpath." //$NON-NLS-1$
                + SystemUtils.LINE_SEPARATOR  
                + "<" + XML_TAG_NAME + " "  //$NON-NLS-1$ //$NON-NLS-2$
                + SystemUtils.LINE_SEPARATOR  
                + " outputDefinedPerSourceFolder            Boolean flag that indicates if there are separate output folders for each source folder" //$NON-NLS-1$
                + SystemUtils.LINE_SEPARATOR
                + " outputFolderMergableSources             The output folder for the generated artefacts that will not be deleted during a " + //$NON-NLS-1$
                                                            "clean build cycle but may be merged with the generated content during a build cycle" //$NON-NLS-1$
                + SystemUtils.LINE_SEPARATOR
                + " basePackageMergable                     The base package for generated and merable java files" //$NON-NLS-1$
                + SystemUtils.LINE_SEPARATOR
                + " outputFolderDerivedSources              The output folder for the generated artefacts that will be deleted during a clean build " + //$NON-NLS-1$
                                                            "cycle and newly generated during each build cycle" //$NON-NLS-1$
                + SystemUtils.LINE_SEPARATOR
                + " basePackageDerived                      The base package for generated derived java files" //$NON-NLS-1$
                + SystemUtils.LINE_SEPARATOR 
                + "The IpsObjectPath is defined through one or more entries."  //$NON-NLS-1$
                + SystemUtils.LINE_SEPARATOR 
                + "Currently the following entry types are supported:"  //$NON-NLS-1$
                + SystemUtils.LINE_SEPARATOR 
                + " " + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + IpsSrcFolderEntry.getXmlFormatDescription() + SystemUtils.LINE_SEPARATOR
                + " "+ SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + IpsProjectRefEntry.getXmlFormatDescription()
                + " "+ SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + IpsArchiveEntry.getXmlFormatDescription();
    }
    
    
    /**
     * Xml element name for ips object path.
     */
    public final static String XML_TAG_NAME = "IpsObjectPath"; //$NON-NLS-1$

    private IIpsObjectPathEntry[] entries = new IIpsObjectPathEntry[0];
    private boolean outputDefinedPerSourceFolder = false;
    
    // output folder and base package for the generated Java files
    private IFolder outputFolderMergableSources;
    private String basePackageMergable = ""; //$NON-NLS-1$
    
    //output folder for generated sources that are marked as derived, more precise this output
    //folder will be marked als derived and hence all members of it will be derived
    //derived resources will not be managed by the resource management system and will
    // output folder and base package for the extension Java files
    private IFolder outputFolderDerivedSources;
    
    private String basePackageDerived = ""; //$NON-NLS-1$
    
    private IIpsProject ipsProject;
    
    
    public IpsObjectPath(IIpsProject ipsProject){
        ArgumentCheck.notNull(ipsProject, this);
        this.ipsProject = ipsProject;
    }
    
    public IIpsProject getIpsProject(){
        return ipsProject;
    }
    
    /**
     * Returns the index of the given entry.
     */
    public int getIndex(IpsObjectPathEntry entry) {
        for (int i = 0; i < entries.length; i++) {
            if (entries[i].equals(entry)) {
                return i;
            }
        }
        throw new IllegalArgumentException("Can't find entry " + entry + " in path " + this);  //$NON-NLS-1$  //$NON-NLS-2$
    }
    
    /**
     * {@inheritDoc}
     */
    public IIpsProjectRefEntry[] getProjectRefEntries() {
        ArrayList projectRefEntries = new ArrayList();
        for (int i = 0; i < entries.length; i++) {
            if(entries[i].getType().equals(IIpsObjectPathEntry.TYPE_PROJECT_REFERENCE)){
                projectRefEntries.add(entries[i]);
            }
        }
        return (IIpsProjectRefEntry[])projectRefEntries.toArray(new IIpsProjectRefEntry[projectRefEntries.size()]);
    }
    
    /**
     * {@inheritDoc}
     */
    public IIpsSrcFolderEntry[] getSourceFolderEntries() {
        ArrayList projectRefEntries = new ArrayList();
        for (int i = 0; i < entries.length; i++) {
            if(entries[i].getType().equals(IIpsObjectPathEntry.TYPE_SRC_FOLDER)){
                projectRefEntries.add(entries[i]);
            }
        }
        return (IIpsSrcFolderEntry[])projectRefEntries.toArray(new IIpsSrcFolderEntry[projectRefEntries.size()]);
    }
    
    /**
     * 
     * {@inheritDoc}
     */
    public IIpsArchiveEntry[] getArchiveEntries() {
        ArrayList archiveEntries = new ArrayList();
        for (int i = 0; i < entries.length; i++) {
            if(entries[i].getType().equals(IIpsObjectPathEntry.TYPE_ARCHIVE)){
                archiveEntries.add(entries[i]);
            }
        }
        return (IIpsArchiveEntry[])archiveEntries.toArray(new IIpsArchiveEntry[archiveEntries.size()]);
    }

    /**
     * {@inheritDoc}
     */
    public IIpsObjectPathEntry[] getEntries() {
        IIpsObjectPathEntry[] copy = new IIpsObjectPathEntry[entries.length];
        System.arraycopy(entries, 0, copy, 0, entries.length);
        return copy;
    }

    /**
     * {@inheritDoc}
     */
    public void setEntries(IIpsObjectPathEntry[] newEntries) {
        entries = new IIpsObjectPathEntry[newEntries.length];
        System.arraycopy(newEntries, 0, entries, 0, newEntries.length);
    }

    /**
     * {@inheritDoc}
     */
    public IIpsProject[] getReferencedIpsProjects() {
        List projects = new ArrayList();
        for (int i=0; i<entries.length; i++) {
            if (entries[i].getType().equals(IIpsObjectPathEntry.TYPE_PROJECT_REFERENCE)) {
                projects.add(((IIpsProjectRefEntry)entries[i]).getReferencedIpsProject());
            }
        }
        return (IIpsProject[])projects.toArray(new IIpsProject[projects.size()]);
    }
    
    /**
     * {@inheritDoc}
     */
    public IIpsSrcFolderEntry newSourceFolderEntry(IFolder srcFolder) {
       IIpsSrcFolderEntry newEntry = new IpsSrcFolderEntry(this, srcFolder);
       IIpsObjectPathEntry[] newEntries = new IIpsObjectPathEntry[entries.length+1];
       System.arraycopy(entries, 0, newEntries, 0, entries.length);
       newEntries[newEntries.length-1] = newEntry;
       entries = newEntries;
       return newEntry;
    }
    
    /**
     * {@inheritDoc}
     */
    public IIpsArchiveEntry newArchiveEntry(IFile archiveFile) throws CoreException {
        IIpsArchiveEntry newEntry = new IpsArchiveEntry(this);
        newEntry.setArchiveFile(archiveFile);
        IIpsObjectPathEntry[] newEntries = new IIpsObjectPathEntry[entries.length+1];
        System.arraycopy(entries, 0, newEntries, 0, entries.length);
        newEntries[newEntries.length-1] = newEntry;
        entries = newEntries;
        return newEntry;
    }

    /**
     * {@inheritDoc}
     */
    public IIpsProjectRefEntry newIpsProjectRefEntry(IIpsProject referencedIpsProject) {
    	if(containsProjectRefEntry(referencedIpsProject)){
    		for (int i = 0; i < entries.length; i++) {
    			IIpsObjectPathEntry entry = entries[i];
    			if(entry instanceof IpsProjectRefEntry) {
    				IpsProjectRefEntry ref = (IpsProjectRefEntry) entry;
    				if(ref.getReferencedIpsProject().equals(referencedIpsProject))
    					return ref;
    			}
    		}
    	}
        IIpsProjectRefEntry newEntry = new IpsProjectRefEntry(this, referencedIpsProject);
        IIpsObjectPathEntry[] newEntries = new IIpsObjectPathEntry[entries.length+1];
        System.arraycopy(entries, 0, newEntries, 0, entries.length);
        newEntries[newEntries.length-1] = newEntry;
        entries = newEntries;
        return newEntry;
    }

    /**
     * {@inheritDoc}
     */
	public boolean containsProjectRefEntry(IIpsProject ipsProject){
		for (int i = 0; i < entries.length; i++) {
			IIpsObjectPathEntry entry = entries[i];
			if(entry instanceof IpsProjectRefEntry) {
				IpsProjectRefEntry ref = (IpsProjectRefEntry) entry;
				if(ref.getReferencedIpsProject().equals(ipsProject))
					return true;
			}
		}
		return false;
	}

    /**
     * {@inheritDoc}
     */
	public void removeProjectRefEntry(IIpsProject ipsProject){
		for (int i = 0; i < entries.length; i++) {
			IIpsObjectPathEntry entry = entries[i];
			if(entry instanceof IpsProjectRefEntry) {
				IpsProjectRefEntry ref = (IpsProjectRefEntry) entry;
				if(ref.getReferencedIpsProject().equals(ipsProject)){
			        IIpsObjectPathEntry[] newEntries = new IIpsObjectPathEntry[entries.length-1];
			        System.arraycopy(entries, 0, newEntries, 0, i);
			        System.arraycopy(entries, i+1, newEntries, i, entries.length-i-1);
			        entries = newEntries;
				}
			}
		}
	}
	
    /**
     * {@inheritDoc}
     */
    public boolean isOutputDefinedPerSrcFolder() {
        return outputDefinedPerSourceFolder;
    }

    /**
     * {@inheritDoc}
     */
    public void setOutputDefinedPerSrcFolder(boolean newValue) {
        outputDefinedPerSourceFolder = newValue;
    }

    /**
     * {@inheritDoc}
     */
    public IFolder getOutputFolderForMergableSources() {
        return outputFolderMergableSources;
    }

    /**
     * {@inheritDoc}
     */
    public void setOutputFolderForMergableSources(IFolder outputFolder) {
        this.outputFolderMergableSources = outputFolder;
    }
    
    /**
     * {@inheritDoc}
     */
    public IFolder[] getOutputFolders() {
        if (!outputDefinedPerSourceFolder) {
            if (outputFolderMergableSources==null) {
                return new IFolder[0];
            } else {
                return new IFolder[]{outputFolderMergableSources};
            }
        }

        ArrayList result = new ArrayList(entries.length);
        for (int i=0; i<entries.length; i++) {
            if (entries[i].getType()==IIpsObjectPathEntry.TYPE_SRC_FOLDER) {
                IIpsSrcFolderEntry srcEntry = (IIpsSrcFolderEntry)entries[i];
                if (srcEntry.getOutputFolderForMergableJavaFiles()!=null) {
                    result.add(srcEntry.getOutputFolderForMergableJavaFiles());
                }
            }
        }
        return (IFolder[])result.toArray(new IFolder[result.size()]);
    }

    /**
     * {@inheritDoc}
     */
    public String getBasePackageNameForMergableJavaClasses() {
        return basePackageMergable;
    }

    /**
     * {@inheritDoc}
     */
    public void setBasePackageNameForMergableJavaClasses(String name) {
        this.basePackageMergable = name;
    }
    
    /**
     * {@inheritDoc}
     */
    public IFolder getOutputFolderForDerivedSources() {
        return outputFolderDerivedSources;
    }

    /**
     * {@inheritDoc}
     */
    public void setOutputFolderForDerivedSources(IFolder outputFolder) {
        outputFolderDerivedSources = outputFolder;
    }

    /**
     * {@inheritDoc}
     */
    public String getBasePackageNameForDerivedJavaClasses() {
        return basePackageDerived;
    }

    /**
     * {@inheritDoc}
     */
    public void setBasePackageNameForDerivedJavaClasses(String name) {
        basePackageDerived = name;
    }
    
    /**
     * Returns the first object with the indicated type and qualified name found
     * on the path. Returns <code>null</code> if no such object is found.
     */
    public IIpsObject findIpsObject(IIpsProject project, IpsObjectType type, String qualifiedName, Set visitedEntries) throws CoreException {
        return findIpsObject(project, new QualifiedNameType(qualifiedName, type), visitedEntries);
    }

    /**
     * Returns the first object with the indicated qualified name tpye found
     * on the path. Returns <code>null</code> if no such object is found.
     */
    public IIpsObject findIpsObject(IIpsProject project, QualifiedNameType nameType, Set visitedEntries) throws CoreException {
        for (int i=0; i<entries.length; i++) {
            IIpsObject object = ((IpsObjectPathEntry)entries[i]).findIpsObject(project, nameType, visitedEntries);
            if (object!=null) {
                return object;
            }
        }
        return null;
    }

    /**
     * Returns the first ips source file with the indicated qualified name tpye found
     * on the path. Returns <code>null</code> if no such object is found.
     */
    public IIpsSrcFile findIpsSrcFile(IIpsProject project, IpsObjectType type, String qualifiedName, Set visitedEntries) throws CoreException {
        return findIpsSrcFile(project, new QualifiedNameType(qualifiedName, type), visitedEntries);
    }

    /**
     * Returns the first ips source file with the indicated qualified name tpye found
     * on the path. Returns <code>null</code> if no such object is found.
     */
    public IIpsSrcFile findIpsSrcFile(IIpsProject project, QualifiedNameType nameType, Set visitedEntries) throws CoreException {
        for (int i=0; i<entries.length; i++) {
            IIpsSrcFile ipsSrcFile = ((IpsObjectPathEntry)entries[i]).findIpsSrcFile(project, nameType, visitedEntries);
            if (ipsSrcFile!=null) {
                return ipsSrcFile;
            }
        }
        return null;
    }
    
    /**
     * Searches all objects of the given type starting with the given prefix found on the path and adds
     * them to the given result list.
     * 
     * @throws CoreException if an error occurs while searching for the objects. 
     */
    public void findIpsObjectsStartingWith(IIpsProject project, IpsObjectType type, String prefix, boolean ignoreCase, List result, Set visitedEntries) throws CoreException {
        for (int i=0; i<entries.length; i++) {
            ((IpsObjectPathEntry)entries[i]).findIpsObjectsStartingWith(project, type, prefix, ignoreCase, result, visitedEntries);
        }
    }
    
    /**
     * Searches all isp src files of the given type starting with the given prefix found on the path and adds
     * them to the given result list.
     * 
     * @throws CoreException if an error occurs while searching for the source files. 
     */    
    public void findIpsSrcFilesStartingWith(IIpsProject project, IpsObjectType type, String prefix, boolean ignoreCase, List result, Set visitedEntries) throws CoreException {
        for (int i=0; i<entries.length; i++) {
            ((IpsObjectPathEntry)entries[i]).findIpsSrcFilesStartingWith(project, type, prefix, ignoreCase, result, visitedEntries);
        }
    }
    
    /**
     * Returns all objects of the given type found on the path. Returns an empty array if no
     * object is found. 
     * 
     * @deprecated use IIpsObjectPath#findIpsSrcFiles(IIpsProject project, IpsObjectType type, Set visitedEntries) due to better performance  
     */
    public IIpsObject[] findIpsObjects(IIpsProject project, IpsObjectType type, Set visitedEntries) throws CoreException {
        List result = new ArrayList();
        findIpsObjects(project, type, result, visitedEntries);
        return (IIpsObject[])result.toArray(new IIpsObject[result.size()]);
    }
    
    /**
     * Returns all ips source files of the given type found on the path. Returns an empty array if no
     * object is found. 
     */    
    public IIpsSrcFile[] findIpsSrcFiles(IIpsProject project, IpsObjectType type, Set visitedEntries) throws CoreException {
        List result = new ArrayList();
        findIpsSrcFiles(project, type, result, visitedEntries);
        return (IIpsSrcFile[])result.toArray(new IIpsSrcFile[result.size()]);
    }
    
    /**
     * Adds all objects of the given type found on the path to the result list.
     * 
     * @deprecated use IIpsObjectPath#findIpsSrcFiles(IIpsProject project, IpsObjectType type, List result, Set visitedEntries) due to better performance
     */
    public void findIpsObjects(IIpsProject project, IpsObjectType type, List result, Set visitedEntries) throws CoreException {
        for (int i=0; i<entries.length; i++) {
            ((IpsObjectPathEntry)entries[i]).findIpsObjects(project, type, result, visitedEntries);
        }
    }

    /**
     * Adds all objects found on the path to the result list.
     */
    public void findAllIpsObjects(IIpsProject project, List result, Set visitedEntries) throws CoreException {
        for (int i=0; i<entries.length; i++) {
            ((IpsObjectPathEntry)entries[i]).findIpsObjects(project, result, visitedEntries);
        }
    }
    
    /**
     * Adds all ips source files of the given type found on the path to the result list.
     */
    public void findIpsSrcFiles(IIpsProject project, IpsObjectType type, List result, Set visitedEntries) throws CoreException {
        for (int i=0; i<entries.length; i++) {
            ((IpsObjectPathEntry)entries[i]).findIpsSrcFiles(project, type, result, visitedEntries);
        }
    }
    
    /**
     * Adds all objects found in <code>IpsSrcFolderEntry</code>s on the path to the result list.
     */
    public void findAllIpsObjectsOfSrcFolderEntries(IIpsProject project, List result, Set visitedEntries) throws CoreException {
        for (int i=0; i<entries.length; i++) {
            if(entries[i].getType().equals(IIpsObjectPathEntry.TYPE_SRC_FOLDER)){
                ((IpsObjectPathEntry)entries[i]).findIpsObjects(project, result, visitedEntries);
            }
        }
    }
        
    /**
     * Returns an xml representation of the object path.
     * 
     * @param doc The xml document used to create new elements.
     */
    public Element toXml(Document doc) {
        Element element = doc.createElement(XML_TAG_NAME);
        element.setAttribute("outputDefinedPerSrcFolder", "" + outputDefinedPerSourceFolder); //$NON-NLS-1$ //$NON-NLS-2$
        element.setAttribute("outputFolderMergableSources", outputFolderMergableSources==null?"":outputFolderMergableSources.getProjectRelativePath().toString()); //$NON-NLS-1$ //$NON-NLS-2$
        element.setAttribute("basePackageMergable", basePackageMergable); //$NON-NLS-1$
        element.setAttribute("outputFolderDerivedSources", outputFolderDerivedSources==null?"":outputFolderDerivedSources.getProjectRelativePath().toString()); //$NON-NLS-1$ //$NON-NLS-2$
        element.setAttribute("basePackageDerived", basePackageDerived); //$NON-NLS-1$
        // entries
        for (int i=0; i<entries.length; i++) {
            Element entryElement = ((IpsObjectPathEntry)entries[i]).toXml(doc);
            element.appendChild(entryElement);
        }
        
        return element;
    }
    
    /**
     * Creates the object path from the data stored in the xml element.
     */
    public final static IIpsObjectPath createFromXml(IIpsProject ipsProject, Element element) {
        IpsObjectPath path = new IpsObjectPath(ipsProject);
        path.setBasePackageNameForMergableJavaClasses(element.getAttribute("basePackageMergable")); //$NON-NLS-1$
        path.setBasePackageNameForDerivedJavaClasses(element.getAttribute("basePackageDerived")); //$NON-NLS-1$
        String outputFolderMergedSourcesString = element.getAttribute("outputFolderMergableSources"); //$NON-NLS-1$
        if (outputFolderMergedSourcesString.equals("")) { //$NON-NLS-1$
            path.setOutputFolderForMergableSources(null);
        } else {
            path.setOutputFolderForMergableSources(ipsProject.getProject().getFolder(new Path(outputFolderMergedSourcesString)));
        }
        String outputFolderDerivedSourcesString = element.getAttribute("outputFolderDerivedSources"); //$NON-NLS-1$
        if (outputFolderDerivedSourcesString.equals("")) { //$NON-NLS-1$
            path.setOutputFolderForDerivedSources(null);
        } else {
            path.setOutputFolderForDerivedSources(ipsProject.getProject().getFolder(new Path(outputFolderDerivedSourcesString)));
        }
        path.setOutputDefinedPerSrcFolder(Boolean.valueOf(element.getAttribute("outputDefinedPerSrcFolder")).booleanValue()); //$NON-NLS-1$
        
        // init entries 
        NodeList nl = element.getElementsByTagName(IpsObjectPathEntry.XML_ELEMENT);
        IIpsObjectPathEntry[] entries = new IIpsObjectPathEntry[nl.getLength()];
        for (int i=0; i<nl.getLength(); i++) {
            Element entryElement = (Element)nl.item(i);
            entries[i] = IpsObjectPathEntry.createFromXml(path, entryElement, ipsProject.getProject());
        }
        path.setEntries(entries);
        return path;
    }

    /**
     * {@inheritDoc}
     */
    public MessageList validate() throws CoreException {
        MessageList list = new MessageList();
        if(!isOutputDefinedPerSrcFolder()){
            if (outputFolderMergableSources == null) {
                list.add(new Message(MSGCODE_MERGABLE_OUTPUT_FOLDER_NOT_SPECIFIED, NLS.bind(Messages.IpsObjectPath_msgOutputFolderMergableMissing, getIpsProject()), Message.ERROR));
            } else {
                list.add(validateFolder(outputFolderMergableSources));
            }
            if (outputFolderDerivedSources == null) {
                list.add(new Message(MSGCODE_DERIVED_OUTPUT_FOLDER_NOT_SPECIFIED, NLS.bind(Messages.IpsObjectPath_msgOutputFolderDerivedMissing, getIpsProject()), Message.ERROR));
            } else {
                list.add(validateFolder(outputFolderDerivedSources));
            }
        }
        IIpsSrcFolderEntry[] srcEntries = getSourceFolderEntries();
        if(srcEntries.length == 0){
            list.add(new Message(MSGCODE_SRC_FOLDER_ENTRY_MISSING, Messages.IpsObjectPath_srcfolderentrymissing, Message.ERROR)); //$NON-NLS-1$
        }
        IIpsObjectPathEntry[] objectPathEntries = getEntries();
        for (int i = 0; i < objectPathEntries.length; i++) {
            MessageList ml = objectPathEntries[i].validate();
            list.add(ml);
        }
        
        return list;
    }
    
    /*
     * Validate that the given folder exists.
     */
    private MessageList validateFolder(IFolder folder) {
        MessageList result = new MessageList();
        if (!folder.exists()){
            String text = NLS.bind(Messages.IpsSrcFolderEntry_msgMissingFolder, folder.getName());
            Message msg = new Message(IIpsObjectPathEntry.MSGCODE_MISSING_FOLDER, text, Message.ERROR, this);
            result.add(msg);
        }
        return result;
    }
    
    /**
     * Check if there is a cycle inside the object path. All IIpsProjectRefEntries will be check if
     * there is a cycle in the ips object path enties of all referenced projects.
     * Returns <code>true</code> if a cycle was detected. Returns <code>false</code> if there is
     * no cycle in the ips object path.
     * 
     * @throws CoreException If an error occurs while resolving the object path entries.
     */
    public boolean detectCycle(IIpsProject project) throws CoreException {
        return detectCycleInternal(project, new HashSet());
    }

    public boolean detectCycleInternal(IIpsProject project, Set visitedEntries) throws CoreException {
        if (visitedEntries.contains(this)) {
            return false;
        }
        visitedEntries.add(this);

        for (int i = 0; i < entries.length; i++) {
            if (entries[i] instanceof IIpsProjectRefEntry) {
                IpsProject refProject = (IpsProject)((IIpsProjectRefEntry)entries[i]).getReferencedIpsProject();
                if (project.equals(refProject)) {
                    return true;
                }
                if (refProject.getIpsObjectPathInternal().detectCycleInternal(project, visitedEntries)) {
                    return true;
                }
            }
        }
        return false;
    }
}
