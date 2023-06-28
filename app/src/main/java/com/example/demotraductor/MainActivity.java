package com.example.demotraductor;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.cloud.language.v1.AnalyzeSyntaxResponse;
import com.google.cloud.language.v1.Document;
import com.google.cloud.language.v1.EncodingType;
import com.google.cloud.language.v1.LanguageServiceClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_SPEECH_INPUT = 110;
    private TextToSpeech textToSpeech;
    private LanguageServiceClient languageServiceClient;

    //Metodo que permitira "leer el texto en voz alta" pero en ingles
    private void readText(String text) {
        // Crea una instancia de TextToSpeech
        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                // Configura el idioma de lectura
                int result = textToSpeech.setLanguage(Locale.ENGLISH);
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("TextToSpeech", "Language not supported");
                } else {
                    // Lee el texto
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
                    }
                }
            } else {
                Log.e("TextToSpeech", "Initialization failed");
            }
        });
    }

    //funcion que permitira "leer el texto en voz alta" pero en español
    private void readText2(String text) {
        // Crea una instancia de TextToSpeech
        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                // Configura el idioma de lectura
                int result = textToSpeech.setLanguage(Locale.getDefault());
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("TextToSpeech", "Language not supported");
                } else {
                    // Lee el texto
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
                    }
                }
            } else {
                Log.e("TextToSpeech", "Initialization failed");
            }
        });
    }

    //Metodo onCreate
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            languageServiceClient = LanguageServiceClient.create();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Boton funcion hablar en español
        Button btnSpanish = findViewById(R.id.btnespanish);
        btnSpanish.setOnClickListener(view -> {
            // Crear un Intent para el reconocimiento de voz
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Habla ahora...");

            // Iniciar el reconocimiento de voz
            try {
                startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(MainActivity.this, "El reconocimiento de voz no está disponible en este dispositivo", Toast.LENGTH_SHORT).show();
            }
        });

        //boton funcion hablar en ingles
        Button btnEnglish = findViewById(R.id.btnspeak);
        btnEnglish.setOnClickListener(view -> {
            // Crear un Intent para el reconocimiento de voz
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak Now...");

            // Iniciar el reconocimiento de voz
            try {
                startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(MainActivity.this, "El reconocimiento de voz no está disponible en este dispositivo", Toast.LENGTH_SHORT).show();
            }
        });
    }
    //reconocimiento de voz funcion
    private String detectLanguage(String text) {
        Document doc = Document.newBuilder()
                .setContent(text)
                .setType(Document.Type.PLAIN_TEXT)
                .build();
        try {
            AnalyzeSyntaxResponse response = languageServiceClient.analyzeSyntax(doc, EncodingType.UTF8);
            return response.getLanguage();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    //Metodo que muestra el texto del speech-to-text
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_SPEECH_INPUT) {
            if (resultCode == RESULT_OK && data != null) {
                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                String recognizedText = result.get(0);

                TextView tvRecognizedText = findViewById(R.id.vozatexto);
                tvRecognizedText.setText(recognizedText);

                TextView tvRecognizedText2 = findViewById(R.id.vozatextoingles);
                tvRecognizedText2.setText(recognizedText);

                //para llamar conectar con el servicio de DeepL
                String apiKey = "a10f12d0-5d19-7e71-d17c-4f008c4b4243:fx";
                String sourceLanguage = detectLanguage(recognizedText); //detectar idioma
                String targetLanguage = "EN-US"; // idioma que traduce al ingles

                String targetLanguage2 = "ES"; // idioma que traduce al español

                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl("https://api-free.deepl.com/")
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();

                DeepLService deepLService = retrofit.create(DeepLService.class);

                //para llamar las variables y poder traducir al ingles
                Call<TranslationResponse> call = deepLService.translateText(apiKey, recognizedText, sourceLanguage, targetLanguage);
                call.enqueue(new Callback<TranslationResponse>() {

                    //Metodo para mostrar la respuesta de la llamada de variables
                    @Override
                    public void onResponse(Call<TranslationResponse> call, Response<TranslationResponse> response) {
                        if (response.isSuccessful()) {
                            // Obtiene la traducción del objeto de respuesta
                            Translation translation = response.body().getTranslations().get(0);

                            // Actualiza el TextView con la traducción
                            TextView tvTranslatedText = findViewById(R.id.textotraducidoingles);
                            tvTranslatedText.setText(translation.getTranslatedText());

                            // Lee el texto traducido en voz alta
                            readText(translation.getTranslatedText());
                        } else {
                            // Muestra un mensaje de error si la respuesta no es satisfactoria
                            Toast.makeText(MainActivity.this, "Error al traducir el texto", Toast.LENGTH_SHORT).show();
                        }
                    }

                    //Metodo en caso de que falle la llamada del API
                    @Override
                    public void onFailure(Call<TranslationResponse> call, Throwable t) {
                        // Muestra un mensaje de error si la llamada falla
                        Toast.makeText(MainActivity.this, "Error al llamar a la API de DeepL", Toast.LENGTH_SHORT).show();
                    }
                });

                //para traducir al español
                Call<TranslationResponse> call2 = deepLService.translateText(apiKey, recognizedText, sourceLanguage, targetLanguage2);
                call2.enqueue(new Callback<TranslationResponse>() {
                    @Override
                    public void onResponse(Call<TranslationResponse> call, Response<TranslationResponse> response) {
                        if (response.isSuccessful()) {
                            // Obtiene la traducción del objeto de respuesta
                            Translation translation = response.body().getTranslations().get(0);

                            // Actualiza el TextView con la traducción
                            TextView tvTranslatedText2 = findViewById(R.id.textotraducidoespañol);
                            tvTranslatedText2.setText(translation.getTranslatedText());

                            // Lee el texto traducido en voz alta
                            readText2(translation.getTranslatedText());
                        } else {
                            // Muestra un mensaje de error si la respuesta no es satisfactoria
                            Toast.makeText(MainActivity.this, "Error al traducir el texto", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onFailure(Call<TranslationResponse> call, Throwable t) {
                        // Muestra un mensaje de error si la llamada falla
                        Toast.makeText(MainActivity.this, "Error al llamar a la API de DeepL", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }
}