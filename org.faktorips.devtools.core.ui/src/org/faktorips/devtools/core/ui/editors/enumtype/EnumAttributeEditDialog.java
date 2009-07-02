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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.enums.EnumTypeDatatypeAdapter;
import org.faktorips.devtools.core.model.enums.IEnumAttribute;
import org.faktorips.devtools.core.model.enums.IEnumType;
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

    /** The UI control to set the <code>name</code> property. */
    private Text nameText;

    /** The UI control to set the <code>datatype</code> property. */
    private DatatypeRefControl datatypeControl;

    /** The UI control to set the <code>literalName</code> property. */
    private Checkbox literalNameCheckbox;

    /** The UI control to set the <code>uniqueIdentifier</code> property. */
    private Checkbox uniqueIdentifierCheckbox;

    /** The UI control to set the <code>usedAsIdInFaktorIpsUi</code> property. */
    private Checkbox usedAsIdInFaktorIpsUiCheckbox;

    /** The UI control to set the <code>usedAsNameInFaktorIpsUi</code> property. */
    private Checkbox usedAsNameInFaktorIpsUiCheckbox;

    /** This is used to track changes of the inherited property of the enum attribute to be edited. */
    private boolean inheritedProperty;

    /** The canvas. */
    private Composite workArea;

    /**
     * This is used to track changes of the literal name property of the enum attribute to be
     * edited.
     */
    private boolean literalNameProperty;

    /**
     * Creates a new <code>EnumAttributeEditDialog</code> for the user to edit the given enum
     * attribute with.
     * 
     * @param part The enum attribute to edit with the dialog.
     * @param parentShell The parent UI shell.
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
        IEnumAttribute enumAttribute = (IEnumAttribute)getIpsPart();
        inheritedProperty = enumAttribute.isInherited();
        literalNameProperty = enumAttribute.isLiteralName();

        Composite control = createTabItemComposite(tabFolder, 1, false);
        workArea = uiToolkit.createLabelEditColumnComposite(control);

        // Create extension properties on position top.
        extFactory.createControls(workArea, uiToolkit, enumAttribute, IExtensionPropertyDefinition.POSITION_TOP);

        createFields();
        createFaktorIpsUiGroup();

        // Content bindings dependent on inherited property.
        bindAndSetContentDependendOnInheritedProperty(enumAttribute);

        // Create extension properties on position bottom.
        extFactory.createControls(workArea, uiToolkit, enumAttribute, IExtensionPropertyDefinition.POSITION_BOTTOM);
        extFactory.bind(bindingContext);

        // Set the focus into the name field for better usability.
        nameText.setFocus();

        return control;
    }

    /** Creates the ui fields. */
    private void createFields() {
        // Name.
        uiToolkit.createFormLabel(workArea, Messages.EnumAttributeEditDialog_labelName);
        nameText = uiToolkit.createText(workArea);

        // Datatype.
        uiToolkit.createFormLabel(workArea, Messages.EnumAttributeEditDialog_labelDatatype);
        datatypeControl = uiToolkit.createDatatypeRefEdit(enumAttribute.getIpsProject(), workArea);
        datatypeControl.setVoidAllowed(false);
        datatypeControl.setPrimitivesAllowed(false);
        datatypeControl.setOnlyValueDatatypesAllowed(true);
        filterDatatypes();

        Composite marginComposite = uiToolkit.createGridComposite(workArea.getParent(), 2, true, false);
        ((GridLayout)marginComposite.getLayout()).marginTop = 14;

        // Unique identifier.
        uiToolkit.createFormLabel(marginComposite, Messages.EnumAttributeEditDialog_labelUniqueIdentifier);
        uniqueIdentifierCheckbox = uiToolkit.createCheckbox(marginComposite);

        // Literal name
        uiToolkit.createFormLabel(marginComposite, Messages.EnumAttributeEditDialog_labelUseAsLiteralName);
        literalNameCheckbox = uiToolkit.createCheckbox(marginComposite);

        // Inherited.
        uiToolkit.createFormLabel(marginComposite, Messages.EnumAttributeEditDialog_labelIsInherited);
        Checkbox inheritedCheckbox = uiToolkit.createCheckbox(marginComposite);
        bindingContext.bindContent(inheritedCheckbox, enumAttribute, IEnumAttribute.PROPERTY_INHERITED);
    }

    /** Creates the Faktor-IPS UI group. */
    private void createFaktorIpsUiGroup() {
        Composite marginComposite = uiToolkit.createGridComposite(workArea.getParent(), 1, false, false);
        ((GridLayout)marginComposite.getLayout()).marginTop = 14;
        Group uiGroup = uiToolkit.createGridGroup(marginComposite, "Faktor-IPS UI", 2, true);

        // Used as ID in Faktor-IPS UI.
        uiToolkit.createFormLabel(uiGroup, Messages.EnumAttributeEditDialog_labelUsedAsIdInFaktorIpsUi);
        usedAsIdInFaktorIpsUiCheckbox = uiToolkit.createCheckbox(uiGroup);

        // Used as name in Faktor-IPS UI.
        uiToolkit.createFormLabel(uiGroup, Messages.EnumAttributeEditDialog_labelUsedAsNameInFaktorIpsUi);
        usedAsNameInFaktorIpsUiCheckbox = uiToolkit.createCheckbox(uiGroup);
    }

    /**
     * Searches all the enum types that are subclasses of the enum type that is the parent of the
     * enum attribute to edit. All those sub enum types and the parent enum type itself will not be
     * available in the datatype selection.
     * <p>
     * Also, if the parent enum type does contain values all enum types that do not contain values
     * will be disallowed to select, too.
     */
    private void filterDatatypes() {
        IEnumType parentEnumType = enumAttribute.getEnumType();
        List<Datatype> disallowedDatatypes = new ArrayList<Datatype>();

        // Disallow parent enum type.
        disallowedDatatypes.add(new EnumTypeDatatypeAdapter(parentEnumType, null));

        // Disallow sub enum types of the parent enum type.
        try {
            for (IEnumType subTypes : parentEnumType.findAllSubEnumTypes(parentEnumType.getIpsProject())) {
                disallowedDatatypes.add(new EnumTypeDatatypeAdapter(subTypes, null));
            }
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }

        // Disallow enum types not containing values if parent enum type does contain values.
        if (parentEnumType.isContainingValues()) {
            try {
                Datatype[] datatypes = parentEnumType.getIpsProject().findDatatypes(true, false);
                for (Datatype currentDatatype : datatypes) {
                    if (currentDatatype instanceof EnumTypeDatatypeAdapter) {
                        EnumTypeDatatypeAdapter enumDatatypeAdapter = (EnumTypeDatatypeAdapter)currentDatatype;
                        IEnumType enumDatatype = enumDatatypeAdapter.getEnumType();
                        if (!(enumDatatype.isContainingValues())) {
                            if (!(disallowedDatatypes.contains(currentDatatype))) {
                                disallowedDatatypes.add(currentDatatype);
                            }
                        }
                    }
                }
            } catch (CoreException e) {
                throw new RuntimeException(e);
            }
        }

        datatypeControl.setDisallowedDatatypes(disallowedDatatypes);
    }

    /**
     * If the enum attribute to be edited is inherited from the supertype hierarchy the
     * <code>name</code>, <code>datatype</code>, <code>useAsLiteralName</code>,
     * <code>uniqueIdentifier</code>, <code>useAsIdInFaktorIpsUi</code> and
     * <code>useAsNameInFaktorIpsUi</code> fields will not be bound to the respective properties and
     * their content will be set to the values of the respective super enum attribute if such can be
     * found.
     * <p>
     * If the enum attribute to be edited is not inherited from the supertype hierarchy the fields
     * will be bound to the respective properties.
     */
    private void bindAndSetContentDependendOnInheritedProperty(IEnumAttribute enumAttribute) {
        if (!(enumAttribute.isInherited())) {
            bindingContext.bindContent(nameText, enumAttribute, IEnumAttribute.PROPERTY_NAME);
            bindingContext.bindContent(datatypeControl, enumAttribute, IEnumAttribute.PROPERTY_DATATYPE);
            bindingContext.bindContent(literalNameCheckbox, enumAttribute, IEnumAttribute.PROPERTY_LITERAL_NAME);
            bindingContext.bindContent(uniqueIdentifierCheckbox, enumAttribute,
                    IEnumAttribute.PROPERTY_UNIQUE_IDENTIFIER);
            bindingContext.bindContent(usedAsIdInFaktorIpsUiCheckbox, enumAttribute,
                    IEnumAttribute.PROPERTY_USED_AS_ID_IN_FAKTOR_IPS_UI);
            bindingContext.bindContent(usedAsNameInFaktorIpsUiCheckbox, enumAttribute,
                    IEnumAttribute.PROPERTY_USED_AS_NAME_IN_FAKTOR_IPS_UI);
        } else {
            bindingContext.removeBindings(nameText);
            bindingContext.removeBindings(datatypeControl);
            bindingContext.removeBindings(literalNameCheckbox);
            bindingContext.removeBindings(uniqueIdentifierCheckbox);
            bindingContext.removeBindings(usedAsIdInFaktorIpsUiCheckbox);
            bindingContext.removeBindings(usedAsNameInFaktorIpsUiCheckbox);
            bindingContext.bindEnabled(nameText, enumAttribute, IEnumAttribute.PROPERTY_INHERITED, false);
            bindingContext.bindEnabled(datatypeControl, enumAttribute, IEnumAttribute.PROPERTY_INHERITED, false);
            bindingContext.bindEnabled(uniqueIdentifierCheckbox, enumAttribute, IEnumAttribute.PROPERTY_INHERITED,
                    false);
            bindingContext.bindEnabled(literalNameCheckbox, enumAttribute, IEnumAttribute.PROPERTY_INHERITED, false);
            bindingContext.bindEnabled(usedAsIdInFaktorIpsUiCheckbox, enumAttribute, IEnumAttribute.PROPERTY_INHERITED,
                    false);
            bindingContext.bindEnabled(usedAsNameInFaktorIpsUiCheckbox, enumAttribute,
                    IEnumAttribute.PROPERTY_INHERITED, false);

            // Obtain the properties from the super enum attribute.
            try {
                IIpsProject ipsProject = enumAttribute.getIpsProject();
                String name = enumAttribute.getName();
                Datatype datatype = enumAttribute.findDatatype(ipsProject);
                Boolean literalName = enumAttribute.findIsLiteralName(ipsProject);
                Boolean uniqueIdentifier = enumAttribute.findIsUniqueIdentifier(ipsProject);
                Boolean usedAsIdInFaktorIpsUi = enumAttribute.findIsUsedAsIdInFaktorIpsUi(ipsProject);
                Boolean usedAsNameInFaktorIpsUi = enumAttribute.findIsUsedAsNameInFaktorIpsUi(ipsProject);
                String datatypeString = (datatype == null) ? "" : datatype.getName();
                boolean literalNameBoolean = (literalName == null) ? false : literalName.booleanValue();
                boolean uniqueIdentifierBoolean = (uniqueIdentifier == null) ? false : uniqueIdentifier.booleanValue();
                boolean usedAsIdInFaktorIpsUiBoolean = (usedAsIdInFaktorIpsUi == null) ? false : usedAsIdInFaktorIpsUi
                        .booleanValue();
                boolean usedAsNameInFaktorIpsUiBoolean = (usedAsNameInFaktorIpsUi == null) ? false
                        : usedAsNameInFaktorIpsUi.booleanValue();
                nameText.setText(name);
                datatypeControl.setText(datatypeString);
                literalNameCheckbox.setChecked(literalNameBoolean);
                uniqueIdentifierCheckbox.setChecked(uniqueIdentifierBoolean);
                usedAsIdInFaktorIpsUiCheckbox.setChecked(usedAsIdInFaktorIpsUiBoolean);
                usedAsNameInFaktorIpsUiCheckbox.setChecked(usedAsNameInFaktorIpsUiBoolean);
            } catch (CoreException e) {
                throw new RuntimeException(e);
            }
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
            IEnumAttribute changedEnumAttribute = (IEnumAttribute)changedPart;

            // Ensure correct enabling / disabling of the inherited fields.
            if (changedEnumAttribute.isInherited() != inheritedProperty) {
                bindAndSetContentDependendOnInheritedProperty(changedEnumAttribute);
                inheritedProperty = !inheritedProperty;
            }

            // Ensure checked unique identifier when checking literal name.
            boolean literalName = changedEnumAttribute.isLiteralName();
            if (literalName != literalNameProperty) {
                if (literalName) {
                    changedEnumAttribute.setUniqueIdentifier(true);
                }
                literalNameProperty = literalName;
            }

            bindingContext.updateUI();
            dialogArea.redraw();
            dialogArea.update();
        }
    }

}
