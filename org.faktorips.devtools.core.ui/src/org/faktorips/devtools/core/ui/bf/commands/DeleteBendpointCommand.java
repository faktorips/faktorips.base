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

import org.faktorips.devtools.model.bf.IControlFlow;

/**
 * A command that deletes the bend point of a control flow at the index that is provided to it.
 * 
 * @author Peter Erzberger
 */
public class DeleteBendpointCommand extends BendpointCommand {

    public DeleteBendpointCommand(int index, IControlFlow controlFlow) {
        super(index, null, controlFlow);
    }

    @Override
    protected void executeInternal() {
        getControlFlow().removeBendpoint(getIndex());
    }

}
