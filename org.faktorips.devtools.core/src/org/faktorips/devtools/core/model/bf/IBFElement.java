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

package org.faktorips.devtools.core.model.bf;

import java.util.List;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectNamingConventions;

/**
 * A business function contains business function elements which are represented by this interface.
 * Types of business function elements are actions, decision, merge, start and end represented by
 * the enumeration {@link BFElementType}. For some elements with additional behavior there exist sub
 * interfaces like action, decision and parameter. With the exception of parameters business
 * function elements can be connected via control flows. For each type of element there apply
 * specific rules about how many control flow can target to respectively start from an element.
 * 
 * @author Peter Erzberger
 */
public interface IBFElement extends IIpsObjectPart {

    public final static String XML_TAG = "BFElement"; //$NON-NLS-1$

    public final static String PROPERTY_TYPE = "type"; //$NON-NLS-1$
    public final static String PROPERTY_LOCATION = "location"; //$NON-NLS-1$
    public final static String PROPERTY_SIZE = "size"; //$NON-NLS-1$
    public final static String PROPERTY_INCOMMING_EDGES = "incommingEdges"; //$NON-NLS-1$
    public final static String PROPERTY_OUTGOING_EDGES = "outgoingEdges"; //$NON-NLS-1$

    // validation message codes
    public static final String MSGCODE_PREFIX = "BFELEMENT-"; //$NON-NLS-1$
    public static final String MSGCODE_NAME_NOT_SPECIFIED = MSGCODE_PREFIX + "nameNotSpecified"; //$NON-NLS-1$
    public static final String MSGCODE_NAME_NOT_VALID = IIpsProjectNamingConventions.INVALID_NAME;

    /**
     * Returns the business function to which this element belongs to.
     */
    public IBusinessFunction getBusinessFunction();

    /**
     * Returns the display string of this element.
     */
    public String getDisplayString();

    /**
     * Returns the location of the graphical representation of this element.
     */
    public Point getLocation();

    /**
     * Sets the location of the graphical representation of this element.
     */
    public void setLocation(Point location);

    /**
     * Returns the size of the graphical representation of this element.
     * 
     * @return
     */
    public Dimension getSize();

    /**
     * Sets the location of the graphical representation of this element.
     */
    public void setSize(Dimension size);

    /**
     * Returns the type of this element.
     */
    public BFElementType getType();

    /**
     * Sets the name of this element. For business function call actions and method call actions the
     * name is not required.
     */
    public void setName(String name);

    /**
     * Adds a control flow to this elements that points to it.
     */
    public void addIncomingControlFlow(IControlFlow controlFlow);

    /**
     * Returns all control flows of this element that point to it. The returned list is a copy and
     * can therefore be modified without corrupting the list that represents the association to the
     * incoming control flows.
     */
    public List<IControlFlow> getIncomingControlFlow();

    /**
     * Removes the specified control flow from the set of incoming control flows.
     */
    public boolean removeIncomingControlFlow(IControlFlow controlFlow);

    /**
     * Adds a control flow to this elements that points from it.
     */
    public void addOutgoingControlFlow(IControlFlow controlFlow);

    /**
     * Returns all control flows of this element that point from it. The returned list is a copy and
     * can therefore be modified without corrupting the list that represents the association of the
     * outgoing control flows.
     */
    public List<IControlFlow> getOutgoingControlFlow();

    /**
     * Removes the specified control flow from the set of outgoing control flows.
     */
    public boolean removeOutgoingControlFlow(IControlFlow controlFlow);

    /**
     * Removes all incoming control flows from this element.
     */
    public void removeAllIncommingControlFlows();

    /**
     * Removes all outgoing control flows from this element.
     */
    public void removeAllOutgoingControlFlows();

    /**
     * Returns all control flows that are connected to this element.
     */
    public List<IControlFlow> getAllControlFlows();
}
