/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.ipsobject.refactor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.core.model.ipsobject.refactor.IIpsMoveRenameIpsObjectProcessor;
import org.faktorips.devtools.core.refactor.IpsRefactoringModificationSet;
import org.faktorips.devtools.core.refactor.IpsRefactoringProcessor;
import org.faktorips.devtools.model.builder.IDependencyGraph;
import org.faktorips.devtools.model.builder.IJavaBuilderSet;
import org.faktorips.devtools.model.dependency.IDependency;
import org.faktorips.devtools.model.dependency.IDependencyDetail;
import org.faktorips.devtools.model.internal.builder.DependencyGraph;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.plugin.IpsStatus;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptNamingStrategy;
import org.faktorips.devtools.model.util.RefactorUtil;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.util.ArgumentCheck;

/**
 * Bundles common functionality of the {@link RenameIpsObjectProcessor} and
 * {@link MoveIpsObjectProcessor} classes.
 * 
 * @author Alexander Weickmann
 */
public final class MoveRenameIpsObjectHelper implements IIpsMoveRenameIpsObjectProcessor {

    private final IIpsObject toBeRefactored;

    private IDependency[] dependencies;

    private Map<IDependency, IIpsProject> dependencyToProject;

    private List<IJavaElement> targetJavaElements;

    /**
     * @throws NullPointerException If the provided {@link IIpsObject} is null
     */
    public MoveRenameIpsObjectHelper(IIpsObject toBeRefactored) {
        ArgumentCheck.notNull(new Object[] { toBeRefactored });
        this.toBeRefactored = toBeRefactored;
    }

    /**
     * Returns a list containing the {@link IIpsSrcFile}s that are affected by the refactoring.
     */
    public List<IIpsSrcFile> getAffectedIpsSrcFiles() {
        List<IIpsSrcFile> ipsSrcFiles = new ArrayList<>(getDependencies().length);
        ipsSrcFiles.add(toBeRefactored.getIpsSrcFile());
        for (IDependency dependency : getDependencies()) {
            IIpsSrcFile ipsSrcFile = getDependencyToProject().get(dependency).findIpsSrcFile(dependency.getSource());
            ipsSrcFiles.add(ipsSrcFile);
        }
        return ipsSrcFiles;
    }

    /**
     * Check if there is already a source file with the new name in this package.
     */
    public void validateIpsModel(IIpsPackageFragment targetIpsPackageFragment,
            String newName,
            MessageList validationMessageList) {
        for (IIpsSrcFile ipsSrcFile : targetIpsPackageFragment.getIpsSrcFiles()) {
            String sourceFileName = ipsSrcFile.getIpsObjectName();
            if (isInvalidNewName(newName, sourceFileName)) {
                String text = NLS.bind(Messages.MoveRenameIpsObjectHelper_msgSourceFileAlreadyExists, newName,
                        targetIpsPackageFragment.getName());
                validationMessageList.add(new Message(null, text, Message.ERROR));
                break;
            }
        }
    }

    private boolean isInvalidNewName(String newName, String sourceFileName) {
        return !toBeRefactored.getName().equalsIgnoreCase(newName) && sourceFileName.equalsIgnoreCase(newName);
    }

    /**
     * The source file of the {@link IIpsObject} will be moved this early. Based on that new source
     * file and on the moved {@link IIpsObject} validation is performed. After validation the file
     * is moved back to perform the real model refactoring.
     * <p>
     * Returns the list of validation messages that should be added to the return status by the
     * calling refactoring processor.
     */
    public MessageList checkFinalConditionsThis(IpsRefactoringProcessor ipsRefactoringProcessor,
            RefactoringStatus status,
            IProgressMonitor pm) {
        IIpsSrcFile originalFile = toBeRefactored.getIpsSrcFile();
        if (isSourceFilesSavedRequired() && originalFile.isDirty()) {
            String text = NLS.bind(Messages.MoveRenameIpsObjectHelper_msgSourceFileDirty,
                    originalFile.getIpsObjectName());
            status.addFatalError(text);
            return new MessageList();
        }
        IpsRefactoringModificationSet modificationSet = null;
        try {
            modificationSet = ipsRefactoringProcessor.refactorIpsModel(pm);
            IIpsObjectPartContainer refactoredTarget = (IIpsObjectPartContainer)modificationSet.getTargetElement();

            rememberTargetJavaElementsForRefactoringParticipants(refactoredTarget);

            return refactoredTarget.validate(refactoredTarget.getIpsProject());
            // CSOFF: IllegalCatch
            // Need to catch Exception to get really every exception and set corresponding
            // fatal error status
        } catch (IpsException e) {
            if (e.getCause() instanceof CoreException) {
                addExceptionStatus(status, ((CoreException)e.getCause()).getStatus());
            } else {
                status.addFatalError(e.getLocalizedMessage());
            }
            return new MessageList();
        } catch (Exception e) {
            if (e.getLocalizedMessage() != null) {
                status.addFatalError(e.getLocalizedMessage());
            } else {
                status.addFatalError("no message available " + e); //$NON-NLS-1$
            }
            return new MessageList();
            // CSON: IllegalCatch
        } finally {
            if (modificationSet != null) {
                modificationSet.undo();
            }
        }
    }

    private void addExceptionStatus(RefactoringStatus status, IStatus exceptionStatus) {
        if (exceptionStatus != null) {
            int severity = exceptionStatus.getSeverity();
            switch (severity) {
                case IStatus.ERROR:
                    status.addFatalError(exceptionStatus.getMessage());
                    break;
                case IStatus.WARNING:
                    status.addWarning(exceptionStatus.getMessage());
                    break;
                case IStatus.INFO:
                    status.addInfo(exceptionStatus.getMessage());
                    break;

                default:
                    break;
            }
            for (IStatus childStatus : exceptionStatus.getChildren()) {
                addExceptionStatus(status, childStatus);
            }
        }
    }

    private void rememberTargetJavaElementsForRefactoringParticipants(IIpsObjectPartContainer copiedIpsObject) {
        IIpsArtefactBuilderSet ipsArtefactBuilderSet = copiedIpsObject.getIpsProject().getIpsArtefactBuilderSet();
        if (ipsArtefactBuilderSet instanceof IJavaBuilderSet) {
            IJavaBuilderSet javaBuilderSet = (IJavaBuilderSet)ipsArtefactBuilderSet;
            targetJavaElements = javaBuilderSet.getGeneratedJavaElements(copiedIpsObject);
        }
    }

    public IpsRefactoringModificationSet refactorIpsModel(IIpsPackageFragment targetIpsPackageFragment,
            String newName,
            boolean adaptRuntimeId,
            IProgressMonitor pm) {
        IpsRefactoringModificationSet modifications = new IpsRefactoringModificationSet(toBeRefactored);
        try {
            modifications.append(updateDependencies(targetIpsPackageFragment, newName));
            modifications.addRenameModification(
                    toBeRefactored.getIpsSrcFile(),
                    targetIpsPackageFragment.getIpsSrcFile((RefactorUtil.getTargetFileName(
                            toBeRefactored.getIpsSrcFile(), newName))));
            IIpsSrcFile targetSrcFile = moveSourceFileToTargetFile(targetIpsPackageFragment, newName, pm);
            modifications.setTargetElement(targetSrcFile.getIpsObject());

            if (adaptRuntimeId && toBeRefactored instanceof IProductCmpt) {
                IProductCmpt productCmpt = (IProductCmpt)toBeRefactored;
                IIpsProject ipsProject = productCmpt.getIpsProject();
                IProductCmptNamingStrategy productCmptNamingStrategy = ipsProject.getProductCmptNamingStrategy();
                String newRuntimeId = productCmptNamingStrategy.getUniqueRuntimeId(ipsProject, newName);
                ((IProductCmpt)targetSrcFile.getIpsObject()).setRuntimeId(newRuntimeId);
            }
            return modifications;
        } catch (IpsException e) {
            modifications.undo();
            throw e;
        }
    }

    private IpsRefactoringModificationSet updateDependencies(IIpsPackageFragment targetIpsPackageFragment,
            String newName) {
        IpsRefactoringModificationSet modifications = new IpsRefactoringModificationSet(null);
        for (IDependency dependency : getDependencies()) {
            if (!isMatching(dependency)) {
                continue;
            }

            IIpsProject ipsProject = getDependencyToProject().get(dependency);
            List<IDependencyDetail> details = ipsProject.findIpsObject(dependency.getSource()).getDependencyDetails(
                    dependency);
            modifications.addBeforeChanged(ipsProject.findIpsSrcFile(dependency.getSource()));
            for (IDependencyDetail detail : details) {
                detail.refactorAfterRename(targetIpsPackageFragment, newName);
            }
        }
        return modifications;
    }

    private IIpsSrcFile moveSourceFileToTargetFile(IIpsPackageFragment targetIpsPackageFragment,
            String newName,
            IProgressMonitor pm) {

        IIpsSrcFile originalSrcFile = toBeRefactored.getIpsSrcFile();

        /*
         * Save the original source file if it is dirty as it is possible that is has been modified
         * during updateDependencies(...)
         */
        if (originalSrcFile.isDirty()) {
            originalSrcFile.save(null);
        }

        IIpsSrcFile targetSrcFile = RefactorUtil.moveIpsSrcFile(originalSrcFile, targetIpsPackageFragment, newName, pm);
        /*
         * If the original source file has changed by means of the updateDependencies(...) method,
         * we have to touch the new file so that the change is reported to the environment.
         */
        targetSrcFile.getCorrespondingFile().touch(pm);

        return targetSrcFile;
    }

    public boolean isSourceFilesSavedRequired() {
        return true;
    }

    private boolean isMatching(IDependency dependency) {
        Object target = dependency.getTarget();

        if (target instanceof QualifiedNameType) {
            return (toBeRefactored.getQualifiedNameType().equals(target));
        } else if (target instanceof String) {
            return toBeRefactored.getQualifiedName().equals(target);
        } else {
            throw new IpsException(
                    new IpsStatus("The type of the dependency-target (" + target + ") is unknown.")); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    private IDependency[] getDependencies() {
        if (dependencies == null) {
            collectDependcies();
        }
        return dependencies;
    }

    private Map<IDependency, IIpsProject> getDependencyToProject() {
        if (dependencyToProject == null) {
            collectDependcies();
        }
        return dependencyToProject;
    }

    private void collectDependcies() {
        dependencyToProject = new HashMap<>();
        List<IDependency> collectedDependencies = new ArrayList<>();

        addDependencies(collectedDependencies, toBeRefactored.getIpsProject());
        IIpsProject[] projects = toBeRefactored.getIpsProject().findReferencingProjects(true);
        for (IIpsProject project : projects) {
            addDependencies(collectedDependencies, project);
        }

        dependencies = collectedDependencies.toArray(new IDependency[collectedDependencies.size()]);
    }

    private void addDependencies(List<IDependency> dependencies, IIpsProject project) {
        IDependencyGraph graph = new DependencyGraph(project);
        for (IDependency dependency : graph.getDependants(toBeRefactored.getQualifiedNameType())) {
            dependencies.add(dependency);
            dependencyToProject.put(dependency, project);
        }
    }

    @Override
    public List<IJavaElement> getTargetJavaElements() {
        if (targetJavaElements != null) {
            return targetJavaElements;
        } else {
            return Collections.emptyList();
        }
    }

}
