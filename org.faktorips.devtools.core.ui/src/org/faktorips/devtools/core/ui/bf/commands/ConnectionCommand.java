/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.bf.commands;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.gef.commands.Command;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.bf.BFElementType;
import org.faktorips.devtools.core.model.bf.IBFElement;
import org.faktorips.devtools.core.model.bf.IBusinessFunction;
import org.faktorips.devtools.core.model.bf.IControlFlow;
import org.faktorips.devtools.core.model.bf.IDecisionBFE;
import org.faktorips.util.memento.Memento;

public class ConnectionCommand extends Command {

    protected IBFElement sourceNode;
    protected IBFElement targetNode;
    private Memento businessFunctionState;
    private IBusinessFunction businessFunction;
    protected IControlFlow controlFlow;
    private boolean reconnect = false;

    public ConnectionCommand(boolean reconnect) {
        super("Connection"); //$NON-NLS-1$
        this.reconnect = reconnect;
    }

    public void setBusinessFunction(IBusinessFunction businessFunction) {
        this.businessFunction = businessFunction;
    }

    @Override
    public boolean canExecute() {
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
            if ((targetNode.getType() == BFElementType.DECISION || targetNode.getType() == BFElementType.DECISION_METHODCALL)
                    && !targetNode.getIncomingControlFlow().isEmpty()) {
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
        if (targetNode == sourceNode) {
            return false;
        }
        return true;
    }

    @Override
    public void execute() {
        businessFunctionState = businessFunction.newMemento();
        if (!reconnect) {
            controlFlow = businessFunction.newControlFlow();
        }
        if (sourceNode != null) {
            setDefaultConditionValueForBooleanDecisionSourceNode();
            controlFlow.setSource(sourceNode);
        }
        if (targetNode != null) {
            controlFlow.setTarget(targetNode);
        }
    }

    private void setDefaultConditionValueForBooleanDecisionSourceNode() {
        if (!reconnect
                && (sourceNode.getType().equals(BFElementType.DECISION) || sourceNode.getType().equals(
                        BFElementType.DECISION_METHODCALL))) {
            IDecisionBFE decision = (IDecisionBFE)sourceNode;
            try {
                Datatype datatype = decision.findDatatype(sourceNode.getIpsProject());
                if (Datatype.BOOLEAN.equals(datatype)) {
                    List<IControlFlow> outs = decision.getOutgoingControlFlow();
                    if (outs.isEmpty()) {
                        controlFlow.setConditionValue(Boolean.TRUE.toString());
                    } else if (outs.size() == 1) {
                        IControlFlow out1 = outs.get(0);
                        Boolean conditionValue = Boolean.parseBoolean(out1.getConditionValue());
                        if (conditionValue != null) {
                            if (conditionValue.equals(Boolean.TRUE)) {
                                controlFlow.setConditionValue(Boolean.FALSE.toString());
                            } else {
                                controlFlow.setConditionValue(Boolean.TRUE.toString());
                            }
                        }
                    }
                }
            } catch (CoreException e) {
                IpsPlugin.log(e);
            }
        }
    }

    @Override
    public String getLabel() {
        return Messages.ConnectionCommand_createConnection;
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

    @Override
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

    @Override
    public void undo() {
        Memento currentState = businessFunction.newMemento();
        businessFunction.setState(businessFunctionState);
        businessFunctionState = currentState;
    }

}
