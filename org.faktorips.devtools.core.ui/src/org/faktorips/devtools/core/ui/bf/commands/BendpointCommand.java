/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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
