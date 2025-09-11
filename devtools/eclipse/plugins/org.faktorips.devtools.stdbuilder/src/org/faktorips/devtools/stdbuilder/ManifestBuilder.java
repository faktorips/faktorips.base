/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder;

import java.io.IOException;
import java.util.jar.Manifest;

import org.faktorips.devtools.abstraction.ABuildKind;
import org.faktorips.devtools.abstraction.AFile;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.builder.AbstractArtefactBuilder;
import org.faktorips.devtools.model.internal.ipsproject.IpsBundleManifest;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.plugin.IpsStatus;

/**
 * Builder that writes the MANIFEST.MF file and all Faktor-IPS settings on every full build.
 */
public class ManifestBuilder extends AbstractArtefactBuilder {

    public ManifestBuilder(IIpsArtefactBuilderSet builderSet) {
        super(builderSet);
    }

    @Override
    public String getName() {
        return "ManifestBuilder"; //$NON-NLS-1$
    }

    @Override
    public boolean isBuilderFor(IIpsSrcFile ipsSrcFile) throws IpsException {
        return false;
    }

    @Override
    public boolean isBuildingInternalArtifacts() {
        return false;
    }

    @Override
    public void delete(IIpsSrcFile ipsSrcFile) throws IpsException {
        // Does not delete artifacts for specific source file
    }

    @Override
    public void build(IIpsSrcFile ipsSrcFile) throws IpsException {
        // Does not build artifacts for specific source file
    }

    /**
     * Writes the MANIFEST.MF file with all Faktor-IPS settings on every full build.
     */
    @Override
    public void afterBuildProcess(IIpsProject project, ABuildKind buildKind) {
        if (buildKind == ABuildKind.FULL) {
            updateManifest(project);
        }
    }

    private void updateManifest(IIpsProject ipsProject) {
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
