/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.controller;

import org.eclipse.swt.widgets.Control;
import org.faktorips.devtools.core.ui.controller.fields.ValueChangeListener;
import org.faktorips.util.message.MessageList;

/**
 * Interface that gives generic access to controls via the methods getValue() and setValue(Object
 * newValue) and allows to listen for changes of the value shown in the control.
 * <p>
 * This interface is neccessary to build a generic data binding framework as the method to get and
 * set a value in a SWT control differ slightly from control to control.
 */
public interface EditField {

    /**
     * Message code to indicate an edit field's underlying control does not contain information that
     * is parsable to the value of the expected datatype.
     */
    public final static String INVALID_VALUE = "EditFieldContainsInvalidValue"; //$NON-NLS-1$

    /**
     * Returns the control this is a helper for.
     */
    public Control getControl();

    /**
     * Returns the value shown in the control.
     */
    public Object getValue();

    /**
     * Sets the value shown in the control.
     */
    public void setValue(Object newValue);

    /**
     * Sets the value shown in the control.
     */
    public void setValue(Object newValue, boolean triggerValueChanged);

    /**
     * Returns the controls content as string or text value.
     */
    public abstract String getText();

    /**
     * Sets the controls content as string or text value.
     */
    public abstract void setText(String newText);

    /**
     * Inserts the text in the control.
     */
    public abstract void insertText(String text);

    /**
     * Selects all the text in the receiver.
     */
    public abstract void selectAll();

    /**
     * Returns true if the control's content can be returned as an instance of the class, this is an
     * edit contrl for.
     */
    public boolean isTextContentParsable();

    /**
     * Sets the messages for the control.
     */
    public void setMessages(MessageList list);

    /**
     * Adds the value change listener.
     */
    public boolean addChangeListener(ValueChangeListener listener);

    /**
     * Removes the value change listener.
     */
    public boolean removeChangeListener(ValueChangeListener listener);

}
