package com.example.campusgoodiessharingplatform;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.campusgoodiessharingplatform.model.Article;
import com.example.campusgoodiessharingplatform.model.Notice;

import java.util.ArrayList;
import java.util.List;

public class HotFragment extends BaseFragment {
    private FrameLayout root;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = (FrameLayout) inflater.inflate(R.layout.fragment_hot, container, false);
        render();
        return root;
    }

    private void render() {
        LinearLayout page = verticalPage();
        page.addView(titleBar("\u70ed\u95e8"));
        LinearLayout notices = listBox(page);
        page.addView(sectionTitle("\u5e16\u5b50\u70b9\u8d5e\u699c"));
        LinearLayout rank = listBox(page);
        setRoot(root, scroll(page));
        call(api().notices(1, 5), p -> {
            notices.removeAllViews();
            if (p.list != null) for (Notice n : p.list) notices.addView(simpleCard(n.title, safe(n.content) + "\n" + safe(n.time)));
        });
        loadHotArticles(rank);
    }

    private void loadHotArticles(LinearLayout list) {
        list.removeAllViews();
        list.addView(text("loading...", 14, false));
        call(api().articlePage(1, 50, null, STATUS_PASS, null, currentUser().id), page -> {
            list.removeAllViews();
            List<Article> articles = page.list == null ? new ArrayList<>() : page.list;
            articles.sort((a, b) -> intValue(b.likeCount) - intValue(a.likeCount));
            if (articles.isEmpty()) list.addView(empty("\u6682\u65e0\u5e16\u5b50"));
            else for (Article article : articles) list.addView(hotArticleRow(article));
        });
    }

    private View hotArticleRow(Article article) {
        LinearLayout row = row();
        row.setPadding(dp(12), dp(10), dp(12), dp(10));
        row.setBackgroundColor(0xffffffff);
        row.setLayoutParams(topLp(1));
        TextView title = text(article.title, 15, true);
        title.setSingleLine(true);
        title.setEllipsize(TextUtils.TruncateAt.END);
        TextView heat = text("\u70ed\u5ea6 " + intValue(article.likeCount), 13, false);
        heat.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        row.addView(title, new LinearLayout.LayoutParams(0, dp(36), 1));
        row.addView(heat, new LinearLayout.LayoutParams(dp(88), dp(36)));
        row.setOnClickListener(v -> host().openArticleDetail(article.id));
        return row;
    }
}
