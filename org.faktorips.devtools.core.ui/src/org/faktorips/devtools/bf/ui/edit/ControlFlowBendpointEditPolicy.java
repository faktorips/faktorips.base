/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.faktorips.devtools.bf.ui.edit;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.requests.BendpointRequest;
import org.faktorips.devtools.bf.ui.model.commands.CreateBendpointCommand;
import org.faktorips.devtools.bf.ui.model.commands.DeleteBendpointCommand;
import org.faktorips.devtools.bf.ui.model.commands.MoveBendpointCommand;
import org.faktorips.devtools.core.model.bf.IControlFlow;

public class ControlFlowBendpointEditPolicy extends org.eclipse.gef.editpolicies.BendpointEditPolicy {

    protected Command getCreateBendpointCommand(BendpointRequest request) {
        Point location = request.getLocation();
        getConnection().translateToRelative(location);
        return new CreateBendpointCommand(request.getIndex(), location, (IControlFlow)request.getSource()
                .getModel());
    }

    protected Command getMoveBendpointCommand(BendpointRequest request) {
        Point location = request.getLocation();
        getConnection().translateToRelative(location);
        return new MoveBendpointCommand(request.getIndex(), location, (IControlFlow)request.getSource()
                .getModel());
    }

    protected Command getDeleteBendpointCommand(BendpointRequest request) {
        return new DeleteBendpointCommand(request.getIndex(), (IControlFlow)request.getSource()
                .getModel());
    }

}
