/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.abstraction;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

public class AVersionTest {

    @Test
    public void testHashCode() {
        assertThat(AVersion.parse("1").hashCode(), is(AVersion.parse("1.0").hashCode()));
        assertThat(AVersion.parse("1.2").hashCode(), is(AVersion.parse("1.2.0").hashCode()));
        assertThat(AVersion.parse("1.2").hashCode(), is(not(AVersion.parse("1.2.1").hashCode())));
        assertThat(AVersion.parse("1.2.3").hashCode(), is(not(AVersion.parse("1.2.3.alpha").hashCode())));
        assertThat(AVersion.parse("1.2.3").hashCode(), is(AVersion.parse("1.2.3.qualifier").hashCode()));
    }

    @Test
    public void testParse() {
        assertThat(AVersion.parse(""), is(AVersion.VERSION_ZERO));
        assertThat(AVersion.parse(" "), is(AVersion.VERSION_ZERO));
        assertThat(AVersion.parse("1").getMajor(), is("1"));
        assertThat(AVersion.parse("1").getMinor(), is("0"));
        assertThat(AVersion.parse("1").toString(), is("1"));
        assertThat(AVersion.parse("1.2").getMajor(), is("1"));
        assertThat(AVersion.parse("1.2").getMinor(), is("2"));
        assertThat(AVersion.parse("1.2").toString(), is("1.2"));
        assertThat(AVersion.parse("1.2.0").getMajor(), is("1"));
        assertThat(AVersion.parse("1.2.0").getMinor(), is("2"));
        assertThat(AVersion.parse("1.2.0").toString(), is("1.2"));
        assertThat(AVersion.parse("1.2.3").getMajor(), is("1"));
        assertThat(AVersion.parse("1.2.3").getMinor(), is("2"));
        assertThat(AVersion.parse("1.2.3").toString(), is("1.2.3"));
        assertThat(AVersion.parse("1.2.3.qualifier").getMajor(), is("1"));
        assertThat(AVersion.parse("1.2.3.qualifier").getMinor(), is("2"));
        assertThat(AVersion.parse("1.2.3.qualifier").toString(), is("1.2.3"));
        assertThat(AVersion.parse("1.2.3.alpha2").getMajor(), is("1"));
        assertThat(AVersion.parse("1.2.3.alpha2").getMinor(), is("2"));
        assertThat(AVersion.parse("1.2.3.alpha2").toString(), is("1.2.3.alpha2"));
        assertThat(AVersion.parse("1.2.3.0.alpha2").getMajor(), is("1"));
        assertThat(AVersion.parse("1.2.3.0.alpha2").getMinor(), is("2"));
        assertThat(AVersion.parse("1.2.3.0.alpha2").toString(), is("1.2.3.alpha2"));
        assertThat(AVersion.parse("1.2.3.202203301419").getMajor(), is("1"));
        assertThat(AVersion.parse("1.2.3.202203301419").getMinor(), is("2"));
        assertThat(AVersion.parse("1.2.3.202203301419").toString(), is("1.2.3.202203301419"));
    }

    @Test
    public void testCompareTo() {
        assertThat(AVersion.parse("1").compareTo(AVersion.parse("1.0")), is(0));
        assertThat(AVersion.parse("1.0").compareTo(AVersion.parse("1")), is(0));
        assertThat(AVersion.parse("1.2").compareTo(AVersion.parse("1.2.0")), is(0));
        assertThat(AVersion.parse("1.2.0").compareTo(AVersion.parse("1.2")), is(0));
        assertThat(AVersion.parse("1.2").compareTo(AVersion.parse("1.2.1")), is(negative()));
        assertThat(AVersion.parse("1.2.1").compareTo(AVersion.parse("1.2")), is(positive()));
        assertThat(AVersion.parse("1.2.3").compareTo(AVersion.parse("1.2.1")), is(positive()));
        assertThat(AVersion.parse("1.2.1").compareTo(AVersion.parse("1.2.3")), is(negative()));
        assertThat(AVersion.parse("1.2.3").compareTo(AVersion.parse("1.2.3.alpha")), is(negative()));
        assertThat(AVersion.parse("1.2.3.alpha").compareTo(AVersion.parse("1.2.3")), is(positive()));
        assertThat(AVersion.parse("1.2.3").compareTo(AVersion.parse("1.2.3.qualifier")), is(0));
        assertThat(AVersion.parse("1.2.3.qualifier").compareTo(AVersion.parse("1.2.3")), is(0));
    }

    @Test
    public void testEquals() {
        assertThat(AVersion.parse("1"), is(AVersion.parse("1.0")));
        assertThat(AVersion.parse("1.0"), is(AVersion.parse("1")));
        assertThat(AVersion.parse("1.2"), is(AVersion.parse("1.2.0")));
        assertThat(AVersion.parse("1.2.0"), is(AVersion.parse("1.2")));
        assertThat(AVersion.parse("1.2"), is(not(AVersion.parse("1.2.1"))));
        assertThat(AVersion.parse("1.2.1"), is(not(AVersion.parse("1.2"))));
        assertThat(AVersion.parse("1.2.3"), is(not(AVersion.parse("1.2.3.alpha"))));
        assertThat(AVersion.parse("1.2.3.alpha"), is(not(AVersion.parse("1.2.3"))));
        assertThat(AVersion.parse("1.2.3"), is(AVersion.parse("1.2.3.qualifier")));
        assertThat(AVersion.parse("1.2.3.qualifier"), is(AVersion.parse("1.2.3")));
    }

    @Test
    public void testMajorMinor() {
        assertThat(AVersion.parse("").majorMinor(), is(AVersion.VERSION_ZERO));
        assertThat(AVersion.parse(" ").majorMinor(), is(AVersion.VERSION_ZERO));
        assertThat(AVersion.parse("1").majorMinor().toString(), is("1"));
        assertThat(AVersion.parse("1.2").majorMinor().toString(), is("1.2"));
        assertThat(AVersion.parse("1.2.3").majorMinor().toString(), is("1.2"));
        assertThat(AVersion.parse("1.2.3.qualifier").majorMinor().toString(), is("1.2"));
        assertThat(AVersion.parse("1.2.3.alpha2").majorMinor().toString(), is("1.2"));
        assertThat(AVersion.parse("1.2.3.202203301419").majorMinor().toString(), is("1.2"));
    }

    private static Matcher<Integer> negative() {
        return new TypeSafeMatcher<>() {

            @Override
            public void describeTo(Description description) {
                description.appendText("negative");
            }

            @Override
            protected boolean matchesSafely(Integer actual) {
                return actual < 0;
            }
        };
    }

    private static Matcher<Integer> positive() {
        return new TypeSafeMatcher<>() {

            @Override
            public void describeTo(Description description) {
                description.appendText("positive");
            }

            @Override
            protected boolean matchesSafely(Integer actual) {
                return actual > 0;
            }
        };
    }

}
