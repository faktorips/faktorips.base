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

package org.faktorips.devtools.htmlexport.pages.elements.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.htmlexport.documentor.DocumentationContext;
import org.faktorips.devtools.htmlexport.generators.WrapperType;
import org.faktorips.devtools.htmlexport.helper.filter.IpsElementInDocumentedSourceFileFilter;
import org.faktorips.devtools.htmlexport.helper.path.LinkedFileType;
import org.faktorips.devtools.htmlexport.helper.path.PathUtilFactory;
import org.faktorips.devtools.htmlexport.pages.elements.types.IpsElementImagePageElement;

/**
 * Utility for {@link PageElement}s
 * 
 * @author dicker
 * 
 */
public class PageElementUtils {

    /**
     * creates {@link PageElement}s from the given {@link String}s with {@link Style}s and
     * {@link TextType}s
     * 
     * @return array of {@link PageElement}s. To enable storing of other types of PageElements like
     *         LinkPageElement, the return type is not TextPageElement[]
     */
    public static PageElement[] createTextPageElements(List<String> texts, Set<Style> styles, TextType type) {
        PageElement[] textPageElements = new PageElement[texts.size()];

        for (int i = 0; i < textPageElements.length; i++) {
            textPageElements[i] = new TextPageElement(texts.get(i), styles, type);
        }

        return textPageElements;
    }

    /**
     * creates {@link PageElement}s from the given {@link String}s
     * 
     * @return array of {@link PageElement}s
     */
    public static PageElement[] createTextPageElements(List<String> texts) {
        return createTextPageElements(texts, null, TextType.WITHOUT_TYPE);
    }

    /**
     * creates a {@link List} with link to the given {@link IIpsObject}s with the given target and
     * {@link Style}s
     * 
     * @return {@link List} of {@link LinkPageElement}s
     */
    public static List<PageElement> createLinkPageElements(List<? extends IIpsSrcFile> srcFiles,
            String target,
            Set<Style> styles,
            DocumentationContext context) {
        List<PageElement> liste = new ArrayList<PageElement>();

        for (IIpsSrcFile srcFile : srcFiles) {
            PageElement linkPageElement = createLinkPageElement(context, srcFile, target, srcFile.getIpsObjectName(),
                    true, styles.toArray(new Style[styles.size()]));
            linkPageElement.addStyles(styles.toArray(new Style[styles.size()]));
            liste.add(linkPageElement);
        }

        return liste;
    }

    /**
     * creates a LinkPageElement for an IpsElement
     */
    public static PageElement createLinkPageElement(DocumentationContext context,
            IIpsElement to,
            String target,
            String text,
            boolean useImage,
            Style... styles) {
        IpsElementInDocumentedSourceFileFilter filter = new IpsElementInDocumentedSourceFileFilter(context);

        PageElement element = createIpsElementRepresentation(to, text, useImage);

        if (filter.accept(to)) {
            return createLinkPageElementToIpsElement(to, target, element).addStyles(styles);
        }
        return element.addStyles(Style.DEAD_LINK);
    }

    public static PageElement createIpsElementRepresentation(IIpsElement ipsElement, String text, boolean useImage) {
        if (useImage) {
            return new WrapperPageElement(WrapperType.NONE).addPageElements(new IpsElementImagePageElement(ipsElement))
                    .addPageElements(new TextPageElement('\u00A0' + text));
        }
        return new TextPageElement(text);
    }

    /**
     * creates a representation of the given {@link IIpsElement}. It uses the name of the ipselement
     * and adds an image if useImage is true.
     * 
     */
    public static PageElement createIpsElementRepresentation(IIpsElement ipsElement, boolean useImage) {
        return createIpsElementRepresentation(ipsElement, ipsElement.getName(), useImage);
    }

    /**
     * creates a Link to the given {@link IIpsElement}
     */
    public static PageElement createLinkPageElement(DocumentationContext context,
            IIpsElement to,
            String target,
            String text,
            boolean useImage) {
        return createLinkPageElement(context, to, target, text, useImage, new Style[0]);
    }

    public static LinkPageElement createLinkPageElementToIpsElement(IIpsElement to, String target, PageElement element) {
        String path = PathUtilFactory.createPathUtil(to).getPathFromRoot(
                LinkedFileType.getLinkedFileTypeByIpsElement(to));
        LinkPageElement linkPageElement = new LinkPageElement(path, target, element);
        return linkPageElement;
    }
}
