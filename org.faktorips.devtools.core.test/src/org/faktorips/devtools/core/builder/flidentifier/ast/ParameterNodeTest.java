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
import static org.mockito.Mockito.when;

import org.faktorips.devtools.core.MultiLanguageSupport;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.method.IParameter;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.util.TextRegion;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ParameterNodeTest {

    private static final String MY_LABEL = "myLabel";

    private static final String MY_DESCRIPTION = "MyDescription";

    @Mock
    private IIpsProject ipsProject;

    @Mock
    private IParameter parameter;

    @Mock
    private TextRegion textRegion;

    @InjectMocks
    private ParameterNode parameterNode;

    @Mock
    private IType type;

    @Mock
    private MultiLanguageSupport multiLanguageSupport;

    @Test
    public void testGetDescription_forTypes() throws Exception {
        when(parameter.findDatatype(ipsProject)).thenReturn(type);
        when(multiLanguageSupport.getLocalizedLabel(type)).thenReturn(MY_LABEL);
        when(multiLanguageSupport.getLocalizedDescription(type)).thenReturn(MY_DESCRIPTION);

        String description = parameterNode.getDescription(multiLanguageSupport);

        assertEquals(MY_LABEL + " - " + MY_DESCRIPTION, description);
    }

}
