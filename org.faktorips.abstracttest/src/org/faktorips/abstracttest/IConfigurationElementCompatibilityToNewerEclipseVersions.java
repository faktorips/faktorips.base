package org.faktorips.abstracttest;

import org.eclipse.core.runtime.IConfigurationElement;

/**
 * This is a hack because newer eclipse versions add {@link #getHandleId()} to the API of
 * {@link IConfigurationElement} and we want our code to be compatible with old and new target
 * platforms.
 */
public interface IConfigurationElementCompatibilityToNewerEclipseVersions {
    int getHandleId();
}