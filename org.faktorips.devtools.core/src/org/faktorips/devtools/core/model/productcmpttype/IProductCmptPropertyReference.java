/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.model.productcmpttype;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.type.IProductCmptProperty;

/**
 * An {@link IIpsObjectPart} that references an {@link IProductCmptProperty}.
 * <p>
 * References to IPS object parts via their names are not always the best solution as these are
 * fragile with respect to the 'Rename' refactoring. An alternative would be to reference the part
 * id. This interface describes an abstraction to the way such a reference is implemented for
 * product component properties.
 * 
 * @since 3.6
 * 
 * @author Alexander Weickmann
 * 
 * @see IProductCmptProperty
 */
public interface IProductCmptPropertyReference extends IIpsObjectPart {

    /**
     * Sets the referenced {@link IProductCmptProperty}.
     */
    public void setReferencedProperty(IProductCmptProperty property);

    /**
     * Returns whether the given {@link IProductCmptProperty} is identified by this
     * {@link IProductCmptPropertyReference}.
     */
    public boolean isReferencingProperty(IProductCmptProperty property);

    /**
     * Returns the referenced {@link IProductCmptProperty} or null if it cannot be found.
     * 
     * @throws CoreException if an error occurs during the search
     */
    public IProductCmptProperty findProductCmptProperty(IIpsProject ipsProject) throws CoreException;

}
