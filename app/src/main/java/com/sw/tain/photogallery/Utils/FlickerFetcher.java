package com.sw.tain.photogallery.Utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.widget.Gallery;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by home on 2016/11/23.
 */

public class FlickerFetcher {
    private static final String TAG = "FlickerFetcher";
    private final Uri mBaseUri;

    public FlickerFetcher() {
         mBaseUri = Uri.parse("https://api.flickr.com/services/rest/")
                .buildUpon()
                .appendQueryParameter("api_key", API_KEY)
                .appendQueryParameter("nojsoncallback", "1")
                .appendQueryParameter("extras", "url_s")
                .appendQueryParameter("format", "json")
                .build();
    }

    public byte[] getUrlByteArray(String urlStr) throws IOException {

        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(urlStr);
            urlConnection = (HttpURLConnection)url.openConnection();
 //           urlConnection.setConnectTimeout(5*1000);
 //           urlConnection.setReadTimeout(3*1000);
            boolean redirect = false;
            int status = urlConnection.getResponseCode();
            if(status!=HttpURLConnection.HTTP_OK){
                // normally, 3xx is redirect
                if(status==HttpURLConnection.HTTP_MOVED_PERM
                        || status==HttpURLConnection.HTTP_MOVED_TEMP
                        || status==HttpURLConnection.HTTP_SEE_OTHER)
                    redirect = true;
                else{
                    Log.d(TAG, urlConnection.getResponseMessage() + " with: " + urlStr);
                    throw new IOException(urlConnection.getResponseMessage() + " with: " + urlStr);
                }
            }
            while(redirect){
                // get redirect url from "location" header field
                String newUrl = urlConnection.getHeaderField("Location");
                // open the new connnection again
                urlConnection = (HttpURLConnection) new URL(newUrl).openConnection();
                status = urlConnection.getResponseCode();
                if(status!=HttpURLConnection.HTTP_OK){
                    if(status==HttpURLConnection.HTTP_MOVED_PERM
                            || status==HttpURLConnection.HTTP_MOVED_TEMP
                            || status==HttpURLConnection.HTTP_SEE_OTHER){
                        redirect = true;
                        System.out.println("Redirect to URL : " + newUrl);
                        continue;
                    }
                    else{
                        Log.d(TAG, urlConnection.getResponseMessage() + " with: " + urlStr);
                        throw new IOException(urlConnection.getResponseMessage() + " with: " + urlStr);
                    }
                }else{
                    System.out.println("Redirect to URL : " + newUrl);
                    break;
                }
            }
            // get the cookie if need, for login
//          String cookies = urlConnection.getHeaderField("Set-Cookie");
//          urlConnection.setRequestProperty("Cookie", cookies);
//          urlConnection.addRequestProperty("Accept-Language", "en-US,en;q=0.8");
//          urlConnection.addRequestProperty("User-Agent", "Mozilla");
//          urlConnection.addRequestProperty("Referer", "google.com");



            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            int bufferread;
            byte[] data = new byte[1024];
            InputStream inputStream = urlConnection.getInputStream();
            while((bufferread = inputStream.read(data))>0)
                outputStream.write(data, 0, bufferread);

            outputStream.close();
            return outputStream.toByteArray();

        }finally {
            if(urlConnection!=null) urlConnection.disconnect();
        }
    }

    public String getUrlString(String url) throws IOException {
//        return getUrlByteArray(url).toString();
        return  new String(getUrlByteArray(url));
    }

    public Bitmap getBitmap(String urlStr) {
        HttpURLConnection urlConnection = null;
        URL url;
        Bitmap bitmap = null;
        try {
            url = new URL(urlStr);
            urlConnection = (HttpURLConnection) url.openConnection();
//            urlConnection.setConnectTimeout(5 * 1000);
//            urlConnection.setReadTimeout(3 * 1000);
//            urlConnection.setDoInput(true);
//            urlConnection.setDoOutput(true);
            bitmap = BitmapFactory.decodeStream(urlConnection.getInputStream());
        } catch (MalformedURLException e) {
            Log.d("Downloader", "invalid URL!");
            e.printStackTrace();
        } catch (IOException e) {
            Log.d("Downloader", "failed download from: "+ urlStr);
            e.printStackTrace();
        }finally {
            if(urlConnection!=null)
                urlConnection.disconnect();
        }
        return bitmap;
    }

    private static final String API_KEY = "7ba84bb9d79956729332868e04317045";

    public List<GalleryItem> FetchItem(int page){
        List<GalleryItem> itemList = new ArrayList<>();

        String jsonResutl = null;

        try {
            String url = buildUrl(page);
            Log.d(TAG, url);
            jsonResutl = getUrlString(url);
 //           Log.d(TAG, jsonResutl);
            JSONObject jsonObject = new JSONObject(jsonResutl);
            JSONObject jsonPhotosObject = jsonObject.getJSONObject("photos");
            JSONArray jsonPhotoArry = jsonPhotosObject.getJSONArray("photo");
            for(int i=0; i<jsonPhotoArry.length(); i++){
                JSONObject jsonPhotoObject = jsonPhotoArry.getJSONObject(i);
                if(jsonPhotoObject.getString("url_s")==null || jsonPhotoObject.getString("url_s").equals("")) continue;
                GalleryItem item =  new GalleryItem();
                item.setCaption(jsonPhotoObject.getString("title"));
                item.setId(jsonPhotoObject.getString("id"));
                item.setUrl(jsonPhotoObject.getString("url_s"));
                itemList.add(item);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return itemList;
    }

    public int FetchTotoalPages(){
        String jsonResutl = null;
        int pages = 0;

        try {
            String url = buildUrl();
            jsonResutl = getUrlString(url);
            JSONObject jsonObject = new JSONObject(jsonResutl);
            JSONObject jsonPhotosObject = jsonObject.getJSONObject("photos");
            pages = jsonPhotosObject.getInt("pages");
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return pages;
    }

    private String buildUrl(){
        String url = mBaseUri.buildUpon()
                .appendQueryParameter("method", "flickr.photos.getRecent")
                .build()
                .toString();

        return url;
    }

    private String buildUrl(int page){
        String url = mBaseUri.buildUpon()
                .appendQueryParameter("method", "flickr.photos.getRecent")
                .appendQueryParameter("page", ""+page)
                .build()
                .toString();
        return url;
    }

    private String buildUrl(String query){
        String url = mBaseUri.buildUpon()
                .appendQueryParameter("method", "flickr.photos.search")
                .appendQueryParameter("text", query)
                .build()
                .toString();
        return url;
    }

    public List<GalleryItem> searchItem(String queryStr) {
        List<GalleryItem> itemList = new ArrayList<>();

        String jsonResutl = null;

        try {
            String url = buildUrl(queryStr);
            Log.d(TAG, url);
            jsonResutl = getUrlString(url);
            JSONObject jsonObject = new JSONObject(jsonResutl);
            JSONObject jsonPhotosObject = jsonObject.getJSONObject("photos");
            JSONArray jsonPhotoArry = jsonPhotosObject.getJSONArray("photo");
            for(int i=0; i<jsonPhotoArry.length(); i++){
                JSONObject jsonPhotoObject = jsonPhotoArry.getJSONObject(i);
                if(jsonPhotoObject.getString("url_s")==null || jsonPhotoObject.getString("url_s").equals("")) continue;
                GalleryItem item =  new GalleryItem();
                item.setCaption(jsonPhotoObject.getString("title"));
                item.setId(jsonPhotoObject.getString("id"));
                item.setUrl(jsonPhotoObject.getString("url_s"));
                itemList.add(item);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return itemList;
    }
}
