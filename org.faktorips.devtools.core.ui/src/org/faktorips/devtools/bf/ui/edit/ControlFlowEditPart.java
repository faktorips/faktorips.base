package org.faktorips.devtools.bf.ui.edit;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.FigureListener;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.PolylineDecoration;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.editparts.AbstractConnectionEditPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.ContentsChangeListener;
import org.faktorips.devtools.core.model.bf.IControlFlow;
import org.faktorips.util.message.MessageList;

/**
 * The edit part for the control flow model object. In addition to its figure this edit part has a label widget
 * for which it is the controller for. The label positioned to the line figure. 
 * 
 * @author Peter Erzberger
 */
public class ControlFlowEditPart extends AbstractConnectionEditPart implements ContentsChangeListener {

    private CLabel label;
    private Color labelBackground;

    private class LabelPositioner implements FigureListener {

        public void figureMoved(IFigure source) {
            PolylineConnection conn = (PolylineConnection)getFigure();
            PointList points = conn.getPoints();
            Point p1 = points.getFirstPoint();
            Point p2 = points.getPoint(1);
            Point labelLocation = calcuateStartPoint(p1, p2);
            boolean thresholdReached = thresholdReached(p1, p2);
            if (thresholdReached) {
                p1 = transformForth(p1);
                p2 = transformForth(p2);
                labelLocation = transformForth(labelLocation);
            }
            int y = (int)(m(p1, p2) * labelLocation.x + b(m(p1, p2), p1));
            labelLocation = new Point(labelLocation.x, y);
            if (thresholdReached) {
                labelLocation = transformBack(labelLocation);
            }
            conn.translateToAbsolute(labelLocation);
            label.setLocation(labelLocation.x, labelLocation.y);
        }

        private Point calcuateStartPoint(Point p1, Point p2) {
            int x = p1.x - p2.x;
            int y = p1.y - p2.y;
            
            double h = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
            double sinAlfa = Math.abs(y) / h;
            int delta = (int)(0.5 * h);
            if (y <= 0 && x >= 0 && sinAlfa < 0.707) {
                delta = -delta;
            } else if (x >= 0 && y >= 0) {
                delta = -delta;
            } else if (x <= 0 && y >= 0 && sinAlfa > 0.707) {
                delta = -delta;
            }
            return new Point(p1.x + delta, p1.y + delta);
        }

        private boolean thresholdReached(Point p1, Point p2) {
            int x = Math.abs(p1.x - p2.x);
            int y = Math.abs(p1.y - p2.y);
            double h = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
            double sinAlfa = y / h;
            // alfa > 45 degrees
            if (sinAlfa > 0.707) {
                return true;
            }
            return false;
        }

        private Point transformForth(Point p) {
            return new Point(-p.y, p.x);
        }

        private Point transformBack(Point p) {
            return new Point(p.y, -p.x);
        }

        private double m(Point p1, Point p2) {
            return (double)((p1.y - p2.y) / (double)(p1.x - p2.x));
        }

        private double b(double m, Point p1) {
            return (p1.y - (m * p1.x));
        }
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        label.dispose();
        labelBackground.dispose();
    }

    private void setText(String text) {
        label.setText(text);
        if(StringUtils.isEmpty(text)){
            label.setSize(0, 0);
            return;
        }
        org.eclipse.swt.graphics.Point point = label.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        point.x = point.x + 2;
        point.y = point.y + 2;
        label.setSize(point);
    }

    @Override
    protected IFigure createFigure() {
        final PolylineConnection conn = new PolylineConnection();
        Composite viewerComp = (Composite)getViewer().getControl();
        label = new CLabel(viewerComp, SWT.CENTER |SWT.SHADOW_NONE);
        labelBackground = new Color(viewerComp.getDisplay(), new RGB(240, 240, 240));
        label.setBackground(labelBackground);
        PolylineDecoration df = new PolylineDecoration();
        PointList pl = new PointList();
        pl.addPoint(-2, -1);
        pl.addPoint(0, 0);
        pl.addPoint(-2, 1);
        df.setTemplate(pl);
        conn.setForegroundColor(ColorConstants.black);
        conn.setTargetDecoration(df);
        return conn;
    }

    @Override
    protected void createEditPolicies() {
        installEditPolicy(EditPolicy.CONNECTION_ENDPOINTS_ROLE, new ControlFlowEndpointEditPolicy());
        installEditPolicy(EditPolicy.CONNECTION_ROLE, new ControlFlowEditPolicy());
        installEditPolicy(EditPolicy.CONNECTION_BENDPOINTS_ROLE, new ControlFlowBendpointEditPolicy());
    }

    private PolylineConnection getPolylineConnection() {
        return (PolylineConnection)getFigure();
    }

    @Override
    public void activate() {
        super.activate();
        getPolylineConnection().addFigureListener(new LabelPositioner());
        getControlFlow().getIpsModel().addChangeListener(this);
    }

    @Override
    public void deactivate() {
        getControlFlow().getIpsModel().removeChangeListener(this);
        super.deactivate();
    }

    /**
     * Returns the control flow object which is the model object of this edit part.
     */
    public IControlFlow getControlFlow() {
        return (IControlFlow)getModel();
    }

    private void showError(MessageList msgList){
        if(!msgList.isEmpty()){
            label.setImage(IpsPlugin.getDefault().getImage("size8/ErrorMessage.gif")); //$NON-NLS-1$
            label.setSize(label.computeSize(-1, -1));
        } else {
            label.setImage(null);
        }
    }
    
    @Override
    protected void refreshVisuals() {
        getConnectionFigure().setRoutingConstraint(getControlFlow().getBendpoints());
        setText(getControlFlow().getConditionValue());
        try {
            showError(getControlFlow().validate(getControlFlow().getIpsProject()));
        } catch (CoreException e) {
            IpsPlugin.log(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void contentsChanged(ContentChangeEvent event) {
        if (event.isAffected(getControlFlow()) && event.getEventType() == ContentChangeEvent.TYPE_PROPERTY_CHANGED) {
            refreshVisuals();
        }
    }

}
