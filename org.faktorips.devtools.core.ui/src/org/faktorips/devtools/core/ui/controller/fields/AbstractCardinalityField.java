/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controller.fields;

import org.apache.commons.lang.StringUtils;

/**
 * Base-class to represent and edit cardinality values (which means int-values and the asterisk
 * (*)). The askerisk is mapped to Integer.MAX_VALUE on object conversions and vice versa.
 * 
 * @author Thorsten Guenther
 */
public abstract class AbstractCardinalityField extends DefaultEditField<Integer> {

    @Override
    public Integer parseContent() {
        String text = getText();
        if ("*".equals(text)) { //$NON-NLS-1$
            return Integer.valueOf(Integer.MAX_VALUE);
        } else {
            return Integer.valueOf(text);
        }
    }

    @Override
    public void setValue(Integer newValue) {
        if (newValue == null) {
            setText(StringUtils.EMPTY);
        } else if (newValue.intValue() == Integer.MAX_VALUE) {
            setText("*"); //$NON-NLS-1$
        } else {
            setText(newValue.toString());
        }
    }

    /**
     * Method to set the text unmodified to the underlying control
     */
    abstract void setTextInternal(String newText);

    @Override
    public void setText(String newText) {
        try {
            Integer value = Integer.valueOf(newText);
            if (value.intValue() == Integer.MAX_VALUE) {
                setTextInternal("*"); //$NON-NLS-1$
            } else {
                setTextInternal(value.toString());
            }
        } catch (NumberFormatException e) {
            setTextInternal(newText);
        }
    }
}
