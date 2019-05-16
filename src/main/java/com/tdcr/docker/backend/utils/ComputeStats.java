package com.tdcr.docker.backend.utils;

import com.github.dockerjava.api.model.Statistics;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;


public class ComputeStats {

    private static DecimalFormat df2 = new DecimalFormat("#.##");
    private static String PERCENT_SYMBOL = "%";
    private static String EMPTY_STRING = "";

    public static String computeMemoryInPercent(Statistics stats) {
        long usage = ((Number)stats.getMemoryStats().get("max_usage")).longValue();
        long limit = ((Number) stats.getMemoryStats().get("limit")).longValue();
        String result = df2.format(((usage*1.0)/limit)*100);
        return result + PERCENT_SYMBOL;
    }

    public static String getMemory(Statistics stats, String param) {
        long size = ((Number) stats.getMemoryStats().get(param)).longValue();
        String hrSize = calculateSize(size,true);
        return hrSize;
    }

    public static String calculateSize(long size, boolean attachSuffix) {
        double k = size /1024;
        double m = (k)/1024;
        double g = (m)/1024;
        double t = (g)/1024;
        String suffix;
        double val;
        if (t > 1) {
            val = t; suffix ="TB";
        } else if (g > 1) {
            val = g; suffix ="GB";
        } else if (m > 1) {
            val = m; suffix ="MB";
        } else if (k >1 ){
            val = k; suffix ="KB";
        }else {
            val = size; suffix ="B";
        }
        if(attachSuffix){
          return df2.format(val).concat(suffix);
        }
        return df2.format(val);
    }

    public static String calcCPU(Statistics stats) {
        Map cpuUsageMap = ((Map)stats.getCpuStats().get("cpu_usage"));
        long totalUsage = ((Number)cpuUsageMap.get("total_usage")).longValue();
        long kernelModeUsage = ((Number)cpuUsageMap.get("usage_in_kernelmode")).longValue();
        long userModeUsage = ((Number)cpuUsageMap.get("usage_in_usermode")).longValue();
        long systemUsage = ((Number)stats.getCpuStats().get("system_cpu_usage")).longValue();
        String result = df2.format(((totalUsage+kernelModeUsage+userModeUsage)*1.0/systemUsage)*100);
        return result + PERCENT_SYMBOL;
    }

    public static String calcNetworkStats(Statistics stats) {
        int rxSize = ((Number)((Map)stats.getNetworks().get("eth0")).get("rx_bytes")).intValue();
        int txSize = ((Number)((Map)stats.getNetworks().get("eth0")).get("tx_bytes")).intValue();
        return calculateSize(rxSize,true)+"/"+calculateSize(txSize,true);
    }

    public static Map<String,String> computeStats(Statistics stats) {
        Map<String,String> statMap = new HashMap<>();
        String memPercent = computeMemoryInPercent(stats);
        String memoryUsage = getMemory(stats, "max_usage");
        String memoryLimit = getMemory(stats, "limit");
        statMap.put("MEM %",memPercent);
        statMap.put("MEM_USAGE / LIMIT",memoryUsage +" / "+memoryLimit);
//        statMap.put("CPU %", calcCPU(stats));
        statMap.put("NET I/O",calcNetworkStats(stats));
        return statMap;
    }
}
