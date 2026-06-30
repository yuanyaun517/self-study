package cn.only.hw.secondmarketserver.service.impl;

import cn.only.hw.secondmarketserver.dao.ForumCommentDao;
import cn.only.hw.secondmarketserver.dto.ForumCommentDto;
import cn.only.hw.secondmarketserver.entity.Forum;
import cn.only.hw.secondmarketserver.entity.ForumComment;
import cn.only.hw.secondmarketserver.entity.User;
import cn.only.hw.secondmarketserver.service.ForumCommentService;
import cn.only.hw.secondmarketserver.service.ForumService;
import cn.only.hw.secondmarketserver.service.UserService;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ForumCommentServiceImpl extends ServiceImpl<ForumCommentDao, ForumComment> implements ForumCommentService {

    private static final int NORMAL_STATUS = 1;
    private static final int DELETED_STATUS = 0;
    private static final int MAX_COMMENT_LENGTH = 500;

    @Autowired
    private ForumService forumService;

    @Autowired
    private UserService userService;

    @Override
    public List<ForumCommentDto> listByForumId(Integer forumId) {
        if (forumId == null) {
            return Collections.emptyList();
        }

        LambdaQueryWrapper<ForumComment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ForumComment::getForumId, forumId)
                .eq(ForumComment::getStatus, NORMAL_STATUS)
                .orderByAsc(ForumComment::getCreateTime)
                .orderByAsc(ForumComment::getId);
        List<ForumComment> commentList = list(queryWrapper);
        if (commentList.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Integer, ForumComment> commentMap = commentList.stream()
                .filter(item -> item.getId() != null)
                .collect(Collectors.toMap(ForumComment::getId, item -> item, (left, right) -> left, LinkedHashMap::new));

        Map<Integer, User> publicUserMap = buildPublicUserMap(commentList);
        Map<Integer, ForumCommentDto> dtoMap = new LinkedHashMap<>();
        for (ForumComment comment : commentList) {
            ForumCommentDto forumCommentDto = new ForumCommentDto();
            BeanUtils.copyProperties(comment, forumCommentDto);
            forumCommentDto.setUser(publicUserMap.getOrDefault(comment.getUserId(), new User()));
            forumCommentDto.setReplyToUser(publicUserMap.getOrDefault(comment.getReplyToUserId(), new User()));
            forumCommentDto.setReplies(new ArrayList<>());
            dtoMap.put(comment.getId(), forumCommentDto);
        }

        List<ForumCommentDto> rootComments = new ArrayList<>();
        for (ForumComment comment : commentList) {
            ForumCommentDto currentDto = dtoMap.get(comment.getId());
            if (currentDto == null) {
                continue;
            }

            Integer rootParentId = resolveRootParentId(comment, commentMap);
            if (rootParentId == null) {
                rootComments.add(currentDto);
                continue;
            }

            ForumCommentDto parentDto = dtoMap.get(rootParentId);
            if (parentDto == null || rootParentId.equals(comment.getId())) {
                rootComments.add(currentDto);
                continue;
            }

            parentDto.getReplies().add(currentDto);
        }
        return rootComments;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ForumComment createComment(ForumComment forumComment) {
        if (forumComment == null) {
            throw new IllegalArgumentException("评论信息不能为空");
        }
        if (forumComment.getForumId() == null) {
            throw new IllegalArgumentException("帖子ID不能为空");
        }
        if (forumComment.getUserId() == null) {
            throw new IllegalArgumentException("评论用户不能为空");
        }

        Forum forum = forumService.getById(forumComment.getForumId());
        if (forum == null) {
            throw new IllegalArgumentException("帖子不存在");
        }
        if (!"1".equals(forum.getManage())) {
            throw new IllegalStateException("当前帖子暂不支持评论");
        }
        if (userService.getById(forumComment.getUserId()) == null) {
            throw new IllegalArgumentException("评论用户不存在");
        }

        ForumComment commentToSave = new ForumComment();
        BeanUtils.copyProperties(forumComment, commentToSave);
        commentToSave.setContent(normalizeContent(forumComment.getContent()));
        commentToSave.setStatus(NORMAL_STATUS);

        if (commentToSave.getParentId() != null) {
            ForumComment parentComment = getVisibleComment(commentToSave.getParentId());
            if (parentComment == null) {
                throw new IllegalArgumentException("被回复的评论不存在");
            }
            if (!commentToSave.getForumId().equals(parentComment.getForumId())) {
                throw new IllegalArgumentException("评论与帖子不匹配");
            }

            Integer rootParentId = resolveRootParentId(parentComment);
            commentToSave.setParentId(rootParentId == null ? parentComment.getId() : rootParentId);

            Integer replyToUserId = commentToSave.getReplyToUserId();
            if (replyToUserId == null) {
                commentToSave.setReplyToUserId(parentComment.getUserId());
            } else if (userService.getById(replyToUserId) == null) {
                throw new IllegalArgumentException("回复目标用户不存在");
            }
        } else {
            commentToSave.setParentId(null);
            commentToSave.setReplyToUserId(null);
        }

        if (!save(commentToSave)) {
            throw new IllegalStateException("评论提交失败");
        }
        return commentToSave;
    }

    @Override
    public long countVisibleByForumId(Integer forumId) {
        if (forumId == null) {
            return 0L;
        }

        LambdaQueryWrapper<ForumComment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ForumComment::getForumId, forumId)
                .eq(ForumComment::getStatus, NORMAL_STATUS);
        return count(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeCommentCascadeById(Integer commentId) {
        if (commentId == null) {
            return false;
        }

        ForumComment targetComment = getVisibleComment(commentId);
        if (targetComment == null) {
            return false;
        }

        List<Integer> idsToDelete = new ArrayList<>();
        idsToDelete.add(targetComment.getId());

        if (targetComment.getParentId() == null) {
            LambdaQueryWrapper<ForumComment> childQueryWrapper = new LambdaQueryWrapper<>();
            childQueryWrapper.eq(ForumComment::getForumId, targetComment.getForumId())
                    .eq(ForumComment::getParentId, targetComment.getId())
                    .eq(ForumComment::getStatus, NORMAL_STATUS);
            List<ForumComment> childComments = list(childQueryWrapper);
            for (ForumComment childComment : childComments) {
                if (childComment.getId() != null) {
                    idsToDelete.add(childComment.getId());
                }
            }
        }

        LambdaUpdateWrapper<ForumComment> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.in(ForumComment::getId, idsToDelete)
                .set(ForumComment::getStatus, DELETED_STATUS);
        return update(updateWrapper);
    }

    private String normalizeContent(String content) {
        String normalizedContent = content == null ? "" : content.trim();
        if (!StringUtils.hasText(normalizedContent)) {
            throw new IllegalArgumentException("评论内容不能为空");
        }
        if (normalizedContent.length() > MAX_COMMENT_LENGTH) {
            throw new IllegalArgumentException("评论内容不能超过500字");
        }
        return normalizedContent;
    }

    private ForumComment getVisibleComment(Integer commentId) {
        ForumComment forumComment = super.getById(commentId);
        if (forumComment == null) {
            return null;
        }
        return NORMAL_STATUS == (forumComment.getStatus() == null ? 0 : forumComment.getStatus())
                ? forumComment
                : null;
    }

    private Integer resolveRootParentId(ForumComment forumComment) {
        if (forumComment == null) {
            return null;
        }
        Integer parentId = forumComment.getParentId();
        Set<Integer> visitedIds = new HashSet<>();
        while (parentId != null) {
            if (!visitedIds.add(parentId)) {
                return null;
            }
            ForumComment parentComment = super.getById(parentId);
            if (parentComment == null) {
                return null;
            }
            if (parentComment.getParentId() == null) {
                return parentComment.getId();
            }
            parentId = parentComment.getParentId();
        }
        return null;
    }

    private Integer resolveRootParentId(ForumComment forumComment, Map<Integer, ForumComment> commentMap) {
        if (forumComment == null) {
            return null;
        }
        Integer parentId = forumComment.getParentId();
        Set<Integer> visitedIds = new HashSet<>();
        while (parentId != null) {
            if (!visitedIds.add(parentId)) {
                return null;
            }
            ForumComment parentComment = commentMap.get(parentId);
            if (parentComment == null) {
                return null;
            }
            if (parentComment.getParentId() == null) {
                return parentComment.getId();
            }
            parentId = parentComment.getParentId();
        }
        return null;
    }

    private Map<Integer, User> buildPublicUserMap(List<ForumComment> commentList) {
        Set<Integer> userIds = new HashSet<>();
        for (ForumComment comment : commentList) {
            if (comment.getUserId() != null) {
                userIds.add(comment.getUserId());
            }
            if (comment.getReplyToUserId() != null) {
                userIds.add(comment.getReplyToUserId());
            }
        }
        if (userIds.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<Integer, User> publicUserMap = new HashMap<>();
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(User::getId, userIds);
        for (User user : userService.list(queryWrapper)) {
            User publicUser = buildPublicUser(user);
            if (publicUser.getId() != null) {
                publicUserMap.put(publicUser.getId(), publicUser);
            }
        }
        return publicUserMap;
    }

    private User buildPublicUser(User user) {
        User publicUser = new User();
        if (user == null) {
            return publicUser;
        }
        publicUser.setId(user.getId());
        publicUser.setAccount(user.getAccount());
        publicUser.setNickname(user.getNickname());
        publicUser.setSex(user.getSex());
        publicUser.setCollege(user.getCollege());
        publicUser.setGrade(user.getGrade());
        publicUser.setIcon(user.getIcon());
        return publicUser;
    }
}
