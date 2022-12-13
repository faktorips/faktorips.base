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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Formats versions in order to be compatible with the version format required by Maven.
 * 
 * @author Florian Orendi
 */
public class MavenVersionFormatter {

    private MavenVersionFormatter() {
        // Utility class not to be instantiated.
    }

    /**
     * Formats a version to be compatible with the Maven format.
     * 
     * @param version The version to be formatted
     * @return The formatted version in the Maven format
     * @throws IllegalArgumentException If the passed version is invalid
     */
    public static String formatVersion(String version) throws IllegalArgumentException {
        Pattern pattern = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+)(\\.(rc\\d\\d|m\\d\\d|a\\d{8}-\\d\\d|release))?");
        Matcher versionMatcher = pattern.matcher(version);
        String mavenVersion = "";
        if (versionMatcher.find() && versionMatcher.start() == 0) {
            mavenVersion = versionMatcher.group();
        }
        if (mavenVersion.isEmpty()) {
            throw new IllegalArgumentException(
                    String.format("The passed version \"%s\" is invalid. Use the format X.X.X[.qualifier].", version));
        }
        if (version.endsWith(".qualifier")) {
            mavenVersion += "-SNAPSHOT";
        }
        return mavenVersion;
    }
}
