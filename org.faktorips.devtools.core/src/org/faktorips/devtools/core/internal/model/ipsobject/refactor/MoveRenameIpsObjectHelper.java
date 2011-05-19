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

package org.faktorips.devtools.core.internal.model.ipsobject.refactor;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.NullChange;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.resource.DeleteResourceChange;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.builder.DependencyGraph;
import org.faktorips.devtools.core.model.IDependency;
import org.faktorips.devtools.core.model.IDependencyDetail;
import org.faktorips.devtools.core.model.enums.IEnumContent;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptNamingStrategy;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.util.BeanUtil;
import org.faktorips.devtools.core.util.RefactorUtil;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.StringUtil;
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
        ignoredValidationMessageCodes.add(IAssociation.MSGCODE_TARGET_DOES_NOT_EXIST);
        ignoredValidationMessageCodes.add(IProductCmptType.MSGCODE_POLICY_CMPT_TYPE_DOES_NOT_SPECIFY_THIS_TYPE);
        ignoredValidationMessageCodes.add(IIpsProject.MSGCODE_RUNTIME_ID_COLLISION);
        ignoredValidationMessageCodes.add(IEnumContent.MSGCODE_ENUM_CONTENT_NAME_NOT_CORRECT);
    }

    /**
     * Returns a list containing the {@link IIpsSrcFile}s that are affected by the refactoring.
     */
    public List<IIpsSrcFile> addIpsSrcFiles() throws CoreException {
        List<IIpsSrcFile> ipsSrcFiles = new ArrayList<IIpsSrcFile>(getDependencies().length);
        for (IDependency dependency : getDependencies()) {
            IIpsSrcFile ipsSrcFile = getDependencyToProject().get(dependency).findIpsSrcFile(dependency.getSource());
            ipsSrcFiles.add(ipsSrcFile);
        }
        return ipsSrcFiles;
    }

    /**
     * Can't validate because for IPS object validation the object's source file must be copied.
     * Validation will still be performed during final condition checking so it should be OK. We
     * check if there is already a source file with the new name in this package however.
     */
    public void validateUserInputThis(IIpsPackageFragment targetIpsPackageFragment,
            String newName,
            RefactoringStatus status) throws CoreException {

        for (IIpsSrcFile ipsSrcFile : targetIpsPackageFragment.getIpsSrcFiles()) {
            String sourceFileName = ipsSrcFile.getName();
            if (sourceFileName.equals(newName + '.' + toBeRefactored.getIpsObjectType().getFileExtension())) {
                status.addFatalError(NLS.bind(Messages.MoveRenameIpsObjectHelper_msgSourceFileAlreadyExists, newName,
                        targetIpsPackageFragment.getName()));
                break;
            }
        }
    }

    /**
     * The source file of the {@link IIpsObject} will be copied this early. Based on that new source
     * file and on the copied {@link IIpsObject} validation is performed. If the validation fails
     * the copy will be deleted so everything is left in a clean state (as it was before). If only a
     * warning or information validation message results the copy must also be deleted and recreated
     * later if the user really starts the refactoring.
     * <p>
     * Returns the list of validation messages that should be added to the return status by the
     * calling refactoring processor.
     */
    public MessageList checkFinalConditionsThis(IIpsPackageFragment targetIpsPackageFragment,
            String newName,
            RefactoringStatus status,
            IProgressMonitor pm) throws CoreException {

        Change undoDeleteChange = new NullChange();
        try {
            IIpsSrcFile fileToBeCopied = toBeRefactored.getIpsSrcFile();
            IIpsSrcFile targetFile;

            if (targetIpsPackageFragment.equals(fileToBeCopied.getIpsPackageFragment())
                    && isOnlyCapitalizationChanged(fileToBeCopied, newName)) {
                // Copy original file to temporary file with time stamp to avoid file system
                // problems
                IIpsSrcFile tempSrcFile = RefactorUtil.copyIpsSrcFileToTemporary(fileToBeCopied,
                        targetIpsPackageFragment, newName, pm);

                // Delete original file using refactoring to be able to undo later
                undoDeleteChange = performDeleteRefactoring(fileToBeCopied, pm);

                // Copy temporary file to target file and delete temporary file
                targetFile = RefactorUtil.copyIpsSrcFile(tempSrcFile, targetIpsPackageFragment, newName, pm);
                deleteIpsSourceFile(tempSrcFile, pm);

            } else {
                // Copy original file to target file
                targetFile = RefactorUtil.copyIpsSrcFile(fileToBeCopied, targetIpsPackageFragment, newName, pm);

                // Delete original file using refactoring to be able to undo later
                undoDeleteChange = performDeleteRefactoring(fileToBeCopied, pm);
            }

            // Perform validation on target file.
            IIpsObject copiedIpsObject = targetFile.getIpsObject();
            MessageList validationMessageList = copiedIpsObject.validate(copiedIpsObject.getIpsProject());

            /*
             * Delete target file (can't leave it here already because participants condition
             * checking may fail which would abort the refactoring completely and we don't want to
             * have the copy around in this case).
             */
            deleteIpsSourceFile(targetFile, pm);

            return validationMessageList;

        } catch (CoreException e) {
            status.addFatalError(e.getLocalizedMessage());
            return new MessageList();

        } finally {
            // Roll-back original source file by undo delete refactoring
            undoDeleteChange.perform(pm);
        }
    }

    private boolean isOnlyCapitalizationChanged(IIpsSrcFile fileToBeCopied, String newName) {
        String oldName = StringUtil.getFilenameWithoutExtension(fileToBeCopied.getName());
        return newName.toLowerCase().equals(oldName.toLowerCase());
    }

    private Change performDeleteRefactoring(IIpsSrcFile fileToBeCopied, IProgressMonitor pm) throws CoreException {
        IPath resourcePath = fileToBeCopied.getCorrespondingResource().getFullPath();
        DeleteResourceChange deleteResourceChange = new DeleteResourceChange(resourcePath, true);
        return deleteResourceChange.perform(pm);
    }

    public void refactorIpsModel(IIpsPackageFragment targetIpsPackageFragment,
            String newName,
            boolean adaptRuntimeId,
            IProgressMonitor pm) throws CoreException {

        updateDependencies(targetIpsPackageFragment, newName);
        IIpsSrcFile targetSrcFile = copySourceFileToTargetFile(targetIpsPackageFragment, newName, pm);

        if (adaptRuntimeId && toBeRefactored instanceof IProductCmpt) {
            IProductCmpt productCmpt = (IProductCmpt)toBeRefactored;
            IIpsProject ipsProject = productCmpt.getIpsProject();
            IProductCmptNamingStrategy productCmptNamingStrategy = ipsProject.getProductCmptNamingStrategy();
            String newRuntimeId = productCmptNamingStrategy.getUniqueRuntimeId(ipsProject, newName);
            ((IProductCmpt)targetSrcFile.getIpsObject()).setRuntimeId(newRuntimeId);
            targetSrcFile.save(true, pm);
        }
    }

    private void updateDependencies(IIpsPackageFragment targetIpsPackageFragment, String newName) throws CoreException {
        for (IDependency dependency : getDependencies()) {
            if (!isMatching(dependency)) {
                continue;
            }

            List<IDependencyDetail> details = getDependencyToProject().get(dependency)
                    .findIpsObject(dependency.getSource()).getDependencyDetails(dependency);
            for (IDependencyDetail detail : details) {
                IIpsObjectPartContainer part = detail.getPart();
                if (part == null || detail.getPropertyName() == null) {
                    continue;
                }

                PropertyDescriptor property = BeanUtil.getPropertyDescriptor(part.getClass(), detail.getPropertyName());
                try {
                    String newQualifiedName = buildQualifiedName(targetIpsPackageFragment, newName);
                    property.getWriteMethod().invoke(part, newQualifiedName);
                } catch (IllegalAccessException e) {
                    throw new CoreException(new IpsStatus(e));
                } catch (IllegalArgumentException e) {
                    throw new CoreException(new IpsStatus(e));
                } catch (InvocationTargetException e) {
                    throw new CoreException(new IpsStatus(e));
                }
            }
        }
    }

    private String buildQualifiedName(IIpsPackageFragment ipsPackageFragment, String name) {
        return ipsPackageFragment.isDefaultPackage() ? name : ipsPackageFragment.getName() + "." + name; //$NON-NLS-1$
    }

    private IIpsSrcFile copySourceFileToTargetFile(IIpsPackageFragment targetIpsPackageFragment,
            String newName,
            IProgressMonitor pm) throws CoreException {

        IIpsSrcFile originalSrcFile = toBeRefactored.getIpsSrcFile();
        IIpsSrcFile targetSrcFile = null;
        if (targetIpsPackageFragment.equals(originalSrcFile.getIpsPackageFragment())
                && isOnlyCapitalizationChanged(originalSrcFile, newName)) {
            IIpsSrcFile tempSrcFile = RefactorUtil.copyIpsSrcFileToTemporary(originalSrcFile, targetIpsPackageFragment,
                    newName, pm);
            deleteIpsSourceFile(originalSrcFile, pm);
            targetSrcFile = RefactorUtil.copyIpsSrcFile(tempSrcFile, targetIpsPackageFragment, newName, pm);
            deleteIpsSourceFile(tempSrcFile, pm);
        } else {
        	targetSrcFile = RefactorUtil.copyIpsSrcFile(originalSrcFile, targetIpsPackageFragment, newName, pm);
            deleteIpsSourceFile(originalSrcFile, pm);
        }
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

    private void deleteIpsSourceFile(IIpsSrcFile ipsSrcFile, IProgressMonitor pm) throws CoreException {
        ipsSrcFile.getCorrespondingResource().delete(true, pm);
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
        DependencyGraph graph = new DependencyGraph(project);
        for (IDependency dependency : graph.getDependants(toBeRefactored.getQualifiedNameType())) {
            dependencies.add(dependency);
            dependencyToProject.put(dependency, project);
        }
    }

}
