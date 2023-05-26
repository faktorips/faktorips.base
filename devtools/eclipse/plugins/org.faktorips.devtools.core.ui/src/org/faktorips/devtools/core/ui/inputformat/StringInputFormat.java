/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.inputformat;

import java.util.Locale;

import org.eclipse.swt.events.VerifyEvent;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.runtime.internal.IpsStringUtils;

public class StringInputFormat extends AbstractInputFormat<String> {

    public StringInputFormat() {
        super(IpsStringUtils.EMPTY, IpsPlugin.getDefault().getIpsPreferences().getDatatypeFormattingLocale());
    }

    @Override
    protected String parseInternal(String stringToBeParsed) {
        return stringToBeParsed;
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
        // Nothing to do
    }
}
