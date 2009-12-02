/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

import org.eclipse.draw2d.BorderLayout;
import org.eclipse.draw2d.ChopboxAnchor;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.RoundedRectangle;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.jface.resource.ImageDescriptor;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.bf.IActionBFE;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.MessageList;

/**
 * A specialization of {@link NodeEditPart} that creates the figure for an action, creates the
 * connection anchores and implements the error display.
 * 
 * @author Peter Erzberger
 */
public abstract class ActionEditPart extends NodeEditPart {

    private ImageDescriptor image;
    private IFigure errorDisplay;

    public ActionEditPart(ImageDescriptor image) {
        ArgumentCheck.notNull(image, this);
        this.image = image;
    }

    public IActionBFE getActionBFE() {
        return (IActionBFE)getModel();
    }

    private void createConnectionAnchor(Figure figure) {
        setTargetConnectionAnchor(new ChopboxAnchor(figure));
        setSourceConnectionAnchor(new ChopboxAnchor(figure));
    }

    @Override
    protected IFigure createFigureInternal() {
        Figure figure = createActionFigure();
        createConnectionAnchor(figure);
        return figure;
    }

    private Figure createActionFigure() {

        RoundedRectangle shape = new RoundedRectangle();
        shape.setCornerDimensions(new Dimension(16, 16));
        shape.setLayoutManager(new BorderLayout());
        shape.add(nameLabel, BorderLayout.CENTER);

        RectangleFigure topSection = new RectangleFigure();
        topSection.setOutline(false);
        topSection.setFill(false);
        shape.add(topSection, BorderLayout.TOP);
        BorderLayout layout = new BorderLayout();
        topSection.setLayoutManager(layout);
        topSection.setBorder(new MarginBorder(0, 5, 0, 0));
        topSection.add(createErrorIconSection(), BorderLayout.LEFT);

        RectangleFigure bottomSection = new RectangleFigure();
        bottomSection.setOutline(false);
        bottomSection.setFill(false);
        shape.add(bottomSection, BorderLayout.BOTTOM);

        layout = new BorderLayout();
        bottomSection.setLayoutManager(layout);
        bottomSection.setBorder(new MarginBorder(0, 5, 0, 0));
        bottomSection.add(createIconSection(), BorderLayout.LEFT);
        return shape;
    }

    private IFigure createIconSection() {
        RectangleFigure iconSection = new RectangleFigure() {
            @Override
            protected boolean useLocalCoordinates() {
                return true;
            }
        };
        iconSection.setOutline(false);
        iconSection.setFill(false);
        Label iconLabel = new Label();
        iconLabel.setIcon(IpsUIPlugin.getDefault().getImage(image));
        iconLabel.setLocation(new Point(0, 0));
        iconLabel.setSize(new Dimension(20, 20));
        iconSection.add(iconLabel);
        iconSection.setPreferredSize(20, 20);
        return iconSection;
    }

    private IFigure createErrorIconSection() {
        RectangleFigure iconSection = new RectangleFigure() {
            @Override
            protected boolean useLocalCoordinates() {
                return true;
            }
        };
        iconSection.setOutline(false);
        iconSection.setFill(false);
        Label iconLabel = new Label();
        iconLabel.setIcon(IpsPlugin.getDefault().getImage("size8/ErrorMessage.gif")); //$NON-NLS-1$
        iconLabel.setLocation(new Point(0, 0));
        iconLabel.setSize(new Dimension(20, 20));
        iconSection.add(iconLabel);
        iconSection.setPreferredSize(new Dimension(20, 20));
        errorDisplay = iconLabel;
        return iconSection;
    }

    @Override
    protected void showError(MessageList msgList) {
        if (!msgList.isEmpty()) {
            errorDisplay.setVisible(true);
            return;
        }
        errorDisplay.setVisible(false);
    }
}
