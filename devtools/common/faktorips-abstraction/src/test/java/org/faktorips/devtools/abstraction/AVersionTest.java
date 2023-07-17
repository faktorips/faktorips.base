/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
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
        assertThat(AVersion.parse("1").hashCode(), is(AVersion.parse("1.0").hashCode())); //$NON-NLS-1$ //$NON-NLS-2$
        assertThat(AVersion.parse("1.2").hashCode(), is(AVersion.parse("1.2.0").hashCode())); //$NON-NLS-1$ //$NON-NLS-2$
        assertThat(AVersion.parse("1.2").hashCode(), is(not(AVersion.parse("1.2.1").hashCode()))); //$NON-NLS-1$ //$NON-NLS-2$
        assertThat(AVersion.parse("1.2.3").hashCode(), is(not(AVersion.parse("1.2.3.alpha").hashCode()))); //$NON-NLS-1$ //$NON-NLS-2$
        assertThat(AVersion.parse("1.2.3").hashCode(), is(AVersion.parse("1.2.3.qualifier").hashCode())); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Test
    public void testParse() {
        assertThat(AVersion.parse(""), is(AVersion.VERSION_ZERO)); //$NON-NLS-1$
        assertThat(AVersion.parse(" "), is(AVersion.VERSION_ZERO)); //$NON-NLS-1$

        assertThat(AVersion.parse("1").getMajor(), is("1")); //$NON-NLS-1$ //$NON-NLS-2$
        assertThat(AVersion.parse("1").getMinor(), is("0")); //$NON-NLS-1$ //$NON-NLS-2$
        assertThat(AVersion.parse("1").toString(), is("1")); //$NON-NLS-1$ //$NON-NLS-2$

        assertThat(AVersion.parse("1.2").getMajor(), is("1")); //$NON-NLS-1$ //$NON-NLS-2$
        assertThat(AVersion.parse("1.2").getMinor(), is("2")); //$NON-NLS-1$ //$NON-NLS-2$
        assertThat(AVersion.parse("1.2").toString(), is("1.2")); //$NON-NLS-1$ //$NON-NLS-2$

        assertThat(AVersion.parse("1.2.0").getMajor(), is("1")); //$NON-NLS-1$ //$NON-NLS-2$
        assertThat(AVersion.parse("1.2.0").getMinor(), is("2")); //$NON-NLS-1$ //$NON-NLS-2$
        assertThat(AVersion.parse("1.2.0").getPatch(), is("0")); //$NON-NLS-1$ //$NON-NLS-2$
        assertThat(AVersion.parse("1.2.0").toString(), is("1.2")); //$NON-NLS-1$ //$NON-NLS-2$

        assertThat(AVersion.parse("1.2.3").getMajor(), is("1")); //$NON-NLS-1$ //$NON-NLS-2$
        assertThat(AVersion.parse("1.2.3").getMinor(), is("2")); //$NON-NLS-1$ //$NON-NLS-2$
        assertThat(AVersion.parse("1.2.3").getPatch(), is("3")); //$NON-NLS-1$ //$NON-NLS-2$
        assertThat(AVersion.parse("1.2.3").toString(), is("1.2.3")); //$NON-NLS-1$ //$NON-NLS-2$

        assertThat(AVersion.parse("1.2.3.qualifier").getMajor(), is("1")); //$NON-NLS-1$ //$NON-NLS-2$
        assertThat(AVersion.parse("1.2.3.qualifier").getMinor(), is("2")); //$NON-NLS-1$ //$NON-NLS-2$
        assertThat(AVersion.parse("1.2.3.qualifier").getPatch(), is("3")); //$NON-NLS-1$ //$NON-NLS-2$
        assertThat(AVersion.parse("1.2.3.qualifier").toString(), is("1.2.3")); //$NON-NLS-1$ //$NON-NLS-2$

        assertThat(AVersion.parse("1.2.3.alpha2").getMajor(), is("1")); //$NON-NLS-1$ //$NON-NLS-2$
        assertThat(AVersion.parse("1.2.3.alpha2").getMinor(), is("2")); //$NON-NLS-1$ //$NON-NLS-2$
        assertThat(AVersion.parse("1.2.3.alpha2").getPatch(), is("3")); //$NON-NLS-1$ //$NON-NLS-2$
        assertThat(AVersion.parse("1.2.3.alpha2").toString(), is("1.2.3.alpha2")); //$NON-NLS-1$ //$NON-NLS-2$

        assertThat(AVersion.parse("1.2.3.0.alpha2").getMajor(), is("1")); //$NON-NLS-1$ //$NON-NLS-2$
        assertThat(AVersion.parse("1.2.3.0.alpha2").getMinor(), is("2")); //$NON-NLS-1$ //$NON-NLS-2$
        assertThat(AVersion.parse("1.2.3.0.alpha2").getPatch(), is("3")); //$NON-NLS-1$ //$NON-NLS-2$
        assertThat(AVersion.parse("1.2.3.0.alpha2").toString(), is("1.2.3.alpha2")); //$NON-NLS-1$ //$NON-NLS-2$

        assertThat(AVersion.parse("1.2.3.202203301419").getMajor(), is("1")); //$NON-NLS-1$ //$NON-NLS-2$
        assertThat(AVersion.parse("1.2.3.202203301419").getMinor(), is("2")); //$NON-NLS-1$ //$NON-NLS-2$
        assertThat(AVersion.parse("1.2.3.202203301419").getPatch(), is("3")); //$NON-NLS-1$ //$NON-NLS-2$
        assertThat(AVersion.parse("1.2.3.202203301419").toString(), is("1.2.3.202203301419")); //$NON-NLS-1$ //$NON-NLS-2$

        assertThat(AVersion.parse("24.1.0.ci_20230711-1341").getMajor(), is("24"));
        assertThat(AVersion.parse("24.1.0.ci_20230711-1341").getMinor(), is("1"));
        assertThat(AVersion.parse("24.1.0.ci_20230711-1341").getPatch(), is("0"));
        assertThat(AVersion.parse("24.1.0.ci_20230711-1341").toString(), is("24.1.ci_20230711-1341"));

        assertThat(AVersion.parse("24.1.1.ci_20230711-1341").getMajor(), is("24"));
        assertThat(AVersion.parse("24.1.1.ci_20230711-1341").getMinor(), is("1"));
        assertThat(AVersion.parse("24.1.1.ci_20230711-1341").getPatch(), is("1"));
        assertThat(AVersion.parse("24.1.1.ci_20230711-1341").toString(), is("24.1.1.ci_20230711-1341"));
    }

    @Test
    public void testCompareTo() {
        assertThat(AVersion.parse("1").compareTo(AVersion.parse("1.0")), is(0)); //$NON-NLS-1$ //$NON-NLS-2$
        assertThat(AVersion.parse("1.0").compareTo(AVersion.parse("1")), is(0)); //$NON-NLS-1$ //$NON-NLS-2$
        assertThat(AVersion.parse("1.2").compareTo(AVersion.parse("1.2.0")), is(0)); //$NON-NLS-1$ //$NON-NLS-2$
        assertThat(AVersion.parse("1.2.0").compareTo(AVersion.parse("1.2")), is(0)); //$NON-NLS-1$ //$NON-NLS-2$
        assertThat(AVersion.parse("1.2").compareTo(AVersion.parse("1.2.1")), is(lowerVersion())); //$NON-NLS-1$ //$NON-NLS-2$
        assertThat(AVersion.parse("1.2.1").compareTo(AVersion.parse("1.2")), is(higherVersion())); //$NON-NLS-1$ //$NON-NLS-2$
        assertThat(AVersion.parse("1.2.3").compareTo(AVersion.parse("1.2.1")), is(higherVersion())); //$NON-NLS-1$ //$NON-NLS-2$
        assertThat(AVersion.parse("1.2.1").compareTo(AVersion.parse("1.2.3")), is(lowerVersion())); //$NON-NLS-1$ //$NON-NLS-2$
        assertThat(AVersion.parse("1.2.3").compareTo(AVersion.parse("1.2.3.alpha")), is(lowerVersion())); //$NON-NLS-1$ //$NON-NLS-2$
        assertThat(AVersion.parse("1.2.3.alpha").compareTo(AVersion.parse("1.2.3")), is(higherVersion())); //$NON-NLS-1$ //$NON-NLS-2$
        assertThat(AVersion.parse("1.2.3").compareTo(AVersion.parse("1.2.3.qualifier")), is(0)); //$NON-NLS-1$ //$NON-NLS-2$
        assertThat(AVersion.parse("1.2.3.qualifier").compareTo(AVersion.parse("1.2.3")), is(0)); //$NON-NLS-1$ //$NON-NLS-2$

        assertThat(AVersion.parse("24.1.0.rc01").compareTo(AVersion.parse("24.1.0.rc02")), is(lowerVersion())); //$NON-NLS-1$ //$NON-NLS-2$
        assertThat(AVersion.parse("24.1.0.rc02").compareTo(AVersion.parse("24.1.0.rc01")), is(higherVersion())); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Test
    public void testCompareTo_ReleaseCycle() {
        // from 24.1.0 to 24.1.1
        assertThat(AVersion.parse("24.1.0").compareTo(AVersion.parse("23.12.5.release")), is(higherVersion())); //$NON-NLS-1$ //$NON-NLS-2$
        assertThat(AVersion.parse("24.1.0.ci_20240117-1341").compareTo(AVersion.parse("24.1.0")), is(higherVersion()));//$NON-NLS-1$ //$NON-NLS-2$
        assertThat(AVersion.parse("24.1.0.a20240117-01").compareTo(AVersion.parse("24.1.0.ci_20240117-1341")), //$NON-NLS-1$ //$NON-NLS-2$
                is(higherVersion()));
        assertThat(AVersion.parse("24.1.0.a20240117-02").compareTo(AVersion.parse("24.1.0.a20240117-01")), //$NON-NLS-1$ //$NON-NLS-2$
                is(higherVersion()));
        assertThat(AVersion.parse("24.1.0.a20240217-01").compareTo(AVersion.parse("24.1.0.a20240117-02")), //$NON-NLS-1$ //$NON-NLS-2$
                is(higherVersion()));
        assertThat(AVersion.parse("24.1.0.m01").compareTo(AVersion.parse("24.1.0.a20220217-01")), is(higherVersion())); //$NON-NLS-1$ //$NON-NLS-2$
        assertThat(AVersion.parse("24.1.0.m02").compareTo(AVersion.parse("24.1.0.m01")), is(higherVersion()));//$NON-NLS-1$ //$NON-NLS-2$
        assertThat(AVersion.parse("24.1.0.rc01").compareTo(AVersion.parse("24.1.0.m02")), is(higherVersion())); //$NON-NLS-1$ //$NON-NLS-2$
        assertThat(AVersion.parse("24.1.0.rc02").compareTo(AVersion.parse("24.1.0.rc01")), is(higherVersion())); //$NON-NLS-1$ //$NON-NLS-2$
        assertThat(AVersion.parse("24.1.0.release").compareTo(AVersion.parse("24.1.0.rc02")), is(higherVersion())); //$NON-NLS-1$ //$NON-NLS-2$
        assertThat(AVersion.parse("24.1.1").compareTo(AVersion.parse("24.1.0.release")), is(higherVersion())); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Test
    public void testSnapshotQualifiers() {
        assertThat(AVersion.parse("24.1.0.ci_20230711-1341").isSnapshot(), is(true)); //$NON-NLS-1$
        assertThat(AVersion.parse("24.1.0.ci_20230711-1341").isAlpha(), is(false)); //$NON-NLS-1$
        assertThat(AVersion.parse("24.1.0.ci_20230711-1341").isMilestone(), is(false)); //$NON-NLS-1$
        assertThat(AVersion.parse("24.1.0.ci_20230711-1341").isReleaseCandidate(), is(false)); //$NON-NLS-1$
        assertThat(AVersion.parse("24.1.0.ci_20230711-1341").isRelease(), is(false)); //$NON-NLS-1$
    }

    @Test
    public void testAlphaQualifiers() {
        assertThat(AVersion.parse("24.1.0.a20221117-02").isSnapshot(), is(false)); //$NON-NLS-1$
        assertThat(AVersion.parse("24.1.0.a20221117-02").isAlpha(), is(true)); //$NON-NLS-1$
        assertThat(AVersion.parse("24.1.0.a20221117-02").isMilestone(), is(false)); //$NON-NLS-1$
        assertThat(AVersion.parse("24.1.0.a20221117-02").isReleaseCandidate(), is(false)); //$NON-NLS-1$
        assertThat(AVersion.parse("24.1.0.a20221117-02").isRelease(), is(false)); //$NON-NLS-1$
    }

    @Test
    public void testMilestoneQualifiers() {
        assertThat(AVersion.parse("24.1.0.m01").isSnapshot(), is(false)); //$NON-NLS-1$
        assertThat(AVersion.parse("24.1.0.m01").isAlpha(), is(false)); //$NON-NLS-1$
        assertThat(AVersion.parse("24.1.0.m01").isMilestone(), is(true)); //$NON-NLS-1$
        assertThat(AVersion.parse("24.1.0.m01").isReleaseCandidate(), is(false)); //$NON-NLS-1$
        assertThat(AVersion.parse("24.1.0.m01").isRelease(), is(false)); //$NON-NLS-1$
    }

    @Test
    public void testReleaseCandidateQualifiers() {
        assertThat(AVersion.parse("24.1.0.rc01").isSnapshot(), is(false)); //$NON-NLS-1$
        assertThat(AVersion.parse("24.1.0.rc01").isAlpha(), is(false)); //$NON-NLS-1$
        assertThat(AVersion.parse("24.1.0.rc01").isMilestone(), is(false)); //$NON-NLS-1$
        assertThat(AVersion.parse("24.1.0.rc01").isReleaseCandidate(), is(true)); //$NON-NLS-1$
        assertThat(AVersion.parse("24.1.0.rc01").isRelease(), is(false)); //$NON-NLS-1$
    }

    @Test
    public void testReleaseQualifiers() {
        assertThat(AVersion.parse("24.1.0.release").isSnapshot(), is(false)); //$NON-NLS-1$
        assertThat(AVersion.parse("24.1.0.release").isAlpha(), is(false)); //$NON-NLS-1$
        assertThat(AVersion.parse("24.1.0.release").isMilestone(), is(false)); //$NON-NLS-1$
        assertThat(AVersion.parse("24.1.0.release").isReleaseCandidate(), is(false)); //$NON-NLS-1$
        assertThat(AVersion.parse("24.1.0.release").isRelease(), is(true)); //$NON-NLS-1$
    }

    @Test
    public void testEquals() {
        assertThat(AVersion.parse("1"), is(AVersion.parse("1.0"))); //$NON-NLS-1$ //$NON-NLS-2$
        assertThat(AVersion.parse("1.0"), is(AVersion.parse("1"))); //$NON-NLS-1$ //$NON-NLS-2$
        assertThat(AVersion.parse("1.2"), is(AVersion.parse("1.2.0"))); //$NON-NLS-1$ //$NON-NLS-2$
        assertThat(AVersion.parse("1.2.0"), is(AVersion.parse("1.2"))); //$NON-NLS-1$ //$NON-NLS-2$
        assertThat(AVersion.parse("1.2"), is(not(AVersion.parse("1.2.1")))); //$NON-NLS-1$ //$NON-NLS-2$
        assertThat(AVersion.parse("1.2.1"), is(not(AVersion.parse("1.2")))); //$NON-NLS-1$ //$NON-NLS-2$
        assertThat(AVersion.parse("1.2.3"), is(not(AVersion.parse("1.2.3.alpha")))); //$NON-NLS-1$ //$NON-NLS-2$
        assertThat(AVersion.parse("1.2.3.alpha"), is(not(AVersion.parse("1.2.3")))); //$NON-NLS-1$ //$NON-NLS-2$
        assertThat(AVersion.parse("1.2.3"), is(AVersion.parse("1.2.3.qualifier"))); //$NON-NLS-1$ //$NON-NLS-2$
        assertThat(AVersion.parse("1.2.3.qualifier"), is(AVersion.parse("1.2.3"))); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Test
    public void testMajorMinor() {
        assertThat(AVersion.parse("").majorMinor(), is(AVersion.VERSION_ZERO)); //$NON-NLS-1$
        assertThat(AVersion.parse(" ").majorMinor(), is(AVersion.VERSION_ZERO)); //$NON-NLS-1$
        assertThat(AVersion.parse("1").majorMinor().toString(), is("1")); //$NON-NLS-1$ //$NON-NLS-2$
        assertThat(AVersion.parse("1.2").majorMinor().toString(), is("1.2")); //$NON-NLS-1$ //$NON-NLS-2$
        assertThat(AVersion.parse("1.2.3").majorMinor().toString(), is("1.2")); //$NON-NLS-1$ //$NON-NLS-2$
        assertThat(AVersion.parse("1.2.3.qualifier").majorMinor().toString(), is("1.2")); //$NON-NLS-1$ //$NON-NLS-2$
        assertThat(AVersion.parse("1.2.3.alpha2").majorMinor().toString(), is("1.2")); //$NON-NLS-1$ //$NON-NLS-2$
        assertThat(AVersion.parse("1.2.3.202203301419").majorMinor().toString(), is("1.2")); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Test
    public void testMajorMinorPatch() {
        assertThat(AVersion.parse("").majorMinorPatch(), is(AVersion.VERSION_ZERO)); //$NON-NLS-1$
        assertThat(AVersion.parse(" ").majorMinorPatch(), is(AVersion.VERSION_ZERO)); //$NON-NLS-1$
        assertThat(AVersion.parse("1").majorMinorPatch().toString(), is("1")); //$NON-NLS-1$ //$NON-NLS-2$
        assertThat(AVersion.parse("1.2").majorMinorPatch().toString(), is("1.2")); //$NON-NLS-1$ //$NON-NLS-2$
        assertThat(AVersion.parse("1.2.3").majorMinorPatch().toString(), is("1.2.3")); //$NON-NLS-1$ //$NON-NLS-2$
        assertThat(AVersion.parse("1.2.3.qualifier").majorMinorPatch().toString(), is("1.2.3")); //$NON-NLS-1$ //$NON-NLS-2$
        assertThat(AVersion.parse("1.2.3.alpha2").majorMinorPatch().toString(), is("1.2.3")); //$NON-NLS-1$ //$NON-NLS-2$
        assertThat(AVersion.parse("1.2.3.202203301419").majorMinorPatch().toString(), is("1.2.3")); //$NON-NLS-1$ //$NON-NLS-2$
    }

    private static Matcher<Integer> lowerVersion() {
        return new TypeSafeMatcher<>() {

            @Override
            public void describeTo(Description description) {
                description.appendText("negative"); //$NON-NLS-1$
            }

            @Override
            protected boolean matchesSafely(Integer actual) {
                return actual < 0;
            }
        };
    }

    private static Matcher<Integer> higherVersion() {
        return new TypeSafeMatcher<>() {

            @Override
            public void describeTo(Description description) {
                description.appendText("positive"); //$NON-NLS-1$
            }

            @Override
            protected boolean matchesSafely(Integer actual) {
                return actual > 0;
            }
        };
    }

}
