/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.ipsproject.search;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.io.InputStream;

import org.faktorips.devtools.model.internal.ipsproject.IpsProjectRefEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ResourceSearchTest {

    private static final String MY_PATH = "myPath";

    @Mock
    private ResourceSearch resourceSearch;

    @Mock
    private IpsProjectRefEntry srcFolderEntry;

    @Mock
    private InputStream inputStream;

    @BeforeEach
    public void setUp() {
        resourceSearch = new ResourceSearch(MY_PATH);
    }

    @Test
    public void testProcessEntry_containsResource() {
        when(srcFolderEntry.containsResource(MY_PATH)).thenReturn(true);
        when(srcFolderEntry.getResourceAsStream(MY_PATH)).thenReturn(inputStream);
        resourceSearch.processEntry(srcFolderEntry);

        assertTrue(resourceSearch.containsResource());
        assertSame(inputStream, resourceSearch.getResourceAsStream());

    }

    @Test
    public void testProcessEntry_noResource() {
        when(srcFolderEntry.containsResource(MY_PATH)).thenReturn(false);
        resourceSearch.processEntry(srcFolderEntry);

        assertFalse(resourceSearch.containsResource());
        assertNull(resourceSearch.getResourceAsStream());

    }

}
