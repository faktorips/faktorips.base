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
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.model.bf.IActionBFE;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.OverlayIcons;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.MessageList;

/**
 * A specialization of {@link NodeEditPart} that creates the figure for an action, creates the
 * connection anchores and implements the error display.
 * 
 * @author Peter Erzberger
 */
public abstract class ActionEditPart extends NodeEditPart {

    private ImageDescriptor imageDescriptor;

    private ResourceManager resourceManager;

    private IFigure errorDisplay;

    public ActionEditPart(ImageDescriptor imageDescriptor) {
        ArgumentCheck.notNull(imageDescriptor, this);
        this.imageDescriptor = imageDescriptor;
        resourceManager = new LocalResourceManager(JFaceResources.getResources());
    }

    @Override
    public void activate() {
        super.activate();
    }

    @Override
    public void deactivate() {
        // if edit part is reactivated, the images will be allocated again
        resourceManager.dispose();
        super.deactivate();
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
        iconLabel.setIcon((Image)resourceManager.get(imageDescriptor));
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
        iconLabel.setIcon(IpsUIPlugin.getImageHandling().getImage(OverlayIcons.ERROR_OVR_DESC));
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
