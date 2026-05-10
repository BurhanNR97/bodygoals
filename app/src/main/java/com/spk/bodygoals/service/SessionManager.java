package com.spk.bodygoals.service;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREF_NAME = "BodyGoalsSession";

    private static final String KEY_LOGIN = "isLogin";
    private static final String KEY_ID = "id";
    private static final String KEY_NAMA = "nama";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_ROLE = "role";
    private static final String KEY_TOKEN = "token";

    SharedPreferences pref;
    SharedPreferences.Editor editor;
    Context context;

    public SessionManager(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    // SIMPAN LOGIN
    public void saveLogin(int id, String name, String email, String role, String token) {
        editor.putBoolean(KEY_LOGIN, true);
        editor.putInt(KEY_ID, id);
        editor.putString(KEY_NAMA, name);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_ROLE, role);
        editor.putString(KEY_TOKEN, token);
        editor.apply();
    }

    // CEK LOGIN
    public boolean isLogin() {
        return pref.getBoolean(KEY_LOGIN, false);
    }

    // GET DATA
    public int getId() {
        return pref.getInt(KEY_ID, 0);
    }

    public String getRole() {
        return pref.getString(KEY_ROLE, "user");
    }

    public String getName() {
        return pref.getString(KEY_NAMA, "");
    }

    public String getEmail() {
        return pref.getString(KEY_EMAIL, "");
    }

    public String getToken() {
        return pref.getString(KEY_TOKEN, "");
    }

    // LOGOUT
    public void logout() {
        editor.clear();
        editor.apply();
    }
}