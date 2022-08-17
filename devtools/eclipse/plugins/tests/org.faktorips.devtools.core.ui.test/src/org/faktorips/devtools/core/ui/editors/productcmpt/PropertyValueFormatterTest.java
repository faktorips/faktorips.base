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

import org.faktorips.devtools.model.productcmpt.IPropertyValue;
import org.junit.Test;

public class PropertyValueFormatterTest {

    @Test(expected = NullPointerException.class)
    public void testFormatNPE() {
        PropertyValueFormatter.format(null);
    }

    @Test(expected = IllegalStateException.class)
    public void testFormatIllegalStateException() {
        IPropertyValue pv = mock(IPropertyValue.class);
        PropertyValueFormatter.format(pv);
    }
}
