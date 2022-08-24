/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.eclipse.builder;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IMarker;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.abstraction.AFile;
import org.faktorips.devtools.abstraction.AMarker;
import org.faktorips.devtools.abstraction.AProject;
import org.faktorips.devtools.abstraction.AResource;
import org.faktorips.devtools.abstraction.AResource.AResourceTreeTraversalDepth;
import org.faktorips.devtools.abstraction.eclipse.internal.EclipseImplementation;
import org.faktorips.devtools.model.builder.IpsBuilder;
import org.faktorips.devtools.model.internal.ipsproject.IpsBundleManifest;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsProjectProperties;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(EclipseImplementation.class)
public class EclipseIpsBuilderTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;

    public EclipseIpsBuilderTest() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = this.newIpsProject();
    }

    @Test
    public void testCreateMarkersFromMessageList_keepSameMarkers() throws Exception {
        AMarker marker1 = mock(AMarker.class);
        when(marker1.getAttribute(IMarker.MESSAGE)).thenReturn("text1");
        when(marker1.getAttribute(IMarker.SEVERITY)).thenReturn(IMarker.SEVERITY_ERROR);
        AMarker marker2 = mock(AMarker.class);
        when(marker2.getAttribute(IMarker.MESSAGE)).thenReturn("text2");
        when(marker2.getAttribute(IMarker.SEVERITY)).thenReturn(IMarker.SEVERITY_WARNING);
        AResource resource = mock(AResource.class);
        when(resource.findMarkers(anyString(), anyBoolean(), any(AResourceTreeTraversalDepth.class)))
                .thenReturn(new LinkedHashSet<>(List.of(marker1, marker2)));
        MessageList list = new MessageList();
        list.add(new Message("", "text2", Message.WARNING));
        list.add(new Message("", "text1", Message.ERROR));
        IpsBuilder ipsBuilder = new EclipseIpsBuilder().getIpsBuilder();

        ipsBuilder.createMarkersFromMessageList(resource, list, "");

        verify(resource).findMarkers(anyString(), anyBoolean(), any(AResourceTreeTraversalDepth.class));
        verifyNoMoreInteractions(resource);
    }

    @Test
    public void testCreateMarkersFromMessageList_newMarkers() throws Exception {
        AMarker marker1 = mock(AMarker.class);
        when(marker1.getAttribute(IMarker.MESSAGE)).thenReturn("text1");
        when(marker1.getAttribute(IMarker.SEVERITY)).thenReturn(IMarker.SEVERITY_ERROR);
        AMarker marker2 = mock(AMarker.class);
        AResource resource = mock(AResource.class);
        when(resource.findMarkers(anyString(), anyBoolean(), any(AResourceTreeTraversalDepth.class)))
                .thenReturn(Set.of(marker1));
        when(resource.createMarker("")).thenReturn(marker2);
        MessageList list = new MessageList();
        list.add(new Message("", "text2", Message.WARNING));
        list.add(new Message("", "text1", Message.ERROR));
        IpsBuilder ipsBuilder = new EclipseIpsBuilder().getIpsBuilder();

        ipsBuilder.createMarkersFromMessageList(resource, list, "");

        verify(resource).findMarkers(anyString(), anyBoolean(), any(AResourceTreeTraversalDepth.class));
        verify(resource).createMarker("");
        verifyNoMoreInteractions(resource);
    }

    @Test
    public void testCreateMarkersFromMessageList_deleteMarkers() throws Exception {
        AMarker marker1 = mock(AMarker.class);
        when(marker1.getAttribute(IMarker.MESSAGE)).thenReturn("text1");
        when(marker1.getAttribute(IMarker.SEVERITY)).thenReturn(IMarker.SEVERITY_ERROR);
        AMarker marker2 = mock(AMarker.class);
        when(marker2.getAttribute(IMarker.MESSAGE)).thenReturn("text2");
        when(marker2.getAttribute(IMarker.SEVERITY)).thenReturn(IMarker.SEVERITY_WARNING);
        AResource resource = mock(AResource.class);
        when(resource.findMarkers(anyString(), anyBoolean(), any(AResourceTreeTraversalDepth.class)))
                .thenReturn(new LinkedHashSet<>(List.of(marker1, marker2)));
        MessageList list = new MessageList();
        list.add(new Message("", "text1", Message.ERROR));
        IpsBuilder ipsBuilder = new EclipseIpsBuilder().getIpsBuilder();

        ipsBuilder.createMarkersFromMessageList(resource, list, "");

        verify(marker2).delete();
        verify(resource).findMarkers(anyString(), anyBoolean(), any(AResourceTreeTraversalDepth.class));
        verifyNoMoreInteractions(resource);
    }

    @Test
    public void testCreateMarkersFromMessageList_duplicatedMarkers() throws Exception {
        AMarker marker1 = mock(AMarker.class);
        when(marker1.getAttribute(IMarker.MESSAGE)).thenReturn("text1");
        when(marker1.getAttribute(IMarker.SEVERITY)).thenReturn(IMarker.SEVERITY_ERROR);
        AMarker marker2 = mock(AMarker.class);
        when(marker2.getAttribute(IMarker.MESSAGE)).thenReturn("text1");
        when(marker2.getAttribute(IMarker.SEVERITY)).thenReturn(IMarker.SEVERITY_ERROR);
        AResource resource = mock(AResource.class);
        when(resource.findMarkers(anyString(), anyBoolean(), any(AResourceTreeTraversalDepth.class)))
                .thenReturn(new LinkedHashSet<>(List.of(marker1, marker2)));
        MessageList list = new MessageList();
        list.add(new Message("", "text1", Message.ERROR));
        list.add(new Message("", "text1", Message.ERROR));
        IpsBuilder ipsBuilder = new EclipseIpsBuilder().getIpsBuilder();

        ipsBuilder.createMarkersFromMessageList(resource, list, "");

        verify(resource).findMarkers(anyString(), anyBoolean(), any(AResourceTreeTraversalDepth.class));
        verifyNoMoreInteractions(resource);
    }

    @Test
    public void testCreateMarkersForIpsProjectProperties() {
        IpsBuilder ipsBuilder = new EclipseIpsBuilder().getIpsBuilder();

        MessageList list = new MessageList();

        IIpsObjectPath ipsObjectPath = ipsProject.getIpsObjectPath();
        ipsObjectPath.setUsingManifest(false);
        ipsProject.setIpsObjectPath(ipsObjectPath);

        list.newError("1111", "1111");
        list.newError("3333", "3333", ipsObjectPath, "3333");
        list.newError("4444", "4444", ipsObjectPath.getEntries()[0], "4444");
        list.newError("5555", "5555", ipsObjectPath.getEntries()[0], "5555");
        list.newError("2222", "2222");

        AFile propertiesFile = spy(ipsProject.getIpsProjectPropertiesFile());

        IIpsProject spiedIpsProject = spy(ipsProject);
        doReturn(propertiesFile).when(spiedIpsProject).getIpsProjectPropertiesFile();

        ipsBuilder.createMarkersForIpsProjectProperties(list, spiedIpsProject);

        verify(propertiesFile, times(5)).createMarker(IpsBuilder.PROBLEM_MARKER);
    }

    @Test
    public void testCreateMarkersForIpsProjectPropertiesUsingManifest() {
        IpsBuilder ipsBuilder = new EclipseIpsBuilder().getIpsBuilder();

        IIpsObjectPath ipsObjectPath = ipsProject.getIpsObjectPath();
        ipsObjectPath.setUsingManifest(true);
        ipsProject.setIpsObjectPath(ipsObjectPath);

        AFile propertiesFile = spy(ipsProject.getIpsProjectPropertiesFile());
        AFile manifestFile = mock(AFile.class);
        when(manifestFile.findMarkers(IpsBuilder.PROBLEM_MARKER, true, AResourceTreeTraversalDepth.RESOURCE_ONLY))
                .thenReturn(Collections.emptySet());

        AMarker marker2222 = mock(AMarker.class);
        AMarker marker3333 = mock(AMarker.class);
        AMarker marker4444 = mock(AMarker.class);

        when(manifestFile.createMarker(IpsBuilder.PROBLEM_MARKER)).thenReturn(marker2222, marker3333, marker4444);
        when(manifestFile.exists()).thenReturn(true);

        IIpsProject spiedIpsProject = spy(ipsProject);
        doReturn(propertiesFile).when(spiedIpsProject).getIpsProjectPropertiesFile();
        doReturn(ipsObjectPath).when(spiedIpsProject).getIpsObjectPath();

        AProject spiedProject = spy(ipsProject.getProject());
        doReturn(spiedProject).when(spiedIpsProject).getProject();
        doReturn(manifestFile).when(spiedProject).getFile(IpsBundleManifest.MANIFEST_NAME);
        // Need to mock the getReadOnlyProperty method because the spiedIpsProject would otherwise
        // ask the ipsModel for the properties and that would fail because of
        // spiedIpsProject.hashCode() != ipsProject.hashCode()
        // https://code.google.com/p/mockito/issues/detail?id=241
        IIpsProjectProperties properties = ipsProject.getReadOnlyProperties();
        doReturn(properties).when(spiedIpsProject).getReadOnlyProperties();

        MessageList messages = new MessageList();

        messages.newError("1111", "1111");
        messages.newError("2222", "2222", ipsObjectPath, "2222");
        messages.newError("3333", "3333", ipsObjectPath.getEntries()[0], "3333");
        messages.newError("4444", "4444", ipsObjectPath.getEntries()[0], "4444");
        messages.newError("5555", "5555");

        ipsBuilder.createMarkersForIpsProjectProperties(messages, spiedIpsProject);

        verify(propertiesFile, times(2)).createMarker(IpsBuilder.PROBLEM_MARKER);
        verify(manifestFile, times(3)).createMarker(IpsBuilder.PROBLEM_MARKER);

        verify(marker2222).setAttributes(new String[] { IMarker.MESSAGE, IMarker.SEVERITY },
                new Object[] { "2222", Integer.valueOf(IMarker.SEVERITY_ERROR) });
        verify(marker3333).setAttributes(new String[] { IMarker.MESSAGE, IMarker.SEVERITY },
                new Object[] { "3333", Integer.valueOf(IMarker.SEVERITY_ERROR) });
        verify(marker4444).setAttributes(new String[] { IMarker.MESSAGE, IMarker.SEVERITY },
                new Object[] { "4444", Integer.valueOf(IMarker.SEVERITY_ERROR) });
    }
}
