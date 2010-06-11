package org.faktorips.devtools.htmlexport.pages.standard;

import org.faktorips.devtools.core.model.enums.IEnumContent;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.htmlexport.documentor.DocumentorConfiguration;

/**
 * A complete page representing an {@link IEnumContent}
 * 
 * @author dicker
 * 
 */
public class IpsObjectContentPageElement extends AbstractObjectContentPageElement<IIpsObject> {

    protected IpsObjectContentPageElement(IIpsObject documentedIpsObject, DocumentorConfiguration config) {
        super(documentedIpsObject, config);
    }

}
