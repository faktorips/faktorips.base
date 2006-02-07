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
