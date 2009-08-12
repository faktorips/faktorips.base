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

package org.faktorips.devtools.core.ui.wizards.enumtype;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.model.enums.EnumTypeValidations;
import org.faktorips.devtools.core.model.enums.IEnumAttribute;
import org.faktorips.devtools.core.model.enums.IEnumLiteralNameAttribute;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.fields.CheckboxField;
import org.faktorips.devtools.core.ui.controller.fields.FieldValueChangedEvent;
import org.faktorips.devtools.core.ui.controller.fields.TextButtonField;
import org.faktorips.devtools.core.ui.controller.fields.TextField;
import org.faktorips.devtools.core.ui.controller.fields.ValueChangeListener;
import org.faktorips.devtools.core.ui.controls.IpsObjectRefControl;
import org.faktorips.devtools.core.ui.wizards.IpsObjectPage;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;

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
    private final String PAGE_IMAGE = "wizards/NewEnumTypeWizard.png"; //$NON-NLS-1$

    /** The text field to choose the supertype for the new enum type. */
    private TextButtonField supertypeField;

    /** The checkbox field to mark the new enum type as being abstract. */
    private CheckboxField isAbstractField;

    /** The checkbox field to mark the new enum type that its values are defined in the model. */
    private CheckboxField valuesDeferredToContentField;

    /** The checkbox field to check if an id attribute should be created by the wizard. */
    private CheckboxField createIdAttributeField;

    /** The checkbox field to check if a name attribute should be created by the wizard. */
    private CheckboxField createNameAttributeField;

    /** This text field is used to specify the name of the id attribute to be created. */
    private Text idAttributeNameField;

    /** This text field is used to specify the name of the name attribute to be created. */
    private Text nameAttributeNameField;

    /**
     * The text field to specify the package for the enum content (only enabled if the values are
     * not part of the model and the enum type is not abstract).
     */
    private TextField enumContentPackageSpecificationField;

    /**
     * Creates the enum type page.
     * 
     * @param selection
     */
    public EnumTypePage(IStructuredSelection selection) {
        super(IpsObjectType.ENUM_TYPE, selection, Messages.Page_Title);
        setImageDescriptor(IpsUIPlugin.getDefault().getImageDescriptor(PAGE_IMAGE));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void fillNameComposite(Composite nameComposite, UIToolkit toolkit) {
        super.fillNameComposite(nameComposite, toolkit);

        // Supertype.
        toolkit.createFormLabel(nameComposite, Messages.Fields_SuperEnumType + ':');
        IpsObjectRefControl superTypeControl = toolkit.createEnumTypeRefControl(null, nameComposite, true);
        supertypeField = new TextButtonField(superTypeControl);
        supertypeField.addChangeListener(this);

        // Abstract.
        toolkit.createLabel(nameComposite, ""); //$NON-NLS-1$
        isAbstractField = new CheckboxField(toolkit.createCheckbox(nameComposite, Messages.Fields_Abstract));
        isAbstractField.addChangeListener(this);
        isAbstractField.addChangeListener(new ValueChangeListener() {
            public void valueChanged(FieldValueChangedEvent e) {
                enableEnumContentControls();
            }
        });

        createIdAndNameGenerationFields(nameComposite, toolkit);
    }

    /**
     * Creates the fields that enable the user to specify whether he wants to create an id attribute
     * and a name attribute.
     */
    private void createIdAndNameGenerationFields(Composite nameComposite, UIToolkit uiToolkit) {
        uiToolkit.createLabel(nameComposite, ""); //$NON-NLS-1$
        Composite createFieldsContainer = uiToolkit.createGridComposite(nameComposite, 2, false, false);

        // Create id attribute.
        createIdAttributeField = new CheckboxField(uiToolkit.createCheckbox(createFieldsContainer,
                Messages.EnumTypePage_labelIdAttribute));
        createIdAttributeField.setValue(true);
        idAttributeNameField = uiToolkit.createText(createFieldsContainer);
        idAttributeNameField.setText("id"); //$NON-NLS-1$
        createIdAttributeField.addChangeListener(new ValueChangeListener() {
            public void valueChanged(FieldValueChangedEvent e) {
                idAttributeNameField.setEnabled(createIdAttributeField.getCheckbox().isChecked());
            }
        });

        // Create name attribute.
        createNameAttributeField = new CheckboxField(uiToolkit.createCheckbox(createFieldsContainer,
                Messages.EnumTypePage_labelNameAttribute));
        createNameAttributeField.setValue(true);
        nameAttributeNameField = uiToolkit.createText(createFieldsContainer);
        nameAttributeNameField.setText("name"); //$NON-NLS-1$
        createNameAttributeField.addChangeListener(new ValueChangeListener() {
            public void valueChanged(FieldValueChangedEvent e) {
                nameAttributeNameField.setEnabled(createNameAttributeField.getCheckbox().isChecked());
            }
        });

        // Disable id and name generation fields if a supertype is specified.
        supertypeField.addChangeListener(new ValueChangeListener() {
            public void valueChanged(FieldValueChangedEvent e) {
                boolean enabled = supertypeField.getText().equals(""); //$NON-NLS-1$
                createIdAttributeField.getCheckbox().setEnabled(enabled);
                createNameAttributeField.getCheckbox().setEnabled(enabled);
                idAttributeNameField.setEnabled(enabled);
                nameAttributeNameField.setEnabled(enabled);
            }
        });

    }

    /**
     * Enables or disables the <code>rootPackageSpecificationControl</code> and the
     * <code>enumContentPackageSpecificationField</code> depending on the values of the
     * <code>isAbstractField</code> and <code>containingValuesField</code>.
     */
    private void enableEnumContentControls() {
        boolean isAbstract = (Boolean)isAbstractField.getValue();
        boolean valuesDeferredToContent = (Boolean)valuesDeferredToContentField.getValue();
        if (isAbstract) {
            enumContentPackageSpecificationField.getTextControl().setEnabled(false);
        } else {
            enumContentPackageSpecificationField.getTextControl().setEnabled(valuesDeferredToContent);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean useAdditionalComposite() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void fillAdditionalComposite(Composite additionalComposite, UIToolkit toolkit) {
        // Set the layout.
        additionalComposite.setLayout(new GridLayout(1, true));
        additionalComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        // Values are part of model.
        valuesDeferredToContentField = new CheckboxField(toolkit.createCheckbox(additionalComposite,
                Messages.Fields_ContainingValues));
        GridData data = new GridData();
        data.verticalAlignment = GridData.BEGINNING;
        valuesDeferredToContentField.getControl().setLayoutData(data);

        valuesDeferredToContentField.setValue(false);
        valuesDeferredToContentField.addChangeListener(this);
        valuesDeferredToContentField.addChangeListener(new ValueChangeListener() {
            public void valueChanged(FieldValueChangedEvent e) {
                enableEnumContentControls();
            }
        });

        // Name specification for product side enum content.
        toolkit.createFormLabel(additionalComposite, Messages.Fields_PackageSpecification + ':');
        Text text = toolkit.createText(additionalComposite);
        enumContentPackageSpecificationField = new TextField(text);
        enumContentPackageSpecificationField.getTextControl().setEnabled(false);
        enumContentPackageSpecificationField.addChangeListener(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void packageChanged() {
        updateEnumContentName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void nameChanged() {
        updateEnumContentName();
    }

    /**
     * Updates the <tt>enumContentPackageSpecificationField</tt> so that it reflects the current
     * package and name of the enum type.
     */
    private void updateEnumContentName() {
        String pack = getPackage();
        String name = getIpsObjectName();
        if (pack.length() > 0 && name.length() > 0) {
            enumContentPackageSpecificationField.setText(pack + '.' + name);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void sourceFolderChanged() {
        super.sourceFolderChanged();
        IIpsPackageFragmentRoot root = getIpsPackageFragmentRoot();
        if (root != null) {
            ((IpsObjectRefControl)supertypeField.getControl()).setIpsProject(root.getIpsProject());
        } else {
            ((IpsObjectRefControl)supertypeField.getControl()).setIpsProject(null);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void finishIpsObjects(IIpsObject newIpsObject, List<IIpsObject> modifiedIpsObjects) throws CoreException {
        IEnumType newEnumType = (IEnumType)newIpsObject;

        // Set properties.
        newEnumType.setAbstract((Boolean)isAbstractField.getValue());
        newEnumType.setContainingValues(!(Boolean)valuesDeferredToContentField.getValue());
        newEnumType.setSuperEnumType(supertypeField.getText());
        newEnumType.setEnumContentName(enumContentPackageSpecificationField.getText());

        // Inherit enum attributes from supertype hierarchy.
        newEnumType.inheritEnumAttributes(newEnumType.findInheritEnumAttributeCandidates(newEnumType.getIpsProject()));

        /*
         * Create id attribute and name attribute if checked and possible (no supertype must be
         * specified).
         */
        String nameAttributeName = "";
        if (supertypeField.getText().equals("")) { //$NON-NLS-1$
            if (createIdAttributeField.getCheckbox().isChecked()) {
                IEnumAttribute idAttribute = newEnumType.newEnumAttribute();
                idAttribute.setName(idAttributeNameField.getText());
                idAttribute.setUnique(true);
                idAttribute.setDatatype(Datatype.STRING.getName());
                idAttribute.setIdentifier(true);
            }
            if (createNameAttributeField.getCheckbox().isChecked()) {
                nameAttributeName = nameAttributeNameField.getText();
                IEnumAttribute nameAttribute = newEnumType.newEnumAttribute();
                nameAttribute.setName(nameAttributeName);
                nameAttribute.setDatatype(Datatype.STRING.getName());
                nameAttribute.setUsedAsNameInFaktorIpsUi(true);
                nameAttribute.setUnique(true);
            }
        }

        // Create literal name attribute if not abstract and containing values.
        if (!((Boolean)valuesDeferredToContentField.getValue()) && !((Boolean)isAbstractField.getValue())) {
            IEnumLiteralNameAttribute literalNameAttribute = newEnumType.newEnumLiteralNameAttribute();
            literalNameAttribute.setDefaultValueProviderAttribute(nameAttributeName);
        }

        modifiedIpsObjects.add(newEnumType);
        newEnumType.getIpsSrcFile().markAsDirty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validatePageExtension() throws CoreException {
        super.validatePageExtension();

        MessageList validationMessages = new MessageList();
        IIpsProject ipsProject = null;

        // Validate super enum type.
        String superTypeFieldText = supertypeField.getText();
        ipsProject = ((IpsObjectRefControl)supertypeField.getControl()).getIpsProject();
        if (!(superTypeFieldText.equals("")) && ipsProject != null) { //$NON-NLS-1$
            EnumTypeValidations.validateSuperEnumType(validationMessages, null, superTypeFieldText, ipsProject);
        }

        // Validate enum content package fragment specification.
        EnumTypeValidations.validateEnumContentName(validationMessages, null, (Boolean)isAbstractField.getValue(),
                valuesDeferredToContentField.getCheckbox().isChecked(), enumContentPackageSpecificationField.getText());

        // Display the first error message if any.
        if (validationMessages.getNoOfMessages() > 0) {
            Message msg = validationMessages.getMessage(0);
            int severity = msg.getSeverity();
            int msgLevel = Message.NONE;
            switch (severity) {
                case Message.INFO:
                    msgLevel = INFORMATION;
                    break;
                case Message.ERROR:
                    msgLevel = ERROR;
                    break;
                case Message.WARNING:
                    msgLevel = WARNING;
                    break;
            }
            setMessage(msg.getText(), msgLevel);
        }
    }
}
