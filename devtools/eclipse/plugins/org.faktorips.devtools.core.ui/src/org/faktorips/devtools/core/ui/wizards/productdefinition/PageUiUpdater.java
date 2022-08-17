/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.productdefinition;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.jface.wizard.WizardPage;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;

public abstract class PageUiUpdater implements PropertyChangeListener {

    private final WizardPage page;

    public PageUiUpdater(WizardPage page) {
        this.page = page;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        updatePageMessages();
        updatePageComplete();
    }

    protected void updatePageMessages() {
        MessageList messageList = validatePage();
        Message message = messageList.getMessageWithHighestSeverity();
        if (message != null) {
            getPage().setMessage(message.getText(), UIToolkit.convertToJFaceSeverity(message.getSeverity()));
        } else {
            getPage().setMessage(null);
        }
    }

    public void updateUI() {
        updatePageComplete();
    }

    void updatePageComplete() {
        MessageList messageList = validatePage();
        getPage().setPageComplete(!messageList.containsErrorMsg());
    }

    protected abstract MessageList validatePage();

    /**
     * @return Returns the page.
     */
    public WizardPage getPage() {
        return page;
    }

}
