package org.faktorips.devtools.htmlexport.pages.elements.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.htmlexport.documentor.DocumentorConfiguration;
import org.faktorips.devtools.htmlexport.generators.WrapperType;
import org.faktorips.devtools.htmlexport.helper.filter.IpsObjectInIpsProjectFilter;

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
     * @param texts
     * @param styles
     * @param type
     * @return array of {@link PageElement}s
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
     * @param texts
     * @return array of {@link PageElement}s
     */
    public static PageElement[] createTextPageElements(List<String> texts) {
        return createTextPageElements(texts, null, TextType.WITHOUT_TYPE);
    }

    /**
     * creates a {@link List} with link to the given {@link IIpsObject}s with the given target and
     * {@link Style}s
     * 
     * @param objects
     * @param target
     * @param styles
     * @param config
     * @return {@link List} of {@link LinkPageElement}s
     */
    public static List<PageElement> createLinkPageElements(List<? extends IIpsObject> objects,
            String target,
            Set<Style> styles,
            DocumentorConfiguration config) {
        List<PageElement> liste = new ArrayList<PageElement>();

        for (IIpsObject object : objects) {
            PageElement linkPageElement = createLinkPageElement(config, object, target, object.getName(), true, styles
                    .toArray(new Style[styles.size()]));
            linkPageElement.addStyles(styles.toArray(new Style[styles.size()]));
            liste.add(linkPageElement);
        }

        return liste;
    }

    /**
     * @param config
     * @param to
     * @param target
     * @param text
     * @param useImage
     * @param styles
     * @return
     */
    public static PageElement createLinkPageElement(DocumentorConfiguration config,
            IIpsElement to,
            String target,
            String text,
            boolean useImage,
            Style... styles) {
        IpsObjectInIpsProjectFilter filter = new IpsObjectInIpsProjectFilter(config);

        PageElement element = createInnerLinkPageElement(to, text, useImage);

        if (filter.accept(to)) {
            return new LinkPageElement(to, target, element).addStyles(styles);
        }
        return element.addStyles(Style.DEAD_LINK);
    }

    /**
     * @param to
     * @param text
     * @param useImage
     * @return
     */
    private static PageElement createInnerLinkPageElement(IIpsElement to, String text, boolean useImage) {
        if (useImage) {
            return new WrapperPageElement(WrapperType.NONE).addPageElements(new ImagePageElement(to)).addPageElements(
                    new TextPageElement('\u00A0' + text));
        }
        return new TextPageElement(text);
    }

    public static PageElement createLinkPageElement(DocumentorConfiguration config,
            IIpsElement to,
            String target,
            String text,
            boolean useImage) {
        return createLinkPageElement(config, to, target, text, useImage, new Style[0]);
    }

}
