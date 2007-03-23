/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controller;

import java.util.Iterator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.widgets.Control;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IExtensionPropertyDefinition;
import org.faktorips.devtools.core.model.IIpsObjectPartContainer;
import org.faktorips.devtools.core.ui.controller.fields.FieldValueChangedEvent;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;

/**
 * This class will replace the subclasses IpsObjectUIController and IpsPartUIController
 * when the implementation of the corresponding class IpsObjectPartContainer is finished.
 * 
 * @author eidenschink
 * @author Jan Ortmann
 */
public abstract class IpsObjectPartContainerUIController extends DefaultUIController {

    private IIpsObjectPartContainer partContainer;

	public IpsObjectPartContainerUIController(IIpsObjectPartContainer partContainer) {
		super();
        this.partContainer = partContainer;
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
        IExtensionPropertyDefinition extProperty = IpsPlugin.getDefault().getIpsModel().getExtensionPropertyDefinition(partContainer.getClass(), propertyName, true);
        if (extProperty!=null) {
            addMapping(new FieldExtensionPropertyMapping(editField, partContainer, propertyName));
        } else {
            super.add(editField, partContainer, propertyName);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public void updateUI() {
        super.updateUI();
        validatePartContainerAndUpdateUI();
    }
    
    /** 
     * {@inheritDoc}
     */
    public void valueChanged(FieldValueChangedEvent e) {
        super.valueChanged(e);
        validatePartContainerAndUpdateUI();
    }
    
    /**
     * Validates the part container and updates the fields that are associated with attributes of the IpsPartContainer.
     * It returns the MessageList which is the result of the validation. This return value 
     * can be evaluated when overriding this method.
     * 
     * @return the validation message list. Never returns <code>null</code>.
     */
    protected MessageList validatePartContainerAndUpdateUI() {
        if (mappings.size()==0) {
            return new MessageList();
        }
        try {
            MessageList list = partContainer.validate();
            for (Iterator it=mappings.iterator(); it.hasNext();) {
                FieldPropertyMapping mapping = (FieldPropertyMapping)it.next();
                Control c = mapping.getField().getControl();
                if (c==null || c.isDisposed()) {
                    continue;
                }
                MessageList fieldMessages;
                if (mapping.getField().isTextContentParsable()) {
                    fieldMessages = list.getMessagesFor(mapping.getObject(), mapping.getPropertyName());
                } else {
                    fieldMessages = new MessageList();
                    fieldMessages.add(Message.newError(EditField.INVALID_VALUE, Messages.getString("IpsObjectPartContainerUIController.invalidValue"))); //$NON-NLS-1$
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
