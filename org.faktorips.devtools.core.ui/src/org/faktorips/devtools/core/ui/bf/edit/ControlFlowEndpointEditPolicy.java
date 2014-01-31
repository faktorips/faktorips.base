/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.bf.edit;

import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.gef.GraphicalEditPart;

/**
 * In addition to the standard connection end point edit policy this policy increases the line width
 * of the connection figure when it is selected.
 * 
 * @author Peter Erzberger
 */
public class ControlFlowEndpointEditPolicy extends org.eclipse.gef.editpolicies.ConnectionEndpointEditPolicy {

    @Override
    protected void addSelectionHandles() {
        super.addSelectionHandles();
        getConnectionFigure().setLineWidth(2);
    }

    protected PolylineConnection getConnectionFigure() {
        return (PolylineConnection)((GraphicalEditPart)getHost()).getFigure();
    }

    @Override
    protected void removeSelectionHandles() {
        super.removeSelectionHandles();
        getConnectionFigure().setLineWidth(0);
    }

}
