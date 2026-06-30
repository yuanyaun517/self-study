package cn.only.hw.secondmarketserver.dto;

import cn.only.hw.secondmarketserver.entity.ForumComment;
import cn.only.hw.secondmarketserver.entity.User;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ForumCommentDto extends ForumComment {
    private User user = new User();

    private User replyToUser = new User();

    private List<ForumCommentDto> replies = new ArrayList<>();
}
