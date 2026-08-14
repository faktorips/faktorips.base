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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.devtools.model.pctype.persistence.IPersistentAttributeInfo;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class EclipseLink25PersistenceProviderTest {

    private EclipseLink25PersistenceProvider provider;

    @Mock
    private IPersistentAttributeInfo persistentAttributeInfo;

    @BeforeEach
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
        assertTrue(IpsStringUtils.isEmpty(indexAnnotations.getSourcecode()));
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
