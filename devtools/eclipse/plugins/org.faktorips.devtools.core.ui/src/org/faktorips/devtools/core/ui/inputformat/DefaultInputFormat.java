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
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.DatatypeFormatter;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.runtime.internal.IpsStringUtils;

/**
 * Fallback input format.
 */
public class DefaultInputFormat extends AbstractInputFormat<String> {

    private ValueDatatype datatype;

    private DatatypeFormatter formatter;

    public DefaultInputFormat(ValueDatatype datatype) {
        this(datatype, IpsPlugin.getDefault().getIpsPreferences().getDatatypeFormatter());
    }

    public DefaultInputFormat(ValueDatatype datatype, DatatypeFormatter formatter) {
        super(IpsPlugin.getDefault().getIpsPreferences().getNullPresentation(), IpsPlugin.getDefault()
                .getIpsPreferences().getDatatypeFormattingLocale());
        this.datatype = datatype;
        this.formatter = formatter;
    }

    @Override
    protected String parseInternal(String stringToBeparsed) {
        return stringToBeparsed;
    }

    /**
     * Allow empty string as an independent value. E.g. for datatype string "" is different than
     * <code>null</code>.
     */
    @Override
    protected boolean isRepresentingNull(String stringToBeParsed) {
        return IpsStringUtils.trimEquals(getNullString(), stringToBeParsed)
                || isPreferencesNullPresentation(stringToBeParsed);
    }

    @Override
    protected String formatInternal(String value) {
        return formatter.formatValue(datatype, value);
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
