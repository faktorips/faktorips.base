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

package org.faktorips.devtools.core.ui.wizards.refactor;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.ProcessorBasedRefactoring;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.refactor.IIpsRenameProcessor;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.controls.Checkbox;

/**
 * A wizard to guide the user trough a Faktor-IPS rename refactoring.
 * 
 * @author Alexander Weickmann
 */
public final class IpsRenameRefactoringWizard extends IpsRefactoringWizard {

    /**
     * @param refactoring The refactoring used by the wizard
     * @param ipsElement The {@link IIpsElement} to be renamed
     * 
     * @throws NullPointerException If any parameter is null
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
     * Provides a text field that allows the user to type a new name (and optionally plural name)
     * for the {@link IIpsElement} to rename.
     */
    private final static class RenameUserInputPage extends IpsRefactoringUserInputPage {

        /**
         * Text field that enables the user to provide a new name for the {@link IIpsElement} to be
         * refactored.
         */
        private Text newNameTextField;

        /**
         * Text field that enables the user to provide a new plural name for the {@link IIpsElement}
         * to be refactored.
         */
        private Text newPluralNameTextField;

        /**
         * Check box that enables the user to decide whether the runtime ID of an
         * {@link IProductCmpt} should be adapted.
         */
        private Checkbox adaptRuntimeIdField;

        /**
         * @param ipsElement The {@link IIpsElement} to be renamed
         */
        RenameUserInputPage(IIpsElement ipsElement) {
            super(ipsElement, "RenameUserInputPage"); //$NON-NLS-1$
        }

        @Override
        protected void setPromptMessage() {
            setMessage(NLS.bind(Messages.RenameUserInputPage_message, getIpsElementName(), getIpsElement().getName()));
        }

        @Override
        public void createControl(Composite parent) {
            Composite controlComposite = getUiToolkit().createGridComposite(parent, 1, false, false);
            setControl(controlComposite);

            Composite fieldsComposite = getUiToolkit().createLabelEditColumnComposite(controlComposite);

            getUiToolkit().createLabel(fieldsComposite, Messages.RenameUserInputPage_labelNewName);
            newNameTextField = getUiToolkit().createText(fieldsComposite);
            newNameTextField.setText(getIpsRenameProcessor().getOriginalName());
            newNameTextField.addModifyListener(new ModifyListener() {
                @Override
                public void modifyText(ModifyEvent event) {
                    userInputChanged();
                }
            });

            if (getIpsRenameProcessor().isPluralNameRefactoringRequired()) {
                getUiToolkit().createLabel(fieldsComposite, Messages.RenameUserInputPage_labelNewPluralName);
                newPluralNameTextField = getUiToolkit().createText(fieldsComposite);
                newPluralNameTextField.setText(getIpsRenameProcessor().getOriginalPluralName());
                newPluralNameTextField.addModifyListener(new ModifyListener() {
                    @Override
                    public void modifyText(ModifyEvent event) {
                        userInputChanged();
                    }
                });
            }

            if (getIpsElement() instanceof IProductCmpt) {
                getUiToolkit().createLabel(fieldsComposite, ""); //$NON-NLS-1$
                adaptRuntimeIdField = getUiToolkit().createCheckbox(fieldsComposite,
                        Messages.IpsRenameAndMoveUserInputPage_labelRefactorRuntimeId);
                adaptRuntimeIdField.getButton().addSelectionListener(new SelectionListener() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        userInputChanged();
                    }

                    @Override
                    public void widgetDefaultSelected(SelectionEvent e) {
                        // Nothing to do
                    }
                });
            }

            setFocus();
            setPageComplete(false);
        }

        /**
         * Responsible for setting the initial focus.
         */
        private void setFocus() {
            newNameTextField.selectAll();
            newNameTextField.setFocus();
        }

        @Override
        protected void validateUserInputThis(RefactoringStatus status) throws CoreException {
            getIpsRenameProcessor().setNewName(newNameTextField.getText());

            if (getIpsRenameProcessor().isPluralNameRefactoringRequired()) {
                getIpsRenameProcessor().setNewPluralName(newPluralNameTextField.getText());
            }

            if (adaptRuntimeIdField != null) {
                getIpsRenameProcessor().setAdaptRuntimeId(adaptRuntimeIdField.isChecked());
            }

            status.merge(getIpsRenameProcessor().validateUserInput(new NullProgressMonitor()));
        }

        private IIpsRenameProcessor getIpsRenameProcessor() {
            return (IIpsRenameProcessor)((ProcessorBasedRefactoring)getRefactoring()).getProcessor();
        }

    }

}
