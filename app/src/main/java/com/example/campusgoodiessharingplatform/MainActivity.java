package com.example.campusgoodiessharingplatform;

import android.app.AlertDialog;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.view.Gravity;
import android.view.View;
import android.widget.*;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.campusgoodiessharingplatform.api.*;
import com.example.campusgoodiessharingplatform.model.*;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.*;
import java.util.function.Consumer;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private static final String ROLE_USER = "\u666e\u901a\u7528\u6237";
    private static final String STATUS_PASS = "\u901a\u8fc7";
    private static final String STATUS_PENDING = "\u5f85\u5ba1\u6838";
    private static final String STATUS_REJECT = "\u62d2\u7edd";
    private final ApiService api = ApiClient.service();
    private final Gson gson = new Gson();
    private User currentUser;
    private FrameLayout container;
    private boolean homeItems = true;
    private String searchKeyword;
    private EditText uploadTarget;
    private ActivityResultLauncher<String> imagePicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imagePicker = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null && uploadTarget != null) uploadImage(uri, uploadTarget);
        });
        String saved = getSharedPreferences("session", MODE_PRIVATE).getString("user", null);
        if (saved != null) currentUser = gson.fromJson(saved, User.class);
        if (currentUser == null) showLogin(); else showMain();
    }

    private void showLogin() {
        LinearLayout root = verticalPage();
        root.setGravity(Gravity.CENTER);
        root.setPadding(dp(28), dp(28), dp(28), dp(28));
        TextView title = text("\u6821\u56ed\u597d\u7269", 30, true);
        title.setGravity(Gravity.CENTER);
        TextView sub = text("\u767b\u5f55\u540e\u53d1\u73b0\u3001\u5206\u4eab\u3001\u4ea4\u6362\u6821\u56ed\u597d\u7269", 14, false);
        sub.setGravity(Gravity.CENTER);
        EditText username = input("\u8d26\u53f7");
        EditText password = input("\u5bc6\u7801");
        password.setInputType(0x00000081);
        MaterialButton login = button("\u767b\u5f55");
        MaterialButton register = outlineButton("\u6ce8\u518c\u65b0\u7528\u6237");
        root.addView(title, lp(-1, -2));
        root.addView(sub, lp(-1, -2));
        root.addView(username, topLp(16));
        root.addView(password, topLp(10));
        root.addView(login, topLp(16));
        root.addView(register, topLp(8));
        setContentView(root);
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
            getSharedPreferences("session", MODE_PRIVATE).edit().putString("user", gson.toJson(user)).apply();
            showMain();
        });
    }

    private void showRegisterDialog() {
        LinearLayout form = formLayout();
        EditText username = input("\u8d26\u53f7");
        EditText password = input("\u5bc6\u7801");
        EditText name = input("\u6635\u79f0");
        EditText phone = input("\u624b\u673a\u53f7");
        EditText email = input("\u90ae\u7bb1");
        form.addView(username); form.addView(password); form.addView(name); form.addView(phone); form.addView(email);
        new AlertDialog.Builder(this).setTitle("\u6ce8\u518c")
                .setView(form).setNegativeButton("\u53d6\u6d88", null)
                .setPositiveButton("\u6ce8\u518c", (d, w) -> {
                    Map<String, Object> body = new HashMap<>();
                    body.put("username", username.getText().toString());
                    body.put("password", password.getText().toString());
                    body.put("name", name.getText().toString());
                    body.put("phone", phone.getText().toString());
                    body.put("email", email.getText().toString());
                    call(api.register(body), x -> toast("\u6ce8\u518c\u6210\u529f\uff0c\u8bf7\u767b\u5f55"));
                }).show();
    }

    private void showMain() {
        setContentView(R.layout.activity_main);
        container = findViewById(R.id.content_container);
        findViewById(R.id.nav_home).setOnClickListener(v -> showHome());
        findViewById(R.id.nav_hot).setOnClickListener(v -> showHot());
        findViewById(R.id.nav_publish).setOnClickListener(v -> showPublishChooser());
        findViewById(R.id.nav_messages).setOnClickListener(v -> showMessages());
        findViewById(R.id.nav_me).setOnClickListener(v -> showMe());
        showHome();
    }

    private void showHome() {
        LinearLayout page = verticalPage();
        LinearLayout header = row();
        header.setPadding(dp(16), dp(14), dp(16), dp(8));
        header.addView(text("\u9996\u9875", 24, true), new LinearLayout.LayoutParams(0, -2, 1));
        MaterialButton search = outlineButton("\u641c\u7d22");
        header.addView(search);
        page.addView(header);
        LinearLayout tabs = row();
        tabs.setPadding(dp(16), 0, dp(16), dp(10));
        MaterialButton market = homeItems ? button("\u5e02\u96c6") : outlineButton("\u5e02\u96c6");
        MaterialButton share = homeItems ? outlineButton("\u5206\u4eab") : button("\u5206\u4eab");
        tabs.addView(market, new LinearLayout.LayoutParams(0, dp(44), 1));
        tabs.addView(share, new LinearLayout.LayoutParams(0, dp(44), 1));
        page.addView(tabs);
        LinearLayout list = listBox(page);
        setPage(scroll(page));
        market.setOnClickListener(v -> { homeItems = true; searchKeyword = null; showHome(); });
        share.setOnClickListener(v -> { homeItems = false; searchKeyword = null; showHome(); });
        search.setOnClickListener(v -> showSearchDialog());
        if (homeItems) loadItems(list, null, null, searchKeyword, false); else loadArticles(list, null, searchKeyword, false);
    }

    private void showSearchDialog() {
        EditText input = input(homeItems ? "\u641c\u7d22\u7269\u54c1" : "\u641c\u7d22\u5e16\u5b50");
        new AlertDialog.Builder(this).setTitle("\u641c\u7d22").setView(input)
                .setNegativeButton("\u53d6\u6d88", null)
                .setPositiveButton("\u641c\u7d22", (d, w) -> { searchKeyword = input.getText().toString(); showHome(); }).show();
    }

    private void loadItems(LinearLayout list, Integer userId, Integer categoryId, String keyword, boolean onlyCollected) {
        list.removeAllViews();
        list.addView(text("loading...", 14, false));
        call(api.itemPage(1, 50, keyword, true, STATUS_PASS, categoryId, userId, currentUser.id), page -> {
            list.removeAllViews();
            if (page.list == null || page.list.isEmpty()) list.addView(empty("\u6682\u65e0\u7269\u54c1"));
            else for (Item item : page.list) if (!onlyCollected || item.collectId != null) list.addView(itemCard(item));
        });
    }

    private void loadArticles(LinearLayout list, Integer userId, String keyword, boolean hotSort) {
        list.removeAllViews();
        list.addView(text("loading...", 14, false));
        call(api.articlePage(1, 50, keyword, STATUS_PASS, userId, currentUser.id), page -> {
            list.removeAllViews();
            List<Article> articles = page.list == null ? new ArrayList<>() : page.list;
            if (hotSort) articles.sort((a, b) -> intValue(b.likeCount) - intValue(a.likeCount));
            if (articles.isEmpty()) list.addView(empty("\u6682\u65e0\u5e16\u5b50"));
            else for (Article article : articles) list.addView(articleCard(article));
        });
    }

    private void loadHotArticles(LinearLayout list) {
        list.removeAllViews();
        list.addView(text("loading...", 14, false));
        call(api.articlePage(1, 50, null, STATUS_PASS, null, currentUser.id), page -> {
            list.removeAllViews();
            List<Article> articles = page.list == null ? new ArrayList<>() : page.list;
            articles.sort((a, b) -> intValue(b.likeCount) - intValue(a.likeCount));
            if (articles.isEmpty()) list.addView(empty("\u6682\u65e0\u5e16\u5b50"));
            else for (Article article : articles) list.addView(hotArticleRow(article));
        });
    }

    private View itemCard(Item item) {
        LinearLayout card = card();
        card.addView(image(item.img, 190));
        card.addView(text(item.name, 18, true));
        card.addView(text((item.categoryName == null ? "\u672a\u5206\u7c7b" : item.categoryName) + "  |  \u6536\u85cf " + intValue(item.collectCount), 12, false));
        card.addView(text(item.description, 14, false));
        card.addView(text("\u4ea4\u6362\u6761\u4ef6: " + safe(item.requirement), 13, false));
        LinearLayout actions = row();
        MaterialButton collect = item.collectId == null ? outlineButton("\u6536\u85cf") : button("\u53d6\u6d88\u6536\u85cf");
        MaterialButton detail = outlineButton("\u8be6\u60c5");
        MaterialButton exchange = outlineButton("\u7533\u8bf7\u4ea4\u6362");
        actions.addView(collect, new LinearLayout.LayoutParams(0, dp(42), 1));
        actions.addView(detail, new LinearLayout.LayoutParams(0, dp(42), 1));
        if (item.userId == null || !item.userId.equals(currentUser.id)) {
            actions.addView(exchange, new LinearLayout.LayoutParams(0, dp(42), 1));
        }
        card.addView(actions);
        collect.setOnClickListener(v -> toggleCollect(item));
        detail.setOnClickListener(v -> showItemDetail(item));
        exchange.setOnClickListener(v -> showExchangeDialog(item));
        return card;
    }

    private View articleCard(Article article) {
        LinearLayout card = card();
        card.addView(image(article.img, 160));
        card.addView(text(article.title, 18, true));
        card.addView(text("\u53d1\u5e03\u4eba: " + safe(article.userName) + "  |  \u8d5e " + intValue(article.likeCount) + "  \u8bc4\u8bba " + intValue(article.commentCount), 12, false));
        card.addView(text(article.description, 14, false));
        LinearLayout actions = row();
        MaterialButton like = article.likedId == null ? outlineButton("\u70b9\u8d5e") : button("\u53d6\u6d88\u70b9\u8d5e");
        MaterialButton detail = outlineButton("\u8be6\u60c5/\u8bc4\u8bba");
        actions.addView(like, new LinearLayout.LayoutParams(0, dp(42), 1));
        actions.addView(detail, new LinearLayout.LayoutParams(0, dp(42), 1));
        card.addView(actions);
        like.setOnClickListener(v -> toggleLike(article));
        detail.setOnClickListener(v -> showArticleDetail(article));
        return card;
    }

    private View hotArticleRow(Article article) {
        LinearLayout row = row();
        row.setPadding(dp(12), dp(10), dp(12), dp(10));
        row.setBackgroundColor(0xffffffff);
        row.setLayoutParams(topLp(1));
        TextView title = text(article.title, 15, true);
        title.setSingleLine(true);
        title.setEllipsize(android.text.TextUtils.TruncateAt.END);
        TextView heat = text("\u70ed\u5ea6 " + intValue(article.likeCount), 13, false);
        heat.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        row.addView(title, new LinearLayout.LayoutParams(0, dp(36), 1));
        row.addView(heat, new LinearLayout.LayoutParams(dp(88), dp(36)));
        row.setOnClickListener(v -> showArticleDetail(article));
        return row;
    }

    private void toggleLike(Article article) {
        if (article.likedId == null) {
            Map<String, Object> body = new HashMap<>();
            body.put("userId", currentUser.id); body.put("articleId", article.id);
            call(api.like(body), x -> { toast("\u5df2\u70b9\u8d5e"); showHome(); });
        } else call(api.unlike(article.likedId), x -> { toast("\u5df2\u53d6\u6d88"); showHome(); });
    }

    private void toggleCollect(Item item) {
        if (item.collectId == null) {
            Map<String, Object> body = new HashMap<>();
            body.put("userId", currentUser.id); body.put("itemId", item.id);
            call(api.collect(body), x -> { toast("\u5df2\u6536\u85cf"); showHome(); });
        } else call(api.uncollect(item.collectId), x -> { toast("\u5df2\u53d6\u6d88\u6536\u85cf"); showHome(); });
    }

    private void showArticleDetail(Article article) {
        call(api.articleById(article.id, currentUser.id), latest -> renderArticleDetail(latest == null ? article : latest));
    }

    private void renderArticleDetail(Article article) {
        LinearLayout page = verticalPage();
        LinearLayout header = row();
        header.setPadding(dp(16), dp(12), dp(16), dp(8));
        MaterialButton back = outlineButton("\u8fd4\u56de");
        header.addView(back, new LinearLayout.LayoutParams(dp(88), dp(42)));
        header.addView(text("\u5e16\u5b50\u8be6\u60c5", 22, true), new LinearLayout.LayoutParams(0, -2, 1));
        page.addView(header);
        LinearLayout box = formLayout();
        box.setBackgroundColor(0xffffffff);
        box.addView(image(article.img, 210));
        box.addView(text(article.title, 21, true));
        box.addView(text("\u53d1\u5e03\u65f6\u95f4: " + safe(article.time), 12, false));
        box.addView(text("\u8d5e " + intValue(article.likeCount) + " | \u8bc4\u8bba " + intValue(article.commentCount), 13, false));
        TextView content = text("", 15, false);
        String articleContent = article.content == null ? safe(article.description) : article.content;
        content.setText(Html.fromHtml(stripImages(articleContent), Html.FROM_HTML_MODE_LEGACY));
        box.addView(sectionTitle("\u6587\u7ae0\u6b63\u6587"));
        addContentImages(box, articleContent);
        box.addView(content);
        page.addView(box);
        LinearLayout commentBox = formLayout();
        commentBox.setBackgroundColor(0xfff6f7f9);
        commentBox.addView(sectionTitle("\u8bc4\u8bba\u533a"));
        EditText comment = input("\u5199\u8bc4\u8bba");
        MaterialButton send = button("\u53d1\u5e03\u8bc4\u8bba");
        commentBox.addView(comment, topLp(6));
        commentBox.addView(send, topLp(8));
        LinearLayout comments = new LinearLayout(this);
        comments.setOrientation(LinearLayout.VERTICAL);
        commentBox.addView(comments, topLp(10));
        page.addView(commentBox);
        setPage(scroll(page));
        back.setOnClickListener(v -> showHome());
        send.setOnClickListener(v -> {
            String contentValue = comment.getText().toString().trim();
            if (contentValue.length() < 2) {
                toast("\u8bc4\u8bba\u81f3\u5c11\u9700\u89812\u4e2a\u5b57");
                return;
            }
            Map<String, Object> body = new HashMap<>();
            body.put("userId", currentUser.id); body.put("articleId", article.id); body.put("content", contentValue);
            call(api.addComment(body), x -> { toast("\u8bc4\u8bba\u6210\u529f"); showArticleDetail(article); });
        });
        loadArticleComments(comments, article);
    }

    private void loadArticleComments(LinearLayout comments, Article article) {
        call(api.comments(1, 20, article.id), page -> {
            comments.removeAllViews();
            if (page.list == null || page.list.isEmpty()) comments.addView(empty("\u6682\u65e0\u8bc4\u8bba"));
            else for (Comment c : page.list) comments.addView(commentCard(c, article));
        });
    }

    private View commentCard(Comment c, Article article) {
        LinearLayout card = card();
        LinearLayout top = row();
        top.addView(image(c.avatar, 34), new LinearLayout.LayoutParams(dp(34), dp(34)));
        top.addView(text(safe(c.userName) + "  ID: " + (c.userId == null ? "" : c.userId), 14, true), new LinearLayout.LayoutParams(0, -2, 1));
        card.addView(top);
        card.addView(text(safe(c.content), 14, false));
        card.addView(text(safe(c.time), 11, false));
        if (currentUser.id != null && currentUser.id.equals(c.userId)) {
            card.setOnLongClickListener(v -> {
                new AlertDialog.Builder(this)
                        .setTitle("\u5220\u9664\u8bc4\u8bba")
                        .setMessage("\u786e\u5b9a\u5220\u9664\u8fd9\u6761\u8bc4\u8bba\u5417\uff1f")
                        .setNegativeButton("\u53d6\u6d88", null)
                        .setPositiveButton("\u5220\u9664", (d, w) -> call(api.deleteComment(c.id), x -> { toast("\u5df2\u5220\u9664"); showArticleDetail(article); }))
                        .show();
                return true;
            });
        }
        return card;
    }

    private void showItemDetail(Item item) {
        LinearLayout box = formLayout();
        box.addView(image(item.img, 220));
        box.addView(text(item.name, 20, true));
        box.addView(text("\u5206\u7c7b: " + safe(item.categoryName) + " | \u6536\u85cf " + intValue(item.collectCount), 13, false));
        box.addView(text("\u5ba1\u6838: " + safe(item.checkStatus) + " | \u4e0a\u67b6: " + (Boolean.TRUE.equals(item.status) ? "\u5df2\u4e0a\u67b6" : "\u672a\u4e0a\u67b6"), 13, false));
        box.addView(text("\u63cf\u8ff0: " + safe(item.description), 15, false));
        box.addView(text("\u4ea4\u6362\u6761\u4ef6: " + safe(item.requirement), 15, false));
        box.addView(text("\u53d1\u5e03\u4eba: " + safe(item.userName), 13, false));
        new AlertDialog.Builder(this).setTitle("\u7269\u54c1\u8be6\u60c5").setView(scroll(box)).setPositiveButton("\u5173\u95ed", null).show();
    }

    private void showExchangeDialog(Item item) {
        LinearLayout form = formLayout();
        EditText content = input("\u6211\u63d0\u4f9b\u7684\u4ea4\u6362\u7269\u54c1");
        EditText remark = input("\u4ea4\u6362\u7406\u7531");
        form.addView(text(item.name, 18, true));
        form.addView(content, topLp(8));
        form.addView(remark, topLp(8));
        new AlertDialog.Builder(this).setTitle("\u7533\u8bf7\u4ea4\u6362")
                .setView(form)
                .setNegativeButton("\u53d6\u6d88", null)
                .setPositiveButton("\u63d0\u4ea4", (d, w) -> {
                    String contentValue = content.getText().toString().trim();
                    String remarkValue = remark.getText().toString().trim();
                    if (contentValue.isEmpty() || remarkValue.isEmpty()) {
                        toast("\u8bf7\u5b8c\u6574\u586b\u5199\u4ea4\u6362\u7269\u54c1\u548c\u7406\u7531");
                        return;
                    }
                    Map<String, Object> body = new HashMap<>();
                    body.put("itemId", item.id);
                    body.put("itemUserid", item.userId);
                    body.put("userId", currentUser.id);
                    body.put("content", contentValue);
                    body.put("remark", remarkValue);
                    call(api.addCharge(body), x -> toast("\u7533\u8bf7\u5df2\u63d0\u4ea4"));
                }).show();
    }

    private void showHot() {
        LinearLayout page = verticalPage();
        page.addView(titleBar("\u70ed\u95e8"));
        LinearLayout notices = listBox(page);
        page.addView(sectionTitle("\u5e16\u5b50\u70b9\u8d5e\u699c"));
        LinearLayout rank = listBox(page);
        setPage(scroll(page));
        call(api.notices(1, 5), p -> { notices.removeAllViews(); if (p.list != null) for (Notice n : p.list) notices.addView(simpleCard(n.title, safe(n.content) + "\n" + safe(n.time))); });
        loadHotArticles(rank);
    }

    private void showMessages() {
        LinearLayout page = verticalPage();
        LinearLayout header = row(); header.setPadding(dp(16), dp(14), dp(16), dp(8));
        header.addView(text("\u4fe1\u606f", 24, true), new LinearLayout.LayoutParams(0, -2, 1));
        MaterialButton readAll = outlineButton("\u5168\u90e8\u5df2\u8bfb"); header.addView(readAll); page.addView(header);
        LinearLayout tabs = row(); tabs.setPadding(dp(12), 0, dp(12), dp(10));
        MaterialButton received = button("\u6536\u5230\u7684\u7533\u8bf7");
        MaterialButton sent = outlineButton("\u6211\u7684\u7533\u8bf7");
        tabs.addView(received, new LinearLayout.LayoutParams(0, dp(42), 1));
        tabs.addView(sent, new LinearLayout.LayoutParams(0, dp(42), 1));
        page.addView(tabs);
        LinearLayout list = listBox(page); setPage(scroll(page));
        readAll.setOnClickListener(v -> call(api.readAll(currentUser.id), x -> showMessages()));
        received.setOnClickListener(v -> showReceivedCharges());
        sent.setOnClickListener(v -> showSentCharges());
        call(api.notifications(1, 50, currentUser.id), p -> {
            list.removeAllViews();
            if (p.list == null || p.list.isEmpty()) list.addView(empty("\u6682\u65e0\u4fe1\u606f"));
            else for (AppNotification n : p.list) {
                View card = simpleCard((Boolean.TRUE.equals(n.isRead) ? "" : "\u672a\u8bfb | ") + safe(n.content), safe(n.time));
                card.setOnClickListener(v -> call(api.readNotification(n.id), x -> showMessages()));
                list.addView(card);
            }
        });
    }

    private void showReceivedCharges() {
        LinearLayout page = verticalPage(); page.addView(titleBar("\u6536\u5230\u7684\u4ea4\u6362\u7533\u8bf7"));
        LinearLayout list = listBox(page); setPage(scroll(page));
        call(api.chargePage(1, 50, null, currentUser.id, null), p -> {
            list.removeAllViews();
            if (p.list == null || p.list.isEmpty()) list.addView(empty("\u6682\u65e0\u7533\u8bf7"));
            else for (Charge charge : p.list) list.addView(receivedChargeCard(charge));
        });
    }

    private void showSentCharges() {
        LinearLayout page = verticalPage(); page.addView(titleBar("\u6211\u7684\u4ea4\u6362\u7533\u8bf7"));
        LinearLayout list = listBox(page); setPage(scroll(page));
        call(api.chargePage(1, 50, currentUser.id, null, null), p -> {
            list.removeAllViews();
            if (p.list == null || p.list.isEmpty()) list.addView(empty("\u6682\u65e0\u7533\u8bf7"));
            else for (Charge charge : p.list) list.addView(sentChargeCard(charge));
        });
    }

    private View receivedChargeCard(Charge charge) {
        LinearLayout card = card();
        card.addView(text(safe(charge.itemName), 17, true));
        card.addView(text("\u7533\u8bf7\u4eba: " + safe(charge.userName) + " | \u72b6\u6001: " + safe(charge.status), 13, false));
        card.addView(text("\u4ea4\u6362\u7269\u54c1: " + safe(charge.content), 14, false));
        card.addView(text("\u7406\u7531: " + safe(charge.remark), 14, false));
        if (STATUS_PASS.equals(charge.status)) card.addView(text("\u5730\u70b9: " + safe(charge.location) + " | \u65f6\u95f4: " + safe(charge.shareTime), 13, false));
        if (STATUS_REJECT.equals(charge.status)) card.addView(text("\u62d2\u7edd\u7406\u7531: " + safe(charge.reason), 13, false));
        if (STATUS_PENDING.equals(charge.status)) {
            LinearLayout actions = row();
            MaterialButton approve = button("\u901a\u8fc7");
            MaterialButton reject = outlineButton("\u62d2\u7edd");
            actions.addView(approve, new LinearLayout.LayoutParams(0, dp(42), 1));
            actions.addView(reject, new LinearLayout.LayoutParams(0, dp(42), 1));
            card.addView(actions, topLp(8));
            approve.setOnClickListener(v -> showApproveChargeDialog(charge));
            reject.setOnClickListener(v -> showRejectChargeDialog(charge));
        }
        return card;
    }

    private View sentChargeCard(Charge charge) {
        LinearLayout card = card();
        card.addView(text(safe(charge.itemName), 17, true));
        card.addView(text("\u7269\u54c1\u4e3b\u4eba: " + safe(charge.itemUserName) + " | \u72b6\u6001: " + safe(charge.status), 13, false));
        card.addView(text("\u6211\u63d0\u4f9b: " + safe(charge.content), 14, false));
        card.addView(text("\u7406\u7531: " + safe(charge.remark), 14, false));
        if (STATUS_PASS.equals(charge.status)) card.addView(text("\u4ea4\u6362\u5730\u70b9: " + safe(charge.location) + "\n\u4ea4\u6362\u65f6\u95f4: " + safe(charge.shareTime), 13, false));
        if (STATUS_REJECT.equals(charge.status)) card.addView(text("\u62d2\u7edd\u7406\u7531: " + safe(charge.reason), 13, false));
        return card;
    }

    private void showApproveChargeDialog(Charge charge) {
        LinearLayout form = formLayout();
        EditText location = input("\u4ea4\u6362\u5730\u70b9");
        EditText shareTime = input("\u4ea4\u6362\u65f6\u95f4");
        form.addView(location); form.addView(shareTime);
        new AlertDialog.Builder(this).setTitle("\u901a\u8fc7\u7533\u8bf7").setView(form)
                .setNegativeButton("\u53d6\u6d88", null)
                .setPositiveButton("\u786e\u5b9a", (d, w) -> {
                    if (location.getText().toString().trim().isEmpty() || shareTime.getText().toString().trim().isEmpty()) {
                        toast("\u4ea4\u6362\u5730\u70b9\u548c\u65f6\u95f4\u4e0d\u80fd\u4e3a\u7a7a");
                        return;
                    }
                    Map<String, Object> body = new HashMap<>();
                    body.put("id", charge.id);
                    body.put("status", STATUS_PASS);
                    body.put("location", location.getText().toString().trim());
                    body.put("shareTime", shareTime.getText().toString().trim());
                    call(api.updateCharge(body), x -> { toast("\u5df2\u901a\u8fc7"); showReceivedCharges(); });
                }).show();
    }

    private void showRejectChargeDialog(Charge charge) {
        EditText reason = input("\u62d2\u7edd\u7406\u7531");
        new AlertDialog.Builder(this).setTitle("\u62d2\u7edd\u7533\u8bf7").setView(reason)
                .setNegativeButton("\u53d6\u6d88", null)
                .setPositiveButton("\u786e\u5b9a", (d, w) -> {
                    String value = reason.getText().toString().trim();
                    if (value.isEmpty()) {
                        toast("\u62d2\u7edd\u7406\u7531\u4e0d\u80fd\u4e3a\u7a7a");
                        return;
                    }
                    Map<String, Object> body = new HashMap<>();
                    body.put("id", charge.id);
                    body.put("status", STATUS_REJECT);
                    body.put("reason", value);
                    call(api.updateCharge(body), x -> { toast("\u5df2\u62d2\u7edd"); showReceivedCharges(); });
                }).show();
    }

    private void showPublishChooser() {
        new AlertDialog.Builder(this).setTitle("\u53d1\u5e03").setItems(new String[]{"\u53d1\u5e03\u5e16\u5b50", "\u53d1\u5e03\u7269\u54c1"}, (d, which) -> { if (which == 0) showPublishArticle(); else showPublishItem(); }).show();
    }

    private void showPublishArticle() {
        LinearLayout form = formLayout();
        EditText title = input("\u6807\u9898"), desc = input("\u7b80\u4ecb"), img = input("\u5c01\u9762\u56fe\u7247 URL"), content = input("\u6b63\u6587\u5185\u5bb9");
        content.setMinLines(5); MaterialButton upload = outlineButton("\u4e0a\u4f20\u5c01\u9762\u56fe\u7247");
        form.addView(title); form.addView(desc); form.addView(img); form.addView(upload); form.addView(content);
        upload.setOnClickListener(v -> { uploadTarget = img; imagePicker.launch("image/*"); });
        new AlertDialog.Builder(this).setTitle("\u53d1\u5e03\u5e16\u5b50").setView(scroll(form)).setNegativeButton("\u53d6\u6d88", null).setPositiveButton("\u53d1\u5e03", (d, w) -> {
            Map<String, Object> body = new HashMap<>(); body.put("title", title.getText().toString()); body.put("description", desc.getText().toString()); body.put("img", img.getText().toString()); body.put("content", content.getText().toString()); body.put("userId", currentUser.id);
            call(api.addArticle(body), x -> { toast("\u5df2\u63d0\u4ea4\uff0c\u7b49\u5f85\u7ba1\u7406\u5458\u5ba1\u6838"); showMineArticles(); });
        }).show();
    }

    private void showPublishItem() {
        LinearLayout form = formLayout();
        EditText name = input("\u7269\u54c1\u540d\u79f0"), desc = input("\u63cf\u8ff0"), req = input("\u4ea4\u6362\u6761\u4ef6"), img = input("\u56fe\u7247 URL"), cat = input("\u5206\u7c7bID");
        MaterialButton upload = outlineButton("\u4e0a\u4f20\u7269\u54c1\u56fe\u7247");
        form.addView(name); form.addView(desc); form.addView(req); form.addView(img); form.addView(upload); form.addView(cat);
        upload.setOnClickListener(v -> { uploadTarget = img; imagePicker.launch("image/*"); });
        new AlertDialog.Builder(this).setTitle("\u53d1\u5e03\u7269\u54c1").setView(scroll(form)).setNegativeButton("\u53d6\u6d88", null).setPositiveButton("\u53d1\u5e03", (d, w) -> {
            Map<String, Object> body = new HashMap<>(); body.put("name", name.getText().toString()); body.put("description", desc.getText().toString()); body.put("requirement", req.getText().toString()); body.put("img", img.getText().toString()); body.put("userId", currentUser.id); body.put("status", false);
            try { body.put("categoryId", Integer.parseInt(cat.getText().toString())); } catch (Exception ignored) {}
            call(api.addItem(body), x -> { toast("\u5df2\u63d0\u4ea4\uff0c\u7b49\u5f85\u7ba1\u7406\u5458\u5ba1\u6838"); showMineItems(); });
        }).show();
    }

    private void uploadImage(Uri uri, EditText target) {
        try (InputStream in = getContentResolver().openInputStream(uri); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buf = new byte[8192]; int len; while ((len = in.read(buf)) != -1) out.write(buf, 0, len);
            RequestBody body = RequestBody.create(MediaType.parse("image/*"), out.toByteArray());
            MultipartBody.Part part = MultipartBody.Part.createFormData("file", "upload.jpg", body);
            call(api.upload(part), url -> { target.setText(url); toast("\u4e0a\u4f20\u6210\u529f"); });
        } catch (Exception e) { toast("upload failed: " + e.getMessage()); }
    }

    private void showMe() {
        LinearLayout page = verticalPage(); page.addView(titleBar("\u6211\u7684"));
        page.addView(avatar(currentUser.avatar, 76), topLp(4));
        page.addView(simpleCard(safe(currentUser.name), "\u8d26\u53f7: " + safe(currentUser.username) + "\n\u624b\u673a: " + safe(currentUser.phone) + "\n\u90ae\u7bb1: " + safe(currentUser.email)));
        MaterialButton profile = button("\u7f16\u8f91\u4e2a\u4eba\u4fe1\u606f"), myArticles = outlineButton("\u6211\u7684\u5e16\u5b50"), myItems = outlineButton("\u6211\u7684\u7269\u54c1"), collects = outlineButton("\u6211\u7684\u6536\u85cf\u5939"), logout = outlineButton("\u9000\u51fa\u767b\u5f55");
        page.addView(profile, topLp(8)); page.addView(myArticles, topLp(8)); page.addView(myItems, topLp(8)); page.addView(collects, topLp(8)); page.addView(logout, topLp(16)); setPage(scroll(page));
        profile.setOnClickListener(v -> showProfileDialog());
        myArticles.setOnClickListener(v -> showMineArticles()); myItems.setOnClickListener(v -> showMineItems()); collects.setOnClickListener(v -> showCollects());
        logout.setOnClickListener(v -> { getSharedPreferences("session", MODE_PRIVATE).edit().clear().apply(); currentUser = null; showLogin(); });
    }


    private void showProfileDialog() {
        LinearLayout form = formLayout();
        EditText name = input("\u6635\u79f0");
        EditText avatar = input("\u5934\u50cf URL");
        EditText phone = input("\u624b\u673a\u53f7");
        EditText email = input("\u90ae\u7bb1");
        MaterialButton uploadAvatar = outlineButton("\u9009\u62e9\u672c\u5730\u5934\u50cf");
        name.setText(safe(currentUser.name));
        avatar.setText(safe(currentUser.avatar));
        phone.setText(safe(currentUser.phone));
        email.setText(safe(currentUser.email));
        form.addView(name); form.addView(avatar); form.addView(uploadAvatar); form.addView(phone); form.addView(email);
        uploadAvatar.setOnClickListener(v -> { uploadTarget = avatar; imagePicker.launch("image/*"); });
        new AlertDialog.Builder(this).setTitle("\u7f16\u8f91\u4e2a\u4eba\u4fe1\u606f").setView(form)
                .setNegativeButton("\u53d6\u6d88", null)
                .setPositiveButton("\u4fdd\u5b58", (d, w) -> {
                    Map<String, Object> body = new HashMap<>();
                    body.put("id", currentUser.id);
                    body.put("username", currentUser.username);
                    body.put("password", currentUser.password);
                    body.put("name", name.getText().toString());
                    body.put("avatar", avatar.getText().toString());
                    body.put("phone", phone.getText().toString());
                    body.put("email", email.getText().toString());
                    body.put("role", ROLE_USER);
                    call(api.updateUser(body), x -> {
                        currentUser.name = name.getText().toString();
                        currentUser.avatar = avatar.getText().toString();
                        currentUser.phone = phone.getText().toString();
                        currentUser.email = email.getText().toString();
                        getSharedPreferences("session", MODE_PRIVATE).edit().putString("user", gson.toJson(currentUser)).apply();
                        toast("\u4fdd\u5b58\u6210\u529f");
                        showMe();
                    });
                }).show();
    }
    private void showMineArticles() {
        LinearLayout p = verticalPage(); p.addView(titleBar("\u6211\u7684\u5e16\u5b50"));
        LinearLayout list = listBox(p); setPage(scroll(p));
        call(api.articlePage(1, 50, null, null, currentUser.id, currentUser.id), page -> {
            list.removeAllViews();
            if (page.list == null || page.list.isEmpty()) list.addView(empty("\u6682\u65e0\u5e16\u5b50"));
            else for (Article article : page.list) list.addView(mineArticleCard(article));
        });
    }

    private void showMineItems() {
        LinearLayout p = verticalPage(); p.addView(titleBar("\u6211\u7684\u7269\u54c1"));
        LinearLayout list = listBox(p); setPage(scroll(p));
        call(api.itemPage(1, 50, null, null, null, null, currentUser.id, currentUser.id), page -> {
            list.removeAllViews();
            if (page.list == null || page.list.isEmpty()) list.addView(empty("\u6682\u65e0\u7269\u54c1"));
            else for (Item item : page.list) list.addView(mineItemCard(item));
        });
    }
    private void showCollects() { LinearLayout p = verticalPage(); p.addView(titleBar("\u6211\u7684\u6536\u85cf\u5939")); LinearLayout list = listBox(p); setPage(scroll(p)); loadItems(list, null, null, null, true); }

    private View mineArticleCard(Article article) {
        LinearLayout card = card();
        card.addView(text(article.title, 18, true));
        card.addView(text("\u5ba1\u6838\u72b6\u6001: " + safe(article.status) + " | \u53d1\u5e03\u65f6\u95f4: " + safe(article.time), 13, false));
        if (STATUS_REJECT.equals(article.status)) card.addView(text("\u62d2\u7edd\u7406\u7531: " + safe(article.reason), 13, false));
        card.addView(text(safe(article.description), 14, false));
        MaterialButton detail = outlineButton("\u67e5\u770b\u8be6\u60c5");
        card.addView(detail, topLp(8));
        detail.setOnClickListener(v -> showArticleDetail(article));
        return card;
    }

    private View mineItemCard(Item item) {
        LinearLayout card = card();
        card.addView(image(item.img, 120));
        card.addView(text(item.name, 18, true));
        card.addView(text("\u5ba1\u6838\u72b6\u6001: " + safe(item.checkStatus), 13, false));
        if (STATUS_REJECT.equals(item.checkStatus)) card.addView(text("\u62d2\u7edd\u7406\u7531: " + safe(item.reason), 13, false));
        card.addView(text("\u4e0a\u67b6\u72b6\u6001: " + (Boolean.TRUE.equals(item.status) ? "\u5df2\u4e0a\u67b6" : "\u672a\u4e0a\u67b6"), 13, false));
        card.addView(text(safe(item.description), 14, false));
        LinearLayout actions = row();
        MaterialButton detail = outlineButton("\u8be6\u60c5");
        MaterialButton status = STATUS_PASS.equals(item.checkStatus)
                ? (Boolean.TRUE.equals(item.status) ? outlineButton("\u4e0b\u67b6") : button("\u4e0a\u67b6"))
                : outlineButton("\u5ba1\u6838\u540e\u53ef\u4e0a\u67b6");
        actions.addView(detail, new LinearLayout.LayoutParams(0, dp(42), 1));
        actions.addView(status, new LinearLayout.LayoutParams(0, dp(42), 1));
        card.addView(actions, topLp(8));
        detail.setOnClickListener(v -> showItemDetail(item));
        status.setOnClickListener(v -> {
            if (!STATUS_PASS.equals(item.checkStatus)) {
                toast("\u7269\u54c1\u5c1a\u672a\u901a\u8fc7\u5ba1\u6838\uff0c\u4e0d\u80fd\u4e0a\u67b6");
                return;
            }
            Map<String, Object> body = new HashMap<>();
            body.put("id", item.id);
            body.put("status", !Boolean.TRUE.equals(item.status));
            call(api.updateItemStatus(body), x -> { toast("\u72b6\u6001\u5df2\u66f4\u65b0"); showMineItems(); });
        });
        return card;
    }

    private <T> void call(Call<ApiResponse<T>> call, Consumer<T> ok) {
        call.enqueue(new Callback<ApiResponse<T>>() {
            @Override public void onResponse(Call<ApiResponse<T>> call, Response<ApiResponse<T>> response) { ApiResponse<T> body = response.body(); if (body != null && body.ok()) ok.accept(body.data); else toast(body == null ? "request failed" : body.msg); }
            @Override public void onFailure(Call<ApiResponse<T>> call, Throwable t) { toast("network error: " + t.getMessage()); }
        });
    }

    private void setPage(View v) { container.removeAllViews(); container.addView(v, lp(-1, -1)); }
    private ScrollView scroll(View child) { ScrollView s = new ScrollView(this); s.addView(child); return s; }
    private LinearLayout verticalPage() { LinearLayout l = new LinearLayout(this); l.setOrientation(LinearLayout.VERTICAL); l.setBackgroundColor(0xfff6f7f9); return l; }
    private LinearLayout row() { LinearLayout l = new LinearLayout(this); l.setGravity(Gravity.CENTER_VERTICAL); l.setOrientation(LinearLayout.HORIZONTAL); return l; }
    private LinearLayout formLayout() { LinearLayout l = new LinearLayout(this); l.setOrientation(LinearLayout.VERTICAL); l.setPadding(dp(16), dp(8), dp(16), dp(8)); return l; }
    private LinearLayout listBox(LinearLayout page) { LinearLayout list = new LinearLayout(this); list.setOrientation(LinearLayout.VERTICAL); list.setPadding(dp(12), 0, dp(12), dp(16)); page.addView(list); return list; }
    private TextView titleBar(String s) { TextView t = text(s, 24, true); t.setPadding(dp(16), dp(16), dp(16), dp(10)); return t; }
    private TextView sectionTitle(String s) { TextView t = text(s, 18, true); t.setPadding(dp(16), dp(16), dp(16), dp(8)); return t; }
    private TextView empty(String s) { TextView t = text(s, 16, false); t.setGravity(Gravity.CENTER); t.setPadding(0, dp(40), 0, dp(40)); return t; }
    private LinearLayout card() { LinearLayout c = new LinearLayout(this); c.setOrientation(LinearLayout.VERTICAL); c.setPadding(dp(14), dp(14), dp(14), dp(14)); c.setBackgroundColor(0xffffffff); c.setLayoutParams(topLp(10)); return c; }
    private View simpleCard(String title, String body) { LinearLayout c = card(); c.addView(text(title, 16, true)); c.addView(text(body, 13, false)); return c; }
    private ImageView image(String url, int height) { ImageView iv = new ImageView(this); iv.setScaleType(ImageView.ScaleType.FIT_CENTER); iv.setAdjustViewBounds(true); iv.setBackgroundColor(0xffeeeeee); iv.setLayoutParams(lp(-1, dp(height))); if (url != null && !url.isEmpty()) Glide.with(this).load(normalizeUrl(url)).into(iv); return iv; }
    private View avatar(String url, int size) {
        FrameLayout wrap = new FrameLayout(this);
        wrap.setPadding(0, dp(4), 0, dp(8));
        ImageView iv = new ImageView(this);
        iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
        iv.setBackgroundColor(0xffeeeeee);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(dp(size), dp(size), Gravity.CENTER);
        wrap.addView(iv, params);
        if (url != null && !url.isEmpty()) Glide.with(this).load(normalizeUrl(url)).circleCrop().into(iv);
        return wrap;
    }
    private TextView text(String s, int sp, boolean bold) { TextView t = new TextView(this); t.setText(s == null ? "" : s); t.setTextSize(sp); t.setTextColor(0xff222222); if (bold) t.setTypeface(android.graphics.Typeface.DEFAULT_BOLD); t.setPadding(0, dp(4), 0, dp(4)); return t; }
    private EditText input(String hint) { EditText e = new EditText(this); e.setHint(hint); e.setSingleLine(false); e.setBackgroundColor(0xffffffff); e.setPadding(dp(12), dp(8), dp(12), dp(8)); return e; }
    private MaterialButton button(String s) { MaterialButton b = new MaterialButton(this); b.setText(s); return b; }
    private MaterialButton outlineButton(String s) { MaterialButton b = new MaterialButton(this); b.setText(s); b.setStrokeWidth(dp(1)); b.setStrokeColorResource(android.R.color.darker_gray); return b; }
    private LinearLayout.LayoutParams topLp(int top) { LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2); p.setMargins(0, dp(top), 0, 0); return p; }
    private LinearLayout.LayoutParams lp(int w, int h) { return new LinearLayout.LayoutParams(w, h); }
    private int dp(int v) { return (int) (v * getResources().getDisplayMetrics().density + 0.5f); }
    private int intValue(Integer i) { return i == null ? 0 : i; }
    private String safe(String s) { return s == null ? "" : s; }
    private String stripImages(String html) { return safe(html).replaceAll("(?is)<img[^>]*>", ""); }
    private void addContentImages(LinearLayout box, String html) {
        java.util.regex.Matcher matcher = java.util.regex.Pattern
                .compile("<img[^>]+src=[\"']([^\"']+)[\"'][^>]*>", java.util.regex.Pattern.CASE_INSENSITIVE)
                .matcher(safe(html));
        while (matcher.find()) {
            box.addView(image(matcher.group(1), 190), topLp(8));
        }
    }
    private String normalizeUrl(String url) {
        if (url == null) return null;
        String trimmed = url.trim();
        if (trimmed.isEmpty()) return trimmed;

        Uri baseUri = Uri.parse(ApiClient.BASE_URL);
        Uri parsed;
        if (trimmed.startsWith("/")) {
            parsed = Uri.parse(ApiClient.BASE_URL).buildUpon().encodedPath(trimmed).build();
        } else {
            parsed = Uri.parse(trimmed);
        }

        if ("localhost".equals(parsed.getHost()) || "127.0.0.1".equals(parsed.getHost()) || parsed.getScheme() == null) {
            Uri.Builder builder = new Uri.Builder()
                    .scheme(baseUri.getScheme())
                    .encodedAuthority(baseUri.getEncodedAuthority());
            for (String segment : parsed.getPathSegments()) {
                builder.appendPath(segment);
            }
            if (parsed.getEncodedQuery() != null) builder.encodedQuery(parsed.getEncodedQuery());
            return builder.build().toString();
        }

        Uri.Builder builder = new Uri.Builder()
                .scheme(parsed.getScheme())
                .encodedAuthority(parsed.getEncodedAuthority());
        for (String segment : parsed.getPathSegments()) {
            builder.appendPath(segment);
        }
        if (parsed.getEncodedQuery() != null) builder.encodedQuery(parsed.getEncodedQuery());
        return builder.build().toString();
    }
    private void toast(String s) { Toast.makeText(this, s, Toast.LENGTH_SHORT).show(); }
}
