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

package org.faktorips.devtools.core.ui.inputformat.parse;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.model.valueset.IValueSetOwner;
import org.faktorips.devtools.core.model.valueset.ValueSetType;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.inputformat.IInputFormat;

/***
 * Abstract class to parse and format an {@link IValueSet} according to its characteristics. The
 * {@link IValueSet} is parsed according to its {@link IInputFormat} datatype.
 * 
 */
public abstract class ValueSetParser {

    private final IValueSetOwner valueSetOwner;

    private final IpsUIPlugin uiPlugin;

    private IInputFormat<String> cachedIinputFormat;

    private ValueDatatype cachedValueDatatype;

    public ValueSetParser(IValueSetOwner valueSetOwner, IpsUIPlugin uiPlugin) {
        this.valueSetOwner = valueSetOwner;
        this.uiPlugin = uiPlugin;
    }

    public abstract IValueSet parseValueSet(String stringTobeParsed);

    public abstract boolean isResponsibleFor(String stringTobeParsed);

    protected boolean isAllowedValueSetType(ValueSetType valueSetType) {
        try {
            List<ValueSetType> allowedValueSetTypes = this.valueSetOwner.getAllowedValueSetTypes(this.valueSetOwner
                    .getIpsProject());
            return allowedValueSetTypes.contains(valueSetType);
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

    protected String getNextPartIdOfValueSetOwner() {
        return valueSetOwner.getIpsModel().getNextPartId(valueSetOwner);
    }

    protected IInputFormat<String> getInputFormat() {
        ValueDatatype valueDatatype = getValueDatatype();
        if (cachedIinputFormat == null || valueDatatype != cachedValueDatatype) {
            cachedIinputFormat = uiPlugin.getInputFormat(valueDatatype, valueSetOwner.getIpsProject());
            cachedValueDatatype = valueDatatype;
        }
        return cachedIinputFormat;
    }

    private ValueDatatype getValueDatatype() {
        try {
            return valueSetOwner.findValueDatatype(valueSetOwner.getIpsProject());
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }
}
