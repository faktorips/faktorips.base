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
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.decorators.IIpsObjectPartDecorator;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.productcmpt.ITableContentUsage;
import org.faktorips.util.StringUtil;

public class TableContentUsageDecorator extends SimpleIpsElementDecorator implements IIpsObjectPartDecorator {

    public static final String TABLE_CONTENTS_USAGE_IMAGE = "TableContentsUsage.gif"; //$NON-NLS-1$

    public TableContentUsageDecorator() {
        super(TABLE_CONTENTS_USAGE_IMAGE);
    }

    @Override
    public ImageDescriptor getImageDescriptor(IIpsObjectPart ipsObjectPart) {
        return super.getImageDescriptor(ipsObjectPart);
    }

    @Override
    public String getLabel(IIpsObjectPart ipsObjectPart) {
        if (!(ipsObjectPart instanceof ITableContentUsage tableContentUsage)) {
            return super.getLabel(ipsObjectPart);
        }

        String caption = IIpsModel.get().getMultiLanguageSupport().getLocalizedCaption(tableContentUsage);
        return caption + ": " + StringUtil.unqualifiedName(tableContentUsage.getTableContentName()); //$NON-NLS-1$
    }

}
