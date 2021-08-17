/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.util;

import java.util.function.Consumer;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;

/**
 * Utility class for easier creation of {@link SelectionListener}s.
 */
public class SelectionListeners {

    private SelectionListeners() {
        // util
    }

    /**
     * Creates a {@link SelectionListener} using the given {@link Consumer} as implementation for
     * {@link SelectionListener#widgetSelected(SelectionEvent)} with an empty
     * {@link SelectionListener#widgetDefaultSelected(SelectionEvent)}.
     */
    public static SelectionListener widgetSelected(Consumer<SelectionEvent> widgetSelected) {
        return new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                widgetSelected.accept(e);
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                // no default selection
            }
        };
    }
}