/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.productcmpt.deltaentries;

import java.text.MessageFormat;

import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.model.productcmpt.DeltaType;
import org.faktorips.devtools.model.productcmpt.IConfiguredValueSet;

/**
 * 
 * @author Jan Ortmann
 */
public class ValueSetMismatchEntry extends AbstractDeltaEntryForProperty {

    private final IPolicyCmptTypeAttribute attribute;
    private final IConfiguredValueSet element;

    public ValueSetMismatchEntry(IPolicyCmptTypeAttribute attribute, IConfiguredValueSet element) {
        super(element);
        this.attribute = attribute;
        this.element = element;
    }

    @Override
    public void fix() {
        element.setValueSetCopy(attribute.getValueSet());
    }

    @Override
    public DeltaType getDeltaType() {
        return DeltaType.VALUE_SET_MISMATCH;
    }

    @Override
    public String getDescription() {
        String desc = Messages.ValueSetMismatchEntry_desc;
        String label = IIpsModel.get().getMultiLanguageSupport().getLocalizedLabel(attribute);
        return MessageFormat.format(desc, label, attribute.getValueSet().getValueSetType().getName(),
                element.getValueSet().getValueSetType().getName());
    }

}
