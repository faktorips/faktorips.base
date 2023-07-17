/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.migrationextensions;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;
import java.util.jar.Manifest;

import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.abstraction.AFile;
import org.faktorips.devtools.abstraction.AVersion;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.migration.DefaultMigration;
import org.faktorips.devtools.model.internal.IpsModel;
import org.faktorips.devtools.model.internal.ipsproject.IpsBundleManifest;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.plugin.IpsStatus;
import org.faktorips.runtime.MessageList;

/**
 * Marks classes generated for a select set of types as dirty to trigger the clean build.
 */
public class MarkAsDirtyMigration extends DefaultMigration {

    private final Set<IpsObjectType> typesToMigrate;
    private String targetVersion;
    private String description;

    MarkAsDirtyMigration(IIpsProject projectToMigrate, String featureId, Set<IpsObjectType> typesToMigrate,
            String targetVersion, String description) {
        super(projectToMigrate, featureId);
        this.typesToMigrate = typesToMigrate;
        this.targetVersion = targetVersion;
        this.description = description;
    }

    @Override
    public String getTargetVersion() {
        AVersion migrationTarget = AVersion.parse(targetVersion);
        AVersion installedVersion = AVersion.parse(getFaktorIpsVersion());
        if (isSameVersionBeforRelease(migrationTarget, installedVersion)) {
            return installedVersion.toString();
        }
        return migrationTarget.toString();
    }

    /* private */ String getFaktorIpsVersion() {
        return IpsPlugin.getInstalledFaktorIpsVersion();
    }

    private boolean isSameVersionBeforRelease(AVersion migrationTarget, AVersion installedVersion) {
        return migrationTarget.majorMinorPatch().equals(installedVersion.majorMinorPatch())
                && !installedVersion.isRelease();
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public MessageList migrate(IProgressMonitor monitor) throws IpsException, InvocationTargetException {
        MessageList messageList = super.migrate(monitor);
        MigrationUtil.updateBuilderSetDefaults(getIpsProject());
        return messageList;
    }

    @Override
    protected void migrate(IIpsSrcFile srcFile) {
        if (typesToMigrate.contains(srcFile.getIpsObjectType())) {
            srcFile.markAsDirty();
        }
    }

    /**
     * Updates the META-INF/MANIFEST.MF with the Faktor-IPS generator settings
     */
    protected void updateManifest() {
        @SuppressWarnings("deprecation")
        IpsModel ipsModel = IpsModel.get();
        ipsModel.clearProjectSpecificCaches(getIpsProject());
        IIpsProject ipsProject = getIpsProject();
        AFile manifestFile = ipsProject.getProject().getFile(IpsBundleManifest.MANIFEST_NAME);
        if (!manifestFile.exists()) {
            manifestFile = ipsProject.getProject().getFile("src/main/resources/" + IpsBundleManifest.MANIFEST_NAME); //$NON-NLS-1$
        }
        if (manifestFile.exists()) {
            try {
                Manifest manifest = new Manifest(manifestFile.getContents());
                IpsBundleManifest ipsBundleManifest = new IpsBundleManifest(manifest);
                ipsBundleManifest.writeBuilderSettings(ipsProject, manifestFile);
            } catch (IOException e) {
                throw new IpsException(new IpsStatus("Can't read " + manifestFile, e)); //$NON-NLS-1$
            }

        }
    }
}
