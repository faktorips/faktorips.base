/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
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
