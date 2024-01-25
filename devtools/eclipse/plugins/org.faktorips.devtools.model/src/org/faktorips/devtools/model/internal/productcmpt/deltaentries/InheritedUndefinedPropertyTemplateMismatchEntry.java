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
import org.faktorips.devtools.model.productcmpt.DeltaType;
import org.faktorips.devtools.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.model.productcmpt.template.TemplateValueStatus;
import org.faktorips.devtools.model.type.IProductCmptProperty;

/**
 * A property value should inherit its configuration from a template but is undefined in the
 * template.
 *
 * @since 24.1.2
 */
public class InheritedUndefinedPropertyTemplateMismatchEntry extends AbstractDeltaEntryForProperty {

    private IProductCmptProperty property;
    private IPropertyValue propertyValue;

    public InheritedUndefinedPropertyTemplateMismatchEntry(IProductCmptProperty property,
            IPropertyValue propertyValue) {
        super(propertyValue);
        this.property = property;
        this.propertyValue = propertyValue;
    }

    @Override
    public void fix() {
        propertyValue.setTemplateValueStatus(TemplateValueStatus.DEFINED);
    }

    @Override
    public DeltaType getDeltaType() {
        return DeltaType.INHERITED_UNDEFINED_TEMPLATE_MISMATCH;
    }

    @Override
    public String getDescription() {
        return MessageFormat.format(Messages.InheritedUndefinedTemplateMismatchEntry_desc,
                IIpsModel.get().getMultiLanguageSupport().getLocalizedLabel(property));
    }

}
