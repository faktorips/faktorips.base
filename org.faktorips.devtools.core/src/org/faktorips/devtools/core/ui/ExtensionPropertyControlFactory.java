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

package org.faktorips.devtools.core.ui;

import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IExtensionPropertyDefinition;
import org.faktorips.devtools.core.model.IIpsObjectPartContainer;
import org.faktorips.devtools.core.ui.binding.BindingContext;
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.devtools.core.ui.controller.IpsObjectUIController;

/**
 * Factory to create controls for the extension properties of a type.

 * @author eidenschink
 */
public class ExtensionPropertyControlFactory {

	private IExtensionPropertyDefinition[] extensionProperties;

	private EditField[] extensionEditFields;
	private IIpsObjectPartContainer[] partContainers;
    
	public ExtensionPropertyControlFactory(Class extensionClass) {
		extensionProperties = IpsPlugin.getDefault().getIpsModel()
				.getExtensionPropertyDefinitions(extensionClass, true);
		extensionEditFields = new EditField[extensionProperties.length];
        partContainers = new IIpsObjectPartContainer[extensionProperties.length];
	}
    
    public boolean needsToCreateControlsFor(IIpsObjectPartContainer ipsObjectPart, String position) {
        for (int i = 0; i < extensionProperties.length; i++) {
            if (extensionProperties[i].getEditedInStandardTextArea().equals(position)
                    && (extensionEditFields[i] == null)) {
                return true;
            }
        }
        return false;
    }

	/**
	 * Creates the <code>EditFields</code> of extension at <code>where</code> position
	 */
	public void createControls(Composite workArea, UIToolkit uiToolkit,
            IIpsObjectPartContainer ipsObjectPart, String position) {
		for (int i = 0; i < extensionProperties.length; i++) {
			if (extensionProperties[i].getEditedInStandardTextArea().equals(position)
					&& (extensionEditFields[i] == null)) {

                createLabelAndEditField(workArea, uiToolkit, ipsObjectPart, i);
			}
		}
	}

	/**
	 * Creates all not yet explicitely created EditFields of an extension not including
	 * the extensions tagged with <code>false</code> 
	 */
	public void createControls(Composite workArea, UIToolkit uiToolkit,
            IIpsObjectPartContainer ipsObjectPart) {
		for (int i = 0; i < extensionProperties.length; i++) {
			if ((!extensionProperties[i].getEditedInStandardTextArea().equals("false")) //$NON-NLS-1$
					&& (extensionEditFields[i] == null)) {
            
                createLabelAndEditField(workArea, uiToolkit, ipsObjectPart, i);
			}
		}
	}
    
    private void createLabelAndEditField(
            Composite workArea, 
            UIToolkit uiToolkit, 
            IIpsObjectPartContainer ipsObjectPart,
            int i) {

        uiToolkit.createFormLabel(workArea, extensionProperties[i].getDisplayName() + ":"); //$NON-NLS-1$
        extensionEditFields[i] = extensionProperties[i].newEditField(ipsObjectPart, workArea, uiToolkit);
        partContainers[i] = ipsObjectPart;
    }

	/**
	 * Connects all EditFields created by this factory with the model.
	 */
	public void connectToModel(IpsObjectUIController uiController) {
		for (int i = 0; i < extensionEditFields.length; i++) {
			if (extensionEditFields[i] != null) {
				uiController.add(extensionEditFields[i], extensionProperties[i]
						.getPropertyId());
			}
		}
	}
    
    /**
     * Binds all edit fields created by this factory into the given context.
     * 
     * @throws NullPointerException if context is <code>null</code>.
     */
    public void bind(BindingContext context) {
        for (int i = 0; i < extensionEditFields.length; i++) {
            if (extensionEditFields[i] != null) {
                context.bindContent(extensionEditFields[i], partContainers[i], extensionProperties[i].getPropertyId());
            }
        }
    }
    
}
