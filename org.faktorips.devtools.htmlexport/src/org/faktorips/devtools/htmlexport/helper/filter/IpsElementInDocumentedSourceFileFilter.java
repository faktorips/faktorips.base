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

package org.faktorips.devtools.htmlexport.helper.filter;

import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.htmlexport.context.DocumentationContext;

/**
 * {@link IpsElementFilter}, which accepts an {@link IIpsElement}, if it is documented according to
 * the given {@link DocumentationContext}
 * 
 * @author dicker
 */
public class IpsElementInDocumentedSourceFileFilter implements IpsElementFilter {

    private final DocumentationContext context;

    public IpsElementInDocumentedSourceFileFilter(DocumentationContext context) {
        this.context = context;
    }

    @Override
    public boolean accept(IIpsElement element) {
        if (element instanceof IIpsSrcFile) {
            return context.getDocumentedSourceFiles().contains(element);
        }
        if (element instanceof IIpsObject) {
            return context.getDocumentedSourceFiles().contains(((IIpsObject)element).getIpsSrcFile());
        }
        if (element instanceof IIpsPackageFragment) {
            return context.getLinkedPackageFragments().contains(element);
        }
        return false;
    }

}
