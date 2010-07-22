/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.internal.migration;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.model.versionmanager.AbstractIpsProjectMigrationOperation;
import org.faktorips.runtime.internal.StringUtils;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;

/**
 * Migration from version 2.5.0.rc1 to version 2.6.0.rc1 Manually steps: if the master to detail
 * association is not unique then the inverse must be set to the corresponding detail to master
 * association. In some cases the interface DependantObject could not removed from classes with
 * annotation (e.g. XmlRootElement) if there is a compile error like: ... must implement the
 * inherited abstract method DependantObject.setParentModelObjectInternal(AbstractModelObject) then
 * the implementation of the interface DependantObject must be removed manually.
 * 
 * @author Joerg Ortmann
 */
public class Migration_2_5_0_rc2 extends AbstractIpsProjectMigrationOperation {
    public static final String MSGCODE_TARGET_POLICY_CMPT_NOT_EXISTS = "TargetPolicyCmptNotExists"; //$NON-NLS-1$
    public static final String MSGCODE_NO_MASTER_TO_DETAIL_CANDIDATE_NOT_EXISTS = "NoMasterToDetailCandidateNotExists"; //$NON-NLS-1$
    public static final String MSGCODE_MASTER_TO_DETAIL_CANDIDATES_NOT_UNIQUE = "MasterToDetailCandidatesNotUnique"; //$NON-NLS-1$
    public static final String MSGCODE_SUBSETTED_DERIVED_UNION_NOT_FOUND = "SubsettedDerivedUnionNotFound"; //$NON-NLS-1$

    public Migration_2_5_0_rc2(IIpsProject projectToMigrate, String featureId) {
        super(projectToMigrate, featureId);
    }

    @Override
    public String getDescription() {
        return "The modeling of composition has changed. " //$NON-NLS-1$
                + "Now every child model object becomes a concrete parent model object variable for each parent they belongs to."; //$NON-NLS-1$
    }

    @Override
    public String getTargetVersion() {
        return "2.5.0.rfinal"; //$NON-NLS-1$
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public MessageList migrate(IProgressMonitor monitor) throws CoreException, InvocationTargetException,
            InterruptedException {
        MessageList msgResultList = new MessageList();
        List<IIpsSrcFile> allIpsSrcFiles = new ArrayList<IIpsSrcFile>();
        getIpsProject().collectAllIpsSrcFilesOfSrcFolderEntries(allIpsSrcFiles);
        for (IIpsSrcFile currentIpsSrcFile : allIpsSrcFiles) {
            if (currentIpsSrcFile.getIpsObjectType().equals(IpsObjectType.POLICY_CMPT_TYPE)) {
                IPolicyCmptType policyCmptType = (IPolicyCmptType)currentIpsSrcFile.getIpsObject();
                migratePolicyCmptType(msgResultList, policyCmptType);
            }
        }
        if (msgResultList.getNoOfMessages() > 0) {
            String text = "Migration finished with warnings. Some manually migration steps are necessary. See other messages for details."; //$NON-NLS-1$
            msgResultList.add(new Message("finishedWithWarnings", text, Message.WARNING)); //$NON-NLS-1$
        }
        return msgResultList;
    }

    private void migratePolicyCmptType(MessageList msgResultList, IPolicyCmptType policyCmptType) throws CoreException {
        IPolicyCmptTypeAssociation[] associations = policyCmptType.getPolicyCmptTypeAssociations();
        for (IPolicyCmptTypeAssociation association : associations) {
            MessageList ml = association.validate(getIpsProject());
            if (!ml.containsErrorMsg()) {
                continue;
            }
            migrateAssociations(msgResultList, association);
        }
    }

    private void migrateAssociations(MessageList msgResultList, IPolicyCmptTypeAssociation association)
            throws CoreException {
        fixInverseAssociation(msgResultList, association);
    }

    private void fixInverseAssociation(MessageList msgResultList, IPolicyCmptTypeAssociation association)
            throws CoreException {
        fixInverseOfDetailToMasterAssociation(getIpsProject(), msgResultList, association);
    }

    /**
     * Sets the inverse of a detail to master association
     * 
     * 
     * returns <code>false</code> if the fix doesn't affect the the given association, otherwise
     * <code>true</code>
     * 
     * Note: Method is public for testing purposes.
     */
    public static boolean fixInverseOfDetailToMasterAssociation(IIpsProject ipsProject,
            MessageList msgList,
            IPolicyCmptTypeAssociation association) throws CoreException {
        if (!(association.getAssociationType().isCompositionDetailToMaster() && StringUtils.isEmpty(association
                .getInverseAssociation()))) {
            // fix only detail to master associations with empty inverse association
            return false;
        }

        IPolicyCmptType targetPolicyCmptType = association.findTargetPolicyCmptType(ipsProject);
        if (targetPolicyCmptType == null) {
            // can't fix inverse because target not found
            String text = "Detail to master association couldn't be fixed, target policy component type not found"; //$NON-NLS-1$
            msgList.add(new Message(MSGCODE_TARGET_POLICY_CMPT_NOT_EXISTS, text, Message.WARNING, association));
            return true;
        }
        IAssociation[] associationCandidates = targetPolicyCmptType.getAssociationsForTarget(association
                .getPolicyCmptType().getQualifiedName());
        List<IPolicyCmptTypeAssociation> masterDetailcanditates = searchMasterToDetailCandidate(association,
                associationCandidates);
        if (masterDetailcanditates.size() == 1) {
            // OK: master to detail found
            // fix inverse on both associations
            IPolicyCmptTypeAssociation masterDetailAssociationToFix = masterDetailcanditates.get(0);
            association.setInverseAssociation(masterDetailAssociationToFix.getName());
            masterDetailAssociationToFix.setInverseAssociation(association.getName());
        } else if (masterDetailcanditates.size() == 0) {
            // error: no master detail found
            String text = "Detail to master association couldn't be fixed, no corresponding master to detail association found"; //$NON-NLS-1$
            msgList.add(new Message(MSGCODE_NO_MASTER_TO_DETAIL_CANDIDATE_NOT_EXISTS, text, Message.WARNING,
                    association));
        } else {
            // error: to many master detail found, not unique
            String text = "Detail to master association couldn't be fixed, no unique master to detail association found"; //$NON-NLS-1$
            msgList
                    .add(new Message(MSGCODE_MASTER_TO_DETAIL_CANDIDATES_NOT_UNIQUE, text, Message.WARNING, association));
        }
        return true;
    }

    private static List<IPolicyCmptTypeAssociation> searchMasterToDetailCandidate(IPolicyCmptTypeAssociation association,
            IAssociation[] associationCandidates) {
        List<IPolicyCmptTypeAssociation> candidates = new ArrayList<IPolicyCmptTypeAssociation>();
        for (IAssociation associationCandidate : associationCandidates) {
            IPolicyCmptTypeAssociation candidate = (IPolicyCmptTypeAssociation)associationCandidate;
            if (!candidate.getAssociationType().isCompositionMasterToDetail()) {
                continue;
            }
            if (!StringUtils.isEmpty(candidate.getInverseAssociation())) {
                // inverse already set on master to detail association
                if (candidate.getInverseAssociation().equals(association.getName())) {
                    // OK: inverse already points to detail to master association
                    candidates.add(candidate);
                }
                // maybe inverse, skip this association
            } else {
                candidates.add(candidate);
            }
        }
        return candidates;
    }
}
