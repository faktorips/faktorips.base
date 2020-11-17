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
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;

/**
 * Default workbench adapter for all kinds of {@link IIpsObjectPart IIpsObjectParts}
 */
public class DefaultIpsObjectPartWorkbenchAdapter extends IpsObjectPartWorkbenchAdapter {

    private ImageDescriptor imageDescriptor;

    public DefaultIpsObjectPartWorkbenchAdapter(ImageDescriptor imageDescriptor) {
        super();
        this.imageDescriptor = imageDescriptor;
    }

    @Override
    protected ImageDescriptor getImageDescriptor(IIpsObjectPart ipsObjectPart) {
        return imageDescriptor;
    }

    @Override
    protected String getLabel(IIpsObjectPart ipsObjectPart) {
        return ipsObjectPart.getName();
    }

    @Override
    public ImageDescriptor getDefaultImageDescriptor() {
        return imageDescriptor;
    }

}
