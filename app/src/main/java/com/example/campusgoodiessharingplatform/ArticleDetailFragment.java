package com.example.campusgoodiessharingplatform;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.campusgoodiessharingplatform.model.Article;
import com.example.campusgoodiessharingplatform.model.Comment;
import com.google.android.material.button.MaterialButton;

import java.util.HashMap;
import java.util.Map;

public class ArticleDetailFragment extends BaseFragment {
    private static final String ARG_ARTICLE_ID = "article_id";
    private View root;
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
        root = inflater.inflate(R.layout.fragment_article_detail, container, false);
        articleId = getArguments() == null ? 0 : getArguments().getInt(ARG_ARTICLE_ID);
        loadArticle();
        return root;
    }

    private void loadArticle() {
        call(api().articleById(articleId, currentUser().id), this::render);
    }

    private void render(Article article) {
        MaterialButton back = root.findViewById(R.id.article_back);
        ImageView image = root.findViewById(R.id.article_image);
        TextView title = root.findViewById(R.id.article_title);
        TextView time = root.findViewById(R.id.article_time);
        TextView stats = root.findViewById(R.id.article_stats);
        TextView content = root.findViewById(R.id.article_content);
        LinearLayout contentImages = root.findViewById(R.id.article_content_images);
        String articleContent = article.content == null ? safe(article.description) : article.content;
        if (article.img != null && !article.img.isEmpty()) com.bumptech.glide.Glide.with(this).load(normalizeUrl(article.img)).into(image);
        title.setText(article.title);
        time.setText("\u53d1\u5e03\u65f6\u95f4: " + safe(article.time));
        stats.setText("\u8d5e " + intValue(article.likeCount) + " | \u8bc4\u8bba " + intValue(article.commentCount));
        content.setText(Html.fromHtml(stripImages(articleContent), Html.FROM_HTML_MODE_LEGACY));
        contentImages.removeAllViews();
        addContentImages(contentImages, articleContent);
        EditText comment = root.findViewById(R.id.comment_input);
        MaterialButton send = root.findViewById(R.id.comment_send);
        LinearLayout comments = root.findViewById(R.id.comment_list);
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
