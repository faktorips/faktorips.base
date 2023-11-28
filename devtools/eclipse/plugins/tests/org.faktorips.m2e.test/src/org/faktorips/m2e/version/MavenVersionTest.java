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

public class MavenVersionTest {

    @Test
    public void testIsEmptyVersion() {
        assertThat(new MavenVersion("").isEmptyVersion(), is(true));
        assertThat(new MavenVersion("1.0").isEmptyVersion(), is(false));
        assertThat(new MavenVersion("FooBar").isEmptyVersion(), is(false));
        assertThat(new MavenVersion("mvn:org.faktorips:testproducts").isEmptyVersion(), is(false));
    }

    @Test
    public void testIsCorrectVersionFormat() {
        assertThat(MavenVersion.isCorrectVersionFormat("Wrdlbrmpft"), is(false));
        assertThat(MavenVersion.isCorrectVersionFormat(""), is(false));
        assertThat(MavenVersion.isCorrectVersionFormat("1"), is(true));
        assertThat(MavenVersion.isCorrectVersionFormat("1.0"), is(true));
        assertThat(MavenVersion.isCorrectVersionFormat("1.0.5"), is(true));
        assertThat(MavenVersion.isCorrectVersionFormat("1.0-SNAPSHOT"), is(true));
        assertThat(MavenVersion.isCorrectVersionFormat("2.3.4-Wrdlbrmpft"), is(true));
        assertThat(MavenVersion.isCorrectVersionFormat("mvn:org.faktorips:testproducts"), is(false));
    }

    @Test
    public void testGetUnqualifiedVersion() throws Exception {
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
        assertThat(new MavenVersion("mvn:org.faktorips:testproducts").getUnqualifiedVersion(),
                is("mvn:org.faktorips:testproducts"));
        assertThat(new MavenVersion("mvn:org.faktorips-test:test-products").getUnqualifiedVersion(),
                is("mvn:org.faktorips-test:test-products"));
    }
}
