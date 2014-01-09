/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.inputformat;

import java.util.Locale;

import org.eclipse.swt.events.VerifyEvent;

/**
 * Fallback inputformat. Simply passes through the given string in {@link #parse(String)} and
 * {@link #format(String)}.
 */
public class DefaultInputFormat extends AbstractInputFormat<String> {

    @Override
    protected String parseInternal(String stringToBeparsed) {
        return stringToBeparsed;
    }

    @Override
    protected String formatInternal(String value) {
        return value;
    }

    @Override
    protected void verifyInternal(VerifyEvent e, String resultingText) {
        // Nothing to do
    }

    @Override
    protected void initFormat(Locale locale) {
        // Nothing to initialize
    }

}
