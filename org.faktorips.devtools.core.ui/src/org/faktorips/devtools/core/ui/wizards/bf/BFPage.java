/***************************************************************************************************
 * Copyright (c) 2005-2008 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 * 
 **************************************************************************************************/

package org.faktorips.devtools.core.ui.wizards.bf;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.faktorips.devtools.core.model.bf.BusinessFunctionIpsObjectType;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.wizards.IpsObjectPage;

/**
 * The wizard page for the new business function wizard.
 * 
 * @author Peter Erzberger
 */
public class BFPage extends IpsObjectPage {

    public BFPage(IStructuredSelection selection) {
        super(BusinessFunctionIpsObjectType.getInstance(), selection, Messages.getString("BFPage.Title")); //$NON-NLS-1$
        setImageDescriptor(IpsUIPlugin.getDefault().getImageDescriptor("wizards/NewBusinessFunctionWizard.png")); //$NON-NLS-1$
    }
}
