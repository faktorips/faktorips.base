/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model;

import java.io.IOException;
import java.util.jar.Attributes;

import org.faktorips.devtools.core.model.IVersion;
import org.faktorips.devtools.core.model.IVersionProvider;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IVersionFormat;
import org.faktorips.devtools.core.model.versionmanager.ManifestUtil;

/**
 * The {@link BundleVersionProvider} is the used if the version of the model shall be configured in
 * the Manifest file.
 * 
 */
public class BundleVersionProvider implements IVersionProvider<OsgiVersion> {

    private IIpsProject ipsProject;

    private ManifestUtil migrationManifest;

    private final IVersionFormat versionFormat;

    public BundleVersionProvider(IIpsProject ipsProject) throws IOException {
        this.ipsProject = ipsProject;
        versionFormat = new OsgiVersionFormat();
        initMigrationManifest();
    }

    private void initMigrationManifest() throws IOException {
        migrationManifest = ManifestUtil.createMigrationManifestUtil(ipsProject);
    }

    @Override
    public boolean isCorrectVersionFormat(String version) {
        return versionFormat.isCorrectVersionFormat(version);
    }

    @Override
    public String getVersionFormat() {
        return versionFormat.getVersionFormat();
    }

    @Override
    public IVersion<OsgiVersion> getVersion(String versionAsString) {
        return new OsgiVersion(versionAsString);
    }

    @Override
    public IVersion<OsgiVersion> getProjectVersion() {
        Attributes attributes = getManifestMainAttributes();
        String value = attributes.getValue(org.osgi.framework.Constants.BUNDLE_VERSION);
        if (value != null) {
            return getVersion(value);
        }
        return null;
    }

    @Override
    public void setProjectVersion(IVersion<OsgiVersion> version) {
        try {
            Attributes attributes = getManifestMainAttributes();
            attributes.putValue(org.osgi.framework.Constants.BUNDLE_VERSION, version.asString());
            migrationManifest.writeManifest();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected Attributes getManifestMainAttributes() {
        return migrationManifest.getManifest().getMainAttributes();
    }
}
