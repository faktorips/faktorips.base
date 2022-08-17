/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.decorators.internal;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;
import org.faktorips.devtools.model.decorators.IIpsDecorators;
import org.faktorips.devtools.model.decorators.IIpsObjectPartDecorator;
import org.faktorips.devtools.model.decorators.OverlayIcons;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.productcmpttype.ITableStructureUsage;

/**
 * The workbench adapter for {@link ITableStructureUsage}.
 */
public class TableStructureUsageDecorator extends SimpleIpsElementDecorator implements IIpsObjectPartDecorator {

    public static final String BASE_IMAGE = "TableStructure.gif"; //$NON-NLS-1$

    public TableStructureUsageDecorator() {
        super(BASE_IMAGE);
    }

    @Override
    public ImageDescriptor getImageDescriptor(IIpsObjectPart ipsObjectPart) {
        if (ipsObjectPart instanceof ITableStructureUsage) {
            ITableStructureUsage tableStructureUsage = (ITableStructureUsage)ipsObjectPart;
            String baseImage = BASE_IMAGE;
            String[] overlays = new String[4];

            if (!tableStructureUsage.isChangingOverTime()) {
                overlays[IDecoration.TOP_LEFT] = OverlayIcons.STATIC;
            }
            if (tableStructureUsage.isDeprecated()) {
                overlays[IDecoration.BOTTOM_LEFT] = OverlayIcons.DEPRECATED;
            }
            return IIpsDecorators.getImageHandling().getSharedOverlayImageDescriptor(baseImage, overlays);
        }
        return getDefaultImageDescriptor();
    }
}
