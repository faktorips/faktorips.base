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

import org.eclipse.ltk.core.refactoring.participants.ProcessorBasedRefactoring;
import org.eclipse.ltk.core.refactoring.participants.RenameProcessor;
import org.eclipse.ltk.core.refactoring.participants.RenameRefactoring;
import org.eclipse.ltk.ui.refactoring.RefactoringWizard;
import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.internal.refactor.RenamePolicyCmptTypeAttributeRefactoring;
import org.faktorips.devtools.core.internal.refactor.RenameRefactoringProcessor;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.core.model.type.IMethod;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.util.ArgumentCheck;

/**
 * A wizard that is used to guide the user trough a Faktor-IPS rename refactoring.
 * 
 * @author Alexander Weickmann
 */
public class RenameRefactoringWizard extends RefactoringWizard {

    /** The <tt>IIpsElement</tt> to be refactored. */
    private final IIpsElement ipsElement;

    /**
     * Returns the <tt>RenameProcessor</tt> that is compatible to the provided <tt>IIpsElement</tt>.
     * <p>
     * This method must be static so that it is possible to call it in the constructor.
     * 
     * @param ipsElement The <tt>IIpsElement</tt> to be renamed.
     */
    private static RenameProcessor getRenameProcessor(IIpsElement ipsElement) {
        ArgumentCheck.notNull(ipsElement);
        if (ipsElement instanceof IPolicyCmptTypeAttribute) {
            return new RenamePolicyCmptTypeAttributeRefactoring((IPolicyCmptTypeAttribute)ipsElement);
        }
        throw new RuntimeException("The IPS element " + ipsElement + " is not supported by this RefactoringWizard.");
    }

    /**
     * Creates a <tt>RenameRefactoringWizard</tt>.
     * 
     * @param ipsElement The <tt>IIpsElement</tt> to be renamed.
     * 
     * @throws NullPointerException If <tt>ipsElement</tt> is <tt>null</tt>.
     */
    public RenameRefactoringWizard(IIpsElement ipsElement) {
        super(new RenameRefactoring(getRenameProcessor(ipsElement)), WIZARD_BASED_USER_INTERFACE | NO_PREVIEW_PAGE);
        setChangeCreationCancelable(false);
        this.ipsElement = ipsElement;
    }

    @Override
    protected void addUserInputPages() {
        addPage(new RenamePage());
    }

    @Override
    public boolean needsPreviousAndNextButtons() {
        return false;
    }

    /** The one-and-only input page the rename refactoring needs. */
    private class RenamePage extends UserInputWizardPage {

        /**
         * Text field to enable the user provide a new name for the <tt>IIpsElement</tt> to be
         * refactored.
         */
        private Text newNameTextField;

        /** Creates the <tt>RenamePage</tt>. */
        private RenamePage() {
            super("RenamePage");
            String ipsElementName = "";
            String qualifiedName = "";
            if (ipsElement instanceof IAttribute) {
                ipsElementName = Messages.RenameRefactoringWizard_Attribute;
                qualifiedName = ((IAttribute)ipsElement).getType().getQualifiedName() + '.' + ipsElement.getName();
            } else if (ipsElement instanceof IMethod) {
                ipsElementName = Messages.RenameRefactoringWizard_Method;
                qualifiedName = ((IMethod)ipsElement).getType().getQualifiedName() + '.' + ipsElement.getName();
            } else if (ipsElement instanceof IType) {
                ipsElementName = Messages.RenameRefactoringWizard_Type;
                qualifiedName = ((IType)ipsElement).getQualifiedName();
            }
            setDefaultPageTitle(NLS.bind(Messages.RenameRefactoringWizard_title, ipsElementName));
            setMessage(NLS.bind(Messages.RenameRefactoringWizard_message, ipsElementName, qualifiedName));
        }

        public void createControl(Composite parent) {
            UIToolkit uiToolkit = new UIToolkit(null);
            Composite control = uiToolkit.createLabelEditColumnComposite(parent);
            uiToolkit.createLabel(control, Messages.RenameRefactoringWizard_labelNewName);
            newNameTextField = uiToolkit.createText(control);
            newNameTextField.setText(ipsElement.getName());
            newNameTextField.selectAll();
            newNameTextField.setFocus();
            newNameTextField.addModifyListener(new ModifyListener() {

                public void modifyText(ModifyEvent e) {
                    String text = newNameTextField.getText();
                    boolean pageComplete = true;
                    if (text.length() < 1) {
                        pageComplete = false;
                        setErrorMessage(Messages.RenameRefactoringWizard_msgNewNameEmpty);
                    }
                    if (text.equals(ipsElement.getName())) {
                        pageComplete = false;
                        setErrorMessage(Messages.RenameRefactoringWizard_msgNewNameEqualsElementName);
                    }
                    if (pageComplete) {
                        setErrorMessage(null);
                    }
                    setPageComplete(pageComplete);
                }

            });
            setPageComplete(false);
            setControl(control);
        }

        @Override
        protected boolean performFinish() {
            ProcessorBasedRefactoring refactoring = (ProcessorBasedRefactoring)getRefactoring();
            RenameRefactoringProcessor processor = (RenameRefactoringProcessor)refactoring.getProcessor();
            processor.setNewElementName(newNameTextField.getText());
            return super.performFinish();
        }

    }

}
