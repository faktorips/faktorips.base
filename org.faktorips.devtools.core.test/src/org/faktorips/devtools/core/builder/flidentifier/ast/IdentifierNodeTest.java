/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.builder.flidentifier.ast;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import org.faktorips.devtools.core.MultiLanguageSupport;
import org.faktorips.devtools.core.model.ipsobject.IDescribedElement;
import org.faktorips.devtools.core.model.ipsobject.ILabeledElement;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class IdentifierNodeTest {

    private static final String MY_NAME = "myName";

    private static final String MY_LABEL = "myLabel";

    private static final String MY_DESCRIPTION = "MyDescription";

    @Mock(answer = Answers.CALLS_REAL_METHODS)
    private IdentifierNode identifierNode;

    @Mock
    private MultiLanguageSupport multiLanguageSupport;

    @Test
    public void testGetNameAndDescriptionIIpsElementMultiLanguageSupport_labeled() throws Exception {
        ILabeledElement labeledElement = mock(ILabeledElement.class);
        when(multiLanguageSupport.getLocalizedLabel(labeledElement)).thenReturn(MY_LABEL);

        String description = identifierNode.getNameAndDescription(labeledElement, multiLanguageSupport);

        assertEquals(MY_LABEL, description);
    }

    @Test
    public void testGetNameAndDescriptionIIpsElementMultiLanguageSupport_described() throws Exception {
        IDescribedElement descibedElement = mock(IDescribedElement.class);
        when(multiLanguageSupport.getLocalizedDescription(descibedElement)).thenReturn(MY_DESCRIPTION);
        when(descibedElement.getName()).thenReturn(MY_NAME);

        String description = identifierNode.getNameAndDescription(descibedElement, multiLanguageSupport);

        assertEquals(MY_NAME + " - " + MY_DESCRIPTION, description);

    }

    @Test
    public void testGetNameAndDescriptionIIpsElementMultiLanguageSupport_labeledAndDescribed() throws Exception {
        ILabeledElement labeledAndDescribedElement = mock(ILabeledElement.class,
                withSettings().extraInterfaces(IDescribedElement.class));
        when(multiLanguageSupport.getLocalizedLabel(labeledAndDescribedElement)).thenReturn(MY_LABEL);
        when(multiLanguageSupport.getLocalizedDescription((IDescribedElement)labeledAndDescribedElement)).thenReturn(
                MY_DESCRIPTION);

        String description = identifierNode.getNameAndDescription(labeledAndDescribedElement, multiLanguageSupport);

        assertEquals(MY_LABEL + " - " + MY_DESCRIPTION, description);

    }

}
