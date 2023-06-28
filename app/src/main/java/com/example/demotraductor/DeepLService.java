package com.example.demotraductor;

import retrofit2.Call;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface DeepLService {
    @POST("/v2/translate")
    Call<TranslationResponse> translateText(
            @Query("auth_key") String apiKey,
            @Query("text") String textToTranslate,
            @Query("source_lang") String sourceLanguage, // nuevo parámetro
            @Query("target_lang") String targetLanguage);
}
