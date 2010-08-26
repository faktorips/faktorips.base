/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.bf.edit;

import org.eclipse.draw2d.ChopboxAnchor;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;
import org.faktorips.devtools.core.ui.bf.draw2d.StartFigure;
import org.faktorips.util.message.MessageList;

/**
 * An edit part that provides a nonresizable figure for the start business function element.
 * 
 * @author Peter Erzberger
 */
public class StartEditPart extends NodeEditPart {

    @Override
    protected IFigure createFigureInternal() {
        Figure figure = new StartFigure();
        setSourceConnectionAnchor(new ChopboxAnchor(figure));
        return figure;
    }

    @Override
    protected void showError(MessageList msgList) {
        StartFigure figure = (StartFigure)getFigure();
        if (!msgList.isEmpty()) {
            figure.showError(true);
        } else {
            figure.showError(false);
        }
    }

}
