/***************************************************************************************************
 * Copyright (c) 2005-2008 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 * 
 **************************************************************************************************/

package org.faktorips.devtools.core.model.bf;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.IpsPlugin;

public enum BFElementType {

    ACTION_INLINE("inlineAction", "Inline Action", "OpaqueAction.gif") {
        @Override
        public IBFElement newBFElement(IBusinessFunction businessFunction, Point location) {
            return businessFunction.newOpaqueAction(location);
        }
    },
    ACTION_METHODCALL("methodCallAction", "Method Call Action", "CallOperationAction.gif") {
        @Override
        public IBFElement newBFElement(IBusinessFunction businessFunction, Point location) {
            return businessFunction.newMethodCallAction(location);
        }
    },
    ACTION_BUSINESSFUNCTIONCALL("businessFunctionCallAction", "Business Function Call Action", "CallBehaviorAction.gif") {
        @Override
        public IBFElement newBFElement(IBusinessFunction businessFunction, Point location) {
            return businessFunction.newBusinessFunctionCallAction(location);
        }
    },
    DECISION("decision", "Decision", "DecisionNode.gif") {
        @Override
        public IBFElement newBFElement(IBusinessFunction businessFunction, Point location) {
            return businessFunction.newDecision(location);
        }
    },
    MERGE("merge", "Merge", "MergeNode.gif") {
        @Override
        public IBFElement newBFElement(IBusinessFunction businessFunction, Point location) {
            return businessFunction.newSimpleBFElement(BFElementType.MERGE, location);
        }
    },
    END("end", "End", "ActivityFinalNode.gif") {
        @Override
        public IBFElement newBFElement(IBusinessFunction businessFunction, Point location) {
            IBFElement element = businessFunction.newSimpleBFElement(BFElementType.END, location);
            element.setSize(new Dimension(30, 30));
            return element;
        }
    },
    START("start", "Start", "InitialNode.gif") {
        @Override
        public IBFElement newBFElement(IBusinessFunction businessFunction, Point location) {
            IBFElement element = businessFunction.newSimpleBFElement(BFElementType.START, location);
            element.setSize(new Dimension(30, 30));
            return element;
        }
    },
    PARAMETER("parameter", "Parameter", null) {
        @Override
        public IBFElement newBFElement(IBusinessFunction businessFunction, Point location) {
            return businessFunction.newParameter();
        }
    };

    private BFElementType(String id, String name, String imageName) {
        this.name = name;
        this.id = id;
        this.imageName = imageName;
    }

    private String name;
    private String id;
    private ImageDescriptor imageDescriptor;
    private String imageName;

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public ImageDescriptor getImageDescriptor(){
        if(imageDescriptor == null){
            imageDescriptor = IpsPlugin.getDefault().getImageDescriptor("/obj16/" + imageName);
        }
        return imageDescriptor;
    }
    
    public Image getImage(){
        return IpsPlugin.getDefault().getImage(getImageDescriptor());
    }
    
    public abstract IBFElement newBFElement(IBusinessFunction businessFunction, Point location);

    public final static BFElementType getType(String id) {
        if (id.equals(ACTION_INLINE.id)) {
            return ACTION_INLINE;
        }
        if (id.equals(ACTION_BUSINESSFUNCTIONCALL.id)) {
            return ACTION_BUSINESSFUNCTIONCALL;
        }
        if (id.equals(ACTION_METHODCALL.id)) {
            return ACTION_METHODCALL;
        }
        if (id.equals(DECISION.id)) {
            return DECISION;
        }
        if (id.equals(MERGE.id)) {
            return MERGE;
        }
        if (id.equals(END.id)) {
            return END;
        }
        if (id.equals(START.id)) {
            return START;
        }
        if (id.equals(PARAMETER.id)) {
            return PARAMETER;
        }
        throw new IllegalArgumentException("Unexpected type id: " + id);
    }
}