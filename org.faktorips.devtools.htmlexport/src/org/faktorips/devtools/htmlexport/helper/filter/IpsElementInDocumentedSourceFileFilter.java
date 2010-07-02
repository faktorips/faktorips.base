package org.faktorips.devtools.htmlexport.helper.filter;

import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.htmlexport.documentor.DocumentorConfiguration;

public class IpsElementInDocumentedSourceFileFilter implements IpsElementFilter {

    private final DocumentorConfiguration config;

    public IpsElementInDocumentedSourceFileFilter(DocumentorConfiguration config) {
        this.config = config;
    }

    public boolean accept(IIpsElement element) {
        if (element instanceof IIpsSrcFile) {
            return config.getDocumentedSourceFiles().contains(element);
        }
        if (element instanceof IIpsObject) {
            return config.getDocumentedSourceFiles().contains(((IIpsObject)element).getIpsSrcFile());
        }
        if (element instanceof IIpsPackageFragment) {
            return config.getLinkedPackageFragments().contains(element);
        }
        return false;
    }

}
