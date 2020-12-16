/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.ipsproject;

import java.util.Collection;

import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.ContentsChangeListener;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;

/**
 * A cache providing a list of all product components identified by their runtime ID. The cache is
 * automatically updated by a {@link ContentsChangeListener} when changes are detected.
 */
public class RuntimeIdCache extends ProductCmptCache {

    private final RuntimeIdChangeUpdater changeListener;

    public RuntimeIdCache(IpsProject ipsProject) {
        super(ipsProject);
        changeListener = new RuntimeIdChangeUpdater(this);
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

    private static final class RuntimeIdChangeUpdater implements ContentsChangeListener {

        private RuntimeIdCache cache;

        public RuntimeIdChangeUpdater(RuntimeIdCache cache) {
            this.cache = cache;
        }

        @Override
        public void contentsChanged(ContentChangeEvent event) {
            IIpsSrcFile file = event.getIpsSrcFile();
            if (isRelevantIpsSrcFile(file)) {
                if (event.getEventType() == ContentChangeEvent.TYPE_WHOLE_CONTENT_CHANGED) {
                    // this is necessary as the product component may be directly changed in the
                    // text editor
                    synchronized (cache) {
                        cache.removeProductCmpt(file);
                        cache.addProductCmpt(file);
                    }
                } else {
                    event.getPropertyChangeEvents().stream()
                            .filter(e -> IProductCmpt.PROPERTY_RUNTIME_ID.equals(e.getPropertyName()))
                            .forEach(e -> {
                                synchronized (cache) {
                                    cache.removeProductCmpt((String)e.getOldValue(), event.getIpsSrcFile());
                                    cache.addProductCmpt(event.getIpsSrcFile());
                                }
                            });
                }
            }
        }

        private boolean isRelevantIpsSrcFile(IIpsSrcFile ipsSrcFile) {
            return isProductCmptSrcFile(ipsSrcFile) && ipsSrcFile.getIpsProject().equals(cache.getIpsProject());
        }

        private boolean isProductCmptSrcFile(IIpsSrcFile ipsSrcFile) {
            return IpsObjectType.PRODUCT_CMPT.equals(ipsSrcFile.getIpsObjectType());
        }

    }
}
