/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.internal.model.datatype;

import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.model.datatype.DatatypeHelperFactory;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSetConfig;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.util.ArgumentCheck;

public abstract class AbstractDateHelperFactory<T extends Datatype> implements DatatypeHelperFactory {

    public static final String CONFIG_PROPERTY_LOCAL_DATE_HELPER_VARIANT = "localDateDatatypeHelperVariant"; //$NON-NLS-1$

    private final Class<T> datatypeClass;

    public AbstractDateHelperFactory(Class<T> datatypeClass) {
        super();
        this.datatypeClass = datatypeClass;
    }

    @Override
    public DatatypeHelper createDatatypeHelper(Datatype datatype, IIpsProject ipsProject) {
        ArgumentCheck.isInstanceOf(datatype, datatypeClass);
        IIpsArtefactBuilderSet builderSet = ipsProject.getIpsArtefactBuilderSet();
        IIpsArtefactBuilderSetConfig config = builderSet.getConfig();
        LocalDateHelperVariant variant = LocalDateHelperVariant.fromString(config
                .getPropertyValueAsString(CONFIG_PROPERTY_LOCAL_DATE_HELPER_VARIANT));
        return createDatatypeHelper(datatypeClass.cast(datatype), variant);

    }

    abstract DatatypeHelper createDatatypeHelper(T datatype, LocalDateHelperVariant variant);

}
