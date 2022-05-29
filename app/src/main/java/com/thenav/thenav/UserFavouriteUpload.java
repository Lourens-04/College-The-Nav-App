package com.thenav.thenav;

public class UserFavouriteUpload {

    //variables declared to be set and get from this class
    //-------------------------------------
    private String upEmail;
    private String upContainer;
    private String upFavourite;
    private String upLongLati;
    //-------------------------------------

    public UserFavouriteUpload() {}

    //constuctor to be used to set values that are sent to this class
    //----------------------------------------------------------------------------------------------------------
    public UserFavouriteUpload(String upEmail, String upContainer, String upFavourite, String upLongLati) {
        this.upEmail = upEmail;
        this.upContainer = upContainer;
        this.upFavourite = upFavourite;
        this.upLongLati = upLongLati;
    }
    //-----------------------------------------------------------------------------------------------------------

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

    public String getUpFavourite() {
        return upFavourite;
    }

    public void setUpFavourite(String upFavourite) {
        this.upFavourite = upFavourite;
    }

    public String getUpLongLati() {
        return upLongLati;
    }

    public void setUpLongLati(String upLongLati) {
        this.upLongLati = upLongLati;
    }
    //-----------------------------------------------------------------------------------------------------------
}
