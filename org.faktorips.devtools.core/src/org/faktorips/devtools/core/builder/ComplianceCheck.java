/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.builder;

import org.eclipse.jdt.core.JavaCore;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.values.Decimal;

/**
 * A class that provides static methods to check against the Java compiler's compliance level.
 * 
 * @author Daniel Hohenberger
 */
public class ComplianceCheck {

    /**
     * @return <code>true</code> if the compliance level is set to at least 1.5, <code>false</code>
     *         if it is set lower or not set at all.
     */
    public static boolean isComplianceLevelAtLeast5(IIpsProject project) {
        String complianceLevel = project.getJavaProject().getOption(JavaCore.COMPILER_COMPLIANCE, true);
        if (complianceLevel != null) {
            return Decimal.valueOf(complianceLevel).greaterThanOrEqual(Decimal.valueOf("1.5")); //$NON-NLS-1$
        }
        return false; // Assume old Java 1.4 project if compliance level is not set.
    }

    /**
     * Returns <code>true</code> if the compliance level of the java project that is associated with
     * the provided {@link IIpsProject} is greater than 1.5. Returns <code>null</code> if the
     * compliance level could not be determined.
     * 
     * @see JavaCore#COMPILER_COMPLIANCE
     */
    public static Boolean isComplianceLevelGreaterJava5(IIpsProject project) {
        String complianceLevel = project.getJavaProject().getOption(JavaCore.COMPILER_COMPLIANCE, true);
        if (complianceLevel != null) {
            return Decimal.valueOf(complianceLevel).greaterThan(Decimal.valueOf("1.5")); //$NON-NLS-1$
        }
        return null;
    }

    /**
     * Returns <code>true</code> if the compliance level of the java project that is associated with
     * the provided {@link IIpsProject} is 1.5. Returns <code>null</code> if the compliance level
     * could not be determined.
     * 
     * @see JavaCore#COMPILER_COMPLIANCE
     */
    public static Boolean isComplianceLevel5(IIpsProject project) {
        String complianceLevel = project.getJavaProject().getOption(JavaCore.COMPILER_COMPLIANCE, true);
        if (complianceLevel != null) {
            return Decimal.valueOf(complianceLevel).equals(Decimal.valueOf("1.5")); //$NON-NLS-1$
        }
        return null;
    }

}
