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
import org.faktorips.devtools.core.ui.controller.fields.TextField;
import org.faktorips.devtools.core.ui.controller.fields.ValueChangeListener;
import org.faktorips.devtools.core.ui.controls.IpsObjectRefControl;
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
    private CheckboxField containingValuesField;

    /**
     * The text field to specify the package for the enum content (only enabled if the values are
     * not part of the model and the enum type is not abstract).
     */
    private TextField packageSpecificationField;

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
        containingValuesField = new CheckboxField(toolkit.createCheckbox(nameComposite,
                Messages.Fields_ContainingValues));
        containingValuesField.setValue(true);
        containingValuesField.addChangeListener(this);
        containingValuesField.addChangeListener(new ValueChangeListener() {
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
     * <code>packageSpecificationField</code> depending on the values of the
     * <code>isAbstractField</code> and <code>containingValuesField</code>.
     */
    private void enableEnumContentControls() {
        boolean isAbstract = (Boolean)isAbstractField.getValue();
        boolean valuesArePartOfModel = (Boolean)containingValuesField.getValue();
        if (isAbstract) {
            packageSpecificationField.getTextControl().setEnabled(false);
        } else {
            packageSpecificationField.getTextControl().setEnabled(!valuesArePartOfModel);
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

        // Package specification for product side enum content
        toolkit.createFormLabel(additionalComposite, Messages.Fields_PackageSpecification + ':');
        Text text = toolkit.createText(additionalComposite);
        packageSpecificationField = new TextField(text);
        packageSpecificationField.getTextControl().setEnabled(false);
        packageSpecificationField.addChangeListener(this);
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

        // Set properties
        newEnumType.setAbstract((Boolean)isAbstractField.getValue());
        newEnumType.setContainingValues((Boolean)containingValuesField.getValue());
        newEnumType.setSuperEnumType(supertypeField.getText());
        newEnumType.setEnumContentPackageFragment(packageSpecificationField.getText());

        // Inherit enum attributes from supertype hierarchy
        newEnumType.inheritEnumAttributes(newEnumType.findInheritEnumAttributeCandidates());

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

        // Display the first error message if any
        if (validationMessages.getNoOfMessages() > 0) {
            setErrorMessage(validationMessages.getMessage(0).getText());
        }
    }

}
