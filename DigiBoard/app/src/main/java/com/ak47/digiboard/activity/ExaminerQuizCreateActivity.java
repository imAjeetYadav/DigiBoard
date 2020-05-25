package com.ak47.digiboard.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.ak47.digiboard.R;
import com.google.android.material.textfield.TextInputEditText;

/*
        #Done
    - Add Quiz Title,description
    then send to add question activity
   */
public class ExaminerQuizCreateActivity extends AppCompatActivity {
    private TextInputEditText quizName, quizDescription, quizEncryptionCode;
    private Button nextButton;
    private String TAG = "Quiz Create Activity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_examiner_quiz_create);
        Toolbar toolbar = findViewById(R.id.toolbar);
        TextView mTitle = toolbar.findViewById(R.id.toolbar_title);

        setSupportActionBar(toolbar);
        mTitle.setText(R.string.quiz_details);

        quizName = findViewById(R.id.quizNameId);
        quizDescription = findViewById(R.id.quizDescriptionId);
        quizEncryptionCode = findViewById(R.id.quizEncryptionCode);
        nextButton = findViewById(R.id.nextButton);

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String Name = quizName.getText().toString().trim();
                String Description = quizDescription.getText().toString().trim();
                String EncryptionCode = quizEncryptionCode.getText().toString().trim();

                if (validation(Name, Description, EncryptionCode)) {
                    // send To QuestionListActivity
                    sendToQuestionListActivity(Name, Description, EncryptionCode);
                }
            }
        });


    }

    private void sendToQuestionListActivity(String Name, String Description, String EncryptionCode) {
        Intent intent = new Intent(ExaminerQuizCreateActivity.this, ExaminerQuestionListActivity.class);
        intent.putExtra("quizName", Name);
        intent.putExtra("quizDescription", Description);
        intent.putExtra("quizEncryptionCode", EncryptionCode);
        startActivity(intent);
    }

    private boolean validation(String Name, String Description, String EncryptionCode) {
        boolean check = true;
        if (TextUtils.isEmpty(Name)) {
            quizName.setError("Enter Quiz Name");
            check = false;
        }
        if (TextUtils.isEmpty(Description)) {
            quizDescription.setError("Enter Quiz Description ");
            check = false;
        }
        if (TextUtils.isEmpty(EncryptionCode)) {
            quizEncryptionCode.setError("Enter Quiz Encryption Code");
            check = false;
        }
        if (EncryptionCode.length() != 4) {
            quizEncryptionCode.setError("4 digit of code required");
            check = false;
        }

        return check;
    }
}
