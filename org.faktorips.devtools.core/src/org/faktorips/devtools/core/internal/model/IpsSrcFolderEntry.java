package org.faktorips.devtools.core.internal.model;

import java.util.List;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsSrcFolderEntry;
import org.faktorips.devtools.core.model.QualifiedNameType;
import org.faktorips.util.ArgumentCheck;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Implementation of IpsSrcFolderEntry.
 * 
 * @author Jan Ortmann
 */
public class IpsSrcFolderEntry extends IpsObjectPathEntry implements IIpsSrcFolderEntry {

    // the folder containg the ips objects
    private IFolder sourceFolder;

    // the output folder containing the generated Java files.
    private IFolder outputFolderGenerated;

    // the name of the base package containing the generated Java files.
    private String basePackageGenerated;

    // the output folder containing the Java files where the developer adds it's own code.
    private IFolder outputFolderExtension;

    // the name of the base package containing the Java files where the developer adds it's own code.
    private String basePackageExtension;

    IpsSrcFolderEntry(IpsObjectPath path) {
        super(path);
    }

    IpsSrcFolderEntry(IpsObjectPath path, IFolder sourceFolder) {
        super(path);
        ArgumentCheck.notNull(sourceFolder);
        this.sourceFolder = sourceFolder;
    }

    /**
     * Overridden IMethod.
     * 
     * @see org.faktorips.devtools.core.model.IIpsObjectPathEntry#getType()
     */
    public String getType() {
        return TYPE_SRC_FOLDER;
    }

    /**
     * Overridden IMethod.
     * 
     * @see org.faktorips.devtools.core.model.IIpsSrcFolderEntry#getSourceFolder()
     */
    public IFolder getSourceFolder() {
        return sourceFolder;
    }

    /**
     * Overridden IMethod.
     * 
     * @see org.faktorips.devtools.core.model.IIpsSrcFolderEntry#getIpsPackageFragmentRoot()
     */
    public IIpsPackageFragmentRoot getIpsPackageFragmentRoot() {
        return getIpsObjectPath().getIpsProject().getIpsPackageFragmentRoot(sourceFolder.getName());
    }

    /**
     * Overridden IMethod.
     * 
     * @see org.faktorips.devtools.core.model.IIpsSrcFolderEntry#getOutputFolderForGeneratedJavaFiles()
     */
    public IFolder getOutputFolderForGeneratedJavaFiles() {
        if (getIpsObjectPath().isOutputDefinedPerSrcFolder()) {
            return outputFolderGenerated;
        }
        return getIpsObjectPath().getOutputFolderForGeneratedJavaFiles();
    }

    /**
     * Overridden IMethod.
     * 
     * @see org.faktorips.devtools.core.model.IIpsSrcFolderEntry#getSpecificOutputFolderForGeneratedJavaFiles()
     */
    public IFolder getSpecificOutputFolderForGeneratedJavaFiles() {
        return outputFolderGenerated;
    }

    /**
     * Overridden IMethod.
     * 
     * @see org.faktorips.devtools.core.model.IIpsSrcFolderEntry#setSpecificOutputFolderForGeneratedJavaFiles(org.eclipse.core.resources.IFolder)
     */
    public void setSpecificOutputFolderForGeneratedJavaFiles(IFolder outputFolder) {
        this.outputFolderGenerated = outputFolder;
    }

    /**
     * Overridden IMethod.
     * 
     * @see org.faktorips.devtools.core.model.IIpsSrcFolderEntry#getBasePackageNameForGeneratedJavaClasses()
     */
    public String getBasePackageNameForGeneratedJavaClasses() {
        if (getIpsObjectPath().isOutputDefinedPerSrcFolder()) {
            return basePackageGenerated;
        }
        return getIpsObjectPath().getBasePackageNameForGeneratedJavaClasses();
    }

    /**
     * Overridden IMethod.
     * 
     * @see org.faktorips.devtools.core.model.IIpsSrcFolderEntry#getSpecificBasePackageNameForGeneratedJavaClasses()
     */
    public String getSpecificBasePackageNameForGeneratedJavaClasses() {
        return basePackageGenerated;
    }

    /**
     * Overridden IMethod.
     * 
     * @see org.faktorips.devtools.core.model.IIpsSrcFolderEntry#setSpecificBasePackageNameForGeneratedJavaClasses(java.lang.String)
     */
    public void setSpecificBasePackageNameForGeneratedJavaClasses(String name) {
        this.basePackageGenerated = name;
    }

    /**
     * Overridden IMethod.
     * 
     * @see org.faktorips.devtools.core.model.IIpsSrcFolderEntry#getOutputFolderForExtensionJavaFiles()
     */
    public IFolder getOutputFolderForExtensionJavaFiles() {
        if (getIpsObjectPath().isOutputDefinedPerSrcFolder()) {
            return outputFolderExtension;
        }
        return getIpsObjectPath().getOutputFolderForExtensionJavaFiles();
    }

    /**
     * Overridden IMethod.
     * 
     * @see org.faktorips.devtools.core.model.IIpsSrcFolderEntry#getSpecificOutputFolderForExtensionJavaFiles()
     */
    public IFolder getSpecificOutputFolderForExtensionJavaFiles() {
        return outputFolderExtension;
    }

    /**
     * Overridden IMethod.
     * 
     * @see org.faktorips.devtools.core.model.IIpsSrcFolderEntry#setSpecificOutputFolderForExtensionJavaFiles(org.eclipse.core.resources.IFolder)
     */
    public void setSpecificOutputFolderForExtensionJavaFiles(IFolder outputFolder) {
        outputFolderExtension = outputFolder;
    }

    /**
     * Overridden IMethod.
     * 
     * @see org.faktorips.devtools.core.model.IIpsSrcFolderEntry#getBasePackageNameForExtensionJavaClasses()
     */
    public String getBasePackageNameForExtensionJavaClasses() {
        if (getIpsObjectPath().isOutputDefinedPerSrcFolder()) {
            return basePackageExtension;
        }
        return getIpsObjectPath().getBasePackageNameForExtensionJavaClasses();
    }

    /**
     * Overridden IMethod.
     * 
     * @see org.faktorips.devtools.core.model.IIpsSrcFolderEntry#getSpecificBasePackageNameForExtensionJavaClasses()
     */
    public String getSpecificBasePackageNameForExtensionJavaClasses() {
        return basePackageExtension;
    }

    /**
     * Overridden IMethod.
     * 
     * @see org.faktorips.devtools.core.model.IIpsSrcFolderEntry#setSpecificBasePackageNameForExtensionJavaClasses(java.lang.String)
     */
    public void setSpecificBasePackageNameForExtensionJavaClasses(String name) {
        basePackageExtension = name;
    }

    /**
     * Overridden IMethod.
     * 
     * @see org.faktorips.devtools.core.internal.model.IpsObjectPathEntry#findIpsObject(org.faktorips.devtools.core.model.IpsObjectType,
     *      java.lang.String)
     */
    public IIpsObject findIpsObject(IpsObjectType type, String qualifiedName) throws CoreException {
        return getIpsPackageFragmentRoot().getIpsObject(type, qualifiedName);
    }

    /**
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.internal.model.IpsObjectPathEntry#findIpsObject(org.faktorips.devtools.core.model.QualifiedNameType)
     */
    public IIpsObject findIpsObject(QualifiedNameType nameType) throws CoreException {
        return getIpsPackageFragmentRoot().getIpsObject(nameType);
    }

    /**
     * Overridden IMethod.
     * 
     * @see org.faktorips.devtools.core.internal.model.IpsObjectPathEntry#findIpsObjects(org.faktorips.devtools.core.model.IpsObjectType,
     *      java.util.List)
     */
    public void findIpsObjects(IpsObjectType type, List result) throws CoreException {
        ((IpsPackageFragmentRoot)getIpsPackageFragmentRoot()).findIpsObjects(type, result);
    }

    /**
     * Overridden IMethod.
     * 
     * @see org.faktorips.devtools.core.internal.model.IpsObjectPathEntry#findIpsObjectsStartingWith(org.faktorips.devtools.core.model.IpsObjectType,
     *      java.lang.String, boolean, java.util.List)
     */
    protected void findIpsObjectsStartingWith(IpsObjectType type, String prefix, boolean ignoreCase, List result)
            throws CoreException {
        ((IpsPackageFragmentRoot)getIpsPackageFragmentRoot()).findIpsObjectsStartingWith(type, prefix, ignoreCase,
                result);
    }

    /**
     * Overridden IMethod.
     * 
     * @see org.faktorips.devtools.core.internal.model.IpsObjectPathEntry#initFromXml(org.w3c.dom.Element)
     */
    public void initFromXml(Element element) {
        IProject project = getIpsObjectPath().getIpsProject().getProject();
        String sourceFolderPath = element.getAttribute("sourceFolder");
        sourceFolder = project.getFolder(new Path(sourceFolderPath));
        String outputFolderPathGenerated = element.getAttribute("outputFolderGenerated");
        outputFolderGenerated = outputFolderPathGenerated.equals("") ? null : project.getFolder(new Path(
                outputFolderPathGenerated));
        basePackageGenerated = element.getAttribute("basePackageGenerated");
        String outputFolderPathExtension = element.getAttribute("outputFolderExtension");
        outputFolderExtension = outputFolderPathExtension.equals("") ? null : project.getFolder(new Path(
                outputFolderPathExtension));
        basePackageExtension = element.getAttribute("basePackageExtension");
    }

    /**
     * Overridden IMethod.
     * 
     * @see org.faktorips.devtools.core.internal.model.IpsObjectPathEntry#toXml(org.w3c.dom.Document)
     */
    public Element toXml(Document doc) {
        Element element = doc.createElement(IpsObjectPathEntry.XML_ELEMENT);
        element.setAttribute("type", TYPE_SRC_FOLDER);
        element.setAttribute("sourceFolder", sourceFolder.getProjectRelativePath().toString());
        element.setAttribute("outputFolderGenerated", outputFolderGenerated == null ? "" : outputFolderGenerated
                .getProjectRelativePath().toString());
        element.setAttribute("basePackageGenerated", basePackageGenerated);
        element.setAttribute("outputFolderExtension", outputFolderExtension == null ? "" : outputFolderExtension
                .getProjectRelativePath().toString());
        element.setAttribute("basePackageExtension", basePackageExtension);
        return element;
    }

}