package org.faktorips.devtools.htmlexport.generators;

/**
 * generates data of the documentation
 * @author dicker
 *
 */
public interface IGenerator {
	/**
	 * returns generated data as byte[]
	 * @return
	 */
    public byte[] generate();
    
    public final String CHARSET = "UTF-8"; //$NON-NLS-1$
}
