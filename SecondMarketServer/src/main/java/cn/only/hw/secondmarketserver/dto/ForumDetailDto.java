package cn.only.hw.secondmarketserver.dto;

import cn.only.hw.secondmarketserver.entity.Forum;
import cn.only.hw.secondmarketserver.entity.User;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ForumDetailDto extends Forum {
    private User user = new User();

    private List<String> imgList = new ArrayList<>();

    private Integer commentCount = 0;
}
