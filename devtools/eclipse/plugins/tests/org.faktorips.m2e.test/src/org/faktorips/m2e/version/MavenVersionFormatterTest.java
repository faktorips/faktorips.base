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
    public void testFormatVersionWithReleaseQualifier() {
        String versionWithReleaseQualifier = "1.0.0.release";
        assertThat(MavenVersionFormatter.formatVersion(versionWithReleaseQualifier), is("1.0.0.release"));
    }

    @Test
    public void testFormatVersionWithRCQualifier() {
        String versionWithRCQualifier = "20.6.0.rc01";
        assertThat(MavenVersionFormatter.formatVersion(versionWithRCQualifier),
                is("20.6.0.rc01"));
    }

    @Test
    public void testFormatVersionWithMilestoneQualifier() {
        String versionWithMilestoneQualifier = "20.6.0.m01";
        assertThat(MavenVersionFormatter.formatVersion(versionWithMilestoneQualifier),
                is("20.6.0.m01"));
    }

    @Test
    public void testFormatVersionWithAlphaQualifier() {
        String versionWithAlphaQualifier = "20.6.0.a20200513-01";
        assertThat(MavenVersionFormatter.formatVersion(versionWithAlphaQualifier),
                is("20.6.0.a20200513-01"));
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
