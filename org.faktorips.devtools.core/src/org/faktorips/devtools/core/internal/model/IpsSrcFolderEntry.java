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

import java.util.List;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsSrcFolderEntry;
import org.faktorips.devtools.core.model.IpsObjectType;
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
    private String basePackageGenerated = ""; //$NON-NLS-1$

    // the output folder containing the Java files where the developer adds it's own code.
    private IFolder outputFolderExtension;

    // the name of the base package containing the Java files where the developer adds it's own code.
    private String basePackageExtension = ""; //$NON-NLS-1$

    IpsSrcFolderEntry(IpsObjectPath path) {
        super(path);
    }

    IpsSrcFolderEntry(IpsObjectPath path, IFolder sourceFolder) {
        super(path);
        ArgumentCheck.notNull(sourceFolder);
        this.sourceFolder = sourceFolder;
    }

    /**
     * Overridden.
     */
    public String getType() {
        return TYPE_SRC_FOLDER;
    }

    /**
     * Overridden.
     */
    public IFolder getSourceFolder() {
        return sourceFolder;
    }

    /**
     * Overridden.
     */
    public IIpsPackageFragmentRoot getIpsPackageFragmentRoot(IIpsProject ipsProject) {
        return ipsProject.getIpsPackageFragmentRoot(sourceFolder.getName());
    }

    /**
     * Overridden.
     */
    public IFolder getOutputFolderForGeneratedJavaFiles() {
        if (getIpsObjectPath().isOutputDefinedPerSrcFolder()) {
            return outputFolderGenerated;
        }
        return getIpsObjectPath().getOutputFolderForGeneratedJavaFiles();
    }

    /**
     * Overridden.
     */
    public IFolder getSpecificOutputFolderForGeneratedJavaFiles() {
        return outputFolderGenerated;
    }

    /**
     * Overridden.
     */
    public void setSpecificOutputFolderForGeneratedJavaFiles(IFolder outputFolder) {
        this.outputFolderGenerated = outputFolder;
    }

    /**
     * Overridden.
     */
    public String getBasePackageNameForGeneratedJavaClasses() {
        if (getIpsObjectPath().isOutputDefinedPerSrcFolder()) {
            return basePackageGenerated;
        }
        return getIpsObjectPath().getBasePackageNameForGeneratedJavaClasses();
    }

    /**
     * Overridden.
     */
    public String getSpecificBasePackageNameForGeneratedJavaClasses() {
        return basePackageGenerated;
    }

    /**
     * Overridden.
     */
    public void setSpecificBasePackageNameForGeneratedJavaClasses(String name) {
        this.basePackageGenerated = name;
    }

    /**
     * Overridden.
     */
    public IFolder getOutputFolderForExtensionJavaFiles() {
        if (getIpsObjectPath().isOutputDefinedPerSrcFolder()) {
            return outputFolderExtension;
        }
        return getIpsObjectPath().getOutputFolderForExtensionJavaFiles();
    }

    /**
     * Overridden.
     */
    public IFolder getSpecificOutputFolderForExtensionJavaFiles() {
        return outputFolderExtension;
    }

    /**
     * Overridden.
     */
    public void setSpecificOutputFolderForExtensionJavaFiles(IFolder outputFolder) {
        outputFolderExtension = outputFolder;
    }

    /**
     * Overridden.
     */
    public String getBasePackageNameForExtensionJavaClasses() {
        if (getIpsObjectPath().isOutputDefinedPerSrcFolder()) {
            return basePackageExtension;
        }
        return getIpsObjectPath().getBasePackageNameForExtensionJavaClasses();
    }

    /**
     * Overridden.
     */
    public String getSpecificBasePackageNameForExtensionJavaClasses() {
        return basePackageExtension;
    }

    /**
     * Overridden.
     */
    public void setSpecificBasePackageNameForExtensionJavaClasses(String name) {
        basePackageExtension = name;
    }

    /**
     * Overridden.
     */
    public IIpsObject findIpsObject(IIpsProject ipsProject, IpsObjectType type, String qualifiedName) throws CoreException {
        return getIpsPackageFragmentRoot(ipsProject).findIpsObject(type, qualifiedName);
    }

    /**
     * Overridden.
     */
    public IIpsObject findIpsObject(IIpsProject ipsProject, QualifiedNameType nameType) throws CoreException {
        return getIpsPackageFragmentRoot(ipsProject).findIpsObject(nameType);
    }

    /**
     * Overridden.
     */
    public void findIpsObjects(IIpsProject ipsProject, IpsObjectType type, List result) throws CoreException {
        ((IpsPackageFragmentRoot)getIpsPackageFragmentRoot(ipsProject)).findIpsObjects(type, result);
    }

    /**
     * Overridden.
     */
    protected void findIpsObjectsStartingWith(IIpsProject ipsProject, IpsObjectType type, String prefix, boolean ignoreCase, List result)
            throws CoreException {
        ((IpsPackageFragmentRoot)getIpsPackageFragmentRoot(ipsProject)).findIpsObjectsStartingWith(type, prefix, ignoreCase,
                result);
    }

    /**
     * Overridden.
     */
    public void initFromXml(Element element, IProject project) {
        String sourceFolderPath = element.getAttribute("sourceFolder"); //$NON-NLS-1$
        sourceFolder = project.getFolder(new Path(sourceFolderPath));
        String outputFolderPathGenerated = element.getAttribute("outputFolderGenerated"); //$NON-NLS-1$
        outputFolderGenerated = outputFolderPathGenerated.equals("") ? null : project.getFolder(new Path( //$NON-NLS-1$
                outputFolderPathGenerated));
        basePackageGenerated = element.getAttribute("basePackageGenerated"); //$NON-NLS-1$
        String outputFolderPathExtension = element.getAttribute("outputFolderExtension"); //$NON-NLS-1$
        outputFolderExtension = outputFolderPathExtension.equals("") ? null : project.getFolder(new Path( //$NON-NLS-1$
                outputFolderPathExtension));
        basePackageExtension = element.getAttribute("basePackageExtension"); //$NON-NLS-1$
    }

    /**
     * Overridden.
     */
    public Element toXml(Document doc) {
        Element element = doc.createElement(IpsObjectPathEntry.XML_ELEMENT);
        element.setAttribute("type", TYPE_SRC_FOLDER); //$NON-NLS-1$
        element.setAttribute("sourceFolder", sourceFolder.getProjectRelativePath().toString()); //$NON-NLS-1$
        element.setAttribute("outputFolderGenerated", outputFolderGenerated == null ? "" : outputFolderGenerated //$NON-NLS-1$ //$NON-NLS-2$
                .getProjectRelativePath().toString());
        element.setAttribute("basePackageGenerated", basePackageGenerated==null ? "" : basePackageGenerated); //$NON-NLS-1$ //$NON-NLS-2$
        element.setAttribute("outputFolderExtension", outputFolderExtension == null ? "" : outputFolderExtension //$NON-NLS-1$ //$NON-NLS-2$
                .getProjectRelativePath().toString());
        element.setAttribute("basePackageExtension", basePackageExtension==null ? "" : basePackageExtension); //$NON-NLS-1$ //$NON-NLS-2$
        return element;
    }

}