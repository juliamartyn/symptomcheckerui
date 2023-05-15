package com.medcare.symptomchecker.activity;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.medcare.symptomchecker.adapter.ChosenItemsAdapter;
import com.medcare.symptomchecker.R;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    List<String> symptomsRequest = new ArrayList<>();

    private static final String PREDICT_DISEASE_API_URL = "http://10.0.2.2:8080/api/predictor/disease";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AutoCompleteTextView autoCompleteTextView = findViewById(R.id.autoCompleteTextView);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.dropdown_item, R.id.dropdown_item_text, suggestions);
        autoCompleteTextView.setAdapter(adapter);

        setUpFullDropDown(autoCompleteTextView);
        setUpInputClearButton(autoCompleteTextView);

        handleNotExistedItemEntered(autoCompleteTextView);

        RecyclerView recyclerView = setUpRecyclerViewWithFlexbox();

        List<String> chosenItems = new ArrayList<>();
        ChosenItemsAdapter chosenItemsAdapter = new ChosenItemsAdapter(chosenItems);

        recyclerView.setAdapter(chosenItemsAdapter);

        handleChooseItem(autoCompleteTextView, adapter, chosenItems, chosenItemsAdapter);
        handleDeleteChosenItem(chosenItems, chosenItemsAdapter);

        ImageButton clearAllButton = findViewById(R.id.clearAllButton);
        clearAllButton.setOnClickListener(v -> {
            // Clear the list of selected items in the RecyclerView adapter
            chosenItems.clear();
            symptomsRequest.clear();
            recyclerView.getAdapter().notifyDataSetChanged();

            // Hide the clear button
            clearAllButton.setVisibility(View.GONE);
        });

        predictAndDisplay();
    }

    private void setUpFullDropDown(AutoCompleteTextView autoCompleteTextView) {
        ImageView arrowImageView = findViewById(R.id.arrowImageView);
        arrowImageView.setOnClickListener(v -> autoCompleteTextView.showDropDown());
    }

    private void setUpInputClearButton(AutoCompleteTextView autoCompleteTextView) {
        ImageView clearButton = findViewById(R.id.clearButton);
        clearButton.setOnClickListener(v -> autoCompleteTextView.setText(""));
    }


    private RecyclerView setUpRecyclerViewWithFlexbox() {
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(this);
        layoutManager.setFlexWrap(FlexWrap.WRAP);
        recyclerView.setLayoutManager(layoutManager);

        return recyclerView;
    }

    private void handleNotExistedItemEntered(AutoCompleteTextView autoCompleteTextView) {
        autoCompleteTextView.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                String enteredText = v.getText().toString().trim();
                if (!Arrays.asList(suggestions).contains(enteredText)) {
                    redToast("Невалідний симптом ведено. Будь ласка, спробуйте ще раз або оберіть симптом зі списку.");
                }
                v.setText("");
                return true;
            }
            return false;
        });
    }

    private void handleChooseItem(AutoCompleteTextView autoCompleteTextView,
                                  ArrayAdapter<String> adapter,
                                  List<String> chosenItems,
                                  ChosenItemsAdapter chosenItemsAdapter) {
        autoCompleteTextView.setOnItemClickListener((parent, view, position, id) -> {
            String chosenItem = adapter.getItem(position);

            if (chosenItems.contains(chosenItem)) {
                redToast("Симптом вже додано.");
                autoCompleteTextView.setText("");
            } else {
                chosenItems.add(chosenItem);
                symptomsRequest.add(chosenItem);
                chosenItemsAdapter.notifyItemInserted(chosenItems.size() - 1);
                autoCompleteTextView.setText("");
                findViewById(R.id.clearAllButton).setVisibility(View.VISIBLE);
            }
        });
    }

    private void handleDeleteChosenItem(List<String> chosenItems, ChosenItemsAdapter chosenItemsAdapter) {
        chosenItemsAdapter.setOnCloseClickListener(pos -> {
            String item = chosenItems.get(pos);
            symptomsRequest.remove(item);
            chosenItems.remove(pos);
            chosenItemsAdapter.notifyItemRemoved(pos);
            chosenItemsAdapter.notifyItemRangeChanged(pos, chosenItems.size() - pos);
        });
    }

    private void predictAndDisplay() {
        Button predictButton = findViewById(R.id.predict_button);

        predictButton.setOnClickListener(v -> {
            if (symptomsRequest.isEmpty()) {
                redToast("Будь ласка, зазначте ваші симптоми.");
                return;
            }
            OkHttpClient client = new OkHttpClient();
            HttpUrl.Builder urlBuilder = HttpUrl.parse(PREDICT_DISEASE_API_URL).newBuilder();
            for (String symptom : symptomsRequest) {
                urlBuilder.addQueryParameter("symptom", symptom);
            }

            Request request = new Request.Builder().url(urlBuilder.build().toString()).build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    new Handler(Looper.getMainLooper()).post(() -> redToast("Упс, щось пішло не так. Спробуйте ще раз."));
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String responseData = response.body().string();

                        // display predicted disease
                        Intent intent = new Intent(MainActivity.this, DisplayResponseActivity.class);
                        intent.putExtra("response_data", responseData);
                        startActivity(intent);
                    }
                }
            });
        });
    }

    private void redToast(String message) {
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.red_toast, findViewById(R.id.red_toast_container));

        TextView text = layout.findViewById(R.id.toast_text);
        text.setText(message);

        Toast toast = new Toast(getApplicationContext());
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.setGravity(Gravity.CENTER, 0, 100);
        toast.show();
    }


    String[] suggestions = {
            "безперервне чхання",
            "біль в анальній ділянці",
            "біль за очима",
            "біль під час випорожнення",
            "біль у грудях",
            "біль у животі",
            "біль у колінах",
            "біль у кульшовому суглобі",
            "біль у м’язах",
            "біль у спині",
            "біль у суглобах",
            "біль у тазостегновому суглобі",
            "біль у шиї",
            "біль у шлунку",
            "блювання",
            "болісна ходьба",
            "вживання алкоголю в анамнезі",
            "виділення газів",
            "виділення жовтої скоринки",
            "виразки на язиці",
            "виснаження м’язів",
            "висока температура",
            "виступаючі вени на литках",
            "відсутність концентрації",
            "відходження газів",
            "внутрішній свербіж",
            "втома",
            "втрата апетиту",
            "втрата ваги",
            "втрата нюху",
            "втрата рівноваги",
            "вугри",
            "вузлові висипання на шкірі",
            "гнійні прищі",
            "головний біль",
            "гостра печінкова недостатність",
            "депресія",
            "дискомфорт у сечовому міхурі",
            "дисхромні плями",
            "діарея",
            "дратівливість",
            "жовта сеча",
            "жовті очі",
            "жовтувата шкіра",
            "задишка",
            "закладеність",
            "занепокоєння",
            "запалення нігтів",
            "запалі очі",
            "запаморочення",
            "запор",
            "затуманення та спотворення зору",
            "збільшення ваги",
            "збільшення лімфовузлів",
            "збільшення щитовидної залози",
            "здуття живота",
            "зміна сенсорної системи",
            "зміна чутливості",
            "зневоднення",
            "іржаве мокротиння",
            "кашель",
            "кислотність",
            "кома",
            "кров у калі",
            "кров у мокроті",
            "кров’янисті виділення",
            "кров’янисті сечовипускання",
            "ламкість нігтів",
            "легка лихоманка",
            "лущення шкіри",
            "м’язова слабкість",
            "млявість",
            "мокрота",
            "мокротиння",
            "набряк суглобів",
            "набряки кінцівок",
            "набряки кровоносних судин",
            "набряки ніг",
            "набряклі кровоносні судини",
            "надмірний голод",
            "невеликі вм’ятини на нігтях",
            "невиразна мова",
            "нежить",
            "нездужання",
            "ненормальна менструація",
            "неприємний запах сечі",
            "нерегулярний рівень цукру",
            "неспокій",
            "нестабільність",
            "нестерильні ін’єкції",
            "нестійкість",
            "нудота",
            "обертові рухи",
            "одутле обличчя та очі",
            "ожиріння",
            "озноб",
            "опухлі ноги",
            "перевантаження рідиною",
            "переливання крові",
            "перепади настрою",
            "печіння при сечовипусканні",
            "печіння сечовипускання",
            "підвищена кислотність",
            "підвищений апетит",
            "пітливість",
            "плями в горлі",
            "подразнення горла",
            "подразнення заднього проходу",
            "пожовтіння очей",
            "позашлюбні контакти",
            "поліурія",
            "попрілості",
            "порушення чутливості",
            "постійне відчуття сечі",
            "постійне чхання",
            "почервоніння очей",
            "прискорене серцебиття",
            "пухирі",
            "ригідність шиї",
            "роздуті кровоносні судини",
            "розлад травлення",
            "розлади зору",
            "розмитість і викривлення зору",
            "рубці",
            "свербіж",
            "серцебиття",
            "синусовий тиск",
            "синці",
            "сімейний анамнез",
            "скутість рухів",
            "слабкість однієї сторони тіла",
            "слабкість у кінцівках",
            "слизове мокротиння",
            "сльозотеча",
            "сочиться жовта кірка",
            "сріблястий наліт",
            "судоми",
            "сухість і поколювання губ",
            "темна сеча",
            "тиск у носових пазухах",
            "тиск у пазухах",
            "токсичний вигляд (тиф)",
            "токсичний вигляд (черевний тиф)",
            "тремтіння",
            "холодні руки та ноги",
            "червона ранка навколо носа",
            "червоні плями на тілі",
            "чорні точки",
            "шкірний висип",
            "шлункова кровотеча"
    };
}