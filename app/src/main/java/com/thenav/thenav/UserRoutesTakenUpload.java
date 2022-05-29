package com.thenav.thenav;

public class UserRoutesTakenUpload {

    //variables declared to be set and get from this class
    //-------------------------------------
    private String upEmail;
    private String upContainer;
    private String upDestination;
    private String upDistanceKM;
    private String upDistanceMI;
    private String upTransport;
    private String upDuration;
    //-------------------------------------

    public UserRoutesTakenUpload() {}

    //constuctor to be used to set values that are sent to this class
    //-----------------------------------------------------------------------------------------------------------------------------------------------------------------------
    public UserRoutesTakenUpload(String upEmail, String upContainer, String upDestination, String upDistanceKM, String upDistanceMI, String upTransport, String upDuration) {
        this.setUpEmail(upEmail);
        this.setUpContainer(upContainer);
        this.setUpDestination(upDestination);
        this.setUpDistanceKM(upDistanceKM);
        this.setUpDistanceMI(upDistanceMI);
        this.setUpTransport(upTransport);
        this.setUpDuration(upDuration);
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------------------------------------

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

    public String getUpDestination() {
        return upDestination;
    }

    public void setUpDestination(String upDestination) {
        this.upDestination = upDestination;
    }

    public String getUpDistanceKM() {
        return upDistanceKM;
    }

    public void setUpDistanceKM(String upDistanceKM) {
        this.upDistanceKM = upDistanceKM;
    }

    public String getUpDistanceMI() {
        return upDistanceMI;
    }

    public void setUpDistanceMI(String upDistanceMI) {
        this.upDistanceMI = upDistanceMI;
    }

    public String getUpTransport() {
        return upTransport;
    }

    public void setUpTransport(String upTransport) {
        this.upTransport = upTransport;
    }

    public String getUpDuration() {
        return upDuration;
    }

    public void setUpDuration(String upDuration) {
        this.upDuration = upDuration;
    }
    //-----------------------------------------------------------------------------------------------------------
}
