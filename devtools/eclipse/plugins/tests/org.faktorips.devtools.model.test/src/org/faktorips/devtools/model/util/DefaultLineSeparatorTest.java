/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.util;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import org.faktorips.devtools.abstraction.AProject;
import org.faktorips.devtools.abstraction.AResource;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.junit.Test;

public class DefaultLineSeparatorTest {

    @Test
    public void testOfSystem() {
        assertThat(DefaultLineSeparator.ofSystem(), is(System.lineSeparator()));
    }

    @Test
    public void testOfAProject() {
        AProject project = mock(AProject.class);
        String lineSeparator = "LineSeparator";
        doReturn(lineSeparator).when(project).getDefaultLineSeparator();

        assertThat(DefaultLineSeparator.of(project), is(lineSeparator));
    }

    @Test
    public void testOfAProject_Null() {
        assertThat(DefaultLineSeparator.of((AProject)null), is(System.lineSeparator()));
    }

    @Test
    public void testOfAResource() {
        AProject project = mock(AProject.class);
        String lineSeparator = "LineSeparator";
        doReturn(lineSeparator).when(project).getDefaultLineSeparator();
        AResource resource = mock(AResource.class);
        doReturn(project).when(resource).getProject();

        assertThat(DefaultLineSeparator.of(resource), is(lineSeparator));
    }

    @Test
    public void testOfAResource_Null() {
        assertThat(DefaultLineSeparator.of((AResource)null), is(System.lineSeparator()));
    }

    @Test
    public void testOfAResource_NullProject() {
        AResource resource = mock(AResource.class);

        assertThat(DefaultLineSeparator.of(resource), is(System.lineSeparator()));
    }

    @Test
    public void testOfIIpsProject() {
        AProject project = mock(AProject.class);
        String lineSeparator = "LineSeparator";
        doReturn(lineSeparator).when(project).getDefaultLineSeparator();
        IIpsProject ipsProject = mock(IIpsProject.class);
        doReturn(project).when(ipsProject).getProject();

        assertThat(DefaultLineSeparator.of(ipsProject), is(lineSeparator));
    }

    @Test
    public void testOfIIpsProject_Null() {
        assertThat(DefaultLineSeparator.of((IIpsProject)null), is(System.lineSeparator()));
    }

    @Test
    public void testOfIIpsProject_NullProject() {
        IIpsProject ipsProject = mock(IIpsProject.class);

        assertThat(DefaultLineSeparator.of(ipsProject), is(System.lineSeparator()));
    }

    @Test
    public void testOfIIpsSrcFile() {
        AProject project = mock(AProject.class);
        String lineSeparator = "LineSeparator";
        doReturn(lineSeparator).when(project).getDefaultLineSeparator();
        IIpsProject ipsProject = mock(IIpsProject.class);
        doReturn(project).when(ipsProject).getProject();
        IIpsSrcFile ipsSrcFile = mock(IIpsSrcFile.class);
        doReturn(ipsProject).when(ipsSrcFile).getIpsProject();

        assertThat(DefaultLineSeparator.of(ipsSrcFile), is(lineSeparator));
    }

    @Test
    public void testOfIIpsSrcFile_Null() {
        assertThat(DefaultLineSeparator.of((IIpsSrcFile)null), is(System.lineSeparator()));
    }

    @Test
    public void testOfIIpsSrcFile_NullIpsProject() {
        IIpsSrcFile ipsSrcFile = mock(IIpsSrcFile.class);

        assertThat(DefaultLineSeparator.of(ipsSrcFile), is(System.lineSeparator()));
    }

    @Test
    public void testOfIIpsObject() {
        AProject project = mock(AProject.class);
        String lineSeparator = "LineSeparator";
        doReturn(lineSeparator).when(project).getDefaultLineSeparator();
        IIpsProject ipsProject = mock(IIpsProject.class);
        doReturn(project).when(ipsProject).getProject();
        IIpsObject ipsObject = mock(IIpsObject.class);
        doReturn(ipsProject).when(ipsObject).getIpsProject();

        assertThat(DefaultLineSeparator.of(ipsObject), is(lineSeparator));
    }

    @Test
    public void testOfIIpsObject_Null() {
        assertThat(DefaultLineSeparator.of((IIpsObject)null), is(System.lineSeparator()));
    }

    @Test
    public void testOfIIpsObject_NullIpsProject() {
        IIpsObject ipsObject = mock(IIpsObject.class);

        assertThat(DefaultLineSeparator.of(ipsObject), is(System.lineSeparator()));
    }

}
