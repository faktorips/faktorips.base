/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.bf.edit;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.requests.GroupRequest;
import org.faktorips.devtools.core.ui.bf.commands.DeleteConnectionCommand;
import org.faktorips.devtools.model.bf.IBusinessFunction;
import org.faktorips.devtools.model.bf.IControlFlow;

/**
 * This policy is responsible for the deletion of a control flow.
 * 
 * @author Peter Erzberger
 */
public class ControlFlowEditPolicy extends org.eclipse.gef.editpolicies.ConnectionEditPolicy {

    @Override
    protected Command getDeleteCommand(GroupRequest request) {
        IControlFlow controlFlow = (IControlFlow)getHost().getModel();
        IBusinessFunction businessFunction = controlFlow.getBusinessFunction();
        return new DeleteConnectionCommand(businessFunction, controlFlow);
    }

}
