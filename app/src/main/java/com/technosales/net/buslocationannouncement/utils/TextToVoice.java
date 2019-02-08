package com.technosales.net.buslocationannouncement.utils;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import com.technosales.net.buslocationannouncement.R;

import java.util.Locale;

public class TextToVoice {
    private Context context;
    private TextToSpeech textToSpeech;

    public TextToVoice(Context context) {
        this.context = context;
        try {

            initTTs();
        } catch (Exception ex) {

        }
    }

    public TextToSpeech initTTs() {
        textToSpeech = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    textToSpeech.setLanguage(new Locale("nep"));
                }
            }
        });
        textToSpeech.setPitch(1.1f);
        return textToSpeech;
    }

    public void speakStation(String current, String next) {
        String speakVoice = context.getString(R.string.hamiahile) + current + ", " + context.getString(R.string.aaipugeu) + next + ", " + context.getString(R.string.pugnechau);
        textToSpeech.speak(speakVoice, TextToSpeech.QUEUE_FLUSH, null);
        Log.i("speakVoice", "" + speakVoice);
    }


}
