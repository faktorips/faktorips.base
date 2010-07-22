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

import org.eclipse.gef.commands.Command;
import org.faktorips.devtools.core.model.bf.IBusinessFunction;
import org.faktorips.devtools.core.model.bf.IControlFlow;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.memento.Memento;

/**
 * This command deletes the control flow object that is provided to it.
 * 
 * @author Peter Erzberger
 */
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
