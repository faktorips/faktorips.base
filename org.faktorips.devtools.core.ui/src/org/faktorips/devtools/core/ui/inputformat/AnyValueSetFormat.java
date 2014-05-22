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

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.events.VerifyEvent;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.valueset.IEnumValueSet;
import org.faktorips.devtools.core.model.valueset.IRangeValueSet;
import org.faktorips.devtools.core.model.valueset.IUnrestrictedValueSet;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.model.valueset.IValueSetOwner;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.inputformat.parse.AbstractValueSetFormat;
import org.faktorips.devtools.core.ui.inputformat.parse.EnumValueSetFormat;
import org.faktorips.devtools.core.ui.inputformat.parse.RangeValueSetFormat;
import org.faktorips.devtools.core.ui.inputformat.parse.UnrestrictedValueSetFormat;

public class AnyValueSetFormat extends AbstractInputFormat<IValueSet> {

    private final IValueSetOwner valueSetOwner;

    private RangeValueSetFormat rangeValueSetFormat;

    private EnumValueSetFormat enumValueSetFormat;

    private UnrestrictedValueSetFormat unrestrictedValueSetFormat;

    public AnyValueSetFormat(IValueSetOwner valueSetOwner, IpsUIPlugin uiPlugin) {
        super(StringUtils.EMPTY, IpsPlugin.getDefault().getIpsPreferences().getDatatypeFormattingLocale());
        this.valueSetOwner = valueSetOwner;
        rangeValueSetFormat = new RangeValueSetFormat(valueSetOwner, uiPlugin);
        enumValueSetFormat = new EnumValueSetFormat(valueSetOwner, uiPlugin);
        unrestrictedValueSetFormat = new UnrestrictedValueSetFormat(valueSetOwner, uiPlugin);
    }

    public static AnyValueSetFormat newInstance(IValueSetOwner valueSetOwner) {
        AnyValueSetFormat format = new AnyValueSetFormat(valueSetOwner, IpsUIPlugin.getDefault());
        format.initFormat();
        return format;
    }

    @Override
    protected String formatInternal(IValueSet valueSet) {
        if (valueSet instanceof IEnumValueSet) {
            return enumValueSetFormat.formatInternal(valueSet);
        } else if (valueSet instanceof IRangeValueSet) {
            return rangeValueSetFormat.formatInternal(valueSet);
        } else if (valueSet instanceof IUnrestrictedValueSet) {
            return unrestrictedValueSetFormat.formatInternal(valueSet);
        }
        return StringUtils.EMPTY;
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
        } else if (enumValueSetFormat.isResponsibleFor(stringToBeParsed)) {
            return enumValueSetFormat;
        }
        return null;
    }

    private IValueSet getValueSet() {
        return this.valueSetOwner.getValueSet();
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
