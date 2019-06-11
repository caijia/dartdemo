package com.jk.dartdemo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;

@Controller
public class DemoController {

    @GetMapping("/toDart")
    public String jsonToDart(Model model) {
        return "json_to_dart";
    }

    @PostMapping("/jsonToDart")
    public String toDartBean(@RequestParam(value = "json", defaultValue = "") String json, RedirectAttributes redirectAttributes) {
        String msg;
        String zipJson;
        if (StringUtils.isEmpty(json)) {
            msg = "json为空";
        } else if (!JSON.isValid(zipJson = json.replaceAll("[\\r\\n\\s]", ""))) {
            msg = "错误的json";
        } else {
            msg = jsonToBean(zipJson);
        }
        redirectAttributes.addFlashAttribute("dart", msg);
        redirectAttributes.addFlashAttribute("json", json);
        return "redirect:/toDart";
    }

    private String jsonToBean(String json) {
        StringBuilder sb = new StringBuilder();
        boolean isArray = JSON.isValidArray(json);
        boolean isObject = JSON.isValidObject(json);
        if (isArray) {
            appendJsonArray(sb, JSON.parseArray(json), null);

        } else if (isObject) {
            appendJsonObject(sb, JSON.parseObject(json), null);
        }
        return sb.toString();
    }

    private void appendJsonArray(StringBuilder sb, JSONArray jsonArray, String key) {
        if (jsonArray != null) {
            Object o = jsonArray.size() > 0 ? jsonArray.get(0) : new JSONObject();
            if (o instanceof JSONObject) {
                JSONObject jo = (JSONObject) o;
                appendJsonObject(sb, jo, key);
            } else if (o instanceof JSONArray) {
                appendJsonArray(sb, (JSONArray) o, key);
            }
        }
    }

    private void appendJsonObject(StringBuilder sb, JSONObject jo, String joKey) {
        String className = firstCharToUpperCase(joKey, "DataBean");
        String classInstance = firstCharToLowerCase(className);

        StringBuilder fields = new StringBuilder();
        StringBuilder constructor = new StringBuilder();
        StringBuilder fromJsonSb = new StringBuilder();
        StringBuilder toJsonSb = new StringBuilder();
        Map<String, JSONObject> filedJsonObjMap = new HashMap<>();
        Map<String, JSONArray> filedJsonArrayMap = new HashMap<>();
        for (Map.Entry<String, Object> entry : jo.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (constructor.length() > 0) {
                constructor.append(",");
            }

            if (fromJsonSb.length() > 0) {
                fromJsonSb.append(",");
            }

            if (toJsonSb.length() > 0) {
                toJsonSb.append(",");
            }

            if (value instanceof JSONArray) {
                String innerClassName = className + firstCharToUpperCase(key, "DataBean");
                filedJsonArrayMap.put(innerClassName, (JSONArray) value);
                fields.append(String.format("&nbsp;&nbsp;final List&lt;%s&gt; %s;<br>", innerClassName, key));
                fromJsonSb.append(String.format("%s: (map['%s'] as List).map((e) => %s().fromJson(e as Map&lt;String, dynamic&gt;)).toList()", key, key, innerClassName));

            } else if (value instanceof JSONObject) {
                String innerClassName = className + firstCharToUpperCase(key, "DataBean");
                filedJsonObjMap.put(innerClassName, (JSONObject) value);
                fields.append(String.format("&nbsp;&nbsp;final %s %s;<br>", innerClassName, key));
                fromJsonSb.append(String.format("%s: %s().fromJson((map['%s'] as Map&lt;String, dynamic&gt;))", key, innerClassName, key));

            } else if (value instanceof Number) {
                fields.append(String.format("&nbsp;&nbsp;final num %s;<br>", key));
                fromJsonSb.append(String.format("%s: map['%s'] as num", key, key));

            } else if (value instanceof Boolean) {
                fields.append(String.format("&nbsp;&nbsp;final bool %s;<br>", key));
                fromJsonSb.append(String.format("%s: map['%s'] as bool", key, key));
            } else {
                fields.append(String.format("&nbsp;&nbsp;final String %s;<br>", key));
                fromJsonSb.append(String.format("%s: map['%s'] as String", key, key));
            }
            toJsonSb.append(String.format("'%s': %s.%s", key, classInstance, key));
            constructor.append(String.format("this.%s", key));
        }

        sb.append(String.format("class %s {<br>", className));
        sb.append(fields.toString());
        if (constructor.length() > 0) {
            sb.append(String.format("&nbsp;&nbsp;%s({%s});<br>", className, constructor.toString()));
        } else {
            sb.append(String.format("&nbsp;&nbsp;%s();<br>", className));
        }
        sb.append(String.format("&nbsp;&nbsp;%s fromJson(Map&lt;String, dynamic&gt; map) {<br>&nbsp;&nbsp;&nbsp;&nbsp;return %s(%s);<br>&nbsp;&nbsp;}<br>", className, className, fromJsonSb.toString()));
        sb.append(String.format("&nbsp;&nbsp;Map&lt;String, dynamic&gt; toJson(%s %s) =><br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;String, dynamic&gt;{%s};<br>", className, classInstance, toJsonSb.toString()));
        sb.append("}<br>");

        if (!filedJsonObjMap.isEmpty()) {
            for (Map.Entry<String, JSONObject> entry : filedJsonObjMap.entrySet()) {
                appendJsonObject(sb, entry.getValue(), entry.getKey());
            }
        }

        if (!filedJsonArrayMap.isEmpty()) {
            for (Map.Entry<String, JSONArray> entry : filedJsonArrayMap.entrySet()) {
                appendJsonArray(sb, entry.getValue(), entry.getKey());
            }
        }
    }

    private String firstCharToLowerCase(String str) {
        if (StringUtils.isEmpty(str)) {
            return "";
        }
        return str.substring(0, 1).toLowerCase() + (str.length() > 1 ? str.substring(1) : "");
    }

    private String firstCharToUpperCase(String str, String defaultValue) {
        if (StringUtils.isEmpty(str)) {
            return defaultValue;
        }
        return str.substring(0, 1).toUpperCase() + (str.length() > 1 ? str.substring(1) : "");
    }
}
