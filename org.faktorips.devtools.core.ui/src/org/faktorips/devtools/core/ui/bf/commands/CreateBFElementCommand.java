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

import org.eclipse.draw2d.geometry.Point;
import org.faktorips.devtools.core.model.bf.BFElementType;
import org.faktorips.devtools.core.model.bf.IBusinessFunction;
import org.faktorips.util.ArgumentCheck;

/**
 * A command that creates a new business function element according to the {@link BFElementType}
 * that is provided to it.
 * 
 * @author Peter Erzberger
 */
public class CreateBFElementCommand extends BFElementCommand {

    private Point location;
    private BFElementType bfElementType;

    public CreateBFElementCommand(BFElementType bfElementType, IBusinessFunction businessFunction, Point location) {
        super("Create Object", businessFunction); //$NON-NLS-1$
        ArgumentCheck.notNull(bfElementType, this);
        ArgumentCheck.notNull(location, this);
        this.bfElementType = bfElementType;
        this.location = location;
    }

    @Override
    protected void executeInternal() {
        bfElementType.newBFElement(businessFunction, location);
    }
}
