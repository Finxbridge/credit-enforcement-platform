package com.finx.communication.util;

import java.util.Map;

public class TemplateVariableReplacer {

    public static String replace(String template, Map<String, String> variables) {
        String result = template;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            result = result.replace("{{ " + entry.getKey() + " }}", entry.getValue());
        }
        return result;
    }
}