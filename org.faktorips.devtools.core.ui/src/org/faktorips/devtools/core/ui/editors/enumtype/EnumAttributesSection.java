/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.enumtype;

import java.util.EnumSet;
import java.util.List;

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
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.editors.EditDialog;
import org.faktorips.devtools.core.ui.editors.IpsPartsComposite;
import org.faktorips.devtools.core.ui.editors.SimpleIpsPartsSection;
import org.faktorips.devtools.core.ui.editors.enums.EnumValuesSection;
import org.faktorips.devtools.model.enums.IEnumAttribute;
import org.faktorips.devtools.model.enums.IEnumLiteralNameAttribute;
import org.faktorips.devtools.model.enums.IEnumType;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;

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

    void setEnumValuesSection(EnumValuesSection enumValuesSection) {
        enumAttributesComposite.setEnumValuesSection(enumValuesSection);
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
        enumAttributesComposite = new EnumAttributesComposite(getEnumType(), getSite(), parent, toolkit);
        return enumAttributesComposite;
    }

    private IEnumType getEnumType() {
        return (IEnumType)getIpsObject();
    }

    @Override
    protected void performRefresh() {
        super.performRefresh();

        enumAttributesComposite.updateInheritButtonEnabledState();
    }

    /**
     * A composite that shows an {@link IEnumType}'s attributes in a viewer and allows to edit these
     * attributes in a dialog, to create new attributes, move attributes and to delete attributes.
     */
    private static class EnumAttributesComposite extends IpsPartsComposite implements ISelectionChangedListener {

        private final IEnumType enumType;

        private EnumValuesSection enumValuesSection;
        private boolean isChangeable = true;

        /**
         * Button that opens a dialog that enables the user to inherit attributes from the super
         * type hierarchy.
         */
        private Button inheritButton;

        public EnumAttributesComposite(IEnumType enumType, IWorkbenchPartSite site, Composite parent,
                UIToolkit toolkit) {
            super(enumType, parent, site,
                    EnumSet.of(Option.CAN_CREATE, Option.CAN_EDIT, Option.CAN_DELETE, Option.CAN_MOVE,
                            Option.SHOW_EDIT_BUTTON, Option.RENAME_REFACTORING_SUPPORTED,
                            Option.PULL_UP_REFACTORING_SUPPORTED, Option.JUMP_TO_SOURCE_CODE_SUPPORTED),
                    toolkit);
            this.enumType = enumType;
            addSelectionChangedListener(this);
        }

        private void setEnumValuesSection(EnumValuesSection enumValuesSection) {
            this.enumValuesSection = enumValuesSection;
        }

        @Override
        protected IStructuredContentProvider createContentProvider() {
            return new IStructuredContentProvider() {

                @Override
                public Object[] getElements(Object inputElement) {
                    return enumType.getEnumAttributesIncludeSupertypeCopies(true).toArray();
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
        protected EditDialog createEditDialog(IIpsObjectPart part, Shell shell) {
            return new EnumAttributeEditDialog((IEnumAttribute)part, shell);
        }

        @Override
        protected IIpsObjectPart newIpsPart() {
            IEnumAttribute newEnumAttribute = enumType.newEnumAttribute();

            /*
             * If this is the first attribute to be created and the values are being defined in the
             * enum type then make sure that there will be one enum value available for editing.
             */
            if (enumType.getEnumAttributesCountIncludeSupertypeCopies(true) == 1) {
                if (enumType.isInextensibleEnum()) {
                    if (enumType.getEnumValuesCount() == 0) {
                        enumType.newEnumValue();
                    }
                }
            }
            return newEnumAttribute;

        }

        @Override
        protected int[] moveParts(int[] indexes, boolean up) {
            int newIndex = indexes[0];
            List<IEnumAttribute> enumAttributes = enumType.getEnumAttributesIncludeSupertypeCopies(true);

            IEnumAttribute enumAttributeToMove = enumAttributes.get(newIndex);
            newIndex = enumType.moveEnumAttribute(enumAttributeToMove, up);

            return new int[] { newIndex };
        }

        @Override
        protected void editPartConfirmed() {
            enumValuesSection.reinit();
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
                    inheritClicked();
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
            /*
             * SW 6.12.2013: Effective Java 2nd Ed. P. 89:
             * "Constructors must not invoke overridable methods"
             */
            IEnumType enumTypeFromSuperclass = (IEnumType)getIpsObject();
            boolean superEnumTypeExists = enumTypeFromSuperclass
                    .hasExistingSuperEnumType(enumTypeFromSuperclass.getIpsProject());
            inheritButton
                    .setEnabled(superEnumTypeExists && IpsUIPlugin.isEditable(enumTypeFromSuperclass.getIpsSrcFile()));
        }

        @Override
        public void setDataChangeable(boolean flag) {
            isChangeable = flag;
            super.setDataChangeable(flag);
        }

        private boolean getIsChangeable() {
            return isChangeable;
        }

        /**
         * Disables the "Delete" - Button if the selected {@link IIpsObjectPart} is an
         * {@link IEnumLiteralNameAttribute} while the {@link IEnumType} is needing to use it.
         */
        @Override
        public void selectionChanged(SelectionChangedEvent event) {
            setCanDelete(getIsChangeable());
            if (getSelectedPart() instanceof IEnumLiteralNameAttribute) {
                setCanDelete(false);
            }
        }

        /**
         * Opens a dialog enabling the user to inherit {@link IEnumAttribute}s from the super type
         * hierarchy in a comfortable way.
         */
        private void inheritClicked() throws CoreRuntimeException {
            InheritEnumAttributesDialog dialog = new InheritEnumAttributesDialog(enumType, getShell());
            if (dialog.open() == Window.OK) {
                enumType.inheritEnumAttributes(dialog.getSelectedParts());
                refresh();
            }
        }

    }

}
