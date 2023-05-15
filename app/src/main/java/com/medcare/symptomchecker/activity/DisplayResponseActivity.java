package com.medcare.symptomchecker.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.medcare.symptomchecker.dialog.DisclaimerPopup;
import com.medcare.symptomchecker.R;

public class DisplayResponseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        Intent intent = getIntent();
        String responseData = intent.getStringExtra("response_data");

        TextView responseTextView = findViewById(R.id.response_text_view);
        responseTextView.setText(responseData);

        ImageView backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());

        String disclaimerText ="УВАГА!\n" +
                "Ніколи не нехтуйте професійною медичною порадою та не зволікайте з її зверненням через те, що ви прочитали в цій програмі.\n" +
                "Хоча було докладено всіх зусиль для надання точної та актуальної інформації, програма не гарантує точності, повноти чи надійності будь-якої наданої інформації. " +
                "Таким чином, розробник цієї програми не несе відповідальності за будь-які прямі чи непрямі наслідки, які можуть виникнути внаслідок використання цієї програми або інформації, що в ній міститься. " +
                "Рекомендується завжди консультуватися з кваліфікованим медичним працівником для будь-якої медичної консультації, діагностики чи лікування.";
        DisclaimerPopup popup = new DisclaimerPopup(DisplayResponseActivity.this, disclaimerText);
        popup.show();
    }
}

