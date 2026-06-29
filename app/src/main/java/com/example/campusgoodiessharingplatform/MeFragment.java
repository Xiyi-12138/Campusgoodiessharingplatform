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
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.example.campusgoodiessharingplatform.model.Article;
import com.example.campusgoodiessharingplatform.model.Item;
import com.google.android.material.button.MaterialButton;

import java.util.HashMap;
import java.util.Map;

public class MeFragment extends BaseFragment {
    private View root;
    private EditText uploadTarget;
    private ActivityResultLauncher<String> imagePicker;
    private MaterialButton myArticles;
    private MaterialButton myItems;
    private MaterialButton collects;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imagePicker = registerForActivityResult(new ActivityResultContracts.GetContent(), this::handlePickedImage);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_me, container, false);
        render();
        return root;
    }

    private void handlePickedImage(Uri uri) {
        if (uri != null && uploadTarget != null) uploadImage(uri, uploadTarget);
    }

    private void render() {
        FrameLayout avatarBox = root.findViewById(R.id.me_avatar_container);
        avatarBox.removeAllViews();
        avatarBox.addView(avatar(currentUser().avatar, 76));
        TextView name = root.findViewById(R.id.me_name);
        TextView info = root.findViewById(R.id.me_info);
        name.setText(safe(currentUser().name));
        info.setText("账号: " + safe(currentUser().username) + "\n手机: " + safe(currentUser().phone) + "\n邮箱: " + safe(currentUser().email));
        MaterialButton profile = root.findViewById(R.id.me_edit_profile);
        myArticles = root.findViewById(R.id.me_articles);
        myItems = root.findViewById(R.id.me_items);
        collects = root.findViewById(R.id.me_collects);
        MaterialButton logout = root.findViewById(R.id.me_logout);
        LinearLayout list = root.findViewById(R.id.me_content_list);
        list.removeAllViews();
        updateTabState(null);
        profile.setOnClickListener(v -> showProfileDialog());
        myArticles.setOnClickListener(v -> showMineArticles());
        myItems.setOnClickListener(v -> showMineItems());
        collects.setOnClickListener(v -> showCollects());
        logout.setOnClickListener(v -> host().logout());
    }

    private void showProfileDialog() {
        LinearLayout form = formLayout();
        EditText name = input("昵称");
        EditText avatar = input("头像 URL");
        EditText phone = input("手机号");
        EditText email = input("邮箱");
        MaterialButton uploadAvatar = outlineButton("选择本地头像");
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
        new AlertDialog.Builder(requireContext()).setTitle("编辑个人信息").setView(form)
                .setNegativeButton("取消", null)
                .setPositiveButton("保存", (d, w) -> {
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
                        toast("保存成功");
                        render();
                    });
                }).show();
    }

    public void showMineArticles() {
        updateTabState(myArticles);
        TextView title = root.findViewById(R.id.me_title);
        title.setText("我的帖子");
        LinearLayout list = root.findViewById(R.id.me_content_list);
        call(api().articlePage(1, 50, null, null, currentUser().id, currentUser().id), page -> {
            list.removeAllViews();
            if (page.list == null || page.list.isEmpty()) list.addView(empty("暂无帖子"));
            else for (Article article : page.list) list.addView(mineArticleCard(article));
        });
    }

    public void showMineItems() {
        updateTabState(myItems);
        TextView title = root.findViewById(R.id.me_title);
        title.setText("我的物品");
        LinearLayout list = root.findViewById(R.id.me_content_list);
        call(api().itemPage(1, 50, null, null, null, null, currentUser().id, currentUser().id), page -> {
            list.removeAllViews();
            if (page.list == null || page.list.isEmpty()) list.addView(empty("暂无物品"));
            else for (Item item : page.list) list.addView(mineItemCard(item));
        });
    }

    private void showCollects() {
        updateTabState(collects);
        TextView title = root.findViewById(R.id.me_title);
        title.setText("我的收藏夹");
        LinearLayout list = root.findViewById(R.id.me_content_list);
        call(api().itemPage(1, 50, null, true, STATUS_PASS, null, null, currentUser().id), page -> {
            list.removeAllViews();
            if (page.list == null || page.list.isEmpty()) list.addView(empty("暂无收藏"));
            else for (Item item : page.list) if (item.collectId != null) list.addView(collectItemCard(item));
        });
    }

    private View mineArticleCard(Article article) {
        LinearLayout card = card();
        card.addView(text(article.title, 18, true));
        card.addView(text("审核状态: " + safe(article.status) + " | 发布时间: " + safe(article.time), 13, false));
        if (STATUS_REJECT.equals(article.status)) card.addView(text("拒绝理由: " + safe(article.reason), 13, false));
        card.addView(text(safe(article.description), 14, false));
        LinearLayout actions = row();
        MaterialButton detail = outlineButton("查看详情");
        MaterialButton delete = outlineButton("删除");
        actions.addView(detail, new LinearLayout.LayoutParams(0, dp(42), 1));
        actions.addView(delete, new LinearLayout.LayoutParams(0, dp(42), 1));
        card.addView(actions, topLp(8));
        detail.setOnClickListener(v -> host().openArticleDetail(article.id));
        delete.setOnClickListener(v -> confirmDeleteArticle(article));
        return card;
    }

    private View mineItemCard(Item item) {
        LinearLayout card = card();
        card.addView(image(item.img, 120));
        card.addView(text(item.name, 18, true));
        card.addView(text("审核状态: " + safe(item.checkStatus), 13, false));
        if (STATUS_REJECT.equals(item.checkStatus)) card.addView(text("拒绝理由: " + safe(item.reason), 13, false));
        card.addView(text("上架状态: " + (Boolean.TRUE.equals(item.status) ? "已上架" : "未上架"), 13, false));
        card.addView(text(safe(item.description), 14, false));
        LinearLayout actions = row();
        MaterialButton detail = outlineButton("详情");
        MaterialButton status = STATUS_PASS.equals(item.checkStatus)
                ? (Boolean.TRUE.equals(item.status) ? outlineButton("下架") : button("上架"))
                : outlineButton("审核后可上架");
        MaterialButton delete = outlineButton("删除");
        actions.addView(detail, new LinearLayout.LayoutParams(0, dp(42), 1));
        actions.addView(status, new LinearLayout.LayoutParams(0, dp(42), 1));
        actions.addView(delete, new LinearLayout.LayoutParams(0, dp(42), 1));
        card.addView(actions, topLp(8));
        detail.setOnClickListener(v -> showItemDetail(item));
        delete.setOnClickListener(v -> confirmDeleteItem(item));
        status.setOnClickListener(v -> {
            if (!STATUS_PASS.equals(item.checkStatus)) {
                toast("物品尚未通过审核，不能上架");
                return;
            }
            Map<String, Object> body = new HashMap<>();
            body.put("id", item.id);
            body.put("status", !Boolean.TRUE.equals(item.status));
            call(api().updateItemStatus(body), x -> { toast("状态已更新"); showMineItems(); });
        });
        return card;
    }

    private View collectItemCard(Item item) {
        LinearLayout card = card();
        card.addView(image(item.img, 140));
        card.addView(text(item.name, 18, true));
        card.addView(text(safe(item.description), 14, false));
        LinearLayout actions = row();
        MaterialButton detail = outlineButton("详情");
        MaterialButton uncollect = outlineButton("取消收藏");
        actions.addView(detail, new LinearLayout.LayoutParams(0, dp(42), 1));
        actions.addView(uncollect, new LinearLayout.LayoutParams(0, dp(42), 1));
        card.addView(actions, topLp(8));
        detail.setOnClickListener(v -> showItemDetail(item));
        uncollect.setOnClickListener(v -> confirmUncollectItem(item));
        return card;
    }

    private void confirmDeleteArticle(Article article) {
        new AlertDialog.Builder(requireContext())
                .setTitle("删除帖子")
                .setMessage("确定删除这篇帖子吗？")
                .setNegativeButton("取消", null)
                .setPositiveButton("删除", (dialog, which) ->
                        call(api().deleteArticle(article.id), x -> {
                            toast("已删除");
                            showMineArticles();
                        }))
                .show();
    }

    private void confirmDeleteItem(Item item) {
        new AlertDialog.Builder(requireContext())
                .setTitle("删除物品")
                .setMessage("确定删除这件物品吗？")
                .setNegativeButton("取消", null)
                .setPositiveButton("删除", (dialog, which) ->
                        call(api().deleteItem(item.id), x -> {
                            toast("已删除");
                            showMineItems();
                        }))
                .show();
    }

    private void confirmUncollectItem(Item item) {
        if (item.collectId == null) {
            toast("收藏记录不存在");
            return;
        }
        new AlertDialog.Builder(requireContext())
                .setTitle("取消收藏")
                .setMessage("确定将这件物品移出收藏夹吗？")
                .setNegativeButton("取消", null)
                .setPositiveButton("确定", (dialog, which) ->
                        call(api().uncollect(item.collectId), x -> {
                            toast("已取消收藏");
                            showCollects();
                        }))
                .show();
    }

    private void updateTabState(MaterialButton selected) {
        if (myArticles == null || myItems == null || collects == null) return;
        setTabSelected(myArticles, myArticles == selected);
        setTabSelected(myItems, myItems == selected);
        setTabSelected(collects, collects == selected);
    }
}
