package com.android.sheguard.model;

import java.util.Arrays;

@SuppressWarnings("unused")
public class HelplineModel {

    String name, number, details;

    public HelplineModel(String name, String details, String... number) {
        this.name = name;
        this.number = String.join("\n", Arrays.asList(number));
        this.details = details;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String... number) {
        this.number = String.join("\n", Arrays.asList(number));
    }
}
