package org.faktorips.devtools.htmlexport.pages.elements.types;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.faktorips.devtools.core.internal.model.ipsobject.IpsObject;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.htmlexport.helper.filter.IpsElementFilter;
import org.faktorips.devtools.htmlexport.pages.elements.core.LinkPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.ListPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextType;

/**
 * Lists and links given {@link IpsObject}s in a page
 * @author dicker
 *
 */
public class IpsObjectListPageElement extends AbstractListPageElement {
	/**
	 * @param baseIpsElement
	 * @param objects
	 */
	public IpsObjectListPageElement(IIpsElement baseIpsElement, List<IIpsObject> objects) {
		this(baseIpsElement, objects, ALL_FILTER);
	}

	/**
	 * @param baseIpsElement
	 * @param objects
	 * @param filter
	 */
	public IpsObjectListPageElement(IIpsElement baseIpsElement, List<IIpsObject> objects, IpsElementFilter filter) {
		super(baseIpsElement, objects, filter);
		setTitle("All Classes");
	}

	/* (non-Javadoc)
	 * @see org.faktorips.devtools.htmlexport.pages.elements.core.AbstractRootPageElement#build()
	 */
	@Override
	public void build() {
		super.build();
		addPageElements(new TextPageElement(getTitle(), TextType.HEADING_2));

		List<PageElement> classes = createClassesList();

		addPageElements(new TextPageElement(classes.size() + " Classes"));

		if (classes.size() > 0) {
			addPageElements(new ListPageElement(classes));
		}
	}

	/**
	 * creates a list with {@link LinkPageElement}s to the given objects.
	 * @return List of {@link PageElement}s
	 */
	protected List<PageElement> createClassesList() {
		Collections.sort(objects, IPS_OBJECT_COMPARATOR);

		List<PageElement> items = new ArrayList<PageElement>();
		for (IIpsObject object : objects) {
			if (!filter.accept(object))
				continue;
			PageElement link = new LinkPageElement(object, getLinkTarget(), object.getName(), true);
			items.add(link);
		}
		return items;
	}
}
