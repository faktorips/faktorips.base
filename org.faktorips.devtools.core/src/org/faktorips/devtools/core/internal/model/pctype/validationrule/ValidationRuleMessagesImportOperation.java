/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.internal.model.pctype.validationrule;

import java.io.InputStream;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;

public class ValidationRuleMessagesImportOperation {

    private final InputStream contents;

    private final IIpsPackageFragmentRoot root;

    private final Locale locale;

    private IStatus resultStatus = new Status(IStatus.OK, IpsPlugin.PLUGIN_ID, StringUtils.EMPTY);

    public ValidationRuleMessagesImportOperation(InputStream contents, IIpsPackageFragmentRoot root, Locale locale) {
        this.contents = contents;
        this.root = root;
        this.locale = locale;
    }

    /**
     * @param resultStatus The resultStatus to set.
     */
    public void setResultStatus(IStatus resultStatus) {
        this.resultStatus = resultStatus;
    }

    /**
     * @return Returns the resultStatus.
     */
    public IStatus getResultStatus() {
        return resultStatus;
    }

    public InputStream getContents() {
        return contents;
    }

    public IIpsPackageFragmentRoot getPackageFragmentRoot() {
        return root;
    }

    public Locale getLocale() {
        return locale;
    }

}