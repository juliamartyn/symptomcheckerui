package com.medcare.symptomchecker.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import com.medcare.symptomchecker.R;

public class DisclaimerPopup extends Dialog {
    private String disclaimerText;

    public DisclaimerPopup(Context context, String disclaimerText) {
        super(context);
        this.disclaimerText = disclaimerText;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.disclaimer_popup);

        TextView disclaimerTextView = findViewById(R.id.disclaimer_text);
        disclaimerTextView.setText(disclaimerText);

        Button okButton = findViewById(R.id.ok_button);
        okButton.setOnClickListener(view -> dismiss());
    }
}

