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

import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.GraphicalNodeEditPolicy;
import org.eclipse.gef.requests.CreateConnectionRequest;
import org.eclipse.gef.requests.ReconnectRequest;
import org.faktorips.devtools.core.model.bf.IBFElement;
import org.faktorips.devtools.core.model.bf.IBusinessFunction;
import org.faktorips.devtools.core.model.bf.IControlFlow;
import org.faktorips.devtools.core.ui.bf.commands.ConnectionCommand;

/**
 * This policy creates the ConnectionCreateion command which is reponsible for establishing a
 * connection and reconnection of a control flow object.
 * 
 * @author Peter Erzberger
 */
public class NodeEditPolicy extends GraphicalNodeEditPolicy {

    @Override
    protected Command getConnectionCompleteCommand(CreateConnectionRequest request) {
        ConnectionAnchor anchor = getNodeEditPart().getTargetConnectionAnchor(request);
        if (anchor == null) {
            return null;
        }
        ConnectionCommand command = (ConnectionCommand)request.getStartCommand();
        command.setTarget(getNode());
        return command;
    }

    @Override
    protected Command getConnectionCreateCommand(CreateConnectionRequest request) {
        ConnectionAnchor anchor = getNodeEditPart().getSourceConnectionAnchor(request);
        if (anchor == null) {
            return null;
        }
        ConnectionCommand command = new ConnectionCommand(false);
        command.setBusinessFunction(getNode().getBusinessFunction());
        command.setSource(getNode());
        request.setStartCommand(command);
        return command;
    }

    private NodeEditPart getNodeEditPart() {
        return (NodeEditPart)getHost();
    }

    private IBFElement getNode() {
        return (IBFElement)getHost().getModel();
    }

    @Override
    protected Command getReconnectSourceCommand(ReconnectRequest request) {
        ConnectionCommand command = new ConnectionCommand(true);
        IControlFlow controlFlow = (IControlFlow)request.getConnectionEditPart().getModel();
        IBusinessFunction bf = controlFlow.getBusinessFunction();
        command.setControlFlow(controlFlow);
        command.setBusinessFunction(bf);
        command.setSource(getNode());
        return command;
    }

    @Override
    protected Command getReconnectTargetCommand(ReconnectRequest request) {
        ConnectionCommand command = new ConnectionCommand(true);
        IControlFlow controlFlow = (IControlFlow)request.getConnectionEditPart().getModel();
        IBusinessFunction bf = controlFlow.getBusinessFunction();
        command.setControlFlow(controlFlow);
        command.setBusinessFunction(bf);
        command.setTarget(getNode());
        return command;
    }

}
