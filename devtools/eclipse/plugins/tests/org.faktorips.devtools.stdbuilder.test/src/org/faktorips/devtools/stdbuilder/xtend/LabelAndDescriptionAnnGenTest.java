/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.xtend;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.stdbuilder.xmodel.AbstractGeneratorModelNode;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class LabelAndDescriptionAnnGenTest extends AbstractIpsPluginTest {

    private LabelAndDescriptionAnnGen annGen = new LabelAndDescriptionAnnGen();

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
