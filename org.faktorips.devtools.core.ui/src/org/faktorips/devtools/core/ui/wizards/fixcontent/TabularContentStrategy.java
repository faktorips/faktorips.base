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

import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.ui.wizards.enumcontent.FixEnumContentStrategy;
import org.faktorips.devtools.core.ui.wizards.tablecontents.FixTableContentStrategy;
import org.faktorips.devtools.model.enums.IEnumContent;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.ILabeledElement;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.tablecontents.ITableContents;
import org.faktorips.devtools.model.value.ValueTypeMismatch;

/**
 * Strategy Interface for FixContentWizard to work with {@link IEnumContent} and
 * {@link ITableContents}.
 * 
 * @see FixEnumContentStrategy
 * @see FixTableContentStrategy
 * 
 */
public interface TabularContentStrategy<T extends IIpsObject, E extends ILabeledElement> {

    /**
     * Returns how many {@code ContentValue ContentValues} this {@code Content} currently contains.
     */
    int getContentValuesCount();

    /**
     * Sets the {@code ContentType} this {@code Content} is based upon.
     * <p>
     * If the new {@code ContentType} can be found then the {@code IPartReference IPartReferences}
     * will be updated to match the {@code ContentAttribute ContentAttributes} of the new
     * {@code ContentType}.
     * 
     * @param contentType The qualified name of the {@code ContentType} this {@code Content} shall
     *            be based upon.
     * @throws CoreException If an error occurs while searching for the new {@code IEnumType}.
     * @throws NullPointerException If {@code ContentType} is {@code null}.
     */
    void setContentType(String contentType) throws CoreException;

    /**
     * Cleans the content after performing the moving of values beetwen columns.
     */
    void fixAllContentAttributeValues();

    /**
     * Returns the {@link IIpsProject} of the content which is currently being fixed.
     */
    IIpsProject getIpsProject();

    /**
     * Returns a reference to the ContentType or {@code null} if no ContentType can be found.
     *
     * @param ipsProject The IPS project which IPS object path is used for the search. This is not
     *            necessarily the project this ContentAttribute is part of.
     * @throws NullPointerException If {@link IIpsProject} is {@code null}.
     */
    T findContentType(IIpsProject ipsProject);

    /**
     * Only used in {@link FixEnumContentStrategy}.
     * 
     * @see FixEnumContentStrategy
     */
    Map<String, ValueTypeMismatch> checkAllContentAttributeValueTypeMismatch();

    /**
     * Deletes all ContentAttributeValues that are not needed anymore since the corresponding
     * ContentAttribute was deleted.
     * 
     * @param assignEnumAttributesPage Wizardpage to display the
     *            {@code ContentAttribute ContentAttributes} and {@code ContentValue ContentValues}.
     */
    void deleteObsoleteContentAttributeValues(AssignContentAttributesPage<T, E> assignEnumAttributesPage);

    /**
     * Adds new ContentAttributeValues because a new Column was created.
     * 
     * @param assignEnumAttributesPage Wizard Page to display the
     *            {@code ContentAttribute ContentAttributes} and {@code ContentValue ContentValues}.
     * 
     */
    void createNewContentAttributeValues(AssignContentAttributesPage<T, E> assignEnumAttributesPage);

    void moveAttributeValues(int[] columnOrdering);

    /**
     * Finds the corresponding image for the wizard.
     */
    String getImage();

    /**
     * Creates a new ContentPageStrategy for the wizard pages.
     */
    DeltaFixWizardStrategy<T, E> createContentPageStrategy();

}