/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.ipsproject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.SystemUtils;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.core.model.ipsproject.IIpsArchive;
import org.faktorips.devtools.core.model.ipsproject.IIpsArchiveEntry;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPathEntry;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectRefEntry;
import org.faktorips.devtools.core.model.ipsproject.IIpsSrcFolderEntry;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.util.ArrayElementMover;
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
        return XML_TAG_NAME
                + " : " //$NON-NLS-1$
                + SystemUtils.LINE_SEPARATOR
                + "The IpsObjectPath defines where Faktor-IPS searches for model and product definition files/objects for this project." //$NON-NLS-1$
                + SystemUtils.LINE_SEPARATOR
                + "Basically it is the same concept as the Java classpath." //$NON-NLS-1$
                + SystemUtils.LINE_SEPARATOR
                + "<" + XML_TAG_NAME + " " //$NON-NLS-1$ //$NON-NLS-2$
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
                + "The IpsObjectPath is defined through one or more entries." //$NON-NLS-1$
                + SystemUtils.LINE_SEPARATOR
                + "Currently the following entry types are supported:" //$NON-NLS-1$
                + SystemUtils.LINE_SEPARATOR
                + " " + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + IpsSrcFolderEntry.getXmlFormatDescription() + SystemUtils.LINE_SEPARATOR
                + " " + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + IpsProjectRefEntry.getXmlFormatDescription() + " " + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
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

    // output folder for generated sources that are marked as derived, more precise this output
    // folder will be marked as derived and hence all members of it will be derived
    // derived resources will not be managed by the resource management system and will
    // output folder and base package for the extension Java files
    private IFolder outputFolderDerivedSources;

    private String basePackageDerived = ""; //$NON-NLS-1$

    private IIpsProject ipsProject;

    // map with QualifiedNameTypes as keys and CachedIpsSrcFiles as values.
    private Map lookupCache = new HashMap(1000);

    public IpsObjectPath(IIpsProject ipsProject) {
        ArgumentCheck.notNull(ipsProject, this);
        this.ipsProject = ipsProject;
    }

    /**
     * {@inheritDoc}
     */
    public IIpsProject getIpsProject() {
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
        throw new IllegalArgumentException("Can't find entry " + entry + " in path " + this); //$NON-NLS-1$  //$NON-NLS-2$
    }

    /**
     * {@inheritDoc}
     */
    public IIpsProjectRefEntry[] getProjectRefEntries() {
        ArrayList projectRefEntries = new ArrayList();
        for (int i = 0; i < entries.length; i++) {
            if (entries[i].getType().equals(IIpsObjectPathEntry.TYPE_PROJECT_REFERENCE)) {
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
            if (entries[i].getType().equals(IIpsObjectPathEntry.TYPE_SRC_FOLDER)) {
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
            if (entries[i].getType().equals(IIpsObjectPathEntry.TYPE_ARCHIVE)) {
                archiveEntries.add(entries[i]);
            }
        }
        return (IIpsArchiveEntry[])archiveEntries.toArray(new IIpsArchiveEntry[archiveEntries.size()]);
    }

    /**
     * {@inheritDoc}
     */
    public IIpsObjectPathEntry getEntry(String rootName) {
        if (rootName == null) {
            return null;
        }
        for (int i = 0; i < entries.length; i++) {
            if (rootName.equals(entries[i].getIpsPackageFragmentRootName())) {
                return entries[i];
            }
        }
        return null;
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
        for (int i = 0; i < entries.length; i++) {
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
        IIpsObjectPathEntry[] newEntries = new IIpsObjectPathEntry[entries.length + 1];
        System.arraycopy(entries, 0, newEntries, 0, entries.length);
        newEntries[newEntries.length - 1] = newEntry;
        entries = newEntries;
        return newEntry;
    }

    /**
     * {@inheritDoc}
     */
    public IIpsArchiveEntry newArchiveEntry(IPath archivePath) throws CoreException {
        IIpsArchiveEntry newEntry = new IpsArchiveEntry(this);
        newEntry.setArchivePath(ipsProject, archivePath);
        IIpsObjectPathEntry[] newEntries = new IIpsObjectPathEntry[entries.length + 1];
        System.arraycopy(entries, 0, newEntries, 0, entries.length);
        newEntries[newEntries.length - 1] = newEntry;
        entries = newEntries;
        return newEntry;
    }

    /**
     * {@inheritDoc}
     */
    public IIpsProjectRefEntry newIpsProjectRefEntry(IIpsProject referencedIpsProject) {
        if (containsProjectRefEntry(referencedIpsProject)) {
            for (int i = 0; i < entries.length; i++) {
                IIpsObjectPathEntry entry = entries[i];
                if (entry instanceof IpsProjectRefEntry) {
                    IpsProjectRefEntry ref = (IpsProjectRefEntry)entry;
                    if (ref.getReferencedIpsProject().equals(referencedIpsProject))
                        return ref;
                }
            }
        }
        IIpsProjectRefEntry newEntry = new IpsProjectRefEntry(this, referencedIpsProject);
        IIpsObjectPathEntry[] newEntries = new IIpsObjectPathEntry[entries.length + 1];
        System.arraycopy(entries, 0, newEntries, 0, entries.length);
        newEntries[newEntries.length - 1] = newEntry;
        entries = newEntries;
        return newEntry;
    }

    /**
     * {@inheritDoc}
     */
    public boolean containsProjectRefEntry(IIpsProject ipsProject) {
        for (int i = 0; i < entries.length; i++) {
            IIpsObjectPathEntry entry = entries[i];
            if (entry instanceof IpsProjectRefEntry) {
                IpsProjectRefEntry ref = (IpsProjectRefEntry)entry;
                if (ref.getReferencedIpsProject().equals(ipsProject))
                    return true;
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public void removeProjectRefEntry(IIpsProject ipsProject) {
        for (int i = 0; i < entries.length; i++) {
            IIpsObjectPathEntry entry = entries[i];
            if (entry instanceof IpsProjectRefEntry) {
                IpsProjectRefEntry ref = (IpsProjectRefEntry)entry;
                if (ref.getReferencedIpsProject().equals(ipsProject)) {
                    IIpsObjectPathEntry[] newEntries = new IIpsObjectPathEntry[entries.length - 1];
                    System.arraycopy(entries, 0, newEntries, 0, i);
                    System.arraycopy(entries, i + 1, newEntries, i, entries.length - i - 1);
                    entries = newEntries;
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean containsArchiveEntry(IIpsArchive ipsArchive) {
        for (int i = 0; i < entries.length; i++) {
            IIpsObjectPathEntry entry = entries[i];
            if (entry instanceof IpsArchiveEntry) {
                IpsArchiveEntry ref = (IpsArchiveEntry)entry;
                if (ref.getIpsArchive().equals(ipsArchive))
                    return true;
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public void removeArchiveEntry(IIpsArchive ipsArchive) {
        for (int i = 0; i < entries.length; i++) {
            IIpsObjectPathEntry entry = entries[i];
            if (entry instanceof IpsArchiveEntry) {
                IpsArchiveEntry archiveEntry = (IpsArchiveEntry)entry;
                if (archiveEntry.getIpsArchive().equals(ipsArchive)) {
                    IIpsObjectPathEntry[] newEntries = new IIpsObjectPathEntry[entries.length - 1];
                    System.arraycopy(entries, 0, newEntries, 0, i);
                    System.arraycopy(entries, i + 1, newEntries, i, entries.length - i - 1);
                    entries = newEntries;
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean containsSrcFolderEntry(IFolder folder) {
        for (int i = 0; i < entries.length; i++) {
            IIpsObjectPathEntry entry = entries[i];
            if (entry instanceof IpsSrcFolderEntry) {
                IpsSrcFolderEntry ref = (IpsSrcFolderEntry)entry;
                if (ref.getSourceFolder().equals(folder))
                    return true;
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public void removeSrcFolderEntry(IFolder srcFolder) {
        for (int i = 0; i < entries.length; i++) {
            IIpsObjectPathEntry entry = entries[i];
            if (entry instanceof IpsSrcFolderEntry) {
                IpsSrcFolderEntry srcFolderEntry = (IpsSrcFolderEntry)entry;
                if (srcFolderEntry.getSourceFolder().equals(srcFolder)) {
                    IIpsObjectPathEntry[] newEntries = new IIpsObjectPathEntry[entries.length - 1];
                    System.arraycopy(entries, 0, newEntries, 0, i);
                    System.arraycopy(entries, i + 1, newEntries, i, entries.length - i - 1);
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
            if (outputFolderMergableSources == null) {
                return new IFolder[0];
            } else {
                return new IFolder[] { outputFolderMergableSources };
            }
        }

        ArrayList result = new ArrayList(entries.length);
        for (int i = 0; i < entries.length; i++) {
            if (entries[i].getType() == IIpsObjectPathEntry.TYPE_SRC_FOLDER) {
                IIpsSrcFolderEntry srcEntry = (IIpsSrcFolderEntry)entries[i];
                if (srcEntry.getOutputFolderForMergableJavaFiles() != null) {
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
     * Returns the first ips source file with the indicated qualified name type found on the path.
     * Returns <code>null</code> if no such object is found.
     */
    public IIpsSrcFile findIpsSrcFile(QualifiedNameType nameType, Set visitedEntries) throws CoreException {
        int maxEntriesToSearch = entries.length;
        CachedSrcFile cachedSrcFile = (CachedSrcFile)lookupCache.get(nameType);
        if (cachedSrcFile != null) {
            if (!cachedSrcFile.file.exists()) {
                lookupCache.remove(nameType);
                cachedSrcFile = null;
            } else {
                if (cachedSrcFile.entryIndex == 0) {
                    // if the file was found via the first entry, it is not possible that a file
                    // with the same name
                    // has been added to another entry that now shadows the found file.
                    return cachedSrcFile.file;
                } else {
                    maxEntriesToSearch = cachedSrcFile.entryIndex;
                }
            }
        }
        for (int i = 0; i < maxEntriesToSearch; i++) {
            IIpsSrcFile ipsSrcFile = ((IpsObjectPathEntry)entries[i]).findIpsSrcFile(nameType, visitedEntries);
            if (ipsSrcFile != null) {
                lookupCache.put(nameType, new CachedSrcFile(ipsSrcFile, i));
                return ipsSrcFile;
            }
        }
        return cachedSrcFile == null ? null : cachedSrcFile.file;
    }

    /**
     * Searches all ips src files of the given type starting with the given prefix found on the path
     * and adds them to the given result list.
     * 
     * @throws CoreException if an error occurs while searching for the source files.
     */
    public void findIpsSrcFilesStartingWith(IpsObjectType type,
            String prefix,
            boolean ignoreCase,
            List result,
            Set visitedEntries) throws CoreException {
        for (int i = 0; i < entries.length; i++) {
            ((IpsObjectPathEntry)entries[i]).findIpsSrcFilesStartingWith(type, prefix, ignoreCase, result,
                    visitedEntries);
        }
    }

    /**
     * Returns all ips source files of the given type found on the path. Returns an empty array if
     * no object is found.
     */
    public IIpsSrcFile[] findIpsSrcFiles(IpsObjectType type, Set visitedEntries) throws CoreException {
        List result = new ArrayList();
        findIpsSrcFiles(type, result, visitedEntries);
        return (IIpsSrcFile[])result.toArray(new IIpsSrcFile[result.size()]);
    }

    /**
     * Adds all ips source files of the given type found on the path to the result list.
     */
    public void findIpsSrcFiles(IpsObjectType type, List result, Set visitedEntries) throws CoreException {
        for (int i = 0; i < entries.length; i++) {
            ((IpsObjectPathEntry)entries[i]).findIpsSrcFiles(type, result, visitedEntries);
        }
    }

    public void findIpsSrcFiles(IpsObjectType type, String packageFragment, List result, Set visitedEntries) throws CoreException {
        for (int i = 0; i < entries.length; i++) {
            ((IpsObjectPathEntry)entries[i]).findIpsSrcFiles(type, packageFragment, result, visitedEntries);
        }
    }

    /**
     * Searches all product components that are based on the given product component type (either
     * directly or because they are based on a subtype of the given type) and adds them to the
     * result. If productCmptType is <code>null</code>, returns all product components found in the
     * fragment root.
     * 
     * @param pcTypeName The product component type product components are searched for.
     * @param includeSubtypes If <code>true</code> is passed also product component that are based
     *            on subtypes of the given policy component are returned, otherwise only product
     *            components that are directly based on the given type are returned.
     * @param result List in which the product components being found are stored in.
     */
    public void findAllProductCmpts(IProductCmptType productCmptType, boolean includeSubytpes, List result)
            throws CoreException {

        Set visitedEntries = new HashSet();
        List allCmptFiles = new ArrayList(100);
        findIpsSrcFiles(IpsObjectType.PRODUCT_CMPT, allCmptFiles, visitedEntries);
        for (Iterator iter = allCmptFiles.iterator(); iter.hasNext();) {
            IIpsSrcFile productCmptFile = (IIpsSrcFile)iter.next();
            if (!productCmptFile.exists()) {
                continue;
            }
            if (productCmptType == null) {
                result.add(productCmptFile.getIpsObject());
                continue;
            }
            QualifiedNameType cmptTypeQnt = new QualifiedNameType(productCmptFile
                    .getPropertyValue(IProductCmpt.PROPERTY_PRODUCT_CMPT_TYPE), IpsObjectType.PRODUCT_CMPT_TYPE);
            visitedEntries.clear();
            IIpsSrcFile typeFoundFile = findIpsSrcFile(cmptTypeQnt, visitedEntries);
            if (typeFoundFile == null) {
                continue;
            }
            if (productCmptType.getIpsSrcFile().equals(typeFoundFile)) {
                result.add(productCmptFile.getIpsObject());
            } else {
                if (includeSubytpes) {
                    IProductCmptType typeFound = (IProductCmptType)typeFoundFile.getIpsObject();
                    if (typeFound.isSubtypeOf(productCmptType, ipsProject)) {
                        result.add(productCmptFile.getIpsObject());
                    }
                }
            }
        }
    }

    /**
     * Adds all source files found in <code>IpsSrcFolderEntry</code>s on the path to the result
     * list.
     */
    public void collectAllIpsSrcFilesOfSrcFolderEntries(List result) throws CoreException {
        Set visitedEntries = new HashSet();
        for (int i = 0; i < entries.length; i++) {
            if (entries[i].getType().equals(IIpsObjectPathEntry.TYPE_SRC_FOLDER)) {
                for (IpsObjectType currentType : IpsPlugin.getDefault().getIpsModel().getIpsObjectTypes()) {
                    ((IpsObjectPathEntry)entries[i]).findIpsSrcFilesInternal(currentType, null, result, visitedEntries);
                }
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
        element
                .setAttribute(
                        "outputFolderMergableSources", outputFolderMergableSources == null ? "" : outputFolderMergableSources.getProjectRelativePath().toString()); //$NON-NLS-1$ //$NON-NLS-2$
        element.setAttribute("basePackageMergable", basePackageMergable); //$NON-NLS-1$
        element
                .setAttribute(
                        "outputFolderDerivedSources", outputFolderDerivedSources == null ? "" : outputFolderDerivedSources.getProjectRelativePath().toString()); //$NON-NLS-1$ //$NON-NLS-2$
        element.setAttribute("basePackageDerived", basePackageDerived); //$NON-NLS-1$
        // entries
        for (int i = 0; i < entries.length; i++) {
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
            path.setOutputFolderForMergableSources(ipsProject.getProject().getFolder(
                    new Path(outputFolderMergedSourcesString)));
        }
        String outputFolderDerivedSourcesString = element.getAttribute("outputFolderDerivedSources"); //$NON-NLS-1$
        if (outputFolderDerivedSourcesString.equals("")) { //$NON-NLS-1$
            path.setOutputFolderForDerivedSources(null);
        } else {
            path.setOutputFolderForDerivedSources(ipsProject.getProject().getFolder(
                    new Path(outputFolderDerivedSourcesString)));
        }
        path.setOutputDefinedPerSrcFolder(Boolean
                .valueOf(element.getAttribute("outputDefinedPerSrcFolder")).booleanValue()); //$NON-NLS-1$

        // init entries
        NodeList nl = element.getElementsByTagName(IpsObjectPathEntry.XML_ELEMENT);
        IIpsObjectPathEntry[] entries = new IIpsObjectPathEntry[nl.getLength()];
        for (int i = 0; i < nl.getLength(); i++) {
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
        if (!isOutputDefinedPerSrcFolder()) {
            if (outputFolderMergableSources == null) {
                list.add(new Message(MSGCODE_MERGABLE_OUTPUT_FOLDER_NOT_SPECIFIED, NLS.bind(
                        Messages.IpsObjectPath_msgOutputFolderMergableMissing, getIpsProject()), Message.ERROR));
            } else {
                list.add(validateFolder(outputFolderMergableSources));
            }
            if (outputFolderDerivedSources == null) {
                list.add(new Message(MSGCODE_DERIVED_OUTPUT_FOLDER_NOT_SPECIFIED, NLS.bind(
                        Messages.IpsObjectPath_msgOutputFolderDerivedMissing, getIpsProject()), Message.ERROR));
            } else {
                list.add(validateFolder(outputFolderDerivedSources));
            }
        }
        IIpsSrcFolderEntry[] srcEntries = getSourceFolderEntries();
        if (srcEntries.length == 0) {
            list.add(new Message(MSGCODE_SRC_FOLDER_ENTRY_MISSING, Messages.IpsObjectPath_srcfolderentrymissing,
                    Message.ERROR)); //$NON-NLS-1$
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
        if (!folder.exists()) {
            String text = NLS.bind(Messages.IpsSrcFolderEntry_msgMissingFolder, folder.getName());
            Message msg = new Message(IIpsObjectPathEntry.MSGCODE_MISSING_FOLDER, text, Message.ERROR, this);
            result.add(msg);
        }
        return result;
    }

    /**
     * Check if there is a cycle inside the object path. All IIpsProjectRefEntries will be checked
     * if there is a cycle in the ips object path entries of all referenced projects. Returns
     * <code>true</code> if a cycle was detected. Returns <code>false</code> if there is no cycle in
     * the ips object path.
     * 
     * @throws CoreException If an error occurs while resolving the object path entries.
     */
    public boolean detectCycle() throws CoreException {
        return detectCycleInternal(ipsProject, new HashSet());
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

    private static class CachedSrcFile {

        IIpsSrcFile file;
        int entryIndex;

        public CachedSrcFile(IIpsSrcFile file, int entryIndex) {
            super();
            this.file = file;
            this.entryIndex = entryIndex;
        }

    }

    /**
     * {@inheritDoc}
     */
    public int[] moveEntries(int[] indices, boolean up) {

        ArgumentCheck.notNull(indices, this);

        ArrayElementMover mover = new ArrayElementMover(entries);

        int[] newSelection;
        if (up) {
            newSelection = mover.moveUp(indices);
        } else {
            newSelection = mover.moveDown(indices);
        }

        return newSelection;
    }

}
