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
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.abstraction.util.PathUtil;
import org.faktorips.devtools.model.internal.ipsproject.IpsBundleManifest;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.util.StreamUtil;

/**
 * This subclass of {@link AbstractIpsBundle} represents a packed version of an IPS bundle. The
 * class handles every reading access to the {@link JarFile} like reading any resources or giving
 * information about included files.
 * 
 * @author dirmeier
 */
public class IpsJarBundle extends AbstractIpsBundle {

    private final JarFileFactory jarFileFactory;

    /**
     * Constructs a {@link IpsJarBundle} for the specified {@link IIpsProject}. The
     * {@link JarFileFactory} is used to create a {@link JarFile} when ever needed. After you have
     * constructed the {@link IpsJarBundle} you need to call {@link #initBundle()}. This method will
     * read the manifest and initializes the content.
     * 
     * @see #initBundle()
     */
    public IpsJarBundle(IIpsProject ipsProject, JarFileFactory jarFileFactory) {
        super(ipsProject);
        this.jarFileFactory = jarFileFactory;
    }

    JarFile getJarFile() throws IOException {
        return jarFileFactory.createJarFile();
    }

    JarFile getJarFileThrowingRuntimeException() {
        try {
            return getJarFile();
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }

    /**
     * Initializes the jar file manifest and index the content to get fast access to the jar file.
     * This method should be called directly after constructing the object and before calling any
     * other method. It is separated from constructor because it throws an {@link IOException}.
     * 
     * @throws IOException in case of reading errors while accessing the jar file
     */
    @Override
    public void initBundle() throws IOException {
        JarFile jarFile = getJarFile();
        Manifest manifest = jarFile.getManifest();
        if (manifest != null) {
            setBundleManifest(new IpsBundleManifest(manifest));
            setBundleContentIndex(new IpsJarBundleContentIndex(jarFile, getBundleManifest().getObjectDirs()));
        }
    }

    void throwExceptionWhenNotFound(ZipEntry zipEntry, Path path) {
        if (zipEntry == null) {
            throw new IpsException("There is no entry " + path + " in " + getLocation()); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    InputStream getInputStream(JarFile jarFile, ZipEntry zipEntry) {
        InputStream inputStream = null;
        try {
            inputStream = jarFile.getInputStream(zipEntry);
            return StreamUtil.copy(inputStream);
        } catch (IOException e) {
            throw newRuntimeException("Error while reading jar file " + getLocation(), e); //$NON-NLS-1$
        }
    }

    private RuntimeException newRuntimeException(String message, IOException e) {
        return new RuntimeException(message, e);
    }

    void closeJarFile(JarFile jarFile) {
        try {
            jarFile.close();
        } catch (IOException e) {
            throw new RuntimeException("Error while closeing jar file " + getLocation(), e); //$NON-NLS-1$
        }
    }

    @Override
    public Path getLocation() {
        return jarFileFactory.getJarPath();
    }

    @Override
    protected InputStream getResourceAsStream(Path path) {
        JarFile jarFile = getJarFileThrowingRuntimeException();
        try {
            ZipEntry zipEntry = jarFile.getEntry(PathUtil.toPortableString(path));
            throwExceptionWhenNotFound(zipEntry, path);
            return getInputStream(jarFile, zipEntry);
        } finally {
            closeJarFile(jarFile);
        }
    }

    @Override
    public boolean isFolder() {
        return false;
    }

    @Override
    public String toString() {
        return getLocation().toString();
    }
}
