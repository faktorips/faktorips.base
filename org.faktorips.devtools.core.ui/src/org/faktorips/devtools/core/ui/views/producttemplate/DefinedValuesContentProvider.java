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
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.core.ui.util.TypedSelection;
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
        if (parentElement instanceof TemplateUsageViewItem) {
            TemplateUsageViewItem viewItem = (TemplateUsageViewItem)parentElement;
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

    private Function<Object, TemplateUsageViewItem> toViewItem() {
        return new Function<Object, TemplateUsageViewItem>() {

            @Override
            public TemplateUsageViewItem apply(Object value) {
                if (value == null) {
                    return null;
                }
                BigDecimal distributionPercent = new BigDecimal(getAbsoluteDistribution().get(value)).multiply(
                        new BigDecimal(100)).divide(new BigDecimal(pmo.getCount()), 1, BigDecimal.ROUND_HALF_UP);
                Set<IPropertyValue> children = getDefinedValuesHistorgram().getElements(value);
                return new TemplateUsageViewItem(value, pmo.getTemplateValue(), distributionPercent, children);
            }
        };
    }

    public Histogram<Object, IPropertyValue> getDefinedValuesHistorgram() {
        return pmo.getDefinedValuesHistogram();
    }

    public SortedMap<Object, Integer> getAbsoluteDistribution() {
        return pmo.getDefinedAbsoluteDistribution();
    }

    /**
     * Helper method to get the selected property values from a selection that might contain
     * {@link TemplateUsageViewItem} objects holding the property values.
     */
    public static Collection<IPropertyValue> getSelectedPropertyValues(ISelection currentSelection) {
        TypedSelection<IPropertyValue> propValueSelection = TypedSelection.createAnyCount(IPropertyValue.class,
                currentSelection);
        if (propValueSelection.isValid()) {
            return propValueSelection.getElements();
        } else {
            TypedSelection<TemplateUsageViewItem> viewItemSelection = TypedSelection.create(
                    TemplateUsageViewItem.class, currentSelection);
            if (viewItemSelection.isValid()) {
                TemplateUsageViewItem element = viewItemSelection.getElement();
                return element.getChildren();
            }
        }
        return Collections.emptyList();
    }

}
