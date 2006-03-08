/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.search;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.text.AbstractTextSearchResult;
import org.eclipse.search.ui.text.IEditorMatchAdapter;
import org.eclipse.search.ui.text.IFileMatchAdapter;

public class ReferencesToProductSearchResult extends AbstractTextSearchResult {

    private ReferencesToProductSearchQuery query;
    
    public ReferencesToProductSearchResult(ReferencesToProductSearchQuery query) {
        this.query = query;
    }
    
    public String getLabel() {
        return "" + super.getMatchCount() + Messages.ReferencesToProductSearchResult_label + this.query.getReferencedName(); //$NON-NLS-1$
    }

    public String getTooltip() {
        return null;
    }

    public ImageDescriptor getImageDescriptor() {
        return null;
    }

    public ISearchQuery getQuery() {
        return this.query;
    }

    public IEditorMatchAdapter getEditorMatchAdapter() {
        return null;
    }

    public IFileMatchAdapter getFileMatchAdapter() {
        return null;
    }
    
}
