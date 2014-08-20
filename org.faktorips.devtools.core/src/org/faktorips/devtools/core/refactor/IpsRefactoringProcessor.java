/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.refactor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.NullChange;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.RefactoringProcessor;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.internal.refactor.Messages;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;

/**
 * Abstract base class for all Faktor-IPS refactoring processors.
 * 
 * @author Alexander Weickmann
 */
public abstract class IpsRefactoringProcessor extends RefactoringProcessor {

    /** The {@link IIpsElement} to be refactored. */
    private final IIpsElement ipsElement;

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
        ignoredValidationMessageCodes = new HashSet<String>();
    }

    /**
     * This implementation checks that the {@link IIpsElement} to be refactored exists. If this
     * check is successful the subclass implementation
     * {@link #checkInitialConditionsThis(RefactoringStatus, IProgressMonitor)} is called which may
     * extent the initial condition checking.
     * 
     * @throws OperationCanceledException In case of an canceled operation
     */
    @Override
    public final RefactoringStatus checkInitialConditions(IProgressMonitor pm) throws CoreException {

        RefactoringStatus status = new RefactoringStatus();
        if (!(ipsElement.exists())) {
            status.addFatalError(NLS.bind(Messages.IpsRefactoringProcessor_errorIpsElementDoesNotExist,
                    ipsElement.getName()));
        }
        if (status.isOK()) {
            checkInitialConditionsThis(status, pm);
        }
        return status;
    }

    /**
     * Subclass implementation which may extend the initial condition checking.
     * <p>
     * The default implementation does nothing.
     * 
     * @param status The {@link RefactoringStatus} to add messages to
     * @param pm The {@link IProgressMonitor} to report progress to
     * 
     * @throws CoreException May be thrown at any time
     */
    protected void checkInitialConditionsThis(RefactoringStatus status, IProgressMonitor pm) throws CoreException {
        // Empty base implementation that may be overwritten by subclasses.
    }

    /**
     * This implementation triggers the user input validation and checks if all registered source
     * files are synchronized with the file system. If the checks are successful the subclass
     * implementation
     * {@link #checkFinalConditionsThis(RefactoringStatus, IProgressMonitor, CheckConditionsContext)}
     * that may extend the final condition checking is called.
     * 
     * @throws OperationCanceledException In case of an canceled operation
     */
    @Override
    public final RefactoringStatus checkFinalConditions(IProgressMonitor pm, CheckConditionsContext context)
            throws CoreException {

        RefactoringStatus status = new RefactoringStatus();
        status.merge(validateUserInput(pm));

        for (IIpsSrcFile ipsSrcFile : getAffectedIpsSrcFiles()) {
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
     * @param status The {@link RefactoringStatus} to add messages to
     * @param pm The {@link IProgressMonitor} to report progress to
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
     * 
     * @throws OperationCanceledException In case of an canceled operation
     */
    @Override
    public final Change createChange(IProgressMonitor pm) throws CoreException {
        IpsRefactoringModificationSet modificationSet = refactorIpsModel(pm);
        saveIpsSourceFiles(modificationSet, pm);
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
    public abstract IpsRefactoringModificationSet refactorIpsModel(IProgressMonitor pm) throws CoreException;

    private void saveIpsSourceFiles(IpsRefactoringModificationSet modificationSet, IProgressMonitor pm)
            throws CoreException {
        for (IpsSrcFileModification modification : modificationSet.getModifications()) {
            if (modification.getTargetIpsSrcFile().exists()) {
                modification.getTargetIpsSrcFile().save(true, pm);
            }
        }
    }

    /**
     * Returns the list of affected {@link IIpsSrcFile IPS source files}.
     * 
     * @return The list of affected {@link IIpsSrcFile IPS source files}.
     * 
     */
    protected abstract Set<IIpsSrcFile> getAffectedIpsSrcFiles();

    /**
     * This method creates modifications using all {@link IIpsSrcFile IPS source files} collected by
     * {@link #getAffectedIpsSrcFiles()}.
     * <p>
     * This method could be called in {@link #refactorIpsModel(IProgressMonitor)} to get all
     * modifications. It needs to be called before any of these source files was modified!
     * <p>
     * This method only creates normal modifications. If you have rename modifications or something
     * else you need to create these modifications by your own.
     */
    protected final void addAffectedSrcFiles(IpsRefactoringModificationSet modificationSet) {
        for (IIpsSrcFile ipsSrcFile : getAffectedIpsSrcFiles()) {
            modificationSet.addBeforeChanged(ipsSrcFile);
        }
    }

    /**
     * Returns the {@link IIpsProject} the {@link IIpsElement} to be refactored belongs to.
     */
    protected final IIpsProject getIpsProject() {
        return getIpsElement().getIpsProject();
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
        return new Object[] { getIpsElement() };
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
     * <p>
     * Because of the refactoring may be performed multiple times (preview, getting generated java
     * artifacts...) the element may be already deleted or moved and reconstructed. That means the
     * element may be identified by its name but it is not the identical object reference. In this
     * case we try to find the element by name.
     */
    public final IIpsElement getIpsElement() {
        if (ipsElement instanceof IIpsObjectPart) {
            IIpsObjectPart ipsObjectPart = (IIpsObjectPart)ipsElement;
            if (!ipsObjectPart.isDeleted()) {
                return ipsObjectPart;
            }
        }
        if (ipsElement instanceof IIpsObject) {
            IIpsObject ipsObject = (IIpsObject)ipsElement;
            // this would reload the content or simply returns the existing one
            return ipsObject.getIpsSrcFile().getIpsObject();
        }
        ArrayList<String> ids = new ArrayList<String>();
        IIpsElement element = ipsElement;
        if (element instanceof IIpsObjectPartContainer) {
            IIpsObjectPartContainer ipsObjectPartContainer = (IIpsObjectPartContainer)element;
            try {
                while (!(ipsObjectPartContainer instanceof IIpsObject)) {
                    ids.add(0, ((IIpsObjectPart)ipsObjectPartContainer).getId());
                    ipsObjectPartContainer = (IIpsObjectPartContainer)ipsObjectPartContainer.getParent();
                }
                ipsObjectPartContainer = ipsObjectPartContainer.getIpsSrcFile().getIpsObject();
                for (String id : ids) {
                    for (IIpsElement child : ipsObjectPartContainer.getChildren()) {
                        IIpsObjectPart childIpsObjectPart = (IIpsObjectPart)child;
                        if (childIpsObjectPart.getId().equals(id)) {
                            ipsObjectPartContainer = childIpsObjectPart;
                            break;
                        }
                    }
                    if (!(ipsObjectPartContainer instanceof IIpsObjectPart)
                            || !((IIpsObjectPart)ipsObjectPartContainer).getId().equals(id)) {
                        throw new RuntimeException(
                                "Cannot find element with id " + id + " in " + ipsObjectPartContainer); //$NON-NLS-1$//$NON-NLS-2$
                    }
                }
            } catch (CoreException e) {
                throw new CoreRuntimeException(e);
            }
            return ipsObjectPartContainer;
        } else {
            return ipsElement;
        }
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

    /**
     * Validates the user input by calling the subclass implementation.
     * <p>
     * If there are no errors the IPS model is checked to be in a valid state.
     * 
     * @param pm The {@link IProgressMonitor} to report progress to
     * 
     * @throws CoreException If an error occurs while validating the user input
     */
    public final RefactoringStatus validateUserInput(IProgressMonitor pm) throws CoreException {
        RefactoringStatus status = new RefactoringStatus();
        MessageList validationMessageList = new MessageList();
        validateUserInputThis(status, pm);
        if (!status.hasFatalError()) {
            validateIpsModel(validationMessageList);
            addValidationMessagesToStatus(validationMessageList, status);
        }
        return status;
    }

    /**
     * Responsible for validating the IPS model as required by the refactoring.
     * 
     * @param validationMessageList Message list to report validation errors to
     * 
     * @throws CoreException If an error occurs while validating the IPS model
     */
    protected abstract void validateIpsModel(MessageList validationMessageList) throws CoreException;

    /**
     * This operation is called by {@link #validateUserInput(IProgressMonitor)}. Subclasses must
     * implement special user input validations here.
     * 
     * @param status {@link RefactoringStatus} to report messages to
     * @param pm {@link IProgressMonitor} to report progress to
     * 
     * @throws CoreException May be thrown at any time
     */
    protected abstract void validateUserInputThis(RefactoringStatus status, IProgressMonitor pm) throws CoreException;

    /**
     * Returns whether this refactoring processor requires that all IPS source files are saved
     * before the refactoring may happen.
     */
    public abstract boolean isSourceFilesSavedRequired();

}
