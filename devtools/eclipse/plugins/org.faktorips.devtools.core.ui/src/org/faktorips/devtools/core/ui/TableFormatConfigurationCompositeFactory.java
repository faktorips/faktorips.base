/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.tableconversion.ITableFormat;
import org.faktorips.devtools.core.ui.controller.fields.FieldValueChangedEvent;
import org.faktorips.devtools.core.ui.controller.fields.ValueChangeListener;
import org.faktorips.runtime.MessageList;

/**
 * This class is intended to be subclassed by implementors of specific import or export formats. The
 * subclasses then provide GUI components to configure the table format.
 * <p>
 * To register a specific GUI composite factory for a table format the
 * <code>externalTableFormat</code> extension point has to adapted. The affected attribute is
 * <code>guiClass</code>.
 * 
 * @author Roman Grutza
 */
public abstract class TableFormatConfigurationCompositeFactory implements ValueChangeListener {

    private ArrayList<ValueChangeListener> listeners = new ArrayList<>();

    /**
     * Creates a Composite to configure <code>ITableFormat</code> properties.
     * 
     * @param parent The parent Composite
     * @param toolkit Toolkit to ensure a consistent look and feel.
     * @return A new Composite under the given parent.
     */
    public abstract Composite createPropertyComposite(Composite parent, UIToolkit toolkit);

    /**
     * Validate the table format properties made in the Composite.
     * 
     * @return A <code>MessageList</code> reflecting the validation status.
     */
    public abstract MessageList validate();

    /**
     * Sets a table specific format.
     * <p>
     * This method must not be called by clients, it ensures that instances of this class can be
     * used even when the table format is not known in advance.
     * 
     * @param tableFormat A valid {@link ITableFormat} instance.
     */
    protected abstract void setTableFormat(ITableFormat tableFormat);

    /**
     * Adds a listener which is notified when properties of this Composite are altered.
     * 
     * @param listener An {@link ValueChangeListener} instance.
     */
    public void addValueChangedListener(ValueChangeListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    /**
     * Removes the given listener from the notification list.
     * 
     * @param listener An {@link ValueChangeListener} instance.
     */
    public void removeValueChangedListener(ValueChangeListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void valueChanged(FieldValueChangedEvent e) {
        Iterator<ValueChangeListener> iterator = listeners.iterator();
        while (iterator.hasNext()) {
            iterator.next().valueChanged(e);
        }
    }

}
