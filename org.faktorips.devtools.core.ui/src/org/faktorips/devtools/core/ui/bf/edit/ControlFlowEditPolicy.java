/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.faktorips.devtools.core.ui.bf.edit;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.requests.GroupRequest;
import org.faktorips.devtools.core.model.bf.IBusinessFunction;
import org.faktorips.devtools.core.model.bf.IControlFlow;
import org.faktorips.devtools.core.ui.bf.commands.DeleteConnectionCommand;

/**
 * This policy is responsible for the deletion of a control flow.
 * 
 * @author Peter Erzberger
 */
public class ControlFlowEditPolicy extends org.eclipse.gef.editpolicies.ConnectionEditPolicy {

    protected Command getDeleteCommand(GroupRequest request) {
        IControlFlow controlFlow = (IControlFlow)getHost().getModel();
        IBusinessFunction businessFunction = controlFlow.getBusinessFunction();
        return new DeleteConnectionCommand(businessFunction, controlFlow);
    }

}
