/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.model.bf;

import java.util.List;

import org.eclipse.draw2d.Bendpoint;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;

/**
 * Business function elements are connected via control flows which are represented by this
 * interface. A control flow has therefore a source and a target elements.
 * <p>
 * If the control flow starts from a decision element a condition value can be assigned to it. The
 * value must be parsable with respect to the data type specified on the decision element.
 * <p>
 * Control flows can also have graphical bend points which mark the position where the graphical
 * line that represents a control flow changes its direction unsteadily.
 * 
 * @author Peter Erzberger
 */
public interface IControlFlow extends IIpsObjectPart {

    public final static String XML_TAG = "ControlFlow"; //$NON-NLS-1$
    public static final String PROPERTY_BENDPOINT = "bendPoint"; //$NON-NLS-1$
    public static final String PROPERTY_TARGET = "target"; //$NON-NLS-1$
    public static final String PROPERTY_SOURCE = "source"; //$NON-NLS-1$
    public static final String PROPERTY_CONDITION_VALUE = "conditionValue"; //$NON-NLS-1$

    public static final String MSGCODE_PREFIX = "DECISIONBFE-"; //$NON-NLS-1$
    public static final String MSGCODE_VALUE_NOT_SPECIFIED = MSGCODE_PREFIX + "valueNotSpecified"; //$NON-NLS-1$
    public static final String MSGCODE_VALUE_NOT_VALID = MSGCODE_PREFIX + "valueNotValid"; //$NON-NLS-1$
    public static final String MSGCODE_DUBLICATE_VALUES = MSGCODE_PREFIX + "duplicateValues"; //$NON-NLS-1$

    /**
     * The business function to which this control flow belongs to.
     */
    public IBusinessFunction getBusinessFunction();

    /**
     * Returns the source element of this control flow.
     */
    public IBFElement getSource();

    /**
     * Sets the source element of this control flow.
     */
    public void setSource(IBFElement source);

    /**
     * Returns the target element of this control flow.
     */
    public IBFElement getTarget();

    /**
     * Sets the target element of this control flow.
     */
    public void setTarget(IBFElement target);

    /**
     * Sets a bend point at the indexed position.
     */
    public void setBendpoint(int index, Bendpoint bendpoint);

    /**
     * Returns an ordered list of the bend points of this control flow.
     */
    public List<Bendpoint> getBendpoints();

    /**
     * Adds a bend point at the index position.
     */
    public void addBendpoint(int index, Bendpoint bendpoint);

    /**
     * Removes a bend point at the indexed position.
     */
    public void removeBendpoint(int index);

    /**
     * Returns the condition value of this control flow. The value is only relevant for control flow
     * that start from a decision element. The value must then be parsable with respect to the data
     * type specified for the decision element.
     */
    public String getConditionValue();

    /**
     * Sets the condition value of this control flow.
     * 
     * @see #getConditionValue()
     */
    public void setConditionValue(String value);

}
