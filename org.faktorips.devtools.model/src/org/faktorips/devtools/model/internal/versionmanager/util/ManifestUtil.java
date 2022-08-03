/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.versionmanager.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.osgi.service.resolver.VersionRange;
import org.faktorips.devtools.abstraction.AFile;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.util.ArgumentCheck;

/**
 * Utility-class for the migration of the manifest-file.
 * 
 * It change the {@link VersionRange} of the Plugin or creates a new entry with {@link VersionRange}
 * 
 * 
 * @author frank
 */
public class ManifestUtil {

    private final Manifest manifest;
    private final AFile file;

    ManifestUtil(AFile file, ManifestFactory manifestFactory) throws IOException {
        ArgumentCheck.notNull(file);
        ArgumentCheck.notNull(manifestFactory);
        this.file = file;
        manifest = manifestFactory.loadManifest(file);
    }

    /**
     * Create a new instance
     * 
     * @param ipsProject the ipsProject
     * @return MigrationUtil
     * @throws IOException if the manifest-file not exists
     */
    public static ManifestUtil createMigrationManifestUtil(IIpsProject ipsProject) throws IOException {
        ArgumentCheck.notNull(ipsProject);
        ArgumentCheck.notNull(ipsProject.getProject());
        AFile file = ipsProject.getProject().getFile(JarFile.MANIFEST_NAME);
        return new ManifestUtil(file, new ManifestFactory());
    }

    /**
     * Set the min and max Version for the plugin in the manifest-file
     * 
     * @param plugin Plugin-Name
     */
    public void setPluginDependency(String plugin, VersionRange versionRange) {
        ArgumentCheck.notNull(plugin);
        ArgumentCheck.notNull(versionRange);
        RequireBundleChanger bundleChanger = new RequireBundleChanger(manifest.getMainAttributes());
        bundleChanger.changePluginDependency(plugin, versionRange);
    }

    /**
     * Writes back the manifest to file
     * 
     * @throws IOException if errors occur while writing
     */
    public void writeManifest() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        manifest.write(outputStream);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
        file.setContents(inputStream, true, new NullProgressMonitor());
    }

    public Manifest getManifest() {
        return manifest;
    }

    public static class ManifestFactory {

        public Manifest loadManifest(AFile file) throws IOException {
            return new Manifest(file.getContents());
        }

    }
}
