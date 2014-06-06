/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.ui.controller.fields.enumproposal;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.faktorips.devtools.core.model.productcmpt.IConfigElement;
import org.faktorips.devtools.core.model.valueset.IValueSetOwner;
import org.faktorips.devtools.core.ui.controller.fields.IValueSource;

/**
 * A {@link IValueSource} for the {@link EnumerationProposalProvider}. Reads all values from another
 * value source and adds <code>null</code> to the list if (both conditions must be met):
 * <ul>
 * <li>it does not contain <code>null</code></li>
 * <li>the valueSetOwner is a {@link IConfigElement}.</li>
 * </ul>
 */
public class EnsureContainsNullForConfigElementValueSource implements IValueSource {

    private final IValueSource valueSource;
    private final IValueSetOwner valueSetOwner;

    public EnsureContainsNullForConfigElementValueSource(IValueSetOwner valueSetOwner, IValueSource valueSource) {
        this.valueSetOwner = valueSetOwner;
        Assert.isNotNull(valueSource);
        this.valueSource = valueSource;
    }

    @Override
    public List<String> getValues() {
        List<String> values = valueSource.getValues();
        return addNullIfNeccessary(values);
    }

    private List<String> addNullIfNeccessary(List<String> values) {
        if (requiresNull(values)) {
            return valuesAndNull(values);
        } else {
            return values;
        }
    }

    private boolean requiresNull(List<String> values) {
        return isConfigElement() && !values.contains(null);
    }

    private boolean isConfigElement() {
        return valueSetOwner instanceof IConfigElement;
    }

    private List<String> valuesAndNull(List<String> values) {
        List<String> result = new ArrayList<String>();
        result.addAll(values);
        result.add(null);
        return result;
    }

}
