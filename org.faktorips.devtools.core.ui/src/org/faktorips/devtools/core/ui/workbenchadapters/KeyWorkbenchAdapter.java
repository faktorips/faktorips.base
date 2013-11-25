/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
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
        if (key instanceof IIndex) {
            IIndex index = (IIndex)key;
            if (!index.isUniqueKey()) {
                return TABLE_KEY_NON_UNIQUE;
            }
        }
        return TABLE_KEY;
    }

    @Override
    public ImageDescriptor getDefaultImageDescriptor() {
        return IpsUIPlugin.getImageHandling().getSharedImageDescriptor(TABLE_KEY, true);
    }

}
