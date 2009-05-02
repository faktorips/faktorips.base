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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.enums.IEnumAttribute;
import org.faktorips.devtools.core.model.ipsobject.IExtensionPropertyDefinition;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.ui.ExtensionPropertyControlFactory;
import org.faktorips.devtools.core.ui.controls.Checkbox;
import org.faktorips.devtools.core.ui.controls.DatatypeRefControl;
import org.faktorips.devtools.core.ui.editors.IpsPartEditDialog2;

/**
 * Dialog to edit an <code>IEnumAttribute</code> of an <code>IEnumType</code>.
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
public class EnumAttributeEditDialog extends IpsPartEditDialog2 {

    /** The enum attribute being edited. */
    private IEnumAttribute enumAttribute;

    /** The extension property factory that may extend the controls. */
    private ExtensionPropertyControlFactory extFactory;

    /** The ui control to set the <code>datatype</code> property. */
    private DatatypeRefControl datatypeControl;

    /** The ui control to set the <code>literalName</code> property. */
    private Checkbox literalNameCheckbox;

    /** The ui control to set the <code>uniqueIdentifier</code> property. */
    private Checkbox uniqueIdentifierCheckbox;

    /** The ui control to set the <code>usedAsIdInFaktorIpsUi</code> property. */
    private Checkbox usedAsIdInFaktorIpsUiCheckbox;

    /** The ui control to set the <code>usedAsNameInFaktorIpsUi</code> property. */
    private Checkbox usedAsNameInFaktorIpsUiCheckbox;

    /**
     * Creates a new <code>EnumAttributeEditDialog</code> for the user to edit the given enum
     * attribute with.
     * 
     * @param part The enum attribute to edit with the dialog.
     * @param parentShell The parent ui shell.
     */
    public EnumAttributeEditDialog(IEnumAttribute enumAttribute, Shell parentShell) {
        super(enumAttribute, parentShell, Messages.EnumAttributeEditDialog_title, true);

        this.enumAttribute = enumAttribute;
        this.extFactory = new ExtensionPropertyControlFactory(enumAttribute.getClass());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Composite createWorkArea(Composite parent) throws CoreException {
        TabFolder tabFolder = (TabFolder)parent;

        TabItem page = new TabItem(tabFolder, SWT.NONE);
        page.setText(Messages.EnumAttributeEditDialog_generalTitle);
        page.setControl(createGeneralPage(tabFolder));

        createDescriptionTabItem(tabFolder);

        return tabFolder;
    }

    /** Creates the general tab. */
    private Control createGeneralPage(TabFolder tabFolder) {
        Composite control = createTabItemComposite(tabFolder, 1, false);
        Composite workArea = uiToolkit.createLabelEditColumnComposite(control);

        // Create extension properties on position top
        extFactory.createControls(workArea, uiToolkit, enumAttribute, IExtensionPropertyDefinition.POSITION_TOP);

        // Name
        uiToolkit.createFormLabel(workArea, Messages.EnumAttributeEditDialog_labelName);
        Text nameText = uiToolkit.createText(workArea);
        bindingContext.bindContent(nameText, enumAttribute, IEnumAttribute.PROPERTY_NAME);

        // Datatype
        uiToolkit.createFormLabel(workArea, Messages.EnumAttributeEditDialog_labelDatatype);
        datatypeControl = uiToolkit.createDatatypeRefEdit(enumAttribute.getIpsProject(), workArea);
        datatypeControl.setVoidAllowed(false);
        datatypeControl.setPrimitivesAllowed(false);
        datatypeControl.setOnlyValueDatatypesAllowed(true);

        // Literal name
        uiToolkit.createFormLabel(workArea, Messages.EnumAttributeEditDialog_labelUseAsLiteralName);
        literalNameCheckbox = uiToolkit.createCheckbox(workArea);

        // Unique identifier
        uiToolkit.createFormLabel(workArea, Messages.EnumAttributeEditDialog_labelUniqueIdentifier);
        uniqueIdentifierCheckbox = uiToolkit.createCheckbox(workArea);

        // Used as ID in Faktor-IPS UI
        uiToolkit.createFormLabel(workArea, Messages.EnumAttributeEditDialog_labelUsedAsIdInFaktorIpsUi);
        usedAsIdInFaktorIpsUiCheckbox = uiToolkit.createCheckbox(workArea);

        // Used as name in Faktor-IPS UI
        uiToolkit.createFormLabel(workArea, Messages.EnumAttributeEditDialog_labelUsedAsNameInFaktorIpsUi);
        usedAsNameInFaktorIpsUiCheckbox = uiToolkit.createCheckbox(workArea);

        // Inherited
        uiToolkit.createFormLabel(workArea, Messages.EnumAttributeEditDialog_labelIsInherited);
        Checkbox inheritedCheckbox = uiToolkit.createCheckbox(workArea);
        bindingContext.bindContent(inheritedCheckbox, enumAttribute, IEnumAttribute.PROPERTY_INHERITED);

        // Content bindings dependend on inherited property
        bindAndSetContentDependendOnInheritedProperty(enumAttribute);

        // Create extension properties on position bottom
        extFactory.createControls(workArea, uiToolkit, enumAttribute, IExtensionPropertyDefinition.POSITION_BOTTOM);
        extFactory.bind(bindingContext);

        // Set the focus into the name field for better usability.
        nameText.setFocus();

        return control;
    }

    /**
     * If the enum attribute to be edited is inherited from the supertype hierarchy the
     * <code>datatype</code>, <code>useAsLiteralName</code>, <code>uniqueIdentifier</code>,
     * <code>useAsIdInFaktorIpsUi</code> and <code>useAsNameInFaktorIpsUi</code> fields will not be
     * bound to the respective properties and their content will be set to the values of the
     * respective super enum attribute if such can be found.
     * <p>
     * If the enum attribute to be edited is not inherited from the supertype hierarchy the fields
     * will be bound to the respective properties.
     */
    private void bindAndSetContentDependendOnInheritedProperty(IEnumAttribute enumAttribute) {
        if (!(enumAttribute.isInherited())) {
            bindingContext.bindContent(datatypeControl, enumAttribute, IEnumAttribute.PROPERTY_DATATYPE);
            bindingContext.bindContent(literalNameCheckbox, enumAttribute, IEnumAttribute.PROPERTY_LITERAL_NAME);
            bindingContext.bindContent(uniqueIdentifierCheckbox, enumAttribute,
                    IEnumAttribute.PROPERTY_UNIQUE_IDENTIFIER);
            bindingContext.bindContent(usedAsIdInFaktorIpsUiCheckbox, enumAttribute,
                    IEnumAttribute.PROPERTY_USED_AS_ID_IN_FAKTOR_IPS_UI);
            bindingContext.bindContent(usedAsNameInFaktorIpsUiCheckbox, enumAttribute,
                    IEnumAttribute.PROPERTY_USED_AS_NAME_IN_FAKTOR_IPS_UI);
        } else {
            bindingContext.removeBindings(datatypeControl);
            bindingContext.removeBindings(literalNameCheckbox);
            bindingContext.removeBindings(uniqueIdentifierCheckbox);
            bindingContext.removeBindings(usedAsIdInFaktorIpsUiCheckbox);
            bindingContext.removeBindings(usedAsNameInFaktorIpsUiCheckbox);
            bindingContext.bindEnabled(datatypeControl, enumAttribute, IEnumAttribute.PROPERTY_INHERITED, false);
            bindingContext.bindEnabled(uniqueIdentifierCheckbox, enumAttribute, IEnumAttribute.PROPERTY_INHERITED,
                    false);
            bindingContext.bindEnabled(literalNameCheckbox, enumAttribute, IEnumAttribute.PROPERTY_INHERITED, false);
            bindingContext.bindEnabled(usedAsIdInFaktorIpsUiCheckbox, enumAttribute, IEnumAttribute.PROPERTY_INHERITED,
                    false);
            bindingContext.bindEnabled(usedAsNameInFaktorIpsUiCheckbox, enumAttribute,
                    IEnumAttribute.PROPERTY_INHERITED, false);

            try {
                IIpsProject ipsProject = enumAttribute.getIpsProject();
                Datatype datatype = enumAttribute.findDatatype(ipsProject);
                Boolean literalName = enumAttribute.findIsLiteralName();
                Boolean uniqueIdentifier = enumAttribute.findIsUniqueIdentifier();
                Boolean usedAsIdInFaktorIpsUi = enumAttribute.findIsUsedAsIdInFaktorIpsUi();
                Boolean usedAsNameInFaktorIpsUi = enumAttribute.findIsUsedAsNameInFaktorIpsUi();
                String datatypeString = (datatype == null) ? "" : datatype.getName();
                boolean literalNameBoolean = (literalName == null) ? false : literalName.booleanValue();
                boolean uniqueIdentifierBoolean = (uniqueIdentifier == null) ? false : uniqueIdentifier.booleanValue();
                boolean usedAsIdInFaktorIpsUiBoolean = (usedAsIdInFaktorIpsUi == null) ? false : usedAsIdInFaktorIpsUi
                        .booleanValue();
                boolean usedAsNameInFaktorIpsUiBoolean = (usedAsNameInFaktorIpsUi == null) ? false
                        : usedAsNameInFaktorIpsUi.booleanValue();
                datatypeControl.setText(datatypeString);
                literalNameCheckbox.setChecked(literalNameBoolean);
                uniqueIdentifierCheckbox.setChecked(uniqueIdentifierBoolean);
                usedAsIdInFaktorIpsUiCheckbox.setChecked(usedAsIdInFaktorIpsUiBoolean);
                usedAsNameInFaktorIpsUiCheckbox.setChecked(usedAsNameInFaktorIpsUiBoolean);
            } catch (CoreException e) {
                throw new RuntimeException(e);
            }
        }

        // Update the ui
        // TODO aw: sometimes this works, sometimes not, somewhat crazy stuff here ;(
        bindingContext.updateUI();
        if (dialogArea != null) {
            dialogArea.redraw();
            dialogArea.update();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void contentsChanged(ContentChangeEvent event) {
        super.contentsChanged(event);

        IEnumAttribute changedPart = (IEnumAttribute)event.getPart();
        if (changedPart == null) {
            return;
        }

        if (changedPart.equals(enumAttribute)) {
            bindAndSetContentDependendOnInheritedProperty(changedPart);
        }
    }

}
