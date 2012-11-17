/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.model.productcmpt;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.internal.model.productcmpt.IProductCmptLinkContainer;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;

/**
 * This interface combines the {@link IPropertyValueContainer} and the
 * {@link IProductCmptLinkContainer}.
 * 
 * @author dirmeier
 */
public interface IProductPartsContainer extends IIpsObjectPartContainer {

    /**
     * Returns all property values for the given type. Returns an empty array if type is
     * <code>null</code> or no property values were found for the given type.
     */
    public <T extends IIpsObjectPart> List<T> getProductParts(Class<T> type);

    /**
     * Returns the product component for this container. If this container is a
     * {@link IProductCmptGeneration product component generation} the corresponding product
     * component is returned. If this is a {@link IProductCmpt product component} it returns itself.
     */
    public IProductCmpt getProductCmpt();

    /**
     * Returns the qualified name of the product component type this property value container is
     * based on.
     */
    public String getProductCmptType();

    /**
     * Finds the {@link IProductCmptType product component type} this container is based on.
     * 
     * @param ipsProject The IPS project which search path is used to search the type.
     * 
     * @return The product component type this link container is based on or <code>null</code> if
     *         the product component type can't be found.
     * 
     * @throws CoreException if an exception occurs while searching for the type.
     * @throws NullPointerException if ipsProject is <code>null</code>.
     */
    public IProductCmptType findProductCmptType(IIpsProject ipsProject) throws CoreException;

}
