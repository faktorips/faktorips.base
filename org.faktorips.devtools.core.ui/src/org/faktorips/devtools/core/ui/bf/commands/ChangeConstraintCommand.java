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
