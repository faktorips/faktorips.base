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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

import org.faktorips.devtools.abstraction.util.PathUtil;
import org.faktorips.devtools.model.internal.ipsproject.IpsBundleManifest;
import org.faktorips.devtools.model.internal.ipsproject.IpsLibraryEntry;
import org.faktorips.devtools.model.internal.ipsproject.IpsObjectPath;
import org.faktorips.devtools.model.internal.ipsproject.IpsObjectPathEntry;
import org.faktorips.devtools.model.internal.ipsproject.LibraryIpsPackageFragmentRoot;
import org.faktorips.devtools.model.internal.ipsproject.jdtcontainer.IpsContainer4JdtClasspathContainer;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.model.ipsproject.IIpsStorage;
import org.faktorips.devtools.model.ipsproject.bundle.IIpsBundleEntry;
import org.faktorips.runtime.MessageList;

/**
 * An {@link IpsBundleEntry} is ab {@link IpsObjectPathEntry} that references a an IPS bundle in a
 * JAR file or a folder. An IPS bundle needs to have a MANIFEST.MF containing all necessary
 * information about the included IPS objects and the generated source code. The structure of the
 * MANIFEST.MF is explained in detail in {@link IpsBundleManifest}.
 * <p>
 * Normally an {@link IpsBundleEntry} is resolved by using a classpath container like
 * {@link IpsContainer4JdtClasspathContainer}. Mostly for testing purposes this entry has also the
 * ability to be initialized by the {@link IpsObjectPath} in the {@link IIpsProjectProperties}. To
 * use this approach you have to setup the following entry:
 * <p>
 * <code>
 * &lt;Entry type="bundle" bundlePath="path/to/grundmodell.jar"/&gt;
 * </code>
 * 
 * @author dirmeier
 */
public class IpsBundleEntry extends IpsLibraryEntry implements IIpsBundleEntry {

    private static final String XML_ATTRIBUTE_PATH = "bundlePath"; //$NON-NLS-1$

    private IIpsStorage ipsBundle;

    private IpsStorageFactory ipsStorageFactory;

    /**
     * This constructor creates a new {@link IpsBundleEntry} for the given {@link IpsObjectPath}.
     * After you created the {@link IpsBundleEntry} you need to initialize the bundle by calling
     * {@link #initStorage(Path)}.
     * 
     * @param ipsObjectPath The parent {@link IpsObjectPath} of this entry
     */
    public IpsBundleEntry(IpsObjectPath ipsObjectPath) {
        super(ipsObjectPath);
        ipsStorageFactory = new IpsStorageFactory();
    }

    protected void setIpsStorageFactory(IpsStorageFactory ipsStorageFactory) {
        this.ipsStorageFactory = ipsStorageFactory;
    }

    @Override
    public void initStorage(Path bundlePath) throws IOException {
        File bundleFile = bundlePath.toFile();
        if (bundleFile.isDirectory()) {
            initFolderBundle(bundlePath);
        } else {
            initJarBundle(bundlePath);
        }
        setIpsPackageFragmentRoot(new LibraryIpsPackageFragmentRoot(getIpsProject(), ipsBundle));
    }

    private void initFolderBundle(Path bundlePath) throws IOException {
        IpsFolderBundle ipsFolderBundle = ipsStorageFactory.createFolderBundle(getIpsProject(), bundlePath);
        ipsFolderBundle.initBundle();
        ipsBundle = ipsFolderBundle;
    }

    private void initJarBundle(Path bundlePath) throws IOException {
        JarFileFactory jarFileFactory = new JarFileFactory(bundlePath);
        try {
            IpsJarBundle ipsJarBundle = ipsStorageFactory.createJarBundle(getIpsProject(), jarFileFactory);
            ipsJarBundle.initBundle();
            ipsBundle = ipsJarBundle;
        } finally {
            jarFileFactory.closeJarFile();
        }
    }

    @Override
    public String getIpsPackageFragmentRootName() {
        return PathUtil.lastSegment(ipsBundle.getLocation());
    }

    @Override
    public MessageList validate() {
        MessageList messageList = new MessageList();
        if (ipsBundle == null || !ipsBundle.isValid()) {
            messageList.newError(MSGCODE_MISSING_BUNDLE, Messages.IpsBundleEntry_msg_invalid, this);
        }
        return messageList;
    }

    @Override
    public boolean containsResource(String resourcePath) {
        return ipsBundle.contains(Path.of(resourcePath));
    }

    @Override
    public InputStream getResourceAsStream(String path) {
        return ipsBundle.getResourceAsStream(path);
    }

    @Override
    public boolean exists(QualifiedNameType qnt) {
        return ipsBundle.contains(qnt.toPath());
    }

    @Override
    public String getType() {
        return TYPE_BUNDLE;
    }

    @Override
    protected IIpsSrcFile getIpsSrcFile(QualifiedNameType qnt) {
        return findIpsSrcFile(qnt);
    }

    @Override
    public IIpsStorage getIpsStorage() {
        return ipsBundle;
    }

    @Override
    protected String getXmlAttributePathName() {
        return XML_ATTRIBUTE_PATH;
    }

    @Override
    protected String getXmlPathRepresentation() {
        return PathUtil.toPortableString(getIpsStorage().getLocation());
    }

    @Override
    public Path getPath() {
        return getIpsStorage().getLocation();
    }

    @Override
    public String toString() {
        return "BundleEntry[" + getXmlPathRepresentation() + "]"; //$NON-NLS-1$ //$NON-NLS-2$
    }

    protected class IpsStorageFactory {

        public IpsFolderBundle createFolderBundle(IIpsProject ipsProject, Path bundlePath) {
            return new IpsFolderBundle(ipsProject, bundlePath);
        }

        public IpsJarBundle createJarBundle(IIpsProject ipsProject, JarFileFactory jarFileFactory) {
            return new IpsJarBundle(ipsProject, jarFileFactory);
        }

    }

}
