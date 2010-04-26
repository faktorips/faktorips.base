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
