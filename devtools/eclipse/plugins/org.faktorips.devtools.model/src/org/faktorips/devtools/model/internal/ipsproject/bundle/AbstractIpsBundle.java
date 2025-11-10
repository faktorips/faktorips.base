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
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.Set;

import org.faktorips.devtools.abstraction.AResource;
import org.faktorips.devtools.abstraction.AResourceDelta;
import org.faktorips.devtools.abstraction.util.PathUtil;
import org.faktorips.devtools.model.internal.ipsproject.IpsBundleManifest;
import org.faktorips.devtools.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.osgi.framework.BundleException;

/**
 * This AbstractIpsBundle is an abstract implementation of a storage, which is included vi this
 * OSGi-mechanism. Therefore it contains a reference to the manifest, which is represented by an
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
        try {
            return bundleManifest != null && bundleManifest.hasObjectDirs();
        } catch (BundleException e) {
            String message = MessageFormat.format(Messages.AbstractIpsBundle_msg_error_while_parsing, getLocation());
            throw new RuntimeException(message, e);
        }
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
    public boolean isAffectedBy(AResourceDelta delta) {
        return false;
    }

    @Override
    public String getBasePackageNameForMergableArtefacts(QualifiedNameType qnt) {
        String objectDir = PathUtil.toPortableString(getRootFolder(qnt.toPath()));
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
    public String getBasePackageNameForDerivedArtefacts(QualifiedNameType qnt) {
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
    Path getRootFolder(Path path) {
        return bundleContentIndex.getModelPath(path);
    }

    @Override
    public String[] getNonEmptyPackages() {
        Set<String> nonEmptyPackagePaths = bundleContentIndex.getNonEmptyPackagePaths();
        return nonEmptyPackagePaths.toArray(new String[nonEmptyPackagePaths.size()]);
    }

    @Override
    public Set<QualifiedNameType> getQNameTypes() {
        return bundleContentIndex.getQualifiedNameTypes();
    }

    @Override
    public Set<QualifiedNameType> getQNameTypes(String packName) {
        return bundleContentIndex.getQualifiedNameTypes(packName);
    }

    @Override
    public boolean contains(Path path) {
        return bundleContentIndex.getModelPath(path) != null;
    }

    @Override
    public InputStream getContent(Path path) {
        if (path == null) {
            return null;
        }
        Path rootFolder = getRootFolder(path);
        Path entryPath = rootFolder.resolve(path);
        return getResourceAsStream(entryPath);
    }

    @Override
    public InputStream getResourceAsStream(String pathName) {
        Path path = Path.of(pathName);
        Path rootFolder = getRootFolder(path);
        return getResourceAsStream(rootFolder.resolve(path));
    }

    @Override
    public Path getResourcePath(Path element) {
        return getRootFolder(element).resolve(element);
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
    protected abstract InputStream getResourceAsStream(Path resourcePath);

    @Override
    public String getName() {
        return PathUtil.lastSegment(getLocation());
    }

    /**
     * {@inheritDoc}
     * <p>
     * The jar file of an IpsJarBundle normally lies outside of the workspace. Hence we cannot get
     * an existing {@link AResource} for this jar file. To be compatible to other parts of the
     * framework we return a virtual resource. The name of the resource is the name of the archive
     * located in the project. Because it is not allowed to have to archive references with the same
     * name this strategy cannot run into conflicts.
     */
    @Override
    public AResource getCorrespondingResource() {
        return getIpsProject().getProject().getFile(PathUtil.lastSegment(getLocation()));
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
