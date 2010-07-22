/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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
