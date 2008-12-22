package org.faktorips.devtools.core.ui.bf.edit;

import org.eclipse.draw2d.ChopboxAnchor;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.LayoutEditPolicy;
import org.eclipse.gef.editpolicies.NonResizableEditPolicy;
import org.eclipse.gef.requests.CreateRequest;
import org.faktorips.devtools.core.ui.bf.draw2d.EndFigure;
import org.faktorips.util.message.MessageList;

/**
 * An edit part that provides a nonresizable figure for the end business function element.
 * 
 * @author Peter Erzberger
 */
public class EndEditPart extends NodeEditPart {

	@Override
	protected void createEditPolicies() {
		super.createEditPolicies();
		installEditPolicy(EditPolicy.LAYOUT_ROLE, createLayoutEditPolicy());
	}

	private LayoutEditPolicy createLayoutEditPolicy() {
		LayoutEditPolicy lep = new LayoutEditPolicy() {

			protected EditPolicy createChildEditPolicy(EditPart child) {
				EditPolicy result = child
						.getEditPolicy(EditPolicy.PRIMARY_DRAG_ROLE);
				if (result == null) {
					result = new NonResizableEditPolicy();
				}
				return result;
			}

			protected Command getMoveChildrenCommand(Request request) {
				return null;
			}

			protected Command getCreateCommand(CreateRequest request) {
				return null;
			}
		};
		return lep;
	}

	protected void showError(MessageList msgList){
	    EndFigure figure = (EndFigure)getFigure();
	    if(!msgList.isEmpty()){
	        figure.showError(true);
	    } else {
	        figure.showError(false);
	    }
	}
	
	@Override
	protected IFigure createFigureInternal() {
	    Figure figure = new EndFigure();
	    setTargetConnectionAnchor(new ChopboxAnchor(figure));
		return figure;
	}
	
}
