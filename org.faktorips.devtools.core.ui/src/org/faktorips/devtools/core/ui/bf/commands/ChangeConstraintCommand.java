/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.bf.commands;

import org.eclipse.draw2d.geometry.Rectangle;
import org.faktorips.devtools.model.bf.IBFElement;
import org.faktorips.devtools.model.bf.Location;
import org.faktorips.devtools.model.bf.Size;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.memento.Memento;

/**
 * A command that updates the position and size of a business function element.
 * 
 * @author Peter Erzberger
 */
public class ChangeConstraintCommand extends org.eclipse.gef.commands.Command {

    private Rectangle constraint;
    private IBFElement bfElement;
    private Memento bfElementState;

    public ChangeConstraintCommand(IBFElement element, Rectangle constraint) {
        ArgumentCheck.notNull(element, this);
        ArgumentCheck.notNull(constraint, this);
        this.bfElement = element;
        this.constraint = constraint;
    }

    @Override
    public void execute() {
        bfElementState = bfElement.newMemento();
        bfElement.setSize(new Size(constraint.getSize().width, constraint.getSize().height));
        bfElement.setLocation(new Location(constraint.getLocation().x, constraint.getLocation().y));
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
