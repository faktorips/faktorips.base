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

    /*
     * Name of the css-File
     */
    private static final String HTML_BASE_CSS = "html/base.css"; //$NON-NLS-1$
    /*
     * path to the resource in the generated site
     */
    private String resourcePath;
    /*
     * path from the actual RootPageElement to the root-folder of the site
     */
    private String pathToRoot;

    public HtmlLayouter(String resourcePath) {
        super();
        this.resourcePath = resourcePath;
        initBaseResources();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.faktorips.devtools.htmlexport.generators.ILayouter#layoutLinkPageElement(org.faktorips
     * .devtools.htmlexport.pages.elements.core.LinkPageElement)
     */
    @Override
    public void layoutLinkPageElement(LinkPageElement pageElement) {
        if (pageElement.hasStyle(Style.BLOCK)) {
            append(HtmlUtil.createHtmlElementOpenTag("div"));
        }
        append(HtmlUtil
                .createLinkOpenTag(createLinkBase(pageElement), pageElement.getTarget(), getClasses(pageElement)));
        visitSubElements(pageElement);
        append(HtmlUtil.createHtmlElementCloseTag("a")); //$NON-NLS-1$
        if (pageElement.hasStyle(Style.BLOCK)) {
            append(HtmlUtil.createHtmlElementCloseTag("div"));
        }
    }

    /**
     * @param pageElement
     * @return
     */
    String createLinkBase(LinkPageElement pageElement) {
        return getPathToRoot() + pageElement.getPathFromRoot() + ".html";
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.faktorips.devtools.htmlexport.generators.ILayouter#layoutListPageElement(org.faktorips
     * .devtools.htmlexport.pages.elements.core.ListPageElement)
     */
    @Override
    public void layoutListPageElement(ListPageElement pageElement) {
        String listBaseHtmlTag = pageElement.isOrdered() ? "ul" : "ol"; //$NON-NLS-1$ //$NON-NLS-2$
        append(HtmlUtil.createHtmlElementOpenTag(listBaseHtmlTag, getClasses(pageElement)));
        visitSubElements(pageElement);
        append(HtmlUtil.createHtmlElementCloseTag(listBaseHtmlTag));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.faktorips.devtools.htmlexport.generators.ILayouter#layoutTablePageElement(org.faktorips
     * .devtools.htmlexport.pages.elements.core.table.TablePageElement)
     */
    @Override
    public void layoutTablePageElement(TablePageElement pageElement) {
        append(HtmlUtil.createHtmlElementOpenTag("table", getClasses(pageElement))); //$NON-NLS-1$
        visitSubElements(pageElement);
        append(HtmlUtil.createHtmlElementCloseTag("table")); //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.faktorips.devtools.htmlexport.generators.ILayouter#layoutWrapperPageElement(org.faktorips
     * .devtools.htmlexport.pages.elements.core.WrapperPageElement)
     */
    @Override
    public void layoutWrapperPageElement(AbstractCompositePageElement wrapperPageElement) {
        WrapperType wrapperType = wrapperPageElement.getWrapperType();
        if (wrapperType == WrapperType.NONE && wrapperPageElement.getStyles().isEmpty()) {
            visitSubElements(wrapperPageElement);
            return;
        }
        String wrappingElement = getHtmlElementByWrappingType(wrapperType);
        append(HtmlUtil.createHtmlElementOpenTag(wrappingElement, getClasses(wrapperPageElement)));
        visitSubElements(wrapperPageElement);
        append(HtmlUtil.createHtmlElementCloseTag(wrappingElement));
    }

    /**
     * returns name of the html-element for the given {@link WrapperType}
     * 
     * @param wrapper
     * @return String
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

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.faktorips.devtools.htmlexport.generators.ILayouter#layoutRootPageElement(org.faktorips
     * .devtools.htmlexport.pages.elements.core.AbstractRootPageElement)
     */
    @Override
    public void layoutRootPageElement(AbstractRootPageElement pageElement) {
        initRootPage(pageElement);

        append(HtmlUtil.createHtmlHead(pageElement.getTitle(), getPathToRoot() + getStyleDefinitionPath()));
        visitSubElements(pageElement);

        append(HtmlUtil.createHtmlFoot());
    }

    /**
     * sets the pathToRoot and clears the content
     * 
     * @param pageElement
     */
    void initRootPage(AbstractRootPageElement pageElement) {
        setPathToRoot(pageElement.getPathToRoot());
        clear();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.faktorips.devtools.htmlexport.generators.ILayouter#layoutTextPageElement(org.faktorips
     * .devtools.htmlexport.pages.elements.core.TextPageElement)
     */
    @Override
    public void layoutTextPageElement(TextPageElement pageElement) {
        if (pageElement.getType() == TextType.WITHOUT_TYPE && pageElement.getStyles().isEmpty()) {
            append(HtmlUtil.getHtmlText(pageElement.getText()));
            return;
        }
        append(HtmlUtil.createHtmlElement(identifyTagName(pageElement), pageElement.getText(), getClasses(pageElement)));
    }

    /**
     * puts all {@link Style}s of the {@link PageElement} in a String for the html-class-attribute.
     * 
     * @param pageElement
     * @return
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
     * 
     * @param textPageElement
     * @return
     */
    private String identifyTagName(TextPageElement textPageElement) {
        return HtmlTextType.getHtmlTextTypeByTextType(textPageElement.getType()).getTagName();
    }

    /**
     * The Layouter visits the subelements of the given {@link ICompositePageElement}
     * 
     * @param compositePageElement
     */
    protected void visitSubElements(ICompositePageElement compositePageElement) {
        compositePageElement.visitSubElements(this);
    }

    /**
     * initializes the basic resources e.g. the external css-stylesheet-definitions
     */
    void initBaseResources() {
        try {
            LayoutResource cssResource = new LayoutResource(getStyleDefinitionPath(), FileHandler.readFile(
                    "org.faktorips.devtools.htmlexport", HTML_BASE_CSS)); //$NON-NLS-1$

            addLayoutResource(cssResource);

        } catch (IOException e) {
            System.out.println("Resources aren't loaded correctly: " + e.getMessage()); //$NON-NLS-1$
        }
    }

    /*
     * return relative path from root to the external css-stylesheet-definitions
     */
    String getStyleDefinitionPath() {
        return resourcePath + '/' + HTML_BASE_CSS;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.faktorips.devtools.htmlexport.generators.ILayouter#layoutImagePageElement(org.faktorips
     * .devtools.htmlexport.pages.elements.core.ImagePageElement)
     */
    @Override
    public void layoutImagePageElement(ImagePageElement imagePageElement) {

        String path = resourcePath + "/images/" + imagePageElement.getFileName() + ".png"; //$NON-NLS-1$ //$NON-NLS-2$
        addLayoutResource(new LayoutResource(path, DocumentorUtil.convertImageDataToByteArray(imagePageElement
                .getImageData(), SWT.IMAGE_PNG)));

        append(HtmlUtil.createImage(getPathToRoot() + path, imagePageElement.getTitle()));
    }

    public String getPathToRoot() {
        return pathToRoot;
    }

    public void setPathToRoot(String pathToRoot) {
        this.pathToRoot = pathToRoot;
    }

}
