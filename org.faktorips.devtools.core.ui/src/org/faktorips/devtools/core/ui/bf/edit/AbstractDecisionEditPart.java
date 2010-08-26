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

import org.eclipse.draw2d.BorderLayout;
import org.eclipse.draw2d.ChopboxAnchor;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;
import org.faktorips.devtools.core.ui.bf.draw2d.ScalableRhombFigure;
import org.faktorips.util.message.MessageList;

/**
 * The edit part for the decision model object.
 * 
 * @author Peter Erzberger
 */
public abstract class AbstractDecisionEditPart extends NodeEditPart {

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
        figure = createNodeShape();
        createConnectionAnchor((Figure)figure);

        return figure;
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
