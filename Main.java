package STC;

import java.io.IOException;
import org.apache.http.client.HttpClient;
import org.apache.http.entity.StringEntity;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.json.JSONException;

import org.apache.http.Header;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.apache.http.client.ClientProtocolException;

public class Main {

    static String grantService = "/services/oauth2/token?grant_type=password";
    private static Header oauthHeader;
    private static Header prettyHeader = new BasicHeader("X-PrettyPrint","1");
    private static String restEndpoint = "/services/data/v48.0";
    private static String baseUri;


    public static void main (String [] args){

        String urlLogin = Credential.getUrlLogin();
        String userName = Credential.getUserName();
        String password = Credential.getPassword();
        String clientSec = Credential.getClientSec();
        String cliensKey = Credential.getClientKey();

        HttpClient httpClient = HttpClientBuilder.create().build();
        String loginUrl = urlLogin + grantService + "&client_id=" +clientSec +"&client_secret=" +cliensKey +"&username=" +userName +"&password=" +password;
        System.out.println("Login URL --> "+loginUrl);

        HttpPost httpPost = new HttpPost(loginUrl);
        HttpResponse response = null;
        try {
            //excute post request
            response = httpClient.execute(httpPost);
        }catch (ClientProtocolException cplExeption){
            cplExeption.printStackTrace();
        }catch (IOException iOE){
            iOE.printStackTrace();
        }

        final int statusCode = response.getStatusLine().getStatusCode();
        if(statusCode != HttpStatus.SC_OK){
            System.out.println("Request Token Error!!!"+statusCode);
        }

        String getResult = null;
        try{
            getResult = EntityUtils.toString(response.getEntity());
        }catch (IOException iOEcept){
            iOEcept.printStackTrace();
        }


        JSONObject jsonObject = null;
        String loginAccessToken = null;
        String loginInstanceUrl = null;
        String tokenTokenType = null;
        try{
            jsonObject = (JSONObject) new JSONTokener(getResult).nextValue();
            loginAccessToken = jsonObject.getString("access_token");
            loginInstanceUrl = jsonObject.getString("instance_url");
            tokenTokenType = jsonObject.getString("token_type");
        }catch (JSONException jsonException){
            jsonException.printStackTrace();
        }

        baseUri = loginInstanceUrl+restEndpoint;
        oauthHeader = new BasicHeader("Authorization",tokenTokenType+" "+loginAccessToken);
        System.out.println("response getStatusLine -->" +response.getStatusLine());
        System.out.println("Alhamdullilah you can successfully login to SFDC !");
        System.out.println("Login url ---> "+loginInstanceUrl);
        System.out.println("SFDC Token --> "+loginAccessToken);
        System.out.println("URL login --> " +baseUri);

        //release connection
        httpPost.releaseConnection();
        PostSMSStatus();

    }


    public static void PostSMSStatus(){
        System.out.println("Posting SMS Status");
        //connect to sfdc using token
        HttpClient httpClient = HttpClientBuilder.create().build();


        try{

            String urlPost = baseUri+"/sobjects/SMS_Respond_Status__c";
            System.out.println("URL Post --> "+urlPost);
            HttpPost httpPost = new HttpPost(urlPost);
            httpPost.addHeader(oauthHeader);
            httpPost.addHeader(prettyHeader);
            httpPost.addHeader("Content-Type","application/json");
            //JSON POST BODY
            StringEntity params = new StringEntity ("{\"delivery_status__c\":\"Delivered\",\"ref_id__c\":\"00QN00000019Q3tTESTFROMJAVA\",\"code_sms__c\":\"938A9DC058855A1CA6ACACBROTBROT\",\"Mobile_Phone__c\":\"08123435343\" }");
            httpPost.setEntity(params);

            //execute post
            HttpResponse response1 = httpClient.execute(httpPost);

            //the result
            int statusCode = response1.getStatusLine().getStatusCode();

            if (statusCode !=HttpStatus.SC_CREATED){
                String response_Ok = EntityUtils.toString(response1.getEntity());
                System.out.println("Return 201 --> " +response_Ok);
            }else {String response_Nok = EntityUtils.toString(response1.getEntity());
                System.out.println("Return not 201 --> "+response_Nok);}

        }catch(ClientProtocolException cpExcept){
            cpExcept.printStackTrace();
        }catch (IOException iOE){
            iOE.printStackTrace();
        }
    }

}