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
import org.faktorips.devtools.htmlexport.pages.elements.core.IPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElementUtils;
import org.faktorips.devtools.model.type.IMethod;
import org.faktorips.devtools.model.type.IType;

/**
 * Represents a table with the {@link IMethod}s of an {@link IType} as rows and the attributes of
 * the method as columns
 * 
 * @author dicker
 * 
 */
public class MethodsTablePageElement extends AbstractIpsObjectPartsContainerTablePageElement<IMethod> {

    /**
     * Creates a {@link MethodsTablePageElement} for the specified {@link IType}
     * 
     */
    public MethodsTablePageElement(IType type, DocumentationContext context) {
        super(type.getMethods(), context);
        setId(type.getName() + "_methods"); //$NON-NLS-1$
    }

    /**
     * returns the data of the given {@link IMethod}
     * 
     */
    protected List<String> getMethodData(IMethod method) {
        List<String> methodData = new ArrayList<>();

        methodData.add(getContext().getLabel(method));
        methodData.add(method.isAbstract() ? "X" : "-"); //$NON-NLS-1$ //$NON-NLS-2$
        methodData.add(method.getModifier().toString());
        methodData.add(method.getDatatype());
        methodData.add(method.getSignatureString());
        methodData.add(getContext().getDescription(method));

        return methodData;
    }

    @Override
    protected List<String> getHeadlineWithIpsObjectPart() {
        ArrayList<String> headline = new ArrayList<>();

        headline.add(getContext().getMessage(HtmlExportMessages.MethodsTablePageElement_headlineName));
        headline.add(getContext().getMessage(HtmlExportMessages.MethodsTablePageElement_headlineAbstract));
        headline.add(getContext().getMessage(HtmlExportMessages.MethodsTablePageElement_headlineModifier));
        headline.add(getContext().getMessage(HtmlExportMessages.MethodsTablePageElement_headlineDatatype));
        headline.add(getContext().getMessage(HtmlExportMessages.MethodsTablePageElement_headlineSignature));
        headline.add(getContext().getMessage(HtmlExportMessages.MethodsTablePageElement_headlineDescription));

        return headline;
    }

    @Override
    protected List<IPageElement> createRowWithIpsObjectPart(IMethod method) {
        IPageElement[] textPageElements = new PageElementUtils(getContext())
                .createTextPageElements(getMethodData(method));
        textPageElements[0].setAnchor(new PageElementUtils(getContext()).createAnchorId(method));
        return Arrays.asList(textPageElements);
    }
}
