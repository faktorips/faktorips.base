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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.events.VerifyEvent;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.internal.model.valueset.EnumValueSet;
import org.faktorips.devtools.core.internal.model.valueset.RangeValueSet;
import org.faktorips.devtools.core.internal.model.valueset.UnrestrictedValueSet;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.valueset.IEnumValueSet;
import org.faktorips.devtools.core.model.valueset.IRangeValueSet;
import org.faktorips.devtools.core.model.valueset.IUnrestrictedValueSet;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.model.valueset.IValueSetOwner;
import org.faktorips.devtools.core.model.valueset.Messages;
import org.faktorips.devtools.core.model.valueset.ValueSetType;
import org.faktorips.devtools.core.ui.IpsUIPlugin;

public class ValueSetFormat extends AbstractInputFormat<IValueSet> {

    private final IValueSetOwner valueSetOwner;

    private final IpsUIPlugin uiPlugin;

    private IInputFormat<String> cachedIinputFormat;

    private ValueDatatype cachedValueDatatype;

    public ValueSetFormat(IValueSetOwner valueSetOwner, IpsUIPlugin uiPlugin) {
        this.valueSetOwner = valueSetOwner;
        this.uiPlugin = uiPlugin;
    }

    public static ValueSetFormat newInstance(IValueSetOwner valueSetOwner) {
        ValueSetFormat format = new ValueSetFormat(valueSetOwner, IpsUIPlugin.getDefault());
        format.initFormat();
        return format;
    }

    @Override
    protected IValueSet parseInternal(String stringToBeParsed) {
        if (isEnumValueSetAllowed()) {
            if (getValueSet() instanceof IRangeValueSet) {
                return getValueSet();
            }
            if (stringToBeParsed.isEmpty()) {
                if (isUnrestrictedAllowed()) {
                    return getUnrestrictedValueSet();
                } else {
                    return getEmptyValueSet();
                }
            } else if (Messages.ValueSetFormat_unrestricted.equals(stringToBeParsed) && isUnrestrictedAllowed()) {
                return getUnrestrictedValueSet();
            } else if (EnumValueSet.ENUM_VALUESET_EMPTRY.equals(stringToBeParsed)) {
                return getEmptyValueSet();
            }
            String[] split = stringToBeParsed.split("\\" + EnumValueSet.ENUM_VALUESET_SEPARATOR); //$NON-NLS-1$
            List<String> parsedValues = parseValues(split);
            if (!isEqualContent(parsedValues)) {
                EnumValueSet enumValueSet = createNewEnumValueSet(parsedValues);
                return enumValueSet;
            }
        }
        return getValueSet();
    }

    private boolean isUnrestrictedAllowed() {
        return isAllowedValueSetType(ValueSetType.UNRESTRICTED);
    }

    private boolean isEnumValueSetAllowed() {
        return isAllowedValueSetType(ValueSetType.ENUM);
    }

    private boolean isAllowedValueSetType(ValueSetType valueSetType) {
        try {
            List<ValueSetType> allowedValueSetTypes = this.valueSetOwner.getAllowedValueSetTypes(this.valueSetOwner
                    .getIpsProject());
            return allowedValueSetTypes.contains(valueSetType);
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    private IValueSet getUnrestrictedValueSet() {
        final IValueSet valueSet = getValueSet();
        if (valueSet.isUnrestricted()) {
            return valueSet;
        } else {
            UnrestrictedValueSet newValueSet = new UnrestrictedValueSet(valueSetOwner, getNextPartId(valueSetOwner));
            return newValueSet;
        }
    }

    private IValueSet getEmptyValueSet() {
        IValueSet valueSet = getValueSet();
        if (valueSet.isEnum() && ((IEnumValueSet)valueSet).getValuesAsList().isEmpty()) {
            return valueSet;
        } else {
            return createNewEnumValueSet(new ArrayList<String>());
        }
    }

    private List<String> parseValues(String[] split) {
        List<String> parseValues = new ArrayList<String>();
        IInputFormat<String> inputFormat = getInputFormat();
        for (String value : split) {
            parseValues.add(inputFormat.parse(value.trim()));
        }
        return parseValues;
    }

    private IInputFormat<String> getInputFormat() {
        ValueDatatype valueDatatype = getValueDatatype();
        if (cachedIinputFormat == null || valueDatatype != cachedValueDatatype) {
            cachedIinputFormat = uiPlugin.getInputFormat(valueDatatype, valueSetOwner.getIpsProject());
            cachedValueDatatype = valueDatatype;
        }

        return cachedIinputFormat;
    }

    private boolean isEqualContent(List<String> parsedValues) {
        return getValueSet() instanceof IEnumValueSet && getValuesAsList().equals(parsedValues);
    }

    private List<String> getValuesAsList() {
        return ((IEnumValueSet)getValueSet()).getValuesAsList();
    }

    private IValueSet getValueSet() {
        return this.valueSetOwner.getValueSet();
    }

    private EnumValueSet createNewEnumValueSet(List<String> values) {
        EnumValueSet valueSet = new EnumValueSet(valueSetOwner, values, getNextPartId(valueSetOwner));
        return valueSet;
    }

    private String getNextPartId(IIpsObjectPartContainer parent) {
        return parent.getIpsModel().getNextPartId(parent);
    }

    @Override
    protected String formatInternal(IValueSet valueSet) {
        if (valueSet instanceof IEnumValueSet) {
            return formatEnumValueSet(valueSet);
        } else if (valueSet instanceof IRangeValueSet) {
            return formatRangeValueSet(valueSet);
        } else if (valueSet instanceof IUnrestrictedValueSet) {
            return formatUnrestrictedValueSet();
        }
        return StringUtils.EMPTY;
    }

    private String formatEnumValueSet(IValueSet valueSet) {
        IEnumValueSet enumValueSet = (IEnumValueSet)valueSet;
        StringBuffer buffer = new StringBuffer();
        String[] values = enumValueSet.getValues();
        if (values.length == 0) {
            return EnumValueSet.ENUM_VALUESET_EMPTRY;
        }
        IInputFormat<String> inputFormat = getInputFormat();
        for (String id : values) {
            String formatedEnumText = inputFormat.format(id);
            buffer.append(formatedEnumText);
            buffer.append(" " + EnumValueSet.ENUM_VALUESET_SEPARATOR + " "); //$NON-NLS-1$ //$NON-NLS-2$
        }
        if (buffer.length() > 3) {
            // Remove the separator after the last value (" | ")
            buffer.delete(buffer.length() - 3, buffer.length());
        }
        return buffer.toString();
    }

    private String formatRangeValueSet(IValueSet valueSet) {
        RangeValueSet rangeValueSet = (RangeValueSet)valueSet;
        StringBuffer sb = new StringBuffer();
        sb.append('[');
        sb.append((rangeValueSet.getLowerBound() == null ? "unlimited" : rangeValueSet.getLowerBound())); //$NON-NLS-1$
        sb.append('-');
        sb.append((rangeValueSet.getUpperBound() == null ? "unlimited" : rangeValueSet.getUpperBound())); //$NON-NLS-1$
        sb.append(']');
        if (rangeValueSet.getStep() != null) {
            sb.append(org.faktorips.devtools.core.internal.model.valueset.Messages.RangeValueSet_0);
            sb.append(rangeValueSet.getStep());
        }
        return sb.toString();
    }

    private String formatUnrestrictedValueSet() {
        return org.faktorips.devtools.core.model.valueset.Messages.ValueSetFormat_unrestricted;
    }

    private ValueDatatype getValueDatatype() {
        try {
            return valueSetOwner.findValueDatatype(valueSetOwner.getIpsProject());
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    @Override
    protected void verifyInternal(VerifyEvent e, String resultingText) {
        // do nothing
    }

    /**
     * {@inheritDoc}
     * <p>
     * Currently parsing range is not supported
     */
    @Override
    public void verifyText(VerifyEvent e) {
        super.verifyText(e);
        if (getValueSet() instanceof IRangeValueSet) {
            e.doit = false;
        }
    }

    @Override
    protected void initFormat(Locale locale) {
        // do nothing
    }

}
