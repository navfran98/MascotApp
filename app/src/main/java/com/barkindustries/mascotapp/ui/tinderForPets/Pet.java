package com.barkindustries.mascotapp.ui.tinderForPets;

public class Pet {
    private String Age;
    private String Birthday;
    private String Breed;
    private  String Gender;
    private  String Name;
    private  String Owner;
    private String PhotoURL;
    private String Weight;
    private String key;
    private String IsLost;
    private Boolean possibleMatch;

    public Pet(String age, String birthday, String breed, String gender, String IsLost , String name, String owner, String photoURL, String weight) {
        Age = age;
        Birthday = birthday;
        Breed = breed;
        Gender = gender;
        Name = name;
        Owner = owner;
        PhotoURL = photoURL;
        Weight = weight;
        key = "";
        possibleMatch = false;
        this.IsLost = IsLost;
    }

    public Pet() {
    }


    public String getIsLost() {
        return IsLost;
    }

    public void setIsLost(String isLost) {
        IsLost = isLost;
    }

    public Boolean getPossibleMatch() {
        return possibleMatch;
    }

    public void setPossibleMatch(Boolean possibleMatch) {
        this.possibleMatch = possibleMatch;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getBirthday() {
        return Birthday;
    }

    public void setBirthday(String birthday) {
        Birthday = birthday;
    }

    public String getBreed() {
        return Breed;
    }

    public void setBreed(String breed) {
        Breed = breed;
    }

    public String getGender() {
        return Gender;
    }

    public void setGender(String gender) {
        Gender = gender;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getOwner() {
        return Owner;
    }

    public void setOwner(String owner) {
        Owner = owner;
    }

    public String getPhotoURL() {
        return PhotoURL;
    }

    public void setPhotoURL(String photoURL) {
        PhotoURL = photoURL;
    }

    public String getAge() {
        return Age;
    }

    public void setAge(String age) {
        Age = age;
    }

    public String getWeight() {
        return Weight;
    }

    public void setWeight(String weight) {
        Weight = weight;
    }


    @Override
    public String toString() {
        return "Pet{" +
                "Age='" + Age + '\'' +
                ", Birthday='" + Birthday + '\'' +
                ", Breed='" + Breed + '\'' +
                ", Gender='" + Gender + '\'' +
                ", Name='" + Name + '\'' +
                ", Owner='" + Owner + '\'' +
                ", PhotoURL='" + PhotoURL + '\'' +
                ", Weight='" + Weight + '\'' +
                ", key='" + key + '\'' +
                ", IsLost='" + IsLost + '\'' +
                ", possibleMatch=" + possibleMatch +
                '}';
    }
}
