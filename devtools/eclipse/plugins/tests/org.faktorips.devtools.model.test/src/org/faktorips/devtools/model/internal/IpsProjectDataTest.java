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

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.LinkedHashSet;

import org.faktorips.devtools.model.internal.ipsproject.AbstractIpsObjectPathContainer;
import org.faktorips.devtools.model.internal.ipsproject.IpsProject;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPathContainer;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPathContainerType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsProjectProperties;
import org.junit.Test;

public class IpsProjectDataTest {

    private static final String MY_IPS_SRC_FILE = "my.qualified.IpsSrcFile";

    private IpsObjectPathContainerFactory factory = new IpsObjectPathContainerFactory();
    private IIpsProject ipsProject = spy(new IpsProject(mock(IpsModel.class), "TestProject"));
    private IpsProjectData ipsProjectData = new IpsProjectData(ipsProject, factory);

    @Test
    public void getIpsObjectPathContainer_should_createNewContainerIfNotCached() {
        IIpsObjectPathContainerType containerType = newContainerType("JDT");
        IIpsObjectPathContainer container1 = mock(IIpsObjectPathContainer.class);
        when(containerType.newContainer(ipsProject, "path1")).thenReturn(container1);
        IIpsObjectPathContainer container2 = mock(IIpsObjectPathContainer.class);
        when(containerType.newContainer(ipsProject, "path2")).thenReturn(container2);

        assertEquals(container1, ipsProjectData.getIpsObjectPathContainer("JDT", "path1"));
        assertEquals(container2, ipsProjectData.getIpsObjectPathContainer("JDT", "path2"));
    }

    @Test
    public void getIpsObjectPathContainer_should_returnCachedContainerIfInCache() {
        IIpsObjectPathContainerType containerType = newContainerType("JDT");
        AbstractIpsObjectPathContainer abstractIpsObjectPathContainer = mock(AbstractIpsObjectPathContainer.class);
        when(containerType.newContainer(ipsProject, "path")).thenReturn(abstractIpsObjectPathContainer);

        IIpsObjectPathContainer container = ipsProjectData.getIpsObjectPathContainer("JDT", "path1");
        assertSame(container, ipsProjectData.getIpsObjectPathContainer("JDT", "path1"));
    }

    @Test
    public void getIpsObjectPathContainer_should_ReturnNullIfNoContainerTypeAvailableById() {
        ipsProjectData.getIpsObjectPathContainer("UnknownId", "some");
    }

    private IIpsObjectPathContainerType newContainerType(String id) {
        IIpsObjectPathContainerType containerType = mock(IIpsObjectPathContainerType.class);
        when(containerType.getId()).thenReturn(id);
        factory.registerContainerType(containerType);
        return containerType;
    }

    @Test
    public void testGetMarkerEnums_empty() throws Exception {
        LinkedHashSet<IIpsSrcFile> markerEnums = ipsProjectData.getMarkerEnums();

        assertTrue(markerEnums.isEmpty());
    }

    @Test
    public void testGetMarkerEnums_invalid() throws Exception {
        IIpsProjectProperties properties = ipsProject.getProperties();
        properties.addMarkerEnum("invalid");
        when(ipsProject.getReadOnlyProperties()).thenReturn(properties);

        LinkedHashSet<IIpsSrcFile> markerEnums = ipsProjectData.getMarkerEnums();

        assertTrue(markerEnums.isEmpty());
    }

    @Test
    public void testGetMarkerEnums_notExisting() throws Exception {
        IIpsSrcFile ipsSrcFile = mock(IIpsSrcFile.class);
        when(ipsSrcFile.exists()).thenReturn(false);
        when(ipsProject.findIpsSrcFile(IpsObjectType.ENUM_TYPE, MY_IPS_SRC_FILE)).thenReturn(ipsSrcFile);

        IIpsProjectProperties properties = ipsProject.getProperties();
        properties.addMarkerEnum(MY_IPS_SRC_FILE);
        when(ipsProject.getReadOnlyProperties()).thenReturn(properties);

        LinkedHashSet<IIpsSrcFile> markerEnums = ipsProjectData.getMarkerEnums();

        assertTrue(markerEnums.isEmpty());
    }

    @Test
    public void testGetMarkerEnums_existing() throws Exception {
        IIpsSrcFile ipsSrcFile = mock(IIpsSrcFile.class);
        when(ipsSrcFile.exists()).thenReturn(true);
        when(ipsProject.findIpsSrcFile(IpsObjectType.ENUM_TYPE, MY_IPS_SRC_FILE)).thenReturn(ipsSrcFile);

        IIpsProjectProperties properties = ipsProject.getProperties();
        properties.addMarkerEnum(MY_IPS_SRC_FILE);
        when(ipsProject.getReadOnlyProperties()).thenReturn(properties);

        LinkedHashSet<IIpsSrcFile> markerEnums = ipsProjectData.getMarkerEnums();

        assertFalse(markerEnums.isEmpty());
        assertThat(markerEnums, hasItems(ipsSrcFile));
    }

}
