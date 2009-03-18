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
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.enums.EnumTypeValidations;
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
import org.faktorips.devtools.core.ui.controller.fields.ValueChangeListener;
import org.faktorips.devtools.core.ui.controls.IpsObjectRefControl;
import org.faktorips.devtools.core.ui.controls.IpsPckFragmentRefControl;
import org.faktorips.devtools.core.ui.controls.IpsPckFragmentRootRefControl;
import org.faktorips.devtools.core.ui.wizards.IpsObjectPage;
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
    private CheckboxField valuesArePartOfModelField;

    /**
     * The text field to specify the root package for the enum content (only enabled if the values
     * are not part of the model and the enum type is not abstract).
     */
    private IpsPckFragmentRootRefControl rootPackageSpecificationControl;

    /**
     * The text field to specify the package for the enum content (only enabled if the values are
     * not part of the model and the enum type is not abstract).
     */
    private IpsPckFragmentRefControl packageSpecificationControl;

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

        // Super type
        toolkit.createFormLabel(nameComposite, Messages.Fields_SuperEnumType + ':');
        IpsObjectRefControl superTypeControl = toolkit.createEnumTypeRefControl(null, nameComposite, true);
        supertypeField = new TextButtonField(superTypeControl);
        supertypeField.addChangeListener(this);

        // Abstract
        toolkit.createLabel(nameComposite, ""); //$NON-NLS-1$
        isAbstractField = new CheckboxField(toolkit.createCheckbox(nameComposite, Messages.Fields_Abstract));
        isAbstractField.addChangeListener(this);
        isAbstractField.addChangeListener(new ValueChangeListener() {
            /**
             * {@inheritDoc}
             */
            public void valueChanged(FieldValueChangedEvent e) {
                enableEnumContentControls();
            }
        });

        // Values are part of model
        toolkit.createLabel(nameComposite, ""); //$NON-NLS-1$
        valuesArePartOfModelField = new CheckboxField(toolkit.createCheckbox(nameComposite,
                Messages.Fields_ValuesArePartOfModel));
        valuesArePartOfModelField.setValue(true);
        valuesArePartOfModelField.addChangeListener(this);
        valuesArePartOfModelField.addChangeListener(new ValueChangeListener() {
            /**
             * {@inheritDoc}
             */
            public void valueChanged(FieldValueChangedEvent e) {
                enableEnumContentControls();
            }
        });
    }

    /**
     * Enables or disables the <code>rootPackageSpecificationControl</code> and the
     * <code>packageSpecificationControl</code> depending on the values of the
     * <code>isAbstractField</code> and <code>valuesArePartOfModelField</code>.
     */
    private void enableEnumContentControls() {
        boolean isAbstract = (Boolean)isAbstractField.getValue();
        boolean valuesArePartOfModel = (Boolean)valuesArePartOfModelField.getValue();
        if (isAbstract) {
            rootPackageSpecificationControl.setEnabled(false);
            packageSpecificationControl.setEnabled(false);
        } else {
            rootPackageSpecificationControl.setEnabled(!valuesArePartOfModel);
            packageSpecificationControl.setEnabled(!valuesArePartOfModel);
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
        // Set the layout
        additionalComposite.setLayout(new GridLayout(1, true));
        additionalComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        // Root package specification for product side enum content
        toolkit.createFormLabel(additionalComposite, Messages.Fields_RootPackageSpecification + ':');
        rootPackageSpecificationControl = toolkit.createPdPackageFragmentRootRefControl(additionalComposite, true);
        rootPackageSpecificationControl.setEnabled(false);
        rootPackageSpecificationControl.getTextControl().addModifyListener(new ModifyListener() {
            /**
             * {@inheritDoc}
             */
            public void modifyText(ModifyEvent event) {
                IIpsPackageFragmentRoot root = rootPackageSpecificationControl.getIpsPckFragmentRoot();
                packageSpecificationControl.setIpsPckFragmentRoot(root);
            }
        });
        TextButtonField enumContentRootPackageField = new TextButtonField(rootPackageSpecificationControl);
        enumContentRootPackageField.addChangeListener(this);

        // Package specification for product side enum content
        toolkit.createFormLabel(additionalComposite, Messages.Fields_PackageSpecification + ':');
        packageSpecificationControl = toolkit.createPdPackageFragmentRefControl(rootPackageSpecificationControl
                .getIpsPckFragmentRoot(), additionalComposite);
        packageSpecificationControl.setEnabled(false);
        TextButtonField enumContentPackageField = new TextButtonField(packageSpecificationControl);
        enumContentPackageField.addChangeListener(this);
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
    @SuppressWarnings("unchecked")
    @Override
    protected void finishIpsObjects(IIpsObject newIpsObject, List modifiedIpsObjects) throws CoreException {
        super.finishIpsObjects(newIpsObject, modifiedIpsObjects);

        IEnumType newEnumType = (IEnumType)newIpsObject;
        newEnumType.setAbstract((Boolean)isAbstractField.getValue());
        newEnumType.setValuesArePartOfModel((Boolean)valuesArePartOfModelField.getValue());
        newEnumType.setSuperEnumType(supertypeField.getText());
        newEnumType.setEnumContentPackageFragmentRoot(rootPackageSpecificationControl.getText());
        newEnumType.setEnumContentPackageFragment(packageSpecificationControl.getText());

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

        // Validate super enum type
        String superTypeFieldText = supertypeField.getText();
        ipsProject = ((IpsObjectRefControl)supertypeField.getControl()).getIpsProject();
        if (!(superTypeFieldText.equals("")) && ipsProject != null) {
            EnumTypeValidations.validateSuperEnumType(validationMessages, null, superTypeFieldText, ipsProject);
        }

        // Validate enum content package fragment root
        ipsProject = getIpsProject();
        int before = validationMessages.getNoOfMessages();
        String rootPackage = rootPackageSpecificationControl.getText();
        if (!(rootPackage.equals(""))) {
            // TODO aw: splitting project and root must be done in validation code
            if (rootPackage.contains("/")) {
                String originalRootPackageString = rootPackage;
                rootPackage = rootPackage.substring(rootPackage.indexOf('/') + 1);
                String projectName = originalRootPackageString.substring(0, originalRootPackageString.indexOf('/'));
                if (!(projectName.equals(""))) {
                    IIpsProject foundProject = IpsPlugin.getDefault().getIpsModel().getIpsProject(projectName);
                    if (foundProject.exists()) {
                        ipsProject = foundProject;
                    }
                }
            }
        }
        EnumTypeValidations.validateEnumContentPackageFragmentRoot(validationMessages, null, (Boolean)isAbstractField
                .getValue(), (Boolean)valuesArePartOfModelField.getValue(), rootPackage, ipsProject);

        // Only validate enum content package fragment if the root was valid
        if (before == validationMessages.getNoOfMessages()) {
            String packageFragment = packageSpecificationControl.getText();
            EnumTypeValidations.validateEnumContentPackageFragment(validationMessages, null, (Boolean)isAbstractField
                    .getValue(), (Boolean)valuesArePartOfModelField.getValue(), rootPackage, packageFragment,
                    ipsProject);
        }

        // Display the first error message if any
        if (validationMessages.getNoOfMessages() > 0) {
            setErrorMessage(validationMessages.getMessage(0).getText());
        }
    }

}
