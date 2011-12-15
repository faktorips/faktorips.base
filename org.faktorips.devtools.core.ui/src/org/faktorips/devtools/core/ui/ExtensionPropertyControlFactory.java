/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsobject.IExtensionPropertyDefinition;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.ui.IExtensionPropertySectionFactory.Position;
import org.faktorips.devtools.core.ui.binding.BindingContext;
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.devtools.core.ui.controller.IpsObjectUIController;
import org.faktorips.devtools.core.ui.forms.IpsSection;

/**
 * Factory to create controls for the extension properties of a type.
 * 
 * @author eidenschink
 */
public class ExtensionPropertyControlFactory {

    private ExtPropControlData[] extPropData;

    public ExtensionPropertyControlFactory(Class<?> extensionClass) {
        IExtensionPropertyDefinition[] extensionProperties = IpsPlugin.getDefault().getIpsModel()
                .getExtensionPropertyDefinitions(extensionClass, true);

        extPropData = new ExtPropControlData[extensionProperties.length];
        for (int i = 0; i < extensionProperties.length; i++) {
            extPropData[i] = new ExtPropControlData(extensionProperties[i]);
        }
    }

    public boolean needsToCreateControlsFor(IIpsObjectPartContainer ipsObjectPart, String position) {
        for (ExtPropControlData element : extPropData) {
            if (position.equals(element.extProperty.getPosition()) && (element.editField == null)) {
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
        for (ExtPropControlData element : extPropData) {
            if (position.equals(element.extProperty.getPosition())) {
                extPropertiesForPosition.add(element);
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
        ArrayList<ExtPropControlData> extPropertiesForPosition = new ArrayList<ExtPropControlData>();
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
        for (ExtPropControlData sortedExtensionPropertyDefinition : sortedExtensionPropertyDefinitions) {
            if (sortedExtensionPropertyDefinition.editField == null) {
                createLabelAndEditField(workArea, uiToolkit, ipsObjectPart, sortedExtensionPropertyDefinition);
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

    public List<IpsSection> createSections(Composite parent,
            UIToolkit toolkit,
            IIpsObjectPartContainer ipsObjectPart,
            Position position) {

        List<IpsSection> sections = new ArrayList<IpsSection>(extPropData.length);
        for (ExtPropControlData extPropControlData : extPropData) {
            try {
                IExtensionPropertySectionFactory sectionFactory = IpsUIPlugin.getDefault()
                        .getExtensionPropertySectionFactory(extPropControlData.extProperty.getPropertyId());
                if (sectionFactory != null && sectionFactory.getPosition() == position
                        && sectionFactory.createsSectionFor(ipsObjectPart)) {
                    IpsSection newSection = sectionFactory.newSection(ipsObjectPart, parent, toolkit);
                    sections.add(newSection);
                }
            } catch (CoreException e) {
                IpsPlugin.log(e);
            }
        }
        return sections;
    }

    /**
     * Connects all EditFields created by this factory with the model.
     */
    public void connectToModel(IpsObjectUIController uiController) {
        for (ExtPropControlData element : extPropData) {
            uiController.add(element.editField, element.extProperty.getPropertyId());
        }
    }

    /**
     * Binds all edit fields created by this factory into the given context.
     * 
     * @throws NullPointerException if context is <code>null</code>.
     */
    public void bind(BindingContext context) {
        for (ExtPropControlData element : extPropData) {
            if (element.editField != null) {
                context.bindContent(element.editField, element.partContainer, element.extProperty.getPropertyId());
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
                /*
                 * Set edit-field back to null to ensure the extPropData will be configured
                 * correctly even if #createControls(ArrayList, Composite, UIToolkit,
                 * IIpsObjectPartContainer) is called multiple times. (Previously the edit-field
                 * would be initialized but the partContainer was null and would never be set, even
                 * if createControls() would be called again. Binding would then fail with an NPE.)
                 */
                extPropData[i].editField = null;
            }
        }
    }

    private class ExtPropControlData implements Comparable<ExtPropControlData> {
        IExtensionPropertyDefinition extProperty;
        EditField<?> editField;
        IIpsObjectPartContainer partContainer;

        public ExtPropControlData(IExtensionPropertyDefinition extProperty) {
            this.extProperty = extProperty;
        }

        @Override
        public int compareTo(ExtPropControlData o) {
            return extProperty.compareTo(o.extProperty);
        }

    }
}
