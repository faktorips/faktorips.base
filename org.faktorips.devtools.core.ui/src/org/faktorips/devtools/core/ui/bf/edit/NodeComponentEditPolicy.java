/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
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
