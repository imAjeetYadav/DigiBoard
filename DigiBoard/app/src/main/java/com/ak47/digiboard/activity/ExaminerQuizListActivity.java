package com.ak47.digiboard.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ak47.digiboard.R;
import com.ak47.digiboard.model.QuizListModel;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.victor.loading.rotate.RotateLoading;

import java.text.MessageFormat;

/*
     List of Quiz
 */
public class ExaminerQuizListActivity extends AppCompatActivity {
    private RecyclerView quizRecyclerList;
    private DatabaseReference quizRef;
    private String TAG = "Quiz List Activity";
    private TextView noQuizFound;
    private RotateLoading rotateLoading;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_examiner_quiz_list);
        Toolbar toolbar = findViewById(R.id.toolbar);
        TextView mTitle = toolbar.findViewById(R.id.toolbar_title);
        setSupportActionBar(toolbar);
        mTitle.setText(R.string.quiz_list);
        changeSetting();

        noQuizFound = findViewById(R.id.no_quiz_found);
        rotateLoading = findViewById(R.id.mainLoading);

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        quizRef = FirebaseDatabase.getInstance().getReference().child("AdminUsers").child(userId).child("MyQuizLists");
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        // to reverse recycle view
        mLayoutManager.setReverseLayout(true);
        mLayoutManager.setStackFromEnd(true);
        quizRecyclerList = findViewById(R.id.quizRecyclerView);
        quizRecyclerList.setLayoutManager(mLayoutManager);

        quizRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                noQuizFound.setText(MessageFormat.format("{0}{1}", getString(R.string.total_quiz), (int) dataSnapshot.getChildrenCount()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error : " + databaseError.getMessage());
            }
        });


        FirebaseRecyclerOptions<QuizListModel> quizDetailsModelFirebaseRecyclerOptions =
                new FirebaseRecyclerOptions.Builder<QuizListModel>()
                        .setQuery(quizRef, QuizListModel.class)
                        .build();
        rotateLoading.start();
        final FirebaseRecyclerAdapter<QuizListModel, QuizListViewHolder> adapter = new FirebaseRecyclerAdapter<QuizListModel, QuizListViewHolder>(quizDetailsModelFirebaseRecyclerOptions) {
            @Override
            protected void onBindViewHolder(@NonNull final QuizListViewHolder holder, final int position, @NonNull final QuizListModel quizListModel) {
                holder.quizName.setText(quizListModel.getQuizName());
                holder.createdDateTime.setText(quizListModel.getCreatedDateTime().split(" ")[0]);

                if (quizListModel.getPublishInfo()) {
                    holder.publishInfo.setText(R.string.published);
                    holder.publishInfo.setBackgroundColor(getColor(R.color.bg_screen2));
                } else {
                    holder.publishInfo.setBackgroundColor(getColor(R.color.bg_screen1));
                }

                final Boolean finalPublishInfo = quizListModel.getPublishInfo();
                holder.itemView.setOnClickListener(v -> {
                    if (finalPublishInfo) {
                        Intent quizInfoIntent = new Intent(ExaminerQuizListActivity.this, ExaminerShowQuizResultActivity.class);
                        quizInfoIntent.putExtra("QuizName", quizListModel.getQuizName());
                        startActivity(quizInfoIntent);
                    } else {
                        Intent quizInfoIntent = new Intent(ExaminerQuizListActivity.this, ExaminerQuizPublishActivity.class);
                        quizInfoIntent.putExtra("QuizName", quizListModel.getQuizName());
                        quizInfoIntent.putExtra("quizDescription", quizListModel.getQuizDescription());
                        startActivity(quizInfoIntent);
                    }
                });
            }

            @Override
            public void onDataChanged() {
                if (rotateLoading != null && rotateLoading.isStart()) {
                    rotateLoading.stop();
                }
            }

            @NonNull
            @Override
            public QuizListViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_examiner_quiz_details, viewGroup, false);
                return new QuizListViewHolder(view);
            }
        };
        quizRecyclerList.setAdapter(adapter);
        adapter.startListening();


    }
    private void changeSetting() {
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE);
    }

    public static class QuizListViewHolder extends RecyclerView.ViewHolder {
        TextView quizName;
        TextView publishInfo;
        TextView createdDateTime;

        QuizListViewHolder(@NonNull View itemView) {
            super(itemView);

            quizName = itemView.findViewById(R.id.quizName);
            publishInfo = itemView.findViewById(R.id.publishInfo);
            createdDateTime = itemView.findViewById(R.id.createdDateTime);
        }
    }
}
