/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.ipsobject.refactor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.builder.IDependencyGraph;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.internal.builder.DependencyGraph;
import org.faktorips.devtools.core.model.IDependency;
import org.faktorips.devtools.core.model.IDependencyDetail;
import org.faktorips.devtools.core.model.enums.IEnumContent;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptNamingStrategy;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.refactor.IpsRefactoringModificationSet;
import org.faktorips.devtools.core.util.RefactorUtil;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;

/**
 * Bundles common functionality of the {@link RenameIpsObjectProcessor} and
 * {@link MoveIpsObjectProcessor} classes.
 * 
 * @author Alexander Weickmann
 */
public final class MoveRenameIpsObjectHelper {

    private final IIpsObject toBeRefactored;

    private IDependency[] dependencies;

    private Map<IDependency, IIpsProject> dependencyToProject;

    /**
     * @throws NullPointerException If the provided {@link IIpsObject} is null
     */
    public MoveRenameIpsObjectHelper(IIpsObject toBeRefactored) {
        ArgumentCheck.notNull(new Object[] { toBeRefactored });
        this.toBeRefactored = toBeRefactored;
    }

    /**
     * Adds message codes to the set of ignored validation message codes that must be ignored by the
     * "Rename Type" and "Move Type" refactorings.
     * <p>
     * For example: The configuring {@link IProductCmptType} / configured {@link IPolicyCmptType}
     * does not reference the copy of the <tt>IType</tt> that is created during the refactoring so
     * this must be ignored during refactoring validation.
     */
    public void addIgnoredValidationMessageCodes(Set<String> ignoredValidationMessageCodes) {
        ignoredValidationMessageCodes.add(IPolicyCmptType.MSGCODE_PRODUCT_CMPT_TYPE_DOES_NOT_CONFIGURE_THIS_TYPE);
        ignoredValidationMessageCodes.add(IPolicyCmptTypeAssociation.MSGCODE_INVERSE_RELATION_MISMATCH);
        ignoredValidationMessageCodes.add(IPolicyCmptTypeAssociation.MSGCODE_INVERSE_RELATION_DOES_NOT_EXIST_IN_TARGET);
        ignoredValidationMessageCodes.add(IPolicyCmptTypeAssociation.MSGCODE_MATCHING_ASSOCIATION_INVALID);
        ignoredValidationMessageCodes.add(IProductCmptTypeAssociation.MSGCODE_MATCHING_ASSOCIATION_INVALID);
        ignoredValidationMessageCodes.add(IAssociation.MSGCODE_TARGET_DOES_NOT_EXIST);
        ignoredValidationMessageCodes.add(IProductCmptType.MSGCODE_POLICY_CMPT_TYPE_DOES_NOT_SPECIFY_THIS_TYPE);
        ignoredValidationMessageCodes.add(IIpsProject.MSGCODE_RUNTIME_ID_COLLISION);
        ignoredValidationMessageCodes.add(IEnumContent.MSGCODE_ENUM_CONTENT_NAME_NOT_CORRECT);
        ignoredValidationMessageCodes.add(IProductCmptLink.MSGCODE_UNKNWON_TARGET);
        ignoredValidationMessageCodes.add(IProductCmptLink.MSGCODE_INVALID_TARGET);
        ignoredValidationMessageCodes.add("KorrespondenzenExtensionPropertyDefinition-FalscherProduktBaustein"); //$NON-NLS-1$
        // TODO diesen miesen Hack entfernen wenn FIPS-965 behoben ist.
    }

    /**
     * Returns a list containing the {@link IIpsSrcFile}s that are affected by the refactoring.
     */
    public List<IIpsSrcFile> getAffectedIpsSrcFiles() {
        List<IIpsSrcFile> ipsSrcFiles = new ArrayList<IIpsSrcFile>(getDependencies().length);
        try {
            ipsSrcFiles.add(toBeRefactored.getIpsSrcFile());
            for (IDependency dependency : getDependencies()) {
                IIpsSrcFile ipsSrcFile = getDependencyToProject().get(dependency)
                        .findIpsSrcFile(dependency.getSource());
                ipsSrcFiles.add(ipsSrcFile);
            }
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
        return ipsSrcFiles;
    }

    /**
     * Check if there is already a source file with the new name in this package.
     */
    public void validateIpsModel(IIpsPackageFragment targetIpsPackageFragment,
            String newName,
            MessageList validationMessageList) throws CoreException {

        for (IIpsSrcFile ipsSrcFile : targetIpsPackageFragment.getIpsSrcFiles()) {
            String sourceFileName = ipsSrcFile.getName();
            if (sourceFileName.equals(newName + '.' + toBeRefactored.getIpsObjectType().getFileExtension())) {
                String text = NLS.bind(Messages.MoveRenameIpsObjectHelper_msgSourceFileAlreadyExists, newName,
                        targetIpsPackageFragment.getName());
                validationMessageList.add(new Message(null, text, Message.ERROR));
                break;
            }
        }
    }

    /**
     * The source file of the {@link IIpsObject} will be moved this early. Based on that new source
     * file and on the moved {@link IIpsObject} validation is performed. After validation the file
     * is moved back to perform the real model refactoring.
     * <p>
     * Returns the list of validation messages that should be added to the return status by the
     * calling refactoring processor.
     */
    public MessageList checkFinalConditionsThis(IIpsPackageFragment targetIpsPackageFragment,
            String newName,
            RefactoringStatus status,
            IProgressMonitor pm) throws CoreException {

        IIpsSrcFile originalFile = toBeRefactored.getIpsSrcFile();
        IIpsSrcFile targetFile = null;
        try {
            targetFile = RefactorUtil.moveIpsSrcFile(originalFile, targetIpsPackageFragment, newName, pm);

            // Perform validation on target file.
            IIpsObject copiedIpsObject = targetFile.getIpsObject();
            MessageList validationMessageList = copiedIpsObject.validate(copiedIpsObject.getIpsProject());

            return validationMessageList;
            // CSOFF: IllegalCatch
            // Need to catch RuntimeException to get really every exception and set corresponding
            // fatal error status
        } catch (RuntimeException e) {
            status.addFatalError(e.getLocalizedMessage());
            return new MessageList();
            // CSON: IllegalCatch
        } finally {
            if (targetFile != null) {
                RefactorUtil.moveIpsSrcFile(targetFile, originalFile.getIpsPackageFragment(),
                        originalFile.getIpsObjectName(), pm);
            }
        }
    }

    public IpsRefactoringModificationSet refactorIpsModel(IIpsPackageFragment targetIpsPackageFragment,
            String newName,
            boolean adaptRuntimeId,
            IProgressMonitor pm) throws CoreException {
        IpsRefactoringModificationSet modifications = new IpsRefactoringModificationSet(toBeRefactored);
        modifications.append(updateDependencies(targetIpsPackageFragment, newName));
        modifications.addRenameModification(toBeRefactored.getIpsSrcFile(), targetIpsPackageFragment
                .getIpsSrcFile((RefactorUtil.getTargetFileName(toBeRefactored.getIpsSrcFile(), newName))));
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
    }

    private IpsRefactoringModificationSet updateDependencies(IIpsPackageFragment targetIpsPackageFragment,
            String newName) throws CoreException {
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
            IProgressMonitor pm) throws CoreException {

        IIpsSrcFile originalSrcFile = toBeRefactored.getIpsSrcFile();

        /*
         * Save the original source file if it is dirty as it is possible that is has been modified
         * during updateDependencies(...)
         */
        if (originalSrcFile.isDirty()) {
            originalSrcFile.save(true, null);
        }

        IIpsSrcFile targetSrcFile = null;
        targetSrcFile = RefactorUtil.moveIpsSrcFile(originalSrcFile, targetIpsPackageFragment, newName, pm);

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

    private boolean isMatching(IDependency dependency) throws CoreException {
        Object target = dependency.getTarget();

        if (target instanceof QualifiedNameType) {
            return (toBeRefactored.getQualifiedNameType().equals(target));
        } else if (target instanceof String) {
            return toBeRefactored.getQualifiedName().equals(target);
        } else {
            throw new CoreException(new IpsStatus("The type of the dependency-target (" + target + ") is unknown.")); //$NON-NLS-1$ //$NON-NLS-2$
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
        dependencyToProject = new HashMap<IDependency, IIpsProject>();
        List<IDependency> collectedDependencies = new ArrayList<IDependency>();

        try {
            addDependencies(collectedDependencies, toBeRefactored.getIpsProject());
            IIpsProject[] projects = toBeRefactored.getIpsProject().findReferencingProjects(true);
            for (IIpsProject project : projects) {
                addDependencies(collectedDependencies, project);
            }
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }

        dependencies = collectedDependencies.toArray(new IDependency[collectedDependencies.size()]);
    }

    private void addDependencies(List<IDependency> dependencies, IIpsProject project) throws CoreException {
        IDependencyGraph graph = new DependencyGraph(project);
        for (IDependency dependency : graph.getDependants(toBeRefactored.getQualifiedNameType())) {
            dependencies.add(dependency);
            dependencyToProject.put(dependency, project);
        }
    }

}
