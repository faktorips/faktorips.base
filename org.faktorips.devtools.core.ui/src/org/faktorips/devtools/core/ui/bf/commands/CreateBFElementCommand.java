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
