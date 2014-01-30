/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
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
        for (IControlFlow controlFlow : bfElement.getOutgoingControlFlow()) {
            controlFlow.setSource(null);
            controlFlow.setTarget(null);
            controlFlow.delete();
        }
        for (IControlFlow controlFlow : bfElement.getIncomingControlFlow()) {
            controlFlow.setSource(null);
            controlFlow.setTarget(null);
            controlFlow.delete();
        }
        bfElement.delete();
    }
}
