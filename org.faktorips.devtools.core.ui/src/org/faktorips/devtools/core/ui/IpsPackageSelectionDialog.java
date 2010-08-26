/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;

/**
 * Dialog to select an ips package fragment.
 */
public class IpsPackageSelectionDialog extends ElementListSelectionDialog {

    public IpsPackageSelectionDialog(Shell parent) {
        super(parent, new DefaultLabelProvider());
        setTitle(Messages.PdPackageSelectionDialog_title);
        setMessage(Messages.PdPackageSelectionDialog_description);
        setIgnoreCase(true);
        setMatchEmptyString(true);
        setMultipleSelection(false);
    }

    public IIpsPackageFragment getSelectedPackage() {
        if (getResult().length > 0) {
            return (IIpsPackageFragment)getResult()[0];
        }
        return null;
    }

}
