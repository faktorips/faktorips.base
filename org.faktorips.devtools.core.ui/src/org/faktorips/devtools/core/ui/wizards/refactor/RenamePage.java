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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.ProcessorBasedRefactoring;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.refactor.IpsRenameMoveProcessor;
import org.faktorips.devtools.core.refactor.LocationDescriptor;
import org.faktorips.devtools.core.util.QNameUtil;

/**
 * The one-and-only input page a Faktor-IPS rename refactoring needs.
 * 
 * @author Alexander Weickmann
 */
class RenamePage extends IpsRefactoringUserInputPage {

    /**
     * Text field to enable the user provide a new name for the <tt>IIpsElement</tt> to be
     * refactored.
     */
    private Text newNameTextField;

    /**
     * Creates the <tt>RenamePage</tt>.
     * 
     * @param ipsElement The <tt>IIpsElement</tt> to be renamed.
     */
    RenamePage(IIpsElement ipsElement) {
        super(ipsElement, "RenamePage");
    }

    @Override
    protected void setPromptMessage() {
        setMessage(NLS.bind(Messages.RenamePage_message, getIpsElementName(), getIpsElement().getName()));
    }

    public void createControl(Composite parent) {
        Composite control = getUiToolkit().createLabelEditColumnComposite(parent);
        getUiToolkit().createLabel(control, Messages.RenamePage_labelNewName);
        newNameTextField = getUiToolkit().createText(control);
        newNameTextField.setText(getIpsElement().getName());
        newNameTextField.selectAll();
        newNameTextField.setFocus();
        newNameTextField.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent event) {
                try {
                    boolean valid = validatePage();
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
    private boolean validatePage() throws CoreException {
        resetPageMessages();
        IpsRenameMoveProcessor renameMoveProcessor = (IpsRenameMoveProcessor)((ProcessorBasedRefactoring)getRefactoring())
                .getProcessor();
        String targetQualifiedName = QNameUtil.getPackageName(getQualifiedName()) + "." + newNameTextField.getText();
        renameMoveProcessor.setTargetLocation(new LocationDescriptor(getIpsPackageFragmentRoot(), targetQualifiedName));
        RefactoringStatus status = renameMoveProcessor.validateTargetLocation(new NullProgressMonitor());
        evaluateValidation(status);
        return !(status.hasError());
    }

}