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

public abstract class BFElementCommand extends org.eclipse.gef.commands.Command {

    protected IBusinessFunction businessFunction;
    private Memento businessFunctionState;

    public BFElementCommand(String label, IBusinessFunction businessFunction) {
        super(label);
        ArgumentCheck.notNull(businessFunction, this);
        this.businessFunction = businessFunction;
    }

    public final void execute() {
        businessFunctionState = businessFunction.newMemento();
        executeInternal();
    }

    protected abstract void executeInternal();

    public final void redo() {
        undo();
    }

    public final void undo() {
        Memento oldState = businessFunction.newMemento();
        businessFunction.setState(businessFunctionState);
        businessFunctionState = oldState;
    }

}
