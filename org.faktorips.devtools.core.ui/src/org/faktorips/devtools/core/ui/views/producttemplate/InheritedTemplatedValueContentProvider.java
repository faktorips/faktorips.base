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

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.faktorips.devtools.model.productcmpt.template.ITemplatedValue;

/**
 * Provides the content for the left tree of the {@link TemplatePropertyUsageView}.
 * 
 * The tree is just a list (only one level) containing all property values that inherit the value
 * from the initial template property value.
 */
public class InheritedTemplatedValueContentProvider implements ITreeContentProvider {

    private TemplatePropertyUsagePmo pmo;

    @Override
    public Object[] getElements(Object inputElement) {
        if (pmo != null) {
            Collection<ITemplatedValue> propertyValues = pmo.getInheritingTemplatedValues();
            ITemplatedValue[] elements = propertyValues.toArray(new ITemplatedValue[propertyValues.size()]);
            Arrays.sort(elements, new TemplatedValueContainerNameComparator());
            return elements;
        } else {
            return ArrayUtils.EMPTY_OBJECT_ARRAY;
        }
    }

    @Override
    public void dispose() {
        // nothing to do
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        if (newInput instanceof TemplatePropertyUsagePmo) {
            pmo = (TemplatePropertyUsagePmo)newInput;
        }
    }

    @Override
    public Object[] getChildren(Object parentElement) {
        return new Object[0];
    }

    @Override
    public Object getParent(Object element) {
        return null;
    }

    @Override
    public boolean hasChildren(Object element) {
        return false;
    }

}