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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.transform.TransformerException;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.internal.model.IpsModel;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsSrcFile;
import org.faktorips.devtools.core.internal.model.productcmpt.ProductCmpt;
import org.faktorips.devtools.core.internal.model.tablecontents.TableContents;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectGeneration;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.ITimedIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentArbitrarySortDefinition;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentSortDefinition;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.util.XmlUtil;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.StringUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Implementation of <code>IIpsPackageFragment<code>.
 */
public class IpsPackageFragment extends AbstractIpsPackageFragment implements IIpsPackageFragment {

    IpsPackageFragment(IIpsElement parent, String name) {
        super(parent, name);
    }

    /**
     * {@inheritDoc}
     */
    public IResource getCorrespondingResource() {
        String path = name.replace('.', IPath.SEPARATOR);
        IFolder folder = (IFolder)getParent().getCorrespondingResource();
        return folder.getFolder(new Path(path));
    }

    /**
     * {@inheritDoc} IpsPackageFragments are always returned, whether they are output locations of
     * the javaproject corresponding to this packagefragments IpsProject or not.
     */
    public IIpsPackageFragment[] getChildIpsPackageFragments() throws CoreException {

        List list = getChildIpsPackageFragmentsAsList();

        return (IIpsPackageFragment[])list.toArray(new IIpsPackageFragment[list.size()]);
    }

    /**
     * {@inheritDoc}
     */
    public IIpsPackageFragmentSortDefinition getSortDefinition() {
        IpsModel model = (IpsModel)getIpsModel();
        IIpsPackageFragmentSortDefinition sortDef = model.getSortDefinition(this);

        return sortDef.copy();
    }

    /**
     * Read the sort definition from the <code>SORT_ORDER_FILE_NAME</code>. Returns a
     * {@link IpsPackageFragmentDefaultSortDefinition} if no <code>SORT_ORDER_FILE_NAME</code> is
     * found.
     * 
     * @return Sort definition.
     * @throws CoreException
     */
    public IIpsPackageFragmentSortDefinition loadSortDefinition() throws CoreException {

        IFile file = getSortOrderFile();

        if (file.exists()) {

            try {
                String content = StringUtil.readFromInputStream(file.getContents(), getIpsProject()
                        .getPlainTextFileCharset());
                IpsPackageFragmentArbitrarySortDefinition sortDef = new IpsPackageFragmentArbitrarySortDefinition();
                sortDef.initPersistenceContent(content);
                return sortDef;
            } catch (IOException e) {
                throw new CoreException(new IpsStatus(e));
            }
        }

        return null;
    }

    /**
     * @return Handle to a sort order file. The folder/file doesn't need to exist!
     */
    public IFile getSortOrderFile() {
        IFolder folder = null;

        if (isDefaultPackage()) {
            folder = (IFolder)getRoot().getCorrespondingResource();
        } else {
            folder = (IFolder)getParentIpsPackageFragment().getCorrespondingResource();
        }

        return folder.getFile(new Path(IIpsPackageFragment.SORT_ORDER_FILE_NAME));
    }

    /**
     * {@inheritDoc}
     */
    public IIpsPackageFragment[] getSortedChildIpsPackageFragments() throws CoreException {

        IpsPackageNameComparator comparator = new IpsPackageNameComparator(false);

        List sortedPacks = getChildIpsPackageFragmentsAsList();
        Collections.sort(sortedPacks, comparator);

        return (IIpsPackageFragment[])sortedPacks.toArray(new IIpsPackageFragment[sortedPacks.size()]);
    }

    /**
     * Get all child IIpsPackageFragments as List.
     * 
     * @return
     * @throws CoreException
     */
    private List getChildIpsPackageFragmentsAsList() throws CoreException {
        List list = new ArrayList();

        IFolder folder = (IFolder)getCorrespondingResource();
        IResource[] content = folder.members();

        for (int i = 0; i < content.length; i++) {
            if (content[i].getType() == IResource.FOLDER) {
                if (!getIpsProject().getNamingConventions().validateIpsPackageName(content[i].getName())
                        .containsErrorMsg()) {
                    String packageName = getName().equals(IIpsPackageFragment.NAME_OF_THE_DEFAULT_PACKAGE) ? content[i]
                            .getName() : getName() + "." + content[i].getName(); //$NON-NLS-1$ 
                    list.add(new IpsPackageFragment(getParent(), packageName));
                }
            }
        }

        return list;
    }

    /**
     * {@inheritDoc}
     */
    public void setSortDefinition(IIpsPackageFragmentSortDefinition newDefinition) throws CoreException {

        if (IpsModel.TRACE_MODEL_MANAGEMENT) {
            System.out.println("IpsPackageFragment.setSortDefinition: pack=" + this); //$NON-NLS-1$
        }

        IFile file = getSortOrderFile();

        if (newDefinition == null) {
            if (file.exists()) {
                file.delete(true, null);
            }
            return;
        }

        if (newDefinition instanceof IIpsPackageFragmentArbitrarySortDefinition) {
            IIpsPackageFragmentArbitrarySortDefinition newSortDef = (IIpsPackageFragmentArbitrarySortDefinition)newDefinition;

            String content = newSortDef.toPersistenceContent();
            byte[] bytes;

            try {
                bytes = content.getBytes(getIpsProject().getPlainTextFileCharset());
            } catch (UnsupportedEncodingException e) {
                throw new CoreException(new IpsStatus(e));
            }

            ByteArrayInputStream is = new ByteArrayInputStream(bytes);
            // overwrite existing files
            if (!file.exists()) {
                file.create(is, true, null);
                return;
            }
            file.setContents(is, IResource.FORCE, null);
        }
    }

    /**
     * {@inheritDoc}
     */
    public IResource[] getNonIpsResources() throws CoreException {
        IContainer cont = (IContainer)getCorrespondingResource();
        List childResources = new ArrayList();
        IResource[] children = cont.members();
        for (int i = 0; i < children.length; i++) {
            if (!isIpsContent(children[i])) {
                childResources.add(children[i]);
            }
        }
        IResource[] resArray = new IResource[childResources.size()];
        return (IResource[])childResources.toArray(resArray);
    }

    /**
     * Returns <code>true</code> if the given IResource is a file or folder that corresponds to an
     * IpsObject or IpsPackageFragment contained in this IpsPackageFragment, false otherwise.
     */
    private boolean isIpsContent(IResource res) throws CoreException {
        IIpsElement[] children = getChildIpsPackageFragments();
        for (int i = 0; i < children.length; i++) {
            if (children[i].getCorrespondingResource().equals(res)) {
                return true;
            }
        }
        children = getChildren();
        for (int i = 0; i < children.length; i++) {
            if (children[i].getCorrespondingResource().equals(res)) {
                return true;
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public IIpsSrcFile[] getIpsSrcFiles() throws CoreException {
        IFolder folder = (IFolder)getCorrespondingResource();
        IResource[] members = folder.members();
        IIpsSrcFile[] children = new IIpsSrcFile[members.length];
        int counter = 0;
        for (int i = 0; i < children.length; i++) {
            if (members[i].getType() == IResource.FILE) {
                IFile file = (IFile)members[i];
                if (IpsObjectType.getTypeForExtension(file.getFileExtension()) != null) {
                    children[counter] = new IpsSrcFile(this, file.getName());
                    counter++;
                }
            }
        }
        if (counter == children.length) {
            return children;
        }
        IIpsSrcFile[] shrinked = new IIpsSrcFile[counter];
        System.arraycopy(children, 0, shrinked, 0, counter);
        return shrinked;
    }

    /**
     * {@inheritDoc}
     */
    public IIpsSrcFile createIpsFile(String name, InputStream source, boolean force, IProgressMonitor monitor)
            throws CoreException {

        IIpsSrcFile ipsSrcFile = getIpsSrcFile(name);
        IpsModel model = (IpsModel)getIpsModel();
        model.removeIpsSrcFileContent(ipsSrcFile);

        IFolder folder = (IFolder)getCorrespondingResource();
        IFile file = folder.getFile(name);
        if (IpsModel.TRACE_MODEL_MANAGEMENT) {
            System.out.println("IpsPackageFragment.createIpsFile - begin: pack=" + this + ", newFile=" + name //$NON-NLS-1$ //$NON-NLS-2$
                    + ", Thead: " + Thread.currentThread().getName()); //$NON-NLS-1$
        }
        file.create(source, force, monitor);

        if (IpsModel.TRACE_MODEL_MANAGEMENT) {
            System.out.println("IpsPackageFragment.createIpsFile - finished: pack=" + this + ", newFile=" + name //$NON-NLS-1$ //$NON-NLS-2$
                    + ", Thead: " + Thread.currentThread().getName()); //$NON-NLS-1$
        }

        // set the new evaluated runtime id for product components
        if (ipsSrcFile.getIpsObjectType() == IpsObjectType.PRODUCT_CMPT) {
            try {
                model.stopBroadcastingChangesMadeByCurrentThread();
                IProductCmpt productCmpt = (IProductCmpt)ipsSrcFile.getIpsObject();
                IIpsProject project = getIpsProject();
                String runtimeId = project.getProductCmptNamingStrategy().getUniqueRuntimeId(project,
                        productCmpt.getName());
                productCmpt.setRuntimeId(runtimeId);
                ipsSrcFile.save(force, monitor);
            } finally {
                model.resumeBroadcastingChangesMadeByCurrentThread();
            }
        }

        return ipsSrcFile;
    }

    /**
     * {@inheritDoc}
     */
    public IIpsSrcFile createIpsFile(String name, String content, boolean force, IProgressMonitor monitor)
            throws CoreException {
        try {
            InputStream is = new ByteArrayInputStream(content.getBytes(getIpsProject().getXmlFileCharset()));
            return createIpsFile(name, is, force, monitor);
        } catch (UnsupportedEncodingException e) {
            throw new CoreException(new IpsStatus(e));
        }
    }

    /**
     * {@inheritDoc}
     */
    public IIpsSrcFile createIpsFile(IpsObjectType type, String ipsObjectName, boolean force, IProgressMonitor monitor)
            throws CoreException {
        String filename = type.getFileName(ipsObjectName);
        IIpsObject ipsObject = type.newObject(getIpsSrcFile(filename));

        Document doc = IpsPlugin.getDefault().newDocumentBuilder().newDocument();
        Element element = ipsObject.toXml(doc);
        try {
            String encoding = getIpsProject().getXmlFileCharset();
            String contents = XmlUtil.nodeToString(element, encoding);
            return createIpsFile(filename, contents, force, monitor);
        } catch (TransformerException e) {
            throw new RuntimeException(e);
            // This is a programing error, rethrow as runtime exception
        }
    }

    /**
     * {@inheritDoc}
     */
    public IIpsSrcFile createIpsFileFromTemplate(String name,
            IIpsObject template,
            GregorianCalendar date,
            boolean force,
            IProgressMonitor monitor) throws CoreException {

        IpsObjectType type = template.getIpsObjectType();
        String filename = type.getFileName(name);
        Document doc = IpsPlugin.getDefault().newDocumentBuilder().newDocument();
        Element element;

        if (template instanceof IProductCmpt) {
            return createProductCmptFromTemplateGeneration(name, (IProductCmpt)template, date, force, null);
        } else {
            IIpsSrcFile ipsSrcFile;
            element = template.toXml(doc);
            try {
                String encoding = getIpsProject().getXmlFileCharset();
                String contents = XmlUtil.nodeToString(element, encoding);
                ipsSrcFile = createIpsFile(filename, contents, force, monitor);
            } catch (TransformerException e) {
                throw new RuntimeException(e);
            }
            if (template instanceof ITableContents) {
                initTableContentsFromTemplate((TableContents)ipsSrcFile.getIpsObject(), (TableContents)template);
            }
            return ipsSrcFile;
        }
    }

    private void initTableContentsFromTemplate(TableContents newTableContents, TableContents template)
            throws CoreException {
        newTableContents.setTableStructure(template.getTableStructure());
        newTableContents.setNumOfColumnsInternal(template.getNumOfColumns());
        newTableContents.setDescription(template.getDescription());
    }

    /**
     * {@inheritDoc}
     */
    private IIpsSrcFile createProductCmptFromTemplateGeneration(String name,
            IProductCmpt template,
            GregorianCalendar date,
            boolean force,
            IProgressMonitor monitor) throws CoreException {

        IIpsObjectGeneration source = ((ITimedIpsObject)template).findGenerationEffectiveOn(date);
        if (source == null) {
            source = getFirstGeneration(template, date);
            if (source == null) {
                throw new CoreException(
                        new IpsStatus(
                                "No generation found for the given date " + date.getTime().toString() + " in " + template.getQualifiedName())); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
        IIpsSrcFile file = createIpsFile(template.getIpsObjectType(), name, force, monitor);
        IpsModel model = (IpsModel)getIpsModel();
        try {
            model.stopBroadcastingChangesMadeByCurrentThread();
            ProductCmpt newProductCmpt = (ProductCmpt)file.getIpsObject();
            newProductCmpt.setProductCmptType(template.getProductCmptType());
            newProductCmpt.setValidTo(template.getValidTo());
            newProductCmpt.setDescription(template.getDescription());
            newProductCmpt.newGeneration(source, IpsPlugin.getDefault().getIpsPreferences().getWorkingDate());
            file.save(true, null);
        } finally {
            model.resumeBroadcastingChangesMadeByCurrentThread();
        }
        return file;
    }

    /**
     * Returns the first generation of the given timed ips object, if this generation is valid from
     * before the given date or <code>null</code> otherwise.
     * 
     * @param timed The timed ips object to get the first generation from.
     * @param date The date the first generation of the timed ips objects valid date has to be
     *            before.
     * @return The first generation or <code>null</code>.
     */
    private IIpsObjectGeneration getFirstGeneration(ITimedIpsObject timed, GregorianCalendar date) {
        IIpsObjectGeneration first = timed.getFirstGeneration();
        if ((first != null) && (!first.getValidFrom().before(date))) {
            first = null;
        }
        return first;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void findIpsObjects(IpsObjectType type, List result) throws CoreException {
        if (!exists()) {
            return;
        }
        IFolder folder = (IFolder)getCorrespondingResource();
        IResource[] members = folder.members();
        String extension = type.getFileExtension();
        for (int i = 0; i < members.length; i++) {
            if (members[i].getType() == IResource.FILE) {
                IFile file = (IFile)members[i];
                if (extension.equals(file.getFileExtension())) {
                    IIpsSrcFile srcFile = new IpsSrcFile(this, file.getName());
                    if (srcFile.getIpsObject() != null) {
                        result.add(srcFile.getIpsObject());
                    }
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void findIpsObjects(List result) throws CoreException {
        if (!exists()) {
            return;
        }
        IFolder folder = (IFolder)getCorrespondingResource();
        IResource[] members = folder.members();

        IpsObjectType[] types = getIpsModel().getIpsObjectTypes();

        Set fileExtensionNames = new HashSet();
        for (int i = 0; i < types.length; i++) {
            fileExtensionNames.add(types[i].getFileExtension());
        }
        for (int i = 0; i < members.length; i++) {
            if (members[i].getType() == IResource.FILE) {
                IFile file = (IFile)members[i];
                if (fileExtensionNames.contains(file.getFileExtension())) {
                    IIpsSrcFile srcFile = new IpsSrcFile(this, file.getName());
                    if (srcFile.getIpsObject() != null) {
                        result.add(srcFile.getIpsObject());
                    }
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void findIpsSourceFiles(IpsObjectType type, List result) throws CoreException {
        if (!exists()) {
            return;
        }
        IFolder folder = (IFolder)getCorrespondingResource();
        IResource[] members = folder.members();
        for (int i = 0; i < members.length; i++) {
            if (members[i].getType() == IResource.FILE) {
                IFile file = (IFile)members[i];
                if (type.getFileExtension().equals(file.getFileExtension())) {
                    IpsSrcFile ipsSrcFile = new IpsSrcFile(this, file.getName());
                    result.add(ipsSrcFile);
                }
            }
        }
    }

    /**
     * Searches all objects of the given type starting with the given prefix and adds them to the
     * result.
     * 
     * @throws NullPointerException if either type, prefix or result is null.
     * @throws CoreException if an error occurs while searching.
     */
    public void findIpsObjectsStartingWith(IpsObjectType type, String prefix, boolean ignoreCase, List result)
            throws CoreException {
        findIpsSourceFilesStartingWithInternal(type, prefix, ignoreCase, result, true);
    }

    /**
     * Searches all objects of the given type starting with the given prefix and adds them to the
     * result.
     * 
     * @throws NullPointerException if either type, prefix or result is null.
     * @throws CoreException if an error occurs while searching.
     */
    public void findIpsSourceFilesStartingWith(IpsObjectType type, String prefix, boolean ignoreCase, List result)
            throws CoreException {
        findIpsSourceFilesStartingWithInternal(type, prefix, ignoreCase, result, false);
    }

    public void findIpsSourceFilesStartingWithInternal(IpsObjectType type,
            String prefix,
            boolean ignoreCase,
            List result,
            boolean returnIpsObject) throws CoreException {
        ArgumentCheck.notNull(type);
        ArgumentCheck.notNull(prefix);
        ArgumentCheck.notNull(result);
        if (!exists()) {
            return;
        }
        IFolder folder = (IFolder)getCorrespondingResource();
        IResource[] members = folder.members();
        String newPrefix = ignoreCase ? prefix.toLowerCase() : prefix;
        for (int i = 0; i < members.length; i++) {
            if (members[i].getType() == IResource.FILE) {
                IFile file = (IFile)members[i];
                if (type.getFileExtension().equals(file.getFileExtension())) {
                    String filename = ignoreCase ? file.getName().toLowerCase() : file.getName();
                    if (filename.startsWith(newPrefix)) {
                        IIpsSrcFile srcFile = new IpsSrcFile(this, file.getName());
                        if (returnIpsObject) {
                            result.add(srcFile.getIpsObject());
                        } else {
                            result.add(srcFile);
                        }
                    }
                }
            }
        }
    }

    public IIpsPackageFragment createSubPackage(String name, boolean force, IProgressMonitor monitor)
            throws CoreException {
        if (getIpsProject().getNamingConventions().validateIpsPackageName(name).containsErrorMsg()) {
            throw new CoreException(new Status(IStatus.ERROR, IpsPlugin.PLUGIN_ID, IStatus.ERROR, NLS.bind(
                    "{0} is not a valid package name.", name), null)); //$NON-NLS-1$
        }
        return getRoot().createPackageFragment(isDefaultPackage() ? name : (getName() + "." + name), true, null); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     * 
     * @throws CoreException
     */
    public boolean hasChildIpsPackageFragments() throws CoreException {

        IFolder folder = (IFolder)getCorrespondingResource();
        IResource[] content = folder.members();

        for (int i = 0; i < content.length; i++) {
            if (content[i].getType() == IResource.FOLDER) {
                if (!getIpsProject().getNamingConventions().validateIpsPackageName(content[i].getName())
                        .containsErrorMsg()) {
                    return true;
                }
            }
        }

        return false;
    }

}
