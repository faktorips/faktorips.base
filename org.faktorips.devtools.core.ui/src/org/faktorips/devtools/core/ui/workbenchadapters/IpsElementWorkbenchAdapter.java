/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.workbenchadapters;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.model.IWorkbenchAdapter2;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;

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
