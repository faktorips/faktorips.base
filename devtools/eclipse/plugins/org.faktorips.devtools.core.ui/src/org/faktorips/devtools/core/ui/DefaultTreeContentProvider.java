/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.model.IIpsElement;

public abstract class DefaultTreeContentProvider implements ITreeContentProvider {

    public DefaultTreeContentProvider() {
        super();
    }

    @Override
    public Object[] getChildren(Object parentElement) {
        if (!(parentElement instanceof IIpsElement)) {
            return new Object[0];
        }
        try {
            return ((IIpsElement)parentElement).getChildren();
        } catch (Exception e) {
            IpsPlugin.logAndShowErrorDialog(e);
            return new Object[0];
        }
    }

    @Override
    public Object getParent(Object element) {
        return ((IIpsElement)element).getParent();
    }

    @Override
    public boolean hasChildren(Object element) {
        try {
            return ((IIpsElement)element).hasChildren();
        } catch (Exception e) {
            IpsPlugin.logAndShowErrorDialog(e);
            return false;
        }
    }

    @Override
    public void dispose() {
        // Nothing to do
    }

}
