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

package org.faktorips.devtools.core.ui.editors.enumtype;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPartSite;
import org.faktorips.devtools.core.model.enums.IEnumAttribute;
import org.faktorips.devtools.core.model.enums.IEnumLiteralNameAttribute;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.enums.IEnumValue;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.editors.EditDialog;
import org.faktorips.devtools.core.ui.editors.IpsPartsComposite;
import org.faktorips.devtools.core.ui.editors.SimpleIpsPartsSection;

/**
 * The UI section for the {@link EnumTypeEditorPage} that contains the {@link IEnumAttribute}s of
 * the {@link IEnumType} to be edited.
 * 
 * @see EnumTypeEditorPage
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
public class EnumAttributesSection extends SimpleIpsPartsSection {

    private static final String ID = "org.faktorips.devtools.core.ui.editors.enumtype.EnumAttributesSection"; //$NON-NLS-1$

    private EnumAttributesComposite enumAttributesComposite;

    /**
     * @param enumType The {@link IEnumType} to show the {@link IEnumAttribute}s from
     * @param parent The parent UI composite
     * @param toolkit The UI toolkit that shall be used to create UI elements
     */
    public EnumAttributesSection(IEnumType enumType, Composite parent, IWorkbenchPartSite site, UIToolkit toolkit) {
        super(ID, enumType, parent, site, Messages.EnumAttributesSection_title, toolkit);

        setGrabVerticalSpace(false);
        /*
         * Workaround: re-layout to let setGrabVerticalSpace() to take effect. This is necessary as
         * the controls are created in the super constructor.
         */
        relayoutSection(true);

        addMonitoredValidationMessageCode(IEnumType.MSGCODE_ENUM_TYPE_MULTIPLE_LITERAL_NAME_ATTRIBUTES);
        addMonitoredValidationMessageCode(IEnumType.MSGCODE_ENUM_TYPE_NO_LITERAL_NAME_ATTRIBUTE);
        addMonitoredValidationMessageCode(IEnumType.MSGCODE_ENUM_TYPE_NO_USED_AS_ID_IN_FAKTOR_IPS_UI_ATTRIBUTE);
        addMonitoredValidationMessageCode(IEnumType.MSGCODE_ENUM_TYPE_NO_USED_AS_NAME_IN_FAKTOR_IPS_UI_ATTRIBUTE);
        addMonitoredValidationMessageCode(IEnumType.MSGCODE_ENUM_TYPE_NOT_INHERITED_ATTRIBUTES_IN_SUPERTYPE_HIERARCHY);
    }

    /**
     * If the edited {@link IEnumType} does not contain any values the section will be expanded
     * independent of the stored preference.
     */
    @Override
    protected void initExpandedState() {
        if (getEnumType().getEnumValuesCount() == 0) {
            setExpanded(true);
        } else {
            super.initExpandedState();
        }
    }

    @Override
    protected IpsPartsComposite createIpsPartsComposite(Composite parent, UIToolkit toolkit) {
        enumAttributesComposite = new EnumAttributesComposite(parent);
        return enumAttributesComposite;
    }

    private IEnumType getEnumType() {
        return (IEnumType)getIpsObject();
    }

    @Override
    protected void performRefresh() {
        super.performRefresh();

        enumAttributesComposite.updateInheritButtonEnabledState();
        try {
            enumAttributesComposite.setCanDelete(!(getEnumType().isCapableOfContainingValues()));
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * A composite that shows an {@link IEnumType}'s attributes in a viewer and allows to edit these
     * attributes in a dialog, to create new attributes, move attributes and to delete attributes.
     */
    private class EnumAttributesComposite extends IpsPartsComposite implements ISelectionChangedListener {

        /**
         * Button that opens a dialog that enables the user to inherit attributes from the super
         * type hierarchy.
         */
        private Button inheritButton;

        public EnumAttributesComposite(Composite parent) {
            super(getEnumType(), parent, getSite(), true, true, true, true, true, true, true, true, getToolkit());
            addSelectionChangedListener(this);
        }

        @Override
        protected IStructuredContentProvider createContentProvider() {
            return new IStructuredContentProvider() {

                @Override
                public Object[] getElements(Object inputElement) {
                    return getEnumType().getEnumAttributesIncludeSupertypeCopies(true).toArray();
                }

                @Override
                public void dispose() {
                    // Nothing to do
                }

                @Override
                public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
                    // Nothing to do
                }

            };
        }

        @Override
        protected EditDialog createEditDialog(IIpsObjectPart part, Shell shell) throws CoreException {
            return new EnumAttributeEditDialog((IEnumAttribute)part, shell);
        }

        @Override
        protected IIpsObjectPart newIpsPart() throws CoreException {
            IEnumAttribute newEnumAttribute = getEnumType().newEnumAttribute();

            /*
             * If this is the first attribute to be created and the values are being defined in the
             * enum type then make sure that there will be one enum value available for editing.
             */
            if (getEnumType().getEnumAttributesCountIncludeSupertypeCopies(true) == 1) {
                if (getEnumType().isContainingValues() && !(getEnumType().isAbstract())) {
                    if (getEnumType().getEnumValuesCount() == 0) {
                        getEnumType().newEnumValue();
                    }
                }
            }

            return newEnumAttribute;
        }

        @Override
        protected void deleteIpsPart(IIpsObjectPart partToDelete) throws CoreException {
            IEnumAttribute enumAttributeToDelete = (IEnumAttribute)partToDelete;
            getEnumType().deleteEnumAttributeWithValues(enumAttributeToDelete);

            // Delete all enum values if there are no more enum attributes.
            if (getEnumType().getEnumAttributesCountIncludeSupertypeCopies(getEnumType().isCapableOfContainingValues()) == 0) {
                for (IEnumValue currentEnumValue : getEnumType().getEnumValues()) {
                    currentEnumValue.delete();
                }
            }
        }

        @Override
        protected int[] moveParts(int[] indexes, boolean up) {
            int newIndex = indexes[0];
            List<IEnumAttribute> enumAttributes = getEnumType().getEnumAttributesIncludeSupertypeCopies(true);

            IEnumAttribute enumAttributeToMove = enumAttributes.get(newIndex);
            try {
                newIndex = getEnumType().moveEnumAttribute(enumAttributeToMove, up);
            } catch (CoreException e) {
                throw new RuntimeException(e);
            }

            return new int[] { newIndex };
        }

        @Override
        protected boolean createButtons(Composite buttons, UIToolkit toolkit) {
            super.createButtons(buttons, toolkit);
            createButtonSpace(buttons, toolkit);

            inheritButton = toolkit.createButton(buttons, Messages.EnumAttributessection_buttonInherit);
            inheritButton
                    .setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING));
            inheritButton.addSelectionListener(new SelectionListener() {
                @Override
                public void widgetSelected(SelectionEvent event) {
                    try {
                        inheritClicked();
                    } catch (CoreException e) {
                        throw new RuntimeException(e);
                    }
                }

                @Override
                public void widgetDefaultSelected(SelectionEvent event) {
                    // Nothing to do
                }
            });
            updateInheritButtonEnabledState();

            return true;
        }

        private void updateInheritButtonEnabledState() {
            try {
                IEnumType enumType = (IEnumType)getIpsObject();
                boolean superEnumTypeExists = enumType.hasExistingSuperEnumType(enumType.getIpsProject());
                inheritButton.setEnabled(superEnumTypeExists && IpsUIPlugin.isEditable(enumType.getIpsSrcFile()));
            } catch (CoreException e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * Disables the "Delete" - Button if the selected {@link IIpsObjectPart} is an
         * {@link IEnumLiteralNameAttribute} while the {@link IEnumType} is needing to use it.
         */
        @Override
        public void selectionChanged(SelectionChangedEvent event) {
            try {
                setCanDelete(true);
                if (getSelectedPart() instanceof IEnumLiteralNameAttribute
                        && getEnumType().isCapableOfContainingValues()) {
                    setCanDelete(false);
                }
            } catch (CoreException e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * Opens a dialog enabling the user to inherit {@link IEnumAttribute}s from the super type
         * hierarchy in a comfortable way.
         */
        private void inheritClicked() throws CoreException {
            InheritEnumAttributesDialog dialog = new InheritEnumAttributesDialog(getEnumType(), getShell());
            if (dialog.open() == Window.OK) {
                getEnumType().inheritEnumAttributes(dialog.getSelectedParts());
                refresh();
            }
        }

    }

}
