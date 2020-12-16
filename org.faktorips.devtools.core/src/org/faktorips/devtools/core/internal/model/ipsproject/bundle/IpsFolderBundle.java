/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.ipsproject.bundle;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.Manifest;

import org.eclipse.core.runtime.IPath;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsBundleManifest;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;

/**
 * This subclass of {@link AbstractIpsBundle} represents a folder version of an IPS bundle. The
 * class handles every reading access to the folder structure like reading any resources or giving
 * information about included files.
 * 
 * @author dicker
 */
public class IpsFolderBundle extends AbstractIpsBundle {

    private final IPath folder;
    private IOFactory ioFactory;

    public IpsFolderBundle(IIpsProject ipsProject, IPath folder) {
        super(ipsProject);
        this.folder = folder;
        ioFactory = new IOFactory();
    }

    void setIOFactory(IOFactory ioFactory) {
        this.ioFactory = ioFactory;
    }

    @Override
    public IPath getLocation() {
        return folder;
    }

    @Override
    public void initBundle() throws IOException {
        Manifest manifest = getManifest();
        if (manifest != null) {
            setBundleManifest(new IpsBundleManifest(manifest));
            setBundleContentIndex(new IpsFolderBundleContentIndex(folder, getBundleManifest().getObjectDirs()));
        }
    }

    public Manifest getManifest() throws IOException {
        File file = folder.append(IpsBundleManifest.MANIFEST_NAME).toFile();
        Manifest manifest;
        InputStream is = null;
        try {
            is = ioFactory.createInputStream(file);
            manifest = new Manifest(is);
            return manifest;
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    @Override
    protected InputStream getResourceAsStream(IPath path) {

        IPath absolutePath = folder.append(path);
        File file = absolutePath.toFile();
        try {
            return ioFactory.createInputStream(file);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isFolder() {
        return true;
    }

    protected static class IOFactory {

        public FileInputStream createInputStream(File file) throws FileNotFoundException {
            return new FileInputStream(file);
        }

    }
}
