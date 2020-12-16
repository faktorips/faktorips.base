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

import org.eclipse.draw2d.BorderLayout;
import org.eclipse.draw2d.ChopboxAnchor;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.swt.graphics.Color;
import org.faktorips.devtools.core.ui.bf.draw2d.ScalableRhombFigure;
import org.faktorips.util.message.MessageList;

/**
 * The edit part of the merge business function element.
 * 
 * @author Peter Erzberger
 */
public class MergeEditPart extends NodeEditPart {

    private ScalableRhombFigure errorDisplay;

    protected void createConnectionAnchor(Figure figure) {
        setTargetConnectionAnchor(new ChopboxAnchor(figure));
        setSourceConnectionAnchor(new ChopboxAnchor(figure));
    }

    protected Figure createNodeShape() {
        ScalableRhombFigure figure = new ScalableRhombFigure();
        BorderLayout layout = new BorderLayout();
        figure.setLayoutManager(layout);
        figure.add(nameLabel, BorderLayout.CENTER);
        errorDisplay = figure;
        return figure;
    }

    @Override
    protected IFigure createFigureInternal() {
        Figure shape = createNodeShape();
        shape.setBackgroundColor(new Color(null, 248, 248, 248));
        createConnectionAnchor(shape);
        return shape;
    }

    @Override
    protected void showError(MessageList msgList) {
        if (!msgList.isEmpty()) {
            errorDisplay.showError(true);
        } else {
            errorDisplay.showError(false);
        }
    }

}
