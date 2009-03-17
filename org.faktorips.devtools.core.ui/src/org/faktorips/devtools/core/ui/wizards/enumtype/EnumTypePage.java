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
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.model.enums.EnumTypeValidations;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.fields.CheckboxField;
import org.faktorips.devtools.core.ui.controller.fields.TextButtonField;
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
    private CheckboxField valuesArePartOfModelField;

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

        // Values are part of model
        toolkit.createLabel(nameComposite, ""); //$NON-NLS-1$
        valuesArePartOfModelField = new CheckboxField(toolkit.createCheckbox(nameComposite,
                Messages.Fields_ValuesArePartOfModel));
        valuesArePartOfModelField.addChangeListener(this);
        valuesArePartOfModelField.setValue(true);
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

        modifiedIpsObjects.add(newEnumType);
        newEnumType.getIpsSrcFile().markAsDirty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validatePageExtension() throws CoreException {
        super.validatePageExtension();

        String superTypeFieldText = supertypeField.getText();
        IIpsProject ipsProject = ((IpsObjectRefControl)supertypeField.getControl()).getIpsProject();
        if (!(superTypeFieldText.equals("")) && ipsProject != null) {
            MessageList validationMessages = new MessageList();
            EnumTypeValidations.validateSuperEnumType(validationMessages, null, superTypeFieldText, ipsProject);
            if (validationMessages.getNoOfMessages() > 0) {
                setErrorMessage(validationMessages.getMessage(0).getText());
            }
        }
    }

}
