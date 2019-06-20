package com.example.samsungshoeshoo;

public class ListItem {

    private int shelfId;
    private int occ;
    private String type, colour, owner;
    private String image;

    public ListItem() {

    }

    public ListItem(int shelfId, int occ, String type, String colour, String owner, String image) {
        this.shelfId = shelfId;
        this.occ = occ;
        this.type = type;
        this.colour = colour;
        this.owner = owner;
        this.image = image;
    }

    public int getShelfId() {
        return shelfId;
    }

    public int getOcc() {
        return occ;
    }

    public String getType() {
        return type;
    }

    public String getColour() {
        return colour;
    }

    public String getOwner() {
        return owner;
    }

    public String getImage() {
        return image;
    }


    public void setShelfId(int shelfId) {
        this.shelfId = shelfId;
    }

    public void setOcc(int occ) {
        this.occ = occ;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setColour(String colour) {
        this.colour = colour;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public void setImage(String image) {
        this.image = image;
    }

}
