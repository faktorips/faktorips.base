/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.pages.elements.types;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.faktorips.devtools.htmlexport.context.DocumentationContext;
import org.faktorips.devtools.htmlexport.context.messages.HtmlExportMessages;
import org.faktorips.devtools.htmlexport.helper.path.TargetType;
import org.faktorips.devtools.htmlexport.pages.elements.core.IPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElementUtils;
import org.faktorips.devtools.htmlexport.pages.elements.core.Style;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.type.IAssociation;
import org.faktorips.devtools.model.type.IType;

/**
 * Represents a table with the associations of an {@link IType} as rows and the attributes of the
 * associations as columns
 * 
 * @author dicker
 * 
 */
public class AssociationTablePageElement extends AbstractIpsObjectPartsContainerTablePageElement<IAssociation> {

    private final IType type;

    /**
     * Creates an {@link AssociationTablePageElement} for the specified {@link IType}
     * 
     */
    public AssociationTablePageElement(IType type, DocumentationContext context) {
        super(type.getAssociations(), context);
        this.type = type;
    }

    @Override
    protected List<IPageElement> createRowWithIpsObjectPart(IAssociation association) {
        List<String> values = new ArrayList<>();

        values.add(getContext().getLabel(association));
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

        IPageElement[] elements = new PageElementUtils(getContext()).createTextPageElements(values);

        IIpsObject target = type.getIpsProject().findIpsObject(type.getIpsObjectType(), association.getTarget());
        elements[linkElementIndex] = new PageElementUtils(getContext()).createLinkPageElement(getContext(), target,
                TargetType.CONTENT, target.getName(), true);

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
        List<String> headline = new ArrayList<>();

        headline.add(getContext().getMessage(HtmlExportMessages.AssociationTablePageElement_headlineName));
        headline.add(getContext().getMessage(HtmlExportMessages.AssociationTablePageElement_headlineLabel));
        headline.add(getContext().getMessage(HtmlExportMessages.AssociationTablePageElement_headlineTarget));
        headline.add(getContext().getMessage(HtmlExportMessages.AssociationTablePageElement_headlineDescription));
        headline.add(getContext().getMessage(HtmlExportMessages.AssociationTablePageElement_headlineAssociationType));
        headline.add(getContext().getMessage(HtmlExportMessages.AssociationTablePageElement_headlineAggregationKind));
        headline.add(
                getContext().getMessage(HtmlExportMessages.AssociationTablePageElement_headlineTargetRoleSingular));
        headline.add(getContext().getMessage(HtmlExportMessages.AssociationTablePageElement_headlineTargetRolePlural));

        addHeadlineAndColumnLayout(headline,
                getContext().getMessage(HtmlExportMessages.AssociationTablePageElement_headlineMinCardinality),
                Style.CENTER);
        addHeadlineAndColumnLayout(headline,
                getContext().getMessage(HtmlExportMessages.AssociationTablePageElement_headlineMaxCardinality),
                Style.CENTER);
        addHeadlineAndColumnLayout(headline,
                getContext().getMessage(HtmlExportMessages.AssociationTablePageElement_headlineDerivedUnion),
                Style.CENTER);
        addHeadlineAndColumnLayout(headline,
                getContext().getMessage(HtmlExportMessages.AssociationTablePageElement_headlineSubsettedDerivedUnion),
                Style.CENTER);
        addHeadlineAndColumnLayout(headline,
                getContext().getMessage(HtmlExportMessages.AssociationTablePageElement_headlineQualified),
                Style.CENTER);

        return headline;
    }
}
