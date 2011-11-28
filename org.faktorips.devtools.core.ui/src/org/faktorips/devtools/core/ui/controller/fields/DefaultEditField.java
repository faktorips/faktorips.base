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

package org.faktorips.devtools.core.ui.controller.fields;

import java.util.ArrayList;
import java.util.List;

import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.devtools.core.ui.controller.EditFieldChangesBroadcaster;
import org.faktorips.util.message.MessageList;

/**
 * Abstract base class for easy implementation of new edit fields.
 * 
 * @see EditField for details about generic type T
 * 
 * @author Jan Ortmann
 */
public abstract class DefaultEditField<T> implements EditField<T> {

    private boolean notifyChangeListeners = true;
    private List<ValueChangeListener> changeListeners;
    private boolean supportNullStringRepresentation = true;

    /**
     * Returns the value shown in the edit field's underlying control. If the control's content
     * can't be parsed to an instance of the appropriate datatype, <code>null</code> is returned.
     */
    @Override
    public final T getValue() {
        try {
            return parseContent();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Parses the content shown in the edit field's underlying control.
     * 
     * @throws Exception if the content can't be parsed.
     */
    protected abstract T parseContent() throws Exception;

    @Override
    public boolean isTextContentParsable() {
        try {
            T content = parseContent();
            return supportsNullStringRepresentation() ? true : content != null;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void setMessages(MessageList list) {
        MessageCueController.setMessageCue(getControl(), list);
    }

    @Override
    public boolean addChangeListener(ValueChangeListener listener) {
        if (changeListeners == null) {
            changeListeners = new ArrayList<ValueChangeListener>(1);
        }
        boolean added = changeListeners.add(listener);
        if (added && changeListeners.size() == 1) {
            addListenerToControl();
        }
        return added;
    }

    @Override
    public boolean removeChangeListener(ValueChangeListener listener) {
        if (changeListeners == null) {
            return false;
        }
        return changeListeners.remove(listener);
    }

    /**
     * If the first change listener is added to the edit field, the edit field itself has to listen
     * to changes in it's control.
     */
    protected abstract void addListenerToControl();

    @Override
    public void setValue(T newValue, boolean triggerValueChanged) {
        notifyChangeListeners = triggerValueChanged;
        try {
            setValue(newValue);
        } catch (Exception e) {
            IpsPlugin.log(e);
        } finally {
            notifyChangeListeners = true;
        }
    }

    /**
     * Sends the given event to all registered listeners (in delayed manner).
     * 
     * @see EditFieldChangesBroadcaster
     */
    protected void notifyChangeListeners(FieldValueChangedEvent event) {
        notifyChangeListeners(event, false);
    }

    /**
     * Sends the given event to all registered listeners.
     * 
     * @param event The event which we'll send to the listeners
     * @param broadcastImmediately <code>true</code> the event will be directly broadcast to all
     *            registered listener <code>false</code> the event will be delayed send to the
     *            listeners.
     * @see EditFieldChangesBroadcaster
     */
    protected void notifyChangeListeners(FieldValueChangedEvent event, boolean broadcastImmediately) {
        if (!notifyChangeListeners || changeListeners == null) {
            return;
        }

        if (broadcastImmediately) {
            // notify change listeners immediately
            IpsUIPlugin
                    .getDefault()
                    .getEditFieldChangeBroadcaster()
                    .broadcastImmediately(event,
                            changeListeners.toArray(new ValueChangeListener[changeListeners.size()]));
        } else {
            // notify change listeners in delayed manner
            IpsUIPlugin.getDefault().getEditFieldChangeBroadcaster()
                    .broadcastDelayed(event, changeListeners.toArray(new ValueChangeListener[changeListeners.size()]));
        }
    }

    /**
     * <code>true</code> to activate null-handling (which means that a null-object is transformed to
     * the user defined null-representations-string and vice versa) or <code>false</code> to
     * deactivate null-handling.
     * <p>
     * If the edit field does not supports the null string representation it does not mean that the
     * {@link #parseContent()} method never returns null. For example formatted text fields may
     * return null if the input text could not be parsed to the expected data type. In this case the
     * method {@link #isTextContentParsable()} should return false. Another exasmple is an edit
     * field that handles a selection. If nothing is selected, {@link #parseContent()} would return
     * null and as far this is an valid state, the method {@link #isTextContentParsable()} return
     * true.
     */
    public void setSupportsNullStringRepresentation(boolean supportsNull) {
        this.supportNullStringRepresentation = supportsNull;
    }

    /**
     * Returns whether null is replaced by the null representation string or not.
     * 
     * @see #setSupportsNullStringRepresentation(boolean)
     */
    public boolean supportsNullStringRepresentation() {
        return supportNullStringRepresentation;
    }
}
