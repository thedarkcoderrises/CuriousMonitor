package com.tdcr.docker.backend.utils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;

import java.time.LocalDate;
import java.time.format.TextStyle;
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

    public static Boolean caseInsensitiveContains(String where, String what) {
        return where.toLowerCase().contains(what.toLowerCase());
    }

    public static String getFullMonthName(LocalDate date) {
        return date.getMonth().getDisplayName(TextStyle.FULL, AppConst.APP_LOCALE);
    }
}
