package com.example.demotraductor;

import com.google.gson.annotations.SerializedName;

public class Translation {
    @SerializedName("text")
    private String translatedText;

    public String getTranslatedText() {
        return translatedText;
    }
}
