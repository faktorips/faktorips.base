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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.eclipse.core.runtime.IPath;
import org.faktorips.devtools.abstraction.AFile;
import org.faktorips.devtools.abstraction.AProject;
import org.faktorips.devtools.abstraction.AResource;
import org.faktorips.devtools.abstraction.AResourceDelta;
import org.faktorips.devtools.abstraction.AWorkspaceRoot;
import org.faktorips.devtools.abstraction.Abstractions;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.abstraction.util.PathUtil;
import org.faktorips.devtools.model.CreateIpsArchiveOperation;
import org.faktorips.devtools.model.abstractions.WorkspaceAbstractions;
import org.faktorips.devtools.model.internal.IpsModel;
import org.faktorips.devtools.model.internal.ipsproject.bundle.AbstractIpsStorage;
import org.faktorips.devtools.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.model.ipsproject.IIpsArchive;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.plugin.IpsStatus;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.faktorips.util.IoUtil;
import org.faktorips.util.StreamUtil;

/**
 * An archive for IPS objects. It is physically stored in a jar file. This class gives read-only
 * access to the archive's content. A new archive can be created with the
 * {@link CreateIpsArchiveOperation}.
 *
 * @see org.faktorips.devtools.model.CreateIpsArchiveOperation
 */
public class IpsArchive extends AbstractIpsStorage implements IIpsArchive {

    private final Path archivePath;

    private long modificationStamp;

    /** package name as key, content as value. content stored as a set of paths */
    private HashMap<String, Set<QualifiedNameType>> packs = null;

    /** map with IPath as keys and IpsObjectProperties as values. */
    private LinkedHashMap<Path, IpsObjectProperties> paths = null;

    public IpsArchive(IIpsProject ipsProject, Path path) {
        super(ipsProject);
        archivePath = path;
    }

    @Override
    public Path getLocation() {
        if (archivePath == null) {
            return null;
        }
        AResource resource = getCorrespondingResource();
        if (resource != null) {
            return PathUtil.fromOSString(resource.getLocation().toString());
        }
        File extFile = archivePath.toFile();
        return PathUtil.fromOSString(extFile.getAbsolutePath());
    }

    @Override
    public boolean isAffectedBy(AResourceDelta delta) {
        AWorkspaceRoot wsRoot = Abstractions.getWorkspace().getRoot();
        AFile file = wsRoot.getFileForLocation(getLocation());
        if (file == null) {
            // file is outside the workspace
            return false;
        }
        if (delta.findMember(
                PathUtil.fromOSString(PathUtil.toPortableString(file.getProjectRelativePath()))) != null) {
            return true;
        }
        return false;
    }

    @Override
    public Path getArchivePath() {
        return archivePath;
    }

    @Override
    public boolean exists() {
        AResource resource = getCorrespondingResource();
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
        try {
            readArchiveContentIfNecessary();
        } catch (IpsException e) {
            return false;
        }
        return packs != null && paths != null;
    }

    @Override
    public String[] getNonEmptyPackages() {
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
    public boolean contains(Path path) {
        readArchiveContentIfNecessary();
        return paths.containsKey(path);
    }

    @Override
    public Set<QualifiedNameType> getQNameTypes() {
        readArchiveContentIfNecessary();
        TreeSet<QualifiedNameType> qualifiedNameTypes = new TreeSet<>();
        for (Path path : paths.keySet()) {
            if (QualifiedNameType.representsQualifiedNameType(path.toString())) {
                QualifiedNameType qualifedNameType = QualifiedNameType.newQualifedNameType(path.toString());
                qualifiedNameTypes.add(qualifedNameType);
            }
        }
        return qualifiedNameTypes;
    }

    @Override
    public Set<QualifiedNameType> getQNameTypes(String packName) {
        readArchiveContentIfNecessary();
        Set<QualifiedNameType> qnts = packs.get(packName);
        if (qnts == null) {
            return new HashSet<>(0);
        }
        return new HashSet<>(qnts);
    }

    @Override
    public InputStream getContent(Path path) {
        if (path == null) {
            return null;
        }
        readArchiveContentIfNecessary();
        if (!paths.containsKey(path)) {
            return null;
        }
        String entryName = IIpsArchive.IPSOBJECTS_FOLDER + IPath.SEPARATOR + PathUtil.toPortableString(path);
        return getResourceAsStream(entryName);
    }

    private void readArchiveContentIfNecessary() {
        synchronized (this) {
            if (!exists()) {
                packs = new HashMap<>();
                paths = new LinkedHashMap<>();
                return;
            }
            if (packs == null || paths == null) {
                readArchiveContent();
                return;
            }
            if (getActualFileModificationStamp() != modificationStamp) {
                readArchiveContent();
            }
        }
    }

    private long getActualFileModificationStamp() {
        AResource resource = getCorrespondingResource();
        if (resource == null) {
            return getFileFromPath().lastModified();
        } else {
            return resource.getModificationStamp();
        }
    }

    private void readArchiveContent() {
        if (!exists()) {
            throw new IpsException(new IpsStatus("IpsArchive file " + getLocation() + " does not exist!")); //$NON-NLS-1$ //$NON-NLS-2$
        }
        if (IpsModel.TRACE_MODEL_MANAGEMENT) {
            System.out.println("Reading archive content from disk: " + this); //$NON-NLS-1$
        }
        packs = new HashMap<>(200);

        File file = getFileFromPath();

        modificationStamp = getActualFileModificationStamp();
        JarFile jar = null;
        try {
            jar = new JarFile(file);
            indexContent(jar);
        } catch (IOException e) {
            throw new IpsException(new IpsStatus("Error reading IPS archive " + getLocation(), e)); //$NON-NLS-1$
        } finally {
            try {
                if (jar != null) {
                    jar.close();
                }
            } catch (IOException e) {
                throw new IpsException(new IpsStatus("Error closing IPS archive " + getLocation())); //$NON-NLS-1$
            }
        }
    }

    private void indexContent(JarFile jar) {
        SortedMap<Path, IpsObjectProperties> pathsTmp = new TreeMap<>(Comparator.comparing(Path::toString));
        Properties ipsObjectProperties = readIpsObjectsProperties(jar);
        for (Enumeration<?> e = jar.entries(); e.hasMoreElements();) {
            JarEntry entry = (JarEntry)e.nextElement();
            if (entry.isDirectory()) {
                continue;
            }
            Path path = getPath(entry);
            if (path == null) {
                continue;
            }
            String basePackageMergable = getPropertyValue(ipsObjectProperties, path,
                    IIpsArchive.PROPERTY_POSTFIX_BASE_PACKAGE_MERGABLE);
            if (basePackageMergable == null) {
                // for archives created with versions up to 2.2.5
                basePackageMergable = getPropertyValue(ipsObjectProperties, path, "basePackage"); //$NON-NLS-1$
            }
            String basePackageDerived = getPropertyValue(ipsObjectProperties, path,
                    IIpsArchive.PROPERTY_POSTFIX_BASE_PACKAGE_DERIVED);
            if (basePackageDerived == null) {
                // for archives created with versions up to 2.2.5
                basePackageDerived = getPropertyValue(ipsObjectProperties, path, "extensionPackage"); //$NON-NLS-1$
            }

            IpsObjectProperties props = new IpsObjectProperties(basePackageMergable, basePackageDerived);
            pathsTmp.put(path, props);
            String pathName = PathUtil.toPortableString(path);
            if (QualifiedNameType.representsQualifiedNameType(pathName)) {
                QualifiedNameType qualifedNameType = QualifiedNameType.newQualifedNameType(pathName);
                Set<QualifiedNameType> content = packs.computeIfAbsent(qualifedNameType.getPackageName(),
                        $ -> new HashSet<>());
                content.add(qualifedNameType);
            }
        }
        paths = new LinkedHashMap<>(pathsTmp);
    }

    private File getFileFromPath() {
        return WorkspaceAbstractions.getFileFromArchivePath(this);
    }

    private Properties readIpsObjectsProperties(JarFile archive) {
        JarEntry entry = archive.getJarEntry(IIpsArchive.JAVA_MAPPING_ENTRY_NAME);
        if (entry == null) {
            throw new IpsException(
                    new IpsStatus("Entry " + JAVA_MAPPING_ENTRY_NAME + " not found in archive " + archivePath)); //$NON-NLS-1$ //$NON-NLS-2$
        }
        InputStream is = null;
        try {
            is = archive.getInputStream(entry);
            Properties props = new Properties();
            props.load(is);
            return props;
        } catch (IOException e) {
            throw new IpsException(
                    new IpsStatus("Error reading " + JAVA_MAPPING_ENTRY_NAME + " from archive " + archivePath, e)); //$NON-NLS-1$ //$NON-NLS-2$
        } finally {
            IoUtil.close(is);
        }
    }

    protected Path getPath(JarEntry jarEntry) {
        // IPS object paths begins after "ipsobject/"
        final String name = jarEntry.getName();
        Path path = Path.of(name);
        if (PathUtil.segment(path, 0).equals(IPSOBJECTS_FOLDER)) {
            return PathUtil.removeFirstSegments(path, 1);
        } else {
            if (IProductCmptType.SUPPORTED_ICON_EXTENSIONS.contains(PathUtil.getFileExtension(path))) {
                return path;
            } else {
                return null;
            }
        }
    }

    private String getPropertyValue(Properties properties, Path path, String postfix) {
        String key = PathUtil.toPortableString(path) + IIpsArchive.QNT_PROPERTY_POSTFIX_SEPARATOR + postfix;
        return properties.getProperty(key);
    }

    @Override
    public String toString() {
        return "Archive " + getIpsProject() + "/" + archivePath; //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Override
    public String getBasePackageNameForMergableArtefacts(QualifiedNameType qualifiedNameType) {
        readArchiveContentIfNecessary();
        IpsObjectProperties props = paths.get(qualifiedNameType.toPath());
        if (props == null) {
            return null;
        }
        return props.basePackageMergable;
    }

    @Override
    public String getBasePackageNameForDerivedArtefacts(QualifiedNameType qualifiedNameType) {
        readArchiveContentIfNecessary();
        IpsObjectProperties props = paths.get(qualifiedNameType.toPath());
        if (props == null) {
            return null;
        }
        return props.extensionPackageDerived;
    }

    @Override
    public AResource getCorrespondingResource() {
        if (PathUtil.isAbsoluteInWorkspace(archivePath)) {
            AWorkspaceRoot wsRoot = Abstractions.getWorkspace().getRoot();
            /*
             * on Unix, absolute paths always start with a slash (/). It is not possible to
             * distinguish between workspace absolut paths and absolute path to locations outside
             * the workspace so we check, if the first segment identifies a project.
             */
            if ((archivePath.getNameCount() == 0) || !wsRoot.getProject(PathUtil.segment(archivePath, 0)).exists()) {
                return null;
                // the archive is not located in the workspace
            }
            return wsRoot.getFile(archivePath);
        } else if (archivePath.isAbsolute()) {
            return null;
        }
        AProject project = getIpsProject().getProject();
        return project.getFile(archivePath);
    }

    @Override
    public InputStream getResourceAsStream(String path) {
        if (path == null) {
            return null;
        }
        readArchiveContentIfNecessary();
        JarFile archive = null;
        try {
            File archiveFile = getFileFromPath();
            archive = new JarFile(archiveFile);
            JarEntry entry = archive.getJarEntry(path);
            if (entry == null) {
                throw new IpsException(new IpsStatus("Entry " + path + " not found in archive " + archivePath)); //$NON-NLS-1$ //$NON-NLS-2$
            }
            try {
                return StreamUtil.copy(archive.getInputStream(entry));
            } catch (IOException e) {
                throw new IpsException(
                        new IpsStatus("Error reading data for " + path + " from archive " + archivePath, e)); //$NON-NLS-1$ //$NON-NLS-2$
            }

        } catch (IOException e) {
            throw new IpsException(new IpsStatus("Error opening jarfile " + archivePath, e)); //$NON-NLS-1$

        } finally {
            try {
                if (archive != null) {
                    archive.close();
                }
            } catch (IOException e) {
                throw new IpsException(
                        new IpsStatus("Error closing stream reading " + path + " from archive " + this, e)); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
    }

    @Override
    public Path getResourcePath(Path elementPath) {
        return Optional.ofNullable(getLocation())
                .map(root -> root.resolve(elementPath))
                .orElse(null);
    }

    @Override
    public String getName() {
        if (archivePath == null) {
            return IpsStringUtils.EMPTY;
        }
        return PathUtil.lastSegment(archivePath);
    }

    @Override
    public boolean isFolder() {
        return false;
    }

    private static class IpsObjectProperties {

        private String basePackageMergable;
        private String extensionPackageDerived;

        public IpsObjectProperties(String basePackageMergable, String extensionPackageDerived) {
            this.basePackageMergable = basePackageMergable;
            this.extensionPackageDerived = extensionPackageDerived;
        }

    }

}
