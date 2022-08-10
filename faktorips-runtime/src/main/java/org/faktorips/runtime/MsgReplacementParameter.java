/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime;

import java.io.Serializable;

/**
 * When creating a message the text might be created by replacing parameters (or placeholders) with
 * concrete values, e.g. "The sum insured must be at least {minSumInsured}." where {minSumInsured}
 * is replaced with the current minimum e.g. 200 Euro. If you need to represent the user a different
 * text, you need the actual value for the parameter. To archieve this the message holds the
 * parameters along with their actual value.
 * <p>
 * The following are scenarios where you might need to present a different text for a message:
 * <ul>
 * <li>You have limited space available for the text, for example if your display is a
 * terminal.</li>
 * <li>You present the text to a different user group, e.g. internet users instead of your
 * backoffice employees.</li>
 * </ul>
 * 
 * @author Jan Ortmann
 */
public class MsgReplacementParameter implements Serializable {

    private static final long serialVersionUID = -4588558762246019241L;

    private String name;
    private Object value;

    /**
     * Creates a new parameter value with name and value.
     * 
     * @throws NullPointerException if paramName is null.
     */
    public MsgReplacementParameter(String paramName, Object paramValue) {
        if (paramName == null) {
            throw new NullPointerException();
        }
        name = paramName;
        value = paramValue;
    }

    /**
     * Returns the parameter's name. This method never returns <code>null</code>.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the parameter's value.
     */
    public Object getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MsgReplacementParameter)) {
            return false;
        }
        MsgReplacementParameter other = (MsgReplacementParameter)o;
        return name.equals(other.name)
                && ((value == null && other.value == null) || (value != null && value.equals(other.value)));
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return name + "=" + value;
    }

}
