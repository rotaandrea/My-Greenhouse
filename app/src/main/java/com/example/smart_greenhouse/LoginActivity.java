package com.example.smart_greenhouse;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity{
    /**
     * Gestisce la schermata di accesso e la sicurezza dell'applicazione.
     * Si interfaccia con Firebase Authentication per verificare le credenziali
     * dell'utente, garantendo che solo il proprietario (o chi autorizzato) possa visualizzare
     * o modificare lo stato della serra. Gestisce inoltre il recupero della password.
     */

    private FirebaseAuth fAuth;
    private EditText emailField, passwordField;
    private Button btnLogin;
    private TextView pswDim;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        /**
         * Inizializza l'interfaccia grafica di login e collega i campi di testo
         * (email e password) ai rispettivi listener.
         * Se l'utente ha già effettuato l'accesso in precedenza, può reindirizzarlo
         * automaticamente alla schermata principale senza reinserire i dati.
         *
         * @param savedInstanceState Lo stato precedentemente salvato dell'applicazione.
         */

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        fAuth=FirebaseAuth.getInstance();
        if(fAuth.getCurrentUser()!=null){
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
            return;
        }
        setContentView(R.layout.activity_login);

        emailField=findViewById(R.id.editEmail);
        passwordField=findViewById(R.id.editPassword);
        btnLogin=findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(v -> loginUtente());
        pswDim=findViewById(R.id.pswDimenticata);
        pswDim.setOnClickListener(v -> reimpostaPassword());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_login), (v, insets) -> {
            Insets systemBars=insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void reimpostaPassword(){
        /**
         * Avvia la procedura di recupero della password.
         * Verifica che l'utente abbia inserito una mail valida e fa inviare da Firebase
         * un messaggio automatico con un link sicuro per reimpostare le proprie credenziali.
         */
        String email=emailField.getText().toString().trim();
        if(email.isEmpty()){
            emailField.setError("Inserisci la tua email per reimpostare la password");
            emailField.requestFocus();
            return;
        }
        fAuth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
                    if(task.isSuccessful()) Toast.makeText(LoginActivity.this, "Email di reset inviata! Controlla la tua posta.", Toast.LENGTH_LONG).show();
                    else Toast.makeText(LoginActivity.this, "Errore: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void loginUtente(){
        /**
         * Preleva le stringhe inserite nei campi di testo (Email e Password),
         * verifica che non siano vuote e tenta l'autenticazione sicura tramite Firebase.
         * Gestisce tutte le eccezioni che i metodi di Firebase potrebbero lanciare e
         * restituisce all'utente un messaggio facilmente interpretabile relativo alla
         * posizione dell'errore.
         * Se il login ha successo, apre la Dashboard (MainActivity); in caso contrario,
         * mostra un messaggio di errore (Toast) per credenziali errate.
         */
        String email=emailField.getText().toString().trim();
        String password=passwordField.getText().toString().trim();

        if(email.isEmpty()){
            emailField.setError("L'Email è obbligatoria");
            emailField.requestFocus();
            return;
        }
        if(password.isEmpty()){
            passwordField.setError("La Password è obbligatoria");
            passwordField.requestFocus();
            return;
        }
        if(password.length()<6){
            passwordField.setError("La password deve essere di almeno 6 caratteri");
            passwordField.requestFocus();
            return;
        }
        fAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, task ->{
            if(task.isSuccessful()){
                Toast.makeText(LoginActivity.this, "Bentornato!", Toast.LENGTH_SHORT).show();
                Intent intent=new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }else{
                try{
                     throw task.getException();
                }catch(com.google.firebase.auth.FirebaseAuthInvalidUserException e) {
                    //FirebaseAuthInvalidUserException=Email non trovata o account bloccato
                    emailField.setError("Non c'è nessun account con questa email");
                    emailField.requestFocus();
                }catch(com.google.firebase.auth.FirebaseAuthInvalidCredentialsException e) {
                    //FirebaseAuthInvalidCredentialsException=Password errata
                    passwordField.setError("Password errata, riprova");
                    passwordField.requestFocus();
                }catch(Exception e){
                    //Per problemi di rete o altri errori come 'Metodo di accesso disabilitato'
                    Toast.makeText(LoginActivity.this, "Errore connessione al Database", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
