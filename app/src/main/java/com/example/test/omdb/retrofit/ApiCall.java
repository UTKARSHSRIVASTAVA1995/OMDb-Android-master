package com.example.test.omdb.retrofit;

import com.example.test.omdb.models.MovieModel;
import com.example.test.omdb.models.moviePoster.MovieSearchList;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;

public interface ApiCall {

   String BASE_URL = "http://www.omdbapi.com/?";
   // String BASE_URL = "http://www.omdbapi.com";

    @GET("/")
    Call<MovieSearchList> search(@Query("apikey") String apikey, @Query("t") String query, @Query("type") String type, @Query("page") int page);

    @Headers("Content-Type: application/json")
    @GET("/")
    Call<MovieSearchList> searchMovie(@Query("apikey") String apikey, @Query("s") String query, @Query("page") String page);

    @GET("/")
    Call<MovieModel> getMovie(@Query("i") String imdbId);

    class Factory {
        public static ApiCall service;

        public static ApiCall getInstance() {
            if (service == null) {
//                Gson gson = new GsonBuilder()
//                        .setLenient()
//                        .create();

                Retrofit retrofit = new Retrofit.Builder()
                        .addConverterFactory(GsonConverterFactory.create())
                        .baseUrl(BASE_URL).build();
                service = retrofit.create(ApiCall.class);
                return service;
            } else {
                return service;
            }
        }
    }
}
