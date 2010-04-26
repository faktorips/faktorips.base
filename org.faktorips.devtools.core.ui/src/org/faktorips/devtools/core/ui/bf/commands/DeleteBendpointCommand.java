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
