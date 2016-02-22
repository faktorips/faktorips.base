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

import com.google.common.base.Preconditions;

import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.model.productcmpt.ITemplatedValue;
import org.faktorips.devtools.core.ui.editors.productcmpt.TemplatedValueFormatter;

public class TemplateUsageViewItem {

    private final Object value;
    private final boolean sameAsTemplateValue;
    private final boolean deletedValue;
    private final BigDecimal distributionPercent;
    private final Collection<ITemplatedValue> children;

    public TemplateUsageViewItem(Object value, boolean sameAsTemplateValue, boolean deletedValue,
            BigDecimal distributionPercent, Set<ITemplatedValue> children) {
        Preconditions.checkArgument(!(sameAsTemplateValue && deletedValue));
        this.value = value;
        this.sameAsTemplateValue = sameAsTemplateValue;
        this.deletedValue = deletedValue;
        this.distributionPercent = distributionPercent;
        this.children = Collections.unmodifiableCollection(children);
    }

    public Object getValue() {
        return value;
    }

    public BigDecimal getRelativeDistributionPercent() {
        return distributionPercent;
    }

    public Collection<ITemplatedValue> getChildren() {
        return children;
    }

    public String getText() {
        final String formattedValue = getFormattedValue();
        final String distribution = getFormattedRelativeDistribution();
        if (isSameValueAsTemplateValue()) {
            return NLS.bind(Messages.TemplatePropertyUsageView_DifferingValues_sameValueLabel, formattedValue,
                    distribution);
        } else if (isDeletedValue()) {
            return NLS.bind(Messages.TemplatePropertyUsageView_DifferingValues_deletedValueLabel, distribution);
        } else {
            return NLS
                    .bind(Messages.TemplatePropertyUsageView_DifferingValues_valueLabel, formattedValue, distribution);
        }
    }

    public boolean isSameValueAsTemplateValue() {
        return sameAsTemplateValue;
    }

    public boolean isDeletedValue() {
        return deletedValue;
    }

    private String getFormattedValue() {
        ITemplatedValue pv = getFirstTemplatedValue();
        return TemplatedValueFormatter.shortedFormat(pv);
    }

    private ITemplatedValue getFirstTemplatedValue() {
        /*
         * There always is at least one value. Otherwise no item would show up in the distribution.
         */
        return children.iterator().next();
    }

    private String getFormattedRelativeDistribution() {
        return getRelativeDistributionPercent().stripTrailingZeros().toPlainString();
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