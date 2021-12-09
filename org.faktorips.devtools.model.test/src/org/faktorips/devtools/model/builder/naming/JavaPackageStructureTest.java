/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.builder.naming;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import org.faktorips.devtools.model.builder.DefaultBuilderSet;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilder;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class JavaPackageStructureTest {

    @Mock
    private IIpsArtefactBuilder builder;

    @Mock
    private DefaultBuilderSet builderSet;

    @Mock
    private IIpsSrcFile ipsSrcFile;

    private JavaPackageStructure packageStructure;

    @Mock
    private IIpsPackageFragment packageFragment;

    @Before
    public void setUp() throws CoreRuntimeException {
        MockitoAnnotations.initMocks(this);
        packageStructure = new JavaPackageStructure();

        when(ipsSrcFile.getBasePackageNameForMergableArtefacts()).thenReturn("mergable");
        when(ipsSrcFile.getBasePackageNameForDerivedArtefacts()).thenReturn("derived");
        when(ipsSrcFile.getIpsPackageFragment()).thenReturn(packageFragment);
        when(packageFragment.getName()).thenReturn("ubx");
    }

    @Test
    public void testGetPackageMergablePublished() throws Exception {
        String package1 = packageStructure.getPackageName(ipsSrcFile, false, true);
        assertEquals("mergable.ubx", package1);
    }

    @Test
    public void testGetPackageDerivedPublished() throws Exception {
        String package1 = packageStructure.getPackageName(ipsSrcFile, false, false);
        assertEquals("derived.ubx", package1);
    }

    @Test
    public void testGetPackageMergableInternal() throws Exception {
        String package1 = packageStructure.getPackageName(ipsSrcFile, true, true);
        assertEquals("mergable.internal.ubx", package1);
    }

    @Test
    public void testGetPackageDerivedInternal() throws Exception {
        when(builder.getBuilderSet()).thenReturn(builderSet);
        doReturn(true).when(builder).isBuildingInternalArtifacts();
        when(builder.buildsDerivedArtefacts()).thenReturn(true);

        String package1 = packageStructure.getPackageName(ipsSrcFile, true, false);
        assertEquals("derived.internal.ubx", package1);
    }

}
