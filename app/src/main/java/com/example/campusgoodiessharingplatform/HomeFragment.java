package com.example.campusgoodiessharingplatform;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.example.campusgoodiessharingplatform.model.Article;
import com.example.campusgoodiessharingplatform.model.Item;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeFragment extends BaseFragment {
    private View root;
    private boolean homeItems = true;
    private String searchKeyword;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_home, container, false);
        render();
        return root;
    }

    private void render() {
        MaterialButton search = root.findViewById(R.id.home_search);
        MaterialButton market = root.findViewById(R.id.tab_market);
        MaterialButton share = root.findViewById(R.id.tab_share);
        LinearLayout list = root.findViewById(R.id.home_list);
        market.setText("\u5e02\u96c6");
        share.setText("\u5206\u4eab");
        setTabSelected(market, homeItems);
        setTabSelected(share, !homeItems);
        market.setOnClickListener(v -> { homeItems = true; searchKeyword = null; render(); });
        share.setOnClickListener(v -> { homeItems = false; searchKeyword = null; render(); });
        search.setOnClickListener(v -> showSearchDialog());
        if (homeItems) loadItems(list, null, null, searchKeyword, false);
        else loadArticles(list, null, searchKeyword, false);
    }

    private void showSearchDialog() {
        EditText input = input(homeItems ? "\u641c\u7d22\u7269\u54c1" : "\u641c\u7d22\u5e16\u5b50");
        new AlertDialog.Builder(requireContext()).setTitle("\u641c\u7d22").setView(input)
                .setNegativeButton("\u53d6\u6d88", null)
                .setPositiveButton("\u641c\u7d22", (d, w) -> { searchKeyword = input.getText().toString(); render(); }).show();
    }

    private void loadItems(LinearLayout list, Integer userId, Integer categoryId, String keyword, boolean onlyCollected) {
        list.removeAllViews();
        list.addView(text("loading...", 14, false));
        call(api().itemPage(1, 50, keyword, true, STATUS_PASS, categoryId, userId, currentUser().id), page -> {
            list.removeAllViews();
            if (page.list == null || page.list.isEmpty()) list.addView(empty("\u6682\u65e0\u7269\u54c1"));
            else for (Item item : page.list) if (!onlyCollected || item.collectId != null) list.addView(itemCard(item));
        });
    }

    private void loadArticles(LinearLayout list, Integer userId, String keyword, boolean hotSort) {
        list.removeAllViews();
        list.addView(text("loading...", 14, false));
        call(api().articlePage(1, 50, keyword, STATUS_PASS, userId, currentUser().id), page -> {
            list.removeAllViews();
            List<Article> articles = page.list == null ? new ArrayList<>() : page.list;
            if (hotSort) articles.sort((a, b) -> intValue(b.likeCount) - intValue(a.likeCount));
            if (articles.isEmpty()) list.addView(empty("\u6682\u65e0\u5e16\u5b50"));
            else for (Article article : articles) list.addView(articleCard(article));
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
        if (item.userId == null || !item.userId.equals(currentUser().id)) actions.addView(exchange, new LinearLayout.LayoutParams(0, dp(42), 1));
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
        detail.setOnClickListener(v -> host().openArticleDetail(article.id));
        return card;
    }

    private void toggleLike(Article article) {
        if (article.likedId == null) {
            Map<String, Object> body = new HashMap<>();
            body.put("userId", currentUser().id); body.put("articleId", article.id);
            call(api().like(body), x -> { toast("\u5df2\u70b9\u8d5e"); render(); });
        } else call(api().unlike(article.likedId), x -> { toast("\u5df2\u53d6\u6d88"); render(); });
    }

    private void toggleCollect(Item item) {
        if (item.collectId == null) {
            Map<String, Object> body = new HashMap<>();
            body.put("userId", currentUser().id); body.put("itemId", item.id);
            call(api().collect(body), x -> { toast("\u5df2\u6536\u85cf"); render(); });
        } else call(api().uncollect(item.collectId), x -> { toast("\u5df2\u53d6\u6d88\u6536\u85cf"); render(); });
    }

    private void showExchangeDialog(Item item) {
        LinearLayout form = formLayout();
        EditText content = input("\u6211\u63d0\u4f9b\u7684\u4ea4\u6362\u7269\u54c1");
        EditText remark = input("\u4ea4\u6362\u7406\u7531");
        form.addView(text(item.name, 18, true));
        form.addView(content, topLp(8));
        form.addView(remark, topLp(8));
        new AlertDialog.Builder(requireContext()).setTitle("\u7533\u8bf7\u4ea4\u6362")
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
                    body.put("userId", currentUser().id);
                    body.put("content", contentValue);
                    body.put("remark", remarkValue);
                    call(api().addCharge(body), x -> toast("\u7533\u8bf7\u5df2\u63d0\u4ea4"));
                }).show();
    }
}
