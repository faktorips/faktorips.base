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

import java.util.Iterator;
import java.util.List;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FreeformLayer;
import org.eclipse.draw2d.FreeformLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.gef.editpolicies.RootComponentEditPolicy;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.ContentsChangeListener;
import org.faktorips.devtools.core.model.bf.IBFElement;
import org.faktorips.devtools.core.model.bf.IBusinessFunction;

/**
 * The edit part of the business function object.
 * 
 * @author Peter Erzberger
 */
public class BusinessFunctionEditPart extends AbstractGraphicalEditPart implements ContentsChangeListener {

    @Override
    protected IFigure createFigure() {
        Figure f = new FreeformLayer();
        f.setLayoutManager(new FreeformLayout());
        f.setBorder(new MarginBorder(5));
        return f;
    }

    @Override
    protected void createEditPolicies() {
        // this policy needs to be set for the root edit part to guarantee that is will be not
        // destroyed
        installEditPolicy(EditPolicy.NODE_ROLE, null);
        installEditPolicy(EditPolicy.GRAPHICAL_NODE_ROLE, null);
        installEditPolicy(EditPolicy.SELECTION_FEEDBACK_ROLE, null);
        installEditPolicy(EditPolicy.COMPONENT_ROLE, new RootComponentEditPolicy());
        installEditPolicy(EditPolicy.LAYOUT_ROLE, new BusinessFunctionXYLayoutEditPolicy());
    }

    /**
     * Returns the business function which is the model object of this edit part.
     */
    public IBusinessFunction getBusinessFunction() {
        return (IBusinessFunction)getModel();
    }

    /**
     * Returns all {@link IBFElement} except for the parameters.
     */
    @Override
    protected List<IBFElement> getModelChildren() {
        return getBusinessFunction().getBFElementsWithoutParameters();
    }

    /**
     * Registers itself as {@link ContentsChangeListener} to the ips model.
     */
    @Override
    public void activate() {
        if (isActive()) {
            return;
        }
        super.activate();
        getBusinessFunction().getIpsModel().addChangeListener(this);
    }

    /**
     * In addition to the super class method behavior special treatment is taken for the parameters
     * of a business function since parameters are all displayed in one figure.
     */
    @SuppressWarnings("unchecked")
    @Override
    public void refreshChildren() {
        List<EditPart> childs = getChildren();
        for (Iterator<EditPart> it = childs.iterator(); it.hasNext();) {
            EditPart editPart = it.next();
            if (editPart instanceof ParameterEditPart) {
                it.remove();
                continue;
            }
            editPart.refresh();
        }
        super.refreshChildren();
        EditPart editPart = new ParameterEditPart();
        editPart.setParent(this);
        editPart.setModel(getBusinessFunction());
        addChild(editPart, getChildren().size());
    }

    /**
     * Unregisters itself as {@link ContentsChangeListener} to the ips model.
     */
    @Override
    public void deactivate() {
        if (!isActive()) {
            return;
        }
        super.deactivate();
        getBusinessFunction().getIpsModel().removeChangeListener(this);
    }

    /**
     * Updates the children of this edit part when an
     * <code>ContentChangeEvent.TYPE_PART_ADDED</code> or
     * <code>ContentChangeEvent.TYPE_PART_REMOVED</code> occurs.
     */
    @Override
    public void contentsChanged(ContentChangeEvent event) {
        if (!event.getIpsSrcFile().equals(getBusinessFunction().getIpsSrcFile())) {
            return;
        }
        if (event.getEventType() == ContentChangeEvent.TYPE_PART_ADDED
                || event.getEventType() == ContentChangeEvent.TYPE_PART_REMOVED) {
            refreshChildren();
        }
    }

}
