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

package org.faktorips.devtools.core.ui;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsobject.IExtensionPropertyDefinition;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.ui.binding.BindingContext;
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.devtools.core.ui.controller.IpsObjectUIController;

/**
 * Factory to create controls for the extension properties of a type.
 * 
 * @author eidenschink
 */
public class ExtensionPropertyControlFactory {

    private ExtPropControlData[] extPropData;

    public ExtensionPropertyControlFactory(Class extensionClass) {
        IExtensionPropertyDefinition[] extensionProperties = IpsPlugin.getDefault().getIpsModel()
                .getExtensionPropertyDefinitions(extensionClass, true);

        extPropData = new ExtPropControlData[extensionProperties.length];
        for (int i = 0; i < extensionProperties.length; i++) {
            extPropData[i] = new ExtPropControlData(extensionProperties[i]);
        }
    }

    public boolean needsToCreateControlsFor(IIpsObjectPartContainer ipsObjectPart, String position) {
        for (int i = 0; i < extPropData.length; i++) {
            if (position.equals(extPropData[i].extProperty.getPosition()) && (extPropData[i].editField == null)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Creates the <code>EditFields</code> of extension at <code>where</code> position
     */
    public void createControls(Composite workArea,
            UIToolkit uiToolkit,
            IIpsObjectPartContainer ipsObjectPart,
            String position) {

        // find all extension property definitions for the given position
        ArrayList<ExtPropControlData> extPropertiesForPosition = new ArrayList<ExtPropControlData>();
        for (int i = 0; i < extPropData.length; i++) {
            if (position.equals(extPropData[i].extProperty.getPosition())) {
                extPropertiesForPosition.add(extPropData[i]);
            }
        }

        createControls(extPropertiesForPosition, workArea, uiToolkit, ipsObjectPart);
    }

    /**
     * Creates all not yet explicitely created EditFields of an extension not including the
     * extensions tagged with <code>false</code>
     */
    public void createControls(Composite workArea, UIToolkit uiToolkit, IIpsObjectPartContainer ipsObjectPart) {
        // find all extension property definitions for the given position
        ArrayList extPropertiesForPosition = new ArrayList();
        for (int i = 0; i < extPropData.length; i++) {
            if (!"false".equals(extPropData[i].extProperty.getPosition())) { //$NON-NLS-1$
                extPropertiesForPosition.add(extPropData[i]);
            }
        }
        createControls(extPropertiesForPosition, workArea, uiToolkit, ipsObjectPart);
    }

    private void createControls(ArrayList<ExtPropControlData> extPropControlData,
            Composite workArea,
            UIToolkit uiToolkit,
            IIpsObjectPartContainer ipsObjectPart) {

        // sort the array of found extension property definitions by their SortOrder
        ExtPropControlData[] sortedExtensionPropertyDefinitions;
        sortedExtensionPropertyDefinitions = extPropControlData.toArray(new ExtPropControlData[extPropControlData
                .size()]);
        Arrays.sort(sortedExtensionPropertyDefinitions);

        // create controls
        for (int i = 0; i < sortedExtensionPropertyDefinitions.length; i++) {
            if (sortedExtensionPropertyDefinitions[i].editField == null) {
                createLabelAndEditField(workArea, uiToolkit, ipsObjectPart, sortedExtensionPropertyDefinitions[i]);
            }
        }
    }

    private void createLabelAndEditField(Composite workArea,
            UIToolkit uiToolkit,
            IIpsObjectPartContainer ipsObjectPart,
            ExtPropControlData extPropertyData) {

        uiToolkit.createFormLabel(workArea, extPropertyData.extProperty.getName() + ":"); //$NON-NLS-1$
        IExtensionPropertyEditFieldFactory factory;
        try {
            factory = IpsUIPlugin.getDefault().getExtensionPropertyEditFieldFactory(
                    extPropertyData.extProperty.getPropertyId());
            extPropertyData.editField = factory.newEditField(ipsObjectPart, workArea, uiToolkit);
            extPropertyData.partContainer = ipsObjectPart;
        } catch (CoreException e) {
            IpsPlugin.log(e);
        }
    }

    /**
     * Connects all EditFields created by this factory with the model.
     */
    public void connectToModel(IpsObjectUIController uiController) {
        for (int i = 0; i < extPropData.length; i++) {
            uiController.add(extPropData[i].editField, extPropData[i].extProperty.getPropertyId());
        }
    }

    /**
     * Binds all edit fields created by this factory into the given context.
     * 
     * @throws NullPointerException if context is <code>null</code>.
     */
    public void bind(BindingContext context) {
        for (int i = 0; i < extPropData.length; i++) {
            if (extPropData[i].editField != null) {
                context.bindContent(extPropData[i].editField, extPropData[i].partContainer, extPropData[i].extProperty
                        .getPropertyId());
            }
        }
    }

    /**
     * Removes all bindings of the edit fields created by this factory from the given context.
     * 
     * @throws NullPointerException if context is <code>null</code>.
     */
    public void removeBinding(BindingContext context) {
        for (int i = 0; i < extPropData.length; i++) {
            if (extPropData[i].editField != null) {
                context.removeBindings(extPropData[i].editField.getControl());
            }
        }
    }

    private class ExtPropControlData implements Comparable {
        IExtensionPropertyDefinition extProperty;
        EditField editField;
        IIpsObjectPartContainer partContainer;

        public ExtPropControlData(IExtensionPropertyDefinition extProperty) {
            this.extProperty = extProperty;
        }

        public int compareTo(Object o) {
            if (!(o instanceof ExtPropControlData)) {
                return 0;
            }
            return extProperty.compareTo(((ExtPropControlData)o).extProperty);
        }

    }
}
