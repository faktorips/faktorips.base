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
import org.faktorips.devtools.core.model.valueset.IEnumValueSet;
import org.faktorips.devtools.core.model.valueset.IRangeValueSet;
import org.faktorips.devtools.core.model.valueset.IUnrestrictedValueSet;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.model.valueset.IValueSetOwner;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.inputformat.parse.EnumValueSetParser;
import org.faktorips.devtools.core.ui.inputformat.parse.RangeValueSetParser;
import org.faktorips.devtools.core.ui.inputformat.parse.UnrestrictedValueSetParser;

public class ValueSetFormat extends AbstractInputFormat<IValueSet> {

    private final IValueSetOwner valueSetOwner;

    private RangeValueSetParser rangeValueSetParser;

    private EnumValueSetParser enumValueSetParser;

    private UnrestrictedValueSetParser unrestrictedValueSetParser;

    public ValueSetFormat(IValueSetOwner valueSetOwner, IpsUIPlugin uiPlugin) {
        this.valueSetOwner = valueSetOwner;
        rangeValueSetParser = new RangeValueSetParser(valueSetOwner, uiPlugin);
        enumValueSetParser = new EnumValueSetParser(valueSetOwner, uiPlugin);
        unrestrictedValueSetParser = new UnrestrictedValueSetParser(valueSetOwner, uiPlugin);
    }

    public static ValueSetFormat newInstance(IValueSetOwner valueSetOwner) {
        ValueSetFormat format = new ValueSetFormat(valueSetOwner, IpsUIPlugin.getDefault());
        format.initFormat();
        return format;
    }

    @Override
    protected String formatInternal(IValueSet valueSet) {
        if (valueSet instanceof IEnumValueSet) {
            return enumValueSetParser.formatEnumValueSet((IEnumValueSet)valueSet);
        } else if (valueSet instanceof IRangeValueSet) {
            return rangeValueSetParser.formatRangeValueSet((IRangeValueSet)valueSet);
        } else if (valueSet instanceof IUnrestrictedValueSet) {
            return unrestrictedValueSetParser.formatUnrestrictedValueSet();
        }
        return StringUtils.EMPTY;
    }

    @Override
    protected IValueSet parseInternal(String stringToBeParsed) {
        if (stringToBeParsed.isEmpty()) {
            return getParsedEmptyString();
        } else if (unrestrictedValueSetParser.isResponsibleFor(stringToBeParsed)) {
            return unrestrictedValueSetParser.parseValueSet(stringToBeParsed);
        } else if (rangeValueSetParser.isResponsibleFor(stringToBeParsed)) {
            return rangeValueSetParser.parseValueSet(stringToBeParsed);
        } else if (enumValueSetParser.isResponsibleFor(stringToBeParsed)) {
            return enumValueSetParser.parseValueSet(stringToBeParsed);
        }
        return getValueSet();
    }

    private IValueSet getParsedEmptyString() {
        if (unrestrictedValueSetParser.isUnrestrictedAllowed()) {
            return unrestrictedValueSetParser.parseValueSet(StringUtils.EMPTY);
        } else if (rangeValueSetParser.isOnlyRangeAllowed()) {
            return rangeValueSetParser.getUnlimitedRangeSet();
        } else {
            return enumValueSetParser.getEmptyEnumSet();
        }
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
