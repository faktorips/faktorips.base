/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui;

import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.ipsobject.ILabeledElement;

public class LocalizedLabelProvider extends DefaultLabelProvider {

    @Override
    public String getText(Object element) {
        if (element instanceof ILabeledElement) {
            ILabeledElement labeledElement = (ILabeledElement)element;
            return IIpsModel.get().getMultiLanguageSupport().getLocalizedLabel(labeledElement);
        }
        return super.getText(element);
    }

}
