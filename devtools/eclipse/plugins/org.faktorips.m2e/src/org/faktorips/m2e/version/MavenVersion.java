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

    public MavenVersion(String version) {
        this.version = new DefaultArtifactVersion(version);
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
        String versionString = asString();
        if (versionString.equals(version.getQualifier())) {
            if (versionString.startsWith("mvn:")) {
                return versionString;
            }
            return ""; //$NON-NLS-1$
        }
        int firstIndexOfSeperator = versionString.indexOf('-');
        if (firstIndexOfSeperator > -1) {
            return versionString.substring(0, firstIndexOfSeperator);
        } else {
            return versionString;
        }
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
