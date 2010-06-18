package org.faktorips.devtools.htmlexport.helper.filter;

import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.htmlexport.documentor.DocumentorConfiguration;

public class IpsObjectInLinkedObjectsFilter implements IpsElementFilter {

    private final DocumentorConfiguration config;

    public IpsObjectInLinkedObjectsFilter(DocumentorConfiguration config) {
        this.config = config;
    }

    public boolean accept(IIpsElement element) {
        if (element instanceof IIpsObject) {
            return config.getLinkedObjects().contains(element);
        }
        if (element instanceof IIpsPackageFragment) {
            return config.getLinkedPackageFragments().contains(element);
        }
        return false;
    }

}
