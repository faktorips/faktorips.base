/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.test;

import org.junit.Test;

/**
 * 
 * @author Joerg Ortmann
 */
public class CmdLineIpsTestRunnerTest {

    @Test
    public void testMain() {
        CmdLineIpsTestRunner.main(new String[] { "{org/faktorips/runtime/testrepository/faktorips-repository-toc.xml}",
                "{test}" });
    }

}
