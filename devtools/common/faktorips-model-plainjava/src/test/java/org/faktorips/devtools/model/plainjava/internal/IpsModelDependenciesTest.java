/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.plainjava.internal;

import static org.junit.Assert.assertThrows;

import org.eclipse.jdt.core.JavaModelException;
import org.faktorips.devtools.model.plugin.IpsStatus;
import org.junit.Test;

public class IpsModelDependenciesTest {

    @Test
    public void test() {
        assertThrows(JavaModelException.class, () -> {
            throw new JavaModelException(new IpsStatus(
                    "When this does not work, we'll get a java.lang.NoClassDefFoundError: org/eclipse/jdt/core/JavaModelException later in dependencies"));
        });
    }

}
