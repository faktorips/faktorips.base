/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.enumexport;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.enums.IEnumContent;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.enums.IEnumValueContainer;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controls.EnumRefControl;
import org.faktorips.devtools.core.ui.controls.IpsObjectRefControl;
import org.faktorips.devtools.core.ui.wizards.ipsexport.IpsObjectExportPage;
import org.faktorips.util.message.Message;

/**
 * Wizard page for configuring an enum type or enum content for export.
 * 
 * @see IEnumType
 * @see IEnumContent
 * 
 * @author Roman Grutza
 */
public class EnumExportPage extends IpsObjectExportPage {

    @Override
    public void createControl(Composite parent) {
        super.createControl(parent);

        // Override page title
        setTitle(Messages.EnumExportPage_messagearea_title);
    }

    public EnumExportPage(IStructuredSelection selection) throws JavaModelException {
        super(Messages.EnumExportPage_title);
        if (selection.getFirstElement() instanceof IResource) {
            selectedResource = (IResource)selection.getFirstElement();
        } else if (selection.getFirstElement() instanceof IJavaElement) {
            selectedResource = ((IJavaElement)selection.getFirstElement()).getCorrespondingResource();
        } else if (selection.getFirstElement() instanceof IIpsElement) {
            selectedResource = ((IIpsElement)selection.getFirstElement()).getEnclosingResource();
        } else {
            selectedResource = null;
        }
    }

    @Override
    public IpsObjectRefControl createExportedIpsObjectRefControlWithLabel(UIToolkit toolkit, Composite parent) {
        toolkit.createFormLabel(parent, Messages.EnumExportPage_enum_label);
        return toolkit.createEnumRefControl(getIpsProject(), parent, true, true);
    }

    @Override
    protected void validateObjectToExport() {
        if (exportedIpsObjectControl.getText().length() == 0) {
            setErrorMessage(Messages.EnumExportPage_msgEnumEmpty);
            return;
        }
        try {
            IEnumValueContainer enumValueContainer = getEnum();
            if (enumValueContainer == null) {
                setErrorMessage(Messages.EnumExportPage_msgNonExistingEnum);
                return;
            }

            if (!enumValueContainer.exists()) {
                setErrorMessage(Messages.EnumExportPage_msgNonExistingEnum);
                return;
            }

            if (enumValueContainer.validate(enumValueContainer.getIpsProject()).getNoOfMessages(Message.ERROR) > 0) {
                setErrorMessage(Messages.EnumExportPage_msgEnumNotValid);
                return;
            }

            if (enumValueContainer instanceof IEnumType) {
                IEnumType enumType = (IEnumType)enumValueContainer;
                if (enumType.isAbstract()) {
                    setErrorMessage(Messages.EnumExportPage_msgAbstractEnumType);
                    return;
                }
                if (!(enumType.isContainingValues())) {
                    setErrorMessage(Messages.EnumExportPage_msgEnumTypeNotContainingValues);
                    return;
                }
            }

            IEnumType enumType = enumValueContainer.findEnumType(enumValueContainer.getIpsProject());
            if (enumValueContainer instanceof IEnumContent) {
                if (enumType.validate(enumType.getIpsProject()).getNoOfMessages(Message.ERROR) > 0) {
                    setErrorMessage(Messages.EnumExportPage_msgEnumTypeNotValid);
                    return;
                }
            }

            boolean includeLiteralName = enumValueContainer instanceof IEnumType;
            if (enumType.getEnumAttributesCountIncludeSupertypeCopies(includeLiteralName) > MAX_EXCEL_COLUMNS) {
                Object[] objects = new Object[3];
                objects[0] = new Integer(enumType.getEnumAttributesCountIncludeSupertypeCopies(includeLiteralName));
                objects[1] = enumType;
                objects[2] = new Short(MAX_EXCEL_COLUMNS);
                String text = NLS.bind(Messages.EnumExportPage_msgEnumHasTooManyColumns, objects);
                setErrorMessage(text);
            }

        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void setDefaults(IResource selectedResource) {
        if (selectedResource == null) {
            setEnum(null);
            return;
        }
        try {
            IIpsElement element = IpsPlugin.getDefault().getIpsModel().getIpsElement(selectedResource);
            if (element == null) {
                setEnum(null);
            }
            setIpsProject(element.getIpsProject());
            if (element instanceof IIpsSrcFile) {
                IIpsSrcFile src = (IIpsSrcFile)element;
                IpsObjectType ipsObjectType = src.getIpsObjectType();
                if (ipsObjectType.equals(IpsObjectType.ENUM_TYPE)) {
                    IEnumType enumType = (IEnumType)src.getIpsObject();
                    if (!(enumType.isAbstract()) && enumType.isContainingValues()) {
                        setEnum(enumType);
                    }
                } else if (ipsObjectType.equals(IpsObjectType.ENUM_CONTENT)) {
                    IEnumContent enumContent = (IEnumContent)src.getIpsObject();
                    setEnum(enumContent);
                }
            }
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

    private void setEnum(IEnumValueContainer enumContainer) {
        if (enumContainer == null) {
            exportedIpsObjectControl.setText(""); //$NON-NLS-1$
            setIpsProject(null);
            return;
        }
        exportedIpsObjectControl.setText(enumContainer.getQualifiedName());
        setIpsProject(enumContainer.getIpsProject());
    }

    public IEnumValueContainer getEnum() throws CoreException {
        // Return the Enum which currently holds the values if an IEnumType and an IEnumContent with
        // the same full qualified name exist
        final IEnumValueContainer enum1 = ((EnumRefControl)exportedIpsObjectControl).findEnum(false);
        final IEnumValueContainer enum2 = ((EnumRefControl)exportedIpsObjectControl).findEnum(true);

        if (enum1 == enum2) {
            return enum1;
        }

        if (enum1.isCapableOfContainingValues()) {
            return enum1;
        } else {
            return enum2;
        }
    }

}
