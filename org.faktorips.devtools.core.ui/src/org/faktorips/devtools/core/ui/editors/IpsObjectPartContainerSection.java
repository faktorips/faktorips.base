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

package org.faktorips.devtools.core.ui.editors;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.fields.MessageCueController;
import org.faktorips.devtools.core.ui.forms.IpsSection;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;

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
    private final Set<String> monitoredValidationMessageCodes = new HashSet<String>();

    private final IIpsObjectPartContainer ipsObjectPartContainer;

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
    public void refresh() {
        super.refresh();
        refreshSectionMessageIndicator();
    }

    /**
     * Refreshes the message indicator that is attached to the section.
     */
    private void refreshSectionMessageIndicator() {
        try {
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
            MessageCueController.setMessageCue(getClientComposite(), filteredMessageList);
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
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
