/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.inputformat.parse;

import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.events.VerifyEvent;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.model.valueset.IValueSetOwner;
import org.faktorips.devtools.core.model.valueset.ValueSetType;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.inputformat.AbstractInputFormat;
import org.faktorips.devtools.core.ui.inputformat.IInputFormat;

/***
 * Abstract class to parse and format an {@link IValueSet} according to its characteristics. The
 * {@link IValueSet} is parsed according to its {@link IInputFormat} datatype.
 * 
 */
public abstract class AbstractValueSetFormat extends AbstractInputFormat<IValueSet> {

    private final IValueSetOwner valueSetOwner;

    private final IpsUIPlugin uiPlugin;

    private IInputFormat<String> cachedInputFormat;

    private ValueDatatype cachedValueDatatype;

    public AbstractValueSetFormat(IValueSetOwner valueSetOwner, IpsUIPlugin uiPlugin) {
        super(StringUtils.EMPTY, IpsPlugin.getDefault().getIpsPreferences().getDatatypeFormattingLocale());
        this.valueSetOwner = valueSetOwner;
        this.uiPlugin = uiPlugin;
    }

    @Override
    public IValueSet parse(String stringToBeparsed) {
        return super.parse(stringToBeparsed, false);
    }

    protected boolean isAllowedValueSetType(ValueSetType valueSetType) {
        return isAllowedValueSetType(valueSetType, false);
    }

    protected boolean isOnlyAllowedValueSetType(ValueSetType valueSetType) {
        return isAllowedValueSetType(valueSetType, true);
    }

    private boolean isAllowedValueSetType(ValueSetType valueSetType, boolean only) {
        try {
            List<ValueSetType> allowedValueSetTypes = this.valueSetOwner.getAllowedValueSetTypes(this.valueSetOwner
                    .getIpsProject());
            if (only) {
                return allowedValueSetTypes.size() == 1 && allowedValueSetTypes.get(0).equals(valueSetType);
            } else {
                return allowedValueSetTypes.contains(valueSetType);
            }
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    protected IValueSetOwner getValueSetOwner() {
        return this.valueSetOwner;
    }

    protected IValueSet getValueSet() {
        return this.valueSetOwner.getValueSet();
    }

    protected String getNextPartId() {
        return valueSetOwner.getIpsModel().getNextPartId(valueSetOwner);
    }

    protected IInputFormat<String> getInputFormat() {
        ValueDatatype valueDatatype = getValueDatatype();
        if (cachedInputFormat == null || valueDatatype != cachedValueDatatype) {
            cachedInputFormat = uiPlugin.getInputFormat(valueDatatype, valueSetOwner.getIpsProject());
            cachedInputFormat.setNullString(getNullPresentationInValueSet());
            cachedValueDatatype = valueDatatype;
        }
        return cachedInputFormat;
    }

    protected abstract String getNullPresentationInValueSet();

    private ValueDatatype getValueDatatype() {
        return valueSetOwner.findValueDatatype(valueSetOwner.getIpsProject());
    }

    protected String parseValue(String value) {
        IInputFormat<String> inputFormat = getInputFormat();
        String parsedValue = inputFormat.parse(value.trim());
        return parsedValue;
    }

    public abstract boolean isResponsibleFor(String resultingText);

    @Override
    protected void verifyInternal(VerifyEvent e, String resultingText) {
        // nothing to verify
    }

    @Override
    protected void initFormat(Locale locale) {
        // nothing to init
    }
}
