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
import org.faktorips.devtools.model.decorators.IIpsElementDecorator;

/**
 * A workbench adapter that wraps an {@link IIpsElementDecorator}.
 *
 * @since 21.6
 */
public class DecoratedWorkbenchAdapter extends IpsElementWorkbenchAdapter {

    private final IIpsElementDecorator decorator;

    public DecoratedWorkbenchAdapter(Class<? extends IIpsElement> adaptableClass) {
        decorator = IIpsDecorators.get(adaptableClass);
    }

    @Override
    protected ImageDescriptor getImageDescriptor(IIpsElement ipsElement) {
        return decorator.getImageDescriptor(ipsElement);
    }

    @Override
    public ImageDescriptor getDefaultImageDescriptor() {
        return decorator.getDefaultImageDescriptor();
    }

    @Override
    protected String getLabel(IIpsElement ipsElement) {
        return decorator.getLabel(ipsElement);
    }
}
