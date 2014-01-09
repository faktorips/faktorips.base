/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.workbenchadapters;

import org.eclipse.jface.resource.ImageDescriptor;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.productcmpt.ITableContentUsage;
import org.faktorips.util.StringUtil;

public class TableContentUsageWorkbenchAdapter extends DefaultIpsObjectPartWorkbenchAdapter {

    public TableContentUsageWorkbenchAdapter(ImageDescriptor imageDescriptor) {
        super(imageDescriptor);
    }

    @Override
    protected String getLabel(IIpsObjectPart ipsObjectPart) {
        if (!(ipsObjectPart instanceof ITableContentUsage)) {
            return super.getLabel(ipsObjectPart);
        }

        ITableContentUsage tableContentUsage = (ITableContentUsage)ipsObjectPart;
        String caption = IpsPlugin.getMultiLanguageSupport().getLocalizedCaption(tableContentUsage);
        return caption + ": " + StringUtil.unqualifiedName(tableContentUsage.getTableContentName()); //$NON-NLS-1$
    }

}
