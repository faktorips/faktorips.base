/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.faktorips.devtools.bf.ui.model.commands;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.commands.Command;
import org.faktorips.devtools.core.model.bf.IControlFlow;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.memento.Memento;

/**
 * Abstract class for the commands handling bend points. This class handles undo and redo functionality.
 * 
 * @author Peter Erzberger
 */
public abstract class BendpointCommand extends Command {

    protected int index;
    protected Point location;
    protected IControlFlow controlFlow;
    private Memento controlFlowState;
    
    public BendpointCommand(int index, Point location, IControlFlow controlFlow) {
        super();
        this.index = index;
        ArgumentCheck.notNull(controlFlow, this);
        this.location = location;
        this.controlFlow = controlFlow;
    }

    protected final int getIndex() {
        return index;
    }

    protected final Point getLocation() {
        return location;
    }

    protected final IControlFlow getControlFlow() {
        return controlFlow;
    }

    @Override
    public final void execute(){
        controlFlowState = controlFlow.newMemento();
        executeInternal();
    }
    
    /**
     * Subclasses implement the execution of the command here.
     */
    protected abstract void executeInternal();
    
   @Override
    public final void undo(){
        Memento currentState = controlFlow.newMemento();
        controlFlow.setState(controlFlowState);
        controlFlowState = currentState;
    }
    
   @Override
    public final void redo() {
        undo();
    }
}
