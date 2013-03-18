/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.internal.model.ipsproject.jarbundle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.eclipse.core.runtime.Path;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsObjectPath;
import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.util.message.MessageList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class IpsJarBundleEntryTest {

    @Mock
    private IpsObjectPath ipsObjectPath;

    @Mock
    private IpsJarBundle ipsJarBundle;

    private IpsJarBundleEntry ipsJarBundleEntry;

    @Before
    public void createIpsJarBundleEntry() throws Exception {
        ipsJarBundleEntry = new IpsJarBundleEntry(ipsObjectPath);
        ipsJarBundleEntry.setIpsJarBundle(ipsJarBundle);
    }

    @Test
    public void testValidate_valid() throws Exception {
        when(ipsJarBundle.isValid()).thenReturn(true);

        MessageList messages = ipsJarBundleEntry.validate();

        assertNull(messages.getMessageByCode(IpsJarBundleEntry.MSGCODE_MISSING_JARBUNDLE));
    }

    @Test
    public void testValidate_invalid() throws Exception {
        when(ipsJarBundle.isValid()).thenReturn(false);

        MessageList messages = ipsJarBundleEntry.validate();

        assertNotNull(messages.getMessageByCode(IpsJarBundleEntry.MSGCODE_MISSING_JARBUNDLE));
    }

    @Test
    public void testValidate_invalid_null() throws Exception {
        ipsJarBundleEntry.setIpsJarBundle(null);

        MessageList messages = ipsJarBundleEntry.validate();

        assertNotNull(messages.getMessageByCode(IpsJarBundleEntry.MSGCODE_MISSING_JARBUNDLE));
    }

    @Test
    public void testGetRessourceAsStream() throws Exception {
        ipsJarBundleEntry.getRessourceAsStream("testAnyPath");

        verify(ipsJarBundle).getResourceAsStream("testAnyPath");
    }

    @Test
    public void testGetIpsPackageFragmentRootName() throws Exception {
        when(ipsJarBundle.getLocation()).thenReturn(new Path("any/where/test.jar"));

        String rootName = ipsJarBundleEntry.getIpsPackageFragmentRootName();

        assertEquals("test.jar", rootName);
    }

    @Test
    public void testGetIpsPackageFragmentRoot() throws Exception {
        IIpsPackageFragmentRoot root = mock(IIpsPackageFragmentRoot.class);
        when(ipsJarBundle.getRoot()).thenReturn(root);

        IIpsPackageFragmentRoot actualRoot = ipsJarBundleEntry.getIpsPackageFragmentRoot();

        assertEquals(root, actualRoot);
    }

    @Test
    public void testExists_existing() throws Exception {
        QualifiedNameType qnt = mock(QualifiedNameType.class);
        when(ipsJarBundle.contains(qnt)).thenReturn(true);

        boolean exists = ipsJarBundleEntry.exists(qnt);

        assertTrue(exists);
    }

    @Test
    public void testExists_notExisting() throws Exception {
        QualifiedNameType qnt = mock(QualifiedNameType.class);

        boolean exists = ipsJarBundleEntry.exists(qnt);

        assertFalse(exists);
    }

}
