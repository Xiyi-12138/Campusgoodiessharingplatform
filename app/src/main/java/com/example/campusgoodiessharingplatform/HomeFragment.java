package com.example.campusgoodiessharingplatform;

import android.app.AlertDialog;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.os.Bundle;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.campusgoodiessharingplatform.model.Category;
import com.example.campusgoodiessharingplatform.model.Article;
import com.example.campusgoodiessharingplatform.model.Item;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends BaseFragment {
    private static final String ARG_HOME_ITEMS = "home_items";
    private View root;
    private boolean homeItems = true;
    private String searchKeyword;
    private Integer selectedCategoryId;
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
        HorizontalScrollView categoryScroll = root.findViewById(R.id.home_category_scroll);
        market.setText("市集");
        share.setText("分享");
        styleHomeTab(market, homeItems);
        styleHomeTab(share, !homeItems);
        styleIconButton(search);
        market.setOnClickListener(v -> { homeItems = true; searchKeyword = null; selectedCategoryId = null; render(); });
        share.setOnClickListener(v -> { homeItems = false; searchKeyword = null; selectedCategoryId = null; render(); });
        search.setOnClickListener(v -> showSearchDialog());
        categoryScroll.setVisibility(homeItems ? View.VISIBLE : View.GONE);
        if (homeItems) {
            loadCategories();
            loadItems(list, null, selectedCategoryId, searchKeyword, false);
        } else {
            loadArticles(list, null, searchKeyword, false);
        }
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

    private void loadCategories() {
        LinearLayout categoryList = root.findViewById(R.id.home_category_list);
        categoryList.removeAllViews();
        addCategoryButton(categoryList, "全部", null);
        call(api().categories(), categories -> {
            categoryList.removeAllViews();
            addCategoryButton(categoryList, "全部", null);
            if (categories == null || categories.isEmpty()) return;
            for (Category category : categories) {
                if (category == null || category.id == null) continue;
                addCategoryButton(categoryList, safe(category.name).isEmpty() ? "未命名分类" : category.name, category.id);
            }
        });
    }

    private void addCategoryButton(LinearLayout categoryList, String name, Integer categoryId) {
        MaterialButton button = new MaterialButton(requireContext());
        boolean selected = categoryId == null ? selectedCategoryId == null : categoryId.equals(selectedCategoryId);
        button.setText(name);
        button.setTextSize(16);
        button.setTypeface(selected ? Typeface.DEFAULT_BOLD : Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        button.setTextColor(selected ? 0xff111827 : 0xffA0A7B4);
        button.setBackgroundTintList(ColorStateList.valueOf(0x00000000));
        button.setStrokeWidth(0);
        button.setMinWidth(0);
        button.setMinHeight(0);
        button.setMinimumWidth(0);
        button.setMinimumHeight(0);
        button.setInsetTop(0);
        button.setInsetBottom(0);
        button.setPadding(dp(4), 0, dp(4), 0);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-2, dp(34));
        params.setMargins(0, 0, dp(14), 0);
        categoryList.addView(button, params);
        button.setOnClickListener(v -> {
            selectedCategoryId = categoryId;
            render();
        });
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
        card.addView(cardImage(item.img, 190));
        card.addView(text(item.name, 20, true), topLp(8));
        TextView meta = text((item.categoryName == null ? "未分类" : item.categoryName) + "      ♡ " + intValue(item.collectCount), 13, false);
        meta.setTextColor(0xff9AA2B1);
        card.addView(meta);
        TextView desc = text(item.description, 16, false);
        desc.setTextColor(0xff5B6472);
        card.addView(desc);
        card.addView(requirementText(safe(item.requirement), 13));
        card.setOnClickListener(v -> showItemDetail(item, this::renderKeepingScroll));
        return card;
    }

    private View articleCard(Article article) {
        LinearLayout card = card();
        card.addView(cardImage(article.img, 160));
        card.addView(text(article.title, 20, true), topLp(8));
        TextView meta = text("发布人: " + safe(article.userName) + "           ♡ " + intValue(article.likeCount) + "   ▢ " + intValue(article.commentCount), 13, false);
        meta.setTextColor(0xff9AA2B1);
        card.addView(meta);
        TextView desc = text(article.description, 16, false);
        desc.setTextColor(0xff5B6472);
        card.addView(desc);
        card.setOnClickListener(v -> host().openArticleDetail(article.id));
        return card;
    }

    private void styleHomeTab(MaterialButton button, boolean selected) {
        button.setTextColor(selected ? 0xff3f51b5 : 0xff9AA2B1);
        button.setTextSize(19);
        button.setTypeface(Typeface.DEFAULT, selected ? Typeface.BOLD : Typeface.NORMAL);
        button.setPaintFlags(selected ? (button.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG) : (button.getPaintFlags() & ~Paint.UNDERLINE_TEXT_FLAG));
        button.setBackgroundTintList(ColorStateList.valueOf(0x00000000));
        button.setStrokeWidth(0);
        button.setMinWidth(0);
        button.setMinimumWidth(0);
        button.setInsetTop(0);
        button.setInsetBottom(0);
        button.setPadding(0, 0, 0, 0);
    }

    private void styleIconButton(MaterialButton button) {
        button.setTextColor(0xff4B5563);
        button.setBackgroundTintList(ColorStateList.valueOf(0x00000000));
        button.setStrokeWidth(0);
        button.setMinWidth(0);
        button.setMinimumWidth(0);
        button.setInsetTop(0);
        button.setInsetBottom(0);
    }
}
