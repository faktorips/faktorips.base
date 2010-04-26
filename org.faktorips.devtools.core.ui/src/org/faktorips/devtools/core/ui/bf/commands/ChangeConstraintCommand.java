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
