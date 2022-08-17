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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.SortedMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.faktorips.devtools.core.ui.util.TypedSelection;
import org.faktorips.devtools.model.internal.util.Histogram;
import org.faktorips.devtools.model.productcmpt.template.ITemplatedValue;

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
        if (pmo.hasData()) {

            Histogram<Object, ITemplatedValue> histogram = pmo.getDefinedValuesHistogram();
            SortedMap<Object, Integer> definedAbsoluteDistribution = histogram.getAbsoluteDistribution();
            ImmutableList<TemplateUsageViewItem> elements = definedAbsoluteDistribution.keySet().stream()
                    .map(toViewItem(histogram))
                    .collect(Collectors.collectingAndThen(Collectors.toList(), ImmutableList::copyOf));
            return getOrdering(elements).sortedCopy(elements).toArray();
        } else {
            return ArrayUtils.EMPTY_OBJECT_ARRAY;
        }
    }

    protected Ordering<TemplateUsageViewItem> getOrdering(ImmutableList<TemplateUsageViewItem> elements) {
        final Ordering<TemplateUsageViewItem> secOrder = Ordering.explicit(elements);

        return Ordering.from((o1, o2) -> {
            if (o1.isSameValueAsTemplateValue()) {
                return -1;
            } else if (o2.isSameValueAsTemplateValue()) {
                return 1;
            } else if (o1.isDeletedValue()) {
                return -1;
            } else if (o2.isDeletedValue()) {
                return 1;
            } else {
                return secOrder.compare(o1, o2);
            }
        });
    }

    @Override
    public Object[] getChildren(Object parentElement) {
        if (parentElement instanceof TemplateUsageViewItem) {
            TemplateUsageViewItem viewItem = (TemplateUsageViewItem)parentElement;
            Collection<ITemplatedValue> values = viewItem.getChildren();
            ITemplatedValue[] children = values.toArray(new ITemplatedValue[values.size()]);
            Arrays.sort(children, new TemplatedValueContainerNameComparator());
            return children;
        } else {
            return ArrayUtils.EMPTY_OBJECT_ARRAY;
        }
    }

    /**
     * Not needed because we have only two levels
     */
    @Override
    public Object getParent(Object element) {
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

    private Function<Object, TemplateUsageViewItem> toViewItem(final Histogram<Object, ITemplatedValue> histogram) {
        return value -> value == null ? null : new TemplateUsageViewItem(value, pmo, histogram);
    }

    /**
     * Helper method to get the selected templated values from a selection that might contain
     * {@link TemplateUsageViewItem} objects holding the property values.
     */
    public static Collection<ITemplatedValue> getSelectedTemplatedValues(ISelection currentSelection) {
        TypedSelection<ITemplatedValue> propValueSelection = TypedSelection.createAnyCount(ITemplatedValue.class,
                currentSelection);
        if (propValueSelection.isValid()) {
            return propValueSelection.getElements();
        } else {
            TypedSelection<TemplateUsageViewItem> viewItemSelection = TypedSelection.create(TemplateUsageViewItem.class,
                    currentSelection);
            if (viewItemSelection.isValid()) {
                TemplateUsageViewItem element = viewItemSelection.getElement();
                return element.getChildren();
            }
        }
        return Collections.emptyList();
    }

}
