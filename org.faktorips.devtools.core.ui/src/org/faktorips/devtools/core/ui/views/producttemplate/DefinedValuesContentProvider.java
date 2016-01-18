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

import static com.google.common.collect.Collections2.transform;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;

import com.google.common.base.Function;

import org.apache.commons.lang.ObjectUtils;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.core.ui.editors.productcmpt.PropertyValueFormatter;
import org.faktorips.devtools.core.util.Histogram;

/**
 * Provides the content for the right tree of the {@link TemplatePropertyUsageView}
 * 
 * The tree has two levels. The first level shows the distinct values including the over all
 * relative distribution. The second level shows the property values that have this value.
 */
public class DefinedValuesContentProvider implements ITreeContentProvider {

    private TemplatePropertyUsagePmo pmo;

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        if (newInput instanceof TemplatePropertyUsagePmo) {
            pmo = (TemplatePropertyUsagePmo)newInput;
        }
    }

    @Override
    public Object[] getElements(Object inputElement) {
        return transform(getAbsoluteDistribution().keySet(), toViewItem()).toArray();
    }

    @Override
    public Object[] getChildren(Object parentElement) {
        if (parentElement instanceof ValueViewItem) {
            ValueViewItem viewItem = (ValueViewItem)parentElement;
            return viewItem.getChildren().toArray();
        } else {
            return new Object[0];
        }
    }

    @Override
    public Object getParent(Object element) {
        for (Entry<Object, IPropertyValue> entry : getDefinedValuesHistorgram().getDistribution().entries()) {
            if (ObjectUtils.equals(element, entry.getValue())) {
                return toViewItem().apply(entry.getKey());
            }
        }
        return null;
    }

    @Override
    public boolean hasChildren(Object element) {
        return getChildren(element).length > 0;
    }

    @Override
    public void dispose() {
        // nothing to do
    }

    private Function<Object, ValueViewItem> toViewItem() {
        return new Function<Object, ValueViewItem>() {

            @Override
            public ValueViewItem apply(Object value) {
                if (value == null) {
                    return null;
                }
                BigDecimal distributionPercent = new BigDecimal(getAbsoluteDistribution().get(value)).multiply(
                        new BigDecimal(100)).divide(new BigDecimal(pmo.getCount()), 1, BigDecimal.ROUND_HALF_UP);
                Set<IPropertyValue> children = getDefinedValuesHistorgram().getElements(value);
                return new ValueViewItem(value, distributionPercent, children);
            }
        };
    }

    public Histogram<Object, IPropertyValue> getDefinedValuesHistorgram() {
        return pmo.getDefinedValuesHistogram();
    }

    public SortedMap<Object, Integer> getAbsoluteDistribution() {
        return pmo.getDefinedAbsoluteDistribution();
    }

    public static class ValueViewItem {

        private final Object value;

        private final BigDecimal distributionPercent;

        private final Collection<IPropertyValue> children;

        public ValueViewItem(Object value, BigDecimal distributionPercent, Set<IPropertyValue> children) {
            this.value = value;
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
            return getFormattedValue() + getFormattedRelDistribution();
        }

        private String getFormattedValue() {
            IPropertyValue pv = getFirstPropertyValue();
            return PropertyValueFormatter.format(pv);
        }

        private IPropertyValue getFirstPropertyValue() {
            /*
             * There always is at least one value. Otherwise no item would show up in the
             * distribution.
             */
            return children.iterator().next();
        }

        private String getFormattedRelDistribution() {
            return " (" + getRelDistributionPercent().stripTrailingZeros().toPlainString() + "%)"; //$NON-NLS-1$ //$NON-NLS-2$
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
            ValueViewItem other = (ValueViewItem)obj;
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

}
