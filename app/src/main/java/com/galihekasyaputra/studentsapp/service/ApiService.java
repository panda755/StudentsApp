package com.galihekasyaputra.studentsapp.service;

import com.galihekasyaputra.studentsapp.model.LoginRequest;
import com.galihekasyaputra.studentsapp.model.LoginResponse;
import com.galihekasyaputra.studentsapp.model.Student;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ApiService {

    @POST("login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @GET("students")
    Call<List<Student>> getStudents();

    @POST("students")
    Call<Student> createStudent(@Body Student student);

    @PUT("students/{id}")
    Call<Student> updateStudent(
            @Path("id") int id,
            @Body Student student
    );

    @DELETE("students/{id}")
    Call<Void> deleteStudent(
            @Header("Authorization") String token,
            @Path("id") int id
    );
}