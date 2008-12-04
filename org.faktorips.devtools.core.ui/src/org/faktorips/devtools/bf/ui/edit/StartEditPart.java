package org.faktorips.devtools.bf.ui.edit;

import org.eclipse.draw2d.ChopboxAnchor;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;
import org.faktorips.devtools.bf.ui.draw2d.StartFigure;
import org.faktorips.util.message.MessageList;

public class StartEditPart extends NodeEditPart {

    @Override
    protected void createConnectionAnchor(Figure figure) {
        setSourceConnectionAnchor(new ChopboxAnchor(figure));
    }

    @Override
    protected IFigure createFigureInternal() {
        Figure figure = new StartFigure();
        createConnectionAnchor(figure);
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
