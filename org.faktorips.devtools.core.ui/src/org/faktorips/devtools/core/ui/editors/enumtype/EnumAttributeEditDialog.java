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
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.model.enums.IEnumAttribute;
import org.faktorips.devtools.core.model.ipsobject.IExtensionPropertyDefinition;
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
        final Text nameText = uiToolkit.createText(workArea);
        bindingContext.bindContent(nameText, enumAttribute, IEnumAttribute.PROPERTY_NAME);

        // Datatype
        uiToolkit.createFormLabel(workArea, Messages.EnumAttributeEditDialog_labelDatatype);
        final DatatypeRefControl datatypeControl = uiToolkit.createDatatypeRefEdit(enumAttribute.getIpsProject(),
                workArea);
        datatypeControl.setVoidAllowed(false);
        datatypeControl.setPrimitivesAllowed(false);
        datatypeControl.setOnlyValueDatatypesAllowed(true);
        bindingContext.bindContent(datatypeControl, enumAttribute, IEnumAttribute.PROPERTY_DATATYPE);

        // Identifier
        uiToolkit.createFormLabel(workArea, Messages.EnumAttributeEditDialog_labelIsIdentifier);
        Checkbox identifierCheckbox = uiToolkit.createCheckbox(workArea);
        bindingContext.bindContent(identifierCheckbox, enumAttribute, IEnumAttribute.PROPERTY_IDENTIFIER);

        // Inherited
        uiToolkit.createFormLabel(workArea, Messages.EnumAttributeEditDialog_labelIsInherited);
        Checkbox inheritedCheckbox = uiToolkit.createCheckbox(workArea);
        bindingContext.bindContent(inheritedCheckbox, enumAttribute, IEnumAttribute.PROPERTY_INHERITED);

        // Create extension properties on position bottom
        extFactory.createControls(workArea, uiToolkit, enumAttribute, IExtensionPropertyDefinition.POSITION_BOTTOM);
        extFactory.bind(bindingContext);

        /*
         * Set the focus into the name field and register listeners to select all contents of the
         * name field and the datatype field when gaining focus trough tabbing for better usability.
         */
        nameText.addListener(SWT.FocusIn, new Listener() {
            public void handleEvent(Event e) {
                nameText.selectAll();
            }
        });
        datatypeControl.getTextControl().addListener(SWT.FocusIn, new Listener() {
            public void handleEvent(Event e) {
                datatypeControl.getTextControl().selectAll();
            }
        });
        nameText.setFocus();

        return control;
    }

}
