/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controls.tableedit;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.faktorips.devtools.core.ui.dialogs.MultiValueTableModel;

/**
 * Content provider for a {@link EditTableControlViewer} that uses an {@link MultiValueTableModel} .
 * 
 * @author Stefan Widmaier
 */
public class ListTableModelContentProvider implements IStructuredContentProvider {

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        // nothing to do
    }

    @Override
    public void dispose() {
        // nothing to do
    }

    @Override
    public Object[] getElements(Object inputElement) {
        IEditTableModel<?> model = (IEditTableModel<?>)inputElement;
        return model.getElements().toArray();
    }

}
