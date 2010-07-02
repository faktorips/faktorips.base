package org.faktorips.devtools.htmlexport.pages.elements.types;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.faktorips.devtools.core.internal.model.ipsobject.IpsObject;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.htmlexport.documentor.DocumentorConfiguration;
import org.faktorips.devtools.htmlexport.generators.WrapperType;
import org.faktorips.devtools.htmlexport.helper.filter.IpsElementFilter;
import org.faktorips.devtools.htmlexport.pages.elements.core.LinkPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.ListPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElementUtils;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextType;
import org.faktorips.devtools.htmlexport.pages.elements.core.WrapperPageElement;

/**
 * Lists and links given {@link IpsObject}s in a page
 * 
 * @author dicker
 * 
 */
public class IpsObjectListPageElement extends AbstractListPageElement {
    /**
     * @param baseIpsElement
     * @param srcFiles
     * @param config
     */
    public IpsObjectListPageElement(IIpsElement baseIpsElement, List<IIpsSrcFile> srcFiles,
            DocumentorConfiguration config) {
        this(baseIpsElement, srcFiles, ALL_FILTER, config);
    }

    /**
     * @param baseIpsElement
     * @param srcFiles
     * @param filter
     * @param config
     */
    public IpsObjectListPageElement(IIpsElement baseIpsElement, List<IIpsSrcFile> srcFiles, IpsElementFilter filter,
            DocumentorConfiguration config) {
        super(baseIpsElement, srcFiles, filter, config);
        setTitle(Messages.IpsObjectListPageElement_objects);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.faktorips.devtools.htmlexport.pages.elements.core.AbstractRootPageElement#build()
     */
    @Override
    public void build() {
        super.build();
        addPageElements(new TextPageElement(getTitle(), TextType.HEADING_2));

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
            PageElement link = PageElementUtils.createLinkPageElement(getConfig(), srcFile, getLinkTarget(), srcFile
                    .getName(), true);
            items.add(link);
        }
        return items;
    }

    @Override
    protected void createId() {
    }
}
