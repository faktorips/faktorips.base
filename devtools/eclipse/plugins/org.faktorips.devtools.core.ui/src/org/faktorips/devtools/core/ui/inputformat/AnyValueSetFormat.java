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
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.inputformat.parse.AbstractValueSetFormat;
import org.faktorips.devtools.core.ui.inputformat.parse.EnumValueSetFormat;
import org.faktorips.devtools.core.ui.inputformat.parse.RangeValueSetFormat;
import org.faktorips.devtools.core.ui.inputformat.parse.StringLengthValueSetFormat;
import org.faktorips.devtools.core.ui.inputformat.parse.UnrestrictedValueSetFormat;
import org.faktorips.devtools.model.valueset.IValueSet;
import org.faktorips.devtools.model.valueset.IValueSetOwner;
import org.faktorips.runtime.internal.IpsStringUtils;

public class AnyValueSetFormat extends AbstractInputFormat<IValueSet> {

    private final IValueSetOwner valueSetOwner;

    private RangeValueSetFormat rangeValueSetFormat;

    private EnumValueSetFormat enumValueSetFormat;

    private UnrestrictedValueSetFormat unrestrictedValueSetFormat;

    private StringLengthValueSetFormat stringLengthValueSetFormat;

    public AnyValueSetFormat(IValueSetOwner valueSetOwner, IpsUIPlugin uiPlugin) {
        super(IpsStringUtils.EMPTY, IpsPlugin.getDefault().getIpsPreferences().getDatatypeFormattingLocale());
        this.valueSetOwner = valueSetOwner;
        rangeValueSetFormat = new RangeValueSetFormat(valueSetOwner, uiPlugin);
        enumValueSetFormat = new EnumValueSetFormat(valueSetOwner, uiPlugin);
        unrestrictedValueSetFormat = new UnrestrictedValueSetFormat(valueSetOwner, uiPlugin);
        stringLengthValueSetFormat = new StringLengthValueSetFormat(valueSetOwner, uiPlugin);
    }

    public static AnyValueSetFormat newInstance(IValueSetOwner valueSetOwner) {
        AnyValueSetFormat format = new AnyValueSetFormat(valueSetOwner, IpsUIPlugin.getDefault());
        format.initFormat();
        return format;
    }

    @Override
    protected String formatInternal(IValueSet valueSet) {
        if (valueSet.isEnum()) {
            return enumValueSetFormat.formatInternal(valueSet);
        } else if (valueSet.isRange()) {
            return rangeValueSetFormat.formatInternal(valueSet);
        } else if (valueSet.isUnrestricted()) {
            return unrestrictedValueSetFormat.formatInternal(valueSet);
        } else if (valueSet.isStringLength()) {
            return stringLengthValueSetFormat.formatInternal(valueSet);
        }
        return IpsStringUtils.EMPTY;
    }

    @Override
    protected IValueSet parseInternal(String stringToBeParsed) {
        AbstractValueSetFormat valueSetFormat = getValueSetFormat(stringToBeParsed);
        if (valueSetFormat != null) {
            return valueSetFormat.parse(stringToBeParsed, false);
        } else {
            return getValueSet();
        }
    }

    private AbstractValueSetFormat getValueSetFormat(String stringToBeParsed) {
        if (unrestrictedValueSetFormat.isResponsibleFor(stringToBeParsed)) {
            return unrestrictedValueSetFormat;
        } else if (rangeValueSetFormat.isResponsibleFor(stringToBeParsed)) {
            return rangeValueSetFormat;
        } else if (stringLengthValueSetFormat.isResponsibleFor(stringToBeParsed)) {
            return stringLengthValueSetFormat;
        } else if (enumValueSetFormat.isResponsibleFor(stringToBeParsed)) {
            return enumValueSetFormat;
        } else if (IpsStringUtils.isEmpty(stringToBeParsed)) {
            return unrestrictedValueSetFormat;
        } else {
            return null;
        }
    }

    private IValueSet getValueSet() {
        return valueSetOwner.getValueSet();
    }

    @Override
    protected void verifyInternal(VerifyEvent e, String resultingText) {
        // do nothing
    }

    @Override
    protected void initFormat(Locale locale) {
        // do nothing
    }

}
