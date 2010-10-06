/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
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
