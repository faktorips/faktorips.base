/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.builder.java.annotations;

import static org.faktorips.abstracttest.MockUtil.createMocks;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.model.builder.xmodel.AbstractGeneratorModelNode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.MockitoSession;

public class LabelAndDescriptionAnnGenTest extends AbstractIpsPluginTest {

    private LabelAndDescriptionAnnGen annGen = new LabelAndDescriptionAnnGen();

    private MockitoSession mockito;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        mockito = createMocks(this);
    }

    @Override
    @AfterEach
    public void tearDown() throws Exception {
        try {
            super.tearDown();
        } finally {
            mockito.finishMocking();
        }
    }

    @Test
    public void testCreateAnnotation() {

        AbstractGeneratorModelNode modelNode = Mockito.mock(AbstractGeneratorModelNode.class);

        when(modelNode.getIpsProject()).thenReturn(newIpsProject());
        when(modelNode.getDocumentationResourceBundleBaseName()).thenReturn("baseBundleName");

        assertEquals(
                "@IpsDocumented(bundleName = \"baseBundleName\", defaultLocale = \"de\")"
                        + System.lineSeparator(),
                annGen.createAnnotation(modelNode).getSourcecode());
    }
}
