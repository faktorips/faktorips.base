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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.abstraction.Abstractions;
import org.faktorips.devtools.abstraction.eclipse.internal.EclipseImplementation;
import org.faktorips.devtools.model.internal.ipsproject.IpsProject;
import org.faktorips.devtools.model.internal.ipsproject.jdtcontainer.IpsContainer4JdtClasspathContainerType;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPathContainer;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPathContainerType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.plugin.IpsLog;
import org.junit.Test;
import org.junit.experimental.categories.Category;

public class IpsObjectPathContainerFactoryTest extends AbstractIpsPluginTest {

    private IpsObjectPathContainerFactory factory = new IpsObjectPathContainerFactory();

    @Test
    public void testRegisterContainerType() {
        IIpsObjectPathContainerType type = newType("JDT");

        factory.registerContainerType(type);

        assertEquals(1, factory.getNumOfRegisteredTypes());
        assertEquals(type, factory.getContainerType("JDT"));
        assertTrue(factory.isRegistered(type));
    }

    @Test
    public void tetRegisterContainerType_ignoreRegisterOfSameType() {
        IIpsObjectPathContainerType type = newType("JDT");

        factory.registerContainerType(type);
        factory.registerContainerType(type);

        assertEquals(1, factory.getNumOfRegisteredTypes());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRegisterContainerType_throwIllegalArgumentExWhenDifferentTypeWithSameIdIsRegistered() {
        IIpsObjectPathContainerType type1 = newType("MAVEN");
        IIpsObjectPathContainerType type2 = newType("MAVEN");

        factory.registerContainerType(type1);
        factory.registerContainerType(type2);
    }

    @Test
    public void testUnregisterContainerType_unregisterARegisteredType() {
        IIpsObjectPathContainerType type = registerNewType("MAVEN");

        factory.unregisterContainerType(type);

        assertNull(factory.getContainerType("MAVEN"));
        assertFalse(factory.isRegistered(type));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnregisterContainerType_throwIllegalArgumentWhenTryingToUnregisterATypeNotBeenRegistered() {
        IIpsObjectPathContainerType type = newType("MAVEN");

        factory.unregisterContainerType(type);
    }

    @Test
    public void testGetContainerTYpe_returnRegisteredTypeById() {
        IIpsObjectPathContainerType type1 = registerNewType("MAVEN");
        IIpsObjectPathContainerType type2 = registerNewType("JDT");

        IIpsObjectPathContainerType containerType = factory.getContainerType("MAVEN");
        IIpsObjectPathContainerType containerType2 = factory.getContainerType("JDT");

        assertEquals(type1, containerType);
        assertEquals(type2, containerType2);
    }

    @Test
    public void testRegisterNewType_returnNullIfNoTypeIsFoundForTheGivenId() {
        assertNull(factory.getContainerType("JDT"));

        registerNewType("MAVEN");

        assertNull(factory.getContainerType("JDT"));
    }

    @Test
    public void testNewContainer_returnNewContainerIfTypeIsRegisteredForTheGivenID() {
        IIpsObjectPathContainer container = mock(IIpsObjectPathContainer.class);
        IIpsObjectPathContainerType type = registerNewType("MAVEN");
        when(type.newContainer(any(IIpsProject.class), anyString())).thenReturn(container);

        IIpsObjectPathContainer newContainer = factory.newContainer(new IpsProject(), "MAVEN", "optionalPath");

        assertEquals(container, newContainer);
    }

    @Test
    public void testRegisterContainerType_returnNullIfNoTypeIsRegisteredForTheGivenID() {
        IIpsObjectPathContainer container = mock(IIpsObjectPathContainer.class);
        IIpsObjectPathContainerType type = registerNewType("MAVEN");
        when(type.newContainer(any(IIpsProject.class), anyString())).thenReturn(container);

        factory.registerContainerType(type);

        assertNull(factory.newContainer(new IpsProject(), "SOMETHING", "optionalPath"));
    }

    @Category(EclipseImplementation.class)
    @Test
    public void testGetContainerType_createNewFactoryBasedOnExtensions() {
        if (Abstractions.isEclipseRunning()) {
            factory = IpsObjectPathContainerFactory.newFactoryBasedOnExtensions();

            // this is a dependency to the fact, that the container for JDT containers is defined in
            // plugin.xml, (but better this test then no test)
            assertNotNull(factory.getContainerType(IpsContainer4JdtClasspathContainerType.ID));
        }
    }

    @Test
    public void testNewFactory_CreateNewFactoryBasedOnGivenTypes() {
        List<IIpsObjectPathContainerType> types = new ArrayList<>();
        IIpsObjectPathContainerType type1 = newType("Type1");
        IIpsObjectPathContainerType type2 = newType("Type2");
        types.add(type1);
        types.add(type2);

        factory = IpsObjectPathContainerFactory.newFactory(types);

        assertEquals(2, factory.getNumOfRegisteredTypes());
        assertEquals(type1, factory.getContainerType("Type1"));
        assertEquals(type2, factory.getContainerType("Type2"));
    }

    @Test
    public void testNewFactory_LogDuplicateTypeIdsDuringFactoryCreation() {
        IpsLog.setSuppressLoggingDuringTest(true);
        List<IIpsObjectPathContainerType> types = new ArrayList<>();
        IIpsObjectPathContainerType type1 = newType("Type1");
        IIpsObjectPathContainerType type2 = newType("Type2");
        IIpsObjectPathContainerType type3 = newType("Type1");
        types.add(type1);
        types.add(type2);
        types.add(type3);

        factory = IpsObjectPathContainerFactory.newFactory(types);

        assertEquals(2, factory.getNumOfRegisteredTypes());
        assertTrue(factory.isRegistered(type1));
        assertTrue(factory.isRegistered(type2));
        assertFalse(factory.isRegistered(type3));
    }

    @Test
    public void testNewFactory_LogTypeWithNullIdsDuringFactoryCreation() {
        IpsLog.setSuppressLoggingDuringTest(true);
        List<IIpsObjectPathContainerType> types = new ArrayList<>();
        IIpsObjectPathContainerType type = newType(null);
        types.add(type);

        factory = IpsObjectPathContainerFactory.newFactory(types);

        assertEquals(0, factory.getNumOfRegisteredTypes());
        assertFalse(factory.isRegistered(type));
    }

    private IIpsObjectPathContainerType registerNewType(String id) {
        IIpsObjectPathContainerType newType = newType(id);
        factory.registerContainerType(newType);
        return newType;
    }

    private IIpsObjectPathContainerType newType(String id) {
        IIpsObjectPathContainerType type = mock(IIpsObjectPathContainerType.class);
        when(type.getId()).thenReturn(id);
        return type;
    }

}
