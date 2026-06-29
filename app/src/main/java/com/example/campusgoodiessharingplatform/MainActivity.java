package com.example.campusgoodiessharingplatform;

import android.app.AlertDialog;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Gravity;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.campusgoodiessharingplatform.api.ApiClient;
import com.example.campusgoodiessharingplatform.api.ApiResponse;
import com.example.campusgoodiessharingplatform.api.ApiService;
import com.example.campusgoodiessharingplatform.model.Category;
import com.example.campusgoodiessharingplatform.model.User;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private static final String ROLE_USER = "普通用户";

    private final ApiService api = ApiClient.service();
    private final Gson gson = new Gson();
    private User currentUser;
    private EditText uploadTarget;
    private ActivityResultLauncher<String> imagePicker;
    private OnBackPressedCallback backPressedCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imagePicker = registerForActivityResult(new ActivityResultContracts.GetContent(), this::handlePickedImage);
        String saved = getSharedPreferences("session", MODE_PRIVATE).getString("user", null);
        if (saved != null) currentUser = gson.fromJson(saved, User.class);
        if (currentUser == null) showLogin(); else showMain();
    }

    public ApiService api() {
        return api;
    }

    public User currentUser() {
        return currentUser;
    }

    public void saveCurrentUser() {
        getSharedPreferences("session", MODE_PRIVATE).edit().putString("user", gson.toJson(currentUser)).apply();
    }

    public void logout() {
        getSharedPreferences("session", MODE_PRIVATE).edit().clear().apply();
        currentUser = null;
        showLogin();
    }

    public void openHome() {
        updateNavState(R.id.nav_home);
        switchFragment(HomeFragment.newInstance(true));
    }

    public void openShareHome() {
        updateNavState(R.id.nav_home);
        switchFragment(HomeFragment.newInstance(false));
    }

    public void openHot() {
        updateNavState(R.id.nav_hot);
        switchFragment(new HotFragment());
    }

    public void openMessages() {
        updateNavState(R.id.nav_messages);
        switchFragment(new MessagesFragment());
    }

    public void openMe() {
        updateNavState(R.id.nav_me);
        switchFragment(new MeFragment());
    }

    public void openArticleDetail(int articleId) {
        updateNavState(0);
        switchFragment(ArticleDetailFragment.newInstance(articleId));
    }

    private void showMain() {
        setContentView(R.layout.activity_main);
        applyStatusBarSpacing(findViewById(R.id.content_container), 4);
        setupBackPressedHandler();
        findViewById(R.id.nav_home).setOnClickListener(v -> openHome());
        findViewById(R.id.nav_hot).setOnClickListener(v -> openHot());
        findViewById(R.id.nav_publish).setOnClickListener(v -> showPublishChooser());
        findViewById(R.id.nav_messages).setOnClickListener(v -> openMessages());
        findViewById(R.id.nav_me).setOnClickListener(v -> openMe());
        openHome();
    }

    private void setupBackPressedHandler() {
        if (backPressedCallback != null) return;
        backPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Fragment current = getSupportFragmentManager().findFragmentById(R.id.content_container);
                if (current instanceof ArticleDetailFragment) {
                    openShareHome();
                    return;
                }
                setEnabled(false);
                getOnBackPressedDispatcher().onBackPressed();
                setEnabled(true);
            }
        };
        getOnBackPressedDispatcher().addCallback(this, backPressedCallback);
    }

    private void switchFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content_container, fragment)
                .commit();
    }

    private void updateNavState(int selectedId) {
        int[] ids = {R.id.nav_home, R.id.nav_hot, R.id.nav_messages, R.id.nav_me};
        for (int id : ids) {
            MaterialButton button = findViewById(id);
            if (button == null) continue;
            boolean selected = id == selectedId;
            button.setSelected(selected);
            button.setTextColor(selected ? 0xffffffff : 0xff444444);
            button.setBackgroundTintList(android.content.res.ColorStateList.valueOf(selected ? 0xff3f51b5 : 0xffffffff));
            button.setStrokeColor(android.content.res.ColorStateList.valueOf(selected ? 0xff3f51b5 : 0xffffffff));
            button.setStrokeWidth(dp(1));
        }
    }

    private void showLogin() {
        setContentView(R.layout.activity_login);
        applyStatusBarSpacing(findViewById(R.id.login_root), 0);
        EditText username = findViewById(R.id.login_username);
        EditText password = findViewById(R.id.login_password);
        MaterialButton login = findViewById(R.id.login_button);
        MaterialButton register = findViewById(R.id.register_button);
        login.setOnClickListener(v -> doLogin(username.getText().toString(), password.getText().toString()));
        register.setOnClickListener(v -> showRegisterDialog());
    }

    private void doLogin(String username, String password) {
        Map<String, Object> body = new HashMap<>();
        body.put("username", username);
        body.put("password", password);
        body.put("role", ROLE_USER);
        call(api.login(body), user -> {
            currentUser = user;
            saveCurrentUser();
            showMain();
        });
    }

    private void showRegisterDialog() {
        android.view.View form = getLayoutInflater().inflate(R.layout.dialog_register, null);
        EditText username = form.findViewById(R.id.register_username);
        EditText password = form.findViewById(R.id.register_password);
        EditText name = form.findViewById(R.id.register_name);
        EditText phone = form.findViewById(R.id.register_phone);
        EditText email = form.findViewById(R.id.register_email);
        new AlertDialog.Builder(this).setTitle("注册")
                .setView(form)
                .setNegativeButton("取消", null)
                .setPositiveButton("注册", (d, w) -> {
                    Map<String, Object> body = new HashMap<>();
                    body.put("username", username.getText().toString());
                    body.put("password", password.getText().toString());
                    body.put("name", name.getText().toString());
                    body.put("phone", phone.getText().toString());
                    body.put("email", email.getText().toString());
                    call(api.register(body), x -> toast("注册成功，请登录"));
                }).show();
    }

    private void showPublishChooser() {
        new AlertDialog.Builder(this)
                .setTitle("发布")
                .setItems(new String[]{"发布帖子", "发布物品"}, (d, which) -> {
                    if (which == 0) showPublishArticle(); else showPublishItem();
                }).show();
    }

    private void showPublishArticle() {
        android.view.View form = getLayoutInflater().inflate(R.layout.dialog_publish_article, null);
        EditText title = form.findViewById(R.id.publish_article_title);
        EditText desc = form.findViewById(R.id.publish_article_desc);
        EditText img = form.findViewById(R.id.publish_article_img);
        EditText content = form.findViewById(R.id.publish_article_content);
        MaterialButton upload = form.findViewById(R.id.publish_article_upload);
        upload.setOnClickListener(v -> { uploadTarget = img; imagePicker.launch("image/*"); });
        new AlertDialog.Builder(this).setTitle("发布帖子")
                .setView(form)
                .setNegativeButton("取消", null)
                .setPositiveButton("发布", (d, w) -> {
                    Map<String, Object> body = new HashMap<>();
                    body.put("title", title.getText().toString());
                    body.put("description", desc.getText().toString());
                    body.put("img", img.getText().toString());
                    body.put("content", content.getText().toString());
                    body.put("userId", currentUser.id);
                    call(api.addArticle(body), x -> {
                        toast("已提交，等待管理员审核");
                        openMe();
                    });
                }).show();
    }

    private void showPublishItem() {
        android.view.View form = getLayoutInflater().inflate(R.layout.dialog_publish_item, null);
        EditText name = form.findViewById(R.id.publish_item_name);
        EditText desc = form.findViewById(R.id.publish_item_desc);
        EditText req = form.findViewById(R.id.publish_item_requirement);
        EditText img = form.findViewById(R.id.publish_item_img);
        Spinner categorySpinner = form.findViewById(R.id.publish_item_category);
        List<Category> categories = new ArrayList<>();
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new ArrayList<>());
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categoryAdapter.add("正在加载分类...");
        categorySpinner.setAdapter(categoryAdapter);
        call(api.categories(), loadedCategories -> {
            categories.clear();
            categoryAdapter.clear();
            if (loadedCategories == null || loadedCategories.isEmpty()) {
                categoryAdapter.add("暂无可选分类");
            } else {
                categories.addAll(loadedCategories);
                for (Category category : loadedCategories) categoryAdapter.add(category.name == null ? "未命名分类" : category.name);
            }
            categoryAdapter.notifyDataSetChanged();
        });
        MaterialButton upload = form.findViewById(R.id.publish_item_upload);
        upload.setOnClickListener(v -> { uploadTarget = img; imagePicker.launch("image/*"); });
        AlertDialog dialog = new AlertDialog.Builder(this).setTitle("发布物品")
                .setView(form)
                .setNegativeButton("取消", null)
                .setPositiveButton("发布", null)
                .create();
        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            int selected = categorySpinner.getSelectedItemPosition();
            if (selected < 0 || selected >= categories.size()) {
                toast("请先选择物品分类");
                return;
            }
            Map<String, Object> body = new HashMap<>();
            body.put("name", name.getText().toString());
            body.put("description", desc.getText().toString());
            body.put("requirement", req.getText().toString());
            body.put("img", img.getText().toString());
            body.put("userId", currentUser.id);
            body.put("status", false);
            body.put("categoryId", categories.get(selected).id);
            call(api.addItem(body), x -> {
                dialog.dismiss();
                toast("已提交，等待管理员审核");
                openMe();
            });
        }));
        dialog.show();
    }

    private void handlePickedImage(Uri uri) {
        if (uri == null || uploadTarget == null) return;
        try (InputStream in = getContentResolver().openInputStream(uri); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buf = new byte[8192];
            int len;
            while ((len = in.read(buf)) != -1) out.write(buf, 0, len);
            RequestBody body = RequestBody.create(MediaType.parse("image/*"), out.toByteArray());
            MultipartBody.Part part = MultipartBody.Part.createFormData("file", "upload.jpg", body);
            call(api.upload(part), url -> {
                uploadTarget.setText(url);
                toast("上传成功");
            });
        } catch (Exception e) {
            toast("upload failed: " + e.getMessage());
        }
    }

    private <T> void call(Call<ApiResponse<T>> call, Consumer<T> ok) {
        call.enqueue(new Callback<ApiResponse<T>>() {
            @Override
            public void onResponse(Call<ApiResponse<T>> call, Response<ApiResponse<T>> response) {
                ApiResponse<T> body = response.body();
                if (body != null && body.ok()) ok.accept(body.data);
                else toast(body == null ? "request failed" : body.msg);
            }

            @Override
            public void onFailure(Call<ApiResponse<T>> call, Throwable t) {
                toast("network error: " + t.getMessage());
            }
        });
    }

    private ScrollView scroll(LinearLayout child) {
        ScrollView s = new ScrollView(this);
        s.addView(child);
        return s;
    }

    private LinearLayout verticalPage() {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        l.setBackgroundColor(0xfff6f7f9);
        return l;
    }

    private LinearLayout formLayout() {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        l.setPadding(dp(16), dp(8), dp(16), dp(8));
        return l;
    }

    private TextView text(String s, int sp, boolean bold) {
        TextView t = new TextView(this);
        t.setText(s == null ? "" : s);
        t.setTextSize(sp);
        t.setTextColor(0xff222222);
        if (bold) t.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        t.setPadding(0, dp(4), 0, dp(4));
        return t;
    }

    private EditText input(String hint) {
        EditText e = new EditText(this);
        e.setHint(hint);
        e.setSingleLine(false);
        e.setBackgroundColor(0xffffffff);
        e.setPadding(dp(12), dp(8), dp(12), dp(8));
        return e;
    }

    private MaterialButton button(String s) {
        MaterialButton b = new MaterialButton(this);
        b.setText(s);
        b.setTextColor(0xffffffff);
        b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xff3f51b5));
        b.setStrokeColor(android.content.res.ColorStateList.valueOf(0xff3f51b5));
        b.setStrokeWidth(dp(1));
        return b;
    }

    private MaterialButton outlineButton(String s) {
        MaterialButton b = new MaterialButton(this);
        b.setText(s);
        b.setTextColor(0xff444444);
        b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xffffffff));
        b.setStrokeColor(android.content.res.ColorStateList.valueOf(0xffb0b0b0));
        b.setStrokeWidth(dp(1));
        return b;
    }

    private LinearLayout.LayoutParams topLp(int top) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2);
        p.setMargins(0, dp(top), 0, 0);
        return p;
    }

    private LinearLayout.LayoutParams lp(int w, int h) {
        return new LinearLayout.LayoutParams(w, h);
    }

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density + 0.5f);
    }

    private void applyStatusBarSpacing(View view, int extraTopDp) {
        if (view == null) return;
        int left = view.getPaddingLeft();
        int top = view.getPaddingTop();
        int right = view.getPaddingRight();
        int bottom = view.getPaddingBottom();
        view.setOnApplyWindowInsetsListener((v, insets) -> {
            v.setPadding(left, top + insets.getSystemWindowInsetTop() + dp(extraTopDp), right, bottom);
            return insets;
        });
        view.requestApplyInsets();
    }

    private void toast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }
}
