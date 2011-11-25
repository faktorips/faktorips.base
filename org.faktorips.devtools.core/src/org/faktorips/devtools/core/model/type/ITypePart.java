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

import org.faktorips.devtools.core.model.ipsobject.IDescribedElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.ILabeledElement;
import org.faktorips.devtools.core.model.ipsobject.Modifier;
import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;

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
     * provided {@link QualifiedNameType}.
     * 
     * @param qualifiedNameType the {@link QualifiedNameType} identifying the {@link IType} in
     *            question
     */
    public boolean isOfType(QualifiedNameType qualifiedNameType);

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

}
