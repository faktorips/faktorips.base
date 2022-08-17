/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.persistence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.apache.commons.lang.StringUtils;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.devtools.model.pctype.persistence.IPersistentAttributeInfo;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EclipseLink25PersistenceProviderTest {

    private EclipseLink25PersistenceProvider provider;

    @Mock
    private IPersistentAttributeInfo persistentAttributeInfo;

    @Before
    public void setUp() {
        provider = new EclipseLink25PersistenceProvider();
    }

    @Test
    public void testIsSupportingIndex() throws Exception {
        assertTrue(provider.isSupportingIndex());
    }

    @Test
    public void testGetIndexAnnotations_IndexNameEmpty() throws Exception {
        when(persistentAttributeInfo.isIndexNameDefined()).thenReturn(false);
        JavaCodeFragment indexAnnotations = provider.getIndexAnnotations(persistentAttributeInfo);

        assertNotNull(indexAnnotations);
        assertTrue(StringUtils.isEmpty(indexAnnotations.getSourcecode()));
    }

    @Test
    public void testGetIndexAnnotations_WithIndexName() throws Exception {
        when(persistentAttributeInfo.isIndexNameDefined()).thenReturn(true);
        when(persistentAttributeInfo.getIndexName()).thenReturn("INDEX_NAME");
        JavaCodeFragment indexAnnotations = provider.getIndexAnnotations(persistentAttributeInfo);

        assertNotNull(indexAnnotations);
        assertEquals("@Index(name=\"INDEX_NAME\")" + System.lineSeparator(),
                indexAnnotations.getSourcecode());
    }
}
