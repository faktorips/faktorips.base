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
import org.faktorips.devtools.model.productcmpt.PropertyValueType;
import org.faktorips.devtools.model.valueset.IValueSet;

/**
 * A value set should inherit its configuration from a template but the configuration differs.
 *
 * @since 24.1.1
 */
public class ValueSetTemplateMismatchEntry extends AbstractDeltaEntryForProperty {

    private final IPolicyCmptTypeAttribute attribute;
    private final IConfiguredValueSet element;
    private final IConfiguredValueSet templateElement;
    private final IValueSet actualValueSet;
    private final IValueSet templateValueSet;

    public ValueSetTemplateMismatchEntry(IPolicyCmptTypeAttribute attribute, IConfiguredValueSet element,
            IConfiguredValueSet templateElement, IValueSet actualValueSet, IValueSet templateValueSet) {
        super(element);
        this.attribute = attribute;
        this.element = element;
        this.templateElement = templateElement;
        this.actualValueSet = actualValueSet;
        this.templateValueSet = templateValueSet;
    }

    @Override
    public void fix() {
        PropertyValueType propertyValueType = PropertyValueType.CONFIGURED_VALUESET;
        propertyValueType.copyValue(templateElement, element);
    }

    @Override
    public DeltaType getDeltaType() {
        return DeltaType.INHERITED_TEMPLATE_MISMATCH;
    }

    @Override
    public String getDescription() {
        String desc = Messages.ValueSetTemplateMismatchEntry_desc;
        String label = IIpsModel.get().getMultiLanguageSupport().getLocalizedLabel(attribute);
        return MessageFormat.format(desc, label, actualValueSet.toShortString(), templateValueSet.toShortString());
    }

}
