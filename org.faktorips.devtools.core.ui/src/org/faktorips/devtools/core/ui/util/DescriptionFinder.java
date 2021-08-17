/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.util;

import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.ipsobject.IDescribedElement;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.type.IType;
import org.faktorips.devtools.model.type.TypeHierarchyVisitor;

/**
 * Searching for a description in the type hierarchy. If the description of the given type is empty
 * this visitor searches for a not empty description in supertype.
 */
public class DescriptionFinder extends TypeHierarchyVisitor<IType> {

    private String localizedDescription;

    public DescriptionFinder(IIpsProject ipsProject) {
        super(ipsProject);
    }

    public String getLocalizedDescription() {
        return localizedDescription;
    }

    public void start(IDescribedElement element) {
        if (element instanceof IType) {
            IType type = (IType)element;
            super.start(type);
        } else {
            setDescription(element);
        }
    }

    @Override
    protected boolean visit(IType currentType) {
        setDescription(currentType);
        return localizedDescription.isEmpty();
    }

    protected void setDescription(IDescribedElement currentType) {
        localizedDescription = IIpsModel.get().getMultiLanguageSupport().getLocalizedDescription(currentType);
    }
}