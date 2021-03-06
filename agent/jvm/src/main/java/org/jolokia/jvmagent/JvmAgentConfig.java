/*
 * Copyright 2009-2012  Roland Huss
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jolokia.jvmagent;

import java.io.*;
import java.util.*;

import org.jolokia.util.ConfigKey;
import org.jolokia.util.EscapeUtil;

/**
 * Holds all Http-Server and Jolokia configuration.
 *
 * Default values are first loaded from the <code>default-jolokia-agent.properties</code>
 * from the class path (top-level). All default values are defined within this file.
 *
 * @author roland
 * @since 13.08.11
 */
public class JvmAgentConfig extends JolokiaServerConfig {

    // Validated properties
    private boolean isStopMode;

    /**
     * Constructor which parser an agent argument string
     *
     * @param pArgs arguments glued together as provided on the commandline
     *        for an agent parameter
     */
    public JvmAgentConfig(String pArgs) {
        Map<String,String> agentConfig = parseArgs(pArgs);

        init(agentConfig);

        // Special mode used by the client in order to indicate whether to stop/start the server.
        initMode(agentConfig);
    }

    /**
     * The mode is 'stop' indicates that the server should be stopped when used in dynamic mode
     * @return the running mode
     */
    public boolean isModeStop() {
        return isStopMode;
    }

    // ==========================================================================================================

    private void initMode(Map<String, String> agentConfig) {
        String mode = agentConfig.get("mode");
        if (mode != null && !mode.equals("start") && !mode.equals("stop")) {
            throw new IllegalArgumentException("Invalid running mode '" + mode + "'. Must be either 'start' or 'stop'");
        }
        isStopMode = "stop".equals(mode);
    }


    // ======================================================================================
    // Parse argument

    private Map<String, String> parseArgs(String pAgentArgs) {
        Map<String,String> ret = new HashMap<String, String>();
        if (pAgentArgs != null && pAgentArgs.length() > 0) {
            for (String arg : EscapeUtil.splitAsArray(pAgentArgs, EscapeUtil.CSV_ESCAPE, ",")) {
                String[] prop = EscapeUtil.splitAsArray(arg, EscapeUtil.CSV_ESCAPE, "=");
                if (prop == null || prop.length != 2) {
                    throw new IllegalArgumentException("jolokia: Invalid option '" + arg + "'. Ignoring");
                } else {
                    ret.put(prop[0],prop[1]);
                }
            }
        }
        Map<String,String> config = getDefaultConfig();
        if (ret.containsKey("config")) {
            Map<String,String> userConfig = readConfig(ret.get("config"));
            config.putAll(userConfig);
        }
        config.putAll(ret);
        prepareDetectorOptions(config);
        return config;
    }

    // Add detector specific options if given on the command line
    private void prepareDetectorOptions(Map<String, String> pConfig) {
        StringBuffer detectorOpts = new StringBuffer("{");
        if (pConfig.containsKey("bootAmx") && Boolean.parseBoolean(pConfig.get("bootAmx"))) {
            detectorOpts.append("\"glassfish\" : { \"bootAmx\" : true }");
        }
        if (detectorOpts.length() > 1) {
            detectorOpts.append("}");
            pConfig.put(ConfigKey.DETECTOR_OPTIONS.getKeyValue(),detectorOpts.toString());
        }
    }

    private Map<String, String> readConfig(String pFilename) {
        File file = new File(pFilename);
        try {
            InputStream is = new FileInputStream(file);
            return readPropertiesFromInputStream(is,pFilename);
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException("jolokia: Can not find configuration file " + pFilename,e);
        }
    }


}
