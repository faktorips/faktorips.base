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
import java.util.Collections;
import java.util.List;

import org.faktorips.devtools.htmlexport.context.DocumentationContext;
import org.faktorips.devtools.htmlexport.context.messages.HtmlExportMessages;
import org.faktorips.devtools.htmlexport.helper.filter.IIpsElementFilter;
import org.faktorips.devtools.htmlexport.helper.path.TargetType;
import org.faktorips.devtools.htmlexport.pages.elements.core.IPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.LinkPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.ListPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElementUtils;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextType;
import org.faktorips.devtools.htmlexport.pages.elements.core.WrapperPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.WrapperType;
import org.faktorips.devtools.htmlexport.pages.elements.types.chooser.TypeChooserPageElement;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;

/**
 * Lists and links given {@link IIpsObject}s in a page
 * 
 * @author dicker
 * 
 */
public class IpsElementListPageElement extends AbstractIpsElementListPageElement {

    private final boolean shownTypeChooser;

    public IpsElementListPageElement(IIpsElement baseIpsElement, List<IIpsSrcFile> srcFiles,
            DocumentationContext context) {
        this(baseIpsElement, srcFiles, ALL_FILTER, context, false);
    }

    public IpsElementListPageElement(IIpsElement baseIpsElement, List<IIpsSrcFile> srcFiles, IIpsElementFilter filter,
            DocumentationContext context, boolean shownTypeChooser) {
        super(baseIpsElement, srcFiles, filter, context);
        this.shownTypeChooser = shownTypeChooser;
        setTitle(getContext().getMessage(HtmlExportMessages.IpsObjectListPageElement_objects));
    }

    @Override
    protected void buildInternal() {
        super.buildInternal();

        addPageElements(new TextPageElement(getTitle(), TextType.HEADING_2, getContext()));

        if (shownTypeChooser) {
            addPageElements(new TypeChooserPageElement(getContext(), getRelatedObjectTypes()));
        }

        addPageElements(new WrapperPageElement(WrapperType.BLOCK, getContext())
                .addPageElements(new LinkPageElement(
                        "classes", TargetType.CLASSES, //$NON-NLS-1$
                        getContext().getMessage(HtmlExportMessages.IpsObjectListPageElement_allObjects),
                        getContext())));

        List<IPageElement> classes = createClassesList();

        addPageElements(new TextPageElement(classes.size()
                + " " + getContext().getMessage(HtmlExportMessages.IpsObjectListPageElement_objects), getContext())); //$NON-NLS-1$

        if (classes.size() > 0) {
            addPageElements(new ListPageElement(classes, getContext()));
        }
    }

    /**
     * creates a list with {@link LinkPageElement}s to the given objects.
     * 
     * @return List of {@link IPageElement}s
     */
    protected List<IPageElement> createClassesList() {

        Collections.sort(getSrcFiles(), IPS_OBJECT_COMPARATOR);

        List<IPageElement> items = new ArrayList<>();
        for (IIpsSrcFile srcFile : getSrcFiles()) {
            if (!getFilter().accept(srcFile)) {
                continue;
            }
            IPageElement link = new PageElementUtils(getContext()).createLinkPageElement(getContext(), srcFile,
                    getLinkTarget(), srcFile.getIpsObjectName(), true);
            items.add(link);
        }
        return items;
    }

    @Override
    protected void createId() {
        // could be overridden
    }
}
