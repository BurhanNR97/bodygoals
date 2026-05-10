package com.spk.bodygoals.service;

public class ApiConfig {
    public static final String BASE_URL = "http://124.156.201.86/api/";
    public static final String ADMIN_DASHBOARD = BASE_URL + "admin/dashboard";
    public static final String USER_DASHBOARD = BASE_URL + "user/dashboard";
    public static final String USER_PROFIL = BASE_URL + "user/profil";
    public static final String USER_PROFIL_UPDATE = BASE_URL + "user/profil/update";
    public static final String MAKANAN = BASE_URL + "makanan";
    public static final String INPUT_MAKAN_HARIAN = BASE_URL + "input-makan-harian";
    public static final String REKOMENDASI_STATUS = BASE_URL + "user/rekomendasi/status";
    public static final String RESET_REKOMENDASI = BASE_URL + "user/rekomendasi/reset";
    public static final String PENYAKIT = BASE_URL + "penyakit";
    public static final String UPDATE_PENYAKIT_USER = BASE_URL + "user/penyakit/update";

    public static String getUrl(String endpoint) {
        return BASE_URL + endpoint;
    }
}
