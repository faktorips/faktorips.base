/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.ipsproject;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsProjectNamingConventions;
import org.faktorips.devtools.model.ipsproject.IIpsStorage;
import org.faktorips.runtime.MessageList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
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
    public void testGetSubPackageName_DefaultPackage() throws Exception {
        when(abstractIpsPackageFragment.getName()).thenReturn(IIpsPackageFragment.NAME_OF_THE_DEFAULT_PACKAGE);

        String subPackageName = abstractIpsPackageFragment.getSubPackageName(MY_SUB_PACKAGE);

        assertEquals(MY_SUB_PACKAGE, subPackageName);
    }

    @Test
    public void testGetSubPackageName_NormalPackage() throws Exception {
        when(abstractIpsPackageFragment.getName()).thenReturn(MY_PACKAGE);

        String subPackageName = abstractIpsPackageFragment.getSubPackageName(MY_SUB_PACKAGE);

        assertEquals(MY_PACKAGE + IIpsPackageFragment.SEPARATOR + MY_SUB_PACKAGE, subPackageName);
    }

    @Test
    public void testGetSubPackage_DefaultPackage() throws Exception {
        when(abstractIpsPackageFragment.getName()).thenReturn(IIpsPackageFragment.NAME_OF_THE_DEFAULT_PACKAGE);
        IIpsPackageFragment subPackage = mock(IIpsPackageFragment.class);
        when(ipsPackageFragmentRoot.getIpsPackageFragment(MY_SUB_PACKAGE)).thenReturn(subPackage);

        IIpsPackageFragment resultPackage = abstractIpsPackageFragment.getSubPackage(MY_SUB_PACKAGE);

        assertEquals(subPackage, resultPackage);
    }

    @Test
    public void testGetSubPackage_NormalPackage() throws Exception {
        when(abstractIpsPackageFragment.getName()).thenReturn(MY_PACKAGE);
        IIpsPackageFragment subPackage = mock(IIpsPackageFragment.class);
        when(ipsPackageFragmentRoot.getIpsPackageFragment(MY_PACKAGE + IIpsPackageFragment.SEPARATOR + MY_SUB_PACKAGE))
                .thenReturn(subPackage);

        IIpsPackageFragment resultPackage = abstractIpsPackageFragment.getSubPackage(MY_SUB_PACKAGE);

        assertEquals(subPackage, resultPackage);
    }

    @Test
    public void testGetParentIpsPackageFragment_DefaultPackage() {
        when(abstractIpsPackageFragment.getName()).thenReturn(IIpsPackageFragment.NAME_OF_THE_DEFAULT_PACKAGE);

        IIpsPackageFragment parentIpsPackageFragment = abstractIpsPackageFragment.getParentIpsPackageFragment();

        assertThat(parentIpsPackageFragment, is(nullValue()));
    }

    @Test
    public void testGetParentIpsPackageFragment_FirstLevelPackage() {
        when(abstractIpsPackageFragment.getName()).thenReturn("first");
        IIpsPackageFragment defaultPackage = mock(IIpsPackageFragment.class);
        when(ipsPackageFragmentRoot.getDefaultIpsPackageFragment()).thenReturn(defaultPackage);

        IIpsPackageFragment parentIpsPackageFragment = abstractIpsPackageFragment.getParentIpsPackageFragment();

        assertThat(parentIpsPackageFragment, is(defaultPackage));
    }

    @Test
    public void testGetParentIpsPackageFragment_SecondLevelPackage() {
        when(ipsPackageFragmentRoot.getIpsPackageFragment(anyString())).then(
                invocation -> new IpsPackageFragment(ipsPackageFragmentRoot, (String)invocation.getArguments()[0]));
        abstractIpsPackageFragment = new IpsPackageFragment(ipsPackageFragmentRoot, "first.second");

        IIpsPackageFragment parentIpsPackageFragment = abstractIpsPackageFragment.getParentIpsPackageFragment();

        assertThat(parentIpsPackageFragment.getParent(), is((IIpsElement)ipsPackageFragmentRoot));
        assertThat(parentIpsPackageFragment.getName(), is("first"));
    }

    @Test
    public void testGetParentIpsPackageFragment_SecondLevelPackage_SameType() {
        IIpsProjectNamingConventions namingConventions = mock(IIpsProjectNamingConventions.class);
        when(namingConventions.validateIpsPackageName(anyString())).thenReturn(new MessageList());
        IIpsProject ipsProject = mock(IIpsProject.class);
        when(ipsProject.getNamingConventions()).thenReturn(namingConventions);
        ipsPackageFragmentRoot = new LibraryIpsPackageFragmentRoot(ipsProject, mock(IIpsStorage.class));
        abstractIpsPackageFragment = new LibraryIpsPackageFragment(
                (LibraryIpsPackageFragmentRoot)ipsPackageFragmentRoot, "first.second");

        IIpsPackageFragment parentIpsPackageFragment = abstractIpsPackageFragment.getParentIpsPackageFragment();

        assertThat(parentIpsPackageFragment.getParent(), is((IIpsElement)ipsPackageFragmentRoot));
        assertThat(parentIpsPackageFragment.getName(), is("first"));
        assertThat(parentIpsPackageFragment, is(instanceOf(LibraryIpsPackageFragment.class)));
    }

}
