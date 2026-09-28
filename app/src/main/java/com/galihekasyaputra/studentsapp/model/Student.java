package com.galihekasyaputra.studentsapp.model;

import com.google.gson.annotations.SerializedName;

public class Student {

    @SerializedName("id")
    private int id;

    @SerializedName("nis")
    private String nis;

    @SerializedName("name")
    private String name;

    @SerializedName("address")
    private String address;

    public Student(String nis, String name, String address) {
        this.nis = nis;
        this.name = name;
        this.address = address;
    }

    public int getId() {
        return id;
    }

    public String getNis() {
        return nis;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }
}