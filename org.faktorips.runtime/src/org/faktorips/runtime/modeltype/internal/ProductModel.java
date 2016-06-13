/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.modeltype.internal;

import java.util.ArrayList;
import java.util.List;

import org.faktorips.runtime.IModelObject;
import org.faktorips.runtime.IProductComponent;
import org.faktorips.runtime.model.Models;
import org.faktorips.runtime.model.annotation.AnnotatedType;
import org.faktorips.runtime.model.annotation.IpsChangingOverTime;
import org.faktorips.runtime.model.annotation.IpsConfigures;
import org.faktorips.runtime.modeltype.IModelType;
import org.faktorips.runtime.modeltype.IPolicyModel;
import org.faktorips.runtime.modeltype.IProductModel;
import org.faktorips.runtime.modeltype.ITableUsageModel;
import org.faktorips.runtime.modeltype.TypeHierarchyVisitor;

public class ProductModel extends ModelType implements IProductModel {

    public ProductModel(String name, AnnotatedType annotatedModelType) {
        super(name, annotatedModelType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isChangingOverTime() {
        return getAnnotatedModelType().is(IpsChangingOverTime.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isConfigurationForPolicyCmptType() {
        return getAnnotatedModelType().is(IpsConfigures.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IPolicyModel getPolicyCmptType() {
        return Models.getPolicyModel(getAnnotatedModelType().get(IpsConfigures.class).value()
                .asSubclass(IModelObject.class));
    }

    @Override
    public IProductModel getSuperType() {
        Class<?> superclass = getJavaClass().getSuperclass();
        return Models.isProductModel(superclass) ? Models.getProductModel(superclass
                .asSubclass(IProductComponent.class)) : null;
    }

    @Override
    public ITableUsageModel getTableUsage(String name) {
        return getModelTypeParts().getTableUsageModel(name);
    }

    @Override
    public List<ITableUsageModel> getDeclaredTableUsages() {
        return new ArrayList<ITableUsageModel>(getModelTypeParts().getTableUsages());
    }

    @Override
    public List<ITableUsageModel> getTableUsages() {
        TableUsagesCollector tuCollector = new TableUsagesCollector();
        tuCollector.visitHierarchy(this);
        return tuCollector.result;

    }

    static class TableUsagesCollector extends TypeHierarchyVisitor {

        private List<ITableUsageModel> result = new ArrayList<ITableUsageModel>();

        @Override
        public boolean visitType(IModelType type) {
            result.addAll(((IProductModel)type).getDeclaredTableUsages());
            return true;
        }

    }

    static class TableUsageFinder extends TypeHierarchyVisitor {

        private String tableUsageName;
        private ITableUsageModel tableUsage = null;

        public TableUsageFinder(String name) {
            super();
            this.tableUsageName = name;
        }

        @Override
        public boolean visitType(IModelType type) {
            tableUsage = ((ModelType)type).getModelTypeParts().getTableUsageModel(tableUsageName);
            return tableUsage == null;
        }
    }
}
