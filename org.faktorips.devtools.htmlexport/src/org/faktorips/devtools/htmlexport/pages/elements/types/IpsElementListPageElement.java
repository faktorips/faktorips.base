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
import java.util.Collections;
import java.util.List;

import org.faktorips.devtools.core.internal.model.ipsobject.IpsObject;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.htmlexport.context.DocumentationContext;
import org.faktorips.devtools.htmlexport.generators.WrapperType;
import org.faktorips.devtools.htmlexport.helper.filter.IpsElementFilter;
import org.faktorips.devtools.htmlexport.pages.elements.core.LinkPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.ListPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElementUtils;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextType;
import org.faktorips.devtools.htmlexport.pages.elements.core.WrapperPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.types.chooser.TypeChooserPageElement;

/**
 * Lists and links given {@link IpsObject}s in a page
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

    public IpsElementListPageElement(IIpsElement baseIpsElement, List<IIpsSrcFile> srcFiles, IpsElementFilter filter,
            DocumentationContext context, boolean shownTypeChooser) {
        super(baseIpsElement, srcFiles, filter, context);
        this.shownTypeChooser = shownTypeChooser;
        setTitle(Messages.IpsObjectListPageElement_objects);
    }

    @Override
    public void build() {
        super.build();

        addPageElements(new TextPageElement(getTitle(), TextType.HEADING_2));

        if (shownTypeChooser) {
            addPageElements(new TypeChooserPageElement(getContext(), getRelatedObjectTypes()));
        }

        addPageElements(new WrapperPageElement(WrapperType.BLOCK).addPageElements(new LinkPageElement(
                "classes", "classes", Messages.IpsObjectListPageElement_allObjects))); //$NON-NLS-1$ //$NON-NLS-2$

        List<PageElement> classes = createClassesList();

        addPageElements(new TextPageElement(classes.size() + " " + Messages.IpsObjectListPageElement_objects)); //$NON-NLS-1$

        if (classes.size() > 0) {
            addPageElements(new ListPageElement(classes));
        }
    }

    /**
     * creates a list with {@link LinkPageElement}s to the given objects.
     * 
     * @return List of {@link PageElement}s
     */
    protected List<PageElement> createClassesList() {
        Collections.sort(srcFiles, IPS_OBJECT_COMPARATOR);

        List<PageElement> items = new ArrayList<PageElement>();
        for (IIpsSrcFile srcFile : srcFiles) {
            if (!filter.accept(srcFile)) {
                continue;
            }
            PageElement link = PageElementUtils.createLinkPageElement(getContext(), srcFile, getLinkTarget(),
                    srcFile.getIpsObjectName(), true);
            items.add(link);
        }
        return items;
    }

    @Override
    protected void createId() {
        // could be overridden
    }
}