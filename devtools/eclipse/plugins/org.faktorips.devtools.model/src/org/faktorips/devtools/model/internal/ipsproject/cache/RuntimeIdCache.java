/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.ipsproject.cache;

import java.util.Collection;

import org.faktorips.devtools.model.ContentChangeEvent;
import org.faktorips.devtools.model.ContentsChangeListener;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;

/**
 * A cache providing a list of all product components identified by their runtime ID. The cache is
 * automatically updated by a {@link ContentsChangeListener} when changes are detected.
 */
public class RuntimeIdCache extends ProductCmptCache {

    private final ContentsChangeListener changeListener;

    public RuntimeIdCache(IIpsProject ipsProject) {
        super(ipsProject);
        changeListener = this::update;
        ipsProject.getIpsModel().addChangeListener(changeListener);
    }

    public Collection<IIpsSrcFile> findProductCmptByRuntimeId(String runtimeId) {
        return super.findProductCmptsByKey(runtimeId);
    }

    @Override
    public void dispose() {
        super.dispose();
        getIpsProject().getIpsModel().removeChangeListener(changeListener);
    }

    @Override
    protected String getKey(IIpsSrcFile productCmpt) {
        return productCmpt.getPropertyValue(IProductCmpt.PROPERTY_RUNTIME_ID);
    }

    private void update(ContentChangeEvent event) {
        IIpsSrcFile file = event.getIpsSrcFile();
        if (isRelevantIpsSrcFile(file)) {
            if (event.getEventType() == ContentChangeEvent.TYPE_WHOLE_CONTENT_CHANGED) {
                // this is necessary as the product component may be directly changed in the
                // text editor
                synchronized (this) {
                    removeProductCmpt(file);
                    addProductCmpt(file);
                }
            } else {
                event.getPropertyChangeEvents().stream()
                        .filter(e -> IProductCmpt.PROPERTY_RUNTIME_ID.equals(e.getPropertyName()))
                        .forEach(e -> {
                            synchronized (this) {
                                removeProductCmpt((String)e.getOldValue(), event.getIpsSrcFile());
                                addProductCmpt(event.getIpsSrcFile());
                            }
                        });
            }
        }
    }

    private boolean isRelevantIpsSrcFile(IIpsSrcFile ipsSrcFile) {
        return isProductCmptSrcFile(ipsSrcFile) && ipsSrcFile.getIpsProject().equals(getIpsProject());
    }

    private boolean isProductCmptSrcFile(IIpsSrcFile ipsSrcFile) {
        return IpsObjectType.PRODUCT_CMPT.equals(ipsSrcFile.getIpsObjectType());
    }
}
