/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.workbenchadapters;

import org.eclipse.jface.resource.ImageDescriptor;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.util.StringUtil;

public class DefaultIpsObjectWorkbenchAdapter extends IpsObjectWorkbenchAdapter {

    private final ImageDescriptor imageDescriptor;

    public DefaultIpsObjectWorkbenchAdapter(ImageDescriptor imageDescriptor) {
        super();
        this.imageDescriptor = imageDescriptor;
    }

    @Override
    protected ImageDescriptor getImageDescriptor(IIpsSrcFile ipsSrcFile) {
        return imageDescriptor;
    }

    @Override
    protected ImageDescriptor getImageDescriptor(IIpsObject ipsObject) {
        return imageDescriptor;
    }

    @Override
    public ImageDescriptor getDefaultImageDescriptor() {
        return imageDescriptor;
    }

    @Override
    protected String getLabel(IIpsSrcFile ipsSrcFile) {
        return StringUtil.getFilenameWithoutExtension(ipsSrcFile.getName());
    }

    @Override
    protected String getLabel(IIpsObject ipsObject) {
        return ipsObject.getName();
    }

}
