/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.maven.plugin.validation.abstraction;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;

import java.util.function.Supplier;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.logging.Log;
import org.faktorips.devtools.model.versionmanager.EmptyIpsFeatureVersionManager;
import org.faktorips.devtools.model.versionmanager.IIpsFeatureVersionManager;
import org.junit.jupiter.api.Test;

class MavenIpsModelExtensionsTest {

    @Test
    void testGetIpsFeatureVersionManager_IPS() {
        MavenSession session = mock(MavenSession.class);
        Log log = mock(Log.class);
        Supplier<Log> logger = () -> log;
        MavenIpsModelExtensions mavenIpsModelExtensions = new MavenIpsModelExtensions(session, logger);

        IIpsFeatureVersionManager ipsFeatureVersionManager = mavenIpsModelExtensions
                .getIpsFeatureVersionManager("org.faktorips.feature");

        assertThat(ipsFeatureVersionManager, is(not(nullValue())));
        assertThat(ipsFeatureVersionManager.getFeatureId(), is("org.faktorips.feature"));
        assertThat(ipsFeatureVersionManager, is(EmptyIpsFeatureVersionManager.INSTANCE));
    }

    @Test
    void testGetIpsFeatureVersionManager_Unknown() {
        MavenSession session = mock(MavenSession.class);
        Log log = mock(Log.class);
        Supplier<Log> logger = () -> log;
        MavenIpsModelExtensions mavenIpsModelExtensions = new MavenIpsModelExtensions(session, logger);

        IIpsFeatureVersionManager ipsFeatureVersionManager = mavenIpsModelExtensions
                .getIpsFeatureVersionManager("unknown.id");

        assertThat(ipsFeatureVersionManager, is(not(nullValue())));
        assertThat(ipsFeatureVersionManager.getFeatureId(), is("unknown.id"));
        assertThat(ipsFeatureVersionManager.isCurrentVersionCompatibleWith("47.11"), is(true));
    }

}
