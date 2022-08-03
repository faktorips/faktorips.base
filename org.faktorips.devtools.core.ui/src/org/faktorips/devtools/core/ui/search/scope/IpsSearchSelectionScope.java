/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.search.scope;

import java.util.List;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

/**
 * Scope for Selection
 * 
 * @author dicker
 */
public class IpsSearchSelectionScope extends AbstractIpsSearchScope {

    ISelection selection;

    public IpsSearchSelectionScope(ISelection selection) {
        this.selection = selection;
    }

    @Override
    protected String getScopeTypeLabel(boolean singular) {
        return ""; //$NON-NLS-1$
    }

    @Override
    protected List<?> getSelectedObjects() {
        return ((IStructuredSelection)selection).toList();
    }

}
