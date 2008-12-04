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
import org.faktorips.devtools.core.model.bf.IBusinessFunction;
import org.faktorips.devtools.core.model.bf.IControlFlow;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.memento.Memento;

public class DeleteConnectionCommand extends Command {

    private IBusinessFunction businessFunction;
    private IControlFlow controlFlow;
    private Memento businessFunctionState;

    public DeleteConnectionCommand() {
        super("Delete Connection");
    }

    public void setBusinessFunction(IBusinessFunction businessFunction) {
        this.businessFunction = businessFunction;
    }

    public boolean canExecute() {
        return true;
    }

    public void execute() {
        ArgumentCheck.notNull(controlFlow, this);
        ArgumentCheck.notNull(businessFunction, this);
        businessFunctionState = businessFunction.newMemento();
        controlFlow.setTarget(null);
        controlFlow.setSource(null);
        controlFlow.delete();
    }

    public String getLabel() {
        return "Delete Connection";
    }

    public IControlFlow getControlFlow() {
        return controlFlow;
    }

    public void redo() {
        Memento currentState = businessFunction.newMemento();
        businessFunction.setState(businessFunctionState);
        businessFunctionState = currentState;
    }

    public void setControlFlow(IControlFlow controlFlow) {
        this.controlFlow = controlFlow;
    }

    public void undo() {
        redo();
    }

}
