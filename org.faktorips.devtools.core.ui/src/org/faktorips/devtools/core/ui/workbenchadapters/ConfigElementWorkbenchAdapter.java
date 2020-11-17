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
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.productcmpt.IConfigElement;
import org.faktorips.devtools.model.productcmpt.IConfiguredDefault;
import org.faktorips.devtools.model.productcmpt.IConfiguredValueSet;

public class ConfigElementWorkbenchAdapter extends IpsObjectPartWorkbenchAdapter {

    @Override
    protected ImageDescriptor getImageDescriptor(IIpsObjectPart ipsObjectPart) {
        if (ipsObjectPart instanceof IConfiguredDefault) {
            return getConfiguredDefaultImageDescriptor();
        } else if (ipsObjectPart instanceof IConfiguredValueSet) {
            return getConfiguredValueSetImageDescriptor();
        } else {
            return null;
        }
    }

    @Override
    public ImageDescriptor getDefaultImageDescriptor() {
        return IpsUIPlugin.getImageHandling().createImageDescriptor("ConfigElement.gif"); //$NON-NLS-1$
    }

    public ImageDescriptor getConfiguredDefaultImageDescriptor() {
        return IpsUIPlugin.getImageHandling().createImageDescriptor("ConfiguredDefault.gif"); //$NON-NLS-1$
    }

    public ImageDescriptor getConfiguredValueSetImageDescriptor() {
        return IpsUIPlugin.getImageHandling().createImageDescriptor("ConfiguredValueSet.gif"); //$NON-NLS-1$
    }

    @Override
    protected String getLabel(IIpsObjectPart ipsObjectPart) {
        String caption = IIpsModel.get().getMultiLanguageSupport().getLocalizedCaption(ipsObjectPart);
        Object value = ((IConfigElement)ipsObjectPart).getPropertyValue();
        if (value instanceof String) {
            ValueDatatype datatype = ((IConfigElement)ipsObjectPart).findValueDatatype(ipsObjectPart.getIpsProject());
            if (datatype != null) {
                value = IpsUIPlugin.getDefault().getDatatypeFormatter().formatValue(datatype, (String)value);
            }
            return caption + ": " + value; //$NON-NLS-1$
        } else {
            return caption;
        }
    }
}
