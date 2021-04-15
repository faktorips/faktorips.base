/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.generators.html;

import java.io.IOException;

import org.eclipse.core.runtime.IStatus;
import org.faktorips.devtools.htmlexport.context.DocumentationContext;
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
import org.faktorips.devtools.model.plugin.IpsStatus;

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
     * the actual {@link DocumentationContext}
     */
    private final DocumentationContext context;
    /**
     * path to the resource in the generated site
     */
    private final String resourcePath;

    private final IoHandler ioHandler;
    /**
     * path from the actual RootPageElement to the root-folder of the site
     */
    private String pathToRoot;

    public HtmlLayouter(DocumentationContext context, String resourcePath) {
        this(context, resourcePath, new FileHandler());
    }

    public HtmlLayouter(DocumentationContext context, String resourcePath, IoHandler ioHandler) {
        this.ioHandler = ioHandler;
        this.resourcePath = resourcePath;
        this.context = context;
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
            LayoutResource cssResource = new LayoutResource(getStyleDefinitionPath(),
                    ioHandler.readFile(HTML_BASE_CSS));

            addLayoutResource(cssResource);

        } catch (IOException e) {
            getContext().addStatus(new IpsStatus(IStatus.WARNING, "Resources aren't loaded correctly", e)); //$NON-NLS-1$
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

    public DocumentationContext getContext() {
        return context;
    }
}
