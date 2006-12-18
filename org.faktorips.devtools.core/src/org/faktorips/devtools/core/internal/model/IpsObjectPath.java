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

package org.faktorips.devtools.core.internal.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.SystemUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.model.IIpsArchiveEntry;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsObjectPath;
import org.faktorips.devtools.core.model.IIpsObjectPathEntry;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsProjectRefEntry;
import org.faktorips.devtools.core.model.IIpsSrcFolderEntry;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.QualifiedNameType;
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
        return "IpsObjectPath" + SystemUtils.LINE_SEPARATOR  //$NON-NLS-1$
            + " " + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
            + "The IpsObjectPath defines where FaktorIPS searches for model and product definition files/objects for this project." + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
            + "Basically it is the same concept as the Java classpath.  The IpsObjectPath is defined through one or more entries." + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
            + "Currently the following entry types are supported:" + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
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
    final static String XML_TAG_NAME = "IpsObjectPath"; //$NON-NLS-1$

    private IIpsObjectPathEntry[] entries = new IIpsObjectPathEntry[0];
    private boolean outputDefinedPerSourceFolder = false;
    
    // output folder and base package for the generated Java files
    private IFolder outputFolderGenerated;
    private String basePackageGenerated = ""; //$NON-NLS-1$
    
    // output folder and base package for the extension Java files
    private IFolder outputFolderExtension;
    private String basePackageExtension = ""; //$NON-NLS-1$
    
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
    public IFolder getOutputFolderForGeneratedJavaFiles() {
        return outputFolderGenerated;
    }

    /**
     * {@inheritDoc}
     */
    public void setOutputFolderForGeneratedJavaFiles(IFolder outputFolder) {
        this.outputFolderGenerated = outputFolder;
    }
    
    /**
     * {@inheritDoc}
     */
    public IFolder[] getOutputFolders() {
        if (!outputDefinedPerSourceFolder) {
            if (outputFolderGenerated==null) {
                return new IFolder[0];
            } else {
                return new IFolder[]{outputFolderGenerated};
            }
        }

        ArrayList result = new ArrayList(entries.length);
        for (int i=0; i<entries.length; i++) {
            if (entries[i].getType()==IIpsObjectPathEntry.TYPE_SRC_FOLDER) {
                IIpsSrcFolderEntry srcEntry = (IIpsSrcFolderEntry)entries[i];
                if (srcEntry.getOutputFolderForGeneratedJavaFiles()!=null) {
                    result.add(srcEntry.getOutputFolderForGeneratedJavaFiles());
                }
            }
        }
        return (IFolder[])result.toArray(new IFolder[result.size()]);
    }

    /**
     * {@inheritDoc}
     */
    public String getBasePackageNameForGeneratedJavaClasses() {
        return basePackageGenerated;
    }

    /**
     * {@inheritDoc}
     */
    public void setBasePackageNameForGeneratedJavaClasses(String name) {
        this.basePackageGenerated = name;
    }
    
    /**
     * {@inheritDoc}
     */
    public IFolder getOutputFolderForExtensionJavaFiles() {
        return outputFolderExtension;
    }

    /**
     * {@inheritDoc}
     */
    public void setOutputFolderForExtensionJavaFiles(IFolder outputFolder) {
        outputFolderExtension = outputFolder;
    }

    /**
     * {@inheritDoc}
     */
    public String getBasePackageNameForExtensionJavaClasses() {
        return basePackageExtension;
    }

    /**
     * {@inheritDoc}
     */
    public void setBasePackageNameForExtensionJavaClasses(String name) {
        basePackageExtension = name;
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
     * Returns all objects of the given type found on the path. Returns an empty array if no
     * object is found. 
     */
    public IIpsObject[] findIpsObjects(IIpsProject project, IpsObjectType type, Set visitedEntries) throws CoreException {
        List result = new ArrayList();
        findIpsObjects(project, type, result, visitedEntries);
        return (IIpsObject[])result.toArray(new IIpsObject[result.size()]);
    }
    
    /**
     * Adds all objects of the given type found on the path to the result list.
     */
    public void findIpsObjects(IIpsProject project, IpsObjectType type, List result, Set visitedEntries) throws CoreException {
        for (int i=0; i<entries.length; i++) {
            ((IpsObjectPathEntry)entries[i]).findIpsObjects(project, type, result, visitedEntries);
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
        element.setAttribute("outputFolderGenerated", outputFolderGenerated==null?"":outputFolderGenerated.getProjectRelativePath().toString()); //$NON-NLS-1$ //$NON-NLS-2$
        element.setAttribute("basePackageGenerated", basePackageGenerated); //$NON-NLS-1$
        element.setAttribute("outputFolderExtension", outputFolderExtension==null?"":outputFolderExtension.getProjectRelativePath().toString()); //$NON-NLS-1$ //$NON-NLS-2$
        element.setAttribute("basePackageExtension", basePackageExtension); //$NON-NLS-1$
        
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
        IpsObjectPath path = new IpsObjectPath();
        path.setBasePackageNameForGeneratedJavaClasses(element.getAttribute("basePackageGenerated")); //$NON-NLS-1$
        path.setBasePackageNameForExtensionJavaClasses(element.getAttribute("basePackageExtension")); //$NON-NLS-1$
        String outputFolderPathGenerated = element.getAttribute("outputFolderGenerated"); //$NON-NLS-1$
        if (outputFolderPathGenerated.equals("")) { //$NON-NLS-1$
            path.setOutputFolderForGeneratedJavaFiles(null);
        } else {
            path.setOutputFolderForGeneratedJavaFiles(ipsProject.getProject().getFolder(new Path(outputFolderPathGenerated)));
        }
        String outputFolderPathExtension = element.getAttribute("outputFolderExtension"); //$NON-NLS-1$
        if (outputFolderPathExtension.equals("")) { //$NON-NLS-1$
            path.setOutputFolderForExtensionJavaFiles(null);
        } else {
            path.setOutputFolderForExtensionJavaFiles(ipsProject.getProject().getFolder(new Path(outputFolderPathExtension)));
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
        if (outputFolderGenerated != null) {
            list.add(validateFolder(outputFolderGenerated));
        }
        if (outputFolderExtension != null) {
            list.add(validateFolder(outputFolderExtension));
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
    public boolean detectCycle(List visitedEntries) throws CoreException {
        for (int i = 0; i < entries.length; i++) {
            if (visitedEntries.contains(entries[i])){
                return true;
            }
            visitedEntries.add(entries[i]);
            if (entries[i] instanceof IIpsProjectRefEntry){
                IpsProject refProject = (IpsProject)((IIpsProjectRefEntry)entries[i]).getReferencedIpsProject();
                if (refProject.getIpsObjectPathInternal().detectCycle(visitedEntries)){
                    return true;
                }
            }
        }
        return false;
    }
}
