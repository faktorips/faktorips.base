package org.faktorips.devtools.core.ui.util;

import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsobject.IDescribedElement;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.model.type.TypeHierarchyVisitor;

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
        localizedDescription = IpsPlugin.getMultiLanguageSupport().getLocalizedDescription(currentType);
    }
}