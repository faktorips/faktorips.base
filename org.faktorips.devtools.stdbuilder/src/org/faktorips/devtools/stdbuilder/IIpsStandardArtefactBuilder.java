/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder;

/**
 * An interface that artefact builders belonging to the Faktor-IPS standard builder set (code
 * generator) implement. The Faktor-IPS' standard builder set distinguishes between published (Java)
 * packages and internal packages. All artefacts that are accessible by clients using the generated
 * code are places in the published packages, all other artefacts in the internal packages. As this
 * feature is not neccessarily needed by other code generators, we put this distinction in this
 * separate interface. The type hierarchie of IIpsArtefactBuilder is build without referencing this
 * interface.
 * 
 * @author Jan Ortmann
 */
public interface IIpsStandardArtefactBuilder {

    /**
     * Returns <code>true</code> if this builder builds artefacts that are considered as published
     * (clients of the generated code can access the artefact). Returns <code>false</code> if the
     * genereated artefact(s) are internal.
     */
    public boolean buildsPublishedArtefacts();

}
