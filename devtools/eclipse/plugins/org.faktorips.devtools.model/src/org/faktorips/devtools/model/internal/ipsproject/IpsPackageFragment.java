/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.ipsproject;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.transform.TransformerException;

import org.eclipse.core.runtime.ICoreRunnable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.faktorips.devtools.abstraction.AContainer;
import org.faktorips.devtools.abstraction.AFile;
import org.faktorips.devtools.abstraction.AFolder;
import org.faktorips.devtools.abstraction.AJavaProject;
import org.faktorips.devtools.abstraction.AResource;
import org.faktorips.devtools.abstraction.AResource.AResourceType;
import org.faktorips.devtools.abstraction.Abstractions;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.internal.IpsModel;
import org.faktorips.devtools.model.internal.ipsobject.IpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.plugin.IpsLog;
import org.faktorips.devtools.model.plugin.IpsModelActivator;
import org.faktorips.devtools.model.plugin.IpsStatus;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.util.QNameUtil;
import org.faktorips.devtools.model.util.XmlUtil;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.StringUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Implementation of <code>IIpsPackageFragment</code>.
 */
public class IpsPackageFragment extends AbstractIpsPackageFragment {

    private final Path path;
    private AFolder correspondingResourceFolder;
    private final ChildOrderComparatorCache childOrderComparatorCache = new ChildOrderComparatorCache();

    IpsPackageFragment(IIpsElement parent, String name) {
        super(parent, name);
        path = Path.of(name.replace(SEPARATOR, IPath.SEPARATOR));
    }

    @Override
    public AResource getCorrespondingResource() {
        if (correspondingResourceFolder == null) {
            AFolder folder = (AFolder)getParent().getCorrespondingResource();
            correspondingResourceFolder = folder.getFolder(path);
        }
        return correspondingResourceFolder;
    }

    @Override
    public void setName(String name) {
        throw new UnsupportedOperationException("Package fragment names cannot be changed."); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     *
     * {@link IIpsPackageFragment IIpsPackageFragments} are always returned, whether they are output
     * locations of the {@link AJavaProject} corresponding to this package fragment's
     * {@link IpsProject} or not.
     */
    @Override
    public IIpsPackageFragment[] getChildIpsPackageFragments() {
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
    private List<IIpsPackageFragment> getChildIpsPackageFragmentsAsList() {
        List<IIpsPackageFragment> list = new ArrayList<>();
        AFolder folder = (AFolder)getCorrespondingResource();
        for (AResource element : folder) {
            if (element.getType() == AResourceType.FOLDER) {
                if (!getIpsProject().getNamingConventions().validateIpsPackageName(element.getName())
                        .containsErrorMsg()) {
                    String packageName = getSubPackageName(element.getName());
                    list.add(new IpsPackageFragment(getParent(), packageName));
                }
            }
        }

        return list;
    }

    @Override
    public AResource[] getNonIpsResources() {
        AContainer cont = (AContainer)getCorrespondingResource();
        List<AResource> childResources = new ArrayList<>();
        for (AResource child : cont) {
            if (!isIpsContent(child)) {
                childResources.add(child);
            }
        }
        AResource[] resArray = new AResource[childResources.size()];
        return childResources.toArray(resArray);
    }

    /**
     * Returns <code>true</code> if the given IResource is a file or folder that corresponds to an
     * IpsObject or IpsPackageFragment contained in this IpsPackageFragment, false otherwise.
     */
    private boolean isIpsContent(AResource res) {
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
    public IIpsSrcFile[] getIpsSrcFiles() {
        AFolder folder = (AFolder)getCorrespondingResource();
        AResource[] members = folder.getMembers().toArray(AResource[]::new);
        IIpsSrcFile[] children = new IIpsSrcFile[members.length];
        int counter = 0;
        for (int i = 0; i < children.length; i++) {
            if (members[i].getType() == AResourceType.FILE) {
                AFile file = (AFile)members[i];
                if (IpsObjectType.getTypeForExtension(file.getExtension()) != null) {
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
    public IIpsSrcFile createIpsFile(String name, InputStream source, boolean force, IProgressMonitor monitor) {

        IIpsSrcFile ipsSrcFile = getIpsSrcFile(name);
        IpsModel model = (IpsModel)getIpsModel();
        model.removeIpsSrcFileContent(ipsSrcFile);

        AFolder folder = (AFolder)getCorrespondingResource();
        AFile file = folder.getFile(name);
        if (IpsModel.TRACE_MODEL_MANAGEMENT) {
            System.out.println("IpsPackageFragment.createIpsFile - begin: pack=" + this + ", newFile=" + name //$NON-NLS-1$ //$NON-NLS-2$
                    + ", Thead: " + Thread.currentThread().getName()); //$NON-NLS-1$
        }
        file.create(source, monitor);

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
                ipsSrcFile.save(monitor);
            } finally {
                model.resumeBroadcastingChangesMadeByCurrentThread();
            }
        }

        return ipsSrcFile;
    }

    @Override
    public IIpsSrcFile createIpsFile(String name, String content, boolean force, IProgressMonitor monitor) {
        try {
            InputStream is = new ByteArrayInputStream(content.getBytes(getIpsProject().getXmlFileCharset()));
            return createIpsFile(name, is, force, monitor);
        } catch (UnsupportedEncodingException e) {
            throw new IpsException(new IpsStatus(e));
        }
    }

    @Override
    public IIpsSrcFile createIpsFile(IpsObjectType type,
            String ipsObjectName,
            boolean force,
            IProgressMonitor monitor) {
        String filename = type.getFileName(ipsObjectName);
        IIpsObject ipsObject = type.newObject(getIpsSrcFile(filename));
        Document doc = XmlUtil.getDefaultDocumentBuilder().newDocument();
        Element element = ipsObject.toXml(doc);
        // Newly created objects may not yet be valid, so remove validation information. It will be
        // re-added when the finalized object is saved.
        element.removeAttribute(XMLConstants.XMLNS_ATTRIBUTE);
        element.removeAttribute(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI);
        try {
            String encoding = getIpsProject().getXmlFileCharset();
            String contents = XmlUtil.nodeToString(element, encoding);
            return createIpsFile(filename, contents, force, monitor);
        } catch (TransformerException e) {
            throw new RuntimeException(e);
            // This is a programing error, rethrow as runtime exception
        }
    }

    @Override
    public void findIpsObjects(IpsObjectType type, List<IIpsObject> result) {
        if (!exists()) {
            return;
        }
        AFolder folder = (AFolder)getCorrespondingResource();
        String extension = type.getFileExtension();
        for (AResource member : folder) {
            if (member.getType() == AResourceType.FILE) {
                AFile file = (AFile)member;
                if (extension.equals(file.getExtension())) {
                    IIpsSrcFile srcFile = new IpsSrcFile(this, file.getName());
                    if (srcFile.getIpsObject() != null) {
                        result.add(srcFile.getIpsObject());
                    }
                }
            }
        }
    }

    public void findIpsObjects(List<IIpsObject> result) {
        if (!exists()) {
            return;
        }
        AFolder folder = (AFolder)getCorrespondingResource();
        IpsObjectType[] types = getIpsModel().getIpsObjectTypes();

        Set<String> fileExtensionNames = new HashSet<>();
        for (IpsObjectType type : types) {
            fileExtensionNames.add(type.getFileExtension());
        }
        for (AResource member : folder) {
            if (member.getType() == AResourceType.FILE) {
                AFile file = (AFile)member;
                if (fileExtensionNames.contains(file.getExtension())) {
                    IIpsSrcFile srcFile = new IpsSrcFile(this, file.getName());
                    if (srcFile.getIpsObject() != null) {
                        result.add(srcFile.getIpsObject());
                    }
                }
            }
        }
    }

    @Override
    public void findIpsSourceFiles(IpsObjectType type, List<IIpsSrcFile> result) {
        if (!exists()) {
            return;
        }
        AFolder folder = (AFolder)getCorrespondingResource();
        for (AResource member : folder) {
            if (member.getType() == AResourceType.FILE) {
                AFile file = (AFile)member;
                if (type.getFileExtension().equals(file.getExtension())) {
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
     * @throws IpsException if an error occurs while searching.
     */
    public void findIpsSourceFilesStartingWith(IpsObjectType type,
            String prefix,
            boolean ignoreCase,
            List<IIpsSrcFile> result) {

        ArgumentCheck.notNull(type);
        ArgumentCheck.notNull(prefix);
        ArgumentCheck.notNull(result);
        if (!exists()) {
            return;
        }
        AFolder folder = (AFolder)getCorrespondingResource();
        String newPrefix = ignoreCase ? prefix.toLowerCase() : prefix;
        for (AResource member : folder) {
            if (member.getType() == AResourceType.FILE) {
                AFile file = (AFile)member;
                if (type.getFileExtension().equals(file.getExtension())) {
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
    public IIpsPackageFragment createSubPackage(String name, boolean force, IProgressMonitor monitor) {
        if (getIpsProject().getNamingConventions().validateIpsPackageName(name).containsErrorMsg()) {
            throw new IpsException(new Status(IStatus.ERROR, IpsModelActivator.PLUGIN_ID, IStatus.ERROR,
                    MessageFormat.format("{0} is not a valid package name.", name), null)); //$NON-NLS-1$
        }
        return getRoot().createPackageFragment(getSubPackageName(name), true, null);
    }

    @Override
    public boolean hasChildIpsPackageFragments() {
        AFolder folder = (AFolder)getCorrespondingResource();
        for (AResource element : folder) {
            if (element.getType() == AResourceType.FOLDER) {
                if (!getIpsProject().getNamingConventions().validateIpsPackageName(element.getName())
                        .containsErrorMsg()) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public void delete() {
        for (IIpsPackageFragment childPackage : getChildIpsPackageFragments()) {
            childPackage.delete();
        }
        for (IIpsSrcFile childSrcFile : getIpsSrcFiles()) {
            childSrcFile.delete();
        }
        getCorrespondingResource().delete(null);
    }

    private AFile getSortOrderFile() {
        return childOrderComparatorCache.getSortOrderFile();
    }

    private class ChildOrderComparatorCache {

        private AFile sortOrderFile;
        private long lastModification = -1;
        private Comparator<IIpsElement> childOrderComparator;

        private Comparator<IIpsElement> get() {
            AFile file = getSortOrderFile();
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

        private AFile getSortOrderFile() {
            if (sortOrderFile == null) {
                IIpsElement parent = isDefaultPackage() ? getRoot() : IpsPackageFragment.this;
                AFolder folder = (AFolder)parent.getCorrespondingResource();
                sortOrderFile = folder.getFile(java.nio.file.Path.of(IIpsPackageFragment.SORT_ORDER_FILE_NAME));
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
                    sortOrderFile.delete(null);
                }
            }
            childOrderComparator = newChildOrderComparator;
        }
    }

    public static class DefinedOrderComparator implements Comparator<IIpsElement>, Serializable {

        private static final long serialVersionUID = 1L;

        private final Map<IIpsElement, Integer> sortOrder;

        public DefinedOrderComparator() {
            sortOrder = new LinkedHashMap<>();
        }

        public DefinedOrderComparator(IIpsElement... orderedElements) {
            sortOrder = new LinkedHashMap<>(orderedElements.length);
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
                AFile sortOrderFile = parentPackage.getSortOrderFile();
                String charsetName = parentPackage.getIpsProject().getPlainTextFileCharset();
                if (sortOrderFile != null && sortOrderFile.exists()) {
                    try {
                        String content = StringUtil.readFromInputStream(sortOrderFile.getContents(),
                                Charset.forName(charsetName));
                        /*
                         * do not use system line separator here because the file could be
                         * transfered from another system. This regex splits the content at \r\n
                         * (windows), \n (unix) or \r (old mac)
                         */
                        String[] lines = content.split("[\r\n]++"); //$NON-NLS-1$
                        LinkedHashMap<IIpsElement, Integer> sortOrder = new LinkedHashMap<>(
                                lines.length);
                        for (String line : lines) {
                            read(line.trim(), parentPackage, sortOrder);
                        }
                        return sortOrder;
                    } catch (IOException e) {
                        throw new IpsException(new IpsStatus(e));
                    } catch (IpsException e) {
                        IpsLog.log(e);
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
                        // don't check for .exists(), as elements might arrive later during a
                        // refactoring
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
             * @return <code>true</code> if it is a valid entry; <code>false</code> if line is empty
             *             or a comment
             */
            private static boolean isNeitherBlankNorComment(String line) {
                return !IpsStringUtils.isBlank(line) && !line.startsWith("#"); //$NON-NLS-1$
            }

            static void write(IpsPackageFragment parentPackage, Map<IIpsElement, Integer> sortOrder) {
                final AFile sortOrderFile = parentPackage.getSortOrderFile();
                final String charsetName = parentPackage.getIpsProject().getPlainTextFileCharset();
                final String content = toPersistenceContent(sortOrder);

                ICoreRunnable runnable = $ -> {

                    byte[] bytes = content.getBytes(Charset.forName(charsetName));

                    ByteArrayInputStream is = new ByteArrayInputStream(bytes);
                    // overwrite existing files
                    if (sortOrderFile.exists()) {
                        sortOrderFile.setContents(is, true, null);
                    } else {
                        sortOrderFile.create(is, null);
                    }
                };
                Abstractions.getWorkspace().run(runnable, null);

            }

            static String toPersistenceContent(Map<IIpsElement, Integer> sortOrder) {
                StringBuilder sb = new StringBuilder(Messages.IpsPackageFragmentArbitrarySortDefinition_CommentLine);
                sb.append(System.lineSeparator());
                for (IIpsElement element : sortOrder.keySet()) {
                    String name = element instanceof IIpsSrcFile
                            ? ((IIpsSrcFile)element).getQualifiedNameType().getFileName()
                            : QNameUtil.getUnqualifiedName(element.getName());
                    sb.append(name);
                    sb.append(System.lineSeparator());
                }
                return sb.toString();
            }

        }

    }

}
