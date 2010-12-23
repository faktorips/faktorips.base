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

package org.faktorips.devtools.htmlexport.generators.html;

import java.io.IOException;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.faktorips.devtools.htmlexport.generators.AbstractLayouter;
import org.faktorips.devtools.htmlexport.generators.LayoutResource;
import org.faktorips.devtools.htmlexport.generators.WrapperType;
import org.faktorips.devtools.htmlexport.helper.DocumentorUtil;
import org.faktorips.devtools.htmlexport.helper.FileHandler;
import org.faktorips.devtools.htmlexport.helper.html.HtmlTextType;
import org.faktorips.devtools.htmlexport.helper.html.HtmlUtil;
import org.faktorips.devtools.htmlexport.pages.elements.core.AbstractCompositePageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.AbstractRootPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.ICompositePageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.ImagePageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.LinkPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.ListPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.Style;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextType;
import org.faktorips.devtools.htmlexport.pages.elements.core.table.TablePageElement;

/**
 * Html-Layouter: Layouts the PageElements as Html
 * 
 * @author dicker
 * 
 */
public class HtmlLayouter extends AbstractLayouter {

    /**
     * Name of the css-File
     */
    private static final String HTML_BASE_CSS = "html/base.css"; //$NON-NLS-1$

    /**
     * path to the resource in the generated site
     */

    private String resourcePath;

    /**
     * path from the actual RootPageElement to the root-folder of the site
     */
    private String pathToRoot;
    private HtmlUtil htmlUtil = new HtmlUtil();

    private FileHandler fileHandler = new FileHandler();

    public HtmlLayouter(String resourcePath) {
        super();
        this.resourcePath = resourcePath;
        initBaseResources();
    }

    @Override
    public void layoutLinkPageElement(LinkPageElement pageElement) {
        if (pageElement.hasStyle(Style.BLOCK)) {
            append(htmlUtil.createHtmlElementOpenTag("div")); //$NON-NLS-1$
        }
        append(htmlUtil.createLinkOpenTag(createLinkBase(pageElement), pageElement.getTarget(),
                getClasses(pageElement), pageElement.getTitle()));
        visitSubElements(pageElement);
        append(htmlUtil.createHtmlElementCloseTag("a")); //$NON-NLS-1$
        if (pageElement.hasStyle(Style.BLOCK)) {
            append(htmlUtil.createHtmlElementCloseTag("div")); //$NON-NLS-1$
        }
    }

    /**
     * creates the relative href from the actual page to the linked page
     */
    String createLinkBase(LinkPageElement pageElement) {
        return getPathToRoot() + pageElement.getPathFromRoot() + ".html"; //$NON-NLS-1$
    }

    @Override
    public void layoutListPageElement(ListPageElement pageElement) {
        String listBaseHtmlTag = pageElement.isOrdered() ? "ul" : "ol"; //$NON-NLS-1$ //$NON-NLS-2$
        append(htmlUtil.createHtmlElementOpenTag(listBaseHtmlTag, pageElement.getId(), getClasses(pageElement)));
        visitSubElements(pageElement);
        append(htmlUtil.createHtmlElementCloseTag(listBaseHtmlTag));
    }

    @Override
    public void layoutTablePageElement(TablePageElement pageElement) {
        append(htmlUtil.createHtmlElementOpenTag("table", pageElement.getId(), getClasses(pageElement))); //$NON-NLS-1$
        visitSubElements(pageElement);
        append(htmlUtil.createHtmlElementCloseTag("table")); //$NON-NLS-1$
    }

    @Override
    public void layoutWrapperPageElement(AbstractCompositePageElement wrapperPageElement) {
        WrapperType wrapperType = wrapperPageElement.getWrapperType();
        if (wrapperType == WrapperType.NONE && wrapperPageElement.getStyles().isEmpty()) {
            visitSubElements(wrapperPageElement);
            return;
        }
        String wrappingElement = getHtmlElementByWrappingType(wrapperType);
        append(htmlUtil.createHtmlElementOpenTag(wrappingElement, wrapperPageElement.getId(),
                getClasses(wrapperPageElement)));
        visitSubElements(wrapperPageElement);
        append(htmlUtil.createHtmlElementCloseTag(wrappingElement));
    }

    /**
     * returns name of the html-element for the given {@link WrapperType}
     * 
     */
    private String getHtmlElementByWrappingType(WrapperType wrapper) {
        if (wrapper == WrapperType.LISTITEM) {
            return "li"; //$NON-NLS-1$
        }
        if (wrapper == WrapperType.TABLEROW) {
            return "tr"; //$NON-NLS-1$
        }
        if (wrapper == WrapperType.TABLECELL) {
            return "td"; //$NON-NLS-1$
        }
        if (wrapper == WrapperType.BLOCK) {
            return "div"; //$NON-NLS-1$
        }
        return "span"; //$NON-NLS-1$
    }

    @Override
    public void layoutRootPageElement(AbstractRootPageElement pageElement) {
        initRootPage(pageElement);

        append(htmlUtil.createHtmlHead(pageElement.getTitle(), getPathToRoot() + getStyleDefinitionPath()));
        visitSubElements(pageElement);

        append(htmlUtil.createHtmlFoot());
    }

    /**
     * sets the pathToRoot and clears the content
     * 
     */
    void initRootPage(AbstractRootPageElement pageElement) {
        setPathToRoot(pageElement.getPathToRoot());
        clear();
    }

    @Override
    public void layoutTextPageElement(TextPageElement pageElement) {
        if (pageElement.getType() == TextType.WITHOUT_TYPE && pageElement.getStyles().isEmpty()) {
            append(htmlUtil.getHtmlText(pageElement.getText()));
            return;
        }
        append(htmlUtil.createHtmlElement(identifyTagName(pageElement), pageElement.getText(), getClasses(pageElement)));
    }

    /**
     * puts all {@link Style}s of the {@link PageElement} in a String for the html-class-attribute.
     * 
     */
    private String getClasses(PageElement pageElement) {

        Set<Style> styles = pageElement.getStyles();
        if (styles == null || styles.isEmpty()) {
            return null;
        }

        StringBuilder classes = new StringBuilder();

        for (Style style : styles) {
            classes.append(style);
            classes.append(' ');
        }
        return classes.toString().trim();
    }

    /**
     * returns the name of an html-element according to the given {@link TextPageElement}
     */
    private String identifyTagName(TextPageElement textPageElement) {
        return HtmlTextType.getHtmlTextTypeByTextType(textPageElement.getType()).getTagName();
    }

    /**
     * The Layouter visits the subelements of the given {@link ICompositePageElement}
     * 
     */
    protected void visitSubElements(ICompositePageElement compositePageElement) {
        compositePageElement.visitSubElements(this);
    }

    /**
     * initializes the basic resources e.g. the external css-stylesheet-definitions
     */
    void initBaseResources() {
        try {
            LayoutResource cssResource = new LayoutResource(getStyleDefinitionPath(), fileHandler.readFile(
                    "org.faktorips.devtools.htmlexport", HTML_BASE_CSS)); //$NON-NLS-1$

            addLayoutResource(cssResource);

        } catch (IOException e) {
            System.out.println("Resources aren't loaded correctly: " + e.getMessage()); //$NON-NLS-1$
        }
    }

    /**
     * returns the relative path from root to the external css-stylesheet-definitions
     */
    String getStyleDefinitionPath() {
        return resourcePath + '/' + HTML_BASE_CSS;
    }

    @Override
    public void layoutImagePageElement(ImagePageElement imagePageElement) {

        String path = resourcePath + "/images/" + imagePageElement.getFileName() + ".png"; //$NON-NLS-1$ //$NON-NLS-2$
        addLayoutResource(new LayoutResource(path, new DocumentorUtil().convertImageDataToByteArray(
                imagePageElement.getImageData(), SWT.IMAGE_PNG)));

        append(htmlUtil.createImage(getPathToRoot() + path, imagePageElement.getTitle()));
    }

    public String getPathToRoot() {
        return pathToRoot;
    }

    public void setPathToRoot(String pathToRoot) {
        this.pathToRoot = pathToRoot;
    }
}
