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

package org.faktorips.devtools.core.ui.binding;

import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.ContentsChangeListener;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.util.ArgumentCheck;

/**
 * An abstract base class for presentation model objects that are based on a single ips object part.
 * If an instance of this class is bound to a binding context, the binding context registers itself
 * as change listener to the ips model. The context interprets changes to the underlying ips object
 * part as changes to the presenation model object.
 * 
 * @author Jan Ortmann
 */
public class IpsObjectPartPmo extends PresentationModelObject implements ContentsChangeListener {

    private IIpsObjectPartContainer part;

    /**
     * @throws NullPointerException if part is <code>null</code>.
     */
    public IpsObjectPartPmo(IIpsObjectPartContainer part) {
        ArgumentCheck.notNull(part);
        this.part = part;
        part.getIpsModel().addChangeListener(this);
    }

    public IIpsObjectPartContainer getIpsObjectPartContainer() {
        return part;
    }

    public void dispose() {
        part.getIpsModel().removeChangeListener(this);
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

}
