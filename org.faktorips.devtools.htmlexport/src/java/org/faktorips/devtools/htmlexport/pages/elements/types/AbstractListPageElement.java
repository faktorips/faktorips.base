package org.faktorips.devtools.htmlexport.pages.elements.types;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.faktorips.devtools.core.internal.model.ipsobject.IpsObject;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.htmlexport.helper.filter.IpsElementFilter;
import org.faktorips.devtools.htmlexport.helper.path.PathUtilFactory;
import org.faktorips.devtools.htmlexport.pages.elements.core.AbstractRootPageElement;

/**
 * Creates a list with links to the pages of the given {@link IpsObject}s. The {@link IpsObject}s will be filtered and sorted on the page.
 * @author dicker
 *
 */
public abstract class AbstractListPageElement extends AbstractRootPageElement {

    protected IIpsElement baseIpsElement;
    protected String linkTarget;
    protected List<IIpsObject> objects;
    protected IpsElementFilter filter = ALL_FILTER;

    /**
     * {@link IpsElementFilter}, which accepts all {@link IIpsElement}s
     */
    protected final static IpsElementFilter ALL_FILTER = new IpsElementFilter() {
        public boolean accept(IIpsElement object) {
            return true;
        }
    };

    /**
     * {@link Comparator}, which is used for sorting the {@link IIpsObject}s according to their unqualified name.
     */
    protected final static Comparator<IIpsObject> IPS_OBJECT_COMPARATOR = new Comparator<IIpsObject>() {
        public int compare(IIpsObject o1, IIpsObject o2) {
            return o1.getUnqualifiedName().compareTo(o2.getUnqualifiedName());
        }
    };


    /**
     * creates an {@link AbstractListPageElement}
     * @param baseIpsElement ipsElement, which represents the location of the page for links from the page
     * @param objects unfiltered and unsorted objects to list on the page
     * @param filter for objects  
     */
    public AbstractListPageElement(IIpsElement baseIpsElement, List<IIpsObject> objects, IpsElementFilter filter) {
        super();
        this.baseIpsElement = baseIpsElement;
        this.objects = objects;
        this.filter = filter;
    }

    /**
     * 
     * @see AbstractListPageElement(IIpsElement baseIpsElement, List<IIpsObject> objects, IpsElementFilter filter) 
     */
    public AbstractListPageElement(IIpsElement baseIpsElement, List<IIpsObject> objects) {
        this(baseIpsElement, objects, ALL_FILTER);
    }

    /**
     * @return the {@link IIpsPackageFragment}s of all filtered objects
     */
    protected Set<IIpsPackageFragment> getRelatedPackageFragments() {
        Set<IIpsPackageFragment> packageFragments = new LinkedHashSet<IIpsPackageFragment>();
        for (IIpsObject object : objects) {
            if (!filter.accept(object))
                continue;
            packageFragments.add(object.getIpsPackageFragment());
        }
        return packageFragments;
    }

    /**
     * @return the target for all links
     */
    public String getLinkTarget() {
        return linkTarget;
    }

    /**
     * sets the target for all links
     * @param linkTarget
     */
    public void setLinkTarget(String linkTarget) {
        this.linkTarget = linkTarget;
    }

	/* (non-Javadoc)
	 * @see org.faktorips.devtools.htmlexport.pages.elements.core.AbstractRootPageElement#getPathToRoot()
	 */
	@Override
	public String getPathToRoot() {
		return PathUtilFactory.createPathUtil(baseIpsElement).getPathToRoot();
	}
    
    
}