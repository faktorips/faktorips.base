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

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;
import org.faktorips.devtools.model.bf.BFElementType;
import org.faktorips.devtools.model.bf.IBFElement;
import org.faktorips.devtools.model.bf.IBusinessFunction;
import org.faktorips.devtools.model.bf.IControlFlow;

/**
 * Creates edit parts for the business function and all of its children.
 * 
 * @author Peter Erzberger
 */
public class BusinessFunctionEditPartFactory implements EditPartFactory {

    @Override
    public EditPart createEditPart(EditPart context, Object model) {
        EditPart child = null;

        if (model instanceof IBusinessFunction) {
            child = new BusinessFunctionEditPart();

        } else if (model instanceof IBFElement) {
            IBFElement element = (IBFElement)model;
            if (element.getType().equals(BFElementType.ACTION_BUSINESSFUNCTIONCALL)) {
                child = new CallBusinessFunctionActionEditPart();
            } else if (element.getType().equals(BFElementType.ACTION_INLINE)) {
                child = new InlineActionEditPart();
            } else if (element.getType().equals(BFElementType.ACTION_METHODCALL)) {
                child = new CallMethodActionEditPart();
            } else if (element.getType().equals(BFElementType.DECISION)) {
                child = new DecisionEditPart();
            } else if (element.getType().equals(BFElementType.DECISION_METHODCALL)) {
                child = new CallMethodDecisionEditPart();
            } else if (element.getType().equals(BFElementType.MERGE)) {
                child = new MergeEditPart();
            } else if (element.getType().equals(BFElementType.START)) {
                child = new StartEditPart();
            } else if (element.getType().equals(BFElementType.END)) {
                child = new EndEditPart();
            } else if (element.getType().equals(BFElementType.PARAMETER)) {
                return null;
            }
        } else if (model instanceof IControlFlow) {
            child = new ControlFlowEditPart();
        }
        if (child == null) {
            throw new IllegalArgumentException("No EditPart can be created for the provided model object: " + model); //$NON-NLS-1$
        }
        child.setModel(model);
        return child;
    }

}
