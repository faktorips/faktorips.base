/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.search;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.ui.DefaultLabelProvider;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.model.IIpsElement;

/**
 * An implementation of the {@link ILabelProvider} for the model, product and reference search
 * 
 * @author dicker
 */
public class IpsSearchResultLabelProvider extends DefaultLabelProvider {

    private List<ILabelProviderListener> listeners = new ArrayList<>();

    @Override
    public void addListener(ILabelProviderListener listener) {
        listeners.add(listener);
    }

    @Override
    public void dispose() {
        listeners = null;
    }

    @Override
    public boolean isLabelProperty(Object element, String property) {
        return false;
    }

    /**
     * {@inheritDoc}
     * <p>
     * test
     */
    @Override
    public void removeListener(ILabelProviderListener listener) {
        listeners.remove(listener);
    }

    @Override
    public Image getImage(Object element) {
        if (element instanceof Object[]) {
            return IpsUIPlugin.getImageHandling().getImage((IIpsElement)((Object[])element)[0]);
        } else {
            return IpsUIPlugin.getImageHandling().getImage((IIpsElement)element);
        }
    }
}
