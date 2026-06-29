package com.example.campusgoodiessharingplatform;

import android.app.AlertDialog;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.campusgoodiessharingplatform.api.ApiClient;
import com.example.campusgoodiessharingplatform.api.ApiResponse;
import com.example.campusgoodiessharingplatform.api.ApiService;
import com.example.campusgoodiessharingplatform.model.Item;
import com.example.campusgoodiessharingplatform.model.User;
import com.google.android.material.button.MaterialButton;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.function.Consumer;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public abstract class BaseFragment extends Fragment {
    protected static final String ROLE_USER = "\u666e\u901a\u7528\u6237";
    protected static final String STATUS_PASS = "\u901a\u8fc7";
    protected static final String STATUS_PENDING = "\u5f85\u5ba1\u6838";
    protected static final String STATUS_REJECT = "\u62d2\u7edd";

    protected MainActivity host() {
        return (MainActivity) requireActivity();
    }

    protected ApiService api() {
        return host().api();
    }

    protected User currentUser() {
        return host().currentUser();
    }

    protected void setRoot(FrameLayout root, View child) {
        root.removeAllViews();
        root.addView(child, lp(-1, -1));
    }

    protected <T> void call(Call<ApiResponse<T>> call, Consumer<T> ok) {
        call(call, ok, this::toast);
    }

    protected <T> void call(Call<ApiResponse<T>> call, Consumer<T> ok, Consumer<String> fail) {
        call.enqueue(new Callback<ApiResponse<T>>() {
            @Override
            public void onResponse(Call<ApiResponse<T>> call, Response<ApiResponse<T>> response) {
                ApiResponse<T> body = response.body();
                if (body != null && body.ok()) ok.accept(body.data);
                else fail.accept(body == null ? "request failed: HTTP " + response.code() : body.msg);
            }

            @Override
            public void onFailure(Call<ApiResponse<T>> call, Throwable t) {
                fail.accept("network error: " + t.getMessage());
            }
        });
    }

    protected void uploadImage(Uri uri, EditText target) {
        try (InputStream in = requireContext().getContentResolver().openInputStream(uri);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buf = new byte[8192];
            int len;
            while ((len = in.read(buf)) != -1) out.write(buf, 0, len);
            RequestBody body = RequestBody.create(MediaType.parse("image/*"), out.toByteArray());
            MultipartBody.Part part = MultipartBody.Part.createFormData("file", "upload.jpg", body);
            call(api().upload(part), url -> {
                target.setText(url);
                toast("\u4e0a\u4f20\u6210\u529f");
            });
        } catch (Exception e) {
            toast("upload failed: " + e.getMessage());
        }
    }

    protected void showItemDetail(Item item) {
        LinearLayout box = formLayout();
        box.addView(image(item.img, 220));
        box.addView(text(item.name, 20, true));
        box.addView(text("\u5206\u7c7b: " + safe(item.categoryName) + " | \u6536\u85cf " + intValue(item.collectCount), 13, false));
        box.addView(text("\u5ba1\u6838: " + safe(item.checkStatus) + " | \u4e0a\u67b6: " + (Boolean.TRUE.equals(item.status) ? "\u5df2\u4e0a\u67b6" : "\u672a\u4e0a\u67b6"), 13, false));
        box.addView(text("\u63cf\u8ff0: " + safe(item.description), 15, false));
        box.addView(text("\u4ea4\u6362\u6761\u4ef6: " + safe(item.requirement), 15, false));
        box.addView(text("\u53d1\u5e03\u4eba: " + safe(item.userName), 13, false));
        new AlertDialog.Builder(requireContext()).setTitle("\u7269\u54c1\u8be6\u60c5").setView(scroll(box)).setPositiveButton("\u5173\u95ed", null).show();
    }

    protected ScrollView scroll(View child) {
        ScrollView s = new ScrollView(requireContext());
        s.addView(child);
        return s;
    }

    protected LinearLayout verticalPage() {
        LinearLayout l = new LinearLayout(requireContext());
        l.setOrientation(LinearLayout.VERTICAL);
        l.setBackgroundColor(0xfff6f7f9);
        return l;
    }

    protected LinearLayout row() {
        LinearLayout l = new LinearLayout(requireContext());
        l.setGravity(Gravity.CENTER_VERTICAL);
        l.setOrientation(LinearLayout.HORIZONTAL);
        return l;
    }

    protected LinearLayout formLayout() {
        LinearLayout l = new LinearLayout(requireContext());
        l.setOrientation(LinearLayout.VERTICAL);
        l.setPadding(dp(16), dp(8), dp(16), dp(8));
        return l;
    }

    protected LinearLayout listBox(LinearLayout page) {
        LinearLayout list = new LinearLayout(requireContext());
        list.setOrientation(LinearLayout.VERTICAL);
        list.setPadding(dp(12), 0, dp(12), dp(16));
        page.addView(list);
        return list;
    }

    protected TextView titleBar(String s) {
        TextView t = text(s, 24, true);
        t.setPadding(dp(16), dp(16), dp(16), dp(10));
        return t;
    }

    protected TextView sectionTitle(String s) {
        TextView t = text(s, 18, true);
        t.setPadding(dp(16), dp(16), dp(16), dp(8));
        return t;
    }

    protected TextView empty(String s) {
        TextView t = text(s, 16, false);
        t.setGravity(Gravity.CENTER);
        t.setPadding(0, dp(40), 0, dp(40));
        return t;
    }

    protected LinearLayout card() {
        LinearLayout c = new LinearLayout(requireContext());
        c.setOrientation(LinearLayout.VERTICAL);
        c.setPadding(dp(14), dp(14), dp(14), dp(14));
        c.setBackgroundColor(0xffffffff);
        c.setLayoutParams(topLp(10));
        return c;
    }

    protected View simpleCard(String title, String body) {
        LinearLayout c = card();
        c.addView(text(title, 16, true));
        c.addView(text(body, 13, false));
        return c;
    }

    protected ImageView image(String url, int height) {
        ImageView iv = new ImageView(requireContext());
        iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
        iv.setAdjustViewBounds(true);
        iv.setBackgroundColor(0xffeeeeee);
        iv.setLayoutParams(lp(-1, dp(height)));
        if (url != null && !url.isEmpty()) Glide.with(this).load(normalizeUrl(url)).into(iv);
        return iv;
    }

    protected View avatar(String url, int size) {
        FrameLayout wrap = new FrameLayout(requireContext());
        wrap.setPadding(0, dp(4), 0, dp(8));
        ImageView iv = new ImageView(requireContext());
        iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
        iv.setBackgroundColor(0xffeeeeee);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(dp(size), dp(size), Gravity.CENTER);
        wrap.addView(iv, params);
        if (url != null && !url.isEmpty()) Glide.with(this).load(normalizeUrl(url)).circleCrop().into(iv);
        return wrap;
    }

    protected TextView text(String s, int sp, boolean bold) {
        TextView t = new TextView(requireContext());
        t.setText(s == null ? "" : s);
        t.setTextSize(sp);
        t.setTextColor(0xff222222);
        if (bold) t.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        t.setPadding(0, dp(4), 0, dp(4));
        return t;
    }

    protected EditText input(String hint) {
        EditText e = new EditText(requireContext());
        e.setHint(hint);
        e.setSingleLine(false);
        e.setBackgroundColor(0xffffffff);
        e.setPadding(dp(12), dp(8), dp(12), dp(8));
        return e;
    }

    protected MaterialButton button(String s) {
        MaterialButton b = new MaterialButton(requireContext());
        b.setText(s);
        setTabSelected(b, true);
        return b;
    }

    protected MaterialButton outlineButton(String s) {
        MaterialButton b = new MaterialButton(requireContext());
        b.setText(s);
        setTabSelected(b, false);
        return b;
    }

    protected void setTabSelected(MaterialButton button, boolean selected) {
        button.setSelected(selected);
        button.setTextColor(selected ? 0xffffffff : 0xff444444);
        button.setBackgroundTintList(ColorStateList.valueOf(selected ? 0xff3f51b5 : 0xffffffff));
        button.setStrokeColor(ColorStateList.valueOf(selected ? 0xff3f51b5 : 0xffb0b0b0));
        button.setStrokeWidth(dp(1));
    }

    protected LinearLayout.LayoutParams topLp(int top) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2);
        p.setMargins(0, dp(top), 0, 0);
        return p;
    }

    protected LinearLayout.LayoutParams lp(int w, int h) {
        return new LinearLayout.LayoutParams(w, h);
    }

    protected int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density + 0.5f);
    }

    protected int intValue(Integer i) {
        return i == null ? 0 : i;
    }

    protected String safe(String s) {
        return s == null ? "" : s;
    }

    protected String stripImages(String html) {
        return safe(html).replaceAll("(?is)<img[^>]*>", "");
    }

    protected void addContentImages(LinearLayout box, String html) {
        java.util.regex.Matcher matcher = java.util.regex.Pattern
                .compile("<img[^>]+src=[\"']([^\"']+)[\"'][^>]*>", java.util.regex.Pattern.CASE_INSENSITIVE)
                .matcher(safe(html));
        while (matcher.find()) box.addView(image(matcher.group(1), 190), topLp(8));
    }

    protected String normalizeUrl(String url) {
        if (url == null) return null;
        String trimmed = url.trim();
        if (trimmed.isEmpty()) return trimmed;
        Uri baseUri = Uri.parse(ApiClient.BASE_URL);
        Uri parsed = trimmed.startsWith("/")
                ? Uri.parse(ApiClient.BASE_URL).buildUpon().encodedPath(trimmed).build()
                : Uri.parse(trimmed);
        if ("localhost".equals(parsed.getHost()) || "127.0.0.1".equals(parsed.getHost()) || parsed.getScheme() == null) {
            Uri.Builder builder = new Uri.Builder().scheme(baseUri.getScheme()).encodedAuthority(baseUri.getEncodedAuthority());
            for (String segment : parsed.getPathSegments()) builder.appendPath(segment);
            if (parsed.getEncodedQuery() != null) builder.encodedQuery(parsed.getEncodedQuery());
            return builder.build().toString();
        }
        Uri.Builder builder = new Uri.Builder().scheme(parsed.getScheme()).encodedAuthority(parsed.getEncodedAuthority());
        for (String segment : parsed.getPathSegments()) builder.appendPath(segment);
        if (parsed.getEncodedQuery() != null) builder.encodedQuery(parsed.getEncodedQuery());
        return builder.build().toString();
    }

    protected void toast(String s) {
        Toast.makeText(requireContext(), s, Toast.LENGTH_SHORT).show();
    }
}
