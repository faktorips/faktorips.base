/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.internal.model.valueset;

import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.model.valueset.IValueSetOwner;
import org.faktorips.util.message.MessageList;

public abstract class AbstractValueSetValidator<T extends IValueSet> {

    private final T valueSet;
    private final IValueSetOwner owner;
    private final ValueDatatype datatype;

    public AbstractValueSetValidator(T valueSet, IValueSetOwner owner, ValueDatatype datatype) {
        super();
        this.valueSet = valueSet;
        this.owner = owner;
        this.datatype = datatype;
    }

    public T getValueSet() {
        return valueSet;
    }

    public IValueSetOwner getOwner() {
        return owner;
    }

    public ValueDatatype getDatatype() {
        return datatype;
    }

    public abstract MessageList validate();
}
