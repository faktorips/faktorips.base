/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.internal.model.ipsproject.AbstractIpsObjectPathContainer;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsProject;
import org.faktorips.devtools.core.model.IIpsModel;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPathContainer;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPathContainerType;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPathEntry;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.util.message.MessageList;
import org.junit.Test;

public class IpsProjectDataTest {

    private IpsObjectPathContainerFactory factory = new IpsObjectPathContainerFactory();
    private IIpsProject ipsProject = new IpsProject(mock(IIpsModel.class), "TestProject");
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
        when(containerType.newContainer(ipsProject, "path")).thenReturn(
                new MyContainer(containerType, ipsProject, "path"));

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

    class MyContainer extends AbstractIpsObjectPathContainer {

        public MyContainer(IIpsObjectPathContainerType containerType, IIpsProject ipsProject, String optionalPath) {
            super(containerType, ipsProject, optionalPath);
        }

        @Override
        public List<IIpsObjectPathEntry> resolveEntries() throws CoreException {
            return null;
        }

        @Override
        public MessageList validate() throws CoreException {
            return null;
        }

    }
}
