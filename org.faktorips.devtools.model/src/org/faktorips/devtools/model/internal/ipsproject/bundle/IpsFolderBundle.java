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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.jar.Manifest;

import org.faktorips.devtools.abstraction.util.PathUtil;
import org.faktorips.devtools.model.internal.ipsproject.IpsBundleManifest;
import org.faktorips.devtools.model.ipsproject.IIpsProject;

/**
 * This subclass of {@link AbstractIpsBundle} represents a folder version of an IPS bundle. The
 * class handles every reading access to the folder structure like reading any resources or giving
 * information about included files.
 * 
 * @author dicker
 */
public class IpsFolderBundle extends AbstractIpsBundle {

    private final Path folder;
    private IOFactory ioFactory;

    public IpsFolderBundle(IIpsProject ipsProject, Path folder) {
        super(ipsProject);
        this.folder = folder;
        ioFactory = new IOFactory();
    }

    void setIOFactory(IOFactory ioFactory) {
        this.ioFactory = ioFactory;
    }

    @Override
    public Path getLocation() {
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
        File file = folder.resolve(IpsBundleManifest.MANIFEST_NAME).toFile();
        Manifest manifest;
        try (InputStream is = ioFactory.createInputStream(file)) {
            manifest = new Manifest(is);
            return manifest;
        }
    }

    @Override
    protected InputStream getResourceAsStream(Path path) {

        Path absolutePath = folder.resolve(path);
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
