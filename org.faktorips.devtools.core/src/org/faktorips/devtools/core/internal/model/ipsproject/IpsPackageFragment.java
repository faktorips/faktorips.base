/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.ipsproject;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.transform.TransformerException;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.internal.model.IpsModel;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsSrcFile;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectGeneration;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.ITimedIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.util.EclipseIOUtil;
import org.faktorips.devtools.core.util.QNameUtil;
import org.faktorips.devtools.core.util.XmlUtil;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.StringUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Implementation of <code>IIpsPackageFragment<code>.
 */
public class IpsPackageFragment extends AbstractIpsPackageFragment {

    private final IPath path;
    private IFolder correspondingResourceFolder;
    private final ChildOrderComparatorCache childOrderComparatorCache = new ChildOrderComparatorCache();

    IpsPackageFragment(IIpsElement parent, String name) {
        super(parent, name);
        this.path = new Path(name.replace(SEPARATOR, IPath.SEPARATOR));
    }

    @Override
    public IResource getCorrespondingResource() {
        if (this.correspondingResourceFolder == null) {
            IFolder folder = (IFolder)getParent().getCorrespondingResource();
            this.correspondingResourceFolder = folder.getFolder(path);
        }
        return this.correspondingResourceFolder;
    }

    @Override
    public void setName(String name) {
        throw new UnsupportedOperationException("Package fragment names cannot be changed."); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     * 
     * {@link IIpsPackageFragment IIpsPackageFragments} are always returned, whether they are output
     * locations of the {@link IJavaProject} corresponding to this package fragment's {@link IpsProject}
     * or not.
     */
    @Override
    public IIpsPackageFragment[] getChildIpsPackageFragments() throws CoreException {
        List<IIpsPackageFragment> list = getChildIpsPackageFragmentsAsList();
        return list.toArray(new IIpsPackageFragment[list.size()]);
    }

    @Override
    public Comparator<IIpsElement> getChildOrderComparator() {
        return childOrderComparatorCache.get();
    }

    public void setChildOrderComparator(Comparator<IIpsElement> newChildOrderComparator) {
        if (IpsModel.TRACE_MODEL_MANAGEMENT) {
            System.out.println("IpsPackageFragment.setChildOrderComparator: pack=" + this); //$NON-NLS-1$
        }
        childOrderComparatorCache.set(newChildOrderComparator);
    }

    /**
     * Get all child IIpsPackageFragments as List.
     */
    private List<IIpsPackageFragment> getChildIpsPackageFragmentsAsList() throws CoreException {
        List<IIpsPackageFragment> list = new ArrayList<IIpsPackageFragment>();

        IFolder folder = (IFolder)getCorrespondingResource();
        IResource[] content = folder.members();

        for (int i = 0; i < content.length; i++) {
            if (content[i].getType() == IResource.FOLDER) {
                if (!getIpsProject().getNamingConventions().validateIpsPackageName(content[i].getName())
                        .containsErrorMsg()) {
                    String packageName = getSubPackageName(content[i].getName());
                    list.add(new IpsPackageFragment(getParent(), packageName));
                }
            }
        }

        return list;
    }

    @Override
    public IResource[] getNonIpsResources() throws CoreException {
        IContainer cont = (IContainer)getCorrespondingResource();
        List<IResource> childResources = new ArrayList<IResource>();
        IResource[] children = cont.members();
        for (int i = 0; i < children.length; i++) {
            if (!isIpsContent(children[i])) {
                childResources.add(children[i]);
            }
        }
        IResource[] resArray = new IResource[childResources.size()];
        return childResources.toArray(resArray);
    }

    /**
     * Returns <code>true</code> if the given IResource is a file or folder that corresponds to an
     * IpsObject or IpsPackageFragment contained in this IpsPackageFragment, false otherwise.
     */
    private boolean isIpsContent(IResource res) throws CoreException {
        IIpsElement[] children = getChildIpsPackageFragments();
        for (IIpsElement element : children) {
            if (element.getCorrespondingResource().equals(res)) {
                return true;
            }
        }
        children = getChildren();
        for (IIpsElement element : children) {
            if (element.getCorrespondingResource().equals(res)) {
                return true;
            }
        }
        return false;
    }

    @Override
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

    @Override
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

    @Override
    public IIpsSrcFile createIpsFile(String name, String content, boolean force, IProgressMonitor monitor)
            throws CoreException {
        try {
            InputStream is = new ByteArrayInputStream(content.getBytes(getIpsProject().getXmlFileCharset()));
            return createIpsFile(name, is, force, monitor);
        } catch (UnsupportedEncodingException e) {
            throw new CoreException(new IpsStatus(e));
        }
    }

    @Override
    public IIpsSrcFile createIpsFile(IpsObjectType type, String ipsObjectName, boolean force, IProgressMonitor monitor)
            throws CoreException {
        String filename = type.getFileName(ipsObjectName);
        IIpsObject ipsObject = type.newObject(getIpsSrcFile(filename));

        Document doc = IpsPlugin.getDefault().getDocumentBuilder().newDocument();
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
     * @deprecated {@link IIpsPackageFragment#createIpsFileFromTemplate(String, IIpsObject, GregorianCalendar, GregorianCalendar, boolean, IProgressMonitor)}
     */
    @Override
    @Deprecated
    public IIpsSrcFile createIpsFileFromTemplate(String name,
            IIpsObject template,
            GregorianCalendar oldDate,
            GregorianCalendar newDate,
            boolean force,
            IProgressMonitor monitor) throws CoreException {

        IpsObjectType type = template.getIpsObjectType();
        String filename = type.getFileName(name);
        Document doc = IpsPlugin.getDefault().getDocumentBuilder().newDocument();
        Element element;

        IIpsSrcFile ipsSrcFile;
        element = template.toXml(doc);
        try {
            String encoding = getIpsProject().getXmlFileCharset();
            String contents = XmlUtil.nodeToString(element, encoding);
            ipsSrcFile = createIpsFile(filename, contents, force, monitor);
        } catch (TransformerException e) {
            throw new RuntimeException(e);
        }
        if (template instanceof ITimedIpsObject) {
            ITimedIpsObject copyProductCmpt = ((ITimedIpsObject)ipsSrcFile.getIpsObject());
            IIpsObjectGeneration generationEffectiveOn = copyProductCmpt.getGenerationEffectiveOn(oldDate);
            if (generationEffectiveOn == null) {
                generationEffectiveOn = copyProductCmpt.getFirstGeneration();
            }
            for (IIpsObjectGeneration generation : copyProductCmpt.getGenerations()) {
                if (!generation.equals(generationEffectiveOn)) {
                    generation.delete();
                }
            }
            if (generationEffectiveOn == null) {
                generationEffectiveOn = copyProductCmpt.newGeneration(newDate);
            } else {
                generationEffectiveOn.setValidFrom(newDate);
            }
        }

        return ipsSrcFile;
    }

    @Override
    public void findIpsObjects(IpsObjectType type, List<IIpsObject> result) throws CoreException {
        if (!exists()) {
            return;
        }
        IFolder folder = (IFolder)getCorrespondingResource();
        IResource[] members = folder.members();
        String extension = type.getFileExtension();
        for (IResource member : members) {
            if (member.getType() == IResource.FILE) {
                IFile file = (IFile)member;
                if (extension.equals(file.getFileExtension())) {
                    IIpsSrcFile srcFile = new IpsSrcFile(this, file.getName());
                    if (srcFile.getIpsObject() != null) {
                        result.add(srcFile.getIpsObject());
                    }
                }
            }
        }
    }

    public void findIpsObjects(List<IIpsObject> result) throws CoreException {
        if (!exists()) {
            return;
        }
        IFolder folder = (IFolder)getCorrespondingResource();
        IResource[] members = folder.members();

        IpsObjectType[] types = getIpsModel().getIpsObjectTypes();

        Set<String> fileExtensionNames = new HashSet<String>();
        for (IpsObjectType type : types) {
            fileExtensionNames.add(type.getFileExtension());
        }
        for (IResource member : members) {
            if (member.getType() == IResource.FILE) {
                IFile file = (IFile)member;
                if (fileExtensionNames.contains(file.getFileExtension())) {
                    IIpsSrcFile srcFile = new IpsSrcFile(this, file.getName());
                    if (srcFile.getIpsObject() != null) {
                        result.add(srcFile.getIpsObject());
                    }
                }
            }
        }
    }

    @Override
    public void findIpsSourceFiles(IpsObjectType type, List<IIpsSrcFile> result) throws CoreException {
        if (!exists()) {
            return;
        }
        IFolder folder = (IFolder)getCorrespondingResource();
        IResource[] members = folder.members();
        for (IResource member : members) {
            if (member.getType() == IResource.FILE) {
                IFile file = (IFile)member;
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
    public void findIpsSourceFilesStartingWith(IpsObjectType type,
            String prefix,
            boolean ignoreCase,
            List<IIpsSrcFile> result) throws CoreException {

        ArgumentCheck.notNull(type);
        ArgumentCheck.notNull(prefix);
        ArgumentCheck.notNull(result);
        if (!exists()) {
            return;
        }
        IFolder folder = (IFolder)getCorrespondingResource();
        IResource[] members = folder.members();
        String newPrefix = ignoreCase ? prefix.toLowerCase() : prefix;
        for (IResource member : members) {
            if (member.getType() == IResource.FILE) {
                IFile file = (IFile)member;
                if (type.getFileExtension().equals(file.getFileExtension())) {
                    String filename = ignoreCase ? file.getName().toLowerCase() : file.getName();
                    if (filename.startsWith(newPrefix)) {
                        IIpsSrcFile srcFile = new IpsSrcFile(this, file.getName());
                        result.add(srcFile);
                    }
                }
            }
        }
    }

    @Override
    public IIpsPackageFragment createSubPackage(String name, boolean force, IProgressMonitor monitor)
            throws CoreException {
        if (getIpsProject().getNamingConventions().validateIpsPackageName(name).containsErrorMsg()) {
            throw new CoreException(new Status(IStatus.ERROR, IpsPlugin.PLUGIN_ID, IStatus.ERROR,
                    NLS.bind("{0} is not a valid package name.", name), null)); //$NON-NLS-1$
        }
        return getRoot().createPackageFragment(getSubPackageName(name), true, null);
    }

    @Override
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

    @Override
    public void delete() throws CoreException {
        for (IIpsPackageFragment childPackage : getChildIpsPackageFragments()) {
            childPackage.delete();
        }
        for (IIpsSrcFile childSrcFile : getIpsSrcFiles()) {
            childSrcFile.delete();
        }
        getCorrespondingResource().delete(true, null);
    }

    private IFile getSortOrderFile() {
        return childOrderComparatorCache.getSortOrderFile();
    }

    private class ChildOrderComparatorCache {

        private IFile sortOrderFile;
        private long lastModification = -1;
        private Comparator<IIpsElement> childOrderComparator;

        private Comparator<IIpsElement> get() {
            IFile file = getSortOrderFile();
            if (file.exists()) {
                long modificationStamp = file.getModificationStamp();
                if (lastModification == modificationStamp) {
                    return childOrderComparator;
                } else {
                    childOrderComparator = DefinedOrderComparator.forPackage(IpsPackageFragment.this);
                    if (childOrderComparator == null) {
                        childOrderComparator = AbstractIpsPackageFragment.DEFAULT_CHILD_ORDER_COMPARATOR;
                    }
                    lastModification = modificationStamp;
                    return childOrderComparator;
                }
            } else {
                lastModification = -1;
            }
            return AbstractIpsPackageFragment.DEFAULT_CHILD_ORDER_COMPARATOR;
        }

        private IFile getSortOrderFile() {
            if (sortOrderFile == null) {
                IIpsElement parent = isDefaultPackage() ? getRoot() : IpsPackageFragment.this;
                IFolder folder = (IFolder)parent.getCorrespondingResource();
                sortOrderFile = folder.getFile(new Path(IIpsPackageFragment.SORT_ORDER_FILE_NAME));
            }
            return sortOrderFile;
        }

        public void set(Comparator<IIpsElement> newChildOrderComparator) {
            if (newChildOrderComparator instanceof DefinedOrderComparator) {
                ((DefinedOrderComparator)newChildOrderComparator).persistTo(IpsPackageFragment.this);
                sortOrderFile = getSortOrderFile();
                lastModification = sortOrderFile.getModificationStamp();
            } else {
                if (sortOrderFile != null && sortOrderFile.exists()) {
                    try {
                        sortOrderFile.delete(true, null);
                    } catch (CoreException e) {
                        throw new CoreRuntimeException(e);
                    }
                }
            }
            childOrderComparator = newChildOrderComparator;
        }
    }

    public static class DefinedOrderComparator implements Comparator<IIpsElement>, Serializable {

        private static final long serialVersionUID = 1L;

        private final Map<IIpsElement, Integer> sortOrder;

        public DefinedOrderComparator() {
            sortOrder = new LinkedHashMap<IIpsElement, Integer>();
        }

        public DefinedOrderComparator(IIpsElement... orderedElements) {
            sortOrder = new LinkedHashMap<IIpsElement, Integer>(orderedElements.length);
            int i = 0;
            for (IIpsElement element : orderedElements) {
                sortOrder.put(element, i++);
            }
        }

        private DefinedOrderComparator(Map<IIpsElement, Integer> sortOrder) {
            this.sortOrder = sortOrder;
        }

        public static DefinedOrderComparator forPackage(IpsPackageFragment parentPackage) {
            Map<IIpsElement, Integer> read = Persistence.read(parentPackage);
            if (read != null) {
                return new DefinedOrderComparator(read);
            } else {
                return null;
            }
        }

        public void persistTo(IpsPackageFragment parentPackage) {
            Persistence.write(parentPackage, sortOrder);
        }

        @Override
        public int compare(IIpsElement o1, IIpsElement o2) {
            if (o1 == null) {
                throw new NullPointerException("o1 must not be null"); //$NON-NLS-1$
            }
            if (o2 == null) {
                throw new NullPointerException("o2 must not be null"); //$NON-NLS-1$
            }
            boolean sortOrderContains1st = sortOrder.containsKey(o1);
            boolean sortOrderContains2nd = sortOrder.containsKey(o2);
            if (sortOrderContains1st && sortOrderContains2nd) {
                Integer pos1 = sortOrder.get(o1);
                Integer pos2 = sortOrder.get(o2);
                return pos1.compareTo(pos2);
            } else {
                // elements not included in the sort-order will be put at the end of the list.
                if (sortOrderContains1st) {
                    return -1;
                }
                if (sortOrderContains2nd) {
                    return 1;
                }
                return DEFAULT_CHILD_ORDER_COMPARATOR.compare(o1, o2);
            }
        }

        public IIpsElement[] getElements() {
            return sortOrder.keySet().toArray(new IIpsElement[sortOrder.size()]);
        }

        private static class Persistence {

            static Map<IIpsElement, Integer> read(IpsPackageFragment parentPackage) {
                IFile sortOrderFile = parentPackage.getSortOrderFile();
                String charsetName = parentPackage.getIpsProject().getPlainTextFileCharset();
                if (sortOrderFile != null && sortOrderFile.exists()) {
                    try {
                        String content = StringUtil.readFromInputStream(sortOrderFile.getContents(),
                                Charset.forName(charsetName));
                        /*
                         * do not use system line separator here because the file could be transfered from another
                         * system. This regex splits the content at \r\n (windows), \n (unix) or \r (old mac)
                         */
                        String[] lines = content.split("[\r\n]++"); //$NON-NLS-1$
                        LinkedHashMap<IIpsElement, Integer> sortOrder = new LinkedHashMap<IIpsElement, Integer>(
                                lines.length);
                        for (String line : lines) {
                            read(line.trim(), parentPackage, sortOrder);
                        }
                        return sortOrder;
                    } catch (IOException e) {
                        throw new CoreRuntimeException(new IpsStatus(e));
                    } catch (CoreException e) {
                        IpsPlugin.log(e);
                        return null;
                    }
                } else {
                    return null;
                }
            }

            private static boolean read(String line,
                    final IpsPackageFragment parentPackage,
                    LinkedHashMap<IIpsElement, Integer> sortOrder) {
                if (isNeitherBlankNorComment(line)) {
                    IIpsElement element = findElement(line, parentPackage);
                    if (element != null) {
                        // don't check for .exists(), as elements might arrive later during a refactoring
                        sortOrder.put(element, sortOrder.size());
                        return true;
                    }
                }
                return false;
            }

            private static IIpsElement findElement(String line, final IpsPackageFragment parentPackage) {
                if (line.contains(".")) { //$NON-NLS-1$
                    return parentPackage.getIpsSrcFile(line);
                } else {
                    return parentPackage.isDefaultPackage() ? parentPackage.getRoot().getIpsPackageFragment(line)
                            : parentPackage.getSubPackage(line);
                }
            }

            /**
             * Skip empty lines and lines starting with a comment ('#').
             * 
             * @param line One single line (String) of the sort order.
             * @return <code>true</code> if it is a valid entry; <code>false</code> if line is empty or a
             *         comment
             */
            private static boolean isNeitherBlankNorComment(String line) {
                return !IpsStringUtils.isBlank(line) && !line.startsWith("#"); //$NON-NLS-1$
            }

            static void write(IpsPackageFragment parentPackage, Map<IIpsElement, Integer> sortOrder) {
                final IFile sortOrderFile = parentPackage.getSortOrderFile();
                final String charsetName = parentPackage.getIpsProject().getPlainTextFileCharset();
                final String content = toPersistenceContent(sortOrder);

                IWorkspaceRunnable runnable = new IWorkspaceRunnable() {

                    @Override
                    public void run(IProgressMonitor monitor) throws CoreException {

                        byte[] bytes = content.getBytes(Charset.forName(charsetName));

                        ByteArrayInputStream is = new ByteArrayInputStream(bytes);
                        try {
                            // overwrite existing files
                            if (sortOrderFile.exists()) {
                                EclipseIOUtil.writeToFile(sortOrderFile, is, true, true, null);
                            } else {
                                sortOrderFile.create(is, true, null);
                            }
                        } catch (CoreException e) {
                            throw new CoreRuntimeException(e);
                        }
                    }
                };
                try {
                    ResourcesPlugin.getWorkspace().run(runnable, null);
                } catch (CoreException e) {
                    throw new CoreRuntimeException(e);
                }

            }

            static String toPersistenceContent(Map<IIpsElement, Integer> sortOrder) {
                StringBuilder sb = new StringBuilder(Messages.IpsPackageFragmentArbitrarySortDefinition_CommentLine);
                sb.append(StringUtil.getSystemLineSeparator());
                for (IIpsElement element : sortOrder.keySet()) {
                    String name = element instanceof IIpsSrcFile
                            ? ((IIpsSrcFile)element).getQualifiedNameType().getFileName()
                            : QNameUtil.getUnqualifiedName(element.getName());
                    sb.append(name);
                    sb.append(StringUtil.getSystemLineSeparator());
                }
                return sb.toString();
            }

        }

    }

}
