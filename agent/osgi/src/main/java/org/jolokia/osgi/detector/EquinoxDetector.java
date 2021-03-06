/*
 * Copyright 2009-2010 Roland Huss
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jolokia.osgi.detector;

import java.util.Set;

import javax.management.MBeanServer;

import org.jolokia.detector.ServerHandle;

/**
 * Detector for the Eclipse Equinix Platform
 *
 * @author roland
 * @since 02.12.10
 */
public class EquinoxDetector extends AbstractOsgiServerDetector {

    /** {@inheritDoc} */
    public ServerHandle detect(Set<MBeanServer> pMbeanServers) {
        if (checkSystemBundleForSymbolicName("org.eclipse.osgi")) {
            String version = getSystemBundleVersion();
            version = version.replaceFirst("\\.v\\d+$","");
            return new ServerHandle("Eclipse","equinox",version,null,null);
        } else {
            return null;
        }
    }

}
