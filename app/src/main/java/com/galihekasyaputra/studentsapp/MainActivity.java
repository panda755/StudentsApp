package com.galihekasyaputra.studentsapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
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

    private ArrayAdapter<String> adapter;
    private List<String> studentDisplayList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listViewStudents = findViewById(R.id.listViewStudents);
        fabAdd = findViewById(R.id.fabAdd);

        apiService = ApiClient.getService();

        adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                studentDisplayList
        );

        listViewStudents.setAdapter(adapter);

        // a. Mendapatkan seluruh data saat aplikasi dibuka
        loadStudents();

        // b. Tambah data via FAB
        fabAdd.setOnClickListener(v -> showStudentDialog(null));

        // Click Item: Pilih Opsi Ubah (c) atau Hapus (d)
        listViewStudents.setOnItemClickListener(
                (parent, view, position, id) -> {

                    Student selectedStudent = studentList.get(position);

                    showOptionDialog(selectedStudent);
                }
        );
    }

    // --- (a) READ ALL DATA ---

    private void loadStudents() {

        apiService.getStudents().enqueue(new Callback<List<Student>>() {

            @Override
            public void onResponse(
                    Call<List<Student>> call,
                    Response<List<Student>> response
            ) {

                if (response.isSuccessful() && response.body() != null) {

                    studentList = response.body();

                    studentDisplayList.clear();

                    for (Student s : studentList) {

                        studentDisplayList.add(
                                s.getNis() + " - " + s.getName()
                                        + "\n" + s.getAddress()
                        );
                    }

                    adapter.notifyDataSetChanged();

                } else {

                    Toast.makeText(
                            MainActivity.this,
                            "Gagal memuat data",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }

            @Override
            public void onFailure(
                    Call<List<Student>> call,
                    Throwable t
            ) {

                Toast.makeText(
                        MainActivity.this,
                        "Error Network: " + t.getMessage(),
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }

    // Dialog Pilihan ketika Item ListView Ditekan

    private void showOptionDialog(Student student) {

        String[] options = {
                "Ubah Data",
                "Hapus Data"
        };

        AlertDialog.Builder builder =
                new AlertDialog.Builder(this);

        builder.setTitle(
                "Pilih Aksi (" + student.getName() + ")"
        );

        builder.setItems(options, (dialog, which) -> {

            if (which == 0) {

                // c. Operasi Ubah Data
                showStudentDialog(student);

            } else if (which == 1) {

                // d. Operasi Hapus Data
                showDeleteConfirmation(student);
            }
        });

        builder.show();
    }

    // --- (b & c) DIALOG FORM CREATE & UPDATE ---

    private void showStudentDialog(Student studentToEdit) {

        AlertDialog.Builder builder =
                new AlertDialog.Builder(this);

        builder.setTitle(
                studentToEdit == null
                        ? "Tambah Student Baru"
                        : "Ubah Data Student"
        );

        View view = LayoutInflater
                .from(this)
                .inflate(R.layout.dialog_student, null);

        EditText etNis = view.findViewById(R.id.etNis);
        EditText etName = view.findViewById(R.id.etName);
        EditText etAddress = view.findViewById(R.id.etAddress);

        if (studentToEdit != null) {

            etNis.setText(studentToEdit.getNis());
            etName.setText(studentToEdit.getName());
            etAddress.setText(studentToEdit.getAddress());
        }

        builder.setView(view);

        builder.setPositiveButton(
                "Simpan",
                (dialog, which) -> {

                    String nis = etNis
                            .getText()
                            .toString()
                            .trim();

                    String name = etName
                            .getText()
                            .toString()
                            .trim();

                    String address = etAddress
                            .getText()
                            .toString()
                            .trim();

                    if (nis.isEmpty()
                            || name.isEmpty()
                            || address.isEmpty()) {

                        Toast.makeText(
                                this,
                                "Semua field harus diisi!",
                                Toast.LENGTH_SHORT
                        ).show();

                        return;
                    }

                    Student studentData =
                            new Student(nis, name, address);

                    if (studentToEdit == null) {

                        // Execute POST
                        createStudentApi(studentData);

                    } else {

                        // Execute PUT
                        updateStudentApi(
                                studentToEdit.getId(),
                                studentData
                        );
                    }
                }
        );

        builder.setNegativeButton(
                "Batal",
                (dialog, which) -> dialog.dismiss()
        );

        builder.show();
    }

    private void createStudentApi(Student student) {

        apiService
                .createStudent(student)
                .enqueue(new Callback<Student>() {

                    @Override
                    public void onResponse(
                            Call<Student> call,
                            Response<Student> response
                    ) {

                        if (response.isSuccessful()) {

                            Toast.makeText(
                                    MainActivity.this,
                                    "Berhasil menambah data",
                                    Toast.LENGTH_SHORT
                            ).show();

                            loadStudents();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<Student> call,
                            Throwable t
                    ) {

                        Toast.makeText(
                                MainActivity.this,
                                "Gagal Tambah: " + t.getMessage(),
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }

    private void updateStudentApi(
            int id,
            Student student
    ) {

        apiService
                .updateStudent(id, student)
                .enqueue(new Callback<Student>() {

                    @Override
                    public void onResponse(
                            Call<Student> call,
                            Response<Student> response
                    ) {

                        if (response.isSuccessful()) {

                            Toast.makeText(
                                    MainActivity.this,
                                    "Berhasil mengupdate data",
                                    Toast.LENGTH_SHORT
                            ).show();

                            loadStudents();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<Student> call,
                            Throwable t
                    ) {

                        Toast.makeText(
                                MainActivity.this,
                                "Gagal Update: " + t.getMessage(),
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }

    // --- (d) DELETE DATA WITH CONFIRMATION DIALOG ---

    private void showDeleteConfirmation(Student student) {

        new AlertDialog.Builder(this)

                .setTitle("Konfirmasi Hapus")

                .setMessage(
                        "Apakah Anda yakin ingin menghapus "
                                + student.getName() + "?"
                )

                .setPositiveButton(
                        "Hapus",
                        (dialog, which) ->
                                deleteStudentApi(student.getId())
                )

                .setNegativeButton("Batal", null)

                .show();
    }

    private void deleteStudentApi(int id) {

        apiService
                .deleteStudent(id)
                .enqueue(new Callback<Void>() {

                    @Override
                    public void onResponse(
                            Call<Void> call,
                            Response<Void> response
                    ) {

                        if (response.isSuccessful()) {

                            Toast.makeText(
                                    MainActivity.this,
                                    "Data berhasil dihapus",
                                    Toast.LENGTH_SHORT
                            ).show();

                            loadStudents();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<Void> call,
                            Throwable t
                    ) {

                        Toast.makeText(
                                MainActivity.this,
                                "Gagal Hapus: " + t.getMessage(),
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }
}