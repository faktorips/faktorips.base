/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.ISaveContext;
import org.eclipse.core.resources.ISaveParticipant;
import org.eclipse.core.runtime.CoreException;

/**
 * A save participant that contains multiple "real" save participants and delegates all method calls
 * to all "real" save participants.
 * 
 * @author Jan Ortmann
 */
public class IpsCompositeSaveParticipant implements ISaveParticipant {

    private Set<ISaveParticipant> saveParticipants = new HashSet<ISaveParticipant>();

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
                IpsPlugin.log(e);
            }
        }
    }

    @Override
    public void prepareToSave(ISaveContext context) throws CoreException {
        for (ISaveParticipant participant : saveParticipants) {
            try {
                participant.prepareToSave(context);
            } catch (Exception e) {
                IpsPlugin.log(e);
            }
        }
    }

    @Override
    public void rollback(ISaveContext context) {
        for (ISaveParticipant participant : saveParticipants) {
            try {
                participant.rollback(context);
            } catch (Exception e) {
                IpsPlugin.log(e);
            }
        }
    }

    @Override
    public void saving(ISaveContext context) throws CoreException {
        for (ISaveParticipant participant : saveParticipants) {
            try {
                participant.saving(context);
            } catch (Exception e) {
                IpsPlugin.log(e);
            }
        }
    }

}
