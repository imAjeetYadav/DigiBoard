package com.ak47.digiboard.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.ak47.digiboard.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/*
      -SignOut button
*/
public class SettingsActivity extends AppCompatActivity {
    private static final String TAG = "SettingsActivity";
    private FirebaseAuth mAuth;
    private android.app.AlertDialog signOutDialog;
    private GoogleSignInClient mGoogleSignInClient;
    private GoogleSignInOptions gso;
    private TextView remainingCreditText;
    private Button getSomeCreditButton, refreshCreditButton;
    private DatabaseReference rootRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = findViewById(R.id.toolbar);
        TextView mTitle = toolbar.findViewById(R.id.toolbar_title);
        setSupportActionBar(toolbar);
        mTitle.setText(R.string.setting);

        remainingCreditText = findViewById(R.id.remainingCreditTextView);
        getSomeCreditButton = findViewById(R.id.getCreditButton);
        refreshCreditButton = findViewById(R.id.refreshCreditButton);

        SharedPreferences sharedPreferences = getSharedPreferences("initial_setup", MODE_PRIVATE);
        int initialSetupInt = sharedPreferences.getInt("initial_setup", 0);

        rootRef = FirebaseDatabase.getInstance().getReference();
        if (initialSetupInt == 1) {
            getSomeCreditButton.setVisibility(View.GONE);
            remainingCreditText.setVisibility(View.GONE);
            refreshCreditButton.setVisibility(View.GONE);

        } else if (initialSetupInt == 2) {
            setCreditInTextView();
        }

        mAuth = FirebaseAuth.getInstance();

        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        Button signOut = findViewById(R.id.signOut);
        signOut.setOnClickListener(v -> {
            signOutDialog = signOutAndCacheCleanDialog();
            signOutDialog.show();
        });

        getSomeCreditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(SettingsActivity.this, R.style.AlertDialogStyle)
                        .setMessage("Coming Soon..\nFor Business Mail At - codingprotocols@gmail.com  ")
                        .show();
            }
        });

        refreshCreditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Query query = rootRef.child("AdminUsers")
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Log.d(TAG, "onDataChange: getCreditedDate from server");
                        String currentDateString = new SimpleDateFormat("dd-MM-yyyy", Locale.US).format(Calendar.getInstance().getTime());
                        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
                        try {
                            Date creditedOn = sdf.parse(String.valueOf(dataSnapshot.child("creditedOn").getValue()));
                            Date currentDate = sdf.parse(currentDateString);
                            long diffInDay = (currentDate.getTime() - creditedOn.getTime()) / (24 * 60 * 60 * 1000);
                            if (diffInDay > 29) {

                                DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                                        .child("AppConfig");
                                reference.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        Log.d(TAG, " onDataChange: getCredit Value");
                                        rootRef.child("AdminUsers").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                .child("creditedOn").setValue(new SimpleDateFormat("dd-MM-yyyy", Locale.US).format(Calendar.getInstance().getTime()));
                                        rootRef.child("AdminUsers").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                .child("credit").setValue(String.valueOf(dataSnapshot.child("credit").getValue())).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                setCreditInTextView();
                                                new AlertDialog.Builder(SettingsActivity.this, R.style.AlertDialogStyle)
                                                        .setMessage("Credit Updated")
                                                        .show();
                                            }
                                        });
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                        new AlertDialog.Builder(SettingsActivity.this, R.style.AlertDialogStyle)
                                                .setMessage("Sorry Error occurred\nTry Again Later ")
                                                .show();
                                    }
                                });
                            } else {
                                new AlertDialog.Builder(SettingsActivity.this, R.style.AlertDialogStyle)
                                        .setMessage("Wait " + (30 - diffInDay) + " days to refresh credit\nIf you want credit now then\n click at get Some Credit Button")
                                        .show();
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.d(TAG, "onCancelled: Error: " + databaseError.getMessage());
                    }
                });


            }
        });

    }

    private void setCreditInTextView() {
        Query query = rootRef.child("AdminUsers")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange: getCredit from server");
                remainingCreditText.setText(getString(R.string.remaining_credit) + " " + dataSnapshot.child("credit").getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: Error: " + databaseError.getMessage());
            }
        });
    }

    private android.app.AlertDialog signOutAndCacheCleanDialog() {
        final android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(this, R.style.AlertDialogStyle)
                .setTitle("Alert")
                .setMessage("Are Really want to Sign Out")
                .setCancelable(false)
                .setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                mGoogleSignInClient.signOut().addOnCompleteListener(SettingsActivity.this, new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        mAuth.signOut();
                                        sendToLogInActivity();
                                    }
                                });
                            }
                        })
                .setNeutralButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //dismiss
                    }
                });
        return (alertDialogBuilder.create());
    }

    private void sendToLogInActivity() {
        Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();

    }
}
