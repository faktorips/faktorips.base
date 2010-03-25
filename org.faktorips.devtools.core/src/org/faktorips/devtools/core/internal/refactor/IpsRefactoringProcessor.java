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

package org.faktorips.devtools.core.internal.refactor;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.NullChange;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.ProcessorBasedRefactoring;
import org.eclipse.ltk.core.refactoring.participants.RefactoringProcessor;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.refactor.IIpsRefactoringProcessor;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;

/**
 * This is the abstract base class for all Faktor-IPS refactorings.
 * 
 * @see ProcessorBasedRefactoring
 * 
 * @author Alexander Weickmann
 */
public abstract class IpsRefactoringProcessor extends RefactoringProcessor implements IIpsRefactoringProcessor {

    /** The <tt>IIpsElement</tt> to be refactored. */
    private final IIpsElement ipsElement;

    /** Set containing all <tt>IIpsSrcFile</tt>s that are affected by the refactoring. */
    private final Set<IIpsSrcFile> affectedIpsSrcFiles;

    /** A set containing all message codes that will be ignored during final condition checking. */
    private final Set<String> ignoredValidationMessageCodes;

    /**
     * Creates a <tt>IpsRefactoringProcessor</tt>.
     * 
     * @param ipsElement The <tt>IIpsElement</tt> to be refactored.
     * 
     * @throws NullPointerException If <tt>ipsElement</tt> is <tt>null</tt>.
     */
    protected IpsRefactoringProcessor(IIpsElement ipsElement) {
        super();
        this.ipsElement = ipsElement;
        affectedIpsSrcFiles = new HashSet<IIpsSrcFile>();
        ignoredValidationMessageCodes = new HashSet<String>();
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation checks that the <tt>IIpsElement</tt> to be renamed exists and calls the
     * subclass implementation.
     */
    @Override
    public final RefactoringStatus checkInitialConditions(IProgressMonitor pm) throws CoreException,
            OperationCanceledException {

        RefactoringStatus status = new RefactoringStatus();
        if (!(ipsElement.exists())) {
            status.addFatalError(NLS.bind(Messages.IpsRefactoringProcessor_errorIpsElementDoesNotExist, ipsElement
                    .getName()));
        }
        return status;
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation triggers the user input validation, checks if all registered source files
     * are synchronized and calls a subclass implementation that may extend the final condition
     * checking.
     */
    @Override
    public final RefactoringStatus checkFinalConditions(IProgressMonitor pm, CheckConditionsContext context)
            throws CoreException, OperationCanceledException {

        RefactoringStatus status = new RefactoringStatus();
        status.merge(validateUserInput(pm));

        addIpsSrcFiles();
        for (IIpsSrcFile ipsSrcFile : affectedIpsSrcFiles) {
            if (!(ipsSrcFile.getCorrespondingResource().isSynchronized(IResource.DEPTH_ZERO))) {
                status.addFatalError(NLS.bind(Messages.IpsRefactoringProcessor_errorIpsSrcFileOutOfSync, ipsSrcFile
                        .getCorrespondingResource().getFullPath()));
            }
        }

        if (status.isOK()) {
            checkFinalConditionsThis(status, pm, context);
        }

        return status;
    }

    /**
     * Subclass implementation which may extend the final condition checking. The default
     * implementation does nothing.
     * 
     * @param status The <tt>RefactoringStatus</tt> to add messages to.
     * @param pm An <tt>IProgressMonitor</tt> to report progress to.
     * @param context A condition checking context to collect shared condition checks.
     * 
     * @throws CoreException May be thrown at any time.
     */
    protected void checkFinalConditionsThis(RefactoringStatus status,
            IProgressMonitor pm,
            CheckConditionsContext context) throws CoreException {

    }

    /**
     * Adds an entry to the provided <tt>RefactoringStatus</tt> for every messages contained in the
     * provided <tt>MessageList</tt>.
     * <p>
     * Excluded are validation message codes that are ignored by the refactoring processor.
     * 
     * @see #getIgnoredValidationMessageCodes()
     * 
     * @param validationMessageList The <tt>MessageList</tt> from that messages shall be added to
     *            the <tt>RefactoringStatus</tt>.
     * @param status The <tt>RefactoringStatus</tt> to add entries to.
     * 
     * @throws NullPointerException If any parameter is <tt>null</tt>.
     */
    protected final void addValidationMessagesToStatus(MessageList validationMessageList, RefactoringStatus status) {
        ArgumentCheck.notNull(new Object[] { validationMessageList, status });

        for (Message message : validationMessageList) {
            // Check if message code is ignored.
            boolean ignoreThisMessage = false;
            for (String messageCode : ignoredValidationMessageCodes) {
                if (messageCode.equals(message.getCode())) {
                    ignoreThisMessage = true;
                    break;
                }
            }

            if (!(ignoreThisMessage)) {
                switch (message.getSeverity()) {
                    case Message.ERROR:
                        status.addFatalError(message.getText());
                        break;
                    case Message.WARNING:
                        status.addWarning(message.getText());
                        break;
                    case Message.INFO:
                        status.addInfo(message.getText());
                        break;
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation triggers the refactoring of the Faktor-IPS model. The registered IPS
     * source files will be saved after all modifications are complete. Always returns a
     * <tt>NullChange</tt>.
     */
    @Override
    public final Change createChange(IProgressMonitor pm) throws CoreException, OperationCanceledException {
        refactorIpsModel(pm);
        saveIpsSourceFiles(pm);
        return new NullChange();
    }

    /**
     * Subclass implementation that is responsible for performing the necessary changes in the
     * Faktor-IPS model.
     * 
     * @param pm A progress monitor to report progress to if necessary.
     * 
     * @throws CoreException Subclasses may throw this kind of exception any time.
     */
    protected abstract void refactorIpsModel(IProgressMonitor pm) throws CoreException;

    /** Saves all registered <tt>IIpsSrcFile</tt>s. */
    private void saveIpsSourceFiles(IProgressMonitor pm) throws CoreException {
        for (IIpsSrcFile ipsSrcFile : affectedIpsSrcFiles) {
            // File may not exist if it has been moved during refactoring.
            if (ipsSrcFile.exists()) {
                ipsSrcFile.save(true, pm);
            }
        }
    }

    /**
     * Subclass implementation responsible for adding all <tt>IIpsSrcFile</tt>s touched by this
     * refactoring.
     * 
     * @see #addIpsSrcFile(IIpsSrcFile)
     * 
     * @throws CoreException May be thrown at any time by this method.
     */
    protected abstract void addIpsSrcFiles() throws CoreException;

    /**
     * Adds the given <tt>IIpsSrcFile</tt> to this refactoring. Added source files will be processed
     * and saved.
     * <p>
     * If the provided <tt>IIpsSrcFile</tt> is already registered as modified source file, nothing
     * will happen.
     * 
     * @param ipsSrcFile The <tt>IIpsSrcFile</tt> to register.
     * 
     * @throws NullPointerException If <tt>ipsSrcFile</tt> is <tt>null</tt>.
     */
    protected final void addIpsSrcFile(IIpsSrcFile ipsSrcFile) {
        ArgumentCheck.notNull(ipsSrcFile);
        affectedIpsSrcFiles.add(ipsSrcFile);
    }

    /** Returns the <tt>IIpsProject</tt> the <tt>IIpsElement</tt> to be refactored belongs to. */
    protected final IIpsProject getIpsProject() {
        return ipsElement.getIpsProject();
    }

    /**
     * Returns the set containing all validation message codes that will be ignored during final
     * condition checking.
     */
    public final Set<String> getIgnoredValidationMessageCodes() {
        return ignoredValidationMessageCodes;
    }

    @Override
    public final Object[] getElements() {
        return new Object[] { ipsElement };
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation always returns <tt>true</tt>, may be overwritten by subclasses if
     * necessary.
     */
    @Override
    public boolean isApplicable() throws CoreException {
        return true;
    }

    /** Returns the <tt>IIpsElement</tt> to be refactored. */
    protected final IIpsElement getIpsElement() {
        return ipsElement;
    }

    /**
     * Searches for all <tt>IIpsSrcFile</tt>s in the object path of all <tt>IIpsProject</tt>s
     * referencing the <tt>IIpsProject</tt> that contains the <tt>IIpsElement</tt> to be renamed.
     * The referenced <tt>IIpsProject</tt> is also searched.
     * 
     * @param ipsObjectType Only <tt>IIpsSrcFile</tt>s with this <tt>IpsObjectType</tt> are
     *            searched.
     * 
     * @throws CoreException If an error occurs while searching for the source files.
     * @throws NullPointerException If <tt>ipsObjectType</tt> is <tt>null</tt>.
     */
    protected final Set<IIpsSrcFile> findReferencingIpsSrcFiles(IpsObjectType ipsObjectType) throws CoreException {
        ArgumentCheck.notNull(ipsObjectType);

        Set<IIpsSrcFile> collectedSrcFiles = new HashSet<IIpsSrcFile>(25);
        IIpsProject[] ipsProjects = getIpsProject().findReferencingProjectLeavesOrSelf();
        for (IIpsProject ipsProject : ipsProjects) {
            IIpsSrcFile[] srcFiles = ipsProject.findIpsSrcFiles(ipsObjectType);
            for (IIpsSrcFile ipsSrcFile : srcFiles) {
                collectedSrcFiles.add(ipsSrcFile);
            }
        }
        return collectedSrcFiles;
    }

}
