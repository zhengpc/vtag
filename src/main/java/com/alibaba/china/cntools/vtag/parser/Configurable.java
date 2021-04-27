package com.alibaba.china.cntools.vtag.parser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.china.cntools.vtag.base.VTagConfigException;

import org.slf4j.Logger;

public class Configurable extends Locatable {

    private String configFile;
    private List<String> warnings = new ArrayList<>();

    public List<String> getWarnings() {
        return warnings;
    }

    public void warn(int line, String msg) {
        warnings.add("line " + line + ": " + msg);
    }

    public void setConfigFile(String configFile) {
        this.configFile = configFile;
    }

    public String getConfigFile() {
        return configFile;
    }

    public static void checkWarnings(Collection<? extends Configurable> files, boolean throwWarnings, Logger logger) {
        Map<String, List<String>> warnings = new HashMap<String, List<String>>();
        for (Configurable config : files) {
            if (config.getWarnings().isEmpty()) {
                continue;
            } else {
                warnings.put(config.getConfigFile(), config.getWarnings());
            }
        }

        if (!warnings.isEmpty()) {
            if (throwWarnings) {
                throw new VTagConfigException(warnings.toString());
            } else {
                logger.warn(warnings.toString());
            }
        }
    }
}
