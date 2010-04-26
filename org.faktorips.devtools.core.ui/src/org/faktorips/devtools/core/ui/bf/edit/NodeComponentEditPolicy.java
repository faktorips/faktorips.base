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

package org.faktorips.devtools.core.ui.bf.edit;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.requests.GroupRequest;
import org.faktorips.devtools.core.model.bf.IBFElement;
import org.faktorips.devtools.core.model.bf.IBusinessFunction;
import org.faktorips.devtools.core.ui.bf.commands.DeleteBFElementCommand;

/**
 * This policy creates the deletion command for business function elements.
 * 
 * @author Peter Erzberger
 */
public class NodeComponentEditPolicy extends org.eclipse.gef.editpolicies.ComponentEditPolicy {

    @Override
    protected Command createDeleteCommand(GroupRequest request) {
        IBusinessFunction bf = (IBusinessFunction)getHost().getParent().getModel();
        DeleteBFElementCommand deleteCmd = new DeleteBFElementCommand(bf, (IBFElement)getHost().getModel());
        return deleteCmd;
    }
}
