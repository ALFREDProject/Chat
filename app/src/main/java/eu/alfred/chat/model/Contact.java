package eu.alfred.chat.model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Contact {

    private String name = "";
    private String lastName = "";
    private String phoneNumber = "";

    public Contact(String name, String lastName, String phoneNumber) {
        this.name = name;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getIdentifier() {
        String rawId = "contact_" + name + "_" + lastName + "_" + phoneNumber;
        String loweredString = rawId.toLowerCase();
        Pattern pattern = Pattern.compile("[^a-z0-9]");
        Matcher matcher = pattern.matcher(loweredString);
        String formattedId = matcher.replaceAll("_");
        return formattedId;
    }
}
