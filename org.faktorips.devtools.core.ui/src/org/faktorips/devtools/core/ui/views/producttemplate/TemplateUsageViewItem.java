/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.views.producttemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.Optional;
import java.util.SortedMap;

import org.apache.commons.lang.StringUtils;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.ui.editors.productcmpt.TemplatedValueFormatter;
import org.faktorips.devtools.model.internal.util.Histogram;
import org.faktorips.devtools.model.productcmpt.template.ITemplatedValue;
import org.faktorips.devtools.model.productcmpt.template.TemplateValueStatus;

public class TemplateUsageViewItem {

    private final Object value;
    private final TemplatePropertyUsagePmo pmo;
    private final Histogram<Object, ITemplatedValue> histogram;

    public TemplateUsageViewItem(Object value, TemplatePropertyUsagePmo pmo,
            Histogram<Object, ITemplatedValue> histogram) {
        this.value = value;
        this.pmo = pmo;
        this.histogram = histogram;
    }

    public Object getValue() {
        return value;
    }

    public Collection<ITemplatedValue> getChildren() {
        return histogram.getElements(value);
    }

    public String getText() {
        final String formattedValue = getFormattedValue();
        final String distribution = getFormattedRelativeDistribution();
        if (isSameValueAsTemplateValue()) {
            return getSameValueItemText(formattedValue, distribution);
        } else if (isDeletedValue()) {
            return NLS.bind(Messages.TemplatePropertyUsageView_DifferingValues_deletedValueLabel, distribution);
        } else {
            return NLS.bind(Messages.TemplatePropertyUsageView_DifferingValues_valueLabel, formattedValue,
                    distribution);
        }
    }

    private String getSameValueItemText(final String formattedValue, final String distribution) {
        if (pmo.showValues()) {
            return NLS.bind(Messages.TemplatePropertyUsageView_DifferingValues_sameValueLabel, formattedValue,
                    distribution);
        } else {
            return NLS.bind(Messages.TemplatePropertyUsageView_DifferingValues_sameValueLabel, StringUtils.EMPTY,
                    distribution).trim();
        }
    }

    public boolean isSameValueAsTemplateValue() {
        Object templateValue = pmo.getActualTemplateValue();
        return pmo.getValueComparator().compare(value, templateValue) == 0;
    }

    public boolean isDeletedValue() {
        return getFirstTemplatedValue().getTemplateValueStatus() == TemplateValueStatus.UNDEFINED;
    }

    private String getFormattedValue() {
        ITemplatedValue pv = getFirstTemplatedValue();
        return TemplatedValueFormatter.shortedFormat(pv);
    }

    private ITemplatedValue getFirstTemplatedValue() {
        /*
         * There always is at least one value. Otherwise no item would show up in the distribution.
         */
        return getChildren().iterator().next();
    }

    private String getFormattedRelativeDistribution() {
        return getRelativeDistributionPercent().stripTrailingZeros().toPlainString();
    }

    public BigDecimal getRelativeDistributionPercent() {
        SortedMap<Object, Integer> definedAbsoluteDistribution = histogram.getAbsoluteDistribution();
        Integer definedDist = Optional.ofNullable(definedAbsoluteDistribution.get(value)).orElse(0);
        BigDecimal distributionPercent = new BigDecimal(definedDist).multiply(new BigDecimal(100))
                .divide(new BigDecimal(pmo.getCount()), 1, RoundingMode.HALF_UP);
        return distributionPercent;
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