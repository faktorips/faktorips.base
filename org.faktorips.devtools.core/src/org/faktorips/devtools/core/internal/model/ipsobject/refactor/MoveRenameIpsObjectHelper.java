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
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.builder.DependencyGraph;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsObject;
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
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.util.BeanUtil;
import org.faktorips.devtools.core.util.RefactorUtil;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.MessageList;

/**
 * The <tt>MoveRenameIpsObjectHelper</tt> bundles common functionality of the "Rename IPS Object"
 * and "Move IPS Object" refactorings.
 * 
 * @author Alexander Weickmann
 */
public final class MoveRenameIpsObjectHelper {

    /** The <tt>IIpsObject</tt> to be refactored. */
    private final IpsObject toBeRefactored;

    private IDependency[] dependencies;

    private Map<IDependency, IIpsProject> dependencyToProject;

    /**
     * @param toBeRefactored The <tt>IIpsObject</tt> to be refactored.
     * 
     * @throws NullPointerException If <tt>toBeRefactored</tt> is <tt>null</tt>.
     */
    public MoveRenameIpsObjectHelper(IpsObject toBeRefactored) {
        ArgumentCheck.notNull(new Object[] { toBeRefactored });
        this.toBeRefactored = toBeRefactored;
    }

    /**
     * Adds message codes to the set of ignored validation message codes that must be ignored by the
     * "Rename Type" and "Move Type" refactorings.
     * <p>
     * For example: The configuring <tt>IProductCmptType</tt> / configured <tt>IPolicyCmptType</tt>
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
     * Returns a list containing the <tt>IIpsSrcFile</tt>s that are affected by the refactoring.
     */
    public List<IIpsSrcFile> addIpsSrcFiles() throws CoreException {
        IDependency[] dependencies = getDependencies();
        List<IIpsSrcFile> ipsSrcFiles = new ArrayList<IIpsSrcFile>(dependencies.length);
        for (IDependency dependency : dependencies) {
            IIpsSrcFile ipsSrcFile = dependencyToProject.get(dependency).findIpsSrcFile(dependency.getSource());
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
     * The source file of the <tt>IIpsObject</tt> will be copied this early. Based on that new
     * source file and on the copied <tt>IIpsObject</tt> validation is performed. If the validation
     * fails the copy will be deleted so everything is left in a clean state (as it was before). If
     * only a warning or information validation message results the copy must also be deleted and
     * recreated later if the user really starts the refactoring.
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
            /*
             * 1) Copy original source file to temporary file with time stamp to avoid file system
             * problems if only the cases of the letters changed.
             */
            IIpsSrcFile fileToBeCopied = toBeRefactored.getIpsSrcFile();
            IIpsSrcFile tempSrcFile = RefactorUtil.copyIpsSrcFileToTemporary(fileToBeCopied, targetIpsPackageFragment,
                    newName, pm);

            // 2) Delete original source file using refactoring to be able to undo later.
            IPath resourcePath = fileToBeCopied.getCorrespondingResource().getFullPath();
            DeleteResourceChange deleteResourceChange = new DeleteResourceChange(resourcePath, true);
            undoDeleteChange = deleteResourceChange.perform(pm);

            // 3) Copy temporary file to target file and delete temporary file.
            IIpsSrcFile targetFile = RefactorUtil.copyIpsSrcFile(tempSrcFile, targetIpsPackageFragment, newName, pm);
            deleteIpsSourceFile(tempSrcFile, pm);

            // 5) Perform validation on target file.
            IIpsObject copiedIpsObject = targetFile.getIpsObject();
            MessageList validationMessageList = copiedIpsObject.validate(copiedIpsObject.getIpsProject());

            /*
             * 6) Delete target file (can't leave it here already because participants condition
             * checking may fail which would abort the refactoring completely and we don't want to
             * have the copy around in this case).
             */
            deleteIpsSourceFile(targetFile, pm);

            return validationMessageList;

        } catch (CoreException e) {
            status.addFatalError(e.getLocalizedMessage());
            return new MessageList();

        } finally {
            // 7) Roll-back original source file by undo delete refactoring.
            undoDeleteChange.perform(pm);
        }
    }

    public void refactorIpsModel(IIpsPackageFragment targetIpsPackageFragment, String newName, IProgressMonitor pm)
            throws CoreException {

        // Update dependencies.
        for (IDependency dependency : getDependencies()) {
            if (!isMatching(dependency)) {
                continue;
            }

            List<IDependencyDetail> details = dependencyToProject.get(dependency).findIpsObject(dependency.getSource())
                    .getDependencyDetails(dependency);

            for (IDependencyDetail detail : details) {
                IIpsObjectPartContainer part = detail.getPart();
                String propertyName = detail.getPropertyName();

                if (part == null || propertyName == null) {
                    continue;
                }

                PropertyDescriptor property = BeanUtil.getPropertyDescriptor(part.getClass(), propertyName);
                try {
                    String newQName = getNewQualifiedName(targetIpsPackageFragment, newName);
                    property.getWriteMethod().invoke(part, newQName);
                } catch (IllegalAccessException e) {
                    throw new CoreException(new IpsStatus(e));
                } catch (IllegalArgumentException e) {
                    throw new CoreException(new IpsStatus(e));
                } catch (InvocationTargetException e) {
                    throw new CoreException(new IpsStatus(e));
                }
            }
        }

        // Copy the original file to a temporary file and from there to the target file.
        IIpsSrcFile originalSrcFile = toBeRefactored.getIpsSrcFile();
        IIpsSrcFile tempSrcFile = RefactorUtil.copyIpsSrcFileToTemporary(originalSrcFile, targetIpsPackageFragment,
                newName, pm);
        deleteIpsSourceFile(originalSrcFile, pm);
        RefactorUtil.copyIpsSrcFile(tempSrcFile, targetIpsPackageFragment, newName, pm);
        deleteIpsSourceFile(tempSrcFile, pm);
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

    private IDependency[] collectDependcies() throws CoreException {
        dependencyToProject = new HashMap<IDependency, IIpsProject>();
        ArrayList<IDependency> dependencies = new ArrayList<IDependency>();
        IIpsProject[] projects = IpsPlugin.getDefault().getIpsModel().getIpsProjects();

        for (IIpsProject project : projects) {
            DependencyGraph graph = new DependencyGraph(project);
            for (IDependency dependency : graph.getDependants(toBeRefactored.getQualifiedNameType())) {
                dependencies.add(dependency);
                dependencyToProject.put(dependency, project);
            }
        }

        return dependencies.toArray(new IDependency[dependencies.size()]);
    }

    private IDependency[] getDependencies() throws CoreException {
        if (dependencies == null) {
            dependencies = collectDependcies();
        }
        return dependencies;
    }

    private void deleteIpsSourceFile(IIpsSrcFile ipsSrcFile, IProgressMonitor pm) throws CoreException {
        ipsSrcFile.getCorrespondingResource().delete(true, pm);
    }

    /** Returns the <tt>IIpsObject</tt>'s new qualified name. */
    private String getNewQualifiedName(IIpsPackageFragment targetIpsPackageFragment, String newName) {
        return getQualifiedName(targetIpsPackageFragment, newName);
    }

    /**
     * Builds and returns a qualified name from the provided <tt>IIpsPackageFragment</tt> and
     * unqualified name.
     */
    private String getQualifiedName(IIpsPackageFragment ipsPackageFragment, String name) {
        return ipsPackageFragment.isDefaultPackage() ? name : ipsPackageFragment.getName() + "." + name; //$NON-NLS-1$
    }

}
