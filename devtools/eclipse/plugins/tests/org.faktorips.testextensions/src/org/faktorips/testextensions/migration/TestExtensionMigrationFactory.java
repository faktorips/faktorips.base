/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.testextensions.migration;

import org.faktorips.devtools.core.migration.MigrationForChangedAttribute;
import org.faktorips.devtools.core.migration.MigrationForChangedAttribute.ChangedAttribute;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.versionmanager.AbstractIpsProjectMigrationOperation;
import org.faktorips.devtools.model.versionmanager.IIpsProjectMigrationOperationFactory;

public class TestExtensionMigrationFactory implements IIpsProjectMigrationOperationFactory {

    @Override
    public AbstractIpsProjectMigrationOperation createIpsProjectMigrationOpertation(IIpsProject ipsProject,
            String featureId) {
        // use your model version here, this has nothing to do with the Faktor-IPS version
        // the version and the description will be shown in the migration wizard
        return new MigrationForChangedAttribute(ipsProject, featureId, "1.0.1",
                "This migration renames all Attributes from xyzA to xyzB.",
                new ChangedAttribute("base.Vertrag", "AttributeA", "AttributeB"),
                new ChangedAttribute("base.Produkt", "ProduktAttributeA", "ProduktAttributeB"),
                new ChangedAttribute("extended.ExtendingVertrag", "AttributeA", "AttributeB"),
                new ChangedAttribute("extended.ExtendingVertrag", "OtherAttributeA", "OtherAttributeB"),
                new ChangedAttribute("extended.ExtendingProdukt", "ProduktAttributeA", "ProduktAttributeB"),
                new ChangedAttribute("extended.ExtendingProdukt", "ProduktOtherAttributeA", "ProduktOtherAttributeB"));
    }
}
