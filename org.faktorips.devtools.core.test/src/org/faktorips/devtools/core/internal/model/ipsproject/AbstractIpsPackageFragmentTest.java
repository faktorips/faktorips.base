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

package org.faktorips.devtools.core.internal.model.ipsproject;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AbstractIpsPackageFragmentTest {

    private static final String MY_PACKAGE = "myPackage";

    private static final String MY_SUB_PACKAGE = "mySubPackage";

    @Mock
    private IIpsPackageFragmentRoot ipsPackageFragmentRoot;

    private AbstractIpsPackageFragment abstractIpsPackageFragment;

    @Before
    public void createAbstractIpsPackageFragment() throws Exception {
        abstractIpsPackageFragment = mock(AbstractIpsPackageFragment.class, CALLS_REAL_METHODS);
        when(abstractIpsPackageFragment.getRoot()).thenReturn(ipsPackageFragmentRoot);
    }

    @Test
    public void testGetSubPackageName_defaultPackage() throws Exception {
        when(abstractIpsPackageFragment.getName()).thenReturn(IIpsPackageFragment.NAME_OF_THE_DEFAULT_PACKAGE);

        String subPackageName = abstractIpsPackageFragment.getSubPackageName(MY_SUB_PACKAGE);

        assertEquals(MY_SUB_PACKAGE, subPackageName);
    }

    @Test
    public void testGetSubPackageName_notmalPackage() throws Exception {
        when(abstractIpsPackageFragment.getName()).thenReturn(MY_PACKAGE);

        String subPackageName = abstractIpsPackageFragment.getSubPackageName(MY_SUB_PACKAGE);

        assertEquals(MY_PACKAGE + IIpsPackageFragment.SEPARATOR + MY_SUB_PACKAGE, subPackageName);
    }

    @Test
    public void testGetSubPackage_defaultPackage() throws Exception {
        when(abstractIpsPackageFragment.getName()).thenReturn(IIpsPackageFragment.NAME_OF_THE_DEFAULT_PACKAGE);
        IIpsPackageFragment subPackage = mock(IIpsPackageFragment.class);
        when(ipsPackageFragmentRoot.getIpsPackageFragment(MY_SUB_PACKAGE)).thenReturn(subPackage);

        IIpsPackageFragment resultPackage = abstractIpsPackageFragment.getSubPackage(MY_SUB_PACKAGE);

        assertEquals(subPackage, resultPackage);
    }

    @Test
    public void testGetSubPackage_normalPackage() throws Exception {
        when(abstractIpsPackageFragment.getName()).thenReturn(MY_PACKAGE);
        IIpsPackageFragment subPackage = mock(IIpsPackageFragment.class);
        when(ipsPackageFragmentRoot.getIpsPackageFragment(MY_PACKAGE + IIpsPackageFragment.SEPARATOR + MY_SUB_PACKAGE))
                .thenReturn(subPackage);

        IIpsPackageFragment resultPackage = abstractIpsPackageFragment.getSubPackage(MY_SUB_PACKAGE);

        assertEquals(subPackage, resultPackage);
    }

}
