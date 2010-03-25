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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.builder.DependencyGraph;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsObject;
import org.faktorips.devtools.core.internal.refactor.IpsMoveProcessor;
import org.faktorips.devtools.core.internal.refactor.IpsRefactoringProcessor;
import org.faktorips.devtools.core.internal.refactor.IpsRenameProcessor;
import org.faktorips.devtools.core.model.IDependency;
import org.faktorips.devtools.core.model.IDependencyDetail;
import org.faktorips.devtools.core.model.enums.IEnumContent;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.util.BeanUtil;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.MessageList;

/**
 * The <tt>MoveRenameIpsObjectHelper</tt> bundles common functionality of the "Rename IPS Object"
 * and "Move IPS Object" refactorings.
 * 
 * @author Alexander Weickmann
 */
public final class MoveRenameIpsObjectHelper {

    /** New <tt>IIpsSrcFile</tt> containing a copy of the <tt>IIpsObject</tt> to be refactored. */
    private IIpsSrcFile copiedIpsSrcFile;

    /** The <tt>IIpsObject</tt> to be refactored. */
    private final IpsObject toBeRefactored;

    /** The <tt>IpsRefactoringProcessor</tt> that uses this helper. */
    private final IpsRefactoringProcessor refactoringProcessor;

    private IDependency[] dependencies;

    private Map<IDependency, IIpsProject> dependencyToProject;

    /**
     * @param refactoringProcessor The <tt>IpsRefactoringProcessor</tt> that uses this helper.
     * @param toBeRefactored The <tt>IIpsObject</tt> to be refactored.
     * 
     * @throws NullPointerException If any parameter is <tt>null</tt>.
     */
    public MoveRenameIpsObjectHelper(IpsRefactoringProcessor refactoringProcessor, IpsObject toBeRefactored) {
        ArgumentCheck.notNull(new Object[] { refactoringProcessor, toBeRefactored });
        this.refactoringProcessor = refactoringProcessor;
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
        ignoredValidationMessageCodes.add(IProductCmptType.MSGCODE_POLICY_CMPT_TYPE_DOES_NOT_SPECIFY_THIS_TYPE);
        ignoredValidationMessageCodes.add(IIpsProject.MSGCODE_RUNTIME_ID_COLLISION);
        ignoredValidationMessageCodes.add(IEnumContent.MSGCODE_ENUM_CONTENT_NAME_NOT_CORRECT);
    }

    /**
     * Returns a list containing the <tt>IIpsSrcFile</tt>s that are affected by the refactoring.
     * 
     * @see IpsRefactoringProcessor#addIpsSrcFiles()
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
     * 
     * @see IpsRenameProcessor#validateUserInputThis(RefactoringStatus, IProgressMonitor)
     * @see IpsMoveProcessor#validateUserInputThis(RefactoringStatus, IProgressMonitor)
     */
    public void validateUserInputThis(IIpsPackageFragment targetIpsPackageFragment,
            String newName,
            RefactoringStatus status,
            IProgressMonitor pm) throws CoreException {

        for (IIpsSrcFile ipsSrcFile : targetIpsPackageFragment.getIpsSrcFiles()) {
            String sourceFileName = ipsSrcFile.getName();
            String relevantNamePart = sourceFileName.substring(0, sourceFileName.lastIndexOf('.'));
            if (relevantNamePart.equals(newName)) {
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
     * 
     * @see IpsRefactoringProcessor#checkFinalConditionsThis(RefactoringStatus, IProgressMonitor,
     *      CheckConditionsContext)
     */
    public MessageList checkFinalConditionsThis(IIpsPackageFragment targetIpsPackageFragment,
            String newName,
            RefactoringStatus status,
            IProgressMonitor pm,
            CheckConditionsContext context) throws CoreException {

        try {
            copySourceFileToTargetLocation(targetIpsPackageFragment, newName, pm);
        } catch (CoreException e) {
            status.addFatalError(e.getLocalizedMessage());
            return new MessageList();
        }

        IpsObject copy = (IpsObject)copiedIpsSrcFile.getIpsObject();

        MessageList validationMessageList = copy.validate(copy.getIpsProject());

        // Delete the copy again in every case because participants condition checking may fail.
        copiedIpsSrcFile.getCorrespondingResource().delete(true, pm);
        copiedIpsSrcFile = null;

        return validationMessageList;
    }

    /**
     * Copies the <tt>IIpsObject</tt> to be refactored into a new source file with the desired new
     * name inside the desired destination package.
     */
    private void copySourceFileToTargetLocation(IIpsPackageFragment targetIpsPackageFragment,
            String newName,
            IProgressMonitor pm) throws CoreException {

        IPath destinationFolder = targetIpsPackageFragment.getCorrespondingResource().getFullPath();

        String targetSrcFileName = newName + "." + toBeRefactored.getIpsObjectType().getFileExtension();
        IPath destinationPath = destinationFolder.append(targetSrcFileName);

        toBeRefactored.getIpsSrcFile().getCorrespondingResource().copy(destinationPath, true, pm);
        copiedIpsSrcFile = targetIpsPackageFragment.getIpsSrcFile(targetSrcFileName);
    }

    public void refactorIpsModel(IIpsPackageFragment targetIpsPackageFragment, String newName, IProgressMonitor pm)
            throws CoreException {

        copySourceFileToTargetLocation(targetIpsPackageFragment, newName, pm);

        String newQName = getNewQualifiedName(targetIpsPackageFragment, newName);

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
                    property.getWriteMethod().invoke(part, newQName);
                } catch (Exception e) {
                    throw new CoreException(new IpsStatus(e));
                }
            }
        }

        deleteOldIpsSourceFile(pm);
    }

    private boolean isMatching(IDependency dependency) throws CoreException {
        Object target = dependency.getTarget();

        if (target instanceof QualifiedNameType) {
            return (toBeRefactored.getQualifiedNameType().equals(target));
        } else if (target instanceof String) {
            return toBeRefactored.getQualifiedName().equals(target);
        } else {
            throw new CoreException(new IpsStatus("The type of the dependency-target (" + target + ") is unknown."));
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

    /** Deletes the original IPS source file that is now no longer needed. */
    private void deleteOldIpsSourceFile(IProgressMonitor pm) throws CoreException {
        toBeRefactored.getIpsSrcFile().getCorrespondingResource().delete(true, pm);
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
        return ipsPackageFragment.isDefaultPackage() ? name : ipsPackageFragment.getName() + "." + name;
    }

}
