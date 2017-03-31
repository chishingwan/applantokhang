package edu.sti.tokhang;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener,View.OnClickListener {
    TextView input;
    public static TextView output;
    Button mic;
    Button maps;
Button aid;
    Button emer;
    TextToSpeech tts;
    SQLiteDatabase db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        input = (TextView)findViewById(R.id.txtInput);
        output=(TextView)findViewById(R.id.txtOutput);
        mic = (Button)findViewById(R.id.btnMic);
        mic.setOnClickListener(this);
        maps = (Button)findViewById(R.id.mapbtn);
        maps.setOnClickListener(this);
        aid = (Button)findViewById(R.id.btnAid);
        emer = (Button) findViewById(R.id.btn911);
        aid.setVisibility(View.GONE);
        emer.setVisibility(View.GONE);
        tts = new TextToSpeech(this, this);
        db = openOrCreateDatabase(
                "tokhangdb"
                , SQLiteDatabase.CREATE_IF_NECESSARY
                , null
        );

        db.execSQL("Create table if not exists chatTbl(question varchar, answer varchar, source varchar);");
        Cursor c = db.rawQuery("SELECT * FROM chatTbl", null);
        if(c.getCount() ==0){
        db.execSQL("Insert into chatTbl values ('what is murder?','the crime of unlawfully killing a person especially with malice aforethought','https://www.merriam-webster.com/dictionary/murder')");
        db.execSQL("Insert into chatTbl values ('can police arrest me without evidence?','Only if they have probable cause to believe you have committed the crime. Once they have probable cause, they can arrest you in a number of ways','http://criminal.findlaw.com/criminal-law-basics/common-criminal-law-questions.html')");
        db.execSQL("Insert into chatTbl values ('driving laws that can help me drive safe','there are several laws on how road accidents can be lessen, speed limits, use of seat belts, distracted driving, drunked and drugged driving and education and training for drivers, ','http://www.rappler.com/corruption/45-road-safety/163933-philippine-road-safety-laws-policies')");
            db.execSQL("Insert into chatTbl values ('i need help','any details of your current situation?','')");
            db.execSQL("Insert into chatTbl values ('im hurt','I will send sms to your emergency contacts now! Please secure yourself','')");
            db.execSQL("Insert into chatTbl values ('i am hurt','I will send sms to your emergency contacts now! Please secure yourself','')");
            db.execSQL("Insert into chatTbl values ('i got hurt','I will send sms to your emergency contacts now! Please secure yourself!','')");
        }
        db.execSQL("Create table if not exists markertbl(crimeType varchar,lat varchar,long varchar,date varchar,time varchar);");
        c = db.rawQuery("Select * from markertbl", null);
        if (c.getCount() < 1) {
            db.execSQL("Insert into markertbl values('Murder','14.553081','121.052230','3-10-2017','20:00');");
            db.execSQL("Insert into markertbl values('Hold-up','14.553792','121.051527','2-15-2017','12:00');");
            db.execSQL("Insert into markertbl values('Murder','14.552421','121.050975','1-5-2017','08:00');");
            db.execSQL("Insert into markertbl values('Rape','14.552929','121.052822','3-7-2017','23:00');");
            db.execSQL("Insert into markertbl values('Kidnap','14.553565','121.052854','2-25-2017','22:00');");
            db.execSQL("Insert into markertbl values('Snatch','14.552558','121.052065','1-17-2017','18:00');");
            db.execSQL("Insert into markertbl values('Rape','14.551334','121.052548','3-16-2017','11:00');");
        }

    }

    public void promptSpeechInput(){
        Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        i.putExtra(RecognizerIntent.EXTRA_PROMPT,"Talk Now!");
        try{
            startActivityForResult(i,100);

        }
        catch(Exception e){
            Toast.makeText(MainActivity.this, e.toString(),
                    Toast.LENGTH_LONG).show();
        }


    }

    @Override
    public void onClick(View view) {
        if (view == mic){

            promptSpeechInput();

        }
        if(view == maps){
            startActivity(new Intent(MainActivity.this,
                    map.class));

        }
    }
    public void onActivityResult(int request_code,int result_code,Intent i){
        super.onActivityResult(request_code,result_code,i);
        switch (request_code){

            case 100: if (result_code == RESULT_OK && i != null){
                ArrayList<String> result = i.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                input.setText(result.get(0).toLowerCase());
                try {
                    //Social condition
                    Cursor c = db.rawQuery("SELECT * FROM chatTbl where question like '%"+input.getText().toString()+"%'", null);
                    if(c.getCount()>0) {
                        c.moveToFirst();
                        output.setText(c.getString(1));
                        if(output.getText().equals("i'm hurt") || output.getText().equals("i am hurt") || output.getText().equals("i got hurt")){
                            aid.setVisibility(View.VISIBLE);
                            emer.setVisibility(View.VISIBLE);
                        }
                        else{
                            aid.setVisibility(View.GONE);
                            emer.setVisibility(View.GONE);
                        }

                    }
                    else {
                        output.setText("Cannot find the proper answer for your question!");
                        db.execSQL("Insert into chatTbl values ('"+input.getText().toString()+"','You already asked this question!, We recorded it as pending question!','')");


                    }
                }
                catch(Exception e){
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, e.toString(),
                            Toast.LENGTH_LONG).show();

                }
                finally {

                    speech();
                }
            }
                break;
        }

    }
    //google api end
    public void speech(){
        try {
            tts.speak(output.getText().toString(), TextToSpeech.QUEUE_FLUSH, null);

        }
        catch(Exception e){
        e.printStackTrace();
        }
    }

    public void onInit(int status) {

        try {
            if (status == TextToSpeech.SUCCESS) {
                Locale bahasa = tts.getLanguage();
                int result = tts.setLanguage(bahasa);
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("TTS", "Language not supported");
                }


            } else {
                Log.e("TTS", "Initialization Failed!");
            }

        }

        catch(Exception e){
            Toast.makeText(MainActivity.this, e.toString(),
                    Toast.LENGTH_LONG).show();


        }
    }
    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        tts.shutdown();
    }

}
