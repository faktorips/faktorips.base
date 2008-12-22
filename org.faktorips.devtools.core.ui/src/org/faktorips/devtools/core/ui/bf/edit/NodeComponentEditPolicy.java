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
import org.faktorips.devtools.core.model.bf.IBFElement;
import org.faktorips.devtools.core.model.bf.IBusinessFunction;
import org.faktorips.devtools.core.ui.bf.commands.DeleteBFElementCommand;

/**
 * This policy creates the deletion command for business function elements.
 * 
 * @author Peter Erzberger
 */
public class NodeComponentEditPolicy extends org.eclipse.gef.editpolicies.ComponentEditPolicy {

    protected Command createDeleteCommand(GroupRequest request) {
        IBusinessFunction bf = (IBusinessFunction)getHost().getParent().getModel();
        DeleteBFElementCommand deleteCmd = new DeleteBFElementCommand(bf, (IBFElement)getHost().getModel());
        return deleteCmd;
    }
}
