/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.bf.commands;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.commands.Command;
import org.faktorips.devtools.core.model.bf.IControlFlow;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.memento.Memento;

/**
 * Abstract class for the commands handling bend points. This class handles undo and redo
 * functionality.
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
    public final void execute() {
        controlFlowState = controlFlow.newMemento();
        executeInternal();
    }

    /**
     * Subclasses implement the execution of the command here.
     */
    protected abstract void executeInternal();

    @Override
    public final void undo() {
        Memento currentState = controlFlow.newMemento();
        controlFlow.setState(controlFlowState);
        controlFlowState = currentState;
    }

    @Override
    public final void redo() {
        undo();
    }
}
