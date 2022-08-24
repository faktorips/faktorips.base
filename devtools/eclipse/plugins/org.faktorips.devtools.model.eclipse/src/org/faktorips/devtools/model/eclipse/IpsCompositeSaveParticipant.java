/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.eclipse;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.ISaveContext;
import org.eclipse.core.resources.ISaveParticipant;
import org.faktorips.devtools.model.plugin.IpsLog;

/**
 * A save participant that contains multiple "real" save participants and delegates all method calls
 * to all "real" save participants.
 * 
 * @author Jan Ortmann
 */
public class IpsCompositeSaveParticipant implements ISaveParticipant {

    private Set<ISaveParticipant> saveParticipants = new HashSet<>();

    public void addSaveParticipant(ISaveParticipant participant) {
        if (participant == null) {
            return;
        }
        saveParticipants.add(participant);
    }

    public boolean removeSaveParticipant(ISaveParticipant participant) {
        return saveParticipants.remove(participant);
    }

    @Override
    public void doneSaving(ISaveContext context) {
        for (ISaveParticipant participant : saveParticipants) {
            try {
                participant.doneSaving(context);
            } catch (Exception e) {
                IpsLog.log(e);
            }
        }
    }

    @Override
    public void prepareToSave(ISaveContext context) {
        for (ISaveParticipant participant : saveParticipants) {
            try {
                participant.prepareToSave(context);
            } catch (Exception e) {
                IpsLog.log(e);
            }
        }
    }

    @Override
    public void rollback(ISaveContext context) {
        for (ISaveParticipant participant : saveParticipants) {
            try {
                participant.rollback(context);
            } catch (Exception e) {
                IpsLog.log(e);
            }
        }
    }

    @Override
    public void saving(ISaveContext context) {
        for (ISaveParticipant participant : saveParticipants) {
            try {
                participant.saving(context);
            } catch (Exception e) {
                IpsLog.log(e);
            }
        }
    }

}
