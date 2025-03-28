/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.fixcontent;

import java.util.List;

import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controls.IpsObjectRefControl;
import org.faktorips.devtools.model.IIpsMetaObject;
import org.faktorips.devtools.model.IPartReference;
import org.faktorips.devtools.model.enums.IEnumLiteralNameAttribute;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.ILabeledElement;
import org.faktorips.devtools.model.ipsproject.IIpsProject;

public interface DeltaFixWizardStrategy<T extends IIpsObject, E extends ILabeledElement> {

    IIpsMetaObject getContent();

    /**
     * Returns a reference to the ContentType or {@code null} if no ContentType can be found.
     * 
     * @param ipsProject The IPS project which IPS object path is used for the search. This is not
     *            necessarily the project this ContentAttribute is part of.
     * @param name The name of the new ContentType
     * @throws NullPointerException If {@link IIpsProject} is {@code null}.
     */
    T findContentType(IIpsProject ipsProject, String name);

    /**
     * Returns the {@link IIpsProject} of the content which is currently being fixed.
     */
    IIpsProject getIpsProject();

    /**
     * Returns how many {@code ContentAttribute ContentAttributes} are currently part of this
     * {@code ContentType}.
     * <p>
     * this operation <strong>does</strong> count inherited
     * {@code ContentAttribute ContentAttributes}.
     * 
     * @param includeLiteralName When set to {@code true} the
     *            {@code IEnumLiteralNameAttribute IEnumLiteralNameAttributes} will be counted, too.
     */
    int getContentAttributesCountIncludeSupertypeCopies(T newContentType, boolean includeLiteralName);

    /**
     * Returns a list containing all {@link IPartReference IPartReferences} that belong to this
     * {@code Content}.
     * <p>
     * Returns an empty list if there are none, never returns {@code null}.
     */
    List<IPartReference> getContentAttributeReferences();

    String getContentAttributeReferenceName(List<IPartReference> contentAttributeReferences, int i);

    /**
     * Returns the number of {@code ContentAttribute ContentAttributes} that are currently
     * referenced by this {@code Content}.
     */
    int getContentAttributeReferencesCount();

    /**
     * Returns a list containing all {@code ContentAttribute ContentAttributes} that belong to this
     * {@code ContentType} <strong>plus</strong> all {@code ContentAttribute ContentAttributes} that
     * have been inherited from the super type hierarchy (these are not the original
     * {@code ContentAttribute ContentAttributes} defined in the respective super types but copies
     * created based upon the originals).
     * <p>
     * If the original {@code ContentAttribute ContentAttributes} defined in the respective super
     * types are needed
     * 
     * @param includeLiteralName When set to {@code true} the {@link IEnumLiteralNameAttribute
     *            IEnumLiteralNameAttributes} will be contained in the returned list.
     */
    List<E> getContentAttributesIncludeSupertypeCopies(T newContentType, boolean includeLiteralName);

    /**
     * Gives back the correct description of the content type.
     */
    String getContentTypeString();

    IpsObjectRefControl createContentTypeRefControl(UIToolkit uitoolkit, Composite workArea);

    void createControl(T contentType, IpsObjectRefControl contentTypeRefControl);

    boolean checkForCorrectDataType(String currentComboText, int i);
}
