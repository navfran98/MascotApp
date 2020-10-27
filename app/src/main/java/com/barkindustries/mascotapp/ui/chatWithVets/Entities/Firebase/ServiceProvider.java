package com.barkindustries.mascotapp.ui.chatWithVets.Entities.Firebase;

public class ServiceProvider extends User {

    private Long Stars;
    private Double StarsSvg;

    public ServiceProvider(String profilePicURL, String UsernameText, String email) {
        super(profilePicURL, UsernameText, email);
    }

    public ServiceProvider() {
    }

    public Long getStars() {
        return Stars;
    }

    public void setStars(Long stars) {
        Stars = stars;
    }

    public Double getStarsSvg() {
        return StarsSvg;
    }

    public void setStarsSvg(Double starsSvg) {
        StarsSvg = starsSvg;
    }
}
