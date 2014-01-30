/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.bf.commands;

import org.faktorips.devtools.core.model.bf.IBusinessFunction;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.memento.Memento;

/**
 * An abstract command class for all business function element commands. It handles undo and redo
 * functionality.
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
