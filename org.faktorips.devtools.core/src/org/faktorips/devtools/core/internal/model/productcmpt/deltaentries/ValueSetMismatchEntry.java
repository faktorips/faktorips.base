/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.productcmpt.deltaentries;

import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpt.DeltaType;
import org.faktorips.devtools.core.model.productcmpt.IConfiguredValueSet;

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
        String label = IpsPlugin.getMultiLanguageSupport().getLocalizedLabel(attribute);
        return NLS.bind(desc, new Object[] { label, attribute.getValueSet().getValueSetType().getName(),
                element.getValueSet().getValueSetType().getName() });
    }

}
