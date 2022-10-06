/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.abstraction.plainjava.internal;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Set;

import org.faktorips.devtools.abstraction.AMarker;
import org.faktorips.devtools.abstraction.AProject;
import org.faktorips.devtools.abstraction.AResource.AResourceTreeTraversalDepth;
import org.faktorips.runtime.Severity;
import org.junit.Before;
import org.junit.Test;

public class PlainJavaMarkerTest extends PlainJavaAbstractionTestSetup {

    private static final String MARKER_TYPE = "TestMarker"; //$NON-NLS-1$
    private AProject testProject;

    @Before
    public void setUp() {
        testProject = newSimpleIpsProject("TestProject"); //$NON-NLS-1$
    }

    @Test
    public void testPlainJavaMarker() {
        AMarker marker = testProject.createMarker(MARKER_TYPE);

        assertThat(marker.unwrap(), is(instanceOf(PlainJavaMarkerImpl.class)));
    }

    @Test
    public void testDelete() {
        AMarker marker = testProject.createMarker(MARKER_TYPE);

        assertThat(marker, is(notNullValue()));
        assertThat(marker.getType(), is(MARKER_TYPE));

        marker.delete();

        assertThat(testProject.findMarkers(MARKER_TYPE, true, AResourceTreeTraversalDepth.INFINITE).isEmpty(),
                is(true));
    }

    @Test
    public void testIsError() {
        AMarker marker = testProject.createMarker(MARKER_TYPE);

        marker.setAttribute(PlainJavaMarkerImpl.SEVERITY, Severity.ERROR);

        assertThat(marker.isError(), is(true));
    }

    @Test
    public void testGetAttribute() {
        AMarker marker = testProject.createMarker(MARKER_TYPE);
        marker.setAttribute(PlainJavaMarkerImpl.SEVERITY, Severity.ERROR);

        assertThat(marker.getAttribute(PlainJavaMarkerImpl.SEVERITY), is(Severity.ERROR));
        // diff to eclipse is -> ERROR marker is an integer, here it is an enum
        assertThat(marker.getAttribute(PlainJavaMarkerImpl.SEVERITY, -1), is(-1));
        assertThat(marker.getAttribute(PlainJavaMarkerImpl.SEVERITY, false), is(false));
        assertThat(marker.getAttribute(PlainJavaMarkerImpl.SEVERITY, "marker_is_not_string_return_this_string"), //$NON-NLS-1$
                is("marker_is_not_string_return_this_string")); //$NON-NLS-1$
    }

    @Test
    public void testSetAttributes() {
        AMarker marker = testProject.createMarker(MARKER_TYPE);

        marker.setAttributes(new String[] { "Name1", "Name2" }, new Object[] { "Obj1", false }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        assertThat(marker.getAttribute("Name1", "default"), is("Obj1")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        assertThat(marker.getAttribute("Name1", true), is(true)); //$NON-NLS-1$

        assertThat(marker.getAttribute("Name2", "default"), is("default")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        assertThat(marker.getAttribute("Name2", true), is(false)); //$NON-NLS-1$
    }

    @Test(expected = NullPointerException.class)
    public void testSetAttributes_NullAttributes() {
        AMarker marker = testProject.createMarker(MARKER_TYPE);

        marker.setAttributes(null, null);
    }

    @Test(expected = NullPointerException.class)
    public void testSetAttributes_NullValues() {
        AMarker marker = testProject.createMarker(MARKER_TYPE);

        marker.setAttributes(new String[] { "Key1" }, null); //$NON-NLS-1$
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetAttributes_MissmatchAttributesToValues() {
        AMarker marker = testProject.createMarker(MARKER_TYPE);

        marker.setAttributes(new String[] { "Key1" }, new Object[] { "Val1", "Val2" }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    @Test
    public void testEquals_false() {
        AMarker marker1 = testProject.createMarker(MARKER_TYPE);
        AMarker marker2 = testProject.createMarker(MARKER_TYPE);

        assertThat(marker1.unwrap().equals(marker2.unwrap()), is(false));
    }

    @Test
    public void testEquals_true() {
        AMarker marker1 = testProject.createMarker(MARKER_TYPE);

        Set<AMarker> findMarkers = testProject.findMarkers(MARKER_TYPE, false, AResourceTreeTraversalDepth.INFINITE);
        for (AMarker aMarker : findMarkers) {
            assertThat(marker1.unwrap().equals(aMarker.unwrap()), is(true));
        }
    }

    @Test
    public void testEqualsType_false() {
        AMarker marker1 = testProject.createMarker(MARKER_TYPE + "diff"); //$NON-NLS-1$

        assertThat(((PlainJavaMarkerImpl)marker1.unwrap()).equalsType(MARKER_TYPE), is(false));
    }

    @Test
    public void testEqualsType_true() {
        AMarker marker1 = testProject.createMarker(MARKER_TYPE);

        assertThat(((PlainJavaMarkerImpl)marker1.unwrap()).equalsType(MARKER_TYPE), is(true));
    }
}
