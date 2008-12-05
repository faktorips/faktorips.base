/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.faktorips.devtools.bf.ui.model.commands;

import org.eclipse.draw2d.geometry.Rectangle;
import org.faktorips.devtools.core.model.bf.IBFElement;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.memento.Memento;

/**
 * A command that updates the position and size of a business function element.
 * 
 * @author Peter Erzberger
 */
public class ChangeConstraintCommand extends org.eclipse.gef.commands.Command {

    private Rectangle contraint;
    private IBFElement bfElement;
    private Memento bfElementState;

    
    public ChangeConstraintCommand(IBFElement element, Rectangle contraint) {
        ArgumentCheck.notNull(element, this);
        ArgumentCheck.notNull(contraint, this);
        this.bfElement = element;
        this.contraint = contraint;
    }

    @Override
    public void execute() {
        bfElementState = bfElement.newMemento();
        bfElement.setSize(contraint.getSize());
        bfElement.setLocation(contraint.getLocation());
    }

    @Override
    public void redo() {
        undo();
    }

    @Override
    public void undo() {
        Memento currentState = bfElement.newMemento();
        bfElement.setState(bfElementState);
        bfElementState = currentState;
    }

}
