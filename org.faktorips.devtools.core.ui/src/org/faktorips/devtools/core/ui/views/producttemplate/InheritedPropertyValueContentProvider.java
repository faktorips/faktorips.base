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

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * Provides the content for the left tree of the {@link TemplatePropertyUsageView}.
 * 
 * The tree is just a list (only one level) containing all property values that inherit the value
 * from the initial template property value.
 */
public class InheritedPropertyValueContentProvider implements ITreeContentProvider {

    private TemplatePropertyUsagePmo pmo;

    @Override
    public Object[] getElements(Object inputElement) {
        if (pmo != null) {
            return pmo.getInheritingPropertyValues().toArray();
        } else {
            return new Object[0];
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