import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.method.PasswordTransformationMethod;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

public class Login {

    private Context context;
    private SharedPreferences sharedPreferences;
    private EditText userInput, passInput;
    private Button loginBtn;

    public Login(Context context) {
        this.context = context;
        this.sharedPreferences = context.getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE);
        buildUI();
    }

    private void buildUI() {
        // Main container
        LinearLayout mainLayout = new LinearLayout(context);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setGravity(Gravity.CENTER);
        mainLayout.setBackgroundColor(Color.parseColor("#1A1A2E"));
        mainLayout.setPadding(dpToPx(30), dpToPx(50), dpToPx(30), dpToPx(50));

        // Card container
        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER_HORIZONTAL);
        card.setPadding(dpToPx(30), dpToPx(40), dpToPx(30), dpToPx(40));
        card.setBackgroundColor(Color.parseColor("#262640"));

        // Title
        TextView title = new TextView(context);
        title.setText("LOGIN");
        title.setTextColor(Color.WHITE);
        title.setTextSize(24);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, dpToPx(30));

        // Username input
        userInput = new EditText(context);
        userInput.setHint("Username");
        userInput.setTextColor(Color.WHITE);
        userInput.setHintTextColor(Color.GRAY);
        userInput.setBackgroundColor(Color.parseColor("#1A1A2E"));
        userInput.setPadding(dpToPx(15), dpToPx(15), dpToPx(15), dpToPx(15));
        userInput.setText(sharedPreferences.getString("username", ""));
        
        LinearLayout.LayoutParams userParams = new LinearLayout.LayoutParams(
                dpToPx(250), ViewGroup.LayoutParams.WRAP_CONTENT);
        userParams.setMargins(0, 0, 0, dpToPx(15));
        userInput.setLayoutParams(userParams);

        // Password input
        passInput = new EditText(context);
        passInput.setHint("Password");
        passInput.setTextColor(Color.WHITE);
        passInput.setHintTextColor(Color.GRAY);
        passInput.setBackgroundColor(Color.parseColor("#1A1A2E"));
        passInput.setPadding(dpToPx(15), dpToPx(15), dpToPx(15), dpToPx(15));
        passInput.setTransformationMethod(PasswordTransformationMethod.getInstance());
        passInput.setText(sharedPreferences.getString("password", ""));
        
        LinearLayout.LayoutParams passParams = new LinearLayout.LayoutParams(
                dpToPx(250), ViewGroup.LayoutParams.WRAP_CONTENT);
        passParams.setMargins(0, 0, 0, dpToPx(25));
        passInput.setLayoutParams(passParams);

        // Login button
        loginBtn = new Button(context);
        loginBtn.setText("LOGIN");
        loginBtn.setTextColor(Color.WHITE);
        loginBtn.setBackgroundColor(Color.parseColor("#9406C7"));
        loginBtn.setTypeface(null, Typeface.BOLD);
        
        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                dpToPx(200), dpToPx(45));
        loginBtn.setLayoutParams(btnParams);

        // Add views to card
        card.addView(title);
        card.addView(userInput);
        card.addView(passInput);
        card.addView(loginBtn);

        // Add card to main layout
        mainLayout.addView(card);

        // Set content view
        ((Activity) context).setContentView(mainLayout);

        // Setup click listener
        setupLoginListener();
    }

    private void setupLoginListener() {
        loginBtn.setOnClickListener(view -> {
            String username = userInput.getText().toString().trim();
            String password = passInput.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                toast("Please fill all fields");
                return;
            }

            // Save credentials
            sharedPreferences.edit()
                    .putString("username", username)
                    .putString("password", password)
                    .apply();

            // Simple validation (you can modify this)
            if (username.equals("admin") && password.equals("password")) {
                toast("Login Successful!");
                // Add your success logic here
            } else {
                toast("Invalid credentials");
            }
        });
    }

    private void toast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }
}
