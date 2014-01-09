/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.ipsproject.bundle;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsBundleManifest;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsLibraryEntry;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsObjectPath;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsObjectPathEntry;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsProjectProperties;
import org.faktorips.devtools.core.internal.model.ipsproject.LibraryIpsPackageFragmentRoot;
import org.faktorips.devtools.core.internal.model.ipsproject.jdtcontainer.IpsContainer4JdtClasspathContainer;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsStorage;
import org.faktorips.util.message.MessageList;

/**
 * An {@link IpsBundleEntry} is ab {@link IpsObjectPathEntry} that references a an IPS bundle in a
 * JAR file or a folder. An IPS bundle needs to have a MANIFEST.MF containing all necessary
 * information about the included IPS objects and the generated source code. The structure of the
 * MANIFEST.MF is explained in detail in {@link IpsBundleManifest}.
 * <p>
 * Normally an {@link IpsBundleEntry} is resolved by using a classpath container like
 * {@link IpsContainer4JdtClasspathContainer}. Mostly for testing purposes this entry has also the
 * ability to be initialized by the {@link IpsObjectPath} in the {@link IpsProjectProperties}. To
 * use this approach you have to setup the following entry:
 * <p>
 * <code>
 * &lt;Entry type="bundle" bundlePath="path/to/grundmodell.jar"/&gt;
 * </code>
 * 
 * @author dirmeier
 */
public class IpsBundleEntry extends IpsLibraryEntry {

    private static final String XML_ATTRIBUTE_PATH = "bundlePath"; //$NON-NLS-1$

    private IIpsStorage ipsBundle;

    private IpsStrorageFactory ipsStrorageFactory;

    /**
     * This constructor creates a new {@link IpsBundleEntry} for the given {@link IpsObjectPath}.
     * After you created the {@link IpsBundleEntry} you need to initialize the bundle by calling
     * {@link #initStorage(IPath)}.
     * 
     * @param ipsObjectPath The parent {@link IpsObjectPath} of this entry
     */
    public IpsBundleEntry(IpsObjectPath ipsObjectPath) {
        super(ipsObjectPath);
        ipsStrorageFactory = new IpsStrorageFactory();
    }

    protected void setIpsStorageFactory(IpsStrorageFactory ipsStrorageFactory) {
        this.ipsStrorageFactory = ipsStrorageFactory;
    }

    @Override
    public void initStorage(IPath bundlePath) throws IOException {
        File bundleFile = bundlePath.toFile();
        if (bundleFile.isDirectory()) {
            initFolderBundle(bundlePath);
        } else {
            initJarBundle(bundlePath);
        }
        setIpsPackageFragmentRoot(new LibraryIpsPackageFragmentRoot(getIpsProject(), ipsBundle));
    }

    private void initFolderBundle(IPath bundlePath) throws IOException {
        IpsFolderBundle ipsFolderBundle;
        ipsFolderBundle = ipsStrorageFactory.createFolderBundle(getIpsProject(), bundlePath);
        ipsFolderBundle.initBundle();
        ipsBundle = ipsFolderBundle;
    }

    private void initJarBundle(IPath bundlePath) throws IOException {
        JarFileFactory jarFileFactory = new JarFileFactory(bundlePath);
        IpsJarBundle ipsJarBundle = ipsStrorageFactory.createJarBundle(getIpsProject(), jarFileFactory);
        ipsJarBundle.initBundle();
        ipsBundle = ipsJarBundle;
    }

    @Override
    public String getIpsPackageFragmentRootName() {
        return ipsBundle.getLocation().lastSegment();
    }

    @Override
    public MessageList validate() {
        MessageList messageList = new MessageList();
        if (ipsBundle == null || !ipsBundle.isValid()) {
            messageList.newError(MSGCODE_MISSING_BUNDLE, Messages.IpsBundleEntry_msg_invalid, this, null);
        }
        return messageList;
    }

    @Override
    public boolean containsResource(String path) {
        return ipsBundle.contains(new Path(path));
    }

    @Override
    public InputStream getResourceAsStream(String path) {
        return ipsBundle.getResourceAsStream(path);
    }

    @Override
    public boolean exists(QualifiedNameType qnt) throws CoreException {
        return ipsBundle.contains(qnt.toPath());
    }

    @Override
    public String getType() {
        return TYPE_BUNDLE;
    }

    @Override
    protected IIpsSrcFile getIpsSrcFile(QualifiedNameType qnt) throws CoreException {
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
        return getIpsStorage().getLocation().toPortableString();
    }

    protected class IpsStrorageFactory {

        public IpsFolderBundle createFolderBundle(IIpsProject ipsProject, IPath bundlePath) {
            return new IpsFolderBundle(ipsProject, bundlePath);
        }

        public IpsJarBundle createJarBundle(IIpsProject ipsProject, JarFileFactory jarFileFactory) {
            return new IpsJarBundle(ipsProject, jarFileFactory);
        }

    }

}
