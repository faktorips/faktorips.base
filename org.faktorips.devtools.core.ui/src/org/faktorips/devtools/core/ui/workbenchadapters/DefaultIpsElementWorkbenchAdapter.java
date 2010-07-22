/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

import org.eclipse.jface.resource.ImageDescriptor;
import org.faktorips.devtools.core.model.IIpsElement;

public class DefaultIpsElementWorkbenchAdapter extends IpsElementWorkbenchAdapter {

    private final ImageDescriptor imageDescriptor;

    public DefaultIpsElementWorkbenchAdapter(ImageDescriptor imageDescriptor) {
        this.imageDescriptor = imageDescriptor;
    }

    @Override
    protected ImageDescriptor getImageDescriptor(IIpsElement ipsElement) {
        return imageDescriptor;
    }

    @Override
    public ImageDescriptor getDefaultImageDescriptor() {
        return imageDescriptor;
    }

}
