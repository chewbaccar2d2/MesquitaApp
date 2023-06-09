package com.example.mesquitaapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.mesquitaapp.R;
import com.example.mesquitaapp.config.ConfiguracaoFirebase;
import com.example.mesquitaapp.model.Usuario;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class PrincipalActivity extends AppCompatActivity {
    private FirebaseAuth autenticacao;
    private TextView ola;
    private Usuario usr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);

        ola = findViewById(R.id.tvOla);

        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();

        usr = new Usuario();
        usr.setId(autenticacao.getUid());
        buscarUsuario();

    }

    public void buscarUsuario(){
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase();
        DatabaseReference usuarioRef = firebaseRef.child("usuarios").child(usr.getId());

        usuarioRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                usr = dataSnapshot.getValue(Usuario.class);
                ola.setText("Olá, " + usr.getNome());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_principal, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.itemSair) {
            ConfiguracaoFirebase.getFirebaseAutenticacao().signOut();
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public void configurarUser(View view){
        Intent i = new Intent(this, ConfiguracoesActivity.class);
        i.putExtra("usr", usr);
        startActivity(i);
    }
}
