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
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IExtensionPropertyDefinition;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.ui.controller.fields.FieldValueChangedEvent;
import org.faktorips.util.message.MessageList;


/**
 *
 */
public class IpsObjectUIController extends IpsObjectPartContainerUIController {

    private IIpsObject object;
    
    public IpsObjectUIController(IIpsObject object) {
        super();
        this.object = object;
    }
    
    public IIpsObject getIpsObject() {
        return object;
    }
    
    public void add(EditField field, String propertyName) {
        IExtensionPropertyDefinition extProperty = object.getIpsModel().getExtensionPropertyDefinition(object.getClass(), propertyName, true);
        if (extProperty!=null) {
            addMapping(new FieldExtensionPropertyMapping(field, object, propertyName));
        } else {
            super.add(field, object, propertyName);
        }
    }
    
    public void updateUI() {
        super.updateUI();
        validateObjectAndUpdateUI();
    }
    
    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.ui.controller.fields.ValueChangeListener#valueChanged(org.faktorips.devtools.core.ui.controls.ContentChangeEvent)
     */
    public void valueChanged(FieldValueChangedEvent e) {
        super.valueChanged(e);
        validateObjectAndUpdateUI();
    }
    
    private void validateObjectAndUpdateUI() {
        try {
            MessageList list = object.validate();    
	        for (Iterator it=mappings.iterator(); it.hasNext();) {
	            FieldPropertyMapping mapping = (FieldPropertyMapping)it.next();
	            MessageList controlList = list.getMessagesFor(mapping.getObject(), mapping.getPropertyName());
	            mapping.getField().setMessages(controlList);
	        }
        } catch (CoreException e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
    }
    
}
