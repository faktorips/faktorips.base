/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.faktorips.devtools.core.ui.bf.model.commands;

import org.faktorips.devtools.core.model.bf.IControlFlow;

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
