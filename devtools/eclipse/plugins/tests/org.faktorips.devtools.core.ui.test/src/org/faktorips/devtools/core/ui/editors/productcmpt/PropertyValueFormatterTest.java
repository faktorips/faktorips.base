/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.productcmpt;

import static org.mockito.Mockito.mock;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.faktorips.devtools.model.productcmpt.IPropertyValue;
import org.junit.jupiter.api.Test;

public class PropertyValueFormatterTest {

    @Test
    public void testFormatNPE() {
        assertThrows(NullPointerException.class, () -> {
            PropertyValueFormatter.format(null);
        });
    }

    @Test
    public void testFormatIllegalStateException() {
        assertThrows(IllegalStateException.class, () -> {
            IPropertyValue pv = mock(IPropertyValue.class);
            PropertyValueFormatter.format(pv);
        });
    }
}
