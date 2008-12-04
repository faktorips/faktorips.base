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

import java.util.List;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;


public interface IBusinessFunction extends IIpsObject {

    public static final String PROPERTY_PARAMETER_RECTANGLE_SIZE = "ParameterRectangleSize";
    public static final String PROPERTY_PARAMETER_RECTANGLE_LOCATION = "ParameterRectangleLocation";
    public static final String PROPERTY_BFELEMENTS = "BFElements";
    public static final String PROPERTY_BFELEMENT_ADDED = "BFElementAdded";
    public static final String PROPERTY_BFELEMENT_REMOVED = "BFElementRemoved";
    public static final String PROPERTY_CONTROLFLOWS = "ControlFlows";
    public static final String XML_TAG = "BusinessFunction";

    public static final String MSGCODE_PREFIX = "BUSINESSFUNCTION-";
    public static final String MSGCODE_START_SINGLE_OCCURRENCE = MSGCODE_PREFIX + "startSingleOccurrence";
    public static final String MSGCODE_END_SINGLE_OCCURRENCE = MSGCODE_PREFIX + "endSingleOccurrence";
    public static final String MSGCODE_ELEMENT_NAME_COLLISION = MSGCODE_PREFIX + "elementNameCollision";
    public static final String MSGCODE_START_DEFINITION_MISSING = MSGCODE_PREFIX + "startDefinitionMissing";
    public static final String MSGCODE_END_DEFINITION_MISSING = MSGCODE_PREFIX + "endDefinitionMissing";
    public static final String MSGCODE_NOT_CONNECTED_WITH_START = MSGCODE_PREFIX + "notConnectedWithStart";
    public static final String MSGCODE_NOT_CONNECTED_WITH_END = MSGCODE_PREFIX + "notConnectedWithEnd";
    
    public IControlFlow newControlFlow();
    
    public List<IControlFlow> getControlFlows();
    
    public IControlFlow getControlFlow(int id);
    
    public IBFElement newSimpleBFElement(BFElementType type, Point location);
    
    public IActionBFE newOpaqueAction(Point location);
    
    public IActionBFE newMethodCallAction(Point location);
    
    public IActionBFE newBusinessFunctionCallAction(Point location);
    
    public IDecisionBFE newDecision(Point location);
    
    public IParameterBFE newParameter();
    
    public List<IParameterBFE> getParameterBFEs();

    public IParameterBFE getParameterBFE(String name);
    
    public List<IBFElement> getBFElements();
    
    public IBFElement getBFElement(Integer id);
    
    public Dimension getParameterRectangleSize();
    
    public void setParameterRectangleSize(Dimension parameterRectangleSize);
    
    public Point getParameterRectangleLocation();
    
    public IBFElement getStart();

    public IBFElement getEnd();
    
    public List<IBFElement> getBFElementsWithoutParameters();

}
