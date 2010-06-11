package org.faktorips.devtools.htmlexport.helper.filter;

import org.faktorips.devtools.core.internal.model.IpsElement;
import org.faktorips.devtools.core.model.IIpsElement;

/**
 * Filter for {@link IpsElement}s
 * 
 * @author dicker
 * 
 */
public interface IpsElementFilter {
    /**
     * @param element
     * @return true, if the given IIpsElement fullfills the conditions of the filter
     */
    public boolean accept(IIpsElement element);

}
