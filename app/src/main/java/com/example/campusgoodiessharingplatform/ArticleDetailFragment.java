package com.example.campusgoodiessharingplatform;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
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
        FrameLayout authorAvatar = root.findViewById(R.id.article_author_avatar);
        TextView authorInfo = root.findViewById(R.id.article_author_info);
        TextView time = root.findViewById(R.id.article_time);
        TextView stats = root.findViewById(R.id.article_stats);
        MaterialButton like = root.findViewById(R.id.article_like);
        TextView content = root.findViewById(R.id.article_content);
        LinearLayout contentImages = root.findViewById(R.id.article_content_images);
        String articleContent = article.content == null ? safe(article.description) : article.content;
        if (article.img != null && !article.img.isEmpty()) com.bumptech.glide.Glide.with(this).load(normalizeUrl(article.img)).into(image);
        styleActionButton(back, "‹", false);
        back.setTextSize(30);
        title.setText(article.title);
        authorAvatar.removeAllViews();
        authorAvatar.addView(roundImage(article.avatar, 46));
        authorInfo.setText(safe(article.userName) + "\nID: " + (article.userId == null ? "" : article.userId));
        time.setText("发布时间: " + safe(article.time));
        stats.setText("赞 " + intValue(article.likeCount) + " | 评论 " + intValue(article.commentCount));
        styleLikeButton(like, article.likedId != null);
        content.setText(Html.fromHtml(stripImages(articleContent), Html.FROM_HTML_MODE_LEGACY));
        contentImages.removeAllViews();
        addContentImages(contentImages, articleContent);
        EditText comment = root.findViewById(R.id.comment_input);
        MaterialButton send = root.findViewById(R.id.comment_send);
        LinearLayout comments = root.findViewById(R.id.comment_list);
        back.setOnClickListener(v -> host().openShareHome());
        like.setOnClickListener(v -> toggleLike(article));
        send.setOnClickListener(v -> {
            String contentValue = comment.getText().toString().trim();
            if (contentValue.length() < 2) {
                toast("评论至少需要2个字");
                return;
            }
            Map<String, Object> body = new HashMap<>();
            body.put("userId", currentUser().id);
            body.put("articleId", article.id);
            body.put("content", contentValue);
            call(api().addComment(body), x -> { toast("评论成功"); loadArticle(); });
        });
        loadArticleComments(comments, article);
    }

    private void styleLikeButton(MaterialButton like, boolean liked) {
        styleActionButton(like, liked ? "取消点赞" : "点赞", !liked);
    }

    private void toggleLike(Article article) {
        if (article.likedId == null) {
            Map<String, Object> body = new HashMap<>();
            body.put("userId", currentUser().id);
            body.put("articleId", article.id);
            call(api().like(body), x -> {
                toast("已点赞");
                loadArticle();
            });
        } else {
            call(api().unlike(article.likedId), x -> {
                toast("已取消");
                loadArticle();
            });
        }
    }

    private void loadArticleComments(LinearLayout comments, Article article) {
        call(api().comments(1, 20, article.id), page -> {
            comments.removeAllViews();
            if (page.list == null || page.list.isEmpty()) comments.addView(empty("暂无评论"));
            else for (Comment c : page.list) comments.addView(commentCard(c, article));
        });
    }

    private View commentCard(Comment c, Article article) {
        LinearLayout card = card();
        LinearLayout top = row();
        top.setGravity(android.view.Gravity.TOP);
        top.addView(roundImage(c.avatar, 34), new LinearLayout.LayoutParams(dp(34), dp(34)));
        TextView author = text(safe(c.userName) + "\nID: " + (c.userId == null ? "" : c.userId), 14, true);
        LinearLayout.LayoutParams authorParams = new LinearLayout.LayoutParams(0, -2, 1);
        authorParams.setMargins(dp(14), 0, 0, 0);
        top.addView(author, authorParams);
        card.addView(top);
        card.addView(text(safe(c.content), 14, false));
        card.addView(text(safe(c.time), 11, false));
        if (currentUser().id != null && currentUser().id.equals(c.userId)) {
            card.setOnLongClickListener(v -> {
                new AlertDialog.Builder(requireContext())
                        .setTitle("删除评论")
                        .setMessage("确定删除这条评论吗？")
                        .setNegativeButton("取消", null)
                        .setPositiveButton("删除", (d, w) -> call(api().deleteComment(c.id), x -> { toast("已删除"); loadArticle(); }))
                        .show();
                return true;
            });
        }
        return card;
    }
}
