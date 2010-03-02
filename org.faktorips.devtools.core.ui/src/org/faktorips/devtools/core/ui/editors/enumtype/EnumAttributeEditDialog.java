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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.enums.EnumTypeDatatypeAdapter;
import org.faktorips.devtools.core.model.enums.EnumUtil;
import org.faktorips.devtools.core.model.enums.IEnumAttribute;
import org.faktorips.devtools.core.model.enums.IEnumLiteralNameAttribute;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.ipsobject.IExtensionPropertyDefinition;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.ui.ExtensionPropertyControlFactory;
import org.faktorips.devtools.core.ui.controls.Checkbox;
import org.faktorips.devtools.core.ui.controls.DatatypeRefControl;
import org.faktorips.devtools.core.ui.editors.IpsPartEditDialog2;

/**
 * Dialog to edit an <tt>IEnumAttribute</tt> of an <tt>IEnumType</tt>.
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
public class EnumAttributeEditDialog extends IpsPartEditDialog2 {

    /** The <tt>IEnumAttribute</tt> being edited. */
    private IEnumAttribute enumAttribute;

    /** The extension property factory that may extend the controls. */
    private ExtensionPropertyControlFactory extFactory;

    /** The UI control to set the <tt>name</tt> property. */
    private Text nameText;

    /**
     * The UI control to set the <tt>defaultValueProviderAttribute</tt> property (only for
     * <tt>IEnumLiteralNameAttribute</tt>).
     */
    private Text defaultValueProviderAttributeText;

    /** The UI control to set the <tt>datatype</tt> property. */
    private DatatypeRefControl datatypeControl;

    /** The UI control to set the <tt>unique</tt> property. */
    private Checkbox uniqueCheckbox;

    /** The UI control to set the <tt>identifier</tt> property. */
    private Checkbox identifierCheckbox;

    /** The UI control to set the <tt>inherited</tt> property. */
    private Checkbox inheritedCheckbox;

    /** The UI control to set the <tt>usedAsNameInFaktorIpsUi</tt> property. */
    private Checkbox displayNameCheckbox;

    /** The canvas. */
    private Composite workArea;

    /**
     * Flag indicating whether the given <tt>IEnumAttribute</tt> is a
     * <tt>IEnumLiteralNameAttribute</tt> which leads to a variation of this dialog.
     */
    private boolean literalNameAttribute;

    private boolean inherited;

    /**
     * Creates a new <tt>EnumAttributeEditDialog</tt> for the user to edit the given
     * <tt>IEnumAttribute</tt> with.
     * 
     * @param part The <tt>IEnumAttribute</tt> to edit with the dialog.
     * @param parentShell The parent UI shell.
     */
    public EnumAttributeEditDialog(IEnumAttribute enumAttribute, Shell parentShell) {
        super(enumAttribute, parentShell, Messages.EnumAttributeEditDialog_title, true);

        this.enumAttribute = enumAttribute;
        inherited = enumAttribute.isInherited();
        extFactory = new ExtensionPropertyControlFactory(enumAttribute.getClass());
        literalNameAttribute = enumAttribute instanceof IEnumLiteralNameAttribute;
    }

    @Override
    public void setDataChangeable(boolean changeable) {
        if (getDialogArea() != null) {
            if (!(changeable)) {
                super.setDataChangeable(changeable);
            } else {
                if (literalNameAttribute) {
                    super.setDataChangeable(changeable);
                } else {
                    rebindContents();
                }
            }
        }
    }

    @Override
    protected Composite createWorkArea(Composite parent) throws CoreException {
        TabFolder tabFolder = (TabFolder)parent;

        TabItem page = new TabItem(tabFolder, SWT.NONE);
        page.setText(Messages.EnumAttributeEditDialog_generalTitle);
        Control generalPage = createGeneralPage(tabFolder);
        page.setControl(generalPage);

        createDescriptionTabItem(tabFolder);

        return tabFolder;
    }

    /** Creates the general tab. */
    private Control createGeneralPage(TabFolder tabFolder) {
        Composite control = createTabItemComposite(tabFolder, 1, false);
        workArea = uiToolkit.createLabelEditColumnComposite(control);

        // Create extension properties on position top.
        extFactory.createControls(workArea, uiToolkit, enumAttribute, IExtensionPropertyDefinition.POSITION_TOP);

        if (literalNameAttribute) {
            createFieldsForLiteralNameAttribute();
        } else {
            createFieldsForNormalAttribute();
            rebindContents();
        }

        // Create extension properties on position bottom.
        extFactory.createControls(workArea, uiToolkit, enumAttribute, IExtensionPropertyDefinition.POSITION_BOTTOM);
        extFactory.bind(bindingContext);

        // Set the focus into the name field for better usability.
        nameText.setFocus();

        return control;
    }

    /** Creates the UI fields for a <tt>IEnumLiteralNameAttribute</tt>. */
    private void createFieldsForLiteralNameAttribute() {
        // Name
        uiToolkit.createFormLabel(workArea, Messages.EnumAttributeEditDialog_labelName);
        nameText = uiToolkit.createText(workArea);
        bindingContext.bindContent(nameText, enumAttribute, IIpsElement.PROPERTY_NAME);

        // Default Value Provider Attribute
        uiToolkit.createFormLabel(workArea, Messages.EnumAttributeEditDialog_labelDefaultValueProviderAttribute);
        defaultValueProviderAttributeText = uiToolkit.createText(workArea);
        bindingContext.bindContent(defaultValueProviderAttributeText, enumAttribute,
                IEnumLiteralNameAttribute.PROPERTY_DEFAULT_VALUE_PROVIDER_ATTRIBUTE);
    }

    /** Creates the UI fields for a normal <tt>IEnumAttribute</tt>. */
    private void createFieldsForNormalAttribute() {
        // Name
        uiToolkit.createFormLabel(workArea, Messages.EnumAttributeEditDialog_labelName);
        nameText = uiToolkit.createText(workArea);

        // Datatype
        uiToolkit.createFormLabel(workArea, Messages.EnumAttributeEditDialog_labelDatatype);
        datatypeControl = uiToolkit.createDatatypeRefEdit(enumAttribute.getIpsProject(), workArea);
        datatypeControl.setVoidAllowed(false);
        datatypeControl.setPrimitivesAllowed(false);
        datatypeControl.setOnlyValueDatatypesAllowed(true);
        filterDatatypes();

        // Identifier
        uiToolkit.createFormLabel(workArea, Messages.EnumAttributeEditDialog_labelIdentifier);
        identifierCheckbox = uiToolkit.createCheckbox(workArea);

        // Display name
        uiToolkit.createFormLabel(workArea, Messages.EnumAttributeEditDialog_labelDisplayName);
        displayNameCheckbox = uiToolkit.createCheckbox(workArea);

        // Unique
        uiToolkit.createFormLabel(workArea, Messages.EnumAttributeEditDialog_labelUnique);
        uniqueCheckbox = uiToolkit.createCheckbox(workArea);

        // Inherited
        uiToolkit.createFormLabel(workArea, Messages.EnumAttributeEditDialog_labelIsInherited);
        inheritedCheckbox = uiToolkit.createCheckbox(workArea);
    }

    /**
     * Searches all the <tt>IEnumType</tt>s that are subclasses of the <tt>IEnumType</tt> the
     * <tt>IEnumAttribute</tt> to edit belongs to. All those sub <tt>IEnumType</tt>s and the
     * <tt>IEnumType</tt> itself will not be available in the data type selection.
     * <p>
     * Also, if the <tt>IEnumType</tt> does contain values all <tt>IEnumType</tt> that do not
     * contain values will be disallowed to select, too.
     */
    private void filterDatatypes() {
        IEnumType parentEnumType = enumAttribute.getEnumType();
        IIpsProject ipsProject = parentEnumType.getIpsProject();
        List<Datatype> disallowedDatatypes = new ArrayList<Datatype>();

        // Disallow parent EnumType.
        disallowedDatatypes.add(new EnumTypeDatatypeAdapter(parentEnumType, null));

        // Go once over all EnumTypeDatatypeAdapters.
        try {
            for (EnumDatatype currentEnumDatatype : ipsProject.findEnumDatatypes()) {
                if (currentEnumDatatype instanceof EnumTypeDatatypeAdapter) {
                    EnumTypeDatatypeAdapter adapter = (EnumTypeDatatypeAdapter)currentEnumDatatype;
                    IEnumType enumType = adapter.getEnumType();

                    // Continue if it is the parent EnumType that we have already disallowed.
                    if (enumType.equals(parentEnumType)) {
                        continue;
                    }

                    // Disallow if it is not containing values while the parent EnumType does.
                    if (parentEnumType.isContainingValues()) {
                        if (!(enumType.isContainingValues())) {
                            disallowedDatatypes.add(adapter);
                            continue;
                        }
                    }

                    // Disallow if it is a subtype of the parent EnumType.
                    if (enumType.hasSuperEnumType()) {
                        if (enumType.findAllSuperEnumTypes(ipsProject).contains(parentEnumType)) {
                            disallowedDatatypes.add(adapter);
                        }
                    }
                }
            }
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }

        datatypeControl.setDisallowedDatatypes(disallowedDatatypes);
    }

    /**
     * Binds the contents of the fields to the contents of the super enumeration attribute if the
     * <tt>IEnumAttribute</tt> to be edited is marked as being inherited. Otherwise the contens are
     * bound to the respective properties of the attribute.
     * <p>
     * Also handles the enabled states of the fields.
     * <p>
     * Only applicable if the enumeration attribute isn't marked as literal name.
     */
    private void rebindContents() {
        bindingContext.clear();

        bindingContext.bindContent(nameText, enumAttribute, IIpsElement.PROPERTY_NAME);
        if (enumAttribute.isInherited()) {
            obtainContentsFromSuperEnumAttribute();
        } else {
            bindingContext.bindContent(datatypeControl, enumAttribute, IEnumAttribute.PROPERTY_DATATYPE);
            bindingContext.bindContent(identifierCheckbox, enumAttribute, IEnumAttribute.PROPERTY_IDENTIFIER);
            bindingContext.bindContent(displayNameCheckbox, enumAttribute,
                    IEnumAttribute.PROPERTY_USED_AS_NAME_IN_FAKTOR_IPS_UI);
            bindingContext.bindContent(uniqueCheckbox, enumAttribute, IEnumAttribute.PROPERTY_UNIQUE);
        }
        bindingContext.bindContent(inheritedCheckbox, enumAttribute, IEnumAttribute.PROPERTY_INHERITED);

        bindEnabledStates();
    }

    private void obtainContentsFromSuperEnumAttribute() {
        try {
            IIpsProject ipsProject = enumAttribute.getIpsProject();
            Datatype datatype = enumAttribute.findDatatype(ipsProject);
            String datatypeName = (datatype == null) ? "" : datatype.getName();
            datatypeControl.setText(datatypeName);
            uniqueCheckbox.setChecked(EnumUtil.findEnumAttributeIsUnique(enumAttribute, ipsProject));
            identifierCheckbox.setChecked(EnumUtil.findEnumAttributeIsIdentifier(enumAttribute, ipsProject));
            displayNameCheckbox.setChecked(EnumUtil.findEnumAttributeIsUsedAsNameInFaktorIpsUi(enumAttribute,
                    ipsProject));
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

    private void bindEnabledStates() {
        bindingContext.bindEnabled(datatypeControl, enumAttribute, IEnumAttribute.PROPERTY_INHERITED, false);
        bindingContext.bindEnabled(uniqueCheckbox, enumAttribute, IEnumAttribute.PROPERTY_INHERITED, false);
        bindingContext.bindEnabled(identifierCheckbox, enumAttribute, IEnumAttribute.PROPERTY_INHERITED, false);
        bindingContext.bindEnabled(displayNameCheckbox, enumAttribute, IEnumAttribute.PROPERTY_INHERITED, false);
    }

    @Override
    public void contentsChanged(ContentChangeEvent event) {
        super.contentsChanged(event);
        IEnumAttribute changedPart = (IEnumAttribute)event.getPart();
        if (changedPart == null) {
            return;
        }

        if (changedPart.equals(enumAttribute)) {
            if (inherited != enumAttribute.isInherited()) {
                inherited = !(inherited);
                inheritedChanged();
            }
        }
    }

    private void inheritedChanged() {
        if (!(literalNameAttribute)) {
            rebindContents();
            bindingContext.updateUI();
        }
    }

}
