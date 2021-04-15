/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.pctype.validationrule;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Properties;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.pctype.IValidationRule;
import org.faktorips.devtools.model.plugin.IpsStatus;
import org.faktorips.util.IoUtil;

/**
 * This class implements an algorithm to import a properties file containing the validation messages
 * for {@link IValidationRule}s. The keys have to be the identification as provided by the method of
 * identification set with {@link #setMethodOfIdentification(ValidationRuleIdentification)}.
 * 
 */
public class ValidationRuleMessagesPropertiesImporter extends ValidationRuleMessagesImportOperation {

    public ValidationRuleMessagesPropertiesImporter(InputStream contents, IIpsPackageFragmentRoot root, Locale locale) {
        super(contents, root, locale);
    }

    /**
     * This method imports the messages in the give file into the objects found in the specified
     * {@link IIpsPackageFragmentRoot}. The messages are set for the specified locale.
     */
    @Override
    protected IStatus loadContent() {
        try {
            loadProperties();
            return IpsStatus.OK_STATUS;
        } catch (IOException e) {
            return new Status(IStatus.ERROR, IpsPlugin.PLUGIN_ID,
                    Messages.ValidationRuleMessagesPropertiesImporter_error_loadingPropertyFile, e);
        } finally {
            IoUtil.close(getContents());
        }

    }

    void loadProperties() throws IOException {
        Properties properties = new Properties();
        properties.load(getContents());
        setProperties(properties);
    }

    void setProperties(Properties properties) {
        HashMap<String, String> contentMap = new HashMap<>();
        for (Entry<Object, Object> pair : properties.entrySet()) {
            contentMap.put(pair.getKey().toString(), pair.getValue().toString());
        }
        setKeyValueMap(contentMap);
    }
}
