/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.builder;

import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.core.JavaCore;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.method.IParameter;
import org.faktorips.values.Decimal;

/**
 * A collection of static helper methods.
 * 
 * @author Jan Ortmann
 */
public class BuilderHelper {

    public final static String[] extractParameterNames(IParameter[] params) {
        String[] paramNames = new String[params.length];
        for (int i = 0; i < params.length; i++) {
            paramNames[i] = params[i].getName();
        }
        return paramNames;
    }

    public final static String[] extractMessageParameters(String message) {
        ArrayList<String> al = new ArrayList<String>();
        while (true) {
            int start = message.indexOf('{');
            if (start > -1) {
                int end = message.indexOf('}', start + 2); // param may not be empty string
                if (end > -1) {
                    String param = message.substring(start + 1, end);
                    param = param.trim();
                    param = param.replace(' ', '_');
                    param = StringUtils.uncapitalize(param);
                    al.add(param);
                    message = message.substring(end + 1);
                } else {
                    break;
                }
            } else {
                break;
            }
        }
        return al.toArray(new String[al.size()]);
    }

    public final static String transformMessage(String message) {
        int count = 0;
        String transformedMessage = ""; //$NON-NLS-1$
        while (true) {
            int start = message.indexOf('{');
            if (start > -1) {
                int end = message.indexOf('}', start + 2); // parameter may not be an empty string.
                if (end > -1) {
                    transformedMessage += message.substring(0, start);
                    transformedMessage += "{"; //$NON-NLS-1$
                    transformedMessage += count;
                    transformedMessage += "}"; //$NON-NLS-1$
                    message = message.substring(end + 1);
                    count++;
                } else {
                    transformedMessage += message;
                    break;
                }
            } else {
                transformedMessage += message;
                break;
            }
        }
        return transformedMessage;
    }

    /**
     * Returns true if the compliance level of the java project that is associated with the provided
     * IPS project is equal or greater that 1.5. Otherwise false is returned.
     */
    public final static boolean javaComplianceGreateEqual5(IIpsProject ipsProject) {
        Decimal complianceLevel = Decimal.valueOf(ipsProject.getJavaProject().getOption(JavaCore.COMPILER_COMPLIANCE,
                true));
        if (complianceLevel.greaterThanOrEqual(Decimal.valueOf(15, 1))) {
            return true;
        }
        return false;
    }

    private BuilderHelper() {
        super();
    }

}
