/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.maven.plugin.validation.mavenversion;

import org.apache.maven.project.MavenProject;
import org.faktorips.devtools.model.IVersion;
import org.faktorips.devtools.model.IVersionProvider;

/**
 * A {@link IVersionProvider} reading the version from a {@link MavenProject}.
 */
public class MavenVersionProvider implements IVersionProvider<MavenVersion> {

    private MavenProject mavenProject;

    public MavenVersionProvider(MavenProject mavenProject) {
        this.mavenProject = mavenProject;
    }

    @Override
    public boolean isCorrectVersionFormat(String version) {
        return MavenVersion.isCorrectVersionFormat(version);
    }

    @Override
    public String getVersionFormat() {
        return MavenVersion.getVersionFormat();
    }

    @Override
    public MavenVersion getVersion(String versionAsString) {
        return new MavenVersion(versionAsString);
    }

    @Override
    public MavenVersion getProjectVersion() {
        if (mavenProject != null) {
            return getVersion(mavenProject.getVersion());
        } else {
            return new MavenVersion("0.0"); //$NON-NLS-1$
        }
    }

    @Override
    public void setProjectVersion(IVersion<MavenVersion> version) {
        if (mavenProject != null) {
            mavenProject.setVersion(version.asString());
        }
    }

}
