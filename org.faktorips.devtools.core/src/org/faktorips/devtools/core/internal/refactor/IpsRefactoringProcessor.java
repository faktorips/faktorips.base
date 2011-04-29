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
 * Abstract base class for all Faktor-IPS refactorings.
 * 
 * @see ProcessorBasedRefactoring
 * 
 * @author Alexander Weickmann
 */
public abstract class IpsRefactoringProcessor extends RefactoringProcessor implements IIpsRefactoringProcessor {

    /** {@link IIpsElement} to be refactored. */
    private final IIpsElement ipsElement;

    /** Set containing all {@link IIpsSrcFile}s that are affected by the refactoring. */
    private final Set<IIpsSrcFile> affectedIpsSrcFiles;

    /** Set containing all message codes that will be ignored during final condition checking. */
    private final Set<String> ignoredValidationMessageCodes;

    /**
     * @param ipsElement {@link IIpsElement} to be refactored
     * 
     * @throws NullPointerException If the parameter is null
     */
    protected IpsRefactoringProcessor(IIpsElement ipsElement) {
        super();
        ArgumentCheck.notNull(ipsElement);

        this.ipsElement = ipsElement;
        affectedIpsSrcFiles = new HashSet<IIpsSrcFile>();
        ignoredValidationMessageCodes = new HashSet<String>();
    }

    /**
     * This implementation checks that the {@link IIpsElement} to be refactored exists.
     */
    @Override
    public final RefactoringStatus checkInitialConditions(IProgressMonitor pm) throws CoreException,
            OperationCanceledException {

        RefactoringStatus status = new RefactoringStatus();
        if (!(ipsElement.exists())) {
            status.addFatalError(NLS.bind(Messages.IpsRefactoringProcessor_errorIpsElementDoesNotExist,
                    ipsElement.getName()));
        }
        return status;
    }

    /**
     * This implementation triggers the user input validation, checks if all registered source files
     * are synchronized and calls the subclass implementation
     * {@link #checkFinalConditionsThis(RefactoringStatus, IProgressMonitor, CheckConditionsContext)}
     * that may extend the final condition checking.
     */
    @Override
    public final RefactoringStatus checkFinalConditions(IProgressMonitor pm, CheckConditionsContext context)
            throws CoreException, OperationCanceledException {

        RefactoringStatus status = new RefactoringStatus();
        status.merge(validateUserInput(pm));

        addIpsSrcFiles();
        for (IIpsSrcFile ipsSrcFile : affectedIpsSrcFiles) {
            if (!(ipsSrcFile.getEnclosingResource().isSynchronized(IResource.DEPTH_ZERO))) {
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
     * Subclass implementation which may extend the final condition checking.
     * <p>
     * The default implementation does nothing.
     * 
     * @param status {@link RefactoringStatus} to add messages to
     * @param pm {@link IProgressMonitor} to report progress to
     * @param context Condition checking context to collect shared condition checks
     * 
     * @throws CoreException May be thrown at any time
     */
    protected void checkFinalConditionsThis(RefactoringStatus status,
            IProgressMonitor pm,
            CheckConditionsContext context) throws CoreException {

        // Empty base implementation that may be overwritten by subclasses.
    }

    /**
     * Adds an entry to the provided {@link RefactoringStatus} for every messages contained in the
     * provided {@link MessageList}.
     * <p>
     * Excluded are validation message codes that are ignored by the refactoring processor.
     * 
     * @see #getIgnoredValidationMessageCodes()
     * 
     * @param validationMessageList {@link MessageList} from which messages shall be added to the
     *            {@link RefactoringStatus}
     * @param status {@link RefactoringStatus} to add entries to
     * 
     * @throws NullPointerException If any parameter is null
     */
    protected final void addValidationMessagesToStatus(MessageList validationMessageList, RefactoringStatus status) {
        ArgumentCheck.notNull(new Object[] { validationMessageList, status });

        for (Message message : validationMessageList) {
            if (isMessageCodeIgnored(message.getCode())) {
                continue;
            }
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

    private boolean isMessageCodeIgnored(String messageCode) {
        for (String currentMessageCode : ignoredValidationMessageCodes) {
            if (currentMessageCode.equals(messageCode)) {
                return true;
            }
        }
        return false;
    }

    /**
     * This implementation triggers the refactoring of the Faktor-IPS model. The registered IPS
     * source files will be saved after all modifications are complete.
     * <p>
     * Always returns a {@link NullChange}.
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
     * @param pm {@link IProgressMonitor} to report progress to if necessary
     * 
     * @throws CoreException Subclasses may throw this kind of exception any time
     */
    protected abstract void refactorIpsModel(IProgressMonitor pm) throws CoreException;

    private void saveIpsSourceFiles(IProgressMonitor pm) throws CoreException {
        for (IIpsSrcFile ipsSrcFile : affectedIpsSrcFiles) {
            // File may not exist if it has been moved during refactoring.
            if (ipsSrcFile.exists()) {
                ipsSrcFile.save(true, pm);
            }
        }
    }

    /**
     * Subclass implementation responsible for adding all {@link IIpsSrcFile}s touched by this
     * refactoring.
     * 
     * @see #addIpsSrcFile(IIpsSrcFile)
     * 
     * @throws CoreException May be thrown at any time
     */
    protected abstract void addIpsSrcFiles() throws CoreException;

    /**
     * Adds the given {@link IIpsSrcFile} to this refactoring.
     * <p>
     * Added source files will be processed and saved. If the {@link IIpsSrcFile} in question was
     * already added before, nothing will happen.
     * 
     * @param ipsSrcFile {@link IIpsSrcFile} to add
     * 
     * @throws NullPointerException If the parameter is null
     */
    protected final void addIpsSrcFile(IIpsSrcFile ipsSrcFile) {
        ArgumentCheck.notNull(ipsSrcFile);
        affectedIpsSrcFiles.add(ipsSrcFile);
    }

    /**
     * Returns the {@link IIpsProject} the {@link IIpsElement} to be refactored belongs to.
     */
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
     * This implementation always returns true, may be overwritten by subclasses if necessary.
     */
    @Override
    public boolean isApplicable() throws CoreException {
        return true;
    }

    /**
     * Returns the {@link IIpsElement} to be refactored.
     */
    @Override
    public final IIpsElement getIpsElement() {
        return ipsElement;
    }

    /**
     * Searches for all {@link IIpsSrcFile}s in the object path of all {@link IIpsProject}s
     * referencing the {@link IIpsProject} that contains the {@link IIpsElement} to be refactored.
     * 
     * @param ipsObjectType Only {@link IIpsSrcFile}s with this {@link IpsObjectType} are searched
     * 
     * @throws CoreException If an error occurs while searching for the source files
     * @throws NullPointerException If the parameter is null
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
