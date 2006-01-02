package org.faktorips.devtools.core.internal.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsObjectPath;
import org.faktorips.devtools.core.model.IIpsObjectPathEntry;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsProjectRefEntry;
import org.faktorips.devtools.core.model.IIpsSrcFolderEntry;
import org.faktorips.devtools.core.model.QualifiedNameType;
import org.faktorips.util.ArgumentCheck;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Implementation of IpsObjectPath.
 * 
 * @author Jan Ortmann
 */
public class IpsObjectPath implements IIpsObjectPath {
    
    /**
     * Xml element name for ips object path.
     */
    final static String XML_ELEMENT = "IpsObjectPath";

    private IIpsProject ipsProject;
    private IIpsObjectPathEntry[] entries = new IIpsObjectPathEntry[0];
    private boolean outputDefinedPerSourceFolder = false;
    
    // output folder and base package for the generated Java files
    private IFolder outputFolderGenerated;
    private String basePackageGenerated = "";
    
    // output folder and base package for the extension Java files
    private IFolder outputFolderExtension;
    private String basePackageExtension = "";
    
    public IpsObjectPath(IIpsProject project) {
        ArgumentCheck.notNull(project);
        ipsProject = project;
    }

    /**
     * Overridden IMethod.
     * @see org.faktorips.devtools.core.model.IIpsObjectPath#getIpsProject()
     */
    public IIpsProject getIpsProject() {
        return ipsProject;
    }
    
    /**
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.model.IIpsObjectPath#getProjectRefEntries()
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
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.model.IIpsObjectPath#getSourceFolderEntries()
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
     * Overridden IMethod.
     * @see org.faktorips.devtools.core.model.IIpsObjectPath#getEntries()
     */
    public IIpsObjectPathEntry[] getEntries() {
        IIpsObjectPathEntry[] copy = new IIpsObjectPathEntry[entries.length];
        System.arraycopy(entries, 0, copy, 0, entries.length);
        return copy;
    }

    /**
     * Overridden IMethod.
     * @see org.faktorips.devtools.core.model.IIpsObjectPath#setEntries(org.faktorips.devtools.core.model.IIpsObjectPathEntry)
     */
    public void setEntries(IIpsObjectPathEntry[] newEntries) {
        entries = new IIpsObjectPathEntry[newEntries.length];
        System.arraycopy(newEntries, 0, entries, 0, newEntries.length);
    }

    /**
     * Overridden IMethod.
     * @see org.faktorips.devtools.core.model.IIpsObjectPath#getReferencedIpsProjects()
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
     * Overridden IMethod.
     * @see org.faktorips.devtools.core.model.IIpsObjectPath#newSourceFolderEntry()
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
     * Overridden IMethod.
     * @see org.faktorips.devtools.core.model.IIpsObjectPath#newIpsProjectRefEntry()
     */
    public IIpsProjectRefEntry newIpsProjectRefEntry(IIpsProject referencedIpsProject) {
        IIpsProjectRefEntry newEntry = new IpsProjectRefEntry(this, referencedIpsProject);
        IIpsObjectPathEntry[] newEntries = new IIpsObjectPathEntry[entries.length+1];
        System.arraycopy(entries, 0, newEntries, 0, entries.length);
        newEntries[newEntries.length-1] = newEntry;
        entries = newEntries;
        return newEntry;
    }
    
    /**
     * Overridden IMethod.
     * @see org.faktorips.devtools.core.model.IIpsObjectPath#isOutputDefinedPerSrcFolder()
     */
    public boolean isOutputDefinedPerSrcFolder() {
        return outputDefinedPerSourceFolder;
    }

    /**
     * Overridden IMethod.
     * @see org.faktorips.devtools.core.model.IIpsObjectPath#setOutputDefinedPerSrcFolder(boolean)
     */
    public void setOutputDefinedPerSrcFolder(boolean newValue) {
        outputDefinedPerSourceFolder = newValue;
    }

    /**
     * Overridden IMethod.
     * @see org.faktorips.devtools.core.model.IIpsObjectPath#getOutputFolderForGeneratedJavaFiles()
     */
    public IFolder getOutputFolderForGeneratedJavaFiles() {
        return outputFolderGenerated;
    }

    /**
     * Overridden IMethod.
     * @see org.faktorips.devtools.core.model.IIpsObjectPath#setOutputFolderForGeneratedJavaFiles(org.eclipse.core.resources.IFolder)
     */
    public void setOutputFolderForGeneratedJavaFiles(IFolder outputFolder) {
        this.outputFolderGenerated = outputFolder;
    }
    
    /**
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.model.IIpsObjectPath#getOutputFolders()
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
     * Overridden IMethod.
     * @see org.faktorips.devtools.core.model.IIpsObjectPath#getBasePackageNameForGeneratedJavaClasses()
     */
    public String getBasePackageNameForGeneratedJavaClasses() {
        return basePackageGenerated;
    }

    /**
     * Overridden IMethod.
     * @see org.faktorips.devtools.core.model.IIpsObjectPath#setBasePackageNameForGeneratedJavaClasses(java.lang.String)
     */
    public void setBasePackageNameForGeneratedJavaClasses(String name) {
        this.basePackageGenerated = name;
    }
    
    /**
     * Overridden IMethod.
     * @see org.faktorips.devtools.core.model.IIpsObjectPath#getOutputFolderForExtensionJavaFiles()
     */
    public IFolder getOutputFolderForExtensionJavaFiles() {
        return outputFolderExtension;
    }

    /**
     * Overridden IMethod.
     * @see org.faktorips.devtools.core.model.IIpsObjectPath#setOutputFolderForExtensionJavaFiles(org.eclipse.core.resources.IFolder)
     */
    public void setOutputFolderForExtensionJavaFiles(IFolder outputFolder) {
        outputFolderExtension = outputFolder;
    }

    /**
     * Overridden IMethod.
     * @see org.faktorips.devtools.core.model.IIpsObjectPath#getBasePackageNameForExtensionJavaClasses()
     */
    public String getBasePackageNameForExtensionJavaClasses() {
        return basePackageExtension;
    }

    /**
     * Overridden IMethod.
     * @see org.faktorips.devtools.core.model.IIpsObjectPath#setBasePackageNameForExtensionJavaClasses(java.lang.String)
     */
    public void setBasePackageNameForExtensionJavaClasses(String name) {
        basePackageExtension = name;
    }
    
    /**
     * Returns the first object with the indicated type and qualified name found
     * on the path. Returns <code>null</code> if no such object is found.
     */
    public IIpsObject findIpsObject(IpsObjectType type, String qualifiedName) throws CoreException {
        for (int i=0; i<entries.length; i++) {
            IIpsObject object = ((IpsObjectPathEntry)entries[i]).findIpsObject(type, qualifiedName);
            if (object!=null) {
                return object;
            }
        }
        return null;
    }

    /**
     * Returns the first object with the indicated qualified name tpye found
     * on the path. Returns <code>null</code> if no such object is found.
     */
    public IIpsObject findIpsObject(QualifiedNameType nameType) throws CoreException {
        for (int i=0; i<entries.length; i++) {
            IIpsObject object = ((IpsObjectPathEntry)entries[i]).findIpsObject(nameType);
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
    void findIpsObjectsStartingWith(IpsObjectType type, String prefix, boolean ignoreCase, List result) throws CoreException {
        for (int i=0; i<entries.length; i++) {
            ((IpsObjectPathEntry)entries[i]).findIpsObjectsStartingWith(type, prefix, ignoreCase, result);
        }
    }
    
    /**
     * Returns all objects of the given type found on the path. Returns an empty array if no
     * object is found. 
     */
    public IIpsObject[] findIpsObjects(IpsObjectType type) throws CoreException {
        List result = new ArrayList();
        findIpsObjects(type, result);
        return (IIpsObject[])result.toArray(new IIpsObject[result.size()]);
    }
    
    /**
     * Adds all objects of the given type found on the path to the result list.
     */
    public void findIpsObjects(IpsObjectType type, List result) throws CoreException {
        for (int i=0; i<entries.length; i++) {
            ((IpsObjectPathEntry)entries[i]).findIpsObjects(type, result);
        }
    }
    
    /**
     * Returns an xml representation of the object path.
     * 
     * @param doc The xml document used to create new elements.
     */
    public Element toXml(Document doc) {
        Element element = doc.createElement(XML_ELEMENT);
        element.setAttribute("outputDefinedPerSrcFolder", "" + outputDefinedPerSourceFolder);
        element.setAttribute("outputFolderGenerated", outputFolderGenerated==null?"":outputFolderGenerated.getProjectRelativePath().toString());
        element.setAttribute("basePackageGenerated", basePackageGenerated);
        element.setAttribute("outputFolderExtension", outputFolderExtension==null?"":outputFolderExtension.getProjectRelativePath().toString());
        element.setAttribute("basePackageExtension", basePackageExtension);
        
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
        path.setBasePackageNameForGeneratedJavaClasses(element.getAttribute("basePackageGenerated"));
        path.setBasePackageNameForExtensionJavaClasses(element.getAttribute("basePackageExtension"));
        String outputFolderPathGenerated = element.getAttribute("outputFolderGenerated");
        if (outputFolderPathGenerated.equals("")) {
            path.setOutputFolderForGeneratedJavaFiles(null);
        } else {
            path.setOutputFolderForGeneratedJavaFiles(ipsProject.getProject().getFolder(new Path(outputFolderPathGenerated)));
        }
        String outputFolderPathExtension = element.getAttribute("outputFolderExtension");
        if (outputFolderPathExtension.equals("")) {
            path.setOutputFolderForExtensionJavaFiles(null);
        } else {
            path.setOutputFolderForExtensionJavaFiles(ipsProject.getProject().getFolder(new Path(outputFolderPathExtension)));
        }
        path.setOutputDefinedPerSrcFolder(Boolean.valueOf(element.getAttribute("outputDefinedPerSrcFolder")).booleanValue());
        
        // init entries 
        NodeList nl = element.getElementsByTagName(IpsObjectPathEntry.XML_ELEMENT);
        IIpsObjectPathEntry[] entries = new IIpsObjectPathEntry[nl.getLength()];
        for (int i=0; i<nl.getLength(); i++) {
            Element entryElement = (Element)nl.item(i);
            entries[i] = IpsObjectPathEntry.createFromXml(path, entryElement);
        }
        path.setEntries(entries);
        return path;
    }

}
