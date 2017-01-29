package com.example.kirill.lab2.model;

import com.google.firebase.database.Exclude;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Kirill on 18.01.2017.
 */

public class Meeting {
    public String name;
    public String description;
    public Date startDate;
    public Date endDate;
    public String priority;
    public Map<String, Boolean> pres = new HashMap<>();

    public Meeting(){}


    public Meeting(String name, String description, Date startDate, Date endDate,  String priority) {
        this.name = name;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.priority = priority;
    }



    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }


    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Date getStartDate() {
        return startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public String getPriority() {
        return priority;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("name", name);
        result.put("description", description);
        result.put("startDate",startDate);
        result.put("endDate",endDate);
        result.put("priority", priority);
        result.put("pres", pres);
        return result;
    }

    @Exclude
    public Map<String, Object> toMapWithoutList() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("name", name);
        result.put("description", description);
        result.put("startDate",startDate);
        result.put("endDate",endDate);
        result.put("priority", priority);
        return result;
    }


}
