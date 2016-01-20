/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.ui.views.producttemplate;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.core.ui.editors.productcmpt.PropertyValueFormatter;

public class TemplateUsageViewItem {

    private final Object value;

    private final boolean sameAsTemplateValue;

    private final BigDecimal distributionPercent;

    private final Collection<IPropertyValue> children;

    public TemplateUsageViewItem(Object value, Object templateValue, BigDecimal distributionPercent,
            Set<IPropertyValue> children) {
        this.value = value;
        this.sameAsTemplateValue = ObjectUtils.equals(value, templateValue);
        this.distributionPercent = distributionPercent;
        this.children = Collections.unmodifiableCollection(children);
    }

    public Object getValue() {
        return value;
    }

    public BigDecimal getRelDistributionPercent() {
        return distributionPercent;
    }

    public Collection<IPropertyValue> getChildren() {
        return children;
    }

    public String getText() {
        final String formattedValue = getFormattedValue();
        final String distribution = getFormattedRelDistribution();
        if (isSameValueAsTemplateValue()) {
            return NLS.bind(Messages.TemplatePropertyUsageView_DifferingValues_sameValueLabel, formattedValue, distribution);
        } else {
            return NLS.bind(Messages.TemplatePropertyUsageView_DifferingValues_valueLabel, formattedValue, distribution);
        }
    }

    public boolean isSameValueAsTemplateValue() {
        return sameAsTemplateValue;
    }

    private String getFormattedValue() {
        IPropertyValue pv = getFirstPropertyValue();
        return PropertyValueFormatter.format(pv);
    }

    private IPropertyValue getFirstPropertyValue() {
        /*
         * There always is at least one value. Otherwise no item would show up in the distribution.
         */
        return children.iterator().next();
    }

    private String getFormattedRelDistribution() {
        return getRelDistributionPercent().stripTrailingZeros().toPlainString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        TemplateUsageViewItem other = (TemplateUsageViewItem)obj;
        if (value == null) {
            if (other.value != null) {
                return false;
            }
        } else if (!value.equals(other.value)) {
            return false;
        }
        return true;
    }

}