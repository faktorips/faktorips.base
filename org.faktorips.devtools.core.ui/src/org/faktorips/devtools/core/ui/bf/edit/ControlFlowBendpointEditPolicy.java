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
