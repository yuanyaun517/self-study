package cn.only.hw.secondmarketserver.service;

import cn.only.hw.secondmarketserver.dto.ForumCommentDto;
import cn.only.hw.secondmarketserver.entity.ForumComment;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface ForumCommentService extends IService<ForumComment> {

    List<ForumCommentDto> listByForumId(Integer forumId);

    ForumComment createComment(ForumComment forumComment);

    long countVisibleByForumId(Integer forumId);

    boolean removeCommentCascadeById(Integer commentId);
}
