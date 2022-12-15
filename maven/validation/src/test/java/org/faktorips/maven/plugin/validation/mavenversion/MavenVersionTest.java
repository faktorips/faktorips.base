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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;

import org.junit.jupiter.api.Test;

class MavenVersionTest {

    @Test
    void testIsEmptyVersion() {
        assertThat(new MavenVersion("").isEmptyVersion(), is(true));
        assertThat(new MavenVersion("0.0.0-0").isEmptyVersion(), is(true));
        assertThat(new MavenVersion("0.0.0-1").isEmptyVersion(), is(false));
        assertThat(new MavenVersion("0.0.1-0").isEmptyVersion(), is(false));
        assertThat(new MavenVersion("0.1.0-0").isEmptyVersion(), is(false));
        assertThat(new MavenVersion("1.0.0-0").isEmptyVersion(), is(false));
        assertThat(new MavenVersion("1.0").isEmptyVersion(), is(false));
        assertThat(new MavenVersion("FooBar").isEmptyVersion(), is(false));
    }

    @Test
    void testIsNotEmptyVersion() {
        assertThat(new MavenVersion("").isNotEmptyVersion(), is(false));
        assertThat(new MavenVersion("0.0.0-0").isNotEmptyVersion(), is(false));
        assertThat(new MavenVersion("0.0.0-1").isNotEmptyVersion(), is(true));
        assertThat(new MavenVersion("0.0.1-0").isNotEmptyVersion(), is(true));
        assertThat(new MavenVersion("0.1.0-0").isNotEmptyVersion(), is(true));
        assertThat(new MavenVersion("1.0.0-0").isNotEmptyVersion(), is(true));
        assertThat(new MavenVersion("1.0").isNotEmptyVersion(), is(true));
        assertThat(new MavenVersion("FooBar").isNotEmptyVersion(), is(true));
    }

    @Test
    void testIsCorrectVersionFormat() {
        assertThat(MavenVersion.isCorrectVersionFormat("Wrdlbrmpft"), is(false));
        assertThat(MavenVersion.isCorrectVersionFormat(""), is(false));
        assertThat(MavenVersion.isCorrectVersionFormat("1"), is(true));
        assertThat(MavenVersion.isCorrectVersionFormat("1.0"), is(true));
        assertThat(MavenVersion.isCorrectVersionFormat("1.0.5"), is(true));
        assertThat(MavenVersion.isCorrectVersionFormat("1.0-SNAPSHOT"), is(true));
        assertThat(MavenVersion.isCorrectVersionFormat("20.6.0.a20200513-01"), is(true));
        assertThat(MavenVersion.isCorrectVersionFormat("20.6.0.m01"), is(true));
        assertThat(MavenVersion.isCorrectVersionFormat("20.6.0.rc01"), is(true));
        assertThat(MavenVersion.isCorrectVersionFormat("20.6.0.release"), is(true));
    }

    @Test
    void testGetUnqualifiedVersion() throws Exception {
        assertThat(new MavenVersion("").getUnqualifiedVersion(), is(""));
        assertThat(new MavenVersion("0.0.0").getUnqualifiedVersion(), is("0.0.0"));
        assertThat(new MavenVersion("1.5.").getUnqualifiedVersion(), is(""));
        assertThat(new MavenVersion("blablablub").getUnqualifiedVersion(), is(""));
        assertThat(new MavenVersion("1.5.7.9").getUnqualifiedVersion(), is(""));
        assertThat(new MavenVersion("47.11-Abc").getUnqualifiedVersion(), is("47.11"));
        assertThat(new MavenVersion("42.23#432").getUnqualifiedVersion(), is(""));
        assertThat(new MavenVersion("11.8.05-SNAPSHOT").getUnqualifiedVersion(), is(""));
        assertThat(new MavenVersion("1.2.3-SNAPSHOT").getUnqualifiedVersion(), is("1.2.3"));
        assertThat(new MavenVersion("1.5.7-9").getUnqualifiedVersion(), is("1.5.7"));
        assertThat(new MavenVersion("1.5.7-9-BLUB").getUnqualifiedVersion(), is("1.5.7"));
        assertThat(new MavenVersion("20.6.0.m01").getUnqualifiedVersion(), is("20.6.0.m01"));
        assertThat(new MavenVersion("20.6.0.rc01").getUnqualifiedVersion(), is("20.6.0.rc01"));
        assertThat(new MavenVersion("20.6.0.release").getUnqualifiedVersion(), is("20.6.0.release"));

    }

    @Test
    void testCompareTo() {
        assertThat(new MavenVersion("1").compareTo(new MavenVersion("1")), is(0));
        assertThat(new MavenVersion("1.0.0").compareTo(new MavenVersion("1")), is(0));
        assertThat(new MavenVersion("1").compareTo(new MavenVersion("2")), is(lessThan(0)));
        assertThat(new MavenVersion("2").compareTo(new MavenVersion("1")), is(greaterThan(0)));
    }
}
