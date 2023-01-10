/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.type;

import org.faktorips.devtools.model.ipsobject.IDescribedElement;
import org.faktorips.devtools.model.ipsobject.IDescription;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.ipsobject.ILabel;
import org.faktorips.devtools.model.ipsobject.ILabeledElement;
import org.faktorips.devtools.model.ipsobject.IVersionControlledElement;
import org.faktorips.devtools.model.ipsobject.Modifier;
import org.faktorips.devtools.model.ipsobject.QualifiedNameType;

/**
 * Represents a part that is common to all {@link IType ITypes}.
 * 
 * @author Alexander Weickmann
 */
public interface ITypePart extends IIpsObjectPart, IDescribedElement, ILabeledElement, IVersionControlledElement {

    String PROPERTY_MODIFIER = "modifier"; //$NON-NLS-1$

    String PROPERTY_CATEGORY = "category"; //$NON-NLS-1$

    String PROPERTY_CATEGORY_POSITION = "categoryPosition"; //$NON-NLS-1$

    /**
     * Returns the {@link IType} this part belongs to.
     */
    IType getType();

    /**
     * Returns whether this {@link ITypePart} belongs to the {@link IType} identified by the
     * provided {@link QualifiedNameType}.
     * 
     * @param qualifiedNameType the {@link QualifiedNameType} identifying the {@link IType} in
     *            question
     */
    boolean isOfType(QualifiedNameType qualifiedNameType);

    /**
     * Returns the part's {@link Modifier}.
     */
    Modifier getModifier();

    /**
     * Sets the part's {@link Modifier}.
     * 
     * @throws NullPointerException If the parameter is null
     */
    void setModifier(Modifier modifier);

    /**
     * Copies the properties of the given type part to this part except the {@link ILabel labels}
     * and {@link IDescription descriptions}.
     * 
     * @throws IllegalArgumentException If the class of the given type part is not the same as the
     *             class of this type part
     */
    void copyFromWithoutLabelAndDescription(ITypePart source);
}
