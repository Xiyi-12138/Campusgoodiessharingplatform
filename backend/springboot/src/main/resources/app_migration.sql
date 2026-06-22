CREATE TABLE IF NOT EXISTS `notification` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NULL DEFAULT NULL COMMENT '接收用户ID',
  `actor_id` int NULL DEFAULT NULL COMMENT '触发用户ID',
  `type` varchar(50) NULL DEFAULT NULL COMMENT 'like/comment/collect',
  `target_type` varchar(50) NULL DEFAULT NULL COMMENT 'article/item',
  `target_id` int NULL DEFAULT NULL COMMENT '目标ID',
  `content` varchar(500) NULL DEFAULT NULL COMMENT '消息内容',
  `time` varchar(255) NULL DEFAULT NULL COMMENT '时间',
  `is_read` tinyint(1) NULL DEFAULT 0 COMMENT '是否已读',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
