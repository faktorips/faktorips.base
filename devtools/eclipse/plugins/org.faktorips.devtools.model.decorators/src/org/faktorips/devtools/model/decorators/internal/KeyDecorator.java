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
import org.faktorips.devtools.model.decorators.IIpsDecorators;
import org.faktorips.devtools.model.decorators.IIpsObjectPartDecorator;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.tablestructure.IIndex;
import org.faktorips.devtools.model.tablestructure.IKey;

/**
 * WorkbenchAdapterProvider for ForeignKeys and Indices in TableStructure. It is the responsibility
 * of the user to register this adapter adequately.
 * 
 */
public class KeyDecorator implements IIpsObjectPartDecorator {

    public static final String TABLE_KEY = "TableKey.gif"; //$NON-NLS-1$
    public static final String TABLE_KEY_NON_UNIQUE = "TableKeyNonUnique.gif"; //$NON-NLS-1$

    @Override
    public ImageDescriptor getImageDescriptor(IIpsObjectPart ipsObjectPart) {
        if (ipsObjectPart instanceof IKey key) {
            String name = getImageName(key);
            return IIpsDecorators.getImageHandling().getSharedImageDescriptor(name, true);
        }
        return getDefaultImageDescriptor();
    }

    private String getImageName(IKey key) {
        if (isNonUniqueKey(key)) {
            return TABLE_KEY_NON_UNIQUE;
        } else {
            return TABLE_KEY;
        }
    }

    private boolean isNonUniqueKey(IKey key) {
        if (key instanceof IIndex index) {
            if (!index.isUniqueKey()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public ImageDescriptor getDefaultImageDescriptor() {
        return IIpsDecorators.getImageHandling().getSharedImageDescriptor(TABLE_KEY, true);
    }

}
