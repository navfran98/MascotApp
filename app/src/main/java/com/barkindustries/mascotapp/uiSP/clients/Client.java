package com.barkindustries.mascotapp.uiSP.clients;

public class Client {

    private String usernameText;
    private String realName;
    private String phone;
    private String profilePicURL;

    public String getUsernameText() {
        return usernameText;
    }

    public void setUsernameText(String usernameText) {
        this.usernameText = usernameText;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getProfilePicURL() {
        return profilePicURL;
    }

    public void setProfilePicURL(String profilePicURL) {
        this.profilePicURL = profilePicURL;
    }
}
