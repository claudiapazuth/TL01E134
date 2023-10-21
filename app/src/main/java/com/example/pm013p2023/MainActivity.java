package com.example.pm013p2023;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.pm013p2023.configuracion.SQLiteConexion;
import com.example.pm013p2023.configuracion.Transacciones;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    EditText nombres, apellidos, edad, correo;
    Button btnprocesar, btnprocesar2;

    //Fotos

    static final int peticion_acceso_camara = 101;
    static final int peticion_toma_fotografica = 102;
    String currentPhotoPath;
    ImageView imageView2;
    Button btntakefoto2;

    String pathfoto;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        nombres = (EditText) findViewById(R.id.txtnombre);
        apellidos = (EditText) findViewById(R.id.txtapellidos);
        edad = (EditText) findViewById(R.id.txtedad);
        correo = (EditText) findViewById(R.id.txtcorreo);
        imageView2 = (ImageView) findViewById(R.id.imageView2);

        btntakefoto2 = (Button) findViewById(R.id.btntakefoto2);
        btnprocesar = (Button) findViewById(R.id.btnprocesar);
        btnprocesar2 = (Button) findViewById(R.id.btnprocesar2);

        btntakefoto2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                permisos();

              camaraLaucher.launch(new Intent(MediaStore.ACTION_IMAGE_CAPTURE));
            }
       ActivityResultLauncher<Intent> camaraLaucher = registerForActivityResult(new
               ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
           @Override
           public void onActivityResult(ActivityResult result) {
               if (result.getResultCode()== RESULT_OK){
                   Bundle extras = result.getData().getExtras();
                   Bitmap imgBitmap = (Bitmap) extras.get("data");
                   imageView2.setImageBitmap(imgBitmap);
               }
           }
       });

        });


        btnprocesar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validar()) {
                    AddPerson();
                }
            }
        });

        btnprocesar2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intentsalvados = new Intent(getApplicationContext(), ActivityList.class);

                startActivity(intentsalvados);

            }
        });
    }


    private void AddPerson() {
        try {
            SQLiteConexion conexion = new SQLiteConexion(this, Transacciones.namedb, null, 1);
            SQLiteDatabase db = conexion.getWritableDatabase();

            ContentValues valores = new ContentValues();
            valores.put(Transacciones.nombres, nombres.getText().toString());
            valores.put(Transacciones.apellidos, apellidos.getText().toString());
            valores.put(Transacciones.edad, edad.getText().toString());
            valores.put(Transacciones.correo, correo.getText().toString());

            Long Result = db.insert(Transacciones.Tabla, Transacciones.id, valores);

            Toast.makeText(this, getString(R.string.Respuesta), Toast.LENGTH_SHORT).show();
            db.close();
        } catch (Exception exception) {
            Toast.makeText(this, getString(R.string.ErrorResp), Toast.LENGTH_SHORT).show();
        }

    }


    private void permisos()
    {
        if(ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},peticion_acceso_camara);
        }
        else
        {
            //TomarFoto();
            dispatchTakePictureIntent();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == peticion_acceso_camara)
        {
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                //TomarFoto();
                dispatchTakePictureIntent();
            }
            else
            {
                Toast.makeText(getApplicationContext(),"Permiso denegado", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void TomarFoto()
    {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(intent.resolveActivity(getPackageManager())!= null)
        {
            startActivityForResult(intent, peticion_toma_fotografica);
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                  storageDir      /* directory */
         );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }


    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File

            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.pm013p2023.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, peticion_toma_fotografica);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == peticion_toma_fotografica && resultCode == RESULT_OK)
        {
            /*
            Bundle extras = data.getExtras();
            Bitmap image = (Bitmap) extras.get("data");
            imageView.setImageBitmap(image);
             */

             try
            {
                File foto = new File(currentPhotoPath);
                imageView2.setImageURI(Uri.fromFile(foto));
            }
            catch (Exception ex)
            {
                ex.toString();
            }
        }

    }


    public boolean validar()

    {
        boolean retorno=true;
        String pa= nombres.getText().toString();
        String no= apellidos.getText().toString();
        String ce= edad.getText().toString();
        String not= correo.getText().toString();
        if (pa.isEmpty()){

            Toast.makeText(this, "Llene el campo vacio", Toast.LENGTH_SHORT).show();
            retorno=false;
        }

        if (no.isEmpty()){
            Toast.makeText(this, "Llene el campo vacio", Toast.LENGTH_SHORT).show();
            retorno=false;
        }


        if (ce.isEmpty()){
            Toast.makeText(this, "Llene el campo vacio", Toast.LENGTH_SHORT).show();
            retorno=false;
        }

        if (not.isEmpty()){
            Toast.makeText(this, "Llene el campo vacio", Toast.LENGTH_SHORT).show();
            retorno=false;
        }

        return retorno;

    }





}