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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.model.IWorkbenchAdapter2;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.model.IIpsElement;

public abstract class IpsElementWorkbenchAdapter implements IWorkbenchAdapter, IWorkbenchAdapter2 {

    @Override
    public Object[] getChildren(Object o) {
        if (o instanceof IIpsElement) {
            IIpsElement ipsElement = (IIpsElement)o;
            try {
                return ipsElement.getChildren();
            } catch (CoreException e) {
                IpsPlugin.log(e);
                return new Object[0];
            }
        } else {
            return null;
        }
    }

    @Override
    public final ImageDescriptor getImageDescriptor(Object object) {
        if (object instanceof IIpsElement) {
            IIpsElement ipsElement = (IIpsElement)object;
            return getImageDescriptor(ipsElement);
        } else {
            return null;
        }
    }

    protected abstract ImageDescriptor getImageDescriptor(IIpsElement ipsElement);

    public abstract ImageDescriptor getDefaultImageDescriptor();

    @Override
    public final String getLabel(Object o) {
        if (o instanceof IIpsElement) {
            IIpsElement ipsElement = (IIpsElement)o;
            return getLabel(ipsElement);
        } else {
            return ""; //$NON-NLS-1$
        }
    }

    /**
     * Returns the given {@link IIpsElement}'s native name.
     * 
     * @param ipsElement the {@link IIpsElement} to return a label for
     * @return the ips element's name
     */
    protected String getLabel(IIpsElement ipsElement) {
        return ipsElement.getName();
    }

    @Override
    public Object getParent(Object o) {
        if (o instanceof IIpsElement) {
            IIpsElement ipsElement = (IIpsElement)o;
            return ipsElement.getParent();
        } else {
            return null;
        }
    }

    @Override
    public RGB getBackground(Object element) {
        return null;
    }

    @Override
    public FontData getFont(Object element) {
        return null;
    }

    @Override
    public RGB getForeground(Object element) {
        return null;
    }

}
