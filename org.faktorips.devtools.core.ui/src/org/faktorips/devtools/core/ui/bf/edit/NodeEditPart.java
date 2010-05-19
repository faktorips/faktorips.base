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

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.ConnectionAnchor;
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

/**
 * The base edit part for all edit parts of this package. It provides an anchor for incoming and one
 * for outgoing connections. It registers itself as {@link ContentsChangeListener} to the faktor ips
 * model so that it refreshs its ui components like its figure and connects according to content
 * change events. It provides a label that can show the name and the error state of the associated
 * model object.
 * 
 * @author Peter Erzberger
 */
public abstract class NodeEditPart extends AbstractGraphicalEditPart implements org.eclipse.gef.NodeEditPart,
        ContentsChangeListener {

    protected Label nameLabel;
    private ConnectionAnchor sourceConnectionAnchor;
    private ConnectionAnchor targetConnectionAnchor;

    /**
     * Sets the source connection anchor.
     */
    protected final void setSourceConnectionAnchor(ConnectionAnchor anchor) {
        this.sourceConnectionAnchor = anchor;
    }

    /**
     * Sets the target connection anchor.
     */
    protected final void setTargetConnectionAnchor(ConnectionAnchor anchor) {
        this.targetConnectionAnchor = anchor;
    }

    /**
     * Returns the {@link IBFElement} that is assigned to this edit part by the
     * {@link BusinessFunctionEditPartFactory}.
     */
    public IBFElement getBFElement() {
        return (IBFElement)getModel();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final IFigure createFigure() {
        nameLabel = new Label();
        nameLabel.setForegroundColor(ColorConstants.black);
        nameLabel.setBorder(new MarginBorder(0, 5, 0, 5));
        return createFigureInternal();
    }

    /**
     * Template method that is called by createFigure().
     */
    protected abstract IFigure createFigureInternal();

    /**
     * {@inheritDoc}
     */
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

    /**
     * Is called by refreshVisuals(). It requests the display string of the {@link IBFElement} and
     * displays it on the name label. It calls the validate method of the element and delegates the
     * message list to the showError(MessageList) method.
     * <p/>
     * Subclass can override this method to do more model updating.
     */
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

    /**
     * Empty implementation. Subclasses implement the error display with this method.
     */
    protected void showError(MessageList msgList) {
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

    /**
     * {@inheritDoc}
     */
    @Override
    public ConnectionAnchor getSourceConnectionAnchor(ConnectionEditPart connection) {
        return sourceConnectionAnchor;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ConnectionAnchor getSourceConnectionAnchor(Request request) {
        return sourceConnectionAnchor;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ConnectionAnchor getTargetConnectionAnchor(ConnectionEditPart connection) {
        return targetConnectionAnchor;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ConnectionAnchor getTargetConnectionAnchor(Request request) {
        return targetConnectionAnchor;
    }

    @Override
    protected void createEditPolicies() {
        installEditPolicy(EditPolicy.COMPONENT_ROLE, new NodeComponentEditPolicy());
        installEditPolicy(EditPolicy.GRAPHICAL_NODE_ROLE, new NodeEditPolicy());
    }

    /**
     * Registers this edit part as {@link ContentsChangeListener}.
     */
    @Override
    public void activate() {
        if (isActive()) {
            return;
        }
        super.activate();
        getBFElement().getIpsModel().addChangeListener(this);
    }

    /**
     * Unregisters this edit part as {@link ContentsChangeListener}.
     */
    @Override
    public void deactivate() {
        if (!isActive()) {
            return;
        }
        super.deactivate();
        getBFElement().getIpsModel().removeChangeListener(this);
    }

    /**
     * Implementation of the {@link ContentsChangeListener} interface. It updates the visuals and
     * connections when a <code>ContentChangeEvent.TYPE_PROPERTY_CHANGED</code> occurs.
     */
    @Override
    public void contentsChanged(ContentChangeEvent event) {
        if (event.isAffected(getBFElement()) && event.getEventType() == ContentChangeEvent.TYPE_PROPERTY_CHANGED) {
            refreshVisuals();
            refreshTargetConnections();
            refreshSourceConnections();
        }
    }

}
