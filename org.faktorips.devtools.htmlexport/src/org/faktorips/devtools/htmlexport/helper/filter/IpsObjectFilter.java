package org.faktorips.devtools.htmlexport.helper.filter;

import org.faktorips.devtools.core.model.ipsobject.IIpsObject;

public interface IpsObjectFilter {
    public boolean accept(IIpsObject object);
}
