/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.faktorips.devtools.bf.ui.model.commands;

import org.eclipse.gef.commands.Command;
import org.faktorips.devtools.core.model.bf.BFElementType;
import org.faktorips.devtools.core.model.bf.IBFElement;
import org.faktorips.devtools.core.model.bf.IBusinessFunction;
import org.faktorips.devtools.core.model.bf.IControlFlow;
import org.faktorips.util.memento.Memento;

public class ConnectionCommand extends Command {

    protected IBFElement sourceNode;
    protected IBFElement targetNode;
    private Memento businessFunctionState;
    private IBusinessFunction businessFunction;
    protected IControlFlow controlFlow;
    private boolean reconnect = false;

    public ConnectionCommand(boolean reconnect) {
        super("Connection");
        this.reconnect = reconnect;
    }

    public void setBusinessFunction(IBusinessFunction businessFunction) {
        this.businessFunction = businessFunction;
    }

    public boolean canExecute() {
        System.out.println("source: " + sourceNode + " target: " + targetNode);
        if (sourceNode != null) {
            if (sourceNode.getType() == BFElementType.START && !sourceNode.getOutgoingControlFlow().isEmpty()) {
                return false;
            }
            if (sourceNode.getType() == BFElementType.END) {
                return false;
            }
            if (sourceNode.getType() == BFElementType.MERGE && !sourceNode.getOutgoingControlFlow().isEmpty()) {
                return false;
            }
            if ((sourceNode.getType() == BFElementType.ACTION_BUSINESSFUNCTIONCALL
                    || sourceNode.getType() == BFElementType.ACTION_INLINE || sourceNode.getType() == BFElementType.ACTION_METHODCALL)
                    && !sourceNode.getOutgoingControlFlow().isEmpty()) {
                return false;
            }
        }
        if (targetNode != null) {
            if (targetNode.getType() == BFElementType.DECISION && !targetNode.getIncomingControlFlow().isEmpty()) {
                return false;
            }
            if (targetNode.getType() == BFElementType.END && !targetNode.getIncomingControlFlow().isEmpty()) {
                return false;
            }
            if ((targetNode.getType() == BFElementType.ACTION_BUSINESSFUNCTIONCALL
                    || targetNode.getType() == BFElementType.ACTION_INLINE || targetNode.getType() == BFElementType.ACTION_METHODCALL)
                    && !targetNode.getIncomingControlFlow().isEmpty()) {
                return false;
            }
        }
        if(targetNode == sourceNode){
            return false;
        }
        return true;
    }

    public void execute() {
        businessFunctionState = businessFunction.newMemento();
        if (!reconnect) {
            controlFlow = businessFunction.newControlFlow();
        }
        if (sourceNode != null) {
            controlFlow.setSource(sourceNode);
        }
        if (targetNode != null) {
            controlFlow.setTarget(targetNode);
        }
    }

    public String getLabel() {
        return "Create Connection";
    }

    public IBFElement getSource() {
        return sourceNode;
    }

    public IBFElement getTarget() {
        return targetNode;
    }

    public IControlFlow getControlFlow() {
        return controlFlow;
    }

    public void redo() {
        undo();
    }

    public void setSource(IBFElement source) {
        sourceNode = source;
    }

    public void setTarget(IBFElement target) {
        targetNode = target;
    }

    public void setControlFlow(IControlFlow controlFlow) {
        this.controlFlow = controlFlow;
    }

    public void undo() {
        Memento currentState = businessFunction.newMemento();
        businessFunction.setState(businessFunctionState);
        businessFunctionState = currentState;
    }

}
