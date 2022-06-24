/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.preferencepages;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Property page for configuring IPS builder sets. Changes made in this page are persisted in an IPS
 * project`s configuration file (.ipsproject).
 * 
 * @author Roman Grutza
 */
public class BuilderSetPropertyPage extends IpsProjectPropertyPage {

    private BuilderSetContainer builderSetContainer;

    @Override
    protected Control createContents(Composite parent) {
        builderSetContainer = new BuilderSetContainer(getIpsProject());
        return builderSetContainer.createContents(parent);
    }

    @Override
    public void setVisible(boolean visible) {
        if (builderSetContainer != null) {
            if (!visible) {
                if (builderSetContainer.hasChangesInDialog()) {
                    String title = Messages.BuilderSetPropertyPage_saveDialog_Title;
                    String message = Messages.BuilderSetPropertyPage_saveDialog_Message;
                    String[] buttonLabels = new String[] { Messages.BuilderSetPropertyPage_saveDialog_Apply,
                            Messages.BuilderSetPropertyPage_saveDialog_Discard,
                            Messages.BuilderSetPropertyPage_saveDialog_ApplyLater };
                    MessageDialog dialog = new MessageDialog(getShell(), title, null, message, MessageDialog.QUESTION,
                            buttonLabels, 0);
                    int res = dialog.open();
                    if (res == 0) {
                        performOk();
                    } else if (res == 1) {
                        // discard
                        builderSetContainer.init(getIpsProject());
                    }
                    // else apply later
                }
            } else {
                if (builderSetContainer.hasChangesInDialog() && builderSetContainer.hasChangesInIpsprojectFile()) {
                    builderSetContainer.init(getIpsProject());
                }
            }
        }
        super.setVisible(visible);
    }

    @Override
    public boolean performOk() {
        if (builderSetContainer != null) {
            if (builderSetContainer.hasChangesInDialog()) {
                builderSetContainer.saveToIpsProjectFile();
            }
        }
        return super.performOk();
    }

    @Override
    protected void performDefaults() {
        super.performDefaults();
        if (builderSetContainer != null) {
            builderSetContainer.performDefaults();
        }
    }

}
