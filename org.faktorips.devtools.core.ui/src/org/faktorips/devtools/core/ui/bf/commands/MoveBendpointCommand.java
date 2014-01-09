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

import org.eclipse.draw2d.AbsoluteBendpoint;
import org.eclipse.draw2d.geometry.Point;
import org.faktorips.devtools.core.model.bf.IControlFlow;

/**
 * This command moves a control flow from to the specified location.
 * 
 * @author Peter Erzberger
 */
public class MoveBendpointCommand extends BendpointCommand {

    public MoveBendpointCommand(int index, Point location, IControlFlow controlFlow) {
        super(index, location, controlFlow);
    }

    @Override
    protected void executeInternal() {
        getControlFlow().setBendpoint(getIndex(), new AbsoluteBendpoint(getLocation()));
    }

}
