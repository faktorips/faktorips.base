/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

import org.faktorips.devtools.htmlexport.generators.AbstractLayouter;
import org.faktorips.devtools.htmlexport.generators.LayoutResource;
import org.faktorips.devtools.htmlexport.generators.html.elements.HtmlCompositePageElementLayouter;
import org.faktorips.devtools.htmlexport.generators.html.elements.HtmlImagePageElementLayouter;
import org.faktorips.devtools.htmlexport.generators.html.elements.HtmlLinkPageElementLayouter;
import org.faktorips.devtools.htmlexport.generators.html.elements.HtmlListPageElementLayouter;
import org.faktorips.devtools.htmlexport.generators.html.elements.HtmlRootPageElementLayouter;
import org.faktorips.devtools.htmlexport.generators.html.elements.HtmlTablePageElementLayouter;
import org.faktorips.devtools.htmlexport.generators.html.elements.HtmlTextPageElementLayouter;
import org.faktorips.devtools.htmlexport.helper.FileHandler;
import org.faktorips.devtools.htmlexport.helper.IoHandler;
import org.faktorips.devtools.htmlexport.pages.elements.core.AbstractCompositePageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.AbstractRootPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.ICompositePageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.ImagePageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.LinkPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.ListPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextPageElement;
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

    private final String resourcePath;

    /**
     * path from the actual RootPageElement to the root-folder of the site
     */
    private String pathToRoot;
    private final IoHandler ioHandler;

    public HtmlLayouter(String resourcePath) {
        this(resourcePath, new FileHandler());
    }

    public HtmlLayouter(String resourcePath, IoHandler ioHandler) {
        this.ioHandler = ioHandler;
        this.resourcePath = resourcePath;
        initBaseResources();
    }

    @Override
    public void layoutLinkPageElement(LinkPageElement pageElement) {
        new HtmlLinkPageElementLayouter(pageElement, this).layout();
    }

    @Override
    public void layoutListPageElement(ListPageElement pageElement) {
        new HtmlListPageElementLayouter(pageElement, this).layout();
    }

    @Override
    public void layoutTablePageElement(TablePageElement pageElement) {
        new HtmlTablePageElementLayouter(pageElement, this).layout();
    }

    @Override
    public void layoutWrapperPageElement(AbstractCompositePageElement pageElement) {
        new HtmlCompositePageElementLayouter(pageElement, this).layout();
    }

    @Override
    public void layoutRootPageElement(AbstractRootPageElement pageElement) {
        new HtmlRootPageElementLayouter(pageElement, this).layout();
    }

    @Override
    public void layoutTextPageElement(TextPageElement pageElement) {
        new HtmlTextPageElementLayouter(pageElement, this).layout();
    }

    @Override
    public void layoutImagePageElement(ImagePageElement pageElement) {
        new HtmlImagePageElementLayouter(pageElement, this).layout();
    }

    /**
     * creates the relative href from the actual page to the linked page
     */
    public String createLinkBase(LinkPageElement pageElement) {
        return getPathToRoot() + pageElement.getPathFromRoot() + ".html"; //$NON-NLS-1$
    }

    /**
     * sets the pathToRoot and clears the content
     * 
     */
    public void initRootPage(AbstractRootPageElement pageElement) {
        setPathToRoot(pageElement.getPathToRoot());
        clear();
    }

    /**
     * The Layouter visits the subelements of the given {@link ICompositePageElement}
     * 
     */
    public void visitSubElements(ICompositePageElement compositePageElement) {
        compositePageElement.visitSubElements(this);
    }

    /**
     * initializes the basic resources e.g. the external css-stylesheet-definitions
     */
    void initBaseResources() {
        try {
            LayoutResource cssResource = new LayoutResource(getStyleDefinitionPath(), ioHandler.readFile(
                    "org.faktorips.devtools.htmlexport", HTML_BASE_CSS)); //$NON-NLS-1$

            addLayoutResource(cssResource);

        } catch (IOException e) {

            System.out.println("Resources aren't loaded correctly: " + e.getMessage()); //$NON-NLS-1$
        }
    }

    /**
     * returns the relative path from root to the external css-stylesheet-definitions
     */
    public String getStyleDefinitionPath() {
        return resourcePath + '/' + HTML_BASE_CSS;
    }

    public String getPathToRoot() {
        return pathToRoot;
    }

    public void setPathToRoot(String pathToRoot) {
        this.pathToRoot = pathToRoot;
    }

    public String getResourcePath() {
        return resourcePath;
    }
}
