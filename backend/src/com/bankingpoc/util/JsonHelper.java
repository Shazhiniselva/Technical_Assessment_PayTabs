package com.bankingpoc.util;

import com.bankingpoc.exception.BadRequestException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class JsonHelper {
    private JsonHelper() {
    }

    public static Map<String, String> parseObject(String json) {
        Map<String, String> valuesByFieldName = new LinkedHashMap<>();
        String trimmedJson = json == null ? "" : json.trim();
        if (!trimmedJson.startsWith("{") || !trimmedJson.endsWith("}")) {
            throw new BadRequestException("Request body must be a JSON object");
        }

        String objectContent = trimmedJson.substring(1, trimmedJson.length() - 1).trim();
        if (objectContent.isEmpty()) {
            return valuesByFieldName;
        }

        for (String field : splitFields(objectContent)) {
            int separatorIndex = field.indexOf(':');
            if (separatorIndex < 1) {
                throw new BadRequestException("Invalid JSON field");
            }
            String fieldName = unquote(field.substring(0, separatorIndex).trim());
            String fieldValue = unquote(field.substring(separatorIndex + 1).trim());
            valuesByFieldName.put(fieldName, fieldValue);
        }
        return valuesByFieldName;
    }

    public static String object(Map<String, ?> fields) {
        StringBuilder json = new StringBuilder("{");
        boolean firstField = true;
        for (Map.Entry<String, ?> field : fields.entrySet()) {
            if (!firstField) {
                json.append(',');
            }
            firstField = false;
            json.append(quote(field.getKey())).append(':').append(value(field.getValue()));
        }
        return json.append('}').toString();
    }

    public static String array(List<String> items) {
        return "[" + String.join(",", items) + "]";
    }

    private static List<String> splitFields(String objectContent) {
        List<String> fields = new ArrayList<>();
        boolean insideString = false;
        int fieldStart = 0;
        for (int index = 0; index < objectContent.length(); index++) {
            char currentCharacter = objectContent.charAt(index);
            if (currentCharacter == '"' && (index == 0 || objectContent.charAt(index - 1) != '\\')) {
                insideString = !insideString;
            } else if (currentCharacter == ',' && !insideString) {
                fields.add(objectContent.substring(fieldStart, index));
                fieldStart = index + 1;
            }
        }
        fields.add(objectContent.substring(fieldStart));
        return fields;
    }

    private static String value(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof Boolean || value instanceof Number) {
            return value.toString();
        }
        return quote(value.toString());
    }

    private static String quote(String value) {
        return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }

    private static String unquote(String value) {
        String trimmedValue = value.trim();
        if (trimmedValue.startsWith("\"") && trimmedValue.endsWith("\"")) {
            return trimmedValue.substring(1, trimmedValue.length() - 1).replace("\\\"", "\"").replace("\\\\", "\\");
        }
        if ("null".equals(trimmedValue)) {
            return null;
        }
        return trimmedValue;
    }
}
