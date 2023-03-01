/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
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
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.NullChange;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.RefactoringProcessor;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.abstraction.AResource.AResourceTreeTraversalDepth;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.core.internal.refactor.Messages;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.util.ArgumentCheck;

/**
 * Abstract base class for all Faktor-IPS refactoring processors.
 * 
 * @author Alexander Weickmann
 */
public abstract class IpsRefactoringProcessor extends RefactoringProcessor {

    /** The {@link IIpsElement} to be refactored. */
    private final IIpsElement ipsElement;

    /**
     * @param ipsElement {@link IIpsElement} to be refactored
     * 
     * @throws NullPointerException If the parameter is null
     */
    protected IpsRefactoringProcessor(IIpsElement ipsElement) {
        super();
        ArgumentCheck.notNull(ipsElement);

        this.ipsElement = ipsElement;
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
    public final RefactoringStatus checkInitialConditions(IProgressMonitor pm) {

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
     * @throws IpsException May be thrown at any time
     */
    protected void checkInitialConditionsThis(RefactoringStatus status, IProgressMonitor pm) {
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
    public final RefactoringStatus checkFinalConditions(IProgressMonitor pm, CheckConditionsContext context) {

        RefactoringStatus status = new RefactoringStatus();
        status.merge(validateUserInput(pm));

        for (IIpsSrcFile ipsSrcFile : getAffectedIpsSrcFiles()) {
            if (!(ipsSrcFile.getEnclosingResource().isSynchronized(AResourceTreeTraversalDepth.RESOURCE_ONLY))) {
                status.addFatalError(NLS.bind(Messages.IpsRefactoringProcessor_errorIpsSrcFileOutOfSync, ipsSrcFile
                        .getCorrespondingResource().getWorkspaceRelativePath()));
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
     * @throws IpsException May be thrown at any time
     */
    protected void checkFinalConditionsThis(RefactoringStatus status,
            IProgressMonitor pm,
            CheckConditionsContext context) {

        // Empty base implementation that may be overwritten by subclasses.
    }

    /**
     * Adds an entry to the provided {@link RefactoringStatus} for every messages contained in the
     * provided {@link MessageList}.
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
            switch (message.getSeverity()) {
                case ERROR:
                    status.addError(message.getText());
                    break;
                case WARNING:
                    status.addWarning(message.getText());
                    break;
                case INFO:
                    status.addInfo(message.getText());
                    break;
                default:
                    break;
            }
        }
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
    public final Change createChange(IProgressMonitor pm) {
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
     * @throws IpsException Subclasses may throw this kind of exception any time
     */
    public abstract IpsRefactoringModificationSet refactorIpsModel(IProgressMonitor pm) throws IpsException;

    private void saveIpsSourceFiles(IpsRefactoringModificationSet modificationSet, IProgressMonitor pm) {
        for (IpsSrcFileModification modification : modificationSet.getModifications()) {
            if (modification.getTargetIpsSrcFile().exists()) {
                modification.getTargetIpsSrcFile().save(pm);
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

    @Override
    public final Object[] getElements() {
        return new Object[] { getIpsElement() };
    }

    /**
     * This implementation always returns true, may be overwritten by subclasses if necessary.
     */
    @Override
    public boolean isApplicable() {
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
        if (ipsElement instanceof IIpsObjectPart ipsObjectPart) {
            if (!ipsObjectPart.isDeleted()) {
                return ipsObjectPart;
            }
        }
        if (ipsElement instanceof IIpsObject ipsObject) {
            // this would reload the content or simply returns the existing one
            return ipsObject.getIpsSrcFile().getIpsObject();
        }
        if (ipsElement instanceof IIpsObjectPartContainer) {
            return findInContainer(ipsElement);
        } else {
            return ipsElement;
        }
    }

    private IIpsElement findInContainer(IIpsElement element) {
        ArrayList<String> ids = new ArrayList<>();
        IIpsObjectPartContainer ipsObjectPartContainer = (IIpsObjectPartContainer)element;
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
        return ipsObjectPartContainer;
    }

    /**
     * Searches for all {@link IIpsSrcFile}s in the object path of all {@link IIpsProject}s
     * referencing the {@link IIpsProject} that contains the {@link IIpsElement} to be refactored.
     * 
     * @param ipsObjectType Only {@link IIpsSrcFile IpsSrcFiles} with one of these
     *            {@link IpsObjectType types} are searched
     * 
     * @throws IpsException If an error occurs while searching for the source files
     * @throws NullPointerException If the parameter is null
     */
    protected final Set<IIpsSrcFile> findReferencingIpsSrcFiles(IpsObjectType... ipsObjectType) {
        ArgumentCheck.notNull(ipsObjectType);

        Set<IIpsSrcFile> collectedSrcFiles = new HashSet<>(25);
        IIpsProject[] ipsProjects = getIpsProject().findReferencingProjectLeavesOrSelf();
        for (IIpsProject ipsProject : ipsProjects) {
            List<IIpsSrcFile> srcFiles = ipsProject.findAllIpsSrcFiles(ipsObjectType);
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
     * @throws IpsException If an error occurs while validating the user input
     */
    public final RefactoringStatus validateUserInput(IProgressMonitor pm) {
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
     * @throws IpsException If an error occurs while validating the IPS model
     */
    protected abstract void validateIpsModel(MessageList validationMessageList) throws IpsException;

    /**
     * This operation is called by {@link #validateUserInput(IProgressMonitor)}. Subclasses must
     * implement special user input validations here.
     * 
     * @param status {@link RefactoringStatus} to report messages to
     * @param pm {@link IProgressMonitor} to report progress to
     * 
     * @throws IpsException May be thrown at any time
     */
    protected abstract void validateUserInputThis(RefactoringStatus status, IProgressMonitor pm)
            throws IpsException;

    /**
     * Returns whether this refactoring processor requires that all IPS source files are saved
     * before the refactoring may happen.
     */
    public abstract boolean isSourceFilesSavedRequired();

}
