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
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.OverlayIcons;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.productcmpttype.ITableStructureUsage;

/**
 * The workbench adapter for {@link ITableStructureUsage}.
 */
public class TableStructureUsageWorkbenchAdapter extends DefaultIpsObjectPartWorkbenchAdapter {

    public static final String BASE_IMAGE = "TableStructure.gif"; //$NON-NLS-1$

    public TableStructureUsageWorkbenchAdapter(ImageDescriptor imageDescriptor) {
        super(imageDescriptor);
    }

    @Override
    protected ImageDescriptor getImageDescriptor(IIpsObjectPart ipsObjectPart) {
        if (ipsObjectPart instanceof ITableStructureUsage) {
            ITableStructureUsage tableStructureUsage = (ITableStructureUsage)ipsObjectPart;
            String baseImage = BASE_IMAGE;
            String[] overlays = new String[4];

            if (!tableStructureUsage.isChangingOverTime()) {
                overlays[0] = OverlayIcons.NOT_CHANGEOVERTIME_OVR;
            }
            return IpsUIPlugin.getImageHandling().getSharedOverlayImage(baseImage, overlays);
        }
        return getDefaultImageDescriptor();
    }
}
