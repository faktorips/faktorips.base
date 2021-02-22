/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.ui.workbenchadapters;

import org.eclipse.jface.resource.ImageDescriptor;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.decorators.IIpsDecorators;

public class DecoratedWorkbenchAdapter extends IpsElementWorkbenchAdapter {

    private final Class<? extends IIpsElement> clazz;

    public DecoratedWorkbenchAdapter(Class<? extends IIpsElement> adaptableClass) {
        this.clazz = adaptableClass;
    }

    @Override
    protected ImageDescriptor getImageDescriptor(IIpsElement ipsElement) {
        return IIpsDecorators.get(clazz).getImageDescriptor(ipsElement);
    }

    @Override
    public ImageDescriptor getDefaultImageDescriptor() {
        return IIpsDecorators.get(clazz).getDefaultImageDescriptor();
    }

    @Override
    protected String getLabel(IIpsElement ipsElement) {
        return IIpsDecorators.get(clazz).getLabel(ipsElement);
    }
}
