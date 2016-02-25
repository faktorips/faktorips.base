/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.ui.views.producttemplate;

import java.util.Collection;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.faktorips.devtools.core.model.productcmpt.ITemplatedValue;
import org.faktorips.devtools.core.model.productcmpt.template.TemplateValueStatus;

/** An operation to set the template value status of property values. */
public class SetTemplateValueStatusOperation extends AbstractTemplatedValueOperation {

    private final Collection<? extends ITemplatedValue> values;
    private final TemplateValueStatus status;

    public SetTemplateValueStatusOperation(Collection<? extends ITemplatedValue> values, TemplateValueStatus status) {
        super();
        this.values = values;
        this.status = status;
    }

    @Override
    public void run(IProgressMonitor monitor) throws CoreException {
        int count = values.size();
        monitor.beginTask(Messages.SetTemplateValueStatusOperation_progress, count + 10);
        for (ITemplatedValue propertyValue : values) {
            checkForSave(propertyValue);
            propertyValue.setTemplateValueStatus(status);
            monitor.worked(1);
        }
        save(new SubProgressMonitor(monitor, 10));
        monitor.done();
    }

    public static boolean isValid(Collection<ITemplatedValue> selectedValues) {
        if (selectedValues.isEmpty()) {
            return false;
        }
        for (ITemplatedValue value : selectedValues) {
            if (value.findTemplateProperty(value.getIpsProject()) == null) {
                return false;
            }
        }
        return true;
    }

}