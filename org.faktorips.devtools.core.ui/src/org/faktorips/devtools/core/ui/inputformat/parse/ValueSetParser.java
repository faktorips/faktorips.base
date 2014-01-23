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

import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.model.valueset.IValueSetOwner;
import org.faktorips.devtools.core.ui.inputformat.IInputFormat;

public abstract class ValueSetParser {

    private IValueSetOwner valueSetOwner;
    private IInputFormat<String> inputFormat;

    public ValueSetParser(IValueSetOwner valueSetOwner, IInputFormat<String> inputFormat) {
        this.valueSetOwner = valueSetOwner;
        this.inputFormat = inputFormat;
    }

    public abstract IValueSet parseValueSet(String stringTobeParsed);

    protected IInputFormat<String> getInputFormat() {
        return inputFormat;
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

}
