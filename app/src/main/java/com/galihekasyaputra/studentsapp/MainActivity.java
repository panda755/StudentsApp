package com.galihekasyaputra.studentsapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.galihekasyaputra.studentsapp.model.Student;
import com.galihekasyaputra.studentsapp.service.ApiClient;
import com.galihekasyaputra.studentsapp.service.ApiService;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private ListView listViewStudents;
    private FloatingActionButton fabAdd;
    private ApiService apiService;

    private List<Student> studentList = new ArrayList<>();
    private StudentAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Cek token — jika belum login, arahkan ke LoginActivity
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        if (prefs.getString("token", null) == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        listViewStudents = findViewById(R.id.listViewStudents);
        fabAdd = findViewById(R.id.fabAdd);

        apiService = ApiClient.getService();

        adapter = new StudentAdapter(this, studentList);
        listViewStudents.setAdapter(adapter);

        loadStudents();

        fabAdd.setOnClickListener(v -> showStudentDialog(null));

        listViewStudents.setOnItemClickListener(
                (parent, view, position, id) ->
                        showOptionDialog(studentList.get(position))
        );
    }

    // --- READ ALL ---

    private void loadStudents() {

        apiService.getStudents().enqueue(new Callback<List<Student>>() {

            @Override
            public void onResponse(
                    Call<List<Student>> call,
                    Response<List<Student>> response
            ) {
                if (response.isSuccessful() && response.body() != null) {

                    studentList.clear();
                    studentList.addAll(response.body());
                    adapter.notifyDataSetChanged();

                } else {
                    Toast.makeText(MainActivity.this, "Gagal memuat data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Student>> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Error Network: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // --- DIALOG PILIHAN ---

    private void showOptionDialog(Student student) {

        String[] options = { "Ubah Data", "Hapus Data" };

        new AlertDialog.Builder(this)
                .setTitle("Pilih Aksi (" + student.getName() + ")")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        showStudentDialog(student);
                    } else {
                        showDeleteConfirmation(student);
                    }
                })
                .show();
    }

    // --- CREATE & UPDATE ---

    private void showStudentDialog(Student studentToEdit) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(studentToEdit == null ? "Tambah Student Baru" : "Ubah Data Student");

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_student, null);

        EditText etNis     = view.findViewById(R.id.etNis);
        EditText etName    = view.findViewById(R.id.etName);
        EditText etAddress = view.findViewById(R.id.etAddress);

        if (studentToEdit != null) {
            etNis.setText(studentToEdit.getNis());
            etName.setText(studentToEdit.getName());
            etAddress.setText(studentToEdit.getAddress());
        }

        builder.setView(view);

        builder.setPositiveButton("Simpan", (dialog, which) -> {

            String nis     = etNis.getText().toString().trim();
            String name    = etName.getText().toString().trim();
            String address = etAddress.getText().toString().trim();

            if (nis.isEmpty() || name.isEmpty() || address.isEmpty()) {
                Toast.makeText(this, "Semua field harus diisi!", Toast.LENGTH_SHORT).show();
                return;
            }

            Student studentData = new Student(nis, name, address);

            if (studentToEdit == null) {
                createStudentApi(studentData);
            } else {
                updateStudentApi(studentToEdit.getId(), studentData);
            }
        });

        builder.setNegativeButton("Batal", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void createStudentApi(Student student) {

        apiService.createStudent(student).enqueue(new Callback<Student>() {

            @Override
            public void onResponse(Call<Student> call, Response<Student> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(MainActivity.this, "Berhasil menambah data", Toast.LENGTH_SHORT).show();
                    loadStudents();
                }
            }

            @Override
            public void onFailure(Call<Student> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Gagal Tambah: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateStudentApi(int id, Student student) {

        apiService.updateStudent(id, student).enqueue(new Callback<Student>() {

            @Override
            public void onResponse(Call<Student> call, Response<Student> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(MainActivity.this, "Berhasil mengupdate data", Toast.LENGTH_SHORT).show();
                    loadStudents();
                }
            }

            @Override
            public void onFailure(Call<Student> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Gagal Update: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // --- DELETE ---

    private void showDeleteConfirmation(Student student) {

        new AlertDialog.Builder(this)
                .setTitle("Konfirmasi Hapus")
                .setMessage("Apakah Anda yakin ingin menghapus " + student.getName() + "?")
                .setPositiveButton("Hapus", (dialog, which) -> deleteStudentApi(student.getId()))
                .setNegativeButton("Batal", null)
                .show();
    }

    private void deleteStudentApi(int id) {

        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        String token = "Bearer " + prefs.getString("token", "");

        apiService.deleteStudent(token, id).enqueue(new Callback<Void>() {

            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {

                if (response.isSuccessful()) {
                    Toast.makeText(MainActivity.this, "Data berhasil dihapus", Toast.LENGTH_SHORT).show();
                    loadStudents();

                } else if (response.code() == 401) {
                    Toast.makeText(MainActivity.this, "Sesi berakhir, silakan login ulang", Toast.LENGTH_SHORT).show();
                    getSharedPreferences("app_prefs", MODE_PRIVATE).edit().remove("token").apply();
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    finish();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Gagal Hapus: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}