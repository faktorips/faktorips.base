/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.faktorips.devtools.bf.ui.model.commands;

import org.faktorips.devtools.core.model.bf.IBusinessFunction;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.memento.Memento;

/**
 * An abstract command class for all business function element commands. It handles undo and
 * redo functionality.
 * 
 * @author Peter Erzberger
 */
public abstract class BFElementCommand extends org.eclipse.gef.commands.Command {

    protected IBusinessFunction businessFunction;
    private Memento businessFunctionState;

    public BFElementCommand(String label, IBusinessFunction businessFunction) {
        super(label);
        ArgumentCheck.notNull(businessFunction, this);
        this.businessFunction = businessFunction;
    }

    @Override
    public final void execute() {
        businessFunctionState = businessFunction.newMemento();
        executeInternal();
    }

    /**
     * Subclasses implement the execution of the command here.
     */
    protected abstract void executeInternal();

    @Override
    public final void redo() {
        undo();
    }

    @Override
    public final void undo() {
        Memento oldState = businessFunction.newMemento();
        businessFunction.setState(businessFunctionState);
        businessFunctionState = oldState;
    }

}
