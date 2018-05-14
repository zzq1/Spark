package com.neu.zzq;


public class Vo {
    private String fanNo;
    private String time;
    private String description;
    private Integer s_count;

    public Vo(String fanNo, String time, String description,Integer s_count) {
        this.fanNo = fanNo;
        this.time = time;
        this.description = description;
        this.s_count = s_count;
    }

    public String getFanNo() {
        return fanNo;
    }

    public String getTime() {
        return time;
    }

    public Integer getS_count() {
        return s_count;
    }

    public String getDescription() {
        return description;
    }


}
