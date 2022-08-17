/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.enumtype;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.fields.CheckboxField;
import org.faktorips.devtools.core.ui.controller.fields.TextButtonField;
import org.faktorips.devtools.core.ui.controller.fields.TextField;
import org.faktorips.devtools.core.ui.controls.IpsObjectRefControl;
import org.faktorips.devtools.core.ui.wizards.IpsObjectPage;
import org.faktorips.devtools.model.enums.IEnumAttribute;
import org.faktorips.devtools.model.enums.IEnumLiteralNameAttribute;
import org.faktorips.devtools.model.enums.IEnumType;
import org.faktorips.devtools.model.internal.enums.EnumTypeValidations;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.Severity;

/**
 * The wizard page for the <code>NewEnumTypeWizard</code>.
 * 
 * @see NewEnumTypeWizard
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
public class EnumTypePage extends IpsObjectPage {

    /** The image for the wizard page. */
    private static final String PAGE_IMAGE = "wizards/NewEnumTypeWizard.png"; //$NON-NLS-1$

    /** The text field to choose the supertype for the new <code>IEnumType</code>. */
    private TextButtonField supertypeField;

    /** The check box field to mark the new <code>IEnumType</code> as being abstract. */
    private CheckboxField isAbstractField;

    /**
     * The check box field to mark the new <code>IEnumType</code> that its values are defined in the
     * model.
     */
    private CheckboxField extensibleField;

    /** The check box field to check if an id attribute should be created by the wizard. */
    private CheckboxField createIdAttributeField;

    /** The check box field to check if a name attribute should be created by the wizard. */
    private CheckboxField createNameAttributeField;

    /** This text field is used to specify the name of the id attribute to be created. */
    private Text idAttributeNameField;

    /** This text field is used to specify the name of the name attribute to be created. */
    private Text nameAttributeNameField;

    /**
     * The text field to specify qualified name for <code>IEnumContent</code> (only enabled if the
     * values are not part of the model and the <code>IEnumType</code> is not abstract).
     */
    private TextField enumContentQualifiedNameField;

    /**
     * Creates the <code>EnumTypePage</code>.
     * 
     * @param selection Active user selection.
     */
    public EnumTypePage(IStructuredSelection selection) {
        super(IpsObjectType.ENUM_TYPE, selection, Messages.Page_Title);
        setImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor(PAGE_IMAGE));
    }

    @Override
    protected void fillNameComposite(Composite nameComposite, UIToolkit toolkit) {
        super.fillNameComposite(nameComposite, toolkit);

        // Create supertype field.
        toolkit.createFormLabel(nameComposite, Messages.Fields_SuperEnumType);
        IpsObjectRefControl superTypeControl = toolkit.createEnumTypeRefControl(null, nameComposite, true);
        supertypeField = new TextButtonField(superTypeControl);
        supertypeField.addChangeListener(this);

        // Create abstract field.
        toolkit.createLabel(nameComposite, StringUtils.EMPTY);
        isAbstractField = new CheckboxField(toolkit.createCheckbox(nameComposite, Messages.Fields_Abstract));
        isAbstractField.addChangeListener(this);
        isAbstractField.addChangeListener($ -> enableEnumContentControls());

        createIdAndNameGenerationFields(nameComposite, toolkit);
    }

    /**
     * Creates the fields that enable the user to specify whether he wants to create an id attribute
     * and a name attribute.
     */
    private void createIdAndNameGenerationFields(Composite nameComposite, UIToolkit uiToolkit) {
        uiToolkit.createLabel(nameComposite, StringUtils.EMPTY);
        Composite createFieldsContainer = uiToolkit.createGridComposite(nameComposite, 2, false, false);

        // Create id attribute.
        createIdAttributeField = new CheckboxField(uiToolkit.createCheckbox(createFieldsContainer,
                Messages.EnumTypePage_labelIdAttribute));
        createIdAttributeField.setValue(true);
        idAttributeNameField = uiToolkit.createText(createFieldsContainer);
        idAttributeNameField.setText("id"); //$NON-NLS-1$
        createIdAttributeField.addChangeListener(
                $ -> idAttributeNameField.setEnabled(createIdAttributeField.getCheckbox().isChecked()));

        // Create name attribute.
        createNameAttributeField = new CheckboxField(uiToolkit.createCheckbox(createFieldsContainer,
                Messages.EnumTypePage_labelNameAttribute));
        createNameAttributeField.setValue(true);
        nameAttributeNameField = uiToolkit.createText(createFieldsContainer);
        nameAttributeNameField.setText("name"); //$NON-NLS-1$
        createNameAttributeField.addChangeListener(
                $ -> nameAttributeNameField.setEnabled(createNameAttributeField.getCheckbox().isChecked()));

        // Disable id and name generation fields if a supertype is specified.
        supertypeField.addChangeListener($ -> {
            boolean enabled = StringUtils.isEmpty(supertypeField.getText());
            createIdAttributeField.getCheckbox().setEnabled(enabled);
            createNameAttributeField.getCheckbox().setEnabled(enabled);
            idAttributeNameField.setEnabled(enabled);
            nameAttributeNameField.setEnabled(enabled);
        });

    }

    /**
     * Enables or disables the <code>rootPackageSpecificationControl</code> and the
     * <code>enumContentQualifiedNameField</code> depending on the values of the
     * <code>isAbstractField</code> and <code>containingValuesField</code>.
     */
    private void enableEnumContentControls() {
        boolean isAbstract = isAbstractField.getValue();
        boolean isExtensible = extensibleField.getValue();
        if (isAbstract) {
            enumContentQualifiedNameField.getTextControl().setEnabled(false);
        } else {
            enumContentQualifiedNameField.getTextControl().setEnabled(isExtensible);
        }
    }

    @Override
    protected boolean useAdditionalComposite() {
        return true;
    }

    @Override
    protected void fillAdditionalComposite(Composite additionalComposite, UIToolkit toolkit) {
        // Set the layout.
        additionalComposite.setLayout(new GridLayout(1, true));
        additionalComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        // Values are part of model.
        extensibleField = new CheckboxField(toolkit.createCheckbox(additionalComposite,
                Messages.Fields_ExtensibleContent));
        GridData data = new GridData();
        data.verticalAlignment = GridData.BEGINNING;
        extensibleField.getControl().setLayoutData(data);

        extensibleField.setValue(false);
        extensibleField.addChangeListener(this);
        extensibleField.addChangeListener($ -> enableEnumContentControls());

        // Qualified name for product side enumeration content.
        toolkit.createFormLabel(additionalComposite, Messages.Fields_EnumContentQualifiedName);
        Text text = toolkit.createText(additionalComposite);
        enumContentQualifiedNameField = new TextField(text);
        enumContentQualifiedNameField.getTextControl().setEnabled(false);
        enumContentQualifiedNameField.addChangeListener(this);
    }

    @Override
    protected void packageChanged() {
        updateEnumContentName();
    }

    @Override
    protected void nameChanged() {
        updateEnumContentName();
    }

    /**
     * Updates the <code>enumContentQualifiedNameField</code> so that it reflects the current
     * package and name of the <code>IEnumType</code>.
     */
    private void updateEnumContentName() {
        // Do only if the check box is not active yet to not destroy any user input.
        if (extensibleField.getCheckbox().isChecked()) {
            return;
        }

        String pack = getPackage();
        String name = getIpsObjectName();
        String point = StringUtils.EMPTY;
        if (pack.length() > 0 && name.length() > 0) {
            point = "."; //$NON-NLS-1$
        } else {
            pack = StringUtils.EMPTY;
        }
        enumContentQualifiedNameField.setText(pack + point + name);
    }

    @Override
    protected void sourceFolderChanged() {
        super.sourceFolderChanged();
        IIpsPackageFragmentRoot root = getIpsPackageFragmentRoot();
        if (root != null) {
            ((IpsObjectRefControl)supertypeField.getControl()).setIpsProjects(Arrays.asList(root.getIpsProject()));
        } else {
            ((IpsObjectRefControl)supertypeField.getControl()).setIpsProjects(new ArrayList<IIpsProject>());
        }
    }

    @Override
    protected void finishIpsObjectsExtension(IIpsObject newIpsObject, Set<IIpsObject> modifiedIpsObjects) {

        IEnumType newEnumType = (IEnumType)newIpsObject;

        // Set properties.
        newEnumType.setAbstract(isAbstractField.getValue());
        newEnumType.setExtensible(extensibleField.getValue().booleanValue());
        newEnumType.setSuperEnumType(supertypeField.getText());
        newEnumType.setEnumContentName(enumContentQualifiedNameField.getText());

        // Inherit EnumAttributes from supertype hierarchy.
        newEnumType.inheritEnumAttributes(newEnumType.findInheritEnumAttributeCandidates(newEnumType.getIpsProject()));

        /*
         * Create id attribute and name attribute if checked and possible (no supertype must be
         * specified).
         */
        if (StringUtils.isEmpty(supertypeField.getText())) {
            if (createIdAttributeField.getCheckbox().isChecked()) {
                IEnumAttribute idAttribute = newEnumType.newEnumAttribute();
                idAttribute.setName(idAttributeNameField.getText());
                idAttribute.setUnique(true);
                idAttribute.setDatatype(Datatype.STRING.getName());
                idAttribute.setIdentifier(true);
            }
            if (createNameAttributeField.getCheckbox().isChecked()) {
                IEnumAttribute nameAttribute = newEnumType.newEnumAttribute();
                nameAttribute.setName(nameAttributeNameField.getText());
                nameAttribute.setDatatype(Datatype.STRING.getName());
                nameAttribute.setUsedAsNameInFaktorIpsUi(true);
                nameAttribute.setUnique(true);
            }
        }

        // Create literal name attribute if not abstract and not extensible.
        if (!isAbstractField.getValue()) {
            IEnumLiteralNameAttribute literalNameAttribute = newEnumType.newEnumLiteralNameAttribute();
            IEnumAttribute nameAttribute = newEnumType.findUsedAsNameInFaktorIpsUiAttribute(getIpsProject());
            literalNameAttribute.setDefaultValueProviderAttribute(nameAttribute.getName());
        }

        modifiedIpsObjects.add(newEnumType);
        newEnumType.getIpsSrcFile().markAsDirty();
    }

    @Override
    protected void validatePageExtension() {
        super.validatePageExtension();

        MessageList validationMessages = new MessageList();
        IIpsProject ipsProject = getIpsProject();

        // Validate super enumeration type.
        String superTypeFieldText = supertypeField.getText();
        if (StringUtils.isNotEmpty(superTypeFieldText) && ipsProject != null) {
            EnumTypeValidations.validateSuperEnumType(validationMessages, null, superTypeFieldText, ipsProject);
        }

        // Validate qualified enumeration content name.
        EnumTypeValidations.validateEnumContentName(validationMessages, null, isAbstractField.getValue(),
                extensibleField.getCheckbox().isChecked(), enumContentQualifiedNameField.getText());

        // Display message with highest severity if any.
        if (validationMessages.size() > 0) {
            Message msg = validationMessages.getMessageWithHighestSeverity();
            Severity severity = msg.getSeverity();
            int msgLevel = IMessageProvider.NONE;
            switch (severity) {
                case INFO:
                    msgLevel = INFORMATION;
                    break;
                case ERROR:
                    msgLevel = ERROR;
                    break;
                case WARNING:
                    msgLevel = WARNING;
                    break;
                default:
                    break;
            }
            setMessage(msg.getText(), msgLevel);
        }
    }

}
