/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.faktorips.devtools.bf.ui.model.commands;

import org.eclipse.draw2d.geometry.Point;
import org.faktorips.devtools.core.model.bf.BFElementType;
import org.faktorips.devtools.core.model.bf.IBusinessFunction;
import org.faktorips.util.ArgumentCheck;

public class CreateBFElementCommand extends BFElementCommand {

    private Point location;
    private BFElementType bfElementType;

    public CreateBFElementCommand(BFElementType bfElementType, IBusinessFunction businessFunction, Point location) {
        super("Create Object", businessFunction);
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
