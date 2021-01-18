/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.ipsproject.bundle;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.faktorips.devtools.model.internal.ipsproject.IpsBundleManifest;
import org.faktorips.devtools.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;

/**
 * This AbstractIpsBundle is an abstract implementation of a storage, which is included vi this
 * OSGi-mechanism. Therefor it contains a reference to the manifest, which is represented by an
 * {@link IpsBundleManifest}.
 * <p>
 * The information about included objects are read from MANIFEST.MF. A detailed description is
 * provided by {@link IpsBundleManifest}.
 * 
 * @see IpsBundleManifest
 * 
 * @author dicker
 */
public abstract class AbstractIpsBundle extends AbstractIpsStorage {

    private IpsBundleManifest bundleManifest;
    private AbstractIpsBundleContentIndex bundleContentIndex;

    public AbstractIpsBundle(IIpsProject ipsProject) {
        super(ipsProject);
    }

    @Override
    public boolean isValid() {
        return bundleManifest != null && bundleManifest.hasObjectDirs();
    }

    @Override
    public boolean exists() {
        return isValid();
    }

    /**
     * {@inheritDoc}
     * <p>
     * For {@link IpsBundleEntry} this method always return false. First the jar bundle normally is
     * located outside of the workspace, second we assume there are no changes in existing bundles
     * while running the platform.
     */
    @Override
    public boolean isAffectedBy(IResourceDelta delta) {
        return false;
    }

    @Override
    public String getBasePackageNameForMergableArtefacts(QualifiedNameType qnt) throws CoreException {
        String objectDir = getRootFolder(qnt.toPath()).toPortableString();
        return bundleManifest.getBasePackage(objectDir);
    }

    /**
     * reads the MANIFEST.MF and initializes the bundle based on it.
     * 
     */
    public abstract void initBundle() throws IOException;

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

    public IpsBundleManifest getBundleManifest() {
        return bundleManifest;
    }

    protected void setBundleManifest(IpsBundleManifest bundleManifest) {
        this.bundleManifest = bundleManifest;
    }

    /**
     * returns the root folder
     */
    IPath getRootFolder(IPath path) {
        return bundleContentIndex.getModelPath(path);
    }

    @Override
    public String[] getNonEmptyPackages() throws CoreException {
        Set<String> nonEmptyPackagePaths = bundleContentIndex.getNonEmptyPackagePaths();
        String[] result = nonEmptyPackagePaths.toArray(new String[nonEmptyPackagePaths.size()]);
        return result;
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
    public boolean contains(IPath path) {
        return bundleContentIndex.getModelPath(path) != null;
    }

    @Override
    public InputStream getContent(IPath path) {
        if (path == null) {
            return null;
        }
        IPath rootFolder = getRootFolder(path);
        IPath entryPath = rootFolder.append(path);
        return getResourceAsStream(entryPath);
    }

    @Override
    public InputStream getResourceAsStream(String pathName) {
        Path path = new Path(pathName);
        IPath rootFolder = getRootFolder(path);
        return getResourceAsStream(rootFolder.append(path));
    }

    /**
     * Returns the InputStream of the resource specified by the given path. The path is relative to
     * the root of this bundle.
     * <p>
     * The caller is responsible for closing the {@link InputStream}.
     * 
     * @param resourcePath The path to the requested resource, relative to this bundle's root
     * 
     * @return An InputStream that represents the content of the requested resource.
     */
    protected abstract InputStream getResourceAsStream(IPath resourcePath);

    @Override
    public String getName() {
        return getLocation().lastSegment();
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
        return getIpsProject().getProject().getFile(getLocation().lastSegment());
    }

    AbstractIpsBundleContentIndex getBundleContentIndex() {
        return bundleContentIndex;
    }

    /**
     * sets an {@link AbstractIpsBundleContentIndex} for exploring the file structure of the bundle
     */
    protected void setBundleContentIndex(AbstractIpsBundleContentIndex bundleContentIndex) {
        this.bundleContentIndex = bundleContentIndex;
    }
}