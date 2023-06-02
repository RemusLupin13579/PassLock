package com.project.passlock;

import android.graphics.Bitmap;

public class Category {

    private Bitmap icon, arrow;
    private String title;

    public Category(String title) {
        this.icon = icon;
        this.arrow = arrow;
        this.title = title;
    }

    public Bitmap getIcon() {
        return icon;
    }

    public void setIcon(Bitmap icon) {
        this.icon = icon;
    }

    public Bitmap getArrow() {
        return arrow;
    }

    public void setArrow(Bitmap arrow) {
        this.arrow = arrow;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

}
