package org.faktorips.devtools.bf.ui.edit;

import org.eclipse.draw2d.BorderLayout;
import org.eclipse.draw2d.ChopboxAnchor;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.swt.graphics.Color;
import org.faktorips.devtools.bf.ui.draw2d.ScalableRhombFigure;
import org.faktorips.util.message.MessageList;

public class MergeEditPart extends NodeEditPart {

    private ScalableRhombFigure errorDisplay;

    @Override
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

    protected void showError(MessageList msgList){
        if(!msgList.isEmpty()){
            errorDisplay.showError(true);
        } else {
            errorDisplay.showError(false);
        }
    }

}
