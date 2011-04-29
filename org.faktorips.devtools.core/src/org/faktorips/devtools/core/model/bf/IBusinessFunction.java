/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.model.bf;

import java.util.List;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;

/**
 * This interface describes a business function. A business function is a course grained definition
 * of a function focusing on the business relevant steps leaving out technical imponderabilities.
 * Elements of a business functions are start, end, actions, decision, merge. The connection between
 * two elements is called a control flow.
 * 
 * @author Peter Erzberger
 */
public interface IBusinessFunction extends IIpsObject {

    public static final String PROPERTY_PARAMETER_RECTANGLE_SIZE = "ParameterRectangleSize"; //$NON-NLS-1$
    public static final String PROPERTY_PARAMETER_RECTANGLE_LOCATION = "ParameterRectangleLocation"; //$NON-NLS-1$
    public static final String PROPERTY_BFELEMENTS = "BFElements"; //$NON-NLS-1$
    public static final String PROPERTY_BFELEMENT_ADDED = "BFElementAdded"; //$NON-NLS-1$
    public static final String PROPERTY_BFELEMENT_REMOVED = "BFElementRemoved"; //$NON-NLS-1$
    public static final String PROPERTY_CONTROLFLOWS = "ControlFlows"; //$NON-NLS-1$
    public static final String XML_TAG = "BusinessFunction"; //$NON-NLS-1$

    public static final String MSGCODE_PREFIX = "BUSINESSFUNCTION-"; //$NON-NLS-1$
    public static final String MSGCODE_START_SINGLE_OCCURRENCE = MSGCODE_PREFIX + "startSingleOccurrence"; //$NON-NLS-1$
    public static final String MSGCODE_END_SINGLE_OCCURRENCE = MSGCODE_PREFIX + "endSingleOccurrence"; //$NON-NLS-1$
    public static final String MSGCODE_ELEMENT_NAME_COLLISION = MSGCODE_PREFIX + "elementNameCollision"; //$NON-NLS-1$
    public static final String MSGCODE_START_DEFINITION_MISSING = MSGCODE_PREFIX + "startDefinitionMissing"; //$NON-NLS-1$
    public static final String MSGCODE_END_DEFINITION_MISSING = MSGCODE_PREFIX + "endDefinitionMissing"; //$NON-NLS-1$
    public static final String MSGCODE_NOT_CONNECTED_WITH_START = MSGCODE_PREFIX + "notConnectedWithStart"; //$NON-NLS-1$
    public static final String MSGCODE_NOT_CONNECTED_WITH_END = MSGCODE_PREFIX + "notConnectedWithEnd"; //$NON-NLS-1$

    /**
     * Creates and returns a control flow and adds it to the set of control flows that belongs to
     * this business function.
     */
    public IControlFlow newControlFlow();

    /**
     * Returns all control flow of this business function.
     */
    public List<IControlFlow> getControlFlows();

    /**
     * Returns the control flow with the specified id or <code>null</code> if none is found.
     */
    public IControlFlow getControlFlow(String id);

    public IBFElement newStart(Point location);

    public IBFElement newEnd(Point location);

    public IBFElement newMerge(Point location);

    /**
     * Creates and returns a in line action and adds it to the set of actions that belong to this
     * business function.
     * 
     * @param location the initial graphical location of the created action
     */
    public IActionBFE newOpaqueAction(Point location);

    /**
     * Creates and returns a method call action and adds it to the set of actions that belong to
     * this business function.
     * 
     * @param location the initial graphical location of the created action
     */
    public IActionBFE newMethodCallAction(Point location);

    /**
     * Creates and returns a business function call action and adds it to the set of actions that
     * belong to this business function.
     * 
     * @param location the initial graphical location of the created action
     */
    public IActionBFE newBusinessFunctionCallAction(Point location);

    /**
     * Creates and returns a decision element and adds it to the set of elements that belong to this
     * business function.
     * 
     * @param location the initial graphical location of the created action
     */
    public IDecisionBFE newDecision(Point location);

    /**
     * Creates and returns a method call decision element and adds it to the set of elements that
     * belong to this business function.
     * 
     * @param location the initial graphical location of the created action
     */
    public IDecisionBFE newMethodCallDecision(Point location);

    /**
     * Creates and returns a parameter element and adds it to the set of elements that belong to
     * this business function.
     */
    public IParameterBFE newParameter();

    /**
     * Returns the parameters of this business function.
     */
    public List<IParameterBFE> getParameterBFEs();

    /**
     * Returns the parameter with the specified name and <code>null</code> if none is found.
     */
    public IParameterBFE getParameterBFE(String name);

    /**
     * Returns all business function elements.
     */
    public List<IBFElement> getBFElements();

    /**
     * Returns the business function element with the specified id and <code>null</code> if none is
     * found.
     */
    public IBFElement getBFElement(String id);

    /**
     * Return the size of the graphical element representing the parameters of this business
     * function.
     */
    public Dimension getParameterRectangleSize();

    /**
     * Sets the size of the graphical element representing the parameters of this business function.
     */
    public void setParameterRectangleSize(Dimension parameterRectangleSize);

    /**
     * Return the location of the graphical element representing the parameters of this business
     * function.
     */
    public Point getParameterRectangleLocation();

    /**
     * Returns the start element.
     */
    public IBFElement getStart();

    /**
     * Returns the end element.
     */
    public IBFElement getEnd();

    /**
     * Returns all business function elements except for the parameters.
     */
    public List<IBFElement> getBFElementsWithoutParameters();

}
