package org.faktorips.devtools.htmlexport.helper.path;

import org.faktorips.devtools.core.model.IIpsElement;

/**
 * Util-Klasse für die Erstellung von Links und Ermittlung relativer Pfade auf Basis eines IIpsElement
 * @author dicker
 *
 */
public interface IpsElementPathUtil {

    /**
     * gibt den relativen Pfad vom <code>IIpsElement</code> ins Root-Verzeichnis
     * 
     * @return relativer Pfad ins Root-Verzeichnis
     */
    public String getPathToRoot();

    /**
     * gibt den relativen Pfad vom Root-Verzeichnis zum <code>IIpsElement</code>
     * @param linkedFileType Typ des Pfads
     * 
     * @return relativer Pfad vom Root-Verzeichnis
     */
    public String getPathFromRoot(LinkedFileType linkedFileType);

    /**
     * Name des IIPSElement in einem Link
     * 
     * @param withImage
     *            true: Darstellung mit Thumb
     * @return Html-Link-Text
     */
    public String getLinkText(boolean withImage);
    
    /**
     * 
     * @return IipsElement des PathUtils 
     */
    public IIpsElement getIpsElement();

}