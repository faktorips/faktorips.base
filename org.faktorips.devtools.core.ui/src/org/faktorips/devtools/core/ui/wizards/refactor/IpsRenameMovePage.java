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
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.refactor.IpsRenameMoveProcessor;
import org.faktorips.devtools.core.refactor.LocationDescriptor;

/**
 * Abstract base class bundling common functionality between "Rename" and "Move" pages.
 * 
 * @author Alexander Weickmann
 */
public abstract class IpsRenameMovePage extends IpsRefactoringUserInputPage {

    /**
     * Text field that enables the user to provide a new name for the <tt>IIpsElement</tt> to be
     * refactored.
     */
    private Text newNameTextField;

    /**
     * Creates an <tt>IpsRenameMovePage</tt>.
     * 
     * @param ipsElement The <tt>IIpsElement</tt> to refactor.
     * @param pageName The concrete name for this page.
     * 
     * @throws NullPointerException If any parameter is <tt>null</tt>.
     */
    IpsRenameMovePage(IIpsElement ipsElement, String pageName) {
        super(ipsElement, pageName);
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation creates sets a basic control composite and creates the text field to type
     * the <tt>IIpsElement</tt>'s new name.
     */
    public final void createControl(Composite parent) {
        Composite controlComposite = getUiToolkit().createGridComposite(parent, 1, false, false);
        setControl(controlComposite);

        createControlBefore(controlComposite);

        Composite newNameComposite = getUiToolkit().createLabelEditColumnComposite(controlComposite);
        getUiToolkit().createLabel(newNameComposite, Messages.IpsRenameMovePage_labelNewName);
        newNameTextField = getUiToolkit().createText(newNameComposite);
        newNameTextField.setText(getIpsElement().getName());
        newNameTextField.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                userInputChanged();
            }
        });

        createControlAfter(controlComposite);

        setFocus();
        setPageComplete(false);
    }

    /**
     * Subclass implementation called by <tt>createControl(Composite)</tt>. The default
     * implementation does nothing.
     */
    protected void createControlBefore(Composite pageControlComposite) {

    }

    /**
     * Subclass implementation called by <tt>createControl(Composite)</tt>. The default
     * implementation does nothing.
     */
    protected void createControlAfter(Composite pageControlComposite) {

    }

    /**
     * This operation is responsible for setting the initial focus and is called by
     * <tt>createControl(Composite)</tt>.
     * <p>
     * This method may be overwritten by subclasses, the default implementation sets the focus to
     * the text field enabling the user to type a new name for the <tt>IIpsElement</tt> to be
     * refactored.
     */
    protected void setFocus() {
        newNameTextField.selectAll();
        newNameTextField.setFocus();
    }

    /** Operation to be called when any user input has changed. */
    protected final void userInputChanged() {
        try {
            boolean userInputValid = validatePage();
            setPageComplete(userInputValid);
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

    /** Validates the user input. */
    private boolean validatePage() throws CoreException {
        resetPageMessages();

        getIpsRenameMoveProcessor().setTargetLocation(getTargetLocationFromUserInput());
        RefactoringStatus status = getIpsRenameMoveProcessor().validateTargetLocation(new NullProgressMonitor());
        evaluateValidation(status);

        return !(status.hasError());
    }

    /**
     * Subclass implementation responsible for returning a <tt>LocationDescriptor</tt> representing
     * the target location of the <tt>IIpsElement</tt> to refactor.
     * <p>
     * This <tt>LocationDescriptor</tt> must be build from the user input.
     */
    protected abstract LocationDescriptor getTargetLocationFromUserInput();

    /** Returns the new name for the <tt>IIpsElement</tt> to be refactored, provided by the user. */
    protected final String getUserInputNewName() {
        return newNameTextField.getText();
    }

    /**
     * Returns a <tt>LocationDescriptor</tt> representing the original location of the
     * <tt>IIpsElement</tt> to be refactored.
     */
    protected final LocationDescriptor getOriginalLocation() {
        return getIpsRenameMoveProcessor().getOriginalLocation();
    }

    /** Returns the <tt>IpsRenameMoveProcessor</tt> this refactoring is working with. */
    private IpsRenameMoveProcessor getIpsRenameMoveProcessor() {
        return (IpsRenameMoveProcessor)((ProcessorBasedRefactoring)getRefactoring()).getProcessor();
    }

}
