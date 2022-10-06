/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.abstraction.eclipse.internal;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.eclipse.core.resources.IMarker;
import org.faktorips.devtools.abstraction.AMarker;
import org.faktorips.devtools.abstraction.AProject;
import org.faktorips.devtools.abstraction.AResource.AResourceTreeTraversalDepth;
import org.junit.Before;
import org.junit.Test;

public class EclipseMarkerTest extends EclipseAbstractionTestSetup {

    private static final String MARKER_TYPE = "TestMarker";
    private AProject testProject;

    @Before
    public void setUp() {
        testProject = newSimpleIpsProject("TestProject");
    }

    @Test
    public void testAEclipseMarker() {
        AMarker marker = testProject.createMarker(MARKER_TYPE);

        assertThat(marker.unwrap(), is(instanceOf(IMarker.class)));
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

        marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);

        assertThat(marker.isError(), is(true));
    }

    @Test
    public void testGetAttribute() {
        AMarker marker = testProject.createMarker(MARKER_TYPE);
        marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);

        assertThat(marker.getAttribute(IMarker.SEVERITY), is(IMarker.SEVERITY_ERROR));
        assertThat(marker.getAttribute(IMarker.SEVERITY, -1), is(IMarker.SEVERITY_ERROR));
        assertThat(marker.getAttribute(IMarker.SEVERITY, false), is(false));
        assertThat(marker.getAttribute(IMarker.SEVERITY, "marker_is_not_string_return_this_string"),
                is("marker_is_not_string_return_this_string"));
    }

    @Test
    public void testSetAttributes() {
        AMarker marker = testProject.createMarker(MARKER_TYPE);

        marker.setAttributes(new String[] { "Name1", "Name2" }, new Object[] { "Obj1", false });

        assertThat(marker.getAttribute("Name1", "default"), is("Obj1"));
        assertThat(marker.getAttribute("Name1", true), is(true));

        assertThat(marker.getAttribute("Name2", "default"), is("default"));
        assertThat(marker.getAttribute("Name2", true), is(false));
    }
}
