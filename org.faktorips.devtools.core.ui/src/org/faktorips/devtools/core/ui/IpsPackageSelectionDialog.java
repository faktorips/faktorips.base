/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;

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
