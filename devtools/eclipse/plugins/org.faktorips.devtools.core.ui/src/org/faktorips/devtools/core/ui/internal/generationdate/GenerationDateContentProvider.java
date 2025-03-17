/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.internal.generationdate;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.internal.DeferredStructuredContentProvider;
import org.faktorips.devtools.core.ui.views.productstructureexplorer.Messages;
import org.faktorips.devtools.model.ipsobject.IIpsObjectGeneration;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAssociation;

/**
 * Content provider to collect all generation dates of a product component structure. This contains
 * the generation dates of a given product component and also the dates of every component connected
 * by a composition.
 * 
 * @author dirmeier
 */
public class GenerationDateContentProvider extends DeferredStructuredContentProvider {

    @Override
    public Object[] collectElements(Object inputElement, IProgressMonitor monitor) {
        if (inputElement instanceof IProductCmpt productCmpt) {
            try {
                List<GenerationDate> result = collectGenerationDates(productCmpt, monitor);
                return result.toArray();
            } catch (IpsException e) {
                IpsPlugin.log(e);
            }
        }
        return new Object[0];
    }

    public List<GenerationDate> collectGenerationDates(IProductCmpt productCmpt, IProgressMonitor monitor) {
        IProgressMonitor theMonitor = monitor;
        if (theMonitor == null) {
            theMonitor = new NullProgressMonitor();
        }
        TreeSet<GregorianCalendar> validFromDates = collectValidFromDates(productCmpt,
                new HashSet<>(), productCmpt.getIpsProject(), theMonitor);
        List<GenerationDate> result = new ArrayList<>();
        GregorianCalendar lastDate = null;
        GenerationDate lastAdjDate = null;
        for (GregorianCalendar nextDate : validFromDates) {
            lastAdjDate = new GenerationDate(nextDate, lastDate);
            lastDate = (GregorianCalendar)nextDate.clone();
            // valitTo Dates are always one millisecond before next valid from
            lastDate.add(Calendar.MILLISECOND, -1);
            result.add(lastAdjDate);
        }
        return result;
    }

    @Override
    protected String getWaitingLabel() {
        return NLS.bind(Messages.ProductStructureExplorer_collectingAdjustmentDates, IpsPlugin.getDefault()
                .getIpsPreferences().getChangesOverTimeNamingConvention().getGenerationConceptNamePlural());
    }

    private TreeSet<GregorianCalendar> collectValidFromDates(IProductCmpt productCmpt,
            Set<IProductCmptGeneration> alreadyPassed,
            IIpsProject ipsProject,
            IProgressMonitor monitor) {

        TreeSet<GregorianCalendar> result = new TreeSet<>(Comparator.comparing(GregorianCalendar::getTime).reversed());
        if (productCmpt == null) {
            return result;
        }

        List<IProductCmptGeneration> generations = productCmpt.getProductCmptGenerations();
        SubMonitor subMonitor = SubMonitor.convert(monitor, productCmpt.getName(), generations.size());
        for (IProductCmptGeneration generation : generations) {
            result.addAll(collectValidFromDates(generation, generation.getValidFrom(), alreadyPassed, ipsProject,
                    subMonitor));
        }
        return result;

    }

    private Set<GregorianCalendar> collectValidFromDates(IProductCmptGeneration generation,
            GregorianCalendar smallestValidFrom,
            Set<IProductCmptGeneration> alreadyPassed,
            IIpsProject ipsProject,
            SubMonitor monitor) {
        GregorianCalendar theSmallestValidFrom = smallestValidFrom;
        Set<GregorianCalendar> result = new HashSet<>();
        if (monitor.isCanceled()) {
            return result;
        }
        if (!generation.getValidFrom().before(theSmallestValidFrom)) {
            result.add(generation.getValidFrom());
            theSmallestValidFrom = generation.getValidFrom();
        }
        List<IProductCmptLink> links = generation.getLinksIncludingProductCmpt();
        SubMonitor subMonitor = monitor.split(1);

        subMonitor.beginTask(null, links.size());
        for (IProductCmptLink link : links) {
            if (monitor.isCanceled()) {
                return result;
            }
            IProductCmptTypeAssociation linkAssociation = link.findAssociation(ipsProject);
            if (linkAssociation != null && !linkAssociation.isAssoziation()) {
                IProductCmpt target = link.findTarget(ipsProject);
                if (target != null) {
                    SubMonitor recMonitor = subMonitor.split(1);
                    List<IProductCmptGeneration> relevantGenerations = getRelevantGenerations(target, generation);
                    for (IProductCmptGeneration aGeneration : relevantGenerations) {
                        if (alreadyPassed.add(aGeneration)) {
                            result.addAll(collectValidFromDates(aGeneration, theSmallestValidFrom, alreadyPassed,
                                    ipsProject, recMonitor));
                        }
                    }
                }
            }
        }
        return result;
    }

    private List<IProductCmptGeneration> getRelevantGenerations(IProductCmpt target, IIpsObjectGeneration reference) {
        List<IProductCmptGeneration> result = new ArrayList<>();
        for (IIpsObjectGeneration aGeneration : target.getGenerations()) {
            if (aGeneration instanceof IProductCmptGeneration prodGeneration) {
                /*
                 * all generations with prodGeneration.validFrom have to be before or equal
                 * reference.validTo and prodGeneration.validTo have to be after or equal
                 * reference.validFrom. validTo == null is equal infinite
                 */
                if (!prodGeneration.getValidFrom().after(reference.getValidTo())
                        && !reference.getValidFrom().after(prodGeneration.getValidTo())) {
                    result.add(prodGeneration);
                }
            }
        }
        return result;
    }

    @Override
    public void dispose() {
        // nothing to dispose
    }
}
