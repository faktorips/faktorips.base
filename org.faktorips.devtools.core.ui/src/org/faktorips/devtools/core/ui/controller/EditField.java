/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controller;

import org.eclipse.swt.widgets.Control;
import org.faktorips.devtools.core.ui.controller.fields.ValueChangeListener;
import org.faktorips.runtime.MessageList;

/**
 * Interface that gives generic access to controls via the methods getValue() and setValue(T
 * newValue) and allows to listen for changes of the value shown in the control.
 * <p>
 * This interface is necessary to build a generic data binding framework as the method to get and
 * set a value in a SWT control differ slightly from control to control.
 * 
 * @param <T> represents the type that is written to the model. That is not the same data type as
 *            this component should show. e.g. an edit field may format an decimal value and let the
 *            user enter decimal values but in the model we only write String values. In this case T
 *            would be String.
 */
public interface EditField<T> {

    /**
     * Message code to indicate an edit field's underlying control does not contain information that
     * is parsable to the value of the expected datatype.
     */
    String INVALID_VALUE = "EditFieldContainsInvalidValue"; //$NON-NLS-1$

    /**
     * Returns the control this is a helper for.
     */
    Control getControl();

    /**
     * Returns the value shown in the control.
     */
    T getValue();

    /**
     * Sets the value shown in the control.
     */
    void setValue(T newValue);

    /**
     * Sets the value shown in the control.
     */
    void setValue(T newValue, boolean triggerValueChanged);

    /**
     * Returns the controls content as string or text value.
     */
    String getText();

    /**
     * Sets the controls content as string or text value.
     */
    void setText(String newText);

    /**
     * Inserts the text in the control.
     */
    void insertText(String text);

    /**
     * Selects all the text in the receiver.
     */
    void selectAll();

    /**
     * Returns true if the control's content can be returned as an instance of the class, this is an
     * edit contrl for.
     */
    boolean isTextContentParsable();

    /**
     * Sets the messages for the control.
     */
    void setMessages(MessageList list);

    /**
     * Adds the value change listener.
     */
    boolean addChangeListener(ValueChangeListener listener);

    /**
     * Removes the value change listener.
     */
    boolean removeChangeListener(ValueChangeListener listener);

}
