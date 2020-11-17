/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.helper.filter;

import org.faktorips.devtools.htmlexport.context.DocumentationContext;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;

/**
 * {@link IIpsElementFilter}, which accepts an {@link IIpsElement}, if it is documented according to
 * the given {@link DocumentationContext}
 * 
 * @author dicker
 */
public class IpsElementInDocumentedSourceFileFilter implements IIpsElementFilter {

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
