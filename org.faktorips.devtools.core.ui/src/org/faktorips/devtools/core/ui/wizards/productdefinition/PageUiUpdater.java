/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.wizards.productdefinition;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.jface.wizard.WizardPage;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;

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
