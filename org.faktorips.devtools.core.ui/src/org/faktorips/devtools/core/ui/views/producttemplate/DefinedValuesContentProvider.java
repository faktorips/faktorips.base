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
import java.util.Map.Entry;
import java.util.SortedMap;

import com.google.common.base.Function;

import org.apache.commons.lang.ObjectUtils;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.core.util.Histogram;

/**
 * Provides the content for the right tree of the {@link TemplatePropertyUsageView}
 * 
 * The tree has two levels. The first level shows the distinct values including the over all
 * relative distribution. The second level shows the property values that have this value.
 */
public class DefinedValuesContentProvider implements ITreeContentProvider {

    private Histogram<Object, IPropertyValue> definedValuesHistorgram;

    private SortedMap<Object, Integer> absoluteDistribution;

    private int inheritedCount;

    private int count;

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        if (newInput instanceof TemplatePropertyUsagePmo) {
            TemplatePropertyUsagePmo pmo = (TemplatePropertyUsagePmo)newInput;
            definedValuesHistorgram = pmo.getDefinedValuesHistogram();
            absoluteDistribution = definedValuesHistorgram.getAbsoluteDistribution();
            inheritedCount = pmo.getInheritingPropertyValues().size();
            count = inheritedCount + definedValuesHistorgram.countElements();
        }
    }

    @Override
    public Object[] getElements(Object inputElement) {
        return transform(absoluteDistribution.keySet(), toViewItem()).toArray();
    }

    @Override
    public Object[] getChildren(Object parentElement) {
        if (parentElement instanceof ValueViewItem) {
            ValueViewItem viewItem = (ValueViewItem)parentElement;
            return definedValuesHistorgram.getElements(viewItem.getValue()).toArray();
        } else {
            return new Object[0];
        }
    }

    @Override
    public Object getParent(Object element) {
        for (Entry<Object, IPropertyValue> entry : definedValuesHistorgram.getDistribution().entries()) {
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
                BigDecimal distributionPercent = new BigDecimal(absoluteDistribution.get(value)).multiply(
                        new BigDecimal(100)).divide(new BigDecimal(count), 1, BigDecimal.ROUND_HALF_UP);
                return new ValueViewItem(value, distributionPercent);
            }
        };
    }

    public static class ValueViewItem {

        private final Object value;

        private final BigDecimal distributionPercent;

        public ValueViewItem(Object value, BigDecimal distributionPercent) {
            this.value = value;
            this.distributionPercent = distributionPercent;
        }

        public Object getValue() {
            return value;
        }

        public BigDecimal getRelDistributionPercent() {
            return distributionPercent;
        }

    }

}
