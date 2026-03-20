package com.catalog.models;

public class Company {
    private int id;
    private String bin;
    private String name;
    private String address;
    private String phone;
    private String email;

    public Company() {}

    public Company(String bin, String name, String address, String phone, String email) {
        this.bin = bin;
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.email = email;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getBin() { return bin; }
    public void setBin(String bin) { this.bin = bin; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}