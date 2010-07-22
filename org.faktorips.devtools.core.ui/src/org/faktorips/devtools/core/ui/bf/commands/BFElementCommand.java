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
