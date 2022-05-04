/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.ui.preferencepages;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Objects;

import org.junit.Test;

public class ColumnLabelProviderWithWrappedTooltipTest {

    private static final String SHORT_TEXT = "Short text";
    private static final String LONG_TEXT = "A very long line that should be wrapped here when displayed in a tooltip --> <-- to start a new line here";
    private static final String LONG_WRAPPED_TEXT = "First line\n"
            + "Second line\n"
            + LONG_TEXT;

    @Test
    public void testGetText() {
        ColumnLabelProviderWithWrappedTooltip labelProvider = new ColumnLabelProviderWithWrappedTooltip(
                Objects::toString);

        assertThat(labelProvider.getText(LONG_WRAPPED_TEXT), is("First line"));
        assertThat(labelProvider.getText(SHORT_TEXT), is(SHORT_TEXT));
        assertThat(labelProvider.getText(LONG_TEXT), is(LONG_TEXT));
    }

    @Test
    public void testGetToolTipText() {
        ColumnLabelProviderWithWrappedTooltip labelProvider = new ColumnLabelProviderWithWrappedTooltip(
                Objects::toString);

        assertThat(labelProvider.getToolTipText(LONG_WRAPPED_TEXT), is("First line\n"
                + "Second line\n"
                + "A very long line that should be wrapped here when displayed in a tooltip -->\n"
                + "<-- to start a new line here"));
        assertThat(labelProvider.getToolTipText(SHORT_TEXT), is(SHORT_TEXT));
        assertThat(labelProvider.getToolTipText(LONG_TEXT),
                is("A very long line that should be wrapped here when displayed in a tooltip -->\n"
                        + "<-- to start a new line here"));
    }

}
