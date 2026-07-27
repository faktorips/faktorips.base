/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.maven.plugin.mojo.internal;

import java.util.regex.Pattern;

/**
 * Configures a Maven plugin execution that m2e should ignore during Eclipse project
 * import/refresh. Corresponds to one {@code <pluginExecution>} entry in m2e's
 * {@code lifecycle-mapping-metadata.xml}.
 */
public class M2eIgnorePlugin {

    /**
     * Whitelist pattern for Maven coordinate identifiers (groupId/artifactId): alphanumerics,
     * hyphens, underscores, dots. Excludes all XML special characters.
     */
    private static final Pattern MAVEN_COORDINATE = Pattern.compile("^[a-zA-Z0-9_\\-.]+$");

    /**
     * Whitelist pattern for Maven version range syntax: digits, letters, dots, brackets, parens,
     * comma, hyphen, underscore, plus. Excludes XML special characters.
     */
    private static final Pattern MAVEN_VERSION_RANGE = Pattern.compile("^[\\[\\]()\\w.,+\\-]+$");

    private String groupId;
    private String artifactId;
    private String versionRange = "[0,)";
    private String goals;

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public String getVersionRange() {
        return versionRange;
    }

    public void setVersionRange(String versionRange) {
        this.versionRange = versionRange;
    }

    /**
     * Comma-separated list of goals to ignore, e.g. {@code spotbugs,check}.
     */
    public String getGoals() {
        return goals;
    }

    public void setGoals(String goals) {
        this.goals = goals;
    }

    /**
     * Validates that all required fields are set and contain only safe values.
     *
     * @throws IllegalArgumentException if any field is missing or invalid
     */
    // CSOFF: CyclomaticComplexity
    public void validate() {
        if (groupId == null || groupId.isBlank()) {
            throw new IllegalArgumentException(
                    "m2eIgnorePlugin: <groupId> must not be empty");
        }
        if (!MAVEN_COORDINATE.matcher(groupId).matches()) {
            throw new IllegalArgumentException(
                    "m2eIgnorePlugin: <groupId> '" + groupId + "' contains invalid characters");
        }
        if (artifactId == null || artifactId.isBlank()) {
            throw new IllegalArgumentException(
                    "m2eIgnorePlugin: <artifactId> must not be empty");
        }
        if (!MAVEN_COORDINATE.matcher(artifactId).matches()) {
            throw new IllegalArgumentException(
                    "m2eIgnorePlugin: <artifactId> '" + artifactId + "' contains invalid characters");
        }
        if (goals == null || goals.isBlank()) {
            throw new IllegalArgumentException(
                    "m2eIgnorePlugin: <goals> must not be empty (e.g. 'spotbugs,check')");
        }
        if (versionRange == null || versionRange.isBlank()) {
            throw new IllegalArgumentException(
                    "m2eIgnorePlugin: <versionRange> must not be empty (e.g. '[0,)')");
        }
        if (!MAVEN_VERSION_RANGE.matcher(versionRange).matches()) {
            throw new IllegalArgumentException(
                    "m2eIgnorePlugin: <versionRange> '" + versionRange + "' is not a valid Maven version range");
        }
    }
    // CSON: CyclomaticComplexity
}
