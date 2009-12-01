/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
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

    private static final Object[] EMPTY_ARRAY = new Object[0];

    public final Object[] getChildren(Object o) {
        if (o instanceof IIpsElement) {
            IIpsElement ipsElement = (IIpsElement)o;
            try {
                return getChildren(ipsElement);
            } catch (CoreException e) {
                IpsPlugin.log(e);
            }
        }
        return EMPTY_ARRAY;
    }

    protected IIpsElement[] getChildren(IIpsElement ipsElement) throws CoreException {
        return ipsElement.getChildren();
    }

    public final ImageDescriptor getImageDescriptor(Object object) {
        if (object instanceof IIpsElement) {
            IIpsElement ipsElement = (IIpsElement)object;
            return getImageDescriptor(ipsElement);
        }
        return null;
    }

    protected abstract ImageDescriptor getImageDescriptor(IIpsElement ipsElement);

    public final String getLabel(Object object) {
        if (object instanceof IIpsElement) {
            IIpsElement ipsElement = (IIpsElement)object;
            return getLabel(ipsElement);
        }
        return null;
    }

    protected abstract String getLabel(IIpsElement ipsElement);

    public final Object getParent(Object o) {
        if (o instanceof IIpsElement) {
            IIpsElement ipsElement = (IIpsElement)o;
            return getParent(ipsElement);
        }
        return null;
    }

    protected IIpsElement getParent(IIpsElement ipsElement) {
        return ipsElement.getParent();
    }

    public RGB getBackground(Object element) {
        if (element instanceof IIpsElement) {
            IIpsElement ipsElement = (IIpsElement)element;
            return getBackground(ipsElement);
        }
        return null;
    }

    protected RGB getBackground(IIpsElement ipsElement) {
        return null;
    }

    public FontData getFont(Object element) {
        if (element instanceof IIpsElement) {
            IIpsElement ipsElement = (IIpsElement)element;
            return getFont(ipsElement);
        }
        return null;
    }

    protected FontData getFont(IIpsElement ipsElement) {
        return null;
    }

    public RGB getForeground(Object element) {
        if (element instanceof IIpsElement) {
            IIpsElement ipsElement = (IIpsElement)element;
            return getForeground(ipsElement);
        }
        return null;
    }

    protected RGB getForeground(IIpsElement ipsElement) {
        return null;
    }

}
