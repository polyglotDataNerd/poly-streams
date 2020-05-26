package com.poly.poc.utils;

import com.github.wnameless.json.flattener.JsonFlattener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;


import java.text.SimpleDateFormat;
import java.util.*;


/**
 * Created by gbartolome on 5/22/16.
 */
public class Transformer {

    private final Log LOG = LogFactory.getLog(Transformer.class);
    private SimpleDateFormat dtc = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    String jsonstring;

    public Transformer(String jsonstring) {
        this.jsonstring = jsonstring;
    }

    public LinkedHashMap<String, String> transform() {
        LinkedHashMap<String, String> jsonCSVSet = new LinkedHashMap<String, String>();
        try {
            ConfigProps config = new ConfigProps();
            ArrayList<String> headers = new ArrayList<String>(Arrays.asList(config.getPropValues("arrayheadersviper").split(",")));
            HashMap<String, String> jsonCSV = new HashMap<String, String>();
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(jsonstring);
            JSONObject jobj = (JSONObject) obj;

            Map<String, Object> map = new HashMap<>(JsonFlattener.flattenAsMap(jobj.toString()));
            map.entrySet().forEach(x -> jsonCSV.put(x.getKey(), x.getValue().toString()));
            /* inserts elements that only exist in headers, if element in
            JSON is empty put the header as key and blank as the value*/
            headers.stream().forEach(al -> {
                if (jsonCSV.containsKey(al)) {
                    jsonCSVSet.put(al.toLowerCase(), "\"" + jsonCSV.get(al) + "\"");
                } else if (!jsonCSVSet.containsKey(al)) {
                    if (al.equals("dtc")) {
                        jsonCSVSet.put(al.toLowerCase(), "\"" + dtc.format(new Date()) + "\"");
                    } else jsonCSVSet.put(al.toLowerCase(), "\"\"");
                }

            });
        } catch (Exception e) {
            LOG.error(e.getMessage());
            LOG.error(e.getCause());
            Arrays.stream(e.getStackTrace()).forEach(x -> LOG.error(x));
        }
        return jsonCSVSet;
    }

}
