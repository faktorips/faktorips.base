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

package org.faktorips.devtools.core.ui.wizards.enumcontent;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.model.enums.EnumContentValidations;
import org.faktorips.devtools.core.model.enums.IEnumContent;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.fields.TextButtonField;
import org.faktorips.devtools.core.ui.controls.IpsObjectRefControl;
import org.faktorips.devtools.core.ui.wizards.IpsObjectPage;
import org.faktorips.util.message.MessageList;

/**
 * The wizard page for the <code>NewEnumContentWizard</code>.
 * 
 * @see NewEnumContentWizard
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
public class EnumContentPage extends IpsObjectPage {

    /** The image for the wizard page. */
    private final String PAGE_IMAGE = "wizards/NewEnumContentWizard.png"; //$NON-NLS-1$

    /** The text field to choose the enum type on which the new enum content shall be based upon. */
    private TextButtonField enumTypeField;

    /**
     * Creates the enum content page.
     * 
     * @param selection If a selection is provided default values for some fields can be derived
     *            from that.
     */
    public EnumContentPage(IStructuredSelection selection) {
        super(IpsObjectType.ENUM_CONTENT, selection, Messages.Page_Title);
        setImageDescriptor(IpsUIPlugin.getDefault().getImageDescriptor(PAGE_IMAGE));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void fillNameComposite(Composite nameComposite, UIToolkit toolkit) {
        super.fillNameComposite(nameComposite, toolkit);

        // Enum type
        toolkit.createFormLabel(nameComposite, Messages.Fields_EnumType + ':');
        IpsObjectRefControl enumTypeControl = toolkit.createEnumTypeRefControl(null, nameComposite, false);
        enumTypeField = new TextButtonField(enumTypeControl);
        enumTypeField.addChangeListener(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void sourceFolderChanged() {
        super.sourceFolderChanged();

        IIpsPackageFragmentRoot root = getIpsPackageFragmentRoot();
        if (root != null) {
            ((IpsObjectRefControl)enumTypeField.getControl()).setIpsProject(root.getIpsProject());
        } else {
            ((IpsObjectRefControl)enumTypeField.getControl()).setIpsProject(null);
        }
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    protected void finishIpsObjects(IIpsObject newIpsObject, List modifiedIpsObjects) throws CoreException {
        super.finishIpsObjects(newIpsObject, modifiedIpsObjects);

        IEnumContent newEnumContent = (IEnumContent)newIpsObject;
        newEnumContent.setEnumType(enumTypeField.getText());

        modifiedIpsObjects.add(newEnumContent);
        newEnumContent.getIpsSrcFile().markAsDirty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validatePageExtension() throws CoreException {
        super.validatePageExtension();

        String enumTypeFieldText = enumTypeField.getText();
        IIpsProject ipsProject = ((IpsObjectRefControl)enumTypeField.getControl()).getIpsProject();
        if (ipsProject != null) {
            MessageList validationMessageList = new MessageList();
            EnumContentValidations.validateEnumType(validationMessageList, null, enumTypeFieldText, ipsProject);

            IEnumType enumType = ipsProject.findEnumType(enumTypeFieldText);
            if (enumType != null) {
                EnumContentValidations.validateEnumContentPackageName(validationMessageList, null, enumType, getQualifiedIpsObjectName());
            }

            if (!(validationMessageList.isEmpty())) {
                setErrorMessage(validationMessageList.getMessage(0));
            }
        }
    }

    /**
     * Returns the selected enum type which defines the structure for the
     * enum content to be created.
     * 
     * @return An <code>IEnumType</code> instance, or null if it could not be determined.
     */
    public IEnumType getEnumType() {
        try {
            IIpsPackageFragmentRoot root = getIpsPackageFragmentRoot();
            return (IEnumType) root.getIpsProject().findIpsObject(IpsObjectType.ENUM_TYPE, enumTypeField.getText());
        } catch (CoreException e) {
            // page controls are currently invalid, return null
            return null;
        }
    }
}
