package com.barkindustries.mascotapp.ui.chatWithVets.Entities.Firebase;

public class User {

    private String profilePicURL;
    private String UsernameText;
    private String Email;
   // private int PetsAmount;
    private String RealName;
    private String Phone;
    private boolean tinderOrigin;
    private Boolean isService;
    private String serviceType;

    public User() {
    }

    public User(String profilePicURL, String UsernameText, String email, String realName, String phone) {
        this.profilePicURL = profilePicURL;
        this.UsernameText = UsernameText;
        this.Email = email;
       // PetsAmount = 0;
        if(realName != null)
            this.RealName = realName;
        if(phone != null)
            this.Phone = phone;
        isService = false;
        serviceType = null;
    }

    public User(String profilePicURL, String UsernameText, String email) {
        this.profilePicURL = profilePicURL;
        this.UsernameText = UsernameText;
        this.Email = email;
       // PetsAmount = 0;
        isService = false;
        serviceType = null;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public Boolean getService() {
        return isService;
    }

    public void setService(Boolean service) {
        isService = service;
    }

    public boolean isTinderOrigin() {
        return tinderOrigin;
    }

    public void setTinderOrigin(boolean tinderOrigin) {
        this.tinderOrigin = tinderOrigin;
    }

    public String getRealName() {
        return RealName;
    }

    public void setRealName(String realName) {
        RealName = realName;
    }

    public String getPhone() {
        return Phone;
    }

    public void setPhone(String phone) {
        Phone = phone;
    }

    // GETTERS
    public String getProfilePicURL() {
        return profilePicURL;
    }
    public String getUsernameText() {
        return UsernameText;
    }
    public String getEmail() {
        return Email;
    }
    /*public int getPetsAmount() {
        return PetsAmount;
    }
*/
    // SETTERS
    public void setProfilePicURL(String profilePicURL) {
        this.profilePicURL = profilePicURL;
    }
    public void setUsernameText(String usernameText) {
        this.UsernameText = usernameText;
    }
    public void setEmail(String email) {
        this.Email = email;
    }
    /*
    public void setPetsAmount(int petsAmount) {
        PetsAmount = petsAmount;
    }*/



}
