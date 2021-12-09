/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.builder;

import java.lang.Runtime.Version;
import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.method.IParameter;

/**
 * A collection of static helper methods.
 * 
 * @author Jan Ortmann
 */
public class BuilderHelper {

    private BuilderHelper() {
        super();
    }

    public static final String[] extractParameterNames(IParameter[] params) {
        String[] paramNames = new String[params.length];
        for (int i = 0; i < params.length; i++) {
            paramNames[i] = params[i].getName();
        }
        return paramNames;
    }

    public static final String[] extractMessageParameters(String message) {
        ArrayList<String> al = new ArrayList<>();
        String msg = message;
        while (true) {
            int start = msg.indexOf('{');
            if (start > -1) {
                // parameter may not be an empty string
                int end = msg.indexOf('}', start + 2);
                if (end > -1) {
                    String param = msg.substring(start + 1, end);
                    param = param.trim();
                    param = param.replace(' ', '_');
                    param = StringUtils.uncapitalize(param);
                    al.add(param);
                    msg = msg.substring(end + 1);
                } else {
                    break;
                }
            } else {
                break;
            }
        }
        return al.toArray(new String[al.size()]);
    }

    public static final String transformMessage(String message) {
        int count = 0;
        String transformedMessage = ""; //$NON-NLS-1$
        String msg = message;
        while (true) {
            int start = msg.indexOf('{');
            if (start > -1) {
                // parameter may not be an empty string
                int end = msg.indexOf('}', start + 2);
                if (end > -1) {
                    transformedMessage += msg.substring(0, start);
                    transformedMessage += "{"; //$NON-NLS-1$
                    transformedMessage += count;
                    transformedMessage += "}"; //$NON-NLS-1$
                    msg = msg.substring(end + 1);
                    count++;
                } else {
                    transformedMessage += msg;
                    break;
                }
            } else {
                transformedMessage += msg;
                break;
            }
        }
        return transformedMessage;
    }

    /**
     * Returns true if the compliance level of the java project that is associated with the provided
     * IPS project is equal or greater that 1.5. Otherwise false is returned.
     */
    public static final boolean javaComplianceGreateEqual5(IIpsProject ipsProject) {
        Version sourceVersion = ipsProject.getJavaProject().getSourceVersion();
        return sourceVersion.compareToIgnoreOptional(Runtime.Version.parse("1.5")) >= 0; //$NON-NLS-1$
    }
}
