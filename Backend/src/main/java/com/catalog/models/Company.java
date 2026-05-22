package com.catalog.models;

import jakarta.persistence.*;

@Entity
@Table(name = "companies")
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false, unique = true, length = 12)
    private String bin;

    @Column(nullable = false)
    private String name;

    private String address;
    private String phone;
    private String email;

    @Column(name = "director_name")
    private String directorName;

    @Column(name = "director_position")
    private String directorPosition;

    @Column(name = "iik", length = 34)
    private String iik;

    @Column(name = "bank_name")
    private String bankName;

    @Column(name = "bik", length = 20)
    private String bik;

    @Column(name = "kbe", length = 10)
    private String kbe;

    @Column(name = "legal_address", length = 500)
    private String legalAddress;

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

    public String getDirectorName() { return directorName; }
    public void setDirectorName(String directorName) { this.directorName = directorName; }

    public String getDirectorPosition() { return directorPosition; }
    public void setDirectorPosition(String directorPosition) { this.directorPosition = directorPosition; }

    public String getIik() { return iik; }
    public void setIik(String iik) { this.iik = iik; }

    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }

    public String getBik() { return bik; }
    public void setBik(String bik) { this.bik = bik; }

    public String getKbe() { return kbe; }
    public void setKbe(String kbe) { this.kbe = kbe; }

    public String getLegalAddress() { return legalAddress; }
    public void setLegalAddress(String legalAddress) { this.legalAddress = legalAddress; }

}