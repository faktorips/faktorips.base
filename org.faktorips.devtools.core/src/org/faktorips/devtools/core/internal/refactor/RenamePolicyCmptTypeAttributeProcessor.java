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

package org.faktorips.devtools.core.internal.refactor;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.ParticipantManager;
import org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant;
import org.eclipse.ltk.core.refactoring.participants.RenameArguments;
import org.eclipse.ltk.core.refactoring.participants.RenameProcessor;
import org.eclipse.ltk.core.refactoring.participants.SharableParticipants;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.testcasetype.ITestAttribute;
import org.faktorips.devtools.core.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.core.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.faktorips.util.ArgumentCheck;

/**
 * 
 * 
 * @author Alexander Weickmann
 */
// XXX AW: - REFACOTRING SUPPORT PROTOTYPE -
public class RenamePolicyCmptTypeAttributeProcessor extends RenameProcessor {

    private IPolicyCmptTypeAttribute policyCmptTypeAttribute;

    private String newName;

    public RenamePolicyCmptTypeAttributeProcessor(IPolicyCmptTypeAttribute policyCmptTypeAttribute) {
        super();
        ArgumentCheck.notNull(policyCmptTypeAttribute);
        this.policyCmptTypeAttribute = policyCmptTypeAttribute;
        newName = "";
    }

    @Override
    public RefactoringStatus checkFinalConditions(IProgressMonitor pm, CheckConditionsContext context)
            throws CoreException, OperationCanceledException {

        return new RefactoringStatus();
    }

    @Override
    public RefactoringStatus checkInitialConditions(IProgressMonitor pm) throws CoreException,
            OperationCanceledException {

        return new RefactoringStatus();
    }

    @Override
    public Change createChange(IProgressMonitor pm) throws CoreException, OperationCanceledException {
        return null;
    }

    @Override
    public Change postCreateChange(Change[] participantChanges, IProgressMonitor pm) throws CoreException,
            OperationCanceledException {

        String oldAttributeName = policyCmptTypeAttribute.getName();

        // Change the name of the attribute.
        policyCmptTypeAttribute.setName(newName);
        policyCmptTypeAttribute.getIpsSrcFile().save(true, pm);

        // Search and change all test case types dependent on the attribute.
        IIpsSrcFile[] testSrcFiles = policyCmptTypeAttribute.getIpsProject().findIpsSrcFiles(
                IpsObjectType.TEST_CASE_TYPE);
        for (IIpsSrcFile ipsSrcFile : testSrcFiles) {
            ITestCaseType testCaseType = (ITestCaseType)ipsSrcFile.getIpsObject();
            ITestPolicyCmptTypeParameter[] testParameters = testCaseType.getTestPolicyCmptTypeParameters();
            for (ITestPolicyCmptTypeParameter parameter : testParameters) {
                // TODO AW: contains test attribute would be nice
                ITestAttribute attribute = parameter.getTestAttribute(oldAttributeName);
                if (attribute != null) {
                    attribute.setAttribute(policyCmptTypeAttribute);
                }
            }
            if (ipsSrcFile.isDirty()) {
                ipsSrcFile.save(true, null);
            }
        }

        return null;
    }

    @Override
    public Object[] getElements() {
        return new Object[] { policyCmptTypeAttribute };
    }

    @Override
    public String getIdentifier() {
        return "renamePolicyCmptTypeAttributeProcessor";
    }

    @Override
    public String getProcessorName() {
        return "Rename Policy Cmpt Type Attribute Processor";
    }

    @Override
    public boolean isApplicable() throws CoreException {
        return policyCmptTypeAttribute.exists();
    }

    public void setNewName(String newName) {
        this.newName = newName;
    }

    @Override
    public RefactoringParticipant[] loadParticipants(RefactoringStatus status, SharableParticipants sharedParticipants)
            throws CoreException {

        return ParticipantManager.loadRenameParticipants(status, this, policyCmptTypeAttribute, new RenameArguments(
                newName, true), new String[] { IIpsProject.NATURE_ID }, sharedParticipants);
    }

}
