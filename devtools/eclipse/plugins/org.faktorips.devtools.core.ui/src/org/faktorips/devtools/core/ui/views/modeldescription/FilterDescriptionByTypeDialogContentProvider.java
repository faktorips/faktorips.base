/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.views.modeldescription;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.faktorips.devtools.model.pctype.IValidationRule;
import org.faktorips.devtools.model.productcmpttype.ITableStructureUsage;
import org.faktorips.devtools.model.type.IAssociation;
import org.faktorips.devtools.model.type.IAttribute;
import org.faktorips.devtools.model.type.IMethod;
import org.faktorips.devtools.model.type.ITypePart;
import org.faktorips.runtime.model.type.PolicyCmptType;
import org.faktorips.runtime.model.type.ProductCmptType;

/**
 * Provides content for the {@link FilterDescriptionsByTypeDialog}.
 */
public class FilterDescriptionByTypeDialogContentProvider implements ITreeContentProvider {

    public FilterDescriptionByTypeDialogContentProvider() {
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

    /**
     * Returns an array of parts based on the current IpsObjectType. {@link PolicyCmptType} does not
     * include {@link ITableStructureUsage} descriptions. {@link ProductCmptType} does not include
     * {@link IValidationRule} These parts will be used to create the content of
     * {@link FilterDescriptionsByTypeDialog}
     */
    @Override
    public Object[] getElements(Object inputElement) {
        List<Class<? extends ITypePart>> parts = new ArrayList<>();
        parts.add(IAssociation.class);
        parts.add(IAttribute.class);
        parts.add(IMethod.class);
        parts.add(IValidationRule.class);
        parts.add(ITableStructureUsage.class);
        return parts.toArray();
    }

    @Override
    public void dispose() {
        // Nothing to do.
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        // Nothing to do.
    }

}
