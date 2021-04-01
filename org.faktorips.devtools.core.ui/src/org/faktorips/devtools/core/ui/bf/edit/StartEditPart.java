/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.bf.edit;

import org.eclipse.draw2d.ChopboxAnchor;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;
import org.faktorips.devtools.core.ui.bf.draw2d.StartFigure;
import org.faktorips.runtime.MessageList;

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
