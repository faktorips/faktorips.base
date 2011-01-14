/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.ipsproject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.internal.model.IpsModel;
import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.core.model.ipsproject.IIpsArchive;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.StreamUtil;

/**
 * An ips archive is an archive for ips objects. It is physically stored in a jar file. This class
 * gives read-only access to the archive's content. A new archive can be created with the
 * CreateIpsArchiveOperation.
 * 
 * @see org.faktorips.devtools.core.model.CreateIpsArchiveOperation
 * 
 * @author Jan Ortmann
 */
public class IpsArchive implements IIpsArchive {

    private final static int IPSOBJECT_FOLDER_NAME_LENGTH = IIpsArchive.IPSOBJECTS_FOLDER.length();

    private IPath archivePath;
    private long modificationStamp;
    private ArchiveIpsPackageFragmentRoot root;

    /** package name as key, content as value. content stored as a set of qNameTypes */
    private HashMap<String, Set<QualifiedNameType>> packs = null;

    /** map with qNameTypes as keys and IpsObjectProperties as values. */
    private LinkedHashMap<QualifiedNameType, IpsObjectProperties> qNameTypes = null;

    public IpsArchive(IIpsProject ipsProject, IPath path) {
        ArgumentCheck.notNull(ipsProject, "The parameter ipsproject cannot be null."); //$NON-NLS-1$
        archivePath = path;
        root = new ArchiveIpsPackageFragmentRoot(ipsProject, this);
    }

    @Override
    public IPath getLocation() {
        if (archivePath == null) {
            return null;
        }
        IResource resource = getCorrespondingResource();
        if (resource != null) {
            return resource.getLocation();
        }
        File extFile = archivePath.toFile();
        return Path.fromOSString(extFile.getAbsolutePath());
    }

    public boolean isContained(IResourceDelta delta) {
        // see javadoc IIpsArchiveEntry#isContained(IResourceDelta)
        IWorkspaceRoot wsRoot = ResourcesPlugin.getWorkspace().getRoot();
        IFile file = wsRoot.getFileForLocation(getLocation());
        if (file == null) {
            return false; // file is outside the workspace
        }
        if (delta.findMember(file.getProjectRelativePath()) != null) {
            return true;
        }
        return false;
    }

    @Override
    public IPath getArchivePath() {
        return archivePath;
    }

    @Override
    public boolean exists() {
        IResource resource = getCorrespondingResource();
        if (resource == null) {
            // its a file outside the workspace
            File extFile = archivePath.toFile();
            return extFile.exists();
        }
        return resource.exists();
    }

    @Override
    public boolean isValid() {
        if (!exists()) {
            return false;
        }
        // File file = getFileFromPath();
        // try {
        // new ZipFile(file);
        // } catch (ZipException e) {
        // return false;
        // } catch (IOException e) {
        // return false;
        // }
        return true;
    }

    @Override
    public String[] getNonEmptyPackages() throws CoreException {
        readArchiveContentIfNecessary();
        String[] packNames = new String[packs.size()];
        int i = 0;
        for (Iterator<String> it = packs.keySet().iterator(); it.hasNext(); i++) {
            packNames[i] = it.next();
        }
        Arrays.sort(packNames);
        return packNames;
    }

    @Override
    public boolean containsPackage(String name) throws CoreException {
        if (name == null) {
            return false;
        }
        if (name.equals("")) { //$NON-NLS-1$
            return true; // default package is always contained.
        }
        readArchiveContentIfNecessary();
        if (packs.containsKey(name)) {
            return true;
        }
        String prefix = name + "."; //$NON-NLS-1$
        for (String pack : packs.keySet()) {
            if (pack.startsWith(prefix)) {
                return true; // given pack name is an empty parent package
            }
        }
        return false;
    }

    @Override
    public String[] getNonEmptySubpackages(String parentPack) throws CoreException {
        if (parentPack == null) {
            return new String[0];
        }
        readArchiveContentIfNecessary();
        Set<String> result = new HashSet<String>();
        for (String nonEmptyPack : packs.keySet()) {
            for (String pack : getParentPackagesIncludingSelf(nonEmptyPack)) {
                if (isChildPackageOf(pack, parentPack)) {
                    result.add(pack);
                }
            }
        }

        String[] packNames = result.toArray(new String[result.size()]);
        Arrays.sort(packNames);

        return packNames;
    }

    private boolean isChildPackageOf(String candidate, String parentPack) {
        if (candidate.equals(parentPack)) {
            return false;
        }
        if (parentPack.equals("")) { // default package //$NON-NLS-1$
            return candidate.indexOf('.') == -1;
        } else {
            if (!candidate.startsWith(parentPack + '.')) {
                return false;
            }
            return StringUtils.countMatches(parentPack, ".") == StringUtils.countMatches(candidate, ".") - 1; //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    @Override
    public boolean contains(QualifiedNameType qnt) throws CoreException {
        readArchiveContentIfNecessary();
        return qNameTypes.containsKey(qnt);
    }

    @Override
    public Set<QualifiedNameType> getQNameTypes() throws CoreException {
        readArchiveContentIfNecessary();
        return qNameTypes.keySet();
    }

    @Override
    public Set<QualifiedNameType> getQNameTypes(String packName) throws CoreException {
        readArchiveContentIfNecessary();
        Set<QualifiedNameType> qnts = packs.get(packName);
        if (qnts == null) {
            return new HashSet<QualifiedNameType>(0);
        }
        Set<QualifiedNameType> packContent = new HashSet<QualifiedNameType>(qnts);
        return packContent;
    }

    @Override
    public InputStream getContent(QualifiedNameType qnt) throws CoreException {
        if (qnt == null) {
            return null;
        }
        readArchiveContentIfNecessary();
        if (!qNameTypes.containsKey(qnt)) {
            return null;
        }
        String entryName = IIpsArchive.IPSOBJECTS_FOLDER + IPath.SEPARATOR + qnt.toPath().toString();
        return getResourceAsStream(entryName);
    }

    private void readArchiveContentIfNecessary() throws CoreException {
        synchronized (this) {
            if (!isValid()) {
                packs = new HashMap<String, Set<QualifiedNameType>>();
                qNameTypes = new LinkedHashMap<QualifiedNameType, IpsObjectProperties>();
                return;
            }
            if (packs == null || qNameTypes == null) {
                readArchiveContent();
                return;
            }
            if (getActualFileModificationStamp() != modificationStamp) {
                readArchiveContent();
                return;
            }
        }
    }

    private long getActualFileModificationStamp() {
        IResource resource = getCorrespondingResource();
        if (resource == null) {
            return getLocation().toFile().lastModified();
        } else {
            return resource.getModificationStamp();
        }
    }

    private void readArchiveContent() throws CoreException {
        if (!exists()) {
            throw new CoreException(new IpsStatus("IpsArchive file " + archivePath + " does not exist!")); //$NON-NLS-1$ //$NON-NLS-2$
        }
        if (IpsModel.TRACE_MODEL_MANAGEMENT) {
            System.out.println("Reading archive content from disk: " + this); //$NON-NLS-1$
        }
        packs = new HashMap<String, Set<QualifiedNameType>>(200);
        SortedMap<QualifiedNameType, IpsObjectProperties> qntTemp = new TreeMap<QualifiedNameType, IpsObjectProperties>();

        File file = getFileFromPath();

        modificationStamp = getActualFileModificationStamp();
        JarFile jar;
        try {
            jar = new JarFile(file);
        } catch (IOException e) {
            throw new CoreException(new IpsStatus("Error reading ips archive " + getLocation(), e)); //$NON-NLS-1$
        }
        Properties ipsObjectProperties = readIpsObjectsProperties(jar);
        for (Enumeration<?> e = jar.entries(); e.hasMoreElements();) {
            JarEntry entry = (JarEntry)e.nextElement();
            if (entry.isDirectory()) {
                continue;
            }
            QualifiedNameType qNameType = getQualifiedNameType(entry);
            if (qNameType == null) {
                continue;
            }
            String basePackageMergable = getPropertyValue(ipsObjectProperties, qNameType,
                    IIpsArchive.PROPERTY_POSTFIX_BASE_PACKAGE_MERGABLE);
            if (basePackageMergable == null) {
                // for archives created with versions up to 2.2.5
                basePackageMergable = getPropertyValue(ipsObjectProperties, qNameType, "basePackage"); //$NON-NLS-1$
            }
            String basePackageDerived = getPropertyValue(ipsObjectProperties, qNameType,
                    IIpsArchive.PROPERTY_POSTFIX_BASE_PACKAGE_DERIVED);
            if (basePackageDerived == null) {
                // for archives created with versions up to 2.2.5
                basePackageDerived = getPropertyValue(ipsObjectProperties, qNameType, "extensionPackage"); //$NON-NLS-1$
            }

            IpsObjectProperties props = new IpsObjectProperties(basePackageMergable, basePackageDerived);
            qntTemp.put(qNameType, props);
            Set<QualifiedNameType> content = packs.get(qNameType.getPackageName());
            if (content == null) {
                content = new HashSet<QualifiedNameType>();
                packs.put(qNameType.getPackageName(), content);
            }
            content.add(qNameType);
        }
        qNameTypes = new LinkedHashMap<QualifiedNameType, IpsObjectProperties>(qntTemp);
        try {
            jar.close();
        } catch (IOException e) {
            throw new CoreException(new IpsStatus("Error closing ips archive " + getLocation())); //$NON-NLS-1$
        }
    }

    private File getFileFromPath() {
        return getLocation().toFile();
    }

    private Properties readIpsObjectsProperties(JarFile archive) throws CoreException {
        JarEntry entry = archive.getJarEntry(IIpsArchive.JAVA_MAPPING_ENTRY_NAME);
        if (entry == null) {
            throw new CoreException(new IpsStatus(
                    "Entry " + IIpsArchive.JAVA_MAPPING_ENTRY_NAME + " not found in archive for " + this)); //$NON-NLS-1$ //$NON-NLS-2$
        }
        InputStream is = null;
        try {
            is = archive.getInputStream(entry);
            Properties props = new Properties();
            props.load(is);
            return props;
        } catch (IOException e) {
            throw new CoreException(new IpsStatus("Error reading ipsobjects.properties from archive for " + this, e)); //$NON-NLS-1$
        }
    }

    private QualifiedNameType getQualifiedNameType(JarEntry jarEntry) {
        try {
            String path = jarEntry.getName().substring(IPSOBJECT_FOLDER_NAME_LENGTH + 1); // qName
            // path begins after "ipsobject/"
            return QualifiedNameType.newQualifedNameType(path);
        } catch (Exception e) {
            return null; // the entry does not contain an ips object
        }
    }

    private String getPropertyValue(Properties properties, QualifiedNameType qnt, String postfix) {
        String key = qnt.toPath().toString() + IIpsArchive.QNT_PROPERTY_POSTFIX_SEPARATOR + postfix;
        return properties.getProperty(key);
    }

    @Override
    public String toString() {
        return "Archive " + archivePath; //$NON-NLS-1$
    }

    private List<String> getParentPackagesIncludingSelf(String pack) {
        ArrayList<String> result = new ArrayList<String>();
        result.add(pack);
        getParentPackages(pack, result);
        return result;
    }

    private void getParentPackages(String pack, List<String> result) {
        int index = pack.lastIndexOf('.');
        if (index == -1) {
            return;
        }
        String parentPack = pack.substring(0, index);
        result.add(parentPack);
        getParentPackages(parentPack, result);
    }

    @Override
    public String getBasePackageNameForMergableArtefacts(QualifiedNameType qnt) throws CoreException {
        readArchiveContentIfNecessary();
        IpsObjectProperties props = qNameTypes.get(qnt);
        if (props == null) {
            return null;
        }
        return props.basePackageMergable;
    }

    @Override
    public String getBasePackageNameForDerivedArtefacts(QualifiedNameType qnt) throws CoreException {
        readArchiveContentIfNecessary();
        IpsObjectProperties props = qNameTypes.get(qnt);
        if (props == null) {
            return null;
        }
        return props.extensionPackageDerived;
    }

    /**
     * Returns an IResource only if the resource can be located by the
     * {@link IWorkspaceRoot#getFileForLocation(IPath)} method.
     */
    public IResource getCorrespondingResource() {
        if (archivePath.isAbsolute()) {
            if (archivePath.getDevice() != null || archivePath.isUNC()) {
                // on Windows we can determin files outside the workspace with the device (like C:)
                return null;
            }
            IWorkspaceRoot wsRoot = ResourcesPlugin.getWorkspace().getRoot();
            if (archivePath.segmentCount() == 0) {
                return null;
            }
            /*
             * on Unix, absolute paths always start with a slash (/). It is not possible to
             * distinguish between workspace absolut paths and absolute path to locations outside
             * the workspace so we check, if the first segment identifies a project.
             */
            if (!wsRoot.getProject(archivePath.segment(0)).exists()) {
                return null;
                // the archive is not located in the workspace
            }
            return wsRoot.getFile(archivePath);
        }
        IProject project = root.getIpsProject().getProject();
        return project.getFile(archivePath);
    }

    @Override
    public IIpsPackageFragmentRoot getRoot() {
        return root;
    }

    class IpsObjectProperties {

        String basePackageMergable;
        String extensionPackageDerived;

        public IpsObjectProperties(String basePackageMergable, String extensionPackageDerived) {
            this.basePackageMergable = basePackageMergable;
            this.extensionPackageDerived = extensionPackageDerived;
        }

    }

    @Override
    public InputStream getResourceAsStream(String path) throws CoreException {
        if (path == null) {
            return null;
        }
        readArchiveContentIfNecessary();
        JarFile archive;
        try {
            File archiveFile = getFileFromPath();
            archive = new JarFile(archiveFile);
        } catch (IOException e) {
            throw new CoreException(new IpsStatus("Error opening jar for " + this, e)); //$NON-NLS-1$
        }
        JarEntry entry = archive.getJarEntry(path);
        if (entry == null) {
            throw new CoreException(new IpsStatus("Entry not found in archive for " + this)); //$NON-NLS-1$
        }
        try {
            return StreamUtil.copy(archive.getInputStream(entry), 1024);
        } catch (IOException e) {
            throw new CoreException(new IpsStatus("Error reading data from archive for " + this, e)); //$NON-NLS-1$
        } finally {
            try {
                archive.close();
            } catch (Exception e) {
                throw new CoreException(new IpsStatus("Error closing stream or archive for " + this, e)); //$NON-NLS-1$
            }
        }
    }
}
