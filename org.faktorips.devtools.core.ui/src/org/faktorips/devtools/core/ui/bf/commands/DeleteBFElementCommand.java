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
package org.faktorips.devtools.core.ui.bf.commands;

import org.faktorips.devtools.core.model.bf.IBFElement;
import org.faktorips.devtools.core.model.bf.IBusinessFunction;
import org.faktorips.devtools.core.model.bf.IControlFlow;
import org.faktorips.util.ArgumentCheck;

/**
 * This command deletes the provided business function element and the control flows pointing to or 
 * starting from it.
 * 
 * @author Peter Erzberger
 */
public class DeleteBFElementCommand extends BFElementCommand {

	private IBFElement bfElement;
	
	public DeleteBFElementCommand(IBusinessFunction businessFunction, IBFElement element) {
		super("Delete Node", businessFunction); //$NON-NLS-1$
		ArgumentCheck.notNull(element, this);
		this.bfElement = element;
	}

	@Override
	public void executeInternal() {
	    for (IControlFlow controlFlow: bfElement.getOutgoingControlFlow()) {
            controlFlow.setSource(null);
            controlFlow.setTarget(null);
            controlFlow.delete();
        }
        for (IControlFlow controlFlow: bfElement.getIncomingControlFlow()) {
            controlFlow.setSource(null);
            controlFlow.setTarget(null);
            controlFlow.delete();
        }
		bfElement.delete();
	}
}
