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

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class M2eIgnorePluginTest {

    private M2eIgnorePlugin validPlugin() {
        M2eIgnorePlugin p = new M2eIgnorePlugin();
        p.setGroupId("com.example");
        p.setArtifactId("my-plugin");
        p.setVersionRange("[1,)");
        p.setGoals("generate");
        return p;
    }

    @Test
    public void validate_ok() {
        assertDoesNotThrow(() -> validPlugin().validate());
    }

    @Test
    public void validate_missingGroupId_throwsWithMessage() {
        M2eIgnorePlugin p = validPlugin();
        p.setGroupId(null);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, p::validate);
        assertThat(ex.getMessage(), containsString("groupId"));
    }

    @Test
    public void validate_blankGroupId_throwsWithMessage() {
        M2eIgnorePlugin p = validPlugin();
        p.setGroupId("   ");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, p::validate);
        assertThat(ex.getMessage(), containsString("groupId"));
    }

    @Test
    public void validate_missingArtifactId_throwsWithMessage() {
        M2eIgnorePlugin p = validPlugin();
        p.setArtifactId(null);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, p::validate);
        assertThat(ex.getMessage(), containsString("artifactId"));
    }

    @Test
    public void validate_missingGoals_throwsWithMessage() {
        M2eIgnorePlugin p = validPlugin();
        p.setGoals(null);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, p::validate);
        assertThat(ex.getMessage(), containsString("goals"));
    }

    @Test
    public void validate_blankGoals_throwsWithMessage() {
        M2eIgnorePlugin p = validPlugin();
        p.setGoals("");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, p::validate);
        assertThat(ex.getMessage(), containsString("goals"));
    }

    @Test
    public void validate_missingVersionRange_throwsWithMessage() {
        M2eIgnorePlugin p = validPlugin();
        p.setVersionRange(null);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, p::validate);
        assertThat(ex.getMessage(), containsString("versionRange"));
    }

    @Test
    public void validate_versionRangeWithXmlSpecialChars_throwsWithMessage() {
        M2eIgnorePlugin p = validPlugin();
        p.setVersionRange("[1,2)<script>");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, p::validate);
        assertThat(ex.getMessage(), containsString("versionRange"));
    }

    @Test
    public void validate_groupIdWithXmlSpecialChars_throwsWithMessage() {
        M2eIgnorePlugin p = validPlugin();
        p.setGroupId("com.example<inject>");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, p::validate);
        assertThat(ex.getMessage(), containsString("groupId"));
    }

    @Test
    public void validate_artifactIdWithXmlSpecialChars_throwsWithMessage() {
        M2eIgnorePlugin p = validPlugin();
        p.setArtifactId("my-plugin&amp;");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, p::validate);
        assertThat(ex.getMessage(), containsString("artifactId"));
    }

    @Test
    public void validate_groupIdWithQuote_throwsWithMessage() {
        M2eIgnorePlugin p = validPlugin();
        p.setGroupId("com.example\"inject");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, p::validate);
        assertThat(ex.getMessage(), containsString("groupId"));
    }

    @Test
    public void validate_validMavenCoordinates_ok() {
        String[] validCoords = {
                "com.github.spotbugs",
                "org.faktorips",
                "my-plugin",
                "my_plugin",
                "plugin.with.dots",
                "plugin-1.2.3",
        };
        for (String coord : validCoords) {
            M2eIgnorePlugin p = validPlugin();
            p.setGroupId(coord);
            p.setArtifactId(coord);
            assertDoesNotThrow(p::validate, "Coordinate should be valid: " + coord);
        }
    }

    @Test
    public void validate_allMavenVersionRangeSyntaxForms_ok() {
        // Covers all documented Maven version range forms:
        // https://maven.apache.org/enforcer/enforcer-rules/versionRanges.html
        String[] valid = {
                // soft requirement (minimum version recommendation)
                "1.0",
                // x <= 1.0
                "(,1.0]",
                // x < 1.0
                "(,1.0)",
                // x == 1.0
                "[1.0]",
                // x >= 1.0
                "[1.0,)",
                // x >= 0 (open lower bound)
                "[0,)",
                // x > 1.0
                "(1.0,)",
                // 1.0 < x < 2.0
                "(1.0,2.0)",
                // 1.0 <= x <= 2.0
                "[1.0,2.0]",
                // x <= 1.0 or x >= 1.2  (multi-range, comma-separated sets)
                "(,1.0],[1.2,)",
                // x != 1.1
                "(,1.1),(1.1,)",
                // three-part version numbers
                "[1.2.3,1.3.0)",
                // major-only ranges used in Faktor-IPS plugins
                "[4,)",
        };
        for (String range : valid) {
            M2eIgnorePlugin p = validPlugin();
            p.setVersionRange(range);
            assertDoesNotThrow(p::validate, "Range should be valid: " + range);
        }
    }

    @Test
    public void validate_versionRangeWithXmlSpecialChars_allRejected() {
        String[] invalid = {
                "[1,)<script>",
                "[1,)&amp;",
                "[1,)>",
                "<groupId>inject</groupId>",
                "[1,2) & [3,4)",
        };
        for (String range : invalid) {
            M2eIgnorePlugin p = validPlugin();
            p.setVersionRange(range);
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, p::validate,
                    "Range should be rejected: " + range);
            assertThat(ex.getMessage(), containsString("versionRange"));
        }
    }
}
