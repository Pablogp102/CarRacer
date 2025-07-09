package com.carracer.infrastructure.hilt;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.carracer.BuildConfig; // Importuj BuildConfig
import com.carracer.infrastructure.network.ApiConfig;
import com.carracer.infrastructure.network.ApiService;
import com.carracer.infrastructure.network.storage.TokenStorage;

import javax.inject.Singleton;
import java.util.concurrent.TimeUnit;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

// Importy dla konfiguracji HTTPS (potrzebne tylko, jeśli akceptujesz niezaufane certyfikaty w DEV)
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.NoSuchAlgorithmException;
import java.security.KeyManagementException;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;


@Module
@InstallIn(SingletonComponent.class)
public class NetworkModule {

    @Provides
    @Singleton
    Gson provideGson() {
        return new GsonBuilder().create();
    }

    @Provides
    @Singleton
    OkHttpClient provideOkHttpClient(TokenStorage tokenStorage) {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        Interceptor apiInterceptor = chain -> {
            Request original = chain.request();
            String token = tokenStorage.getToken();

            Request.Builder builder = original.newBuilder();

            // 1. ZAWSZE dodajemy nasz tajny Klucz API
            builder.header("X-Api-Key", ApiConfig.API_KEY);

            // 2. Jeśli mamy token, to DODATKOWO go dołączamy
            if (token != null) {
                builder.header("Authorization", "Bearer " + token);
            }

            return chain.proceed(builder.build());
        };

        OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .addInterceptor(apiInterceptor)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS);

        // --- KONFIGURACJA HTTPS DLA LOKALNEGO DEBUGOWANIA (NIEBEZPIECZNE W PRODUKCJI!) ---
        // Ten kod jest aktywowany TYLKO w buildach debugowych. Pozwala na łączenie się z serwerami
        // z samopodpisanymi certyfikatami (np. lokalne API HTTPS).
        // W produkcyjnych buildach, OkHttpClient będzie ufał tylko publicznie zaufanym certyfikatom,
        // co jest prawidłowym i bezpiecznym zachowaniem.
        if (BuildConfig.DEBUG) {
            try {
                // Utwórz TrustManager, który akceptuje wszystkie certyfikaty
                final TrustManager[] trustAllCerts = new TrustManager[]{
                        new X509TrustManager() {
                            @Override
                            public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                                // Pozostaw puste, aby ufać wszystkim certyfikatom klienta
                            }

                            @Override
                            public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                                // Pozostaw puste, aby ufać wszystkim certyfikatom serwera
                            }

                            @Override
                            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                                return new X509Certificate[0]; // Zwróć pustą tablicę, aby nie wymagać konkretnych CA
                            }
                        }
                };

                // Zainstaluj TrustManager
                final SSLContext sslContext = SSLContext.getInstance("SSL");
                sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

                // Ustaw SSLSocketFactory i HostnameVerifier, aby akceptować wszystkie połączenia
                okHttpClientBuilder.sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) trustAllCerts[0]);
                okHttpClientBuilder.hostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        return true; // Zezwól na wszystkie hosty (NIEBEZPIECZNE W PRODUKCJI!)
                    }
                });

            } catch (NoSuchAlgorithmException | KeyManagementException e) {
                // Zaloguj błąd konfiguracji SSL, ale nie crashuj aplikacji w debugu
                e.printStackTrace();
            }
        }
        // --- KONIEC KONFIGURACJI DLA DEBUGOWANIA ---

        return okHttpClientBuilder.build();
    }

    @Provides
    @Singleton
    Retrofit provideRetrofit(Gson gson, OkHttpClient okHttpClient) {
        return new Retrofit.Builder()
                .baseUrl(ApiConfig.BASE_URL) // Używa stałego URL z ApiConfig
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
    }

    @Provides
    @Singleton
    ApiService provideApiService(Retrofit retrofit) {
        return retrofit.create(ApiService.class);
    }
}
