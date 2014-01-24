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

import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.events.VerifyEvent;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.valueset.IEnumValueSet;
import org.faktorips.devtools.core.model.valueset.IRangeValueSet;
import org.faktorips.devtools.core.model.valueset.IUnrestrictedValueSet;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.model.valueset.IValueSetOwner;
import org.faktorips.devtools.core.model.valueset.Messages;
import org.faktorips.devtools.core.model.valueset.ValueSetType;
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
        if (stringToBeParsed.isEmpty()) {
            return getParsedEmptyString();
        } else {
            AbstractValueSetFormat valueSetFormat = getResponsibleValueSetFormat(stringToBeParsed);
            if (valueSetFormat != null) {
                return valueSetFormat.parse(stringToBeParsed);
            } else {
                return getValueSet();
            }
        }
    }

    private AbstractValueSetFormat getResponsibleValueSetFormat(String stringToBeParsed) {
        if (isResponsibleForU(stringToBeParsed)) {
            return unrestrictedValueSetFormat;
        } else if (isResponsibleForR(stringToBeParsed)) {
            return rangeValueSetFormat;
        } else if (isResponsibleForEnum()) {
            return enumValueSetFormat;
        }
        return null;
    }

    private IValueSet getParsedEmptyString() {
        if (isUnrestrictedAllowed()) {
            return unrestrictedValueSetFormat.parse(StringUtils.EMPTY);
        } else if (isOnlyRangeAllowed()) {
            return rangeValueSetFormat.getUnlimitedRangeSet();
        } else {
            return enumValueSetFormat.getEmptyEnumSet();
        }
    }

    protected boolean isAllowedValueSetType(ValueSetType valueSetType) {
        try {
            List<ValueSetType> allowedValueSetTypes = this.valueSetOwner.getAllowedValueSetTypes(this.valueSetOwner
                    .getIpsProject());
            return allowedValueSetTypes.contains(valueSetType);
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    public boolean isResponsibleForU(String stringTobeParsed) {
        return Messages.ValueSetFormat_unrestricted.equals(stringTobeParsed) && isUnrestrictedAllowed();
    }

    public boolean isUnrestrictedAllowed() {
        return isAllowedValueSetType(ValueSetType.UNRESTRICTED);
    }

    public boolean isResponsibleForEnum() {
        return isAllowedValueSetType(ValueSetType.ENUM);
    }

    public boolean isResponsibleForR(String stringTobeParsed) {
        return (isRange(stringTobeParsed) && isRangeValueSetAllowed()) || isOnlyRangeAllowed();
    }

    private boolean isRange(String stringToBeParsed) {
        return stringToBeParsed.startsWith(IRangeValueSet.RANGE_VALUESET_START)
                && stringToBeParsed.endsWith(IRangeValueSet.RANGE_VALUESET_END);
    }

    private boolean isRangeValueSetAllowed() {
        return isAllowedValueSetType(ValueSetType.RANGE);
    }

    public boolean isOnlyRangeAllowed() {
        try {
            List<ValueSetType> allowedValueSetTypes = this.valueSetOwner.getAllowedValueSetTypes(this.valueSetOwner
                    .getIpsProject());
            return allowedValueSetTypes.size() == 1 && allowedValueSetTypes.get(0).equals(ValueSetType.RANGE);
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
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
