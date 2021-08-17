/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.devtools.stdbuilder.xmodel.AbstractGeneratorModelNode;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SinceVersionJavaDocTagGeneratorTest {

    @Mock
    private AbstractGeneratorModelNode modelNode;

    private SinceVersionJavaDocTagGenerator generator = new SinceVersionJavaDocTagGenerator();

    @Test
    public void testCreateAnnotation_withSinceVersion() throws Exception {
        when(modelNode.hasSinceVersion()).thenReturn(true);
        when(modelNode.getSinceVersion()).thenReturn("1.2.3");
        JavaCodeFragment expected = new JavaCodeFragment("@since 1.2.3" + System.lineSeparator());

        JavaCodeFragment annotation = generator.createAnnotation(modelNode);

        assertEquals(expected, annotation);
    }

    /**
     * This case should never happen because the caller needs to check
     * {@link SinceVersionJavaDocTagGenerator#isGenerateAnnotationFor(AbstractGeneratorModelNode)}
     * before calling
     * {@link SinceVersionJavaDocTagGenerator#createAnnotation(AbstractGeneratorModelNode)}. But we
     * want to ensure that there is no error if it happens in mistake.
     */
    @Test
    public void testCreateAnnotation_noSinceVersion() throws Exception {
        when(modelNode.hasSinceVersion()).thenReturn(false);
        JavaCodeFragment expected = new JavaCodeFragment("@since null" + System.lineSeparator());

        JavaCodeFragment annotation = generator.createAnnotation(modelNode);

        assertEquals(expected, annotation);
    }

    @Test
    public void testIsGenerateAnnotationFor() throws Exception {
        generator.isGenerateAnnotationFor(modelNode);

        verify(modelNode).hasSinceVersion();
        verifyNoMoreInteractions(modelNode);
    }

}
