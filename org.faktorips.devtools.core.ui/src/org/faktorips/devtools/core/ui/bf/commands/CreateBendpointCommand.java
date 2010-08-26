/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.bf.commands;

import org.eclipse.draw2d.AbsoluteBendpoint;
import org.eclipse.draw2d.geometry.Point;
import org.faktorips.devtools.core.model.bf.IControlFlow;

/**
 * A command that adds a bend point to a control flow.
 * 
 * @author Peter Erzberger
 */
public class CreateBendpointCommand extends BendpointCommand {

    public CreateBendpointCommand(int index, Point location, IControlFlow controlFlow) {
        super(index, location, controlFlow);
    }

    @Override
    protected void executeInternal() {
        getControlFlow().addBendpoint(getIndex(), new AbsoluteBendpoint(getLocation()));
    }

}
