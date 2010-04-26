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
    @SuppressWarnings("unchecked")
    protected List getModelChildren() {
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
