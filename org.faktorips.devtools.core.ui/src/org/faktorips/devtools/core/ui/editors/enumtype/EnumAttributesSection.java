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
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.devtools.core.model.enums.IEnumAttribute;
import org.faktorips.devtools.core.model.enums.IEnumLiteralNameAttribute;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.enums.IEnumValue;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.editors.EditDialog;
import org.faktorips.devtools.core.ui.editors.IpsPartsComposite;
import org.faktorips.devtools.core.ui.editors.SimpleIpsPartsSection;
import org.faktorips.util.ArgumentCheck;

/**
 * The UI section for the <tt>EnumTypeStructurePage</tt> that contains the <tt>IEnumAttribute</tt>s
 * of the <tt>IEnumType</tt> to be edited.
 * 
 * @see EnumTypeStructurePage
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
public class EnumAttributesSection extends SimpleIpsPartsSection {

    /**
     * Creates a new <tt>EnumAttributesSection</tt> containing the <tt>IEnumAttribute</tt>s of the
     * given <tt>IEnumType</tt>.
     * 
     * @param enumType The <tt>IEnumType</tt> to show the <tt>IEnumAttribute</tt>s from.
     * @param parent The parent UI composite.
     * @param toolkit The UI toolkit that shall be used to create UI elements.
     */
    public EnumAttributesSection(IEnumType enumType, Composite parent, UIToolkit toolkit) {
        super(enumType, parent, Messages.EnumAttributesSection_title, toolkit);
    }

    @Override
    protected IpsPartsComposite createIpsPartsComposite(Composite parent, UIToolkit toolkit) {
        return new EnumAttributesComposite((IEnumType)getIpsObject(), parent, toolkit);
    }

    /**
     * A composite that shows an <tt>IEnumType</tt>'s attributes in a viewer and allows to edit
     * these attributes in a dialog, to create new attributes, move attributes and to delete
     * attributes.
     */
    private class EnumAttributesComposite extends IpsPartsComposite implements ISelectionChangedListener {

        /** The <tt>IEnumType</tt> being edited by the editor. */
        private IEnumType enumType;

        /**
         * Creates a new <tt>EnumAttributesComposite</tt> based upon the attributes of the given
         * <tt>IEnumType</tt>.
         * 
         * @param enumType The <tt>IEnumType</tt> to show the <tt>IEnumAttribute</tt>s of.
         * @param parent The parent UI composite.
         * @param toolkit The UI toolkit to create new UI elements with.
         * 
         * @throws NullPointerException If <tt>enumType</tt> is <tt>null</tt>.
         */
        public EnumAttributesComposite(IEnumType enumType, Composite parent, UIToolkit toolkit) {
            super(enumType, parent, toolkit);
            ArgumentCheck.notNull(enumType);
            this.enumType = enumType;
            addSelectionChangedListener(this);
        }

        @Override
        protected IStructuredContentProvider createContentProvider() {
            return new IStructuredContentProvider() {

                public Object[] getElements(Object inputElement) {
                    return enumType.getEnumAttributesIncludeSupertypeCopies(true).toArray();
                }

                public void dispose() {
                    // Nothing to do.
                }

                public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
                    // Nothing to do.
                }

            };
        }

        @Override
        protected EditDialog createEditDialog(IIpsObjectPart part, Shell shell) throws CoreException {
            return new EnumAttributeEditDialog((IEnumAttribute)part, shell);
        }

        @Override
        protected IIpsObjectPart newIpsPart() throws CoreException {
            IEnumAttribute newEnumAttribute = enumType.newEnumAttribute();

            /*
             * If this is the first attribute to be created and the values are being defined in the
             * EnumType then make sure that there will be one EnumValue available for editing.
             */
            if (enumType.getEnumAttributesCountIncludeSupertypeCopies(true) == 1) {
                if (enumType.isContainingValues() && !(enumType.isAbstract())) {
                    if (enumType.getEnumValuesCount() == 0) {
                        enumType.newEnumValue();
                    }
                }
            }

            return newEnumAttribute;
        }

        @Override
        protected void deleteIpsPart(IIpsObjectPart partToDelete) throws CoreException {
            IEnumAttribute enumAttributeToDelete = (IEnumAttribute)partToDelete;
            enumType.deleteEnumAttributeWithValues(enumAttributeToDelete);

            // Delete all EnumValues if there are no more EnumAttributes.
            if (enumType.getEnumAttributesCountIncludeSupertypeCopies(enumType.isUsingEnumLiteralNameAttribute()) == 0) {
                for (IEnumValue currentEnumValue : enumType.getEnumValues()) {
                    currentEnumValue.delete();
                }
            }
        }

        @Override
        protected int[] moveParts(int[] indexes, boolean up) {
            int newIndex = indexes[0];
            List<IEnumAttribute> enumAttributes = enumType.getEnumAttributesIncludeSupertypeCopies(true);

            IEnumAttribute enumAttributeToMove = enumAttributes.get(newIndex);
            try {
                newIndex = enumType.moveEnumAttribute(enumAttributeToMove, up);
            } catch (CoreException e) {
                throw new RuntimeException(e);
            }

            return new int[] { newIndex };
        }

        @Override
        protected boolean createButtons(Composite buttons, UIToolkit toolkit) {
            super.createButtons(buttons, toolkit);
            createButtonSpace(buttons, toolkit);

            // TODO AW: out commented for release 2.3.0rfinal
            /*
             * Button inheritButton = toolkit.createButton(buttons,
             * Messages.EnumAttributessection_buttonInherit); inheritButton .setLayoutData(new
             * GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING));
             * inheritButton.addSelectionListener(new SelectionListener() { public void
             * widgetSelected(SelectionEvent e) { inheritClicked(); }
             * 
             * public void widgetDefaultSelected(SelectionEvent e) {
             * 
             * } });
             */

            return true;
        }

        /**
         * {@inheritDoc}
         * <p>
         * Disables the "Delete" - Button if the selected <tt>IIpsObjectPart</tt> is an
         * <tt>IEnumLiteralNameAttribute</tt>.
         */
        public void selectionChanged(SelectionChangedEvent event) {
            setCanDelete(true);
            if (getSelectedPart() instanceof IEnumLiteralNameAttribute && enumType.isUsingEnumLiteralNameAttribute()) {
                setCanDelete(false);
            }
        }

        /**
         * Opens a dialog enabling the user to inherit <tt>IEnumAttribute</tt>s from the supertype
         * hierarchy in a comfortable way.
         */
        // TODO AW: out commented for release 2.3.0.rfinal
        /*
         * private void inheritClicked() {
         * 
         * }
         */

    }

}
