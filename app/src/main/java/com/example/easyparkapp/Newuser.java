package com.example.easyparkapp;

public class Newuser {
    private String Name;
    private String Username;
    private String Email;
    private String Number;
    private String Id;

    public Newuser(String name, String username, String email, String number, String id) {
        Name = name;
        Username = username;
        Email = email;
        Number = number;
        Id=id;
    }

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    public String getName() {

        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getUsername() {

        return Username;
    }

    public void setUserame(String username) {
        Username = username;
    }

    public String getEmail() {

        return Email;
    }

    public void setEmail(String email) {

        Email = email;
    }

    public String getNumber() {

        return Number;
    }

    public void setNumber(String number) {
        Number = number;
    }


}


