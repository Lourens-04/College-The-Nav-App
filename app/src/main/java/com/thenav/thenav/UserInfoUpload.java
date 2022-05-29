package com.thenav.thenav;

public class UserInfoUpload {

    //variables declared to be set and get from this class of a user details
    //-------------------------------------
    private String upEmail;
    private String upContainer;
    private String upFirstName;
    private String upLastName;
    private String upMetOrImp;
    private String upTransport;
    //-------------------------------------

    public UserInfoUpload(){}

    //constuctor to be used to set values that are sent to this class
    //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    public UserInfoUpload(String upEmail, String upContainer, String upFirstName, String upLastName, String upMetOrImp, String upTransport) {

        if (upFirstName.trim().equals("")){
            upFirstName = "No Name";
        }
        if (upLastName.trim().equals("")){
            upLastName = "No Last Name";
        }
        if (upMetOrImp == null){
            upMetOrImp = "Metric System (Kilometers - Km)";
        }
        if (upTransport.trim().equals("--- Select Default Transport ---")){
            upTransport = "Car";
        }

        this.upEmail = upEmail;
        this.upContainer = upContainer;
        this.upFirstName = upFirstName;
        this.upLastName = upLastName;
        this.upMetOrImp = upMetOrImp;
        this.upTransport = upTransport;
    }
    //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------


    //getters and setters for this class
    //-----------------------------------------------------------------------------------------------------------
    public String getUpEmail() {
        return upEmail;
    }

    public void setUpEmail(String upEmail) {
        this.upEmail = upEmail;
    }

    public String getUpContainer() {
        return upContainer;
    }

    public void setUpContainer(String upContainer) {
        this.upContainer = upContainer;
    }

    public String getUpFirstname() {
        return upFirstName;
    }

    public void setUpFirstname(String upFirstName) {
        this.upFirstName = upFirstName;
    }

    public String getUpLastname() {
        return upLastName;
    }

    public void setUpLastname(String upLastName) {
        this.upLastName = upLastName;
    }

    public String getUpMetOrImp() {
        return upMetOrImp;
    }

    public void setUpMetOrImp(String upMetOrImp) {
        this.upMetOrImp = upMetOrImp;
    }

    public String getUpTransport() {
        return upTransport;
    }

    public void setUpTransport(String upTransport) {
        this.upTransport = upTransport;
    }
    //-----------------------------------------------------------------------------------------------------------



















}
