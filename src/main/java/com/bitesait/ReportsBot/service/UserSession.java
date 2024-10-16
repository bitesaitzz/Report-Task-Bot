package com.bitesait.ReportsBot.service;

import com.bitesait.ReportsBot.model.Report;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class UserSession {
    private Map<Long, String> userStates = new HashMap<>();
    private Map<Long, Report> userReports = new HashMap<>();



    public Map<Long, String> getUserStates() {
        return userStates;
    }

    public Map<Long, Report> getUserReports() {
        return userReports;
    }


}