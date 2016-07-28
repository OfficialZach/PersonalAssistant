package com.application.nick.personalassistant;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextToSpeech tts;
    private String[] questions, answers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = tts.setLanguage(Locale.US);
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "This Language is not supported");
                    }
                    speak("Hello");

                } else {
                    Log.e("TTS", "Initilization Failed!");
                }
            }
        });

        findViewById(R.id.microphoneButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listen();
            }
        });

        //load questions and answers from xml
        Resources res = getResources();
        questions = res.getStringArray(R.array.questions);
        answers = res.getStringArray(R.array.answers);
    }

    private void speak(String text){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        } else {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    private void listen(){
        Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        i.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say something");

        try {
            startActivityForResult(i, 100);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(MainActivity.this, "Your device doesn't support Speech Recognition", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 100){
            if (resultCode == RESULT_OK && null != data) {
                ArrayList<String> res = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                String inSpeech = res.get(0);
                recognition(inSpeech);
            }
        }
    }

    private void recognition(String text){
        Log.e("Speech", "" + text);

        //Ask: What time is it?
        if(text.contains("what") && text.contains(" time")){
            SimpleDateFormat sdfDate = new SimpleDateFormat("HH:mm", Locale.US);//dd/MM/yyyy
            Date now = new Date();
            String[] strDate = sdfDate.format(now).split(":");

            if(Integer.valueOf(strDate[0]) > 12)
                strDate[0] = Integer.toString(Integer.valueOf(strDate[0]) - 12);
            if(strDate[1].contains("00"))
                strDate[1] = "o'clock";

            String time = strDate[0] + ":" + strDate[1];

            Log.i("Time", time);
            speak("The time is " + time);

        } else {

            boolean responseFound = false;
            for(int i = 0; i < questions.length; i++) {
                String[] words = questions[i].split(" ");
                boolean containsWords = true;
                for(String word : words) {
                    if(!text.contains(word)) {
                        containsWords = false;
                        break;
                    }
                }

                if(containsWords) {
                    responseFound = true;
                    speak(answers[i]);
                    break;
                }
            }

            if(!responseFound) {

                speak("You said... " + text);
            }
        }




    }


}
