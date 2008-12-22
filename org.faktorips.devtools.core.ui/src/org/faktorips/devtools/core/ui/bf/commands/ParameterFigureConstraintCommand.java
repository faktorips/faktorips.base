/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.faktorips.devtools.core.ui.bf.commands;

import org.eclipse.draw2d.geometry.Rectangle;
import org.faktorips.devtools.core.model.bf.IBusinessFunction;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.memento.Memento;

/**
 * This command resizes the rectangular figure that displays the parameters of a business function.
 * 
 * @author Peter Erzberger
 */
public class ParameterFigureConstraintCommand extends org.eclipse.gef.commands.Command {

    private Rectangle contraint;
    private IBusinessFunction businessFunction;
    private Memento bfElementState;

    
    public ParameterFigureConstraintCommand(IBusinessFunction bf, Rectangle contraint) {
        ArgumentCheck.notNull(bf, this);
        ArgumentCheck.notNull(contraint, this);
        this.businessFunction = bf;
        this.contraint = contraint;
    }

    @Override
    public void execute() {
        bfElementState = businessFunction.newMemento();
        businessFunction.setParameterRectangleSize(contraint.getSize());
    }

    @Override
    public void redo() {
        undo();
    }

    @Override
    public void undo() {
        Memento currentState = businessFunction.newMemento();
        businessFunction.setState(bfElementState);
        bfElementState = currentState;
    }

}
