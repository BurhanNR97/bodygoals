package com.spk.bodygoals.service;

public class ApiConfig {
    public static final String BASE_URL = "https://bodygoals.web.id/api/";
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
    public static final String ADMIN_PETUGAS_FETCH = BASE_URL + "admin/petugas";
    public static final String ADMIN_PETUGAS_HAPUS = BASE_URL + "admin/petugas/hapus";
    public static final String ADMIN_PETUGAS_STORE = BASE_URL + "admin/petugas/store";
    public static final String ADMIN_CEK_NIK = BASE_URL + "admin/petugas/cek-nik";
    public static final String ADMIN_CEK_EMAIL = BASE_URL + "admin/petugas/cek-email";
    public static final String ADMIN_PASIEN_FETCH = BASE_URL + "admin/pasien";
    public static final String ADMIN_PASIEN_HAPUS = BASE_URL + "admin/pasien/hapus";
    public static final String ADMIN_BMI_FETCH = BASE_URL + "admin/bmi";
    public static final String ADMIN_BMI_STORE = BASE_URL + "admin/bmi/store";
    public static final String ADMIN_BMI_HAPUS = BASE_URL + "admin/bmi/hapus";
    public static final String ADMIN_RULES_PENYAKIT_FETCH = BASE_URL + "admin/rules-penyakit";
    public static final String ADMIN_RULES_PENYAKIT_DETAIL = BASE_URL + "admin/rules-penyakit/";
    public static final String ADMIN_RULES_PENYAKIT_HAPUS = BASE_URL + "admin/rules-penyakit/hapus";
    public static final String ADMIN_MAKANAN_FETCH = BASE_URL + "admin/makanan";
    public static final String ADMIN_MAKANAN_KATEGORI = BASE_URL + "admin/makanan/kategori";
    public static final String ADMIN_MAKANAN_DETAIL = BASE_URL + "admin/makanan/";
    public static final String ADMIN_MAKANAN_HAPUS = BASE_URL + "admin/makanan/hapus";
    public static final String ADMIN_MAKANAN_UPDATE = BASE_URL + "admin/makanan/update";
    public static String getUrl(String endpoint) {
        return BASE_URL + endpoint;
    }
}
