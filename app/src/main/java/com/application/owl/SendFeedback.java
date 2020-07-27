package com.application.owl;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

// activity class for the Send Feedback activity
public class SendFeedback extends AppCompatActivity {

    private EditText feedbackEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_feedback);
        feedbackEditText = findViewById(R.id.feedback_edit_text);
    }

    public void submitFeedback(View v) {

        final String feedbackText = feedbackEditText.getText().toString().trim();

        if (feedbackText.isEmpty()) {
            feedbackEditText.setError(getResources().getString(R.string.empty_feedback_toast));
            feedbackEditText.requestFocus();
            return;
        }

        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

        String uriText =
                "mailto:" + Uri.encode(getResources().getString(R.string.feedback_email))
                        + "?subject=" + Uri.encode(getResources().getString(R.string.feedback_subject_line)) +
                        "&body=" + Uri.encode(feedbackText);

        emailIntent.setData(Uri.parse(uriText)); // only email apps should handle this

        if (emailIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(Intent.createChooser(emailIntent, getResources().getString(R.string.select_email_app_toast)));
        }

        this.finish();
    }
}
