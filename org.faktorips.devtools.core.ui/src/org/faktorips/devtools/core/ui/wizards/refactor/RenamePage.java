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

package org.faktorips.devtools.core.ui.wizards.refactor;

import java.util.Iterator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ltk.core.refactoring.participants.ProcessorBasedRefactoring;
import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.core.model.type.IMethod;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.refactor.RenameRefactoringProcessor;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;

/**
 * The one-and-only input page a Faktor-IPS rename refactoring needs.
 * 
 * @author Alexander Weickmann
 */
class RenamePage extends UserInputWizardPage {

    /**
     * Text field to enable the user provide a new name for the <tt>IIpsElement</tt> to be
     * refactored.
     */
    private Text newNameTextField;

    /** The <tt>IIpsElement</tt> to be renamed. */
    private IIpsElement ipsElement;

    /**
     * Creates the <tt>RenamePage</tt>.
     * 
     * @param ipsElement The <tt>IIpsElement</tt> to be renamed.
     * 
     * @throws NullPointerException If <tt>ipsElement</tt> is <tt>null</tt>.
     */
    RenamePage(IIpsElement ipsElement) {
        super("RenamePage");
        ArgumentCheck.notNull(ipsElement);
        this.ipsElement = ipsElement;

        String ipsElementName = "";
        if (ipsElement instanceof IAttribute) {
            ipsElementName = Messages.RenameRefactoringWizard_Attribute;
        } else if (ipsElement instanceof IMethod) {
            ipsElementName = Messages.RenameRefactoringWizard_Method;
        } else if (ipsElement instanceof IType) {
            ipsElementName = Messages.RenameRefactoringWizard_Type;
        }
        setMessage(NLS.bind(Messages.RenamePage_message, ipsElementName, ipsElement.getName()));
    }

    public void createControl(Composite parent) {
        UIToolkit uiToolkit = new UIToolkit(null);
        Composite control = uiToolkit.createLabelEditColumnComposite(parent);
        uiToolkit.createLabel(control, Messages.RenamePage_labelNewName);
        newNameTextField = uiToolkit.createText(control);
        newNameTextField.setText(ipsElement.getName());
        newNameTextField.selectAll();
        newNameTextField.setFocus();
        newNameTextField.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent event) {
                String text = newNameTextField.getText();
                try {
                    boolean valid = validateNewName(text);
                    setPageComplete(valid);
                } catch (CoreException e) {
                    throw new RuntimeException(e);
                }
            }

        });
        setPageComplete(false);
        setControl(control);
    }

    /** Validates the new name provided by the user. */
    private boolean validateNewName(String newName) throws CoreException {
        // Reset any messages.
        setErrorMessage(null);
        setMessage(null, WARNING);
        setMessage(null, INFORMATION);

        if (newName.length() < 1) {
            setErrorMessage(Messages.RenamePage_msgNewNameEmpty);
            return false;

        } else if (newName.equals(ipsElement.getName())) {
            setErrorMessage(Messages.RenamePage_msgNewNameEqualsElementName);
            return false;

        } else {
            MessageList validationMessageList = new MessageList();

            if (ipsElement instanceof IAttribute) {
                validateNewName((IAttribute)ipsElement, newName, validationMessageList);
            } else if (ipsElement instanceof IType) {
                validateNewName((IType)ipsElement, newName, validationMessageList);
            }

            evaluateValidation(validationMessageList);
            return getErrorMessage() == null;
        }
    }

    /** Validates the new name for an <tt>IAttribute</tt>. */
    private void validateNewName(IAttribute attribute, String newName, MessageList validationMessageList)
            throws CoreException {

        String originalName = attribute.getName();

        attribute.setName(newName);
        validationMessageList = attribute.validate(attribute.getIpsProject());
        validationMessageList.add(attribute.getType().validate(attribute.getIpsProject()));

        attribute.setName(originalName);

        // The source file was not really modified.
        attribute.getIpsSrcFile().markAsClean();
    }

    /** Validates the new name for an <tt>IType</tt>. */
    private void validateNewName(IType type, String newName, MessageList validationMessageList) throws CoreException {
        /*
         * Can't validate because for type validation the type's source file must be copied.
         * Validation will still be performed during final condition checking so it should be OK. We
         * still check if there is already a source file with the new name in this package however.
         */
        IIpsPackageFragment fragment = type.getIpsPackageFragment();
        for (IIpsSrcFile ipsSrcFile : fragment.getIpsSrcFiles()) {
            String sourceFileName = ipsSrcFile.getName();
            String relevantNamePart = sourceFileName.substring(0, sourceFileName.lastIndexOf('.'));
            if (relevantNamePart.equals(newName)) {
                setErrorMessage(NLS.bind(Messages.RenamePage_msgSourceFileAlreadyExists, newName, fragment.getName()));
                break;
            }
        }
    }

    /** Evaluates the given <tt>MessageList</tt> by setting appropriate page messages. */
    private void evaluateValidation(MessageList validationMessageList) {
        for (Iterator<Message> it = validationMessageList.iterator(); it.hasNext();) {
            Message message = it.next();
            switch (message.getSeverity()) {
                case Message.ERROR:
                    setErrorMessage(message.getText());
                    break;
                case Message.WARNING:
                    setMessage(message.getText(), WARNING);
                    break;
                case Message.INFO:
                    setMessage(message.getText(), INFORMATION);
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    protected boolean performFinish() {
        ProcessorBasedRefactoring refactoring = (ProcessorBasedRefactoring)getRefactoring();
        RenameRefactoringProcessor processor = (RenameRefactoringProcessor)refactoring.getProcessor();
        processor.setNewElementName(newNameTextField.getText());
        return super.performFinish();
    }

}