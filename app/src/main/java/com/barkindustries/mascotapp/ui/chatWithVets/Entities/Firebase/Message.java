package com.barkindustries.mascotapp.ui.chatWithVets.Entities.Firebase;

import com.google.firebase.database.ServerValue;

public class Message {

    private String message;
    private String urlPic;
    private boolean containsPhoto;
    private String keyEmisor;
    private Object createdTimestamp;
   // private boolean leido;

    public Message() {
        createdTimestamp = ServerValue.TIMESTAMP;
    }

    // GETTERS
    public String getMessage() {
        return message;
    }
    public String getUrlPic() {
        return urlPic;
    }
    public boolean isContainsPhoto() {
        return containsPhoto;
    }
    public String getKeyEmisor() {
        return keyEmisor;
    }
    public Object getCreatedTimestamp() {
        return createdTimestamp;
    }

    // SETTERS
    public void setMessage(String message) {
        this.message = message;
    }
    public void setUrlPic(String urlPic) {
        this.urlPic = urlPic;
    }
    public void setContainsPhoto(boolean containsPhoto) {
        this.containsPhoto = containsPhoto;
    }
    public void setKeyEmisor(String keyEmisor) {
        this.keyEmisor = keyEmisor;
    }



   /* public boolean isLeido() {
        return leido;
    }

    public void setLeido(boolean leido) {
        this.leido = leido;
    }*/
}
