/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.bf.commands;

import org.eclipse.gef.commands.Command;
import org.faktorips.devtools.model.bf.IBusinessFunction;
import org.faktorips.devtools.model.bf.IControlFlow;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.memento.Memento;

/**
 * This command deletes the control flow object that is provided to it.
 * 
 * @author Peter Erzberger
 * @deprecated for removal since 21.6
 */
@Deprecated
public class DeleteConnectionCommand extends Command {

    private IBusinessFunction businessFunction;
    private IControlFlow controlFlow;
    private Memento businessFunctionState;

    public DeleteConnectionCommand(IBusinessFunction businessFunction, IControlFlow controlFlow) {
        super("Delete Connection"); //$NON-NLS-1$
        this.businessFunction = businessFunction;
        this.controlFlow = controlFlow;
        ArgumentCheck.notNull(controlFlow, this);
        ArgumentCheck.notNull(businessFunction, this);
    }

    @Override
    public boolean canExecute() {
        return true;
    }

    @Override
    public void execute() {
        businessFunctionState = businessFunction.newMemento();
        controlFlow.setTarget(null);
        controlFlow.setSource(null);
        controlFlow.delete();
    }

    @Override
    public String getLabel() {
        return Messages.DeleteConnectionCommand_deleteConnection;
    }

    public IControlFlow getControlFlow() {
        return controlFlow;
    }

    @Override
    public void redo() {
        Memento currentState = businessFunction.newMemento();
        businessFunction.setState(businessFunctionState);
        businessFunctionState = currentState;
    }

    @Override
    public void undo() {
        redo();
    }

}
