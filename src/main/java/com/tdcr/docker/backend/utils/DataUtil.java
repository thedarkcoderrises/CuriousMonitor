package com.tdcr.docker.backend.utils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;

import java.util.List;

public class DataUtil {

    public static JsonArray listToJson(List<Object> list){
        JsonArray jsonArray = new Gson().toJsonTree(list).getAsJsonArray();
        return  jsonArray;
    }

    public static JsonArray objectToJson(Object object){
        JsonArray jsonArray = new Gson().toJsonTree(object).getAsJsonArray();
        return  jsonArray;
    }
}
