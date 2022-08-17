/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.fields.MessageDecoration;
import org.faktorips.devtools.core.ui.forms.IpsSection;
import org.faktorips.devtools.model.internal.ipsobject.IpsObjectPartContainer;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;

/**
 * A section that is representing data of an {@link IpsObjectPartContainer}.
 * <p>
 * This kind of section provides the possibility of monitoring validation message codes. Should the
 * section encounter a monitored validation message code it will render an indicator image at the
 * section level.
 * 
 * @author Alexander Weickmann
 */
public abstract class IpsObjectPartContainerSection extends IpsSection {

    /**
     * Set containing all validation message codes that are monitored by this section meaning that
     * they will be indicated at the section level with an appropriate marker.
     */
    private final Set<String> monitoredValidationMessageCodes = new HashSet<>();

    private final IIpsObjectPartContainer ipsObjectPartContainer;

    private MessageDecoration messageDecoration;

    protected IpsObjectPartContainerSection(String id, IIpsObjectPartContainer ipsObjectPartContainer,
            Composite parent, int layoutData, UIToolkit toolkit) {

        super(id, parent, layoutData, toolkit);
        this.ipsObjectPartContainer = ipsObjectPartContainer;
    }

    protected IpsObjectPartContainerSection(IIpsObjectPartContainer ipsObjectPartContainer, Composite parent,
            int style, int layoutData, UIToolkit toolkit) {

        super(parent, style, layoutData, toolkit);
        this.ipsObjectPartContainer = ipsObjectPartContainer;
    }

    @Override
    protected void performRefresh() {
        refreshSectionMessageIndicator();
    }

    /**
     * Refreshes the message indicator that is attached to the section.
     */
    private void refreshSectionMessageIndicator() {
        MessageList filteredMessageList = new MessageList();
        if (!(monitoredValidationMessageCodes.isEmpty())) {
            MessageList validationMessageList = ipsObjectPartContainer.validate(ipsObjectPartContainer
                    .getIpsProject());
            for (Message message : validationMessageList) {
                if (monitoredValidationMessageCodes.contains(message.getCode())) {
                    filteredMessageList.add(message);
                }
            }
        }
        if (messageDecoration == null) {
            messageDecoration = getToolkit().createMessageDecoration(getClientComposite(), SWT.LEFT | SWT.TOP);
        }
        messageDecoration.setMessageList(filteredMessageList);
    }

    /**
     * Adds the given validation message code to this section's monitored validation message codes.
     * <p>
     * Should the section encounter a monitored validation error it will render an appropriate
     * indicator image at the section level.
     * <p>
     * Returns true if the validation message code was successfully added or false if the code is
     * already monitored.
     * 
     * @param validationMessageCode The validation message code that should be monitored from now on
     *            by this section
     */
    protected final boolean addMonitoredValidationMessageCode(String validationMessageCode) {
        return monitoredValidationMessageCodes.add(validationMessageCode);
    }

    /**
     * Removes the given validation message code from this section's monitored validation message
     * codes.
     * <p>
     * The section therefore will no longer render an indicator image at the section level if the
     * validation error is encountered.
     * <p>
     * Returns true if the validation message code was successfully removed or false if the code
     * wasn't monitored in the first place.
     * 
     * @param validationMessageCode The validation message code that will no longer be monitored by
     *            this section
     */
    protected final boolean removeMonitoredValidationMessageCode(String validationMessageCode) {
        return monitoredValidationMessageCodes.remove(validationMessageCode);
    }

    /**
     * Returns an unmodifiable view on this section's set of monitored validation message codes.
     */
    protected final Set<String> getMonitoredValidationMessageCodes() {
        return Collections.unmodifiableSet(monitoredValidationMessageCodes);
    }

}
