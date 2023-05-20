package com.example.mesquitaapp.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.mesquitaapp.R;
import com.example.mesquitaapp.config.ConfiguracaoFirebase;
import com.example.mesquitaapp.config.Permissao;
import com.example.mesquitaapp.config.Util;
import com.example.mesquitaapp.model.Usuario;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

import de.hdodenhof.circleimageview.CircleImageView;

public class ConfiguracoesActivity extends AppCompatActivity {
    private EditText nome;
    private EditText endereco;
    private CircleImageView fotoPerfil;

    private StorageReference storageReference;

    private Usuario usr;

    private static final int SELECAO_CAMERA = 100;
    private static final int SELECAO_GALERIA = 200;

    private String[] permissoes = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracoes);

        getSupportActionBar().setTitle("Configurações");

        storageReference = ConfiguracaoFirebase.getFirebaseStorage();

        //Validar permissões
        Permissao.validarPermissoes(permissoes, this, 1);

        nome = findViewById(R.id.etNome);
        endereco = findViewById(R.id.etEndereco);
        fotoPerfil = findViewById(R.id.civFotoPerfil);

        //Pega o usuário que veio por parâmetro na chamada
        usr = (Usuario) getIntent().getSerializableExtra("usr");
        if (usr != null){
            nome.setText(usr.getNome());
            endereco.setText(usr.getEndereco());

            //Recupera a foto do usuário
            StorageReference imagemRef = storageReference
                    .child("imagens")
                    .child(usr.getId() + ".jpeg");
            final long ONE_MEGABYTE = 1024 * 1024;
            imagemRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    fotoPerfil.setImageBitmap(Bitmap.createScaledBitmap(bmp, fotoPerfil.getWidth(),
                            fotoPerfil.getHeight(), false));
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    fotoPerfil.setImageResource(R.drawable.ic_foto_cinza_24dp);
                }
            });
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        for (int permissaoResultado : grantResults){
            if (permissaoResultado == PackageManager.PERMISSION_DENIED){
                alertaValidacaoPermissao();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK){
            Bitmap imagem = null;

            try {
                switch (requestCode){
                    case SELECAO_CAMERA:
                        imagem = (Bitmap) data.getExtras().get("data");
                        break;
                    case SELECAO_GALERIA:
                        Uri uriImagemSelec = data.getData();
                        ImageDecoder.Source source = ImageDecoder.createSource(getContentResolver(), uriImagemSelec);
                        imagem = ImageDecoder.decodeBitmap(source);
                        break;
                }

                if (imagem != null){
                    fotoPerfil.setImageBitmap(imagem);

                    //Converter os dados da imagem para armazenar no Firebase
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    imagem.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                    byte[] dadosImagem = baos.toByteArray();

                    //Salvar imagem no Firebase
                    StorageReference imagemRef = storageReference
                            .child("imagens")
                            .child(usr.getId() + ".jpeg");
                    UploadTask uploadTask = imagemRef.putBytes(dadosImagem);
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(ConfiguracoesActivity.this,
                                    "Erro ao fazer upload da foto.",
                                    Toast.LENGTH_LONG).show();
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Toast.makeText(ConfiguracoesActivity.this,
                                    "Foto armazenada com sucesso.",
                                    Toast.LENGTH_LONG).show();
                        }
                    });

                }

            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private void alertaValidacaoPermissao(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permissões Negadas");
        builder.setMessage("Para utilizar o app é necessário aceitar as permissões de acesso.");
        builder.setCancelable(false);
        builder.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void gravar(View view) {
        if (!nome.getText().toString().isEmpty()){
            if (!endereco.getText().toString().isEmpty()) {
                usr.setNome(nome.getText().toString());
                usr.setEndereco(endereco.getText().toString());
                usr.salvar();

                Util.esconderTeclado(view);

                Toast.makeText(ConfiguracoesActivity.this,
                        "Dados salvos com sucesso!",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void abreCamera(View view) {
        Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (i.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(i, SELECAO_CAMERA);
        }
    }

    public void abreGaleria(View view){
        Intent i = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (i.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(i, SELECAO_GALERIA);
        }

    }
}
