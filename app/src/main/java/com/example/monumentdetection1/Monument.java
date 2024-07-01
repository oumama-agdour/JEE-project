package com.example.monumentdetection1;

import com.google.gson.annotations.SerializedName;

public class Monument {
    private String description;
    private String  id;
    private String ImageUrl;
    private String Name;




    public Monument() { }

    public Monument(String id, String Name, String description, String ImageUrl) {
        this.id = id;
        this.Name = Name;
        this.description = description;
        this.ImageUrl = ImageUrl;
    }

    public String getId() { return id; }
    public String getName() { return Name; }
    public String getDescription() { return description; }
    public String getImageUrl() { return ImageUrl; }


    public void setId(String id) {
        this.id = id;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setImageUrl(String ImageUrl) {
        this.ImageUrl = ImageUrl;
    }

    public void setName(String name) {
        this.Name = Name;
    }
}

