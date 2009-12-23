package org.faktorips.devtools.htmlexport.helper.filter;

import org.faktorips.devtools.core.model.IIpsElement;

public interface IpsObjectFilter {
    public boolean accept(IIpsElement element);
}
