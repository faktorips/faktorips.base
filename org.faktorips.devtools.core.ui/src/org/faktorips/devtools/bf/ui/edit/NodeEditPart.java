package org.faktorips.devtools.bf.ui.edit;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.ContentsChangeListener;
import org.faktorips.devtools.core.model.bf.IBFElement;
import org.faktorips.util.message.MessageList;

public abstract class NodeEditPart extends AbstractGraphicalEditPart implements org.eclipse.gef.NodeEditPart,
        ContentsChangeListener {

    protected Label nameLabel;
    private ConnectionAnchor sourceConnectionAnchor;
    private ConnectionAnchor targetConnectionAnchor;

    protected final void setSourceConnectionAnchor(ConnectionAnchor anchor) {
        this.sourceConnectionAnchor = anchor;
    }

    protected final void setTargetConnectionAnchor(ConnectionAnchor anchor) {
        this.targetConnectionAnchor = anchor;
    }

    protected abstract void createConnectionAnchor(Figure figure);

    public IBFElement getBFElement() {
        return (IBFElement)getModel();
    }

    public final IFigure createFigure(){
        nameLabel = new Label();
        nameLabel.setForegroundColor(ColorConstants.black);
        nameLabel.setBorder(new MarginBorder(0, 5, 0, 5));
        return createFigureInternal();
    }
    
    protected abstract IFigure createFigureInternal();
    
    @Override
    protected void refreshVisuals() {
        Point loc = getBFElement().getLocation();
        Dimension size = getBFElement().getSize();
        Rectangle r = new Rectangle(loc, size);
        // TODO consider if the foreground color is persisted by the model if not the setting of
        // of the color needs to be placed somewhere else
        getFigure().setForegroundColor(ColorConstants.lightGray);
        refreshVisualsFromModel();
        ((GraphicalEditPart)getParent()).setLayoutConstraint(this, getFigure(), r);
    }

    protected void refreshVisualsFromModel() {
        nameLabel.setText(getBFElement().getDisplayString());
        try {
            MessageList list = getBFElement().getBusinessFunction().validate(
                    getBFElement().getBusinessFunction().getIpsProject());
            MessageList bfeMsgList = list.getMessagesFor(getBFElement());
            showError(bfeMsgList);
        } catch (CoreException e) {
            IpsPlugin.log(e);
        }
    }

    protected void showError(MessageList msgList){
    }
    
    @Override
    @SuppressWarnings("unchecked")
    protected List getModelSourceConnections() {
        return getBFElement().getOutgoingControlFlow();
    }

    @Override
    @SuppressWarnings("unchecked")
    protected List getModelTargetConnections() {
        return getBFElement().getIncomingControlFlow();
    }

    public ConnectionAnchor getSourceConnectionAnchor(ConnectionEditPart connection) {
        return sourceConnectionAnchor;
    }

    public ConnectionAnchor getSourceConnectionAnchor(Request request) {
        return sourceConnectionAnchor;
    }

    public ConnectionAnchor getTargetConnectionAnchor(ConnectionEditPart connection) {
        return targetConnectionAnchor;
    }

    public ConnectionAnchor getTargetConnectionAnchor(Request request) {
        return targetConnectionAnchor;
    }

    @Override
    protected void createEditPolicies() {
        installEditPolicy(EditPolicy.COMPONENT_ROLE, new NodeComponentEditPolicy());
        installEditPolicy(EditPolicy.GRAPHICAL_NODE_ROLE, new NodeEditPolicy());
    }

    @Override
    public void activate() {
        if (isActive())
            return;
        super.activate();
        getBFElement().getIpsModel().addChangeListener(this);
    }

    @Override
    public void deactivate() {
        if (!isActive())
            return;
        super.deactivate();
        getBFElement().getIpsModel().removeChangeListener(this);
    }

    public void contentsChanged(ContentChangeEvent event) {
        if (event.isAffected(getBFElement()) && event.getEventType() == ContentChangeEvent.TYPE_PROPERTY_CHANGED) {
            refreshVisuals();
            refreshTargetConnections();
            refreshSourceConnections();
        }
    }

}
