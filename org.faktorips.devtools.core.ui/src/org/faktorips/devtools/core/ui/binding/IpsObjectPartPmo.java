/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.binding;

import java.beans.PropertyChangeEvent;

import org.faktorips.devtools.model.ContentChangeEvent;
import org.faktorips.devtools.model.ContentsChangeListener;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.runtime.MessageList;
import org.faktorips.util.ArgumentCheck;

/**
 * An abstract base class for presentation model objects that are based on a single ips object part.
 * If an instance of this class is bound to a binding context, the binding context registers itself
 * as change listener to the ips model. The context interprets changes to the underlying ips object
 * part as changes to the presentation model object.
 * 
 * @author Jan Ortmann
 */
public class IpsObjectPartPmo extends ValidatablePMO implements ContentsChangeListener {

    public static final String PROPERTY_IPS_OBJECT_PART_CONTAINER = "ipsObjectPartContainer"; //$NON-NLS-1$

    private IIpsObjectPartContainer part;

    public IpsObjectPartPmo() {
        IIpsModel.get().addChangeListener(this);
    }

    /**
     * @throws NullPointerException if part is <code>null</code>.
     */
    public IpsObjectPartPmo(IIpsObjectPartContainer part) {
        this();
        ArgumentCheck.notNull(part);
        this.part = part;
    }

    public void setIpsObjectPartContainer(IIpsObjectPartContainer part) {
        IIpsObjectPartContainer oldValue = this.part;
        this.part = part;
        partHasChanged();
        notifyListeners(new PropertyChangeEvent(this, PROPERTY_IPS_OBJECT_PART_CONTAINER, oldValue, part));
    }

    public IIpsObjectPartContainer getIpsObjectPartContainer() {
        return part;
    }

    public void dispose() {
        IIpsModel.get().removeChangeListener(this);
    }

    @Override
    public String toString() {
        return "PMO for " + part; //$NON-NLS-1$
    }

    @Override
    public void contentsChanged(ContentChangeEvent event) {
        if (isAffected(event)) {
            partHasChanged();
            if (event.getPropertyChangeEvents().isEmpty()) {
                notifyListeners(new PropertyChangeEvent(event.getIpsSrcFile(), null, null, null));
            } else {
                for (PropertyChangeEvent changeEvent : event.getPropertyChangeEvents()) {
                    notifyListeners(changeEvent);
                }
            }
        }
    }

    /**
     * The default implementation uses the standard behavior
     * {@link ContentChangeEvent#isAffected(IIpsObjectPartContainer)}. Subclasses may override to
     * behave differently to certain events.
     * 
     * @param event the change event sent by the IPS model
     * @return <code>true</code> if this PMO is affected by the event. Listeners are notified and
     *             {@link #partHasChanged()} is called in that case. <code>false</code> if the PMO
     *             is unaffected.
     */
    protected boolean isAffected(ContentChangeEvent event) {
        return event.isAffected(part);
    }

    protected void partHasChanged() {
        // Empty default implementation
    }

    @Override
    public MessageList validate(IIpsProject ipsProject) {
        if (getIpsObjectPartContainer() == null) {
            return new MessageList();
        } else {
            MessageList messageList = getIpsObjectPartContainer().getIpsObject().validate(ipsProject);
            return createCopyAndMapObjectProperties(messageList);
        }
    }

    @Override
    public IIpsProject getIpsProject() {
        if (getIpsObjectPartContainer() != null) {
            return getIpsObjectPartContainer().getIpsProject();
        }
        return null;
    }

}
