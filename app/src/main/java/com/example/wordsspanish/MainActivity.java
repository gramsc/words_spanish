package com.example.wordsspanish;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private List<VocabularyItem> vocabulary;
    private List<VocabularyItem> wordList;
    private int currentIndex;
    private int correctCount = 0;
    private VocabularyItem currentWord;

    private TextView wordText;
    private TextView definitionText;
    private TextView categoryText;
    private TextView detailsText;
    private MaterialButtonToggleGroup genreButtonGroup;
    private MaterialButton masculineButton;
    private MaterialButton feminineButton;
    private MaterialButton invariableButton;
    private TextView feedbackText;

    private TextInputEditText translationInput;
    private TextInputLayout translationInputLayout;

    private TextView correctCountText;
    private Button submitTranslationButton;
    private Button nextButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MaterialToolbar toolbar = findViewById(R.id.bannerHeader);
        TextView toolbarTitle = findViewById(R.id.toolbarTitle);
        toolbarTitle.setSelected(true);

        vocabulary = getVoc();
        wordList = new ArrayList<>(vocabulary);

        wordText = findViewById(R.id.wordText);
        categoryText = findViewById(R.id.categoryText);
        detailsText = findViewById(R.id.detailsText);
        definitionText = findViewById(R.id.definitionText);
        genreButtonGroup = findViewById(R.id.genreButtonGroup);
        masculineButton = findViewById(R.id.masculineButton);
        feminineButton = findViewById(R.id.feminineButton);
        invariableButton = findViewById(R.id.invariableButton);

        feedbackText = findViewById(R.id.feedbackText);

        translationInput = findViewById(R.id.translationInput);
        translationInputLayout = findViewById(R.id.translationInputLayout);

        correctCountText = findViewById(R.id.correctCountText);
        submitTranslationButton = findViewById(R.id.submitTranslationButton);
        nextButton = findViewById(R.id.nextButton);

        startNewSession();

        submitTranslationButton.setOnClickListener(v -> checkTranslation());

        nextButton.setOnClickListener(v -> nextWord());

        genreButtonGroup.setSingleSelection(true);
        genreButtonGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.masculineButton) {
                    currentWord.setUserGenre("masculin");
                } else if (checkedId == R.id.feminineButton) {
                    currentWord.setUserGenre("féminin");
                } else if (checkedId == R.id.invariableButton) {
                    currentWord.setUserGenre("invariable");
                }
            }
        });

        translationInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                    actionId == EditorInfo.IME_ACTION_NEXT ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                checkTranslation();
                return true;
            }
            return false;
        });
    }

    private void startNewSession() {
        Collections.shuffle(wordList);
        currentIndex = 0;
        correctCount = 0;
        updateCorrectCountText();
        setNewWord();
    }

    private void checkTranslation() {
        String userTranslation = translationInput.getText().toString().trim();
        String correctTranslation = currentWord.getSpanish();

        String definitionAndGenre = currentWord.getSpanish();
        if ("masculin".equalsIgnoreCase(currentWord.getGenre())) {
            definitionAndGenre = definitionAndGenre + " (m.)";
        } else if ("féminin".equalsIgnoreCase(currentWord.getGenre())) {
            definitionAndGenre = definitionAndGenre + " (f.)";
        } else if ("invariable".equalsIgnoreCase(currentWord.getGenre())) {
            definitionAndGenre = definitionAndGenre + " (inv.)";
        }
        definitionText.setText(definitionAndGenre);

        boolean isCorrect = userTranslation.equalsIgnoreCase(correctTranslation);

        if (currentWord.getCategory().equals("nom commun") && currentWord.getGenre() != null) {
            isCorrect = isCorrect && currentWord.getGenre().equalsIgnoreCase(currentWord.getUserGenre());
        }

        if (isCorrect) {
            feedbackText.setText("Correct!");
            correctCount++;
            updateCorrectCountText();
        } else {
            feedbackText.setText("Incorrect.");
        }

        // Hide user input and genre buttons
        translationInputLayout.setVisibility(View.GONE);
        genreButtonGroup.setVisibility(View.GONE);

        feedbackText.setVisibility(View.VISIBLE);
        definitionText.setVisibility(View.VISIBLE);
        submitTranslationButton.setVisibility(View.GONE);
        nextButton.setVisibility(View.VISIBLE);

        // Hide the keyboard
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(translationInput.getWindowToken(), 0);
    }

    private void nextWord() {
        currentIndex++;
        if (currentIndex >= wordList.size()) {
            showRestartDialog();
        } else {
            setNewWord();
        }
    }

    private void setNewWord() {
        currentWord = wordList.get(currentIndex);
        wordText.setText(currentWord.getFrench());
        categoryText.setText(currentWord.getCategory());
        detailsText.setText(currentWord.getDetails());
        translationInput.setText("");
        feedbackText.setVisibility(View.GONE);
        definitionText.setVisibility(View.GONE);

        if (currentWord.getCategory().equals("nom commun")) {
            genreButtonGroup.setVisibility(View.VISIBLE);
            if (currentWord.getGenre() != null) {
                masculineButton.setVisibility(View.VISIBLE);
                feminineButton.setVisibility(View.VISIBLE);
                invariableButton.setVisibility(View.VISIBLE);
            } else {
                masculineButton.setVisibility(View.GONE);
                feminineButton.setVisibility(View.GONE);
                invariableButton.setVisibility(View.GONE);
            }
        } else {
            genreButtonGroup.setVisibility(View.GONE);
        }

        translationInputLayout.setVisibility(View.VISIBLE);
        submitTranslationButton.setVisibility(View.VISIBLE);
        nextButton.setVisibility(View.GONE);
    }

    private void showRestartDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Session Complete")
                .setMessage("You have gone through all the words. Do you want to start again?")
                .setPositiveButton("Yes", (dialog, which) -> startNewSession())
                .setNegativeButton("No", (dialog, which) -> finish())
                .show();
    }

    private void updateCorrectCountText() {
        correctCountText.setText("Correct translations: " + correctCount + "/" + wordList.size());
    }

    private List<VocabularyItem> getVoc() {
        List<VocabularyItem> voc = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(getAssets().open("voc.json")))) {
            Gson gson = new Gson();
            Type type = new TypeToken<List<VocabularyItem>>() {}.getType();
            voc = gson.fromJson(reader, type);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return voc;
    }
}

class VocabularyItem {
    private String french;
    private String spanish;
    private String category;
    private String genre;
    private String details;
    private String userGenre;

    public String getFrench() {
        return french;
    }

    public String getSpanish() {
        return spanish;
    }

    public String getDetails() {
        return details;
    }

    public String getCategory() {
        return category;
    }

    public String getGenre() {
        return genre;
    }

    public String getUserGenre() {
        return userGenre;
    }

    public void setUserGenre(String userGenre) {
        this.userGenre = userGenre;
    }
}
