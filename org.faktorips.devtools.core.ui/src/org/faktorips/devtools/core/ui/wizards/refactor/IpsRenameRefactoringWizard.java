/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.wizards.refactor;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.ProcessorBasedRefactoring;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.refactor.IIpsRenameProcessor;
import org.faktorips.devtools.core.ui.IpsUIPlugin;

/**
 * A wizard to guide the user trough a Faktor-IPS rename refactoring.
 * 
 * @author Alexander Weickmann
 */
public final class IpsRenameRefactoringWizard extends IpsRefactoringWizard {

    /**
     * @param refactoring The refactoring used by the wizard.
     * @param ipsElement The <tt>IIpsElement</tt> to be renamed.
     * 
     * @throws NullPointerException If any parameter is <tt>null</tt>.
     */
    public IpsRenameRefactoringWizard(Refactoring refactoring, IIpsElement ipsElement) {
        super(refactoring, ipsElement, WIZARD_BASED_USER_INTERFACE | NO_PREVIEW_PAGE);
        setDefaultPageImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor("wizards/RenameWizard.png")); //$NON-NLS-1$
        setDefaultPageTitle(NLS.bind(Messages.RenameRefactoringWizard_title, getIpsElementName()));
    }

    @Override
    protected void addUserInputPages() {
        addPage(new RenameUserInputPage(getIpsElement()));
    }

    @Override
    public boolean needsPreviousAndNextButtons() {
        return false;
    }

    /**
     * The <tt>RenameUserInputPage</tt> provides a text field that allows the user to type a new
     * name for <tt>IIpsElement</tt> to rename.
     */
    private final static class RenameUserInputPage extends IpsRefactoringUserInputPage {

        /**
         * Text field that enables the user to provide a new name for the <tt>IIpsElement</tt> to be
         * refactored.
         */
        private Text newNameTextField;

        /**
         * Creates the <tt>RenameUserInputPage</tt>.
         * 
         * @param ipsElement The <tt>IIpsElement</tt> to be renamed.
         */
        RenameUserInputPage(IIpsElement ipsElement) {
            super(ipsElement, "RenameUserInputPage"); //$NON-NLS-1$
        }

        @Override
        protected void setPromptMessage() {
            setMessage(NLS.bind(Messages.RenamePage_message, getIpsElementName(), getIpsElement().getName()));
        }

        @Override
        public void createControl(Composite parent) {
            Composite controlComposite = getUiToolkit().createGridComposite(parent, 1, false, false);
            setControl(controlComposite);

            Composite newNameComposite = getUiToolkit().createLabelEditColumnComposite(controlComposite);
            getUiToolkit().createLabel(newNameComposite, Messages.IpsRenameMovePage_labelNewName);
            newNameTextField = getUiToolkit().createText(newNameComposite);
            newNameTextField.setText(getIpsElement().getName());
            newNameTextField.addModifyListener(new ModifyListener() {
                @Override
                public void modifyText(ModifyEvent event) {
                    userInputChanged();
                }
            });

            setFocus();
            setPageComplete(false);
        }

        /** This operation is responsible for setting the initial focus. */
        private void setFocus() {
            newNameTextField.selectAll();
            newNameTextField.setFocus();
        }

        @Override
        protected void validateUserInputThis(RefactoringStatus status) throws CoreException {
            getIpsRenameProcessor().setNewName(newNameTextField.getText());
            status.merge(getIpsRenameProcessor().validateUserInput(new NullProgressMonitor()));
        }

        /** Returns the <tt>IIpsRenameProcessor</tt> this refactoring is working with. */
        private IIpsRenameProcessor getIpsRenameProcessor() {
            return (IIpsRenameProcessor)((ProcessorBasedRefactoring)getRefactoring()).getProcessor();
        }

    }

}
