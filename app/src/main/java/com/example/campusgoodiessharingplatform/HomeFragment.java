package com.example.campusgoodiessharingplatform;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.example.campusgoodiessharingplatform.model.Article;
import com.example.campusgoodiessharingplatform.model.Item;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeFragment extends BaseFragment {
    private static final String ARG_HOME_ITEMS = "home_items";
    private View root;
    private boolean homeItems = true;
    private String searchKeyword;
    private Integer restoreScrollY;

    public static HomeFragment newInstance(boolean homeItems) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_HOME_ITEMS, homeItems);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_home, container, false);
        if (getArguments() != null) homeItems = getArguments().getBoolean(ARG_HOME_ITEMS, true);
        render();
        return root;
    }

    private void render() {
        MaterialButton search = root.findViewById(R.id.home_search);
        MaterialButton market = root.findViewById(R.id.tab_market);
        MaterialButton share = root.findViewById(R.id.tab_share);
        LinearLayout list = root.findViewById(R.id.home_list);
        market.setText("市集");
        share.setText("分享");
        setTabSelected(market, homeItems);
        setTabSelected(share, !homeItems);
        market.setOnClickListener(v -> { homeItems = true; searchKeyword = null; render(); });
        share.setOnClickListener(v -> { homeItems = false; searchKeyword = null; render(); });
        search.setOnClickListener(v -> showSearchDialog());
        if (homeItems) loadItems(list, null, null, searchKeyword, false);
        else loadArticles(list, null, searchKeyword, false);
    }

    private void renderKeepingScroll() {
        ScrollView scroll = root.findViewById(R.id.home_scroll);
        restoreScrollY = scroll == null ? null : scroll.getScrollY();
        render();
    }

    private void restoreScrollIfNeeded() {
        if (restoreScrollY == null) return;
        ScrollView scroll = root.findViewById(R.id.home_scroll);
        int y = restoreScrollY;
        restoreScrollY = null;
        if (scroll != null) scroll.post(() -> scroll.scrollTo(0, y));
    }

    private void showSearchDialog() {
        EditText input = input(homeItems ? "搜索物品" : "搜索帖子");
        new AlertDialog.Builder(requireContext()).setTitle("搜索").setView(input)
                .setNegativeButton("取消", null)
                .setPositiveButton("搜索", (d, w) -> { searchKeyword = input.getText().toString(); render(); }).show();
    }

    private void loadItems(LinearLayout list, Integer userId, Integer categoryId, String keyword, boolean onlyCollected) {
        list.removeAllViews();
        list.addView(text("loading...", 14, false));
        call(api().itemPage(1, 50, keyword, true, STATUS_PASS, categoryId, userId, currentUser().id), page -> {
            list.removeAllViews();
            if (page.list == null || page.list.isEmpty()) list.addView(empty("暂无物品"));
            else for (Item item : page.list) if (!onlyCollected || item.collectId != null) list.addView(itemCard(item));
            restoreScrollIfNeeded();
        });
    }

    private void loadArticles(LinearLayout list, Integer userId, String keyword, boolean hotSort) {
        list.removeAllViews();
        list.addView(text("loading...", 14, false));
        call(api().articlePage(1, 50, keyword, STATUS_PASS, userId, currentUser().id), page -> {
            list.removeAllViews();
            List<Article> articles = page.list == null ? new ArrayList<>() : page.list;
            if (hotSort) articles.sort((a, b) -> intValue(b.likeCount) - intValue(a.likeCount));
            if (articles.isEmpty()) list.addView(empty("暂无帖子"));
            else for (Article article : articles) list.addView(articleCard(article));
            restoreScrollIfNeeded();
        });
    }

    private View itemCard(Item item) {
        LinearLayout card = card();
        card.addView(image(item.img, 190));
        card.addView(text(item.name, 18, true));
        card.addView(text((item.categoryName == null ? "未分类" : item.categoryName) + "  |  收藏 " + intValue(item.collectCount), 12, false));
        card.addView(text(item.description, 14, false));
        card.addView(text("交换条件: " + safe(item.requirement), 13, false));
        LinearLayout actions = row();
        MaterialButton collect = item.collectId == null ? outlineButton("收藏") : button("取消收藏");
        MaterialButton detail = outlineButton("详情");
        MaterialButton exchange = outlineButton("申请交换");
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
        card.addView(text("发布人: " + safe(article.userName) + "  |  赞 " + intValue(article.likeCount) + "  评论 " + intValue(article.commentCount), 12, false));
        card.addView(text(article.description, 14, false));
        LinearLayout actions = row();
        MaterialButton like = article.likedId == null ? outlineButton("点赞") : button("取消点赞");
        MaterialButton detail = outlineButton("详情/评论");
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
            call(api().like(body), x -> { toast("已点赞"); renderKeepingScroll(); });
        } else call(api().unlike(article.likedId), x -> { toast("已取消"); renderKeepingScroll(); });
    }

    private void toggleCollect(Item item) {
        if (item.collectId == null) {
            Map<String, Object> body = new HashMap<>();
            body.put("userId", currentUser().id); body.put("itemId", item.id);
            call(api().collect(body), x -> { toast("已收藏"); renderKeepingScroll(); });
        } else call(api().uncollect(item.collectId), x -> { toast("已取消收藏"); renderKeepingScroll(); });
    }

    private void showExchangeDialog(Item item) {
        LinearLayout form = formLayout();
        EditText content = input("我提供的交换物品");
        EditText remark = input("交换理由");
        form.addView(text(item.name, 18, true));
        form.addView(content, topLp(8));
        form.addView(remark, topLp(8));
        new AlertDialog.Builder(requireContext()).setTitle("申请交换")
                .setView(form)
                .setNegativeButton("取消", null)
                .setPositiveButton("提交", (d, w) -> {
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
                    call(api().addCharge(body), x -> toast("申请已提交"));
                }).show();
    }
}
