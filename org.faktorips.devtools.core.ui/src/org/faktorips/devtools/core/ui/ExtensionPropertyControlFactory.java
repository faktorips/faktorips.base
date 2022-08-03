/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.IExtensionPropertySectionFactory.Position;
import org.faktorips.devtools.core.ui.binding.BindingContext;
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.devtools.core.ui.forms.IpsSection;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.extproperties.IExtensionPropertyAccess;
import org.faktorips.devtools.model.extproperties.IExtensionPropertyDefinition;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;

/**
 * Factory to create controls for the extension properties of a type.
 * 
 */
public class ExtensionPropertyControlFactory {

    private ExtPropControlData[] extPropData;

    /**
     * Creates a ne {@link ExtensionPropertyControlFactory} for the given
     * {@link IExtensionPropertyAccess object}.
     * 
     * @param extendedObject The object for which the extension properties are registered
     */
    public ExtensionPropertyControlFactory(IExtensionPropertyAccess extendedObject) {
        Collection<IExtensionPropertyDefinition> extensionProperties = extendedObject.getExtensionPropertyDefinitions();

        extPropData = new ExtPropControlData[extensionProperties.size()];
        int i = 0;
        for (IExtensionPropertyDefinition propertyDefinition : extensionProperties) {
            extPropData[i] = new ExtPropControlData(propertyDefinition);
            i++;
        }
    }

    /**
     * This constructor uses the extension class to get the registered extension properties. Since
     * 3.10 we have the ability to limit the scope of an extension property to a specific instance
     * of {@link IIpsObjectPartContainer}. This constructor ignores this feature! Use with caution,
     * the extension property may not be applicable for a special instance.
     * <p>
     * If you use this constructor you should use
     * {@link #bind(IIpsObjectPartContainer, BindingContext)} to hide the unneeded extension
     * properties.
     * 
     * @param extensionClass The class for which the extension properties are registered
     */
    public ExtensionPropertyControlFactory(Class<? extends IExtensionPropertyAccess> extensionClass) {
        Set<IExtensionPropertyDefinition> extensionProperties = IIpsModel.get()
                .getExtensionPropertyDefinitionsForClass(extensionClass, true);
        extPropData = new ExtPropControlData[extensionProperties.size()];
        int i = 0;
        for (IExtensionPropertyDefinition propertyDefinition : extensionProperties) {
            extPropData[i] = new ExtPropControlData(propertyDefinition);
            i++;
        }
    }

    /**
     * Checks whether controls need to be created. If you created this factory using the recommended
     * constructor {@link #ExtensionPropertyControlFactory(IExtensionPropertyAccess)} you could
     * simply us {@link #needsToCreateControlsFor(String)}. If you do not have the concrete instance
     * when creating this factory you could use this method later to evaluate
     * {@link IExtensionPropertyDefinition#isApplicableFor(IIpsObjectPartContainer)}.
     * 
     * @param ipsObjectPartContainer The {@link IIpsObjectPartContainer} for which the controls may
     *            need to be created
     * @param position the position where the controls would be created
     */
    public boolean needsToCreateControlsFor(IIpsObjectPartContainer ipsObjectPartContainer, String position) {
        for (ExtPropControlData element : extPropData) {
            if (element.extProperty.isApplicableFor(ipsObjectPartContainer)
                    && position.equals(element.extProperty.getPosition()) && (element.editField == null)) {
                return true;
            }
        }
        return false;
    }

    public boolean needsToCreateControlsFor(String position) {
        for (ExtPropControlData element : extPropData) {
            if (position.equals(element.extProperty.getPosition()) && (element.editField == null)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Creates the {@link EditField EditFields} for the extensions at the given
     * <code>position</code>.
     * 
     * @param workArea the parent composite where to create the controls
     * @param uiToolkit the toolkit used to create the controls
     * @param ipsObjectPart the object used to bind the controls, may be <code>null</code> if it is
     *            unknown yet
     * @param position either {@link IExtensionPropertyDefinition#POSITION_TOP} or
     *            {@link IExtensionPropertyDefinition#POSITION_BOTTOM}
     */
    public void createControls(Composite workArea,
            UIToolkit uiToolkit,
            IIpsObjectPartContainer ipsObjectPart,
            String position) {

        // find all extension property definitions for the given position
        ArrayList<ExtPropControlData> extPropertiesForPosition = new ArrayList<>();
        for (ExtPropControlData element : extPropData) {
            if (position.equals(element.extProperty.getPosition())) {
                extPropertiesForPosition.add(element);
            }
        }

        createControls(extPropertiesForPosition, workArea, uiToolkit, ipsObjectPart);
    }

    /**
     * Creates all not yet explicitly created {@link EditField EditFields} for extensions, not
     * including the extensions tagged with <code>false</code>.
     */
    public void createControls(Composite workArea, UIToolkit uiToolkit, IIpsObjectPartContainer ipsObjectPart) {
        // find all extension property definitions for the given position
        ArrayList<ExtPropControlData> extPropertiesForPosition = new ArrayList<>();
        for (ExtPropControlData element : extPropData) {
            if (!"false".equals(element.extProperty.getPosition())) { //$NON-NLS-1$
                extPropertiesForPosition.add(element);
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
        sortedExtensionPropertyDefinitions = extPropControlData
                .toArray(new ExtPropControlData[extPropControlData.size()]);
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
            factory = IpsUIPlugin.getDefault()
                    .getExtensionPropertyEditFieldFactory(extPropertyData.extProperty.getPropertyId());
            extPropertyData.editField = factory.newEditField(ipsObjectPart, workArea, uiToolkit);
            extPropertyData.partContainer = ipsObjectPart;
        } catch (IpsException e) {
            IpsPlugin.log(e);
        }
    }

    public List<IpsSection> createSections(Composite parent,
            UIToolkit toolkit,
            IIpsObjectPartContainer ipsObjectPart,
            Position position) {

        List<IpsSection> sections = new ArrayList<>(extPropData.length);
        for (ExtPropControlData extPropControlData : extPropData) {
            try {
                IExtensionPropertySectionFactory sectionFactory = IpsUIPlugin.getDefault()
                        .getExtensionPropertySectionFactory(extPropControlData.extProperty.getPropertyId());
                if (sectionFactory != null && sectionFactory.getPosition() == position
                        && sectionFactory.createsSectionFor(ipsObjectPart)) {
                    IpsSection newSection = sectionFactory.newSection(ipsObjectPart, parent, toolkit);
                    sections.add(newSection);
                }
            } catch (IpsException e) {
                IpsPlugin.log(e);
            }
        }
        return sections;
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
     * Sets the {@link IIpsObjectPartContainer} object and binds all {@link EditField EditFields} to
     * it, setting their visibility according to the
     * {@link IExtensionPropertyDefinition#isApplicableFor(IIpsObjectPartContainer) applicability}
     * of the extension properties to that {@link IIpsObjectPartContainer}.
     * <p>
     * This is usable in cases where edit fields must be created independent of the actual
     * {@link IIpsObjectPartContainer partConatiner}, especially when creating the
     * {@link ExtensionPropertyControlFactory} with the constructor
     * {@link #ExtensionPropertyControlFactory(Class)}, initially ignoring the
     * {@link IExtensionPropertyDefinition#isApplicableFor(IIpsObjectPartContainer) applicability}.
     */
    public void bind(IIpsObjectPartContainer partContainer, BindingContext bindingContext) {
        for (ExtPropControlData element : extPropData) {
            element.partContainer = partContainer;
            if (element.editField != null) {
                if (element.extProperty.isApplicableFor(partContainer)) {
                    bindingContext.bindContent(element.editField, element.partContainer,
                            element.extProperty.getPropertyId());
                } else {
                    element.editField.getControl().setVisible(false);
                }
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
                 * would be initialised but the partContainer was null and would never be set, even
                 * if createControls() would be called again. Binding would then fail with an NPE.)
                 */
                extPropData[i].editField = null;
            }
        }
    }

    private static class ExtPropControlData implements Comparable<ExtPropControlData> {

        private IExtensionPropertyDefinition extProperty;
        private EditField<?> editField;
        private IIpsObjectPartContainer partContainer;

        public ExtPropControlData(IExtensionPropertyDefinition extProperty) {
            this.extProperty = extProperty;
        }

        @Override
        public int compareTo(ExtPropControlData o) {
            return extProperty.compareTo(o.extProperty);
        }

    }
}
