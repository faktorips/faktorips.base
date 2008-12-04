package org.faktorips.devtools.bf.ui.edit;

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
import org.faktorips.devtools.core.model.bf.IParameterBFE;

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

    public IBusinessFunction getBusinessFunction() {
        return (IBusinessFunction)getModel();
    }

    @Override
    @SuppressWarnings("unchecked")
    protected List getModelChildren() {
        List<IBFElement> elements = getBusinessFunction().getBFElements();
        for (Iterator it = elements.iterator(); it.hasNext();) {
            IBFElement element = (IBFElement)it.next();
            if(element instanceof IParameterBFE){
                it.remove();
            }
        }
        return elements;
    }
    
    @Override
    public void activate() {
        if (isActive())
            return;
        super.activate();
        getBusinessFunction().getIpsModel().addChangeListener(this);
    }

    @SuppressWarnings("unchecked")
    public void refreshChildren(){
        List<EditPart> childs = getChildren();
        for (Iterator<EditPart> it = childs.iterator(); it.hasNext();) {
            EditPart editPart = it.next();
            if(editPart instanceof ParameterEditPart){
                it.remove();
            }
        }
        super.refreshChildren();
        EditPart editPart = new ParameterEditPart();
        editPart.setParent(this);
        editPart.setModel(getBusinessFunction());
        addChild(editPart, getChildren().size());
    }
    
    @Override
    public void deactivate() {
        if (!isActive())
            return;
        super.deactivate();
        getBusinessFunction().getIpsModel().removeChangeListener(this);
    }

    public void contentsChanged(ContentChangeEvent event) {
        if (!event.getIpsSrcFile().equals(getBusinessFunction().getIpsSrcFile())) {
            return;
        }
        if(event.getEventType() == ContentChangeEvent.TYPE_PART_ADDED ||
                event.getEventType() == ContentChangeEvent.TYPE_PART_REMOVED){
            refreshChildren();
        }
    }

}
