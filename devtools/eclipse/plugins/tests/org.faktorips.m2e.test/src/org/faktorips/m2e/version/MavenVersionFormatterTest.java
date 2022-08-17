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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;

public class MavenVersionFormatterTest {

    @Test
    public void testFormatVersion() {
        String version = "1.0.0";
        assertThat(MavenVersionFormatter.formatVersion(version), is("1.0.0"));
    }

    @Test
    public void testFormatVersionWithDefaultQualifier() {
        String versionWithQualifier = "1.0.0.qualifier";
        assertThat(MavenVersionFormatter.formatVersion(versionWithQualifier), is("1.0.0-SNAPSHOT"));
    }

    @Test
    public void testFormatVersionWithRuntimeQualifier() {
        String versionWithQualifier = "1.0.0.randomQualifier";
        assertThat(MavenVersionFormatter.formatVersion(versionWithQualifier), is("1.0.0"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatVersionInvalidFormat() {
        String invalidVersion = "a1.0.0.randomQualifier";
        MavenVersionFormatter.formatVersion(invalidVersion);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatVersionNoVersion() {
        String randomString = "randomString";
        MavenVersionFormatter.formatVersion(randomString);
    }
}
