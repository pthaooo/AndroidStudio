package com.example.baivolley.api;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.text.TextUtils;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.example.baivolley.model.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class SignupApi {

    public static void signUp(
            Context context, String name, String email, String password, String imageUri,
            final SignupListener listener
    ) {
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || imageUri == null) {
            listener.onSignupError("All fields are required");
            return;
        }

        // Convert Uri to Bitmap
//        Bitmap bitmap = /* Convert your Uri to Bitmap */ uriToBitmap(context, imageUri);

        // Convert Bitmap to Base64 string
//        String imageString = bitmapToBase64(bitmap);

        StringRequest stringRequest = new StringRequest(
                Request.Method.POST, Constants.URL_REGISTER,
                response -> {
                    try {
                        JSONObject obj = new JSONObject(response);
                        if (!obj.getBoolean("error")) {
                            JSONObject userJson = obj.getJSONObject("user");
                            User user = new User(
                                    userJson.getInt("id"),
                                    userJson.getString("username"),
                                    userJson.getString("email"),
                                    userJson.getString("gender")
//                                    userJson.getString("images")
                            );
                            listener.onSignupSuccess(user);
                        } else {
                            listener.onSignupError(obj.getString("message"));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        listener.onSignupError("JSON parsing error");
                    }
                },
                error -> listener.onSignupError("Signup request failed")
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("username", name);
                params.put("email", email);
                params.put("password", password);
                params.put("gender", "nam");
                params.put("images", imageUri);
                return params;
            }
        };

        VolleySingle.getInstance(context)
                    .addToRequestQueue(stringRequest);
    }

    private static String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        if (bitmap == null) return "";
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return android.util.Base64.encodeToString(byteArray, android.util.Base64.DEFAULT);
    }

    private static Bitmap uriToBitmap(Context context, Uri uri) {
        try {
            return android.provider.MediaStore.Images.Media.getBitmap(
                    context.getContentResolver(),
                    uri
            );
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public interface SignupListener {
        void onSignupSuccess(User user);

        void onSignupError(String errorMessage);
    }
}
