package com.example.campusgoodiessharingplatform;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.net.Uri;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
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
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public abstract class BaseFragment extends Fragment {
    protected static final String ROLE_USER = "普通用户";
    protected static final String STATUS_PASS = "通过";
    protected static final String STATUS_PENDING = "待审核";
    protected static final String STATUS_REJECT = "拒绝";

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
                toast("上传成功");
            });
        } catch (Exception e) {
            toast("upload failed: " + e.getMessage());
        }
    }

    protected void showItemDetail(Item item) {
        showItemDetail(item, null);
    }

    protected void showItemDetail(Item item, Runnable afterChange) {
        View detail = getLayoutInflater().inflate(R.layout.dialog_item_detail, null);
        ImageView image = detail.findViewById(R.id.item_detail_image);
        TextView name = detail.findViewById(R.id.item_detail_name);
        TextView meta = detail.findViewById(R.id.item_detail_meta);
        TextView audit = detail.findViewById(R.id.item_detail_audit);
        TextView description = detail.findViewById(R.id.item_detail_description);
        TextView requirement = detail.findViewById(R.id.item_detail_requirement);
        TextView owner = detail.findViewById(R.id.item_detail_owner);
        LinearLayout actions = detail.findViewById(R.id.item_detail_actions);
        MaterialButton collect = detail.findViewById(R.id.item_detail_collect);
        MaterialButton exchange = detail.findViewById(R.id.item_detail_exchange);
        if (item.img != null && !item.img.isEmpty()) Glide.with(this).load(normalizeUrl(item.img)).into(image);
        name.setText(safe(item.name));
        meta.setText("分类: " + safe(item.categoryName) + " | 收藏 " + intValue(item.collectCount));
        audit.setText("审核: " + safe(item.checkStatus) + " | 上架: " + (Boolean.TRUE.equals(item.status) ? "已上架" : "未上架"));
        description.setText(safe(item.description));
        requirement.setText(safe(item.requirement));
        owner.setText("发布人: " + safe(item.userName));
        styleActionButton(collect, item.collectId == null ? "收藏" : "取消收藏", item.collectId == null);
        if (item.userId == null || !item.userId.equals(currentUser().id)) {
            styleActionButton(exchange, "申请交换", true);
            exchange.setOnClickListener(v -> showExchangeDialog(item));
        } else {
            exchange.setVisibility(View.GONE);
        }
        AlertDialog dialog = new AlertDialog.Builder(requireContext()).setTitle("物品详情").setView(detail).setPositiveButton("关闭", null).show();
        collect.setOnClickListener(v -> toggleCollectInDetail(item, dialog, afterChange));
    }

    protected TextView requirementText(String value, int sp) {
        TextView t = text("", sp, true);
        String label = "交换条件";
        t.setText(styledRequirement(value));
        return t;
    }

    protected SpannableString styledRequirement(String value) {
        String label = "交换条件";
        SpannableString content = new SpannableString(label + ": " + safe(value));
        content.setSpan(new ForegroundColorSpan(0xff2f6f73), 0, label.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        content.setSpan(new StyleSpan(Typeface.BOLD), 0, label.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return content;
    }

    protected void styleActionButton(MaterialButton button, String text, boolean primary) {
        button.setText(text);
        button.setTextColor(primary ? 0xffffffff : 0xff3f51b5);
        button.setTextSize(16);
        button.setTypeface(Typeface.DEFAULT_BOLD);
        button.setCornerRadius(dp(16));
        button.setBackgroundTintList(ColorStateList.valueOf(primary ? 0xff3f51b5 : 0xffeef1ff));
        button.setStrokeWidth(0);
    }

    protected MaterialButton primaryActionButton(String s) {
        MaterialButton b = new MaterialButton(requireContext());
        styleActionButton(b, s, true);
        return b;
    }

    protected MaterialButton secondaryActionButton(String s) {
        MaterialButton b = new MaterialButton(requireContext());
        styleActionButton(b, s, false);
        return b;
    }

    private void toggleCollectInDetail(Item item, DialogInterface dialog, Runnable afterChange) {
        if (item.collectId == null) {
            Map<String, Object> body = new HashMap<>();
            body.put("userId", currentUser().id);
            body.put("itemId", item.id);
            call(api().collect(body), x -> {
                toast("已收藏");
                dialog.dismiss();
                if (afterChange != null) afterChange.run();
            });
        } else {
            call(api().uncollect(item.collectId), x -> {
                toast("已取消收藏");
                dialog.dismiss();
                if (afterChange != null) afterChange.run();
            });
        }
    }

    protected void showExchangeDialog(Item item) {
        View form = getLayoutInflater().inflate(R.layout.dialog_exchange, null);
        TextView title = form.findViewById(R.id.exchange_item_title);
        EditText content = form.findViewById(R.id.exchange_content);
        EditText remark = form.findViewById(R.id.exchange_remark);
        MaterialButton submit = form.findViewById(R.id.exchange_submit);
        title.setText("正在申请: " + safe(item.name));
        styleActionButton(submit, "提交", true);
        AlertDialog dialog = new AlertDialog.Builder(requireContext()).setTitle("申请交换").setView(form).create();
        submit.setOnClickListener(v -> {
            String contentValue = content.getText().toString().trim();
            String remarkValue = remark.getText().toString().trim();
            if (contentValue.isEmpty() || remarkValue.isEmpty()) {
                toast("请完整填写交换物品和理由");
                return;
            }
            Map<String, Object> body = new HashMap<>();
            body.put("itemId", item.id);
            body.put("itemUserid", item.userId);
            body.put("userId", currentUser().id);
            body.put("content", contentValue);
            body.put("remark", remarkValue);
            call(api().addCharge(body), x -> {
                dialog.dismiss();
                toast("申请已提交");
            });
        });
        dialog.show();
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
        c.setPadding(dp(12), dp(12), dp(12), dp(14));
        c.setBackgroundResource(R.drawable.bg_soft_card);
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

    protected ImageView cardImage(String url, int height) {
        ImageView iv = new ImageView(requireContext());
        iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
        iv.setBackgroundColor(0xffffffff);
        iv.setLayoutParams(lp(-1, dp(height)));
        if (url != null && !url.isEmpty()) Glide.with(this).load(normalizeUrl(url)).centerCrop().into(iv);
        return iv;
    }

    protected ImageView thumbnailImage(String url, int size) {
        ImageView iv = new ImageView(requireContext());
        iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
        iv.setBackgroundColor(0xffeeeeee);
        iv.setLayoutParams(lp(dp(size), dp(size)));
        if (url != null && !url.isEmpty()) Glide.with(this).load(normalizeUrl(url)).centerCrop().into(iv);
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

    protected ImageView roundImage(String url, int size) {
        ImageView iv = new ImageView(requireContext());
        iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
        iv.setBackgroundColor(0xffeeeeee);
        iv.setLayoutParams(lp(dp(size), dp(size)));
        if (url != null && !url.isEmpty()) Glide.with(this).load(normalizeUrl(url)).circleCrop().into(iv);
        return iv;
    }

    protected TextView text(String s, int sp, boolean bold) {
        TextView t = new TextView(requireContext());
        t.setText(s == null ? "" : s);
        t.setTextSize(sp);
        t.setTextColor(0xff111827);
        if (bold) t.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        t.setPadding(0, dp(3), 0, dp(3));
        return t;
    }

    protected EditText input(String hint) {
        EditText e = new EditText(requireContext());
        e.setHint(hint);
        e.setSingleLine(false);
        e.setBackgroundResource(R.drawable.bg_soft_input);
        e.setPadding(dp(16), dp(10), dp(16), dp(10));
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
