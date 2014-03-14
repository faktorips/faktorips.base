/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.model.versionmanager;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.osgi.service.resolver.VersionRange;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.internal.model.versionmanager.util.RequireBundleChanger;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.util.ArgumentCheck;

/**
 * Utility-class for the migration of the manifest-file.
 * 
 * It change the {@link VersionRange} of the Plugin or creates a new entry with {@link VersionRange}
 * 
 * 
 * @author frank
 */
public class MigrationManifestUtil {

    private final Manifest manifest;
    private final IFile file;

    MigrationManifestUtil(IFile file, ManifestFactory manifestFactory) throws IOException {
        ArgumentCheck.notNull(file);
        ArgumentCheck.notNull(manifestFactory);
        this.file = file;
        this.manifest = manifestFactory.loadManifest(file);
    }

    /**
     * Create a new instance
     * 
     * @param ipsProject the ipsProject
     * @return MigrationUtil
     * @throws IOException if the manifest-file not exists
     */
    public static MigrationManifestUtil createMigrationManifestUtil(IIpsProject ipsProject) throws IOException {
        ArgumentCheck.notNull(ipsProject);
        ArgumentCheck.notNull(ipsProject.getProject());
        IFile file = ipsProject.getProject().getFile(JarFile.MANIFEST_NAME);
        return new MigrationManifestUtil(file, new ManifestFactory());
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
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            manifest.write(outputStream);
            ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
            file.setContents(inputStream, true, true, new NullProgressMonitor());
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    public Manifest getManifest() {
        return manifest;
    }

    public static class ManifestFactory {

        public Manifest loadManifest(IFile file) throws IOException {
            try {
                return new Manifest(file.getContents());
            } catch (CoreException e) {
                throw new CoreRuntimeException(e);
            }
        }

    }
}
