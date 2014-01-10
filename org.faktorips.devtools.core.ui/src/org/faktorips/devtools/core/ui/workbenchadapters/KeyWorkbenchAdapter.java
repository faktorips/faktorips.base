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
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.tablestructure.IIndex;
import org.faktorips.devtools.core.model.tablestructure.IKey;
import org.faktorips.devtools.core.ui.IpsUIPlugin;

/**
 * WorkbenchAdapterProvider for ForeignKeys and Indices in TableStructure. It is the responsibility
 * of the user to register this adapter adequately.
 * 
 */
public class KeyWorkbenchAdapter extends IpsObjectPartWorkbenchAdapter {

    public static final String TABLE_KEY = "TableKey.gif"; //$NON-NLS-1$
    public static final String TABLE_KEY_NON_UNIQUE = "TableKeyNonUnique.gif"; //$NON-NLS-1$

    @Override
    protected ImageDescriptor getImageDescriptor(IIpsObjectPart ipsObjectPart) {
        if (ipsObjectPart instanceof IKey) {
            IKey key = (IKey)ipsObjectPart;
            String name = getImageName(key);
            return IpsUIPlugin.getImageHandling().getSharedImageDescriptor(name, true);
        }
        return null;
    }

    private String getImageName(IKey key) {
        if (isNonUniqueKey(key)) {
            return TABLE_KEY_NON_UNIQUE;
        } else {
            return TABLE_KEY;
        }
    }

    private boolean isNonUniqueKey(IKey key) {
        if (key instanceof IIndex) {
            IIndex index = (IIndex)key;
            if (!index.isUniqueKey()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public ImageDescriptor getDefaultImageDescriptor() {
        return IpsUIPlugin.getImageHandling().getSharedImageDescriptor(TABLE_KEY, true);
    }

}
