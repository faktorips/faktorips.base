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

import org.faktorips.devtools.core.model.type.IMethod;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.htmlexport.context.DocumentationContext;
import org.faktorips.devtools.htmlexport.context.messages.HtmlExportMessages;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElementUtils;

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
        List<String> methodData = new ArrayList<String>();

        methodData.add(method.getName());
        methodData.add(method.isAbstract() ? "X" : "-"); //$NON-NLS-1$ //$NON-NLS-2$
        methodData.add(method.getModifier().toString());
        methodData.add(method.getDatatype());
        methodData.add(method.getSignatureString());
        methodData.add(getContext().getDescription(method));

        return methodData;
    }

    @Override
    protected List<String> getHeadlineWithIpsObjectPart() {
        ArrayList<String> headline = new ArrayList<String>();

        headline.add(getContext().getMessage(HtmlExportMessages.MethodsTablePageElement_headlineName));
        headline.add(getContext().getMessage(HtmlExportMessages.MethodsTablePageElement_headlineAbstract));
        headline.add(getContext().getMessage(HtmlExportMessages.MethodsTablePageElement_headlineModifier));
        headline.add(getContext().getMessage(HtmlExportMessages.MethodsTablePageElement_headlineDatatype));
        headline.add(getContext().getMessage(HtmlExportMessages.MethodsTablePageElement_headlineSignature));
        headline.add(getContext().getMessage(HtmlExportMessages.MethodsTablePageElement_headlineDescription));

        return headline;
    }

    @Override
    protected List<? extends PageElement> createRowWithIpsObjectPart(IMethod method) {
        PageElement[] textPageElements = new PageElementUtils().createTextPageElements(getMethodData(method));
        textPageElements[0].setAnchor(new PageElementUtils().createAnchorId(method));
        return Arrays.asList(textPageElements);
    }
}
