/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.internal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import java.util.Set;

import org.eclipse.e4.ui.model.application.ui.basic.MBasicFactory;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.junit.jupiter.api.Test;

class IconUriMigrationProcessorTest {

    private static final String PREFIX = "platform:/plugin/org.faktorips.devtools.core.ui/icons/"; //$NON-NLS-1$

    private final IconUriMigrationProcessor processor = new IconUriMigrationProcessor(
            Set.of(PREFIX + "ModelExplorer.svg", PREFIX + "Existing.gif", PREFIX + "Existing.svg")::contains); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    @Test
    void testMigrate_StaleGifIsReplacedBySvg() {
        MPart part = partWithIcon(PREFIX + "ModelExplorer.gif"); //$NON-NLS-1$

        processor.migrate(part);

        assertThat(part.getIconURI(), is(PREFIX + "ModelExplorer.svg")); //$NON-NLS-1$
    }

    @Test
    void testMigrate_ExistingGifIsKept() {
        MPart part = partWithIcon(PREFIX + "Existing.gif"); //$NON-NLS-1$

        processor.migrate(part);

        assertThat(part.getIconURI(), is(PREFIX + "Existing.gif")); //$NON-NLS-1$
    }

    @Test
    void testMigrate_MissingWithoutSvgIsKept() {
        MPart part = partWithIcon(PREFIX + "Unknown.gif"); //$NON-NLS-1$

        processor.migrate(part);

        assertThat(part.getIconURI(), is(PREFIX + "Unknown.gif")); //$NON-NLS-1$
    }

    @Test
    void testMigrate_SvgIsKept() {
        MPart part = partWithIcon(PREFIX + "ModelExplorer.svg"); //$NON-NLS-1$

        processor.migrate(part);

        assertThat(part.getIconURI(), is(PREFIX + "ModelExplorer.svg")); //$NON-NLS-1$
    }

    @Test
    void testMigrate_NoIcon() {
        MPart part = partWithIcon(null);

        processor.migrate(part);

        assertThat(part.getIconURI(), is(nullValue()));
    }

    private MPart partWithIcon(String iconUri) {
        MPart part = MBasicFactory.INSTANCE.createPart();
        part.setIconURI(iconUri);
        return part;
    }
}
