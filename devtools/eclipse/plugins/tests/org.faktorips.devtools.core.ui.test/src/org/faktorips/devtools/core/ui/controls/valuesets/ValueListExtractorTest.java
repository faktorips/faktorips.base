/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controls.valuesets;

import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.faktorips.datatype.EnumDatatype;
import org.junit.Test;

public class ValueListExtractorTest {

    @Test
    public void testExtractValues_fromEnumDatatype_honorsIncludeNull() throws Exception {
        EnumDatatype valueDatatype = mock(EnumDatatype.class);
        when(valueDatatype.getAllValueIds(anyBoolean())).thenReturn(new String[0]);

        ValueListExtractor.extractValues(valueDatatype, true);
        verify(valueDatatype).getAllValueIds(true);

        ValueListExtractor.extractValues(valueDatatype, false);
        verify(valueDatatype).getAllValueIds(false);
    }

}
