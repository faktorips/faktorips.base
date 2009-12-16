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

package org.faktorips.devtools.core.refactor;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.ParticipantManager;
import org.eclipse.ltk.core.refactoring.participants.ProcessorBasedRefactoring;
import org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant;
import org.eclipse.ltk.core.refactoring.participants.RenameArguments;
import org.eclipse.ltk.core.refactoring.participants.RenameProcessor;
import org.eclipse.ltk.core.refactoring.participants.SharableParticipants;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.util.ArgumentCheck;

/**
 * This is the abstract base class for all Faktor-IPS rename refactorings.
 * <p>
 * Subclasses must use the method <tt>addModifiedSrcFile(IIpsSrcFile)</tt> in order to register all
 * modified <tt>IIpsSrcFile</tt>s. All registered modified <tt>IIpsSrcFile</tt>s will be saved at
 * the end of the refactoring.
 * 
 * @see ProcessorBasedRefactoring
 * 
 * @author Alexander Weickmann
 */
public abstract class RenameRefactoringProcessor extends RenameProcessor {

    /** The <tt>IIpsElement</tt> to be refactored. */
    private final IIpsElement ipsElement;

    /** Set containing all <tt>IIpsSrcFile</tt>s that have been modified by the refactoring. */
    private final Set<IIpsSrcFile> modifiedSrcFiles;

    /** The original name of the <tt>IIpsElement</tt> to be refactored. */
    private final String originalElementName;

    /** The new name for the <tt>IIpsElement</tt>, provided by the user. */
    private String newElementName;

    /**
     * Creates a <tt>RenameRefactoringProcessor</tt>.
     * 
     * @param ipsElement The <tt>IIpsElement</tt> to be refactored.
     * 
     * @throws NullPointerException If <tt>ipsElement</tt> is <tt>null</tt>.
     */
    protected RenameRefactoringProcessor(IIpsElement ipsElement) {
        super();
        ArgumentCheck.notNull(ipsElement);
        this.ipsElement = ipsElement;
        newElementName = "";
        originalElementName = ipsElement.getName();
        modifiedSrcFiles = new HashSet<IIpsSrcFile>();
    }

    @Override
    public RefactoringStatus checkFinalConditions(IProgressMonitor pm, CheckConditionsContext context)
            throws CoreException, OperationCanceledException {

        // TODO AW: check final condition valid IPS element in subclasses.
        return new RefactoringStatus();
    }

    @Override
    public RefactoringStatus checkInitialConditions(IProgressMonitor pm) throws CoreException,
            OperationCanceledException {

        RefactoringStatus status = new RefactoringStatus();
        if (!(ipsElement.exists())) {
            status.addFatalError(NLS.bind(Messages.RenameRefactoringProcessor_errorIpsElementDoesNotExist, ipsElement
                    .getName()));
        }

        return status;
    }

    @Override
    public final Change createChange(IProgressMonitor pm) throws CoreException, OperationCanceledException {
        return null;
    }

    @Override
    public final Change postCreateChange(Change[] participantChanges, IProgressMonitor pm) throws CoreException,
            OperationCanceledException {

        refactorModel(pm);
        saveModifiedSourceFiles(pm);
        return null;
    }

    @Override
    public final Object[] getElements() {
        return new Object[] { ipsElement };
    }

    @Override
    public final RefactoringParticipant[] loadParticipants(RefactoringStatus status,
            SharableParticipants sharedParticipants) throws CoreException {

        return ParticipantManager.loadRenameParticipants(status, this, ipsElement, new RenameArguments(newElementName,
                true), new String[] { IIpsProject.NATURE_ID }, sharedParticipants);
    }

    @Override
    public boolean isApplicable() throws CoreException {
        return true;
    }

    /** Saves all modified <tt>IIpsSrcFile</tt>s. */
    private void saveModifiedSourceFiles(IProgressMonitor pm) throws CoreException {
        for (IIpsSrcFile ipsSrcFile : modifiedSrcFiles) {
            ipsSrcFile.save(true, pm);
        }
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
        IIpsProject[] ipsProjects = getIpsProject().getReferencingProjectLeavesOrSelf();
        for (IIpsProject ipsProject : ipsProjects) {
            IIpsSrcFile[] srcFiles = ipsProject.findIpsSrcFiles(ipsObjectType);
            for (IIpsSrcFile ipsSrcFile : srcFiles) {
                collectedSrcFiles.add(ipsSrcFile);
            }
        }
        return collectedSrcFiles;
    }

    /**
     * Sets the new name for the <tt>IIpsElement</tt> to be refactored.
     * 
     * @param newElementName The new name for the <tt>IIpsElement</tt> to be refactored.
     * 
     * @throws NullPointerException If <tt>newElementName</tt> is <tt>null</tt>.
     */
    public final void setNewElementName(String newElementName) {
        ArgumentCheck.notNull(newElementName);
        this.newElementName = newElementName;
    }

    /** Returns the <tt>IIpsElement</tt> to be refactored. */
    protected final IIpsElement getIpsElement() {
        return ipsElement;
    }

    /** Returns the new name for the <tt>IIpsElement</tt> to be refactored. */
    public final String getNewElementName() {
        return newElementName;
    }

    /** Returns the original name of the <tt>IIpsElement</tt> to be refactored. */
    public final String getOriginalElementName() {
        return originalElementName;
    }

    /**
     * Subclass implementation that is responsible for performing the necessary changes in the
     * Faktor-IPS model.
     * 
     * @param pm Progress monitor to report progress to if necessary.
     * 
     * @throws CoreException Subclasses may throw this kind of exception any time.
     */
    protected abstract void refactorModel(IProgressMonitor pm) throws CoreException;

    /**
     * Registers the given <tt>IIpsSrcFile</tt> as modified source file so it saved at the end of
     * the refactoring.
     * <p>
     * If the provided <tt>IIpsSrcFile</tt> is already registered as modified source file, nothing
     * will happen.
     * 
     * @param ipsSrcFile The <tt>IIpsSrcFile</tt> to register as modified.
     * 
     * @throws NullPointerException If <tt>ipsSrcFile</tt> is <tt>null</tt>.
     */
    protected final void addModifiedSrcFile(IIpsSrcFile ipsSrcFile) {
        ArgumentCheck.notNull(ipsSrcFile);
        modifiedSrcFiles.add(ipsSrcFile);
    }

    /** Returns the <tt>IIpsProject</tt> the <tt>IIpsElement</tt> to be refactored belongs to. */
    protected final IIpsProject getIpsProject() {
        return ipsElement.getIpsProject();
    }

}
