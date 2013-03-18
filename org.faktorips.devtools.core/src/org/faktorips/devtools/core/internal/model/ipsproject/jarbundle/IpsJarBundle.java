/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.internal.model.ipsproject.jarbundle;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.internal.model.ipsproject.ArchiveIpsPackageFragmentRoot;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsBundleManifest;
import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.core.model.ipsproject.IIpsArchive;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.util.StreamUtil;

/**
 * This implementation of {@link IIpsArchive} represents a packed version of an IPS bundle. The
 * class handles every access to the {@link JarFile} like reading any resources or giving
 * information about included files. The information about included objects are read from
 * MANIFEST.MF. A detailed description is provided by {@link IpsBundleManifest}.
 * 
 * @see IpsBundleManifest
 * 
 * @author dirmeier
 */
public class IpsJarBundle implements IIpsArchive {

    private IpsBundleManifest bundleManifest;

    private final ArchiveIpsPackageFragmentRoot root;

    private IpsJarBundleContentIndex bundleContentIndex;

    private final JarFileFactory jarFileFactory;

    /**
     * Constructs a {@link IpsJarBundle} for the specified {@link IIpsProject}. The
     * {@link JarFileFactory} is used to create a {@link JarFile} when ever needed. After you have
     * constructed the {@link IpsJarBundle} you need to call {@link #initJarFile()}. This method
     * will read the manifest and initializes the content.
     * 
     * @see #initJarFile()
     */
    public IpsJarBundle(IIpsProject ipsProject, JarFileFactory jarFileFactory) {
        this.jarFileFactory = jarFileFactory;
        root = new ArchiveIpsPackageFragmentRoot(ipsProject, this);
    }

    JarFile getJarFile() {
        try {
            return jarFileFactory.createJarFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Initializes the jar file manifest and index the content to get fast access to the jar file.
     * This method should be called directly after constructing the object and before calling any
     * other method. It is separated from constructor because it throws an {@link IOException}.
     * 
     * @throws IOException in case of reading errors while accessing the jar file
     */
    public void initJarFile() throws IOException {
        JarFile jarFile = getJarFile();
        Manifest manifest = jarFile.getManifest();
        if (manifest != null) {
            bundleManifest = new IpsBundleManifest(manifest);
            bundleContentIndex = new IpsJarBundleContentIndex(jarFile, bundleManifest.getObjectDirs());
        }
    }

    IpsBundleManifest getManifest() {
        return bundleManifest;
    }

    IpsJarBundleContentIndex getBundleContentIndex() {
        return bundleContentIndex;
    }

    void setBundleContentIndex(IpsJarBundleContentIndex bundleContentIndex) {
        this.bundleContentIndex = bundleContentIndex;
    }

    @Override
    public boolean isValid() {
        return bundleManifest != null;
    }

    @Override
    public IPath getArchivePath() {
        return getLocation();
    }

    @Override
    public IPath getLocation() {
        return jarFileFactory.getJarPath();
    }

    @Override
    public IIpsPackageFragmentRoot getRoot() {
        return root;
    }

    IPath getRootFolder(IPath path) {
        return bundleContentIndex.getModelPath(path);
    }

    @Override
    public boolean exists() {
        return isValid();
    }

    @Override
    public String[] getNonEmptyPackages() throws CoreException {
        Set<String> nonEmptyPackagePaths = bundleContentIndex.getNonEmptyPackagePaths();
        String[] result = nonEmptyPackagePaths.toArray(new String[nonEmptyPackagePaths.size()]);
        return result;
    }

    @Override
    public boolean containsPackage(String name) throws CoreException {
        if (StringUtils.EMPTY.equals(name)) {
            return true;
        }
        String prefix = getPackagePrefix(name);
        String[] nonEmptyPackages = getNonEmptyPackages();
        for (String packageName : nonEmptyPackages) {
            if (packageName.equals(name) || packageName.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String[] getNonEmptySubpackages(String pack) throws CoreException {
        ArrayList<String> result = new ArrayList<String>();
        String prefix = getPackagePrefix(pack);
        String[] nonEmptyPackages = getNonEmptyPackages();
        for (String packageName : nonEmptyPackages) {
            if (packageName.startsWith(prefix)) {
                result.add(packageName);
            }
        }
        return result.toArray(new String[result.size()]);
    }

    private String getPackagePrefix(String pack) {
        if (StringUtils.isEmpty(pack)) {
            return StringUtils.EMPTY;
        } else {
            return pack + '.';
        }
    }

    @Override
    public Set<QualifiedNameType> getQNameTypes() throws CoreException {
        return bundleContentIndex.getQualifiedNameTypes();
    }

    @Override
    public Set<QualifiedNameType> getQNameTypes(String packName) throws CoreException {
        return bundleContentIndex.getQualifiedNameTypes(packName);
    }

    @Override
    public boolean contains(QualifiedNameType qnt) throws CoreException {
        return getQNameTypes().contains(qnt);
    }

    @Override
    public InputStream getContent(QualifiedNameType qnt) throws CoreException {
        if (qnt == null) {
            return null;
        }
        if (!contains(qnt)) {
            return null;
        }
        IPath entryPath = getRootFolder(qnt.toPath()).append(qnt.toPath());
        return getResourceAsStream(entryPath);
    }

    @Override
    public InputStream getResourceAsStream(String pathName) throws CoreException {
        Path path = new Path(pathName);
        IPath rootFolder = getRootFolder(path);
        return getResourceAsStream(rootFolder.append(path));
    }

    private InputStream getResourceAsStream(IPath path) {
        JarFile jarFile = getJarFile();
        try {
            ZipEntry zipEntry = jarFile.getEntry(path.toPortableString());
            throwExceptionWhenNotFound(zipEntry, path);
            return getInputStream(jarFile, zipEntry);
        } finally {
            closeJarFile(jarFile);
        }

    }

    private void throwExceptionWhenNotFound(ZipEntry zipEntry, IPath path) {
        if (zipEntry == null) {
            throw new CoreRuntimeException("There is no entry " + path + " in " + getLocation()); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    private InputStream getInputStream(JarFile jarFile, ZipEntry zipEntry) {
        InputStream inputStream = null;
        try {
            inputStream = jarFile.getInputStream(zipEntry);
            return StreamUtil.copy(inputStream, 1024);
        } catch (IOException e) {
            throw newRuntimeException("Error while reading jar file " + getLocation(), e); //$NON-NLS-1$
        }
    }

    private RuntimeException newRuntimeException(String message, IOException e) {
        return new RuntimeException(message, e);
    }

    private void closeJarFile(JarFile jarFile) {
        try {
            jarFile.close();
        } catch (IOException e) {
            throw new RuntimeException("Error while closeing jar file " + getLocation(), e); //$NON-NLS-1$
        }
    }

    @Override
    public String getBasePackageNameForMergableArtefacts(QualifiedNameType qnt) throws CoreException {
        String objectDir = getRootFolder(qnt.toPath()).toPortableString();
        return bundleManifest.getBasePackage(objectDir);
    }

    /**
     * {@inheritDoc}
     * <p>
     * For {@link IpsJarBundle} we do not have the ability to configure different base packages for
     * mergable and derived artifacts. So this method always returns the same as
     * {@link #getBasePackageNameForMergableArtefacts(QualifiedNameType)}
     */
    @Override
    public String getBasePackageNameForDerivedArtefacts(QualifiedNameType qnt) throws CoreException {
        return getBasePackageNameForMergableArtefacts(qnt);
    }

    /**
     * {@inheritDoc}
     * <p>
     * For {@link IpsJarBundleEntry} this method always return false. First the jar bundle normally
     * is located outside of the workspace, second we assume there are no changed in existing
     * bundles while running the platform.
     */
    @Override
    public boolean isAffectedBy(IResourceDelta delta) {
        return false;
    }

    /**
     * {@inheritDoc}
     * <p>
     * The jar file of an IpsJarBundle normally lies outside of the workspace. Hence we cannot get
     * an existing {@link IResource} for this jar file. To be compatible to other parts of the
     * framework we return a virtual resource. The name of the resource is the name of the archive
     * located in the project. Because it is not allowed to have to archive references with the same
     * name this strategy cannot run into conflicts.
     */
    @Override
    public IResource getCorrespondingResource() {
        return root.getIpsProject().getProject().getFile(root.getName());
    }

}
