/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.m2e.version;

import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.faktorips.devtools.model.IVersion;
import org.faktorips.runtime.internal.IpsStringUtils;

/**
 * A version as defined in <a href=
 * "http://www.mojohaus.org/versions-maven-plugin/version-rules.html">http://www.mojohaus.org/versions-maven-plugin/version-rules.html</a>
 */
public class MavenVersion implements IVersion<MavenVersion> {

    private final DefaultArtifactVersion version;
    private final String fakeVersion;

    public MavenVersion(String version) {
        fakeVersion = version;
        if (fakeVersion.startsWith("mvn:")) {
            this.version = new DefaultArtifactVersion(version.split(":")[2]);
        } else {
            this.version = new DefaultArtifactVersion(version);
        }
    }

    @Override
    public int compareTo(MavenVersion o) {
        return version.compareTo(o.version);
    }

    @Override
    public String asString() {
        return version.toString();
    }

    @Override
    public String getUnqualifiedVersion() {
        if (fakeVersion.startsWith("mvn:")) {
            return fakeVersion;
        }
        String versionString = asString();
        String qualifier = version.getQualifier();
        if (versionString.equals(qualifier)) {
            return ""; //$NON-NLS-1$
        }
        if (IpsStringUtils.isNotBlank(qualifier)) {
            DefaultArtifactVersion qualifierVersion = new DefaultArtifactVersion(qualifier);
            String qualifierQualifier = qualifierVersion.getQualifier();
            if (!qualifierVersion.toString().equals(qualifierQualifier)) {
                // FIPS-10931: Weird version format
                return versionString.replace('-' + qualifier, "") + '-'
                        + qualifier.replace('-' + qualifierQualifier, "");
            }
        }
        return versionString.replace('-' + qualifier, "");
    }

    @Override
    public boolean isEmptyVersion() {
        // CSOFF: BooleanExpressionComplexityCheck
        return version.getMajorVersion() == 0 && version.getMinorVersion() == 0 && version.getIncrementalVersion() == 0
                && version.getBuildNumber() == 0 && IpsStringUtils.isBlank(version.getQualifier());
        // CSON: BooleanExpressionComplexityCheck
    }

    @Override
    public boolean isNotEmptyVersion() {
        return !isEmptyVersion();
    }

    @Override
    public int hashCode() {
        return version.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }
        MavenVersion other = (MavenVersion)obj;
        return version.equals(other.version);
    }

    static boolean isCorrectVersionFormat(String versionString) {
        // Maven considers any String a Qualifier as a fallback, so if the whole versionString ends
        // up as the qualifier, the format was wrong.
        return versionString != null && !versionString.equals(new DefaultArtifactVersion(versionString).getQualifier());
    }

    /**
     * @see <a href=
     *          "http://www.mojohaus.org/versions-maven-plugin/version-rules.html">http://www.mojohaus.org/versions-maven-plugin/version-rules.html</a>
     */
    static String getVersionFormat() {
        return "<MajorVersion [> . <MinorVersion [> . <IncrementalVersion ] ] [> - <BuildNumber | Qualifier ]> "; //$NON-NLS-1$
    }

}
