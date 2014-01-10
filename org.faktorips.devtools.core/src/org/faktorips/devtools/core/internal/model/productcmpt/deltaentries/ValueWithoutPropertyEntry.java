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

package org.faktorips.devtools.core.internal.model.productcmpt.deltaentries;

import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.productcmpt.DeltaType;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValue;

/**
 * 
 * @author Jan Ortmann
 */
public class ValueWithoutPropertyEntry extends AbstractDeltaEntryForProperty {

    private final IPropertyValue value;

    public ValueWithoutPropertyEntry(IPropertyValue value) {
        super(value);
        this.value = value;
    }

    @Override
    public String getDescription() {
        return IpsPlugin.getMultiLanguageSupport().getLocalizedCaption(value);
    }

    @Override
    public DeltaType getDeltaType() {
        return DeltaType.VALUE_WITHOUT_PROPERTY;
    }

    @Override
    public void fix() {
        value.delete();
    }

}
