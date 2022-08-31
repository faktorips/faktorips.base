/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.extproperties;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class ExtensionPropertyDefinitionTest {

    private static final String VALUE = "test123";

    @Mock
    private Object defaultValue;

    @Mock(answer = Answers.CALLS_REAL_METHODS)
    private ExtensionPropertyDefinition extensionPropertyDefinition;

    @Test
    public void testGetDefaultValueIIpsObjectPartContainer_default() throws Exception {
        when(extensionPropertyDefinition.getDefaultValue()).thenReturn(VALUE);
        IIpsObjectPartContainer part = mock(IIpsObjectPartContainer.class);

        Object defaultValue = extensionPropertyDefinition.getDefaultValue(part);

        assertEquals(VALUE, defaultValue);
    }

}
