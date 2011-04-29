/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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
