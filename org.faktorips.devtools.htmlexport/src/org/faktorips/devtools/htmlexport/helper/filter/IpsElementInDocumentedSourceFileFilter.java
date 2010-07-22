/*******************************************************************************
 * Copyright © 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen, 
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 ******************************************************************************/
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

    @Override
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
