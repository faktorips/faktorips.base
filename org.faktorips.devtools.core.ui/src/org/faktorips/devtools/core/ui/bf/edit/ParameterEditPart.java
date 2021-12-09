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

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.draw2d.BorderLayout;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.GridLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.DefaultLabelProvider;
import org.faktorips.devtools.model.ContentChangeEvent;
import org.faktorips.devtools.model.ContentsChangeListener;
import org.faktorips.devtools.model.bf.IBusinessFunction;
import org.faktorips.devtools.model.bf.IParameterBFE;
import org.faktorips.devtools.model.bf.Size;
import org.faktorips.devtools.model.exception.CoreRuntimeException;

/**
 * The edit part for the parameter business function element. Since there is only one figure that
 * displays all parameters of a business function this edit part cannot be created via the
 * {@link BusinessFunctionEditPartFactory} like all other edit parts. Instead it is created within
 * the <code>refreshchildren()</code> method of the {@link BusinessFunctionEditPart}. The
 * rectangular figure that displays all parameters is always position in the upper left corner of
 * the business function editor.
 * 
 * @author Peter Erzberger
 * @deprecated for removal since 21.6
 */
@Deprecated
public class ParameterEditPart extends AbstractGraphicalEditPart implements ContentsChangeListener {

    private RectangleFigure lowerRectangle;
    private DefaultLabelProvider labelProvider = new DefaultLabelProvider();
    private SelectionListener viewportScolllistener;

    @Override
    public void activate() {
        if (isActive()) {
            return;
        }
        super.activate();
        /*
         * unfortunately this listener is neccessary to ensure that the parameter rectangle is
         * always in the upper left corner while scrolling the viewport of the editor
         */
        viewportScolllistener = new SelectionListener() {
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                // Nothing to do in this case
            }

            @Override
            public void widgetSelected(SelectionEvent e) {
                refreshVisuals();
            }
        };
        ((FigureCanvas)getViewer().getControl()).getVerticalBar().addSelectionListener(viewportScolllistener);
        ((FigureCanvas)getViewer().getControl()).getHorizontalBar().addSelectionListener(viewportScolllistener);
        getBusinessFunction().getIpsModel().addChangeListener(this);
    }

    @Override
    public void deactivate() {
        if (!isActive()) {
            return;
        }
        super.deactivate();
        ((FigureCanvas)getViewer().getControl()).getVerticalBar().removeSelectionListener(viewportScolllistener);
        ((FigureCanvas)getViewer().getControl()).getHorizontalBar().removeSelectionListener(viewportScolllistener);
        getBusinessFunction().getIpsModel().removeChangeListener(this);
    }

    /**
     * Empty implementation.
     */
    @Override
    protected void createEditPolicies() {
        // Empty implementation
    }

    @Override
    protected IFigure createFigure() {
        RectangleFigure figure = new RectangleFigure();
        BorderLayout layout = new BorderLayout();
        figure.setLayoutManager(layout);

        RectangleFigure upperRectangle = new RectangleFigure();
        layout = new BorderLayout();
        upperRectangle.setLayoutManager(layout);
        figure.add(upperRectangle, BorderLayout.TOP);

        Label parameterLabel = new Label(Messages.ParameterEditPart_Parameters);
        parameterLabel.setForegroundColor(ColorConstants.black);
        MarginBorder border = new MarginBorder(10);
        parameterLabel.setBorder(border);
        upperRectangle.add(parameterLabel, BorderLayout.CENTER);

        lowerRectangle = new RectangleFigure();
        GridLayout lowerRectangleLayout = new GridLayout(1, false);
        lowerRectangle.setLayoutManager(lowerRectangleLayout);
        figure.add(lowerRectangle, BorderLayout.CENTER);

        return figure;
    }

    private IBusinessFunction getBusinessFunction() {
        return (IBusinessFunction)getModel();
    }

    @Override
    protected void refreshVisuals() {
        Size size = getBusinessFunction().getParameterRectangleSize();
        Location loc = getBusinessFunction().getParameterRectangleLocation();
        Rectangle r = new Rectangle(new Point(loc.getX(), loc.getY()),
                new Dimension(size.getWidth(), size.getHeight()));
        // TODO consider if the foreground color is persisted by the model if not the setting of
        // of the color needs to be placed somewhere else
        getFigure().setForegroundColor(ColorConstants.lightGray);
        getFigure().translateToRelative(r);
        ((GraphicalEditPart)getParent()).setLayoutConstraint(this, getFigure(), r);
        try {
            updateParameters();
        } catch (CoreException e) {
            IpsPlugin.log(e);
        }
    }

    private void updateParameters() throws CoreRuntimeException {
        List<IParameterBFE> parameters = getBusinessFunction().getParameterBFEs();
        lowerRectangle.removeAll();
        for (IParameterBFE parameterBFE : parameters) {
            Label pLabel = new Label(parameterBFE.getDisplayString());
            pLabel.setForegroundColor(ColorConstants.black);
            Datatype datatype = parameterBFE.findDatatype();
            pLabel.setIcon(labelProvider.getImage(datatype));
            lowerRectangle.add(pLabel);
        }
    }

    @Override
    public void contentsChanged(ContentChangeEvent event) {
        if (event.isAffected(getBusinessFunction())) {
            refreshVisuals();
        }
    }
}
