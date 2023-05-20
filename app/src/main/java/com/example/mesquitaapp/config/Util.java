package com.example.mesquitaapp.config;

import android.view.View;
import android.view.inputmethod.InputMethodManager;

import static android.content.Context.INPUT_METHOD_SERVICE;

/*
Classe que contém métodos comuns que possam ser utilizados
em qualquer ponto do APP
 */
public class Util {

    //Método que esconde o teclado virtual
    public static void esconderTeclado(View view){
        InputMethodManager inputMethodManager = (InputMethodManager) view.getContext().getSystemService(INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
