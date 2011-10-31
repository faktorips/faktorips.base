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

package org.faktorips.devtools.core.model.type;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ipsobject.IDescribedElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.ILabeledElement;
import org.faktorips.devtools.core.model.ipsobject.Modifier;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;

/**
 * Represents a part that is common to all {@link IType}s.
 * 
 * @author Alexander Weickmann
 */
public interface ITypePart extends IIpsObjectPart, IDescribedElement, ILabeledElement {

    public final static String PROPERTY_MODIFIER = "modifier"; //$NON-NLS-1$

    public static final String PROPERTY_CATEGORY = "category"; //$NON-NLS-1$

    /**
     * Returns the {@link IType} this part belongs to.
     */
    public IType getType();

    /**
     * Returns whether this {@link ITypePart} belongs to the {@link IType} identified by the
     * provided qualified name.
     * 
     * @param typeQualifiedName The qualified name identifying the {@link IType} in question
     */
    public boolean isOfType(String typeQualifiedName);

    /**
     * Returns the part's {@link Modifier}.
     */
    public Modifier getModifier();

    /**
     * Sets the part's {@link Modifier}.
     * 
     * @throws NullPointerException If the parameter is null
     */
    public void setModifier(Modifier modifier);

    /**
     * Returns the category this part is assigned to.
     */
    public String getCategory();

    /**
     * Sets the name of the category this part is assigned to.
     * 
     * @param category The name of the category this part is assigned to
     */
    public void setCategory(String category);

    /**
     * Returns whether this part is assigned to a specific category.
     * <p>
     * This operation is a shortcut for {@code !getCategory().isEmpty()}.
     */
    public boolean hasCategory();

    /**
     * Returns the {@link IProductCmptType} this property belongs to.
     * 
     * @param ipsProject The IPS project whose IPS object path is used for the search
     * 
     * @throws CoreException If an error occurs during the search
     */
    public IProductCmptType findProductCmptType(IIpsProject ipsProject) throws CoreException;

    /**
     * Returns whether this {@link ITypePart} belongs to the same {@link IProductCmptType} as the
     * provided other {@link ITypePart}.
     * 
     * @param otherPart the other {@link ITypePart} to compare the associated
     *            {@link IProductCmptType} with
     * @param ipsProject the {@link IIpsProject} whose {@link IIpsObjectPath} is used for the search
     * 
     * @throws CoreException If an error occurs while searching for {@link IProductCmptType}s
     */
    public boolean findIsBelongingToSameProductCmptType(ITypePart otherPart, IIpsProject ipsProject)
            throws CoreException;

}
