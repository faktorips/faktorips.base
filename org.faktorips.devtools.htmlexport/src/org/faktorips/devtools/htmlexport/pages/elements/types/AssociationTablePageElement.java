/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.pages.elements.types;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.htmlexport.context.DocumentationContext;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElementUtils;
import org.faktorips.devtools.htmlexport.pages.elements.core.Style;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextPageElement;

/**
 * Represents a table with the associations of an {@link IType} as rows and the attributes of the
 * associations as columns
 * 
 * @author dicker
 * 
 */
public class AssociationTablePageElement extends AbstractIpsObjectPartsContainerTablePageElement<IAssociation> {

    private final DocumentationContext context;
    private final IType type;

    /**
     * Creates an {@link AssociationTablePageElement} for the specified {@link IType}
     * 
     */
    public AssociationTablePageElement(IType type, DocumentationContext context) {
        super(Arrays.asList(type.getAssociations()), context);
        this.context = context;
        this.type = type;
    }

    @Override
    protected List<? extends PageElement> createRowWithIpsObjectPart(IAssociation association) {
        List<String> values = new ArrayList<String>();

        values.add(association.getName());
        values.add(getContext().getLabel(association));

        // will be replaced with the link
        values.add(""); //$NON-NLS-1$
        int linkElementIndex = 2;

        values.add(getContext().getDescription(association));
        values.add(association.getAssociationType().getName());
        values.add(association.getAggregationKind().getName());
        values.add(association.getTargetRoleSingular());
        values.add(association.getTargetRolePlural());
        values.add(Integer.toString(association.getMinCardinality()));
        values.add(getMaxCardinalityString(association.getMaxCardinality()));
        values.add(association.isDerivedUnion() ? "X" : "-"); //$NON-NLS-1$ //$NON-NLS-2$
        values.add(association.isSubsetOfADerivedUnion() ? "X" : "-"); //$NON-NLS-1$ //$NON-NLS-2$
        values.add(association.isQualified() ? "X" : "-"); //$NON-NLS-1$ //$NON-NLS-2$

        PageElement[] elements = PageElementUtils.createTextPageElements(values);

        try {
            IIpsObject target = type.getIpsProject().findIpsObject(type.getIpsObjectType(), association.getTarget());
            elements[linkElementIndex] = PageElementUtils.createLinkPageElement(context, target,
                    "content", target.getName(), true); //$NON-NLS-1$
        } catch (CoreException e) {
            context.addStatus(new IpsStatus(IStatus.WARNING,
                    "Error setting Link to target of " + association.getName(), e)); //$NON-NLS-1$
            elements[linkElementIndex] = new TextPageElement(""); //$NON-NLS-1$
        }

        return Arrays.asList(elements);
    }

    private String getMaxCardinalityString(int maxCardinality) {
        if (maxCardinality == Integer.MAX_VALUE) {
            return "*"; //$NON-NLS-1$
        }
        return Integer.toString(maxCardinality);
    }

    @Override
    protected List<String> getHeadlineWithIpsObjectPart() {
        List<String> headline = new ArrayList<String>();

        headline.add(getContext().getMessage("AssociationTablePageElement_headlineName")); //$NON-NLS-1$
        headline.add(getContext().getMessage("AssociationTablePageElement_headlineLabel")); //$NON-NLS-1$
        headline.add(getContext().getMessage("AssociationTablePageElement_headlineTarget")); //$NON-NLS-1$
        headline.add(getContext().getMessage("AssociationTablePageElement_headlineDescription")); //$NON-NLS-1$
        headline.add(getContext().getMessage("AssociationTablePageElement_headlineAssociationType")); //$NON-NLS-1$
        headline.add(getContext().getMessage("AssociationTablePageElement_headlineAggregationKind")); //$NON-NLS-1$
        headline.add(getContext().getMessage("AssociationTablePageElement_headlineTargetRoleSingular")); //$NON-NLS-1$
        headline.add(getContext().getMessage("AssociationTablePageElement_headlineTargetRolePlural")); //$NON-NLS-1$

        addHeadlineAndColumnLayout(headline,
                getContext().getMessage("AssociationTablePageElement_headlineMinCardinality"), Style.CENTER); //$NON-NLS-1$
        addHeadlineAndColumnLayout(headline,
                getContext().getMessage("AssociationTablePageElement_headlineMaxCardinality"), Style.CENTER); //$NON-NLS-1$
        addHeadlineAndColumnLayout(headline, getContext()
                .getMessage("AssociationTablePageElement_headlineDerivedUnion"), Style.CENTER); //$NON-NLS-1$
        addHeadlineAndColumnLayout(headline,
                getContext().getMessage("AssociationTablePageElement_headlineSubsettedDerivedUnion"), //$NON-NLS-1$
                Style.CENTER);
        addHeadlineAndColumnLayout(headline,
                getContext().getMessage("AssociationTablePageElement_headlineQualified"), Style.CENTER); //$NON-NLS-1$

        return headline;
    }
}
