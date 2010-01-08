package org.faktorips.devtools.htmlexport.generators.html;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.htmlexport.helper.filter.IpsObjectFilter;
import org.faktorips.devtools.htmlexport.pages.elements.core.RootPageElement;

public abstract class AbstractAllPageElement extends RootPageElement {

    protected IIpsElement baseIpsElement;
    protected String linkTarget;

    protected final static IpsObjectFilter ALL_FILTER = new IpsObjectFilter() {
        public boolean accept(IIpsElement object) {
            return true;
        }
    };

    protected final static Comparator<IIpsObject> IPS_OBJECT_COMPARATOR = new Comparator<IIpsObject>() {
        public int compare(IIpsObject o1, IIpsObject o2) {
            return o1.getUnqualifiedName().compareTo(o2.getUnqualifiedName());
        }
    };

    protected List<IIpsObject> objects;
    protected IpsObjectFilter filter = ALL_FILTER;

    public AbstractAllPageElement(IIpsElement baseIpsElement, List<IIpsObject> objects, IpsObjectFilter filter) {
        super();
        this.baseIpsElement = baseIpsElement;
        this.objects = objects;
        this.filter = filter;
    }

    public AbstractAllPageElement(IIpsElement baseIpsElement, List<IIpsObject> objects) {
        super();
        this.baseIpsElement = baseIpsElement;
        this.objects = objects;
        this.filter = ALL_FILTER;
    }

    protected Set<IIpsPackageFragment> getRelatedPackageFragments() {
        Set<IIpsPackageFragment> packageFragments = new LinkedHashSet<IIpsPackageFragment>();
        for (IIpsObject object : objects) {
            if (!filter.accept(object))
                continue;
            packageFragments.add(object.getIpsPackageFragment());
        }
        return packageFragments;
    }

    public String getLinkTarget() {
        return linkTarget;
    }

    public void setLinkTarget(String linkTarget) {
        this.linkTarget = linkTarget;
    }
}