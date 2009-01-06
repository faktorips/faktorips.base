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

package org.faktorips.devtools.core;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.resources.ISaveContext;
import org.eclipse.core.resources.ISaveParticipant;
import org.eclipse.core.runtime.CoreException;

public class IpsCompositeSaveParticipant implements ISaveParticipant {

    private Set saveParticipants = new HashSet();
    
    public void addSaveParticipant(ISaveParticipant participant){
        if(participant == null){
            return;
        }
        saveParticipants.add(participant);
    }
    
    public boolean removeSaveParticipant(ISaveParticipant participant){
        return saveParticipants.remove(participant);
    }
    
    public void doneSaving(ISaveContext context) {
        for (Iterator it = saveParticipants.iterator(); it.hasNext();) {
            ISaveParticipant participant = (ISaveParticipant)it.next();
            try {
                participant.doneSaving(context);
            } catch (Exception e) {
                IpsPlugin.log(e);
            }
        }
    }

    public void prepareToSave(ISaveContext context) throws CoreException {
        for (Iterator it = saveParticipants.iterator(); it.hasNext();) {
            ISaveParticipant participant = (ISaveParticipant)it.next();
            try {
                participant.prepareToSave(context);
            } catch (Exception e) {
                IpsPlugin.log(e);
            }
        }

    }

    public void rollback(ISaveContext context) {
        for (Iterator it = saveParticipants.iterator(); it.hasNext();) {
            ISaveParticipant participant = (ISaveParticipant)it.next();
            try {
                participant.rollback(context);
            } catch (Exception e) {
                IpsPlugin.log(e);
            }
        }
    }

    public void saving(ISaveContext context) throws CoreException {
        for (Iterator it = saveParticipants.iterator(); it.hasNext();) {
            ISaveParticipant participant = (ISaveParticipant)it.next();
            try {
                participant.saving(context);
            } catch (Exception e) {
                IpsPlugin.log(e);
            }
        }
    }

}
