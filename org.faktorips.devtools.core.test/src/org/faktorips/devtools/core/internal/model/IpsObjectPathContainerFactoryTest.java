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
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsContainer4JdtClasspathContainerType;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPathContainer;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPathContainerType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.junit.Test;

public class IpsObjectPathContainerFactoryTest extends AbstractIpsPluginTest {

    private IpsObjectPathContainerFactory factory = new IpsObjectPathContainerFactory();

    @Test
    public void shouldRegisterNewType() {
        IIpsObjectPathContainerType type = newType("JDT");

        factory.registerContainerType(type);
        assertEquals(type, factory.getContainerType("JDT"));
        assertTrue(factory.isRegistered(type));
    }

    @Test
    public void shouldIgnoreRegisterOfSameType() {
        IIpsObjectPathContainerType type = newType("JDT");

        factory.registerContainerType(type);
        factory.registerContainerType(type);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExWhenDifferentTypeWithSameIdIsRegistered() {
        IIpsObjectPathContainerType type1 = newType("MAVEN");
        IIpsObjectPathContainerType type2 = newType("MAVEN");

        factory.registerContainerType(type1);
        factory.registerContainerType(type2);
    }

    @Test
    public void shouldUnregisterARegisteredType() {
        IIpsObjectPathContainerType type = registerNewType("MAVEN");

        factory.unregisterContainerType(type);
        assertNull(factory.getContainerType("MAVEN"));
        assertFalse(factory.isRegistered(type));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentWhenTryingToUnregisterATypeNotBeenRegistered() {
        IIpsObjectPathContainerType type = newType("MAVEN");
        factory.unregisterContainerType(type);
    }

    @Test
    public void shouldReturnRegisteredTypeById() {
        IIpsObjectPathContainerType type1 = registerNewType("MAVEN");
        IIpsObjectPathContainerType type2 = registerNewType("JDT");

        assertEquals(type1, factory.getContainerType("MAVEN"));
        assertEquals(type2, factory.getContainerType("JDT"));
    }

    @Test
    public void shouldReturnNullIfNoTypeIsFoundForTheGivenId() {
        assertNull(factory.getContainerType("JDT"));

        registerNewType("MAVEN");
        assertNull(factory.getContainerType("JDT"));
    }

    @Test
    public void shouldReturnNewContainerIfTypeIsRegisteredForTheGivenID() {
        IIpsObjectPathContainer container = mock(IIpsObjectPathContainer.class);
        IIpsObjectPathContainerType type = registerNewType("MAVEN");
        when(type.newContainer(any(IIpsProject.class), anyString())).thenReturn(container);

        factory.newContainer(new IpsProject(), "MAVEN", "optionalPath");
    }

    @Test
    public void shouldReturnNullIfNoTypeIsRegisteredForTheGivenID() {
        IIpsObjectPathContainer container = mock(IIpsObjectPathContainer.class);
        IIpsObjectPathContainerType type = registerNewType("MAVEN");
        when(type.newContainer(any(IIpsProject.class), anyString())).thenReturn(container);

        factory.registerContainerType(type);
        assertNull(factory.newContainer(new IpsProject(), "SOMETHING", "optionalPath"));
    }

    @Test
    public void shouldCreateNewFactoryBasedOnExtensions() {
        factory = IpsObjectPathContainerFactory.newFactoryBasedOnExtensions();

        // this is a dependency to the fact, that the container for JDT containers is defined in
        // plugin.xml, (but better this test then no test)
        assertNotNull(factory.getContainerType(IpsContainer4JdtClasspathContainerType.ID));
    }

    @Test
    public void shouldCreateNewFactoryBasedOnGivenTypes() {
        List<IIpsObjectPathContainerType> types = new ArrayList<IIpsObjectPathContainerType>();
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
    public void shouldLogDuplicateTypeIdsDuringFactoryCreation() {
        IpsPlugin.getDefault().setSuppressLoggingDuringTest(true);
        List<IIpsObjectPathContainerType> types = new ArrayList<IIpsObjectPathContainerType>();
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
    public void shouldLogTypeWithNullIdsDuringFactoryCreation() {
        IpsPlugin.getDefault().setSuppressLoggingDuringTest(true);
        List<IIpsObjectPathContainerType> types = new ArrayList<IIpsObjectPathContainerType>();
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
