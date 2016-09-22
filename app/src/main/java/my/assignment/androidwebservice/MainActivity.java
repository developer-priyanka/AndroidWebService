
package my.assignment.androidwebservice;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    TextView locationtxtV,updatetxtV,temtxtV,desctxtV,humiditytxtV,pressuretxtV,icontxtV;
    Typeface weatherFont;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar();

        weatherFont=Typeface.createFromAsset(this.getAssets(),"fonts/weather.ttf");
        locationtxtV=(TextView)findViewById(R.id.citycountrytxt);
        updatetxtV=(TextView)findViewById(R.id.updatetxt);
        temtxtV=(TextView)findViewById(R.id.temptxt);
        desctxtV=(TextView)findViewById(R.id.desctxt);
        humiditytxtV=(TextView)findViewById(R.id.humiditytxt);
        pressuretxtV=(TextView)findViewById(R.id.pressuretxt);
        icontxtV=(TextView)findViewById(R.id.icon);
        icontxtV.setTypeface(weatherFont);

        String city=new CityPreference(this).getCity();

        String weatherUrl= "http://api.openweathermap.org/data/2.5/weather?q=" +city+
               ",In&appid=d7b900681c37193223281142bd919019";
         new FetchWeatherInformation().execute(weatherUrl);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==R.id.change_city){
            showInputDialog();
        }

        return true;
    }

    public void showInputDialog(){
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("Enter City");
        final EditText input=new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                changeCity(input.getText().toString());
                new CityPreference(MainActivity.this).setCity(input.getText().toString());

            }
        });
        builder.show();
    }
    public void changeCity(String city){
        String weatherUrl= "http://api.openweathermap.org/data/2.5/weather?q=" +city+
                ",In&appid=d7b900681c37193223281142bd919019";
        new FetchWeatherInformation().execute(weatherUrl);


    }

    private class FetchWeatherInformation extends AsyncTask<String,Void,Void>{
        ProgressDialog progressDialog=new ProgressDialog(MainActivity.this);
        String content;
        String error;
        String data="";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog.setTitle("Please wait....");
            progressDialog.show();

        }

        @Override
        protected Void doInBackground(String... strings) {
            BufferedReader br=null;
            URL url;

            try {
                url=new URL(strings[0]);

                URLConnection connection=url.openConnection();
                connection.setDoOutput(true);

                OutputStreamWriter outputStreamWriter=new OutputStreamWriter(connection.getOutputStream());
                outputStreamWriter.write(data);
                outputStreamWriter.flush();

                br=new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder sb=new StringBuilder();
                String line=null;

                while ((line=br.readLine())!=null){
                    sb.append(line+"\n");

                }
                content=sb.toString();


            } catch (MalformedURLException e) {
                error=e.getMessage();
                e.printStackTrace();
            } catch (IOException e) {
                error=e.getMessage();
                e.printStackTrace();
            }finally {
                try {
                    br.close();
                } catch (IOException e) {
                    error=e.getMessage();
                    e.printStackTrace();
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            DateFormat df=DateFormat.getDateTimeInstance();
            super.onPostExecute(aVoid);
            progressDialog.dismiss();
            if(error!=null){
                Toast.makeText(MainActivity.this,error,Toast.LENGTH_LONG).show();

            }else{
                String output="";
                try {
                    JSONObject respone=new JSONObject(content);

                    JSONArray jsonArray=respone.getJSONArray("weather");
                    JSONObject main=respone.getJSONObject("main");
                    String description=jsonArray.getJSONObject(0).getString("description");
                    String humidity=main.getString("humidity");
                    String pressure=main.getString("pressure");
                    String cityName=respone.getString("name");
                    String countryName=respone.getJSONObject("sys").getString("country");
                    double tempkelvin=main.getDouble("temp");
                    double tempcelcius=tempkelvin-273.15;
                    String cur_temp=String.format("%.2f",tempcelcius)+" \u00b0"+"C";
                    String updatedOn=df.format(new Date(respone.getLong("dt")*1000));
                    Log.i("WeatherInfo:",description+" "+humidity+" "+pressure+" "+cityName+" "+countryName+" "+cur_temp+" "+updatedOn);

                    locationtxtV.setText(cityName+","+countryName);
                    updatetxtV.setText("LastUpdated:"+updatedOn);
                    temtxtV.setText(cur_temp);
                    desctxtV.setText(description.toUpperCase());
                    humiditytxtV.setText("Humidity:"+humidity+"%");
                    pressuretxtV.setText("Pressure:"+pressure+" hPa");

                    setWeatherIcon(jsonArray.getJSONObject(0).getInt("id"),respone.getJSONObject("sys").getLong("sunrise")*1000,respone.getJSONObject("sys").getLong("sunset")*1000);

                } catch (JSONException e) {
                    Log.e("Weather Info:","one or more field not found in JSON data");
                    e.printStackTrace();
                }
            }
        }
    }
    public void setWeatherIcon(int id,long sunrise,long sunset){
        int actualId=id/100;
        String icon="";

        if(id==800){
            long current_time=new Date().getTime();
            if(current_time>=sunrise && current_time<sunset) {
                icon = this.getString(R.string.weather_sunny);
            } else{
                   icon=this.getString(R.string.weather_clear_night);
                }
            }else{
            switch(actualId){
                case 2:icon=this.getString(R.string.weather_thunder);
                    break;
                case 3:icon=this.getString(R.string.weather_drizzle);
                    break;
                case 5:icon=this.getString(R.string.weather_rainy);
                    break;
                case 6:icon=this.getString(R.string.weather_snowy);
                    break;
                case 7:icon=this.getString(R.string.weather_foggy);
                    break;
                case 8:icon=this.getString(R.string.weather_cloudy);
                    break;
            }
        }
        icontxtV.setText(icon);
    }
}
