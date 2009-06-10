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

package org.faktorips.devtools.core.ui.editors.enumtype;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.devtools.core.model.enums.IEnumAttribute;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.enums.IEnumValue;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.editors.EditDialog;
import org.faktorips.devtools.core.ui.editors.IpsPartsComposite;
import org.faktorips.devtools.core.ui.editors.SimpleIpsPartsSection;
import org.faktorips.util.ArgumentCheck;

/**
 * The ui section for the enum type structure page that contains the enum attributes of the enum
 * type to be edited.
 * 
 * @see EnumTypeStructurePage
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
public class EnumAttributesSection extends SimpleIpsPartsSection {

    /**
     * Creates a new <code>EnumAttributesSection</code> containing the enum attributes of the given
     * enum type.
     * 
     * @param enumType The enum type to show the enum attributes from.
     * @param parent The parent ui composite.
     * @param toolkit The ui toolkit that shall be used to create ui elements.
     */
    public EnumAttributesSection(IEnumType enumType, Composite parent, UIToolkit toolkit) {
        super(enumType, parent, Messages.EnumAttributesSection_title, toolkit);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected IpsPartsComposite createIpsPartsComposite(Composite parent, UIToolkit toolkit) {
        return new EnumAttributesComposite((IEnumType)getIpsObject(), parent, toolkit);
    }

    /**
     * A composite that shows an enum type's attributes in a viewer and allows to edit these
     * attributes in a dialog, to create new attributes, move attributes and to delete attributes.
     */
    private class EnumAttributesComposite extends IpsPartsComposite {

        /** The enum type being edited by the editor. */
        private IEnumType enumType;

        /**
         * Creates a new <code>EnumAttributesComposite</code> based upon the attributes of the given
         * enum type.
         * 
         * @param enumType The enum type to show the attributes of.
         * @param parent The parent ui composite.
         * @param toolkit The ui toolkit to create new ui elements with.
         * 
         * @throws NullPointerException If <code>enumType</code> is <code>null</code>.
         */
        public EnumAttributesComposite(IEnumType enumType, Composite parent, UIToolkit toolkit) {
            super(enumType, parent, toolkit);

            ArgumentCheck.notNull(enumType);

            this.enumType = enumType;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected IStructuredContentProvider createContentProvider() {
            return new IStructuredContentProvider() {

                public Object[] getElements(Object inputElement) {
                    return enumType.getEnumAttributesIncludeSupertypeCopies().toArray();
                }

                public void dispose() {
                    // nothing todo
                }

                public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
                    // nothing todo
                }

            };
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected EditDialog createEditDialog(IIpsObjectPart part, Shell shell) throws CoreException {
            return new EnumAttributeEditDialog((IEnumAttribute)part, shell);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected IIpsObjectPart newIpsPart() throws CoreException {
            IEnumAttribute newEnumAttribute = enumType.newEnumAttribute();

            /*
             * If this is the first attribute to be created and the values are being defined in the
             * enum type then make sure that there will be one enum value available for editing.
             */
            if (enumType.getEnumAttributesCount(true) == 1) {
                if (enumType.isContainingValues() && !(enumType.isAbstract())) {
                    if (enumType.getEnumValuesCount() == 0) {
                        enumType.newEnumValue();
                    }
                }
            }

            return newEnumAttribute;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void deleteIpsPart(IIpsObjectPart partToDelete) throws CoreException {
            IEnumAttribute enumAttributeToDelete = (IEnumAttribute)partToDelete;
            enumType.deleteEnumAttributeWithValues(enumAttributeToDelete);

            // Delete all enum values if there are no more enum attributes
            if (enumType.getEnumAttributesCount(true) == 0) {
                for (IEnumValue currentEnumValue : enumType.getEnumValues()) {
                    currentEnumValue.delete();
                }
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected int[] moveParts(int[] indexes, boolean up) {
            int newIndex = indexes[0];
            List<IEnumAttribute> enumAttributes = enumType.getEnumAttributesIncludeSupertypeCopies();

            IEnumAttribute enumAttributeToMove = enumAttributes.get(newIndex);
            try {
                newIndex = enumType.moveEnumAttribute(enumAttributeToMove, up);
            } catch (CoreException e) {
                throw new RuntimeException(e);
            }

            return new int[] { newIndex };
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected boolean createButtons(Composite buttons, UIToolkit toolkit) {
            super.createButtons(buttons, toolkit);

            createButtonSpace(buttons, toolkit);

            Button inheritButton = toolkit.createButton(buttons, Messages.EnumAttributessection_buttonInherit);
            inheritButton
                    .setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING));
            inheritButton.addSelectionListener(new SelectionListener() {
                public void widgetSelected(SelectionEvent e) {
                    // TODO aw: open the InheritAttributesDialog.
                }

                public void widgetDefaultSelected(SelectionEvent e) {

                }
            });

            return true;
        }

    }

}
