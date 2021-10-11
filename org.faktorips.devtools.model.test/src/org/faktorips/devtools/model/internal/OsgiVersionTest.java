/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class OsgiVersionTest {

    @Test
    public void testOsgiVersion() throws Exception {
        OsgiVersion osgiVersion = new OsgiVersion("1.2.3.asd");

        assertEquals("1.2.3.asd", osgiVersion.asString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOsgiVersion_invalidVersionFormat() throws Exception {
        new OsgiVersion("asd");
    }

    @Test
    public void testAsString_expandVersionFormat() throws Exception {
        OsgiVersion osgiVersion = new OsgiVersion("1");

        assertEquals("1.0.0", osgiVersion.asString());
    }

    @Test
    public void testCompareTo() throws Exception {
        assertTrue(new OsgiVersion("1.0.0").compareTo(new OsgiVersion("1.0.0.a")) < 0);
        assertTrue(new OsgiVersion("1.0.1").compareTo(new OsgiVersion("1.0.0.a")) > 0);
        assertTrue(new OsgiVersion("1").compareTo(new OsgiVersion("1.0.0")) == 0);
    }

    @Test
    public void testEquals() throws Exception {
        assertEquals(new OsgiVersion("1"), new OsgiVersion("1.0.0"));
        assertEquals(new OsgiVersion("1.2"), new OsgiVersion("1.2.0"));
        assertEquals(new OsgiVersion("1.2.5"), new OsgiVersion("1.2.5"));
        assertEquals(new OsgiVersion("1.2.5.asd"), new OsgiVersion("1.2.5.asd"));
        assertFalse(new OsgiVersion("1").equals(new OsgiVersion("1.0.0.a")));
        assertFalse(new OsgiVersion("1").equals(null));
    }

    @Test
    public void testGetUnqualifiedVersion() throws Exception {
        assertThat(new OsgiVersion("0.0.0").getUnqualifiedVersion(), is("0.0.0"));
        assertThat(new OsgiVersion("1.5").getUnqualifiedVersion(), is("1.5.0"));
        assertThat(new OsgiVersion("1.5.7.9").getUnqualifiedVersion(), is("1.5.7"));
        assertThat(new OsgiVersion("47.11.0.Abc").getUnqualifiedVersion(), is("47.11.0"));
        assertThat(new OsgiVersion("42.23.432").getUnqualifiedVersion(), is("42.23.432"));
        assertThat(new OsgiVersion("11.8.05.SNAPSHOT").getUnqualifiedVersion(), is("11.8.5"));
        assertThat(new OsgiVersion("1.2.3.qualifier").getUnqualifiedVersion(), is("1.2.3"));
    }

}
