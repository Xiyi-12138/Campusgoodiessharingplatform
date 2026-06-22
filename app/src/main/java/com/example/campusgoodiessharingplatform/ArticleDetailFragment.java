package com.example.campusgoodiessharingplatform;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.campusgoodiessharingplatform.model.Article;
import com.example.campusgoodiessharingplatform.model.Comment;
import com.google.android.material.button.MaterialButton;

import java.util.HashMap;
import java.util.Map;

public class ArticleDetailFragment extends BaseFragment {
    private static final String ARG_ARTICLE_ID = "article_id";
    private FrameLayout root;
    private int articleId;

    public static ArticleDetailFragment newInstance(int articleId) {
        ArticleDetailFragment fragment = new ArticleDetailFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_ARTICLE_ID, articleId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = (FrameLayout) inflater.inflate(R.layout.fragment_article_detail, container, false);
        articleId = getArguments() == null ? 0 : getArguments().getInt(ARG_ARTICLE_ID);
        loadArticle();
        return root;
    }

    private void loadArticle() {
        call(api().articleById(articleId, currentUser().id), this::render);
    }

    private void render(Article article) {
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
        LinearLayout comments = new LinearLayout(requireContext());
        comments.setOrientation(LinearLayout.VERTICAL);
        commentBox.addView(comments, topLp(10));
        page.addView(commentBox);

        setRoot(root, scroll(page));
        back.setOnClickListener(v -> host().openHome());
        send.setOnClickListener(v -> {
            String contentValue = comment.getText().toString().trim();
            if (contentValue.length() < 2) {
                toast("\u8bc4\u8bba\u81f3\u5c11\u9700\u89812\u4e2a\u5b57");
                return;
            }
            Map<String, Object> body = new HashMap<>();
            body.put("userId", currentUser().id);
            body.put("articleId", article.id);
            body.put("content", contentValue);
            call(api().addComment(body), x -> { toast("\u8bc4\u8bba\u6210\u529f"); loadArticle(); });
        });
        loadArticleComments(comments, article);
    }

    private void loadArticleComments(LinearLayout comments, Article article) {
        call(api().comments(1, 20, article.id), page -> {
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
        if (currentUser().id != null && currentUser().id.equals(c.userId)) {
            card.setOnLongClickListener(v -> {
                new AlertDialog.Builder(requireContext())
                        .setTitle("\u5220\u9664\u8bc4\u8bba")
                        .setMessage("\u786e\u5b9a\u5220\u9664\u8fd9\u6761\u8bc4\u8bba\u5417\uff1f")
                        .setNegativeButton("\u53d6\u6d88", null)
                        .setPositiveButton("\u5220\u9664", (d, w) -> call(api().deleteComment(c.id), x -> { toast("\u5df2\u5220\u9664"); loadArticle(); }))
                        .show();
                return true;
            });
        }
        return card;
    }
}
