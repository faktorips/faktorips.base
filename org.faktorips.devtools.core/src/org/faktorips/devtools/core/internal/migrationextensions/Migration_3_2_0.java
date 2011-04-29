/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.internal.migrationextensions;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.internal.migration.DefaultMigration;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectGeneration;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.model.versionmanager.AbstractIpsProjectMigrationOperation;
import org.faktorips.devtools.core.model.versionmanager.IIpsProjectMigrationOperationFactory;
import org.faktorips.runtime.CardinalityRange;
import org.faktorips.valueset.IntegerRange;

/**
 * Migration to version 3.2.0.ms1.
 * <p/>
 * Changes all ProductCmpt-XML files by adding a default cardinality to all ProductCmptLinks. The
 * value is set to the current minimum cardinality.
 * <p/>
 * <em>IMPORTANT:</em> the code generators will be updated with this migration changing the
 * signature of several generated methods. From now on the following methods will use a
 * {@link CardinalityRange} instead of an {@link IntegerRange}.
 * <ul>
 * <li>add"AssociationName"(I"ChildProdCmptTypeName" child, CardinalityRange cardinality)</li> will
 * require a CardinalityRange as an argument.
 * <li>getCardinalityFor"AssociationName"(I"ChildProdCmptTypeName" child) will return an instance of
 * CardinalityRange.</li>
 * </ul>
 * The {@link CardinalityRange} holds a default cardinality in addition to the upper and lower bound
 * defined by an {@link IntegerRange}.
 * 
 * @author Stefan Widmaier
 */
public class Migration_3_2_0 extends DefaultMigration {

    public Migration_3_2_0(IIpsProject projectToMigrate, String featureId) {
        super(projectToMigrate, featureId);
    }

    @Override
    protected void migrate(IIpsSrcFile srcFile) throws CoreException {
        if (srcFile.getIpsObjectType().equals(IpsObjectType.PRODUCT_CMPT)) {
            IIpsObject ipsObject = srcFile.getIpsObject();
            if (migrateProdCmpt((IProductCmpt)ipsObject)) {
                srcFile.markAsDirty();
            }
        }
    }

    private boolean migrateProdCmpt(IProductCmpt prodCmpt) {
        List<IIpsObjectGeneration> gens = prodCmpt.getGenerations();
        boolean result = false;
        for (IIpsObjectGeneration gen : gens) {
            IProductCmptLink[] links = ((IProductCmptGeneration)gen).getLinks();
            for (IProductCmptLink link : links) {
                link.setDefaultCardinality(link.getMinCardinality());
                result = true;
            }
        }
        return result;
    }

    @Override
    protected boolean migrate(IFile file) throws CoreException {
        return false;
    }

    @Override
    public String getDescription() {
        return "Changes all ProductCmpt-XML files by adding a default cardinality to all ProductCmptLinks. The value is set to the currently defined minimum cardinality. The code generators will be updated with this migration. Thereby the Method signature for adding child-Instances to an object changes. From now on add<ProdCmptTypeName>(I<ProdCmptTypeName> child, IntegerRange cardinality) will require a CardinalityRange instead of an IntegerRange. The CardinalityRange holds a default cardinality in addition to the upper and lower bound defined by an IntegerRange."; //$NON-NLS-1$
    }

    @Override
    public String getTargetVersion() {
        return "3.2.0"; //$NON-NLS-1$
    }

    public static class Factory implements IIpsProjectMigrationOperationFactory {

        @Override
        public AbstractIpsProjectMigrationOperation createIpsProjectMigrationOpertation(IIpsProject ipsProject,
                String featureId) {
            return new Migration_3_2_0(ipsProject, featureId);
        }

    }

}
