/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.binding;

import java.beans.PropertyChangeEvent;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.ContentsChangeListener;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.MessageList;

/**
 * An abstract base class for presentation model objects that are based on a single ips object part.
 * If an instance of this class is bound to a binding context, the binding context registers itself
 * as change listener to the ips model. The context interprets changes to the underlying ips object
 * part as changes to the presenation model object.
 * 
 * @author Jan Ortmann
 */
public class IpsObjectPartPmo extends ValidatablePMO implements ContentsChangeListener {

    public static final String PROPERTY_IPS_OBJECT_PART_CONTAINER = "ipsObjectPartContainer"; //$NON-NLS-1$

    private IIpsObjectPartContainer part;

    public IpsObjectPartPmo() {
        IpsPlugin.getDefault().getIpsModel().addChangeListener(this);
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
        notifyListeners(new PropertyChangeEvent(this, PROPERTY_IPS_OBJECT_PART_CONTAINER, oldValue, part));
    }

    public IIpsObjectPartContainer getIpsObjectPartContainer() {
        return part;
    }

    public void dispose() {
        IpsPlugin.getDefault().getIpsModel().removeChangeListener(this);
    }

    @Override
    public String toString() {
        return "PMO for " + part; //$NON-NLS-1$
    }

    @Override
    public void contentsChanged(ContentChangeEvent event) {
        if (event.isAffected(part)) {
            partHasChanged();
        }
        notifyListeners();
    }

    protected void partHasChanged() {
        // Empty default implementation
    }

    @Override
    public MessageList validate(IIpsProject ipsProject) throws CoreException {
        if (getIpsObjectPartContainer() == null) {
            return new MessageList();
        } else {
            MessageList messageList = getIpsObjectPartContainer().getIpsObject().validate(ipsProject);
            MessageList copy = createCopyAndMapObjectProperties(messageList);
            return copy;
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
