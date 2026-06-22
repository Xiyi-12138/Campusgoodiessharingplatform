package com.example.campusgoodiessharingplatform;

import android.app.AlertDialog;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.example.campusgoodiessharingplatform.model.Article;
import com.example.campusgoodiessharingplatform.model.Item;
import com.google.android.material.button.MaterialButton;

import java.util.HashMap;
import java.util.Map;

public class MeFragment extends BaseFragment {
    private FrameLayout root;
    private EditText uploadTarget;
    private ActivityResultLauncher<String> imagePicker;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imagePicker = registerForActivityResult(new ActivityResultContracts.GetContent(), this::handlePickedImage);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = (FrameLayout) inflater.inflate(R.layout.fragment_me, container, false);
        render();
        return root;
    }

    private void handlePickedImage(Uri uri) {
        if (uri != null && uploadTarget != null) uploadImage(uri, uploadTarget);
    }

    private void render() {
        LinearLayout page = verticalPage();
        page.addView(titleBar("\u6211\u7684"));
        page.addView(avatar(currentUser().avatar, 76), topLp(4));
        page.addView(simpleCard(safe(currentUser().name), "\u8d26\u53f7: " + safe(currentUser().username) + "\n\u624b\u673a: " + safe(currentUser().phone) + "\n\u90ae\u7bb1: " + safe(currentUser().email)));
        MaterialButton profile = button("\u7f16\u8f91\u4e2a\u4eba\u4fe1\u606f");
        MaterialButton myArticles = outlineButton("\u6211\u7684\u5e16\u5b50");
        MaterialButton myItems = outlineButton("\u6211\u7684\u7269\u54c1");
        MaterialButton collects = outlineButton("\u6211\u7684\u6536\u85cf\u5939");
        MaterialButton logout = outlineButton("\u9000\u51fa\u767b\u5f55");
        page.addView(profile, topLp(8));
        page.addView(myArticles, topLp(8));
        page.addView(myItems, topLp(8));
        page.addView(collects, topLp(8));
        page.addView(logout, topLp(16));
        setRoot(root, scroll(page));
        profile.setOnClickListener(v -> showProfileDialog());
        myArticles.setOnClickListener(v -> showMineArticles());
        myItems.setOnClickListener(v -> showMineItems());
        collects.setOnClickListener(v -> showCollects());
        logout.setOnClickListener(v -> host().logout());
    }

    private void showProfileDialog() {
        LinearLayout form = formLayout();
        EditText name = input("\u6635\u79f0");
        EditText avatar = input("\u5934\u50cf URL");
        EditText phone = input("\u624b\u673a\u53f7");
        EditText email = input("\u90ae\u7bb1");
        MaterialButton uploadAvatar = outlineButton("\u9009\u62e9\u672c\u5730\u5934\u50cf");
        name.setText(safe(currentUser().name));
        avatar.setText(safe(currentUser().avatar));
        phone.setText(safe(currentUser().phone));
        email.setText(safe(currentUser().email));
        form.addView(name);
        form.addView(avatar);
        form.addView(uploadAvatar);
        form.addView(phone);
        form.addView(email);
        uploadAvatar.setOnClickListener(v -> { uploadTarget = avatar; imagePicker.launch("image/*"); });
        new AlertDialog.Builder(requireContext()).setTitle("\u7f16\u8f91\u4e2a\u4eba\u4fe1\u606f").setView(form)
                .setNegativeButton("\u53d6\u6d88", null)
                .setPositiveButton("\u4fdd\u5b58", (d, w) -> {
                    Map<String, Object> body = new HashMap<>();
                    body.put("id", currentUser().id);
                    body.put("username", currentUser().username);
                    body.put("password", currentUser().password);
                    body.put("name", name.getText().toString());
                    body.put("avatar", avatar.getText().toString());
                    body.put("phone", phone.getText().toString());
                    body.put("email", email.getText().toString());
                    body.put("role", ROLE_USER);
                    call(api().updateUser(body), x -> {
                        currentUser().name = name.getText().toString();
                        currentUser().avatar = avatar.getText().toString();
                        currentUser().phone = phone.getText().toString();
                        currentUser().email = email.getText().toString();
                        host().saveCurrentUser();
                        toast("\u4fdd\u5b58\u6210\u529f");
                        render();
                    });
                }).show();
    }

    public void showMineArticles() {
        LinearLayout p = verticalPage();
        p.addView(titleBar("\u6211\u7684\u5e16\u5b50"));
        LinearLayout list = listBox(p);
        setRoot(root, scroll(p));
        call(api().articlePage(1, 50, null, null, currentUser().id, currentUser().id), page -> {
            list.removeAllViews();
            if (page.list == null || page.list.isEmpty()) list.addView(empty("\u6682\u65e0\u5e16\u5b50"));
            else for (Article article : page.list) list.addView(mineArticleCard(article));
        });
    }

    public void showMineItems() {
        LinearLayout p = verticalPage();
        p.addView(titleBar("\u6211\u7684\u7269\u54c1"));
        LinearLayout list = listBox(p);
        setRoot(root, scroll(p));
        call(api().itemPage(1, 50, null, null, null, null, currentUser().id, currentUser().id), page -> {
            list.removeAllViews();
            if (page.list == null || page.list.isEmpty()) list.addView(empty("\u6682\u65e0\u7269\u54c1"));
            else for (Item item : page.list) list.addView(mineItemCard(item));
        });
    }

    private void showCollects() {
        LinearLayout p = verticalPage();
        p.addView(titleBar("\u6211\u7684\u6536\u85cf\u5939"));
        LinearLayout list = listBox(p);
        setRoot(root, scroll(p));
        call(api().itemPage(1, 50, null, true, STATUS_PASS, null, null, currentUser().id), page -> {
            list.removeAllViews();
            if (page.list == null || page.list.isEmpty()) list.addView(empty("\u6682\u65e0\u6536\u85cf"));
            else for (Item item : page.list) if (item.collectId != null) list.addView(collectItemCard(item));
        });
    }

    private View mineArticleCard(Article article) {
        LinearLayout card = card();
        card.addView(text(article.title, 18, true));
        card.addView(text("\u5ba1\u6838\u72b6\u6001: " + safe(article.status) + " | \u53d1\u5e03\u65f6\u95f4: " + safe(article.time), 13, false));
        if (STATUS_REJECT.equals(article.status)) card.addView(text("\u62d2\u7edd\u7406\u7531: " + safe(article.reason), 13, false));
        card.addView(text(safe(article.description), 14, false));
        MaterialButton detail = outlineButton("\u67e5\u770b\u8be6\u60c5");
        card.addView(detail, topLp(8));
        detail.setOnClickListener(v -> host().openArticleDetail(article.id));
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
            call(api().updateItemStatus(body), x -> { toast("\u72b6\u6001\u5df2\u66f4\u65b0"); showMineItems(); });
        });
        return card;
    }

    private View collectItemCard(Item item) {
        LinearLayout card = card();
        card.addView(image(item.img, 140));
        card.addView(text(item.name, 18, true));
        card.addView(text(safe(item.description), 14, false));
        MaterialButton detail = outlineButton("\u8be6\u60c5");
        card.addView(detail, topLp(8));
        detail.setOnClickListener(v -> showItemDetail(item));
        return card;
    }
}
