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
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.PerformChangeOperation;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.ProcessorBasedRefactoring;
import org.eclipse.ltk.core.refactoring.participants.RefactoringProcessor;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;

/**
 * This is the abstract base class for all Faktor-IPS refactorings.
 * <p>
 * Subclasses must use the method <tt>addModifiedSrcFile(IIpsSrcFile)</tt> in order to register all
 * modified <tt>IIpsSrcFile</tt>s. All registered modified <tt>IIpsSrcFile</tt>s will be saved at
 * the end of the refactoring.
 * <p>
 * The method <tt>addValidationMessagesToStatus(MessageList, RefactoringStatus)</tt> can be used to
 * transfer the common Faktor-IPS validation messages to a <tt>RefactoringStatus</tt> used by the
 * refactoring API. If certain validation message codes shall be ignored during this process
 * subclasses may access a set containing ignored message codes.
 * 
 * @see ProcessorBasedRefactoring
 * 
 * @author Alexander Weickmann
 */
public abstract class IpsRefactoringProcessor extends RefactoringProcessor {

    /** The <tt>IIpsElement</tt> to be refactored. */
    private final IIpsElement ipsElement;

    /** Set containing all <tt>IIpsSrcFile</tt>s that have been modified by the refactoring. */
    private final Set<IIpsSrcFile> modifiedSrcFiles;

    /** A set containing all message codes that will be ignored during final condition checking. */
    private final Set<String> ignoredValidationMessageCodes;

    /**
     * Creates a <tt>IpsRefactoringProcessor</tt>.
     * 
     * @param ipsElement The <tt>IIpsElement</tt> to be refactored.
     * 
     * @throws NullPointerException If <tt>ipsElement</tt> is <tt>null</tt>.
     */
    protected IpsRefactoringProcessor(IIpsElement ipsElement) {
        super();
        this.ipsElement = ipsElement;
        modifiedSrcFiles = new HashSet<IIpsSrcFile>();
        ignoredValidationMessageCodes = new HashSet<String>();
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation checks that the <tt>IIpsElement</tt> to be renamed exists and calls the
     * subclass implementation.
     */
    @Override
    public final RefactoringStatus checkInitialConditions(IProgressMonitor pm) throws CoreException,
            OperationCanceledException {

        RefactoringStatus status = new RefactoringStatus();
        if (!(ipsElement.exists())) {
            status.addFatalError(NLS.bind(Messages.IpsRefactoringProcessor_errorIpsElementDoesNotExist, ipsElement
                    .getName()));
        }
        checkInitialConditionsThis(status, pm);
        return status;
    }

    /**
     * Subclass implementation for initial condition checking that is performed in addition to the
     * default initial condition checking which validates the existence of the <tt>IIpsElement</tt>
     * to be refactored.
     * 
     * @param status The <tt>RefactoringStatus</tt> to add messages to.
     * @param pm An <tt>IProgressMonitor</tt> to report progress to.
     * 
     * @throws CoreException May be thrown at any time.
     */
    protected abstract void checkInitialConditionsThis(RefactoringStatus status, IProgressMonitor pm)
            throws CoreException;

    /**
     * Adds an entry to the provided <tt>RefactoringStatus</tt> for every messages contained in the
     * provided <tt>MessageList</tt>.
     * <p>
     * If validation messages with certain message codes shall not be added to the
     * <tt>RefactoringStatus</tt> these message codes must be added to the set of ignored message
     * codes.
     * 
     * @see #getIgnoredValidationMessageCodes()
     * 
     * @param validationMessageList The <tt>MessageList</tt> from that messages shall be added to
     *            the <tt>RefactoringStatus</tt>.
     * @param status The <tt>RefactoringStatus</tt> to add entries to.
     * 
     * @throws NullPointerException If any parameter is <tt>null</tt>.
     */
    protected final void addValidationMessagesToStatus(MessageList validationMessageList, RefactoringStatus status) {
        ArgumentCheck.notNull(new Object[] { validationMessageList, status });

        for (Message message : validationMessageList) {
            // Check if message code is ignored.
            boolean ignoreThisMessage = false;
            for (String messageCode : ignoredValidationMessageCodes) {
                if (messageCode.equals(message.getCode())) {
                    ignoreThisMessage = true;
                    break;
                }
            }

            if (!(ignoreThisMessage)) {
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
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation does nothing and returns <tt>null</tt>, may be overwritten by subclasses
     * if any changes need to be done before any refactoring participants are called.
     */
    @Override
    public Change createChange(IProgressMonitor pm) throws CoreException, OperationCanceledException {
        return null;
    }

    @Override
    public final Change postCreateChange(Change[] participantChanges, IProgressMonitor pm) throws CoreException,
            OperationCanceledException {

        Change change = refactorIpsModel(pm);
        if (change != null) {
            PerformChangeOperation op = new PerformChangeOperation(change);
            op.run(new NullProgressMonitor());
        }

        saveModifiedSourceFiles(pm);

        return null;
    }

    /**
     * Subclass implementation that is responsible for performing the necessary changes in the
     * Faktor-IPS model.
     * <p>
     * This method may return a <tt>Change</tt> object that will be executed right before all
     * modified source files will be saved. May also return <tt>null</tt>.
     * <p>
     * Note that anything implemented in this operation will be performed after processing all
     * refactoring participants, e.g. source code refactoring. If any changes must be performed
     * before refactoring participants are called it must be done by overwriting
     * <tt>createChange(IProgressMonitor)</tt>.
     * 
     * @see #createChange(IProgressMonitor)
     * 
     * @param pm Progress monitor to report progress to if necessary.
     * 
     * @throws CoreException Subclasses may throw this kind of exception any time.
     */
    // TODO AW: Subclasses need the ability to search for references, we need a reference search.
    protected abstract Change refactorIpsModel(IProgressMonitor pm) throws CoreException;

    /** Saves all modified <tt>IIpsSrcFile</tt>s. */
    private void saveModifiedSourceFiles(IProgressMonitor pm) throws CoreException {
        for (IIpsSrcFile ipsSrcFile : modifiedSrcFiles) {
            ipsSrcFile.save(true, pm);
        }
    }

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

    /**
     * Returns the set containing all validation message codes that will be ignored during final
     * condition checking.
     */
    protected final Set<String> getIgnoredValidationMessageCodes() {
        return ignoredValidationMessageCodes;
    }

    @Override
    public final Object[] getElements() {
        return new Object[] { ipsElement };
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation always returns <tt>true</tt>, may be overwritten by subclasses if
     * necessary.
     */
    @Override
    public boolean isApplicable() throws CoreException {
        return true;
    }

    /** Returns the <tt>IIpsElement</tt> to be refactored. */
    protected final IIpsElement getIpsElement() {
        return ipsElement;
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

}
