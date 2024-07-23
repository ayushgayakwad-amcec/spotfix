package com.ambition.spotfix;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.storage.UploadTask;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

public class Report extends AppCompatActivity {

    private FirebaseDatabase _firebase = FirebaseDatabase.getInstance();

    private HashMap<String, Object> map = new HashMap<>();
    private static final int PICK_IMAGE_REQUEST = 1;
    private String imagestate = "";
    private String timestamp = "";
    private String DownloadUri = "";
    private TextView progresst;
    private LinearLayout linear2;
    private LinearLayout linear3;
    private LinearLayout linear5;
    private TextView textview1;
    private LinearLayout linear7;
    private LinearLayout linear9;
    private LinearLayout linear8;
    private LinearLayout linear10;
    private LinearLayout linear11;
    private ProgressBar p1;
    private LinearLayout departmentlay;
    private TextView department;
    private EditText desc;
    private Button button1;
    private ImageView pick;
    private TextView success;
    private Intent INTENT = new Intent();
    private SharedPreferences userdata;
    private StorageReference storageRef;
    private DatabaseReference water = _firebase.getReference("water");
    private ChildEventListener _water_child_listener;

    private DatabaseReference electricity = _firebase.getReference("electricity");
    private ChildEventListener _electricity_child_listener;

    private DatabaseReference construction = _firebase.getReference("construction");
    private ChildEventListener _construction_child_listener;

    private DatabaseReference sanitation = _firebase.getReference("sanitation");
    private ChildEventListener _sanitation_child_listener;

    @Override
    protected void onCreate(Bundle _savedInstanceState) {
        super.onCreate(_savedInstanceState);
        setContentView(R.layout.activity_report);
        initialize(_savedInstanceState);
        FirebaseApp.initializeApp(this);
        storageRef = FirebaseStorage.getInstance().getReference();
        initializeLogic();
    }

    private void initialize(Bundle _savedInstanceState) {
        linear2 = findViewById(R.id.linear2);
        linear3 = findViewById(R.id.linear3);
        linear5 = findViewById(R.id.linear5);
        linear7 = findViewById(R.id.linear7);
        linear9 = findViewById(R.id.linear9);
        linear8 = findViewById(R.id.linear8);
        linear10 = findViewById(R.id.linear10);
        linear11 = findViewById(R.id.linear11);
        pick = findViewById(R.id.pick);
        progresst = findViewById(R.id.progresst);
        desc = findViewById(R.id.desc);
        success = findViewById(R.id.success);
        p1 = findViewById(R.id.progressbar1);
        departmentlay = findViewById(R.id.departmentlay);
        department = findViewById(R.id.department);
        button1 = findViewById(R.id.button1);

        userdata = getSharedPreferences("userdata", Activity.MODE_PRIVATE);

        pick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkStoragePermission()) {
                    pickImage();
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        requestStoragePermission();
                    }
                }
            }
        });

        departmentlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popu = new PopupMenu(Report.this, departmentlay);
                Menu departmentlay = popu.getMenu();
                departmentlay.add("Water Department");
                departmentlay.add("Electricity Department");
                departmentlay.add("Construction Department");
                departmentlay.add("Sanitation Department");
                popu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getTitle().toString()) {
                            case "Water Department":
                                department.setText("Water Department");
                                return true;
                            case "Electricity Department":
                                department.setText("Electricity Department");
                                return true;
                            case "Construction Department":
                                department.setText("Construction Department");
                                return true;
                            case "Sanitation Department":
                                department.setText("Sanitation Department");
                                return true;
                            default:
                                return false;
                        }
                    }
                });
                try {
                    java.lang.reflect.Field[] fields = popu.getClass().getDeclaredFields();
                    for (java.lang.reflect.Field field : fields) {
                        if ("mPopup".equals(field.getName())) {
                            field.setAccessible(true);
                            Object menuPopupHelper = field.get(popu); Class<?> classPopupHelper = Class.forName(menuPopupHelper.getClass().getName());
                            java.lang.reflect.Method setForceIcons = classPopupHelper.getMethod("setForceShowIcon", boolean.class); setForceIcons.invoke(menuPopupHelper, true);
                            break;
                        }
                    }
                } catch(Exception e) {
                    e.printStackTrace();
                }
                popu.show();
            }
        });

        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View _view) {

                if (desc.getText().toString().trim().equals("")) {
                    Toast.makeText(getApplicationContext(), "Please enter your description...", Toast.LENGTH_SHORT).show();
                }
                else {
                    if (department.getText().toString().trim().equals("Choose an option")) {
                        Toast.makeText(getApplicationContext(), "Please choose a department...", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        if (DownloadUri.equals("")) {
                            Toast.makeText(getApplicationContext(), "Please choose an image...", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            if (department.getText().toString().trim().equals("Water Department")) {
                                timestamp = getCurrentTimestamp();
                                map = new HashMap<>();
                                map.put("name", userdata.getString("name", ""));
                                map.put("desc", desc.getText().toString().trim());
                                map.put("cn", userdata.getString("cn", ""));
                                map.put("latitude", userdata.getString("latitude", ""));
                                map.put("longitude", userdata.getString("longitude", ""));
                                map.put("image", DownloadUri);
                                map.put("status", "unsolved");
                                map.put("reported_time", timestamp);
                                map.put("email", FirebaseAuth.getInstance().getCurrentUser().getEmail());
                                map.put("uid", FirebaseAuth.getInstance().getCurrentUser().getUid());
                                water.push().updateChildren(map);
                                map.clear();
                                department.setText("Choose an option");
                                desc.setText("");
                                Toast.makeText(getApplicationContext(), "Report submitted!", Toast.LENGTH_SHORT).show();
                            } else {
                                if (department.getText().toString().trim().equals("Electricity Department")) {
                                    timestamp = getCurrentTimestamp();
                                    map = new HashMap<>();
                                    map.put("name", userdata.getString("name", ""));
                                    map.put("desc", desc.getText().toString().trim());
                                    map.put("cn", userdata.getString("cn", ""));
                                    map.put("latitude", userdata.getString("latitude", ""));
                                    map.put("longitude", userdata.getString("longitude", ""));
                                    map.put("image", DownloadUri);
                                    map.put("status", "unsolved");
                                    map.put("reported_time", timestamp);
                                    map.put("email", FirebaseAuth.getInstance().getCurrentUser().getEmail());
                                    map.put("uid", FirebaseAuth.getInstance().getCurrentUser().getUid());
                                    electricity.push().updateChildren(map);
                                    map.clear();
                                    department.setText("Choose an option");
                                    desc.setText("");
                                    Toast.makeText(getApplicationContext(), "Report submitted!", Toast.LENGTH_SHORT).show();
                                } else {
                                    if (department.getText().toString().trim().equals("Construction Department")) {
                                        timestamp = getCurrentTimestamp();
                                        map = new HashMap<>();
                                        map.put("name", userdata.getString("name", ""));
                                        map.put("desc", desc.getText().toString().trim());
                                        map.put("cn", userdata.getString("cn", ""));
                                        map.put("latitude", userdata.getString("latitude", ""));
                                        map.put("longitude", userdata.getString("longitude", ""));
                                        map.put("image", DownloadUri);
                                        map.put("status", "unsolved");
                                        map.put("reported_time", timestamp);
                                        map.put("email", FirebaseAuth.getInstance().getCurrentUser().getEmail());
                                        map.put("uid", FirebaseAuth.getInstance().getCurrentUser().getUid());
                                        construction.push().updateChildren(map);
                                        map.clear();
                                        department.setText("Choose an option");
                                        desc.setText("");
                                        Toast.makeText(getApplicationContext(), "Report submitted!", Toast.LENGTH_SHORT).show();
                                    } else {
                                        if (department.getText().toString().trim().equals("Sanitation Department")) {
                                            timestamp = getCurrentTimestamp();
                                            map = new HashMap<>();
                                            map.put("name", userdata.getString("name", ""));
                                            map.put("desc", desc.getText().toString().trim());
                                            map.put("cn", userdata.getString("cn", ""));
                                            map.put("latitude", userdata.getString("latitude", ""));
                                            map.put("longitude", userdata.getString("longitude", ""));
                                            map.put("image", DownloadUri);
                                            map.put("status", "unsolved");
                                            map.put("reported_time", timestamp);
                                            map.put("email", FirebaseAuth.getInstance().getCurrentUser().getEmail());
                                            map.put("uid", FirebaseAuth.getInstance().getCurrentUser().getUid());
                                            sanitation.push().updateChildren(map);
                                            map.clear();
                                            department.setText("Choose an option");
                                            desc.setText("");
                                            Toast.makeText(getApplicationContext(), "Report submitted!", Toast.LENGTH_SHORT).show();
                                        } else {

                                        }
                                    }
                                }
                            }
                        }
                    }
                }

            }
        });

        _water_child_listener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot _param1, String _param2) {
                GenericTypeIndicator<HashMap<String, Object>> _ind = new GenericTypeIndicator<HashMap<String, Object>>() {};
                final String _childKey = _param1.getKey();
                final HashMap<String, Object> _childValue = _param1.getValue(_ind);

            }

            @Override
            public void onChildChanged(DataSnapshot _param1, String _param2) {
                GenericTypeIndicator<HashMap<String, Object>> _ind = new GenericTypeIndicator<HashMap<String, Object>>() {};
                final String _childKey = _param1.getKey();
                final HashMap<String, Object> _childValue = _param1.getValue(_ind);

            }

            @Override
            public void onChildMoved(DataSnapshot _param1, String _param2) {

            }

            @Override
            public void onChildRemoved(DataSnapshot _param1) {
                GenericTypeIndicator<HashMap<String, Object>> _ind = new GenericTypeIndicator<HashMap<String, Object>>() {};
                final String _childKey = _param1.getKey();
                final HashMap<String, Object> _childValue = _param1.getValue(_ind);

            }

            @Override
            public void onCancelled(DatabaseError _param1) {
                final int _errorCode = _param1.getCode();
                final String _errorMessage = _param1.getMessage();

            }
        };
        water.addChildEventListener(_water_child_listener);

        _electricity_child_listener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot _param1, String _param2) {
                GenericTypeIndicator<HashMap<String, Object>> _ind = new GenericTypeIndicator<HashMap<String, Object>>() {};
                final String _childKey = _param1.getKey();
                final HashMap<String, Object> _childValue = _param1.getValue(_ind);

            }

            @Override
            public void onChildChanged(DataSnapshot _param1, String _param2) {
                GenericTypeIndicator<HashMap<String, Object>> _ind = new GenericTypeIndicator<HashMap<String, Object>>() {};
                final String _childKey = _param1.getKey();
                final HashMap<String, Object> _childValue = _param1.getValue(_ind);

            }

            @Override
            public void onChildMoved(DataSnapshot _param1, String _param2) {

            }

            @Override
            public void onChildRemoved(DataSnapshot _param1) {
                GenericTypeIndicator<HashMap<String, Object>> _ind = new GenericTypeIndicator<HashMap<String, Object>>() {};
                final String _childKey = _param1.getKey();
                final HashMap<String, Object> _childValue = _param1.getValue(_ind);

            }

            @Override
            public void onCancelled(DatabaseError _param1) {
                final int _errorCode = _param1.getCode();
                final String _errorMessage = _param1.getMessage();

            }
        };
        electricity.addChildEventListener(_electricity_child_listener);

        _construction_child_listener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot _param1, String _param2) {
                GenericTypeIndicator<HashMap<String, Object>> _ind = new GenericTypeIndicator<HashMap<String, Object>>() {};
                final String _childKey = _param1.getKey();
                final HashMap<String, Object> _childValue = _param1.getValue(_ind);

            }

            @Override
            public void onChildChanged(DataSnapshot _param1, String _param2) {
                GenericTypeIndicator<HashMap<String, Object>> _ind = new GenericTypeIndicator<HashMap<String, Object>>() {};
                final String _childKey = _param1.getKey();
                final HashMap<String, Object> _childValue = _param1.getValue(_ind);

            }

            @Override
            public void onChildMoved(DataSnapshot _param1, String _param2) {

            }

            @Override
            public void onChildRemoved(DataSnapshot _param1) {
                GenericTypeIndicator<HashMap<String, Object>> _ind = new GenericTypeIndicator<HashMap<String, Object>>() {};
                final String _childKey = _param1.getKey();
                final HashMap<String, Object> _childValue = _param1.getValue(_ind);

            }

            @Override
            public void onCancelled(DatabaseError _param1) {
                final int _errorCode = _param1.getCode();
                final String _errorMessage = _param1.getMessage();

            }
        };
        construction.addChildEventListener(_construction_child_listener);

        _sanitation_child_listener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot _param1, String _param2) {
                GenericTypeIndicator<HashMap<String, Object>> _ind = new GenericTypeIndicator<HashMap<String, Object>>() {};
                final String _childKey = _param1.getKey();
                final HashMap<String, Object> _childValue = _param1.getValue(_ind);

            }

            @Override
            public void onChildChanged(DataSnapshot _param1, String _param2) {
                GenericTypeIndicator<HashMap<String, Object>> _ind = new GenericTypeIndicator<HashMap<String, Object>>() {};
                final String _childKey = _param1.getKey();
                final HashMap<String, Object> _childValue = _param1.getValue(_ind);

            }

            @Override
            public void onChildMoved(DataSnapshot _param1, String _param2) {

            }

            @Override
            public void onChildRemoved(DataSnapshot _param1) {
                GenericTypeIndicator<HashMap<String, Object>> _ind = new GenericTypeIndicator<HashMap<String, Object>>() {};
                final String _childKey = _param1.getKey();
                final HashMap<String, Object> _childValue = _param1.getValue(_ind);

            }

            @Override
            public void onCancelled(DatabaseError _param1) {
                final int _errorCode = _param1.getCode();
                final String _errorMessage = _param1.getMessage();

            }
        };
        sanitation.addChildEventListener(_sanitation_child_listener);
    }

    private void initializeLogic() {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        getWindow().setStatusBarColor(0xFFE7EEFB);
        Window window = this.getWindow();
        window.setNavigationBarColor(Color.parseColor("#1944f1"));
        p1.setVisibility(View.GONE);
        pick.setVisibility(View.VISIBLE);
        success.setVisibility(View.GONE);
        progresst.setVisibility(View.GONE);
        button1.setEnabled(false);
        _rippleRoundStroke(button1, "#0026c0", "#f5f5f5", 100, 2, "#eeeeee");
        desc.setBackground(new GradientDrawable() { public GradientDrawable getIns(int a, int b, int c, int d) { this.setCornerRadius(a); this.setStroke(b, c); this.setColor(d); return this; }}.getIns((int)50, (int)0, 0xFFFFFFFF, 0xFFEEEEEE));
    }

    // Method to check storage permission
    private boolean checkStoragePermission() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
            || ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    // Method to request storage permission
    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    private void requestStoragePermission() {
        requestPermissions(new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.READ_MEDIA_IMAGES
        }, PICK_IMAGE_REQUEST); // Use the same code as for image selection request
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PICK_IMAGE_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with picking image
                pickImage();
            } else {
                // Permission denied, inform user
                Toast.makeText(this, "Storage permission is required to select an image.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST); // Replace PICK_IMAGE_REQUEST with a unique code
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK) {
            if (data != null && data.getData() != null) {
                // Get the selected image URI
                Uri imageUri = data.getData();

                // Upload the image to Firebase Storage
                uploadImage(imageUri);
                p1.setVisibility(View.VISIBLE);
                pick.setVisibility(View.GONE);
                success.setVisibility(View.GONE);
                progresst.setVisibility(View.VISIBLE);
            }
        }
    }

    private void uploadImage(Uri imageUri) {
        // Create a reference for the image in Firebase Storage
        final StorageReference imageRef = storageRef.child("images/" + UUID.randomUUID().toString());

        // Upload the image
        imageRef.putFile(imageUri)
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot snapshot) {
                        double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                        String progressText = String.format("%.2f%%", progress);
                        progresst.setText(progressText);
                    }
                })
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Image uploaded successfully
                        // Get the download URL
                        imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri downloadUri) {
                                String imageUrl = downloadUri.toString();
                                Toast.makeText(getApplicationContext(), "Image uploaded!", Toast.LENGTH_SHORT).show();
                                DownloadUri = downloadUri.toString();
                                button1.setEnabled(true);
                                p1.setVisibility(View.GONE);
                                pick.setVisibility(View.GONE);
                                success.setVisibility(View.VISIBLE);
                                progresst.setVisibility(View.GONE);
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle upload failure
                        Toast.makeText(getApplicationContext(), "Image upload failed!", Toast.LENGTH_SHORT).show();
                        DownloadUri = "";
                        button1.setEnabled(false);
                        p1.setVisibility(View.GONE);
                        pick.setVisibility(View.VISIBLE);
                        success.setVisibility(View.GONE);
                        progresst.setVisibility(View.GONE);
                    }
                });
    }

    public static String getCurrentTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date now = new Date();
        return sdf.format(now);
    }

    public void _rippleRoundStroke(final View _view, final String _focus, final String _pressed, final double _round, final double _stroke, final String _strokeclr) {
        GradientDrawable GG = new GradientDrawable();
        GG.setColor(Color.parseColor(_focus));
        GG.setCornerRadius((float)_round);
        GG.setStroke((int) _stroke,
                Color.parseColor("#" + _strokeclr.replace("#", "")));
        android.graphics.drawable.RippleDrawable RE = new android.graphics.drawable.RippleDrawable(new android.content.res.ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor(_pressed)}), GG, null);
        _view.setBackground(RE);
    }
}
