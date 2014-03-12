/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.ui.editors;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.faktorips.devtools.core.internal.model.ipsobject.IVersionControlledElement;
import org.faktorips.devtools.core.model.IVersion;
import org.faktorips.devtools.core.model.IVersionProvider;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.ui.editors.VersionsComposite.VersionPMO;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class VersionPMOTest {

    @Mock
    private IVersionControlledElement part;

    @Mock
    private IVersion<?> version;

    @Mock
    private IIpsProject ipsProject;

    @Mock
    private IVersionProvider<?> provider;

    @Mock
    private IVersion<?> newVersion;

    private VersionPMO pmo;

    private static final String VERSION_STRING = "version";

    private static final String NEW_VERSION_STRING = "newVersion";

    @Before
    public void setUp() {
        when(version.asString()).thenReturn(VERSION_STRING);
        doReturn(version).when(part).getSinceVersion();
        pmo = new VersionPMO(part);
    }

    @Test
    public void testGetSinceVersion() {
        pmo.getSinceVersion();

        verify(part).getSinceVersion();
    }

    @Test
    public void testSetSinceVersion() {
        when(part.getIpsProject()).thenReturn(ipsProject);
        doReturn(provider).when(ipsProject).getVersionProvider();
        doReturn(newVersion).when(provider).getVersion(NEW_VERSION_STRING);

        pmo.setSinceVersion(NEW_VERSION_STRING);

        verify(part).setSinceVersion(newVersion);
    }
}
