/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.views.productstructureexplorer;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectGeneration;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.ui.internal.DeferredStructuredContentProvider;
import org.faktorips.devtools.core.ui.internal.adjustmentdate.AdjustmentDate;

public class AdjustmentDateContentProvider extends DeferredStructuredContentProvider {

    @Override
    protected Object[] collectElements(Object inputElement, IProgressMonitor monitor) {
        if (inputElement instanceof IProductCmpt) {
            IProductCmpt productCmpt = (IProductCmpt)inputElement;
            try {
                TreeSet<GregorianCalendar> validFromDates = collectValidFromDates(productCmpt,
                        new HashSet<IProductCmptGeneration>(), productCmpt.getIpsProject(), monitor);
                List<AdjustmentDate> result = new ArrayList<AdjustmentDate>();
                GregorianCalendar lastDate = null;
                AdjustmentDate lastAdjDate = null;
                for (Iterator<GregorianCalendar> validFromIterator = validFromDates.iterator(); validFromIterator
                        .hasNext();) {
                    GregorianCalendar nextDate = validFromIterator.next();
                    lastAdjDate = new AdjustmentDate(nextDate, lastDate);
                    lastDate = (GregorianCalendar)nextDate.clone();
                    // valitTo Dates are always one millisecond before next valid from
                    lastDate.add(Calendar.MILLISECOND, -1);
                    result.add(lastAdjDate);
                }
                return result.toArray();
            } catch (CoreException e) {
                IpsPlugin.log(e);
            }
        }
        return new Object[0];
    }

    @Override
    protected String getWaitingLabel() {
        return NLS.bind(Messages.ProductStructureExplorer_collectingAdjustmentDates, IpsPlugin.getDefault()
                .getIpsPreferences().getChangesOverTimeNamingConvention().getGenerationConceptNamePlural());
    }

    private TreeSet<GregorianCalendar> collectValidFromDates(IProductCmpt productCmpt,
            Set<IProductCmptGeneration> alreadyPassed,
            IIpsProject ipsProject,
            IProgressMonitor monitor) throws CoreException {
        TreeSet<GregorianCalendar> result = new TreeSet<GregorianCalendar>(new Comparator<GregorianCalendar>() {

            public int compare(GregorianCalendar o1, GregorianCalendar o2) {
                // descending order
                return o2.getTime().compareTo(o1.getTime());
            }

        });
        if (productCmpt == null) {
            return result;
        }

        List<IIpsObjectGeneration> generations = productCmpt.getGenerations();
        try {
            monitor.beginTask(productCmpt.getName(), generations.size());
            for (IIpsObjectGeneration generation : generations) {
                result.addAll(collectValidFromDates(generation, generation.getValidFrom(), alreadyPassed, ipsProject,
                        monitor));
            }
        } finally {
            monitor.done();
        }
        return result;
    }

    private Set<GregorianCalendar> collectValidFromDates(IIpsObjectGeneration generation,
            GregorianCalendar smallestValidFrom,
            Set<IProductCmptGeneration> alreadyPassed,
            IIpsProject ipsProject,
            IProgressMonitor monitor) throws CoreException {
        Set<GregorianCalendar> result = new HashSet<GregorianCalendar>();

        if (monitor.isCanceled()) {
            return result;
        }
        if (!generation.getValidFrom().before(smallestValidFrom)) {
            result.add(generation.getValidFrom());
            smallestValidFrom = generation.getValidFrom();
        }
        if (generation instanceof IProductCmptGeneration) {
            IProductCmptGeneration prodCmptGeneration = (IProductCmptGeneration)generation;
            IProductCmptLink[] links = prodCmptGeneration.getLinks();
            IProgressMonitor subMonitor = new SubProgressMonitor(monitor, 1);
            try {
                subMonitor.beginTask(null, links.length);
                for (IProductCmptLink link : links) {
                    if (monitor.isCanceled()) {
                        return result;
                    }
                    IProductCmptTypeAssociation linkAssociation = link.findAssociation(ipsProject);
                    if (linkAssociation != null && !linkAssociation.isAssoziation()) {
                        IProductCmpt target = link.findTarget(ipsProject);
                        if (target != null) {
                            IProgressMonitor recMonitor = new SubProgressMonitor(subMonitor, 1);
                            List<IProductCmptGeneration> relevantGenerations = getRelevantGenerations(target,
                                    generation);
                            for (IProductCmptGeneration aGeneration : relevantGenerations) {
                                if (alreadyPassed.add(aGeneration)) {
                                    result.addAll(collectValidFromDates(aGeneration, smallestValidFrom, alreadyPassed,
                                            ipsProject, recMonitor));
                                }
                            }
                        }
                    }
                }
            } finally {
                subMonitor.done();
            }
        }
        return result;
    }

    private List<IProductCmptGeneration> getRelevantGenerations(IProductCmpt target, IIpsObjectGeneration reference) {
        List<IProductCmptGeneration> result = new ArrayList<IProductCmptGeneration>();
        for (IIpsObjectGeneration aGeneration : target.getGenerations()) {
            if (aGeneration instanceof IProductCmptGeneration) {
                IProductCmptGeneration prodGeneration = (IProductCmptGeneration)aGeneration;
                // all generations with prodGeneration.validFrom must before or equal
                // reference.validTo and
                // prodGeneration.validTo after or equal reference.validFrom
                // validTo == null should be infinite
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

    }
}
