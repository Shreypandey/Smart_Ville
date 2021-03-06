package com.example.smartville;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class LoginActivity extends AppCompatActivity {

        EditText emailtext,passwordtext;
        Button loginButton,Signupbutton;
        final String[] passwordget=new String[1];
        final String[] password = new String[1];
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_login);
            emailtext=findViewById(R.id.login_emailinput);
            passwordtext=findViewById(R.id.login_passwordinput);
            loginButton=findViewById(R.id.login_loginButton);
            Signupbutton=findViewById(R.id.login_newaccountButton);
            loginButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    String email=emailtext.getText().toString();
                    if (email.isEmpty()){
                        emailtext.setError("Please Enter EmailID");
                        return;
                    }
                    else if (!CreateHash.isValid(emailtext.getText().toString())){
                        emailtext.setError("Enter Correct Email");
                        return;
                    }
                    else if (passwordtext.getText().toString().isEmpty()){
                        passwordtext.setError("Please Enter Password");
                        return;
                    }
                    password[0] =CreateHash.createHash(passwordtext.getText().toString());
                    new LoginTask().execute(email);


                }
            });
            Signupbutton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent=new Intent(LoginActivity.this,SignupActivity.class);
                    startActivity(intent);
                }
            });

        }

    private class LoginTask extends AsyncTask<String,Void,String[]>{
        @Override
        protected void onPostExecute(String[] s) {
            super.onPostExecute(s);
            if (passwordget[0]==null){
                emailtext.setError("Email Does not exist");
            }
            else if(passwordget[0].equals(password[0])){
                Intent intent=new Intent(LoginActivity.this,HomeActivity.class);
                SharedPreferences sharedpreferences = getSharedPreferences("Smart_Ville", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor=sharedpreferences.edit();
                editor.putString("UID",s[5]);
                editor.putString("email",s[1]);
                editor.putString("fname",s[2]);
                editor.putString("lname",s[3]);
                editor.putString("contact",s[4]);
                editor.putString("status","true");
                editor.commit();
                startActivity(intent);
                finish();
            }
            else {
                passwordtext.setError("Password not matched");
            }
        }

        @Override
        protected String[] doInBackground(String... strings) {
            String info[]=new String[6];
            try {
                String email=strings[0];
                String query = "{\"query\":\"query {\\n  User(\\n    where: {EmailId: {_eq: \\\""+email+"\\\"}}\\n  ) {\\n    EmailId\\n    Password\\n    FirstName\\n    LastName\\n    Id\\n    ContactNo\\n  }\\n}\",\"variables\":null}";

                URL url = new URL(MainActivity.hasuraurl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                conn.setRequestProperty("Accept","application/json");
                conn.setDoOutput(true);
                conn.setDoInput(true);
                DataOutputStream os = new DataOutputStream(conn.getOutputStream());
                os.writeBytes(query);
                BufferedReader br = new BufferedReader(new InputStreamReader(
                        (conn.getInputStream())));
                String output1="",output;
                while ((output = br.readLine()) != null) {
                    output1+=output;
                }
                Log.e("fatal",output1);
                JSONObject jsonObj = new JSONObject(output1);
                jsonObj=jsonObj.getJSONObject("data");
                JSONArray data = jsonObj.getJSONArray("User");
                if (data==null){
                    passwordget[0]=null;
                    return null;
                }
                String emails=data.getString(0);
                jsonObj=new JSONObject(emails);
                info[0]=jsonObj.getString("Password");
                info[1]=jsonObj.getString("EmailId");
                info[2]=jsonObj.getString("FirstName");
                info[3]=jsonObj.getString("LastName");
                info[4]=jsonObj.getString("ContactNo");
                info[5]=jsonObj.getString("Id");


                os.flush();
                os.close();

                conn.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
            passwordget[0]=info[0];
            Log.e("fatal",passwordget[0]);
            return info;
        }
    }
}
