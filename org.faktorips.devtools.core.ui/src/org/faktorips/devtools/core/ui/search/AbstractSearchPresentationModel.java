/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.search;

import java.beans.PropertyChangeEvent;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.faktorips.devtools.core.ui.binding.PresentationModelObject;
import org.faktorips.devtools.core.ui.search.scope.IIpsSearchScope;

/**
 * Abstract implementation of the {@link IIpsSearchPresentationModel}
 * 
 * @author dicker
 */
public abstract class AbstractSearchPresentationModel extends PresentationModelObject implements
        IIpsSearchPresentationModel {

    private IIpsSearchScope searchScope;
    private String srcFilePattern = ""; //$NON-NLS-1$

    @Override
    public void setSearchScope(IIpsSearchScope searchScope) {
        this.searchScope = searchScope;
    }

    @Override
    public IIpsSearchScope getSearchScope() {
        return searchScope;
    }

    @Override
    public abstract void store(IDialogSettings settings);

    @Override
    public abstract void read(IDialogSettings settings);

    @Override
    public String getSrcFilePattern() {
        return srcFilePattern;
    }

    @Override
    public void setSrcFilePattern(String newValue) {
        String oldValue = srcFilePattern;
        srcFilePattern = newValue;
        notifyListeners(new PropertyChangeEvent(this, SRC_FILE_PATTERN, oldValue, newValue));
    }

}
