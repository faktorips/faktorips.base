/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.bf.edit;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.BendpointEditPolicy;
import org.eclipse.gef.requests.BendpointRequest;
import org.faktorips.devtools.core.model.bf.IControlFlow;
import org.faktorips.devtools.core.ui.bf.commands.CreateBendpointCommand;
import org.faktorips.devtools.core.ui.bf.commands.DeleteBendpointCommand;
import org.faktorips.devtools.core.ui.bf.commands.MoveBendpointCommand;

/**
 * A specialization of {@link BendpointEditPolicy} that implements the necessary methods to create
 * commands.
 * 
 * @author Peter Erzberger
 */
public class ControlFlowBendpointEditPolicy extends org.eclipse.gef.editpolicies.BendpointEditPolicy {

    @Override
    protected Command getCreateBendpointCommand(BendpointRequest request) {
        Point location = request.getLocation();
        getConnection().translateToRelative(location);
        return new CreateBendpointCommand(request.getIndex(), location, (IControlFlow)request.getSource().getModel());
    }

    @Override
    protected Command getMoveBendpointCommand(BendpointRequest request) {
        Point location = request.getLocation();
        getConnection().translateToRelative(location);
        return new MoveBendpointCommand(request.getIndex(), location, (IControlFlow)request.getSource().getModel());
    }

    @Override
    protected Command getDeleteBendpointCommand(BendpointRequest request) {
        return new DeleteBendpointCommand(request.getIndex(), (IControlFlow)request.getSource().getModel());
    }

}
