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
import org.faktorips.devtools.model.bf.IBusinessFunction;
import org.faktorips.devtools.model.bf.Size;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.memento.Memento;

/**
 * This command resizes the rectangular figure that displays the parameters of a business function.
 * 
 * @author Peter Erzberger
 * @deprecated for removal since 21.6
 */
@Deprecated
public class ParameterFigureConstraintCommand extends org.eclipse.gef.commands.Command {

    private Rectangle constraint;
    private IBusinessFunction businessFunction;
    private Memento bfElementState;

    public ParameterFigureConstraintCommand(IBusinessFunction bf, Rectangle constraint) {
        ArgumentCheck.notNull(bf, this);
        ArgumentCheck.notNull(constraint, this);
        this.businessFunction = bf;
        this.constraint = constraint;
    }

    @Override
    public void execute() {
        bfElementState = businessFunction.newMemento();
        businessFunction.setParameterRectangleSize(new Size(constraint.getSize().width, constraint.getSize().height));
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
