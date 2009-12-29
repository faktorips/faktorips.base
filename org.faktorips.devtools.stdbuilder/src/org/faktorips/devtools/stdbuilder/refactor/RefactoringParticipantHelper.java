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

package org.faktorips.devtools.stdbuilder.refactor;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.refactoring.IJavaRefactorings;
import org.eclipse.jdt.core.refactoring.descriptors.RenameJavaElementDescriptor;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CheckConditionsOperation;
import org.eclipse.ltk.core.refactoring.PerformRefactoringOperation;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringContribution;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsSrcFile;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptType;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.refactor.LocationDescriptor;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.util.ArgumentCheck;

/**
 * Because all refactoring participants must be derived from an abstract LTK base class we can't use
 * inheritance ourselves to provide common functionality for the standard builder refactoring
 * participants.
 * <p>
 * Instead of using inheritance every participant may hold a <tt>RefactoringParticipantHelper</tt>.
 * 
 * @author Alexander Weickmann
 */
public abstract class RefactoringParticipantHelper {

    /**
     * List containing the <tt>IJavaElement</tt>s generated for the <tt>IIpsElement</tt> to be
     * refactored.
     */
    private List<IJavaElement> generatedJavaElements;

    /**
     * List containing the <tt>IJavaElement</tt>s generated for the <tt>IIpsElement</tt> to be
     * refactored as they will be after the refactoring. This information is needed to be able to
     * provide the JDT refactorings with the correct new names for the <tt>IJavaElement</tt>s.
     */
    private List<IJavaElement> newJavaElements;

    /**
     * Checks for errors in <tt>ICompilationUnit</tt>s of the generated <tt>IJavaElement</tt>s.
     * 
     * @see RefactoringParticipant#checkConditions(IProgressMonitor, CheckConditionsContext)
     */
    public final RefactoringStatus checkConditions(IProgressMonitor pm, CheckConditionsContext context)
            throws OperationCanceledException {

        // List to remember broken compilation units.
        List<ICompilationUnit> invalidCompilationUnits = new LinkedList<ICompilationUnit>();

        RefactoringStatus status = new RefactoringStatus();
        for (IJavaElement javaElement : generatedJavaElements) {
            IType type = null;
            if (javaElement instanceof IType) {
                type = (IType)javaElement;
            } else if (javaElement instanceof IField) {
                IField field = (IField)javaElement;
                type = (IType)field.getParent();
            } else if (javaElement instanceof IMethod) {
                IMethod method = (IMethod)javaElement;
                type = (IType)method.getParent();
            }

            // Only report each broken compilation unit once.
            if (invalidCompilationUnits.contains(type.getCompilationUnit())) {
                continue;
            }

            try {
                if (type.getCompilationUnit().exists()) {
                    if (!(type.getCompilationUnit().isStructureKnown())) {
                        invalidCompilationUnits.add(type.getCompilationUnit());
                        status.addFatalError(NLS.bind(Messages.RefactoringParticipantHelper_errorSyntaxErrors, type
                                .getCompilationUnit().getElementName()));
                    }
                }
            } catch (JavaModelException e) {
                throw new RuntimeException(e);
            }
        }

        return status;
    }

    /**
     * Iterates over all generated <tt>IJavaElement</tt>s and calls the subclass implementation that
     * processes it if it exists.
     * 
     * @see RefactoringParticipant#createChange(IProgressMonitor)
     */
    public final Change createChange(IProgressMonitor pm) throws CoreException, OperationCanceledException {
        for (int i = 0; i < generatedJavaElements.size(); i++) {
            IJavaElement javaElement = generatedJavaElements.get(i);

            /*
             * Do not try to refactor non-existing Java elements as the user may want to try to
             * start the refactoring when there is no source code at all. This also solves the
             * problem of what should happen when there is a Java element that occurs in the
             * implementation as well as in the published interface. If for example a setter method
             * occurs in the implementation as well as in the published interface then the first
             * encountered will be refactored. The second no longer exists then because the JDT
             * rename method refactoring renamed it already.
             */
            if (!(javaElement.exists())) {
                continue;
            }

            createChangeThis(javaElement, newJavaElements.get(i), pm);
        }

        return null;
    }

    /**
     * Subclass implementation responsible for change processing the given <tt>IJavaElement</tt>.
     * 
     * @param originalJavaElement The <tt>IJavaElement</tt> to be refactored.
     * @param newJavaElement The <tt>IJavaElement</tt> as it should be after the refactoring.
     * @param pm An <tt>IProgressMonitor</tt> to report progress to.
     */
    protected abstract void createChangeThis(IJavaElement originalJavaElement,
            IJavaElement newJavaElement,
            IProgressMonitor pm) throws CoreException, OperationCanceledException;

    /**
     * Renames the given <tt>IJavaElement</tt> to the given new name by calling the appropriate JDT
     * refactoring.
     * 
     * @param javaElement The <tt>IJavaElement</tt> to rename.
     * @param newName A new name for the <tt>IJavaElement</tt>.
     * @param updateReferences Flag whether to update Java references.
     * @param pm An <tt>IProgressMonitor</tt> to report progress to.
     * 
     * @throws NullPointerException If any parameter is <tt>null</tt>.
     */
    public final void renameJavaElement(IJavaElement javaElement,
            String newName,
            boolean updateReferences,
            final IProgressMonitor pm) throws OperationCanceledException, CoreException {

        ArgumentCheck.notNull(new Object[] { javaElement, newName, pm });

        String javaRefactoringContributionId;
        switch (javaElement.getElementType()) {
            case IJavaElement.FIELD:
                javaRefactoringContributionId = IJavaRefactorings.RENAME_FIELD;
                break;
            case IJavaElement.METHOD:
                javaRefactoringContributionId = IJavaRefactorings.RENAME_METHOD;
                break;
            case IJavaElement.TYPE:
                javaRefactoringContributionId = IJavaRefactorings.RENAME_TYPE;
                break;
            default:
                throw new RuntimeException("This kind of Java element is not supported.");
        }

        RefactoringContribution contribution = RefactoringCore
                .getRefactoringContribution(javaRefactoringContributionId);
        RenameJavaElementDescriptor descriptor = (RenameJavaElementDescriptor)contribution.createDescriptor();
        descriptor.setJavaElement(javaElement);
        descriptor.setNewName(newName);
        descriptor.setUpdateReferences(updateReferences);

        performRefactoring(descriptor, pm);
    }

    /**
     * Executes the refactoring described by the provided <tt>RefactoringDescriptor</tt>.
     * 
     * @param refactoringDescriptor The <tt>RefactoringDescriptor</tt> describing the refactoring to
     *            be performed.
     * @param pm An <tt>IProgressMonitor</tt> to report progress to.
     * 
     * @throws NullPointerException If any parameter is <tt>null</tt>.
     */
    public final void performRefactoring(RefactoringDescriptor refactoringDescriptor, final IProgressMonitor pm)
            throws CoreException {

        ArgumentCheck.notNull(refactoringDescriptor, pm);

        RefactoringStatus status = new RefactoringStatus();
        Refactoring refactoring = refactoringDescriptor.createRefactoring(status);
        if (status.isOK()) {
            final PerformRefactoringOperation operation = new PerformRefactoringOperation(refactoring,
                    CheckConditionsOperation.ALL_CONDITIONS);
            Display display = (Display.getCurrent() != null) ? Display.getCurrent() : Display.getDefault();
            display.syncExec(new Runnable() {
                public void run() {
                    try {
                        ResourcesPlugin.getWorkspace().run(operation, pm);
                    } catch (CoreException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }
    }

    /**
     * This implementation initializes the list of generated <tt>IJavaElement</tt>s for the provided
     * <tt>IIpsElement</tt>.
     * <p>
     * Returns <tt>false</tt> in case the element passed to this operation is not an
     * <tt>IIpsElement</tt>. Else the subclass implementation is called to initialize the
     * <tt>IJavaElement</tt>s that will be generated for the <tt>IIpsElement</tt> after the
     * refactoring has finished and the result is returned.
     * 
     * @see RefactoringParticipant#initialize(Object)
     */
    public final boolean initialize(Object element) {
        if (!(element instanceof IIpsElement)) {
            return false;
        }

        IIpsElement ipsElement = (IIpsElement)element;
        StandardBuilderSet builderSet = (StandardBuilderSet)ipsElement.getIpsProject().getIpsArtefactBuilderSet();
        generatedJavaElements = builderSet.getGeneratedJavaElements(ipsElement);

        return initializeNewJavaElements(ipsElement, builderSet);
    }

    /**
     * Subclass implementation responsible for initializing the <tt>IJavaElement</tt>s that will be
     * generated for the <tt>IIpsElement</tt> after the refactoring has finished.
     * 
     * @param ipsElement The <tt>IIpsElement</tt> to be refactored.
     * @param builderSet A reference to the <tt>StandardBuilderSet</tt> to ask for generated Java
     *            elements.
     */
    protected abstract boolean initializeNewJavaElements(IIpsElement ipsElement, StandardBuilderSet builderSet);

    /**
     * Initializes the list of the <tt>IJavaElement</tt>s generated for the <tt>IPolicyCmptType</tt>
     * after it has been refactored.
     * 
     * @param policyCmptType The <tt>IPolicyCmptType</tt> to be refactored.
     * @param targetLocation The target location of the <tt>IPolicyCmptType</tt> to be refactored.
     * @param builderSet A reference to the <tt>StandardBuilderSet</tt> to ask for generated Java
     *            elements.
     * 
     * @throws NullPointerException If any parameter is <tt>null</tt>.
     */
    protected final void initNewJavaElements(IPolicyCmptType policyCmptType,
            LocationDescriptor targetLocation,
            StandardBuilderSet builderSet) {

        ArgumentCheck.notNull(new Object[] { policyCmptType, targetLocation, builderSet });

        /*
         * Creating an in-memory-only source file for an in-memory-only policy component type that
         * can be passed to the builder to obtain the generated Java elements for.
         */
        IIpsSrcFile temporarySrcFile = new IpsSrcFile(targetLocation.getIpsPackageFragment(), targetLocation.getName()
                + "." + IpsObjectType.POLICY_CMPT_TYPE.getFileExtension());
        IPolicyCmptType copiedPolicyCmptType = new PolicyCmptType(temporarySrcFile);

        /*
         * TODO AW: I think this could lead to bugs in the future easily. If other properties are
         * added to policy component type's this code must be updated, too. It is very likely that
         * this won't be done, so this code should be moved. Actually I don't really know where and
         * to put that code.
         */
        copiedPolicyCmptType.setAbstract(policyCmptType.isAbstract());
        copiedPolicyCmptType.setConfigurableByProductCmptType(policyCmptType.isConfigurableByProductCmptType());
        copiedPolicyCmptType.setProductCmptType(policyCmptType.getProductCmptType());
        copiedPolicyCmptType.setSupertype(policyCmptType.getSupertype());

        newJavaElements = builderSet.getGeneratedJavaElements(copiedPolicyCmptType);
    }

    /**
     * Initializes the list of the <tt>IJavaElement</tt>s generated for the
     * <tt>IProductCmptType</tt> after it has been refactored.
     * 
     * @param productCmptType The <tt>IProductCmptType</tt> to be refactored.
     * @param targetLocation The target location of the <tt>IProductCmptType</tt> to be refactored.
     * @param builderSet A reference to the <tt>StandardBuilderSet</tt> to ask for generated Java
     *            elements.
     * 
     * @throws NullPointerException If any parameter is <tt>null</tt>.
     */
    protected final void initNewJavaElements(IProductCmptType productCmptType,
            LocationDescriptor targetLocation,
            StandardBuilderSet builderSet) {

        ArgumentCheck.notNull(new Object[] { productCmptType, targetLocation, builderSet });

        /*
         * Creating an in-memory-only source file for an in-memory-only product component type that
         * can be passed to the builder to obtain the generated Java elements for.
         */
        IIpsSrcFile temporarySrcFile = new IpsSrcFile(targetLocation.getIpsPackageFragment(), targetLocation.getName()
                + "." + IpsObjectType.PRODUCT_CMPT_TYPE.getFileExtension());
        IProductCmptType copiedProductCmptType = new ProductCmptType(temporarySrcFile);

        /*
         * TODO AW: I think this could lead to bugs in the future easily. If other properties are
         * added to product component type's this code must be updated, too. It is very likely that
         * this won't be done, so this code should be moved. Actually I don't really know where and
         * to put that code.
         */
        copiedProductCmptType.setAbstract(productCmptType.isAbstract());
        copiedProductCmptType.setConfigurationForPolicyCmptType(productCmptType.isConfigurationForPolicyCmptType());
        copiedProductCmptType.setPolicyCmptType(productCmptType.getPolicyCmptType());
        copiedProductCmptType.setSupertype(productCmptType.getSupertype());

        newJavaElements = builderSet.getGeneratedJavaElements(copiedProductCmptType);
    }

    /**
     * Provides access to the list of <tt>IJavaElement</tt>s that will be generated for the
     * <tt>IIpsElement</tt> after the refactoring has finished.
     */
    protected final List<IJavaElement> getNewJavaElements() {
        return newJavaElements;
    }

    /**
     * Allows subclasses to set the list of <tt>IJavaElement</tt>s that will be generated for the
     * <tt>IIpsElement</tt> after the refactoring has finished.
     * 
     * @param newJavaElements The list of <tt>IJavaElement</tt>s.
     * 
     * @throws NullPointerException If <tt>newJavaElements</tt> is <tt>null</tt>.
     */
    protected final void setNewJavaElements(List<IJavaElement> newJavaElements) {
        ArgumentCheck.notNull(newJavaElements);
        this.newJavaElements = newJavaElements;
    }

    /**
     * Provides access to the list <tt>IJavaElement</tt> generated for the provided
     * <tt>IIpsElement</tt>.
     */
    protected final List<IJavaElement> getGeneratedJavaElements() {
        return generatedJavaElements;
    }

}
