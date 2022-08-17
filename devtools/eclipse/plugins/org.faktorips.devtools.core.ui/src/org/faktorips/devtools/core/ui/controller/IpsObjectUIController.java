/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controller;

import org.eclipse.swt.widgets.Control;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.binding.BindingContext;
import org.faktorips.devtools.core.ui.controller.fields.FieldValueChangedEvent;
import org.faktorips.devtools.model.extproperties.IExtensionPropertyDefinition;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;

/**
 * UIController for ips objects and their parts.
 * 
 * @author eidenschink
 * @author Jan Ortmann
 * 
 * @deprecated deprecated since 3.6, use {@link BindingContext} instead
 */
@Deprecated
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
    public <T> void add(EditField<T> editField, String propertyName) {
        IExtensionPropertyDefinition extProperty = partContainer.getExtensionPropertyDefinition(propertyName);
        if (extProperty != null) {
            addMapping(new FieldExtensionPropertyMapping<>(editField, partContainer, propertyName));
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
        if (getMappings().size() == 0) {
            return new MessageList();
        }
        try {
            IIpsObjectPartContainer validatee = partContainer;
            if (enableWholeIpsObjectValidation) {
                validatee = partContainer.getIpsObject();
            }
            MessageList list = validatee.validate(partContainer.getIpsProject());
            for (FieldPropertyMapping<?> mapping : getMappings()) {
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
        } catch (IpsException e) {
            IpsPlugin.log(e);
            return new MessageList();
        }
    }

}
