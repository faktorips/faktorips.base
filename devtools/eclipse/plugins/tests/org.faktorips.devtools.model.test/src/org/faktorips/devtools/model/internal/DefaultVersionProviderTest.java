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
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.faktorips.devtools.model.IVersion;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.model.ipsproject.IVersionFormat;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DefaultVersionProviderTest {

    private static final String VERSION_STRING = "1.2.3.test";

    @Mock
    private IIpsProject ipsProject;

    @Mock
    private IIpsProjectProperties properties;

    @Mock
    private IVersionFormat versionFormat;

    @InjectMocks
    private DefaultVersionProvider defaultVersionProvider;

    @Before
    public void initDefaultVersionProvider() {
        when(ipsProject.getReadOnlyProperties()).thenReturn(properties);
        when(ipsProject.getProperties()).thenReturn(properties);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetVersion_illegalVersion() throws Exception {
        when(versionFormat.isCorrectVersionFormat(VERSION_STRING)).thenReturn(false);

        defaultVersionProvider.getVersion(VERSION_STRING);
    }

    @Test
    public void testGetProjectlVersion() throws Exception {
        when(versionFormat.isCorrectVersionFormat(VERSION_STRING)).thenReturn(true);
        when(properties.getVersion()).thenReturn(VERSION_STRING);

        IVersion<DefaultVersion> projectlVersion = defaultVersionProvider.getProjectVersion();

        assertEquals(VERSION_STRING, projectlVersion.asString());
    }

    @Test
    public void testSetProjectVersion() throws Exception {
        when(versionFormat.isCorrectVersionFormat(VERSION_STRING)).thenReturn(true);
        IVersion<DefaultVersion> version = defaultVersionProvider.getVersion(VERSION_STRING);

        defaultVersionProvider.setProjectVersion(version);

        verify(properties).setVersion(VERSION_STRING);
        verify(ipsProject).setProperties(properties);
    }

    @Test
    public void testIsCorrectVersionFormat_true() throws Exception {
        when(versionFormat.isCorrectVersionFormat(VERSION_STRING)).thenReturn(true);

        boolean result = defaultVersionProvider.isCorrectVersionFormat(VERSION_STRING);

        assertTrue(result);
    }

    @Test
    public void testIsCorrectVersionFormat_false() throws Exception {

        boolean result = defaultVersionProvider.isCorrectVersionFormat(VERSION_STRING);

        assertFalse(result);
    }

    @Test
    public void testGetVersionFormat() throws Exception {
        String expectedFormat = "ASDF";
        when(versionFormat.getVersionFormat()).thenReturn(expectedFormat);

        String resultVersionFormat = defaultVersionProvider.getVersionFormat();

        assertEquals(expectedFormat, resultVersionFormat);
    }

}
