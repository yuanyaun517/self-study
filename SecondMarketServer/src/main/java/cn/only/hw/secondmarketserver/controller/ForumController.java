package cn.only.hw.secondmarketserver.controller;

import cn.only.hw.secondmarketserver.dto.ForumCommentDto;
import cn.only.hw.secondmarketserver.dto.ForumDetailDto;
import cn.only.hw.secondmarketserver.entity.Forum;
import cn.only.hw.secondmarketserver.entity.ForumComment;
import cn.only.hw.secondmarketserver.entity.User;
import cn.only.hw.secondmarketserver.service.ForumCommentService;
import cn.only.hw.secondmarketserver.service.ForumService;
import cn.only.hw.secondmarketserver.service.UserService;
import cn.only.hw.secondmarketserver.util.Result;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 论坛控制器
 * 处理论坛帖子和评论相关的HTTP请求，包括发帖、评论、搜索等功能
 */
@RestController
@RequestMapping("/forum")
@Slf4j
@Api(tags = "Forum")
public class ForumController {

    /**
     * 论坛服务
     */
    @Autowired
    private ForumService forumService;

    /**
     * 论坛评论服务
     */
    @Autowired
    private ForumCommentService forumCommentService;

    /**
     * 用户服务
     */
    @Autowired
    private UserService userService;

    @ApiOperation("List approved forum posts")
    @PostMapping("/list")
    public Result<List<Forum>> login() {
        log.info("List approved forum posts");
        LambdaQueryWrapper<Forum> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Forum::getManage, "1");
        List<Forum> list = forumService.list(queryWrapper);
        return Result.success(list);
    }

    @ApiOperation("Get forum post detail")
    @PostMapping("/detail")
    public Result<ForumDetailDto> detail(Integer id) {
        log.info("Get forum post detail by id: {}", id);
        if (id == null) {
            return Result.error("forum id is empty");
        }

        Forum forum = forumService.getById(id);
        if (forum == null) {
            return Result.error("No data");
        }

        ForumDetailDto forumDetailDto = new ForumDetailDto();
        BeanUtils.copyProperties(forum, forumDetailDto);
        forumDetailDto.setImgList(splitImgList(forum.getImgs()));
        forumDetailDto.setCommentCount((int) forumCommentService.countVisibleByForumId(id));
        if (forum.getSendUser() != null) {
            forumDetailDto.setUser(buildPublicUser(userService.getById(forum.getSendUser())));
        }
        return Result.success(forumDetailDto);
    }

    @ApiOperation("Get forum post by id")
    @PostMapping("/getById")
    public Result<Forum> getById(Integer id) {
        log.info("Get forum post by id: {}", id);
        Forum forum = forumService.getById(id);
        if (forum != null) {
            Result<Forum> result = Result.success(forum);
            if (forum.getImgs() != null && !forum.getImgs().trim().isEmpty()) {
                String[] imgStrs = forum.getImgs().split(",");
                result.add("imgs", Arrays.asList(imgStrs));
            }
            return result;
        }
        return Result.error("No data");
    }

    @ApiOperation("List forum comments by forum id")
    @PostMapping("/comment/list")
    public Result<List<ForumCommentDto>> listComments(Integer forumId) {
        log.info("List forum comments by forum id: {}", forumId);
        if (forumId == null) {
            return Result.error("forum id is empty");
        }
        return Result.success(forumCommentService.listByForumId(forumId));
    }

    @ApiOperation("Create forum comment")
    @PostMapping("/comment/save")
    public Result<String> saveComment(@RequestBody ForumComment forumComment) {
        log.info("Create forum comment: {}", forumComment);
        try {
            forumCommentService.createComment(forumComment);
            return Result.success("评论提交成功");
        } catch (IllegalArgumentException | IllegalStateException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("Create forum comment failed", e);
            return Result.error("评论提交失败");
        }
    }

    @ApiOperation("Get forum posts by user id")
    @PostMapping("/getByUserId")
    public Result<List<Forum>> getByUserId(Integer userid) {
        log.info("Get forum posts by user id: {}", userid);
        LambdaQueryWrapper<Forum> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Forum::getSendUser, userid);
        List<Forum> list = forumService.list(queryWrapper);
        return Result.success(list);
    }

    @ApiOperation("Get forum posts by type")
    @PostMapping("/getByType")
    public Result<List<Forum>> getByType(String type) {
        log.info("Get forum posts by type: {}", type);
        List<Forum> list = forumService.getByType(type);
        return Result.success(list);
    }

    @ApiOperation("Delete forum post")
    @PostMapping("/del")
    public Result<String> del(String id) {
        log.info("Delete forum post: {}", id);
        boolean removed = forumService.removeById(id);
        if (removed) {
            return Result.success("Delete success");
        }
        return Result.error("Delete failed");
    }

    @ApiOperation("Create forum post")
    @PostMapping("/save")
    public Result<String> save(@RequestBody Forum forum) {
        log.info("Create forum post: {}", forum);
        forum.setManage("0");
        boolean saved = forumService.save(forum);
        if (saved) {
            return Result.success("Create success");
        }
        return Result.error("Create failed");
    }

    @ApiOperation("Search forum posts")
    @PostMapping("/searchForum")
    public Result<List<Forum>> searchForum(String type, String title, String content) {
        log.info("Search forum posts: {}, {}, {}", type, title, content);
        LambdaQueryWrapper<Forum> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Forum::getManage, "1");
        queryWrapper.and(wrapper -> wrapper.like(Forum::getType, type)
                .or().like(Forum::getTitle, title)
                .or().like(Forum::getContent, content));
        List<Forum> list = forumService.list(queryWrapper);
        return Result.success(list);
    }

    private List<String> splitImgList(String imgs) {
        if (imgs == null || imgs.trim().isEmpty()) {
            return Collections.emptyList();
        }
        String[] imgStrs = imgs.split(",");
        List<String> imgList = new ArrayList<>();
        for (String img : imgStrs) {
            if (img != null && !img.trim().isEmpty()) {
                imgList.add(img.trim());
            }
        }
        return imgList;
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
