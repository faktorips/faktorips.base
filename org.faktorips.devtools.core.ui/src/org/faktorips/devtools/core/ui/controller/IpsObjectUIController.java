/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.controller;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.widgets.Control;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsobject.IExtensionPropertyDefinition;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.ui.controller.fields.FieldValueChangedEvent;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;

/**
 * UIController for ips objects and their parts.
 * 
 * @author eidenschink
 * @author Jan Ortmann
 */
public class IpsObjectUIController extends DefaultUIController {

    private IIpsObjectPartContainer partContainer;
    private boolean enableWholeIpsObjectValidation = false;

    public IpsObjectUIController(IIpsObjectPartContainer partContainer) {
        super();
        this.partContainer = partContainer;
    }

    /**
     * If set to true the validate method of the ips object of the ips object part container is
     * called instead of the one of the ips object part container. By default this is set to
     * <code>false</code>.
     */
    public void setEnableWholeIpsObjectValidation(boolean enable) {
        enableWholeIpsObjectValidation = enable;
    }

    public IIpsObject getIpsObject() {
        return partContainer.getIpsObject();
    }

    /**
     * Returns the ips object part container this is a controller for.
     */
    public IIpsObjectPartContainer getIpsObjectPartContainer() {
        return partContainer;
    }

    /**
     * @see DefaultUIController#add(EditField, Object, String)
     */
    public void add(EditField editField, String propertyName) {
        IExtensionPropertyDefinition extProperty = IpsPlugin.getDefault().getIpsModel().getExtensionPropertyDefinition(
                partContainer.getClass(), propertyName, true);
        if (extProperty != null) {
            addMapping(new FieldExtensionPropertyMapping(editField, partContainer, propertyName));
        } else {
            super.add(editField, partContainer, propertyName);
        }
    }

    @Override
    public void updateUI() {
        super.updateUI();
        validatePartContainerAndUpdateUI();
    }

    @Override
    public void valueChanged(FieldValueChangedEvent e) {
        super.valueChanged(e);
        validatePartContainerAndUpdateUI();
    }

    /**
     * Validates the part container and updates the fields that are associated with attributes of
     * the IpsPartContainer. It returns the MessageList which is the result of the validation. This
     * return value can be evaluated when overriding this method.
     * 
     * @return the validation message list. Never returns <code>null</code>.
     */
    protected MessageList validatePartContainerAndUpdateUI() {
        if (mappings.size() == 0) {
            return new MessageList();
        }
        try {
            IIpsObjectPartContainer validatee = partContainer;
            if (enableWholeIpsObjectValidation) {
                validatee = partContainer.getIpsObject();
            }
            MessageList list = validatee.validate(partContainer.getIpsProject());
            for (FieldPropertyMapping mapping : mappings) {
                Control c = mapping.getField().getControl();
                if (c == null || c.isDisposed()) {
                    continue;
                }
                MessageList fieldMessages;
                if (mapping.getField().isTextContentParsable()) {
                    fieldMessages = list.getMessagesFor(mapping.getObject(), mapping.getPropertyName());
                } else {
                    fieldMessages = new MessageList();
                    fieldMessages.add(Message.newError(EditField.INVALID_VALUE,
                            Messages.IpsObjectPartContainerUIController_invalidValue));
                }
                mapping.getField().setMessages(fieldMessages);
            }
            return list;
        } catch (CoreException e) {
            IpsPlugin.log(e);
            return new MessageList();
        }
    }

}
