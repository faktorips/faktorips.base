/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.faktorips.devtools.model.builder.base.BaseTocFileBuilder;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.runtime.internal.toc.ITocEntryFactory;
import org.faktorips.runtime.internal.toc.TocEntryObject;

/**
 *
 * @author Jan Ortmann
 */
public class TocFileBuilder extends BaseTocFileBuilder {

    private Map<IpsObjectType, List<ITocEntryBuilder>> ipsObjectTypeToTocEntryBuilderMap;

    public TocFileBuilder(StandardBuilderSet builderSet) {
        super(builderSet);
        initExtensionBuilders();
    }

    protected void initExtensionBuilders() {
        List<ITocEntryBuilderFactory> tocEntryBuilderFactories = StdBuilderPlugin.getDefault()
                .getTocEntryBuilderFactories();
        ipsObjectTypeToTocEntryBuilderMap = new HashMap<>();
        for (ITocEntryBuilderFactory tocEntryBuilderFactory : tocEntryBuilderFactories) {
            ITocEntryBuilder builder = tocEntryBuilderFactory.createTocEntryBuilder(this);
            IpsObjectType ipsObjectType = builder.getIpsObjectType();
            List<ITocEntryBuilder> builderList = ipsObjectTypeToTocEntryBuilderMap.computeIfAbsent(ipsObjectType,
                    $ -> new ArrayList<>());
            builderList.add(builder);
        }
    }

    @Override
    public StandardBuilderSet getBuilderSet() {
        return (StandardBuilderSet)super.getBuilderSet();
    }

    @Override
    public boolean isBuilderFor(IIpsSrcFile ipsSrcFile) {
        IpsObjectType type = ipsSrcFile.getIpsObjectType();
        return super.isBuilderFor(ipsSrcFile) || ipsObjectTypeToTocEntryBuilderMap.containsKey(type);
    }

    @Override
    protected Set<ITocEntryFactory<?>> getTocEntryFactories() {
        Set<ITocEntryFactory<?>> tocEntryFactories = new LinkedHashSet<>(super.getTocEntryFactories());
        tocEntryFactories.addAll(StdBuilderPlugin.getDefault().getTocEntryFactories());
        return tocEntryFactories;
    }

    @Override
    protected List<TocEntryObject> buildTocEntries(IIpsObject object) {
        List<TocEntryObject> entries = new ArrayList<>();
        IpsObjectType type = object.getIpsObjectType();
        if (ipsObjectTypeToTocEntryBuilderMap.containsKey(type)) {
            List<ITocEntryBuilder> builderList = ipsObjectTypeToTocEntryBuilderMap.get(type);
            for (ITocEntryBuilder builder : builderList) {
                entries.addAll(builder.createTocEntries(object));
            }
        } else {
            entries.addAll(super.buildTocEntries(object));
        }
        return entries;
    }

    @Override
    protected String getImplementationClass(IProductCmpt productCmpt, IProductCmptType pcType) {
        return getBuilderSet().getProductCmptBuilder().getImplementationClass(productCmpt);
    }

    @Override
    protected String getImplementationClass(IProductCmptGeneration productCmptGeneration) {
        return getBuilderSet().getProductCmptBuilder().getImplementationClass(productCmptGeneration);
    }
}
